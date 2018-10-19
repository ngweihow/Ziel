package com.quartz.zielclient.activities.channel;

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
import android.support.v7.app.ActionBar;
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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.koushikdutta.ion.Ion;
import com.quartz.zielclient.R;
import com.quartz.zielclient.channel.ChannelController;
import com.quartz.zielclient.channel.ChannelData;
import com.quartz.zielclient.channel.ChannelListener;
import com.quartz.zielclient.voip.SoundPoolManager;
import com.twilio.voice.Call;
import com.twilio.voice.CallException;
import com.twilio.voice.CallInvite;
import com.twilio.voice.CallState;
import com.twilio.voice.RegistrationException;
import com.twilio.voice.RegistrationListener;
import com.twilio.voice.Voice;

import java.util.HashMap;

/**
 * This class is responsible for handling voice activities within the app.
 */
public class VoiceActivity extends AppCompatActivity implements ChannelListener {

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
  private boolean firstRegistration = false;
  private static String identity = "alice";
  private static Call activeCall;
  private static String toCall;
  // Empty HashMap, never populated for the Quickstart
  private HashMap<String, String> twiMLParams = new HashMap<>();
  private String accessToken;
  private AudioManager audioManager;
  private int savedAudioMode = AudioManager.MODE_INVALID;
  private boolean isReceiverRegistered = false;
  private VoiceBroadcastReceiver voiceBroadcastReceiver;
  private CoordinatorLayout coordinatorLayout;
  private RegistrationListener registrationListener = registrationListener();
  private FloatingActionButton callActionFab;
  private FloatingActionButton hangupActionFab;
  private FloatingActionButton muteActionFab;
  private Chronometer chronometer;
  private Call.Listener callListener = callListener();
  private SoundPoolManager soundPoolManager;
  private NotificationManager notificationManager;
  private AlertDialog alertDialog;
  private CallInvite activeCallInvite;
  private int activeCallNotificationId;
  private ChannelData channelData;

  /**
   * Creates an incoming call dialog.
   *
   * @param context                 The current context of the activity
   * @param answerCallClickListener The answer to call back.
   * @param cancelClickListener     Checks if the cancel button was clicked.
   * @return AlertDialog The dialog to be created.
   */
  public static AlertDialog createIncomingCallDialog(
      Context context,
      DialogInterface.OnClickListener answerCallClickListener,
      DialogInterface.OnClickListener cancelClickListener) {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context)
        .setIcon(R.drawable.ic_call_black_24dp)
        .setTitle("Incoming Call")
        .setPositiveButton("Accept", answerCallClickListener)
        .setNegativeButton("Reject", cancelClickListener)
        .setMessage("Call will be established");
    return alertDialogBuilder.create();
  }

  /**
   * Create a call dialog.
   *
   * @param callClickListener   Checks if the call button was clicked.
   * @param cancelClickListener Checks if the cancel button was clicked.
   * @param context             current context of the activity
   * @return AlertDialog The dialog to be created.
   */
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
    final TextView contact = dialogView.findViewById(R.id.contact);
    contact.setVisibility(View.INVISIBLE);
    contact.setText(toCall);
    contact.setHint(R.string.callee);
    alertDialogBuilder.setView(dialogView);

    return alertDialogBuilder.create();
  }

  /**
   * Ends voice call.
   */
  public static void endCall() {
    if (activeCall != null) {
      activeCall.disconnect();
      activeCall = null;
    }
  }

  /**
   * Called when the activity is starting.
   *
   * <p>Documentation: https://developer.android.com/reference/android/app/
   * Activity.html#onCreate(android.os.Bundle)
   *
   * @param savedInstanceState The saved state of the activity.
   */
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
    // Setup the broadcast receiver to be notified of FCM Token updates
    // or incoming call invite in this Activity.
    voiceBroadcastReceiver = new VoiceBroadcastReceiver();
    registerReceiver();

    // Needed for setting/abandoning audio focus during a call
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    if (audioManager != null) {
      audioManager.setSpeakerphoneOn(true);
    }
    setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

    if (activeCall != null ) {
      setCallUI();
    } else {
      resetUI();
    }

    // Displays a call dialog if the intent contains a call invite
    handleIncomingCallIntent(getIntent());

    int init = getIntent().getIntExtra("initiate", 0);

    // Ensure the microphone permission is enabled
    if (!checkPermissionForMicrophone()) {
      requestPermissionForMicrophone();
      init = 0;
      firstRegistration = true;
    } else {
      retrieveAccessToken();
    }

    if (init == 1) {
      identity = FirebaseAuth.getInstance().getUid();
      toCall = getIntent().getStringExtra("CallId");
      onBackPressed();
    } else {
      identity = FirebaseAuth.getInstance().getUid();
      toCall = getIntent().getStringExtra("CallId");
    }

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    String channelId = getIntent().getStringExtra(getResources().getString(R.string.channel_key));
    if (channelId != null) {
      channelData = ChannelController.retrieveChannel(channelId, this);
    }

    if (activeCall != null) {
      setCallUI();
      CallState callState = activeCall.getState();
      if (callState.compareTo(CallState.DISCONNECTED) == 0) {
        resetUI();
        activeCall = null;
      }
    } else {
      resetUI();
    }
  }

  /**
   * Handles incoming calls for new intents.
   *
   * @param intent The new intent.
   */
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
        activeCall = null;
        Log.d(TAG, "Connect failure");
        String message =
            String.format("Call Error: %d, %s", error.getErrorCode(), error.getMessage());
        Log.e(TAG, message);
        Snackbar.make(coordinatorLayout, message, SNACKBAR_DURATION).show();
        resetUI();
      }

      /**
       * After calling connect(), this method will be invoked asynchronously when the connect
       * request has successfully completed.
       *
       * <p>Documentation: https://developers.google.com/android/reference/com/google/android/
       * gms/common/api/GoogleApiClient.ConnectionCallbacks.html#onConnected(android.os.Bundle)
       *
       * @param call The call object.
       */
      @Override
      public void onConnected(Call call) {
        setAudioFocus(true);
        Log.d(TAG, "Connected");
        activeCall = call;
      }

      /**
       * The method called when a camera device is no longer available for use.
       *
       * <p>Documentation: https://developer.android.com/reference/android/hardware/camera2/
       * CameraDevice.StateCallback.html#onDisconnected(android.hardware.camera2.CameraDevice)
       *
       * @param call The call object.
       * @param error The error returned from the connection.
       */
      @Override
      public void onDisconnected(Call call, CallException error) {
        setAudioFocus(false);
        activeCall=null;
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

  /**
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

  /**
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

  /**
   * Handles resuming of the activity.
   *
   * <p>Documentation: https://developer.android.com/reference/android/app/Activity.html#onResume()
   */
  @Override
  protected void onResume() {
    super.onResume();
    registerReceiver();
  }

  /**
   * Handles pausing of the activity.
   *
   * <p>Documentation: https://developer.android.com/reference/android/app/Activity.html#onPause()
   */
  @Override
  protected void onPause() {
    super.onPause();
    unregisterReceiver();
  }

  /**
   * Perform any final cleanup before an activity is destroyed.
   *
   * <p>Documentation: https://developer.android.com/reference/android/app/Activity.html#onDestroy()
   */
  @Override
  public void onDestroy() {
    SoundPoolManager.release();
    super.onDestroy();
  }

  /**
   * Handle incoming call intent into the application.
   *
   * @param intent The intent coming in.
   */
  private void handleIncomingCallIntent(Intent intent) {
    if (intent != null && intent.getAction() != null) {
      if (intent.getAction().equals(ACTION_INCOMING_CALL)) {
        activeCallInvite = intent.getParcelableExtra(INCOMING_CALL_INVITE);
        if (activeCallInvite != null && (activeCallInvite.getState() == CallInvite.State.PENDING)) {
          soundPoolManager.playRinging();
          alertDialog =
              createIncomingCallDialog(
                  VoiceActivity.this,
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

  /**
   * Registers a receiver to the voice call.
   */
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

  /**
   * Unregisters a receiver from the app.
   */
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
      TextView contact = ((AlertDialog) dialog).findViewById(R.id.contact);
      if (contact != null) {
        contact.setVisibility(View.INVISIBLE);
        twiMLParams.put("to", contact.getText().toString());
        activeCall = Voice.call(VoiceActivity.this, accessToken, twiMLParams, callListener);
        setCallUI();
        alertDialog.dismiss();
      }
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

  /**
   * Register for Call invites on the Cloud messaging serevr
   */
  private void registerForCallInvites() {
    final String fcmToken = FirebaseInstanceId.getInstance().getToken();
    if (fcmToken != null) {
      Log.i(TAG, "Registering with FCM");
      Voice.register(
          this, accessToken, Voice.RegistrationChannel.FCM, fcmToken, registrationListener);
      if (firstRegistration) {
        firstRegistration = false;
        onBackPressed();
      }
    }
  }

  /**
   * @return Button to allow for calls
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
      endCall();
    };
  }

  /**
   * @return Mute the current call
   */
  private View.OnClickListener muteActionFabClickListener() {
    return v -> mute();
  }

  /**
   * Accept an incoming Call
   */
  private void answer() {
    activeCallInvite.accept(this, callListener);
    notificationManager.cancel(activeCallNotificationId);
  }

  /**
   * Mute current call
   */
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
   * @param setFocus Whether or not to set the audio focus.
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
                  .setOnAudioFocusChangeListener(i -> {
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
   * Check the MicroPhone Permissions.
   *
   * @return Whether the permission for the microphone was granted.
   */
  private boolean checkPermissionForMicrophone() {
    int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
    return resultMic == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Requests permission for the microphone.
   */
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
          this, new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_REQUEST_CODE);
    }
  }

  /**
   * Callback for the result from requesting permissions.
   *
   * <p>Documentation: https://developer.android.com/reference/android/support/v4/app/
   * ActivityCompat.OnRequestPermissionsResultCallback.html#
   * onRequestPermissionsResult(int,%20java.lang.String[],%20int[])
   *
   * @param requestCode  The request code passed in requestPermissions()
   * @param permissions  The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions which is either
   *                     PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
   */
  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

  /**
   * Initialize the contents of the Activity's standard options menu.
   *
   * <p>Documentation: https://developer.android.com/reference/android/app/
   * Activity#onCreateOptionsMenu(android.view.Menu)
   *
   * @param menu The menu items
   * @return You must return true for the menu to be displayed. if you return false it will not be
   * shown.
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    return true;
  }

  /**
   * This hook is called whenever an item in your options menu is selected.
   *
   * <p>Documentation: https://developer.android.com/reference/android/app/Activity.html
   * #onOptionsItemSelected(android.view.MenuItem)
   *
   * @param item The item selected from the menu.
   * @return boolean Return false to allow normal menu processing to proceed, true to consume it
   * here.
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int i = item.getItemId();
    if (i == R.id.speaker_menu_item) {
      if (audioManager.isSpeakerphoneOn()) {
        audioManager.setSpeakerphoneOn(false);
        item.setIcon(R.drawable.ic_phonelink_ring_white_24dp);
      } else {
        audioManager.setSpeakerphoneOn(true);
        item.setIcon(R.drawable.ic_volume_up_white_24dp);
      }
    } else if (i == android.R.id.home) {
      onBackPressed();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Get an access token from your Twillio access token server
   */
  private void retrieveAccessToken() {
    Ion.with(this)
        .load(TWILIO_ACCESS_TOKEN_SERVER_URL + "?identity=" + identity)
        .asString()
        .setCallback(
            (e, token) -> {
              if (e == null) {
                Log.d(TAG, "Access token: " + token);
                VoiceActivity.this.accessToken = token;
                registerForCallInvites();
              } else {
                Snackbar.make(
                    coordinatorLayout,
                    "Error retrieving access token. Unable to make calls",
                    Snackbar.LENGTH_LONG)
                    .show();
              }
            });
  }

  @Override
  public void dataChanged() {
    if (channelData != null && channelData.getVideoCallStatus()) {
      if (this.getWindow().getDecorView().getRootView().isShown()) {
        onBackPressed();
      }
    }
  }

  /**
   * Handles video broadcast from receiver.
   */
  private class VoiceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action != null && action.equals(ACTION_INCOMING_CALL)) {
        handleIncomingCallIntent(intent);
      }
    }
  }

  @Override
  public void onBackPressed(){
    finish();
  }
}
