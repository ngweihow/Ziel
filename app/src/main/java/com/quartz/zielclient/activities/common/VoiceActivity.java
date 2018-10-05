package com.quartz.zielclient.activities.common;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.quartz.zielclient.R;
import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.CallInvite;
import com.twilio.voice.RegistrationException;
import com.twilio.voice.RegistrationListener;
import com.twilio.voice.Voice;

import java.util.HashMap;

public class VoiceActivity extends AppCompatActivity {

  public static final String INCOMING_CALL_INVITE = "INCOMING_CALL_INVITE";
  public static final String INCOMING_CALL_NOTIFICATION_ID = "INCOMING_CALL_NOTIFICATION_ID";
  public static final String ACTION_INCOMING_CALL = "ACTION_INCOMING_CALL";
  public static final String ACTION_FCM_TOKEN = "ACTION_FCM_TOKEN";
  private static final String TAG = "VoiceActivity";

  // Server for access token
  private static final String TWILIO_ACCESS_TOKEN_SERVER_URL =
      "http://35.189.54.26:3000/accessToken";
  private static final int MIC_PERMISSION_REQUEST_CODE = 1;
  private static final int SNACKBAR_DURATION = 4000;
  private static String identity = "alice";
  private static Call activeCall;
  private static String toCall;
  // Empty HashMap, never populated for the Quickstart
  HashMap<String, String> twiMLParams = new HashMap<>();
  private String accessToken;
  private AudioManager audioManager;
  private int savedAudioMode = AudioManager.MODE_INVALID;
  private boolean isReceiverRegistered = false;
  private VoiceBroadcastReceiver voiceBroadcastReceiver;
  private CoordinatorLayout coordinatorLayout;
  RegistrationListener registrationListener = registrationListener();
  private FloatingActionButton callActionFab;
  private FloatingActionButton hangupActionFab;
  private FloatingActionButton muteActionFab;
  private Chronometer chronometer;
  Call.Listener callListener = callListener();
  private SoundPoolManager soundPoolManager;
  private NotificationManager notificationManager;
  private AlertDialog alertDialog;
  private CallInvite activeCallInvite;
  private int activeCallNotificationId;

  public static AlertDialog createIncomingCallDialog(
      Context context,
      CallInvite callInvite,
      DialogInterface.OnClickListener answerCallClickListener,
      DialogInterface.OnClickListener cancelClickListener) {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
    alertDialogBuilder.setIcon(R.drawable.ic_call_black_24dp);
    alertDialogBuilder.setTitle("Incoming Call");
    alertDialogBuilder.setPositiveButton("Accept", answerCallClickListener);
    alertDialogBuilder.setNegativeButton("Reject", cancelClickListener);
    alertDialogBuilder.setMessage(callInvite.getFrom() + " is calling.");
    return alertDialogBuilder.create();
  }

  public static AlertDialog createCallDialog(
      final DialogInterface.OnClickListener callClickListener,
      final DialogInterface.OnClickListener cancelClickListener,
      final Context context) {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

    alertDialogBuilder.setIcon(R.drawable.ic_call_black_24dp);
    alertDialogBuilder.setTitle("Call");
    alertDialogBuilder.setPositiveButton("Call", callClickListener);
    alertDialogBuilder.setNegativeButton("Cancel", cancelClickListener);
    alertDialogBuilder.setCancelable(false);

    LayoutInflater li = LayoutInflater.from(context);
    View dialogView = li.inflate(R.layout.dialog_call, null);
    final EditText contact = (EditText) dialogView.findViewById(R.id.contact);
    contact.setText(toCall);
    contact.setHint(R.string.callee);
    alertDialogBuilder.setView(dialogView);

    return alertDialogBuilder.create();
  }

  public static void endCall() {
    if (activeCall != null) {
      activeCall.disconnect();
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_voice);

    // These flags ensure that the activity can be launched when the screen is locked.
    Window window = getWindow();
    window.addFlags(
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    coordinatorLayout = findViewById(R.id.coordinator_layout);
    callActionFab = findViewById(R.id.call_action_fab);
    hangupActionFab = findViewById(R.id.hangup_action_fab);
    muteActionFab = findViewById(R.id.mute_action_fab);
    chronometer = findViewById(R.id.chronometer);

    callActionFab.setOnClickListener(callActionFabClickListener());
    hangupActionFab.setOnClickListener(hangupActionFabClickListener());
    muteActionFab.setOnClickListener(muteActionFabClickListener());

    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    soundPoolManager = SoundPoolManager.getInstance(this);

    /*
     * Setup the broadcast receiver to be notified of FCM Token updates
     * or incoming call invite in this Activity.
     */
    voiceBroadcastReceiver = new VoiceBroadcastReceiver();
    registerReceiver();

    /*
     * Needed for setting/abandoning audio focus during a call
     */
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    audioManager.setSpeakerphoneOn(true);

    /*
     * Enable changing the volume using the up/down keys during a conversation
     */
    setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

    /*
     * Setup the UI
     */
    if (activeCall != null) {
      setCallUI();
    } else {
      resetUI();
    }
    /*
     * Displays a call dialog if the intent contains a call invite
     */
    handleIncomingCallIntent(getIntent());

    /*
     * Ensure the microphone permission is enabled
     */
    if (!checkPermissionForMicrophone()) {
      requestPermissionForMicrophone();
    } else {
      retrieveAccessToken();
    }
    if (getIntent().getIntExtra("initiate", 0) == 1) {
      identity = FirebaseAuth.getInstance().getUid();
      onBackPressed();
    } else {
      toCall = getIntent().getStringExtra("CallId");
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    handleIncomingCallIntent(intent);
  }

  private RegistrationListener registrationListener() {
    return new RegistrationListener() {
      @Override
      public void onRegistered(String accessToken, String fcmToken) {
        Log.d(TAG, "Successfully registered FCM " + fcmToken);
      }

      @Override
      public void onError(RegistrationException error, String accessToken, String fcmToken) {
        String message =
            String.format("Registration Error: %d, %s", error.getErrorCode(), error.getMessage());
        Log.e(TAG, message);
        Snackbar.make(coordinatorLayout, message, SNACKBAR_DURATION).show();
      }
    };
  }

  private Call.Listener callListener() {
    return new Call.Listener() {
      @Override
      public void onConnectFailure(Call call, CallException error) {
        setAudioFocus(false);
        Log.d(TAG, "Connect failure");
        String message =
            String.format("Call Error: %d, %s", error.getErrorCode(), error.getMessage());
        Log.e(TAG, message);
        Snackbar.make(coordinatorLayout, message, SNACKBAR_DURATION).show();
        resetUI();
      }

      @Override
      public void onConnected(Call call) {
        setAudioFocus(true);
        Log.d(TAG, "Connected");
        activeCall = call;
      }

      @Override
      public void onDisconnected(Call call, CallException error) {
        setAudioFocus(false);
        Log.d(TAG, "Disconnected");
        if (error != null) {
          String message =
              String.format("Call Error: %d, %s", error.getErrorCode(), error.getMessage());
          Log.e(TAG, message);
          Snackbar.make(coordinatorLayout, message, SNACKBAR_DURATION).show();
        }
        resetUI();
      }
    };
  }

  /*
   * The UI state when there is an active call
   */
  private void setCallUI() {
    callActionFab.hide();
    hangupActionFab.show();
    muteActionFab.show();
    chronometer.setVisibility(View.VISIBLE);
    chronometer.setBase(SystemClock.elapsedRealtime());
    chronometer.start();
  }

  /*
   * Reset UI elements
   */
  private void resetUI() {
    callActionFab.show();
    muteActionFab.setImageDrawable(
        ContextCompat.getDrawable(VoiceActivity.this, R.drawable.ic_mic_white_24dp));
    muteActionFab.hide();
    hangupActionFab.hide();
    chronometer.setVisibility(View.INVISIBLE);
    chronometer.stop();
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerReceiver();
  }

  @Override
  protected void onPause() {
    super.onPause();
    unregisterReceiver();
  }

  @Override
  public void onDestroy() {
    soundPoolManager.release();
    super.onDestroy();
  }

  private void handleIncomingCallIntent(Intent intent) {
    if (intent != null && intent.getAction() != null) {
      if (intent.getAction().equals(ACTION_INCOMING_CALL)) {
        activeCallInvite = intent.getParcelableExtra(INCOMING_CALL_INVITE);
        if (activeCallInvite != null && (activeCallInvite.getState() == CallInvite.State.PENDING)) {
          soundPoolManager.playRinging();
          alertDialog =
              createIncomingCallDialog(
                  VoiceActivity.this,
                  activeCallInvite,
                  answerCallClickListener(),
                  cancelCallClickListener());
          alertDialog.show();
          activeCallNotificationId = intent.getIntExtra(INCOMING_CALL_NOTIFICATION_ID, 0);
        } else {
          if (alertDialog != null && alertDialog.isShowing()) {
            soundPoolManager.stopRinging();
            alertDialog.cancel();
          }
        }
      } else if (intent.getAction().equals(ACTION_FCM_TOKEN)) {
        retrieveAccessToken();
      }
    }
  }

  private void registerReceiver() {
    if (!isReceiverRegistered) {
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(ACTION_INCOMING_CALL);
      intentFilter.addAction(ACTION_FCM_TOKEN);
      LocalBroadcastManager.getInstance(this)
          .registerReceiver(voiceBroadcastReceiver, intentFilter);
      isReceiverRegistered = true;
    }
  }

  private void unregisterReceiver() {
    if (isReceiverRegistered) {
      LocalBroadcastManager.getInstance(this).unregisterReceiver(voiceBroadcastReceiver);
      isReceiverRegistered = false;
    }
  }

  private DialogInterface.OnClickListener answerCallClickListener() {
    return (dialog, which) -> {
      soundPoolManager.stopRinging();
      answer();
      setCallUI();
      alertDialog.dismiss();
    };
  }

  private DialogInterface.OnClickListener callClickListener() {
    return (dialog, which) -> {
      // Place a call
      EditText contact = (EditText) ((AlertDialog) dialog).findViewById(R.id.contact);
      twiMLParams.put("to", contact.getText().toString());
      activeCall = Voice.call(VoiceActivity.this, accessToken, twiMLParams, callListener);
      setCallUI();
      alertDialog.dismiss();
    };
  }

  private DialogInterface.OnClickListener cancelCallClickListener() {
    return (dialogInterface, i) -> {
      soundPoolManager.stopRinging();
      if (activeCallInvite != null) {
        activeCallInvite.reject(VoiceActivity.this);
        notificationManager.cancel(activeCallNotificationId);
      }
      alertDialog.dismiss();
    };
  }

  /** Register for Call invites on the Cloud messaging serevr */
  private void registerForCallInvites() {
    final String fcmToken = FirebaseInstanceId.getInstance().getToken();
    if (fcmToken != null) {
      Log.i(TAG, "Registering with FCM");
      Voice.register(
          this, accessToken, Voice.RegistrationChannel.FCM, fcmToken, registrationListener);
    }
  }

  /**
   * Button to allow for calls
   *
   * @return
   */
  private View.OnClickListener callActionFabClickListener() {
    return v -> {
      alertDialog =
          createCallDialog(callClickListener(), cancelCallClickListener(), VoiceActivity.this);
      alertDialog.show();
    };
  }

  private View.OnClickListener hangupActionFabClickListener() {
    return v -> {
      soundPoolManager.playDisconnect();
      resetUI();
      disconnect();
    };
  }

  /**
   * Mute the current call
   *
   * @return
   */
  private View.OnClickListener muteActionFabClickListener() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mute();
      }
    };
  }

  /** Accept an incoming Call */
  private void answer() {
    activeCallInvite.accept(this, callListener);
    notificationManager.cancel(activeCallNotificationId);
  }

  /** Disconnect from Call */
  private void disconnect() {
    if (activeCall != null) {
      activeCall.disconnect();
      activeCall = null;
    }
  }

  /** Mute current call */
  private void mute() {
    if (activeCall != null) {
      boolean mute = !activeCall.isMuted();
      activeCall.mute(mute);
      if (mute) {
        muteActionFab.setImageDrawable(
            ContextCompat.getDrawable(VoiceActivity.this, R.drawable.ic_mic_white_off_24dp));
      } else {
        muteActionFab.setImageDrawable(
            ContextCompat.getDrawable(VoiceActivity.this, R.drawable.ic_mic_white_24dp));
      }
    }
  }

  /**
   * Allows from switching between loud and quiet speaker
   *
   * @param setFocus
   */
  private void setAudioFocus(boolean setFocus) {
    if (audioManager != null) {
      if (setFocus) {
        savedAudioMode = audioManager.getMode();
        // Request audio focus before making any device switch.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          AudioAttributes playbackAttributes =
              new AudioAttributes.Builder()
                  .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                  .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                  .build();
          AudioFocusRequest focusRequest =
              new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                  .setAudioAttributes(playbackAttributes)
                  .setAcceptsDelayedFocusGain(true)
                  .setOnAudioFocusChangeListener(
                      new AudioManager.OnAudioFocusChangeListener() {
                        @Override
                        public void onAudioFocusChange(int i) {}
                      })
                  .build();
          audioManager.requestAudioFocus(focusRequest);
        } else {
          audioManager.requestAudioFocus(
              null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        // set the mode the speaker by default
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
      } else {
        audioManager.setMode(savedAudioMode);
        audioManager.abandonAudioFocus(null);
      }
    }
  }

  /**
   * Check the MicroPhone Permissions
   *
   * @return
   */
  private boolean checkPermissionForMicrophone() {
    int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
    return resultMic == PackageManager.PERMISSION_GRANTED;
  }

  private void requestPermissionForMicrophone() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(
        this, Manifest.permission.RECORD_AUDIO)) {
      Snackbar.make(
              coordinatorLayout,
              "Microphone permissions needed. Please allow in your application settings.",
              SNACKBAR_DURATION)
          .show();
    } else {
      ActivityCompat.requestPermissions(
          this, new String[] {Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_REQUEST_CODE);
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    /*
     * Check if microphone permissions is granted
     */
    if (requestCode == MIC_PERMISSION_REQUEST_CODE && permissions.length > 0) {
      if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
        Snackbar.make(
                coordinatorLayout,
                "Microphone permissions needed. Please allow in your application settings.",
                SNACKBAR_DURATION)
            .show();
      } else {
        retrieveAccessToken();
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.speaker_menu_item:
        if (audioManager.isSpeakerphoneOn()) {
          audioManager.setSpeakerphoneOn(false);
          item.setIcon(R.drawable.ic_phonelink_ring_white_24dp);
        } else {
          audioManager.setSpeakerphoneOn(true);
          item.setIcon(R.drawable.ic_volume_up_white_24dp);
        }
        break;
    }
    return true;
  }

  /*
   * Get an access token from your Twilio access token server
   */
  private void retrieveAccessToken() {
    Ion.with(this)
        .load(TWILIO_ACCESS_TOKEN_SERVER_URL + "?identity=" + identity)
        .asString()
        .setCallback(
            new FutureCallback<String>() {
              @Override
              public void onCompleted(Exception e, String accessToken) {
                if (e == null) {
                  Log.d(TAG, "Access token: " + accessToken);
                  VoiceActivity.this.accessToken = accessToken;
                  registerForCallInvites();
                } else {
                  Snackbar.make(
                          coordinatorLayout,
                          "Error retrieving access token. Unable to make calls",
                          Snackbar.LENGTH_LONG)
                      .show();
                }
              }
            });
  }

  private class VoiceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action.equals(ACTION_INCOMING_CALL)) {
        /*
         * Handle the incoming call invite
         */
        handleIncomingCallIntent(intent);
      }
    }
  }
}
