package com.quartz.zielclient.activities.channel;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.common.Dialog;
import com.quartz.zielclient.channel.ChannelController;
import com.quartz.zielclient.channel.ChannelData;
import com.quartz.zielclient.channel.ChannelListener;
import com.quartz.zielclient.voip.CameraCapturerCompat;
import com.quartz.zielclient.voip.VideoRemoteParticipant;
import com.twilio.video.AudioCodec;
import com.twilio.video.CameraCapturer;
import com.twilio.video.CameraCapturer.CameraSource;
import com.twilio.video.ConnectOptions;
import com.twilio.video.EncodingParameters;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.OpusCodec;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.RoomState;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoCodec;
import com.twilio.video.VideoRenderer;
import com.twilio.video.VideoTrack;
import com.twilio.video.Vp8Codec;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static com.quartz.zielclient.R.drawable.ic_phonelink_ring_white_24dp;
import static com.quartz.zielclient.R.drawable.ic_volume_up_white_24dp;

/**
 * This activity is responsible for handling video activity.
 */
public class VideoActivity extends AppCompatActivity implements ChannelListener {
  private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
  private static final String TAG = "VideoActivity";
  private static final String LOCAL_AUDIO_TRACK_NAME = "mic";
  private static final String LOCAL_VIDEO_TRACK_NAME = "camera";
  private static final String ACCESS_TOKEN_SERVER = "http://35.189.54.26:3001/token";
  private String accessToken;
  private Room room;
  private boolean inChannel;
  private LocalParticipant localParticipant;

  private AudioCodec audioCodec;
  private VideoCodec videoCodec;

  private EncodingParameters encodingParameters;

  private com.twilio.video.VideoView primaryVideoView;
  private com.twilio.video.VideoView thumbnailVideoView;

  // Android application UI elements
  private TextView videoStatusTextView;
  private CameraCapturerCompat cameraCapturerCompat;
  private LocalAudioTrack localAudioTrack;
  private LocalVideoTrack localVideoTrack;

  private FloatingActionButton connectActionFab;
  private FloatingActionButton switchCameraActionFab;
  private FloatingActionButton localVideoActionFab;
  private FloatingActionButton muteActionFab;
  private AlertDialog connectDialog;
  private AudioManager audioManager;
  private String remoteParticipantIdentity;

  private int previousAudioMode;
  private boolean previousMicrophoneMute;
  private VideoRenderer localVideoView;
  private boolean disconnectedFromOnDestroy;

  private String channelId;
  private ChannelData channel;

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
    inChannel = true;
    setContentView(R.layout.activity_video);

    primaryVideoView = findViewById(R.id.primary_video_view);
    thumbnailVideoView = findViewById(R.id.thumbnail_video_view);
    videoStatusTextView = findViewById(R.id.video_status_textview);

    connectActionFab = findViewById(R.id.connect_action_fab);
    switchCameraActionFab = findViewById(R.id.switch_camera_action_fab);
    localVideoActionFab = findViewById(R.id.local_video_action_fab);
    muteActionFab = findViewById(R.id.mute_action_fab);
    channelId = getIntent().getStringExtra(getResources().getString(R.string.channel_key));

    if (channelId != null) {
      channel = ChannelController.retrieveChannel(channelId, this);
    }

    // Enable changing the volume using the up/down keys during a conversation
    setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

    // Needed for setting/abandoning audio focus during call
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    if (audioManager != null) {
      audioManager.setSpeakerphoneOn(true);
    }
    // Check camera and microphone permissions. Needed in Android M.
    if (!checkPermissionForCameraAndMicrophone()) {
      requestPermissionForCameraAndMicrophone();
    } else {
      createAudioAndVideoTracks();
      setAccessToken();
    }

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    intializeUI();
    showConnectDialog();
  }

  /**
   * Initialize the contents of the Activity's standard options menu
   *
   * <p>Documentation: https://developer.android.com/reference/android/app/
   * Fragment#onCreateOptionsMenu(android.view.Menu,%20android.view.MenuInflater)
   *
   * @param menu The options menu in which you place your items.
   * @return boolean Whether the option menu was created successfully.
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_video_activity, menu);
    return true;
  }

  /**
   * This hook is called whenever an item in your options menu is selected.
   *
   * <p>Documentation: https://developer.android.com/reference/android/app/
   * Fragment.html#onOptionsItemSelected(android.view.MenuItem)
   *
   * @param item The item in the menu selected.
   * @return boolean Return false to allow normal menu processing to proceed, true to consume it
   * here.
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.speaker_menu_item:
        if (audioManager.isSpeakerphoneOn()) {
          audioManager.setSpeakerphoneOn(false);
          item.setIcon(ic_phonelink_ring_white_24dp);
        } else {
          audioManager.setSpeakerphoneOn(true);
          item.setIcon(ic_volume_up_white_24dp);
        }
        return true;
      case android.R.id.home:
        onBackPressed();
        return true;
      default:
        return false;
    }
  }

  /**
   * Callback for the result from requesting permissions.
   *
   * <p>Documentation: https://developer.android.com/reference/android/support/v4/app/
   * ActivityCompat.OnRequestPermissionsResultCallback.html#
   * onRequestPermissionsResult(int,%20java.lang.String[],%20int[])
   *
   * @param requestCode  The request code passed in requestPermissions.
   * @param permissions  The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions which is either
   *                     PERMISSION_GRANTED or PERMISSION_DENIED
   */
  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE) {
      boolean cameraAndMicPermissionGranted = Arrays.stream(grantResults)
          .allMatch(grantResult -> grantResult == PackageManager.PERMISSION_GRANTED);

      if (cameraAndMicPermissionGranted) {
        createAudioAndVideoTracks();
        setAccessToken();
      } else {
        Toast.makeText(this, R.string.permissions_needed, Toast.LENGTH_LONG).show();
      }
    }
  }

  /**
   * Handles button back press in activity.
   *
   * <p>Documentation: https://developer.android.com/reference/android/app/Activity.html#onResume()
   */
  @Override
  protected void onResume() {
    super.onResume();

    // Update preferred audio and video codec in case changed in settings
    audioCodec = new OpusCodec();
    videoCodec = new Vp8Codec();
    final EncodingParameters params = new EncodingParameters(0, 0);

    // If the local video track was released when the app was put in the background, recreate.
    if (localVideoTrack == null && checkPermissionForCameraAndMicrophone()) {
      localVideoTrack =
          LocalVideoTrack.create(
              this, true, cameraCapturerCompat.getVideoCapturer(), LOCAL_VIDEO_TRACK_NAME);
      localVideoTrack.addRenderer(localVideoView);

      // If connected to a Room then share the local video track.
      if (localParticipant != null) {
        localParticipant.publishTrack(localVideoTrack);

        // Update encoding parameters if they have changed.
        if (!params.equals(encodingParameters)) {
          localParticipant.setEncodingParameters(params);
        }
      }
    }

    encodingParameters = params;
  }

  /**
   * Handles pausing of activity in the application.
   *
   * <p>Documentation: https://developer.android.com/reference/android/app/Activity.html#onPause()
   */
  @Override
  protected void onPause() {
    /*
     * Release the local video track before going in the background. This ensures that the
     * camera can be used by other applications while this app is in the background.
     */
    if (localVideoTrack != null) {
      /*
       * If this local video track is being shared in a Room, unpublished from room before
       * releasing the video track. Participants will be notified that the track has been
       * unpublished.
       */
      if (localParticipant != null) {
        localParticipant.unpublishTrack(localVideoTrack);
      }

      localVideoTrack.release();
      localVideoTrack = null;
    }
    super.onPause();
  }

  /**
   * Perform any final cleanup before an activity is destroyed.
   *
   * <p>Documentation: https://developer.android.com/reference/android/app/Activity.html#onDestroy()
   */
  @Override
  protected void onDestroy() {
    /*
     * Always disconnect from the room before leaving the Activity to
     * ensure any memory allocated to the Room resource is freed.
     */
    if (room != null && room.getState() != RoomState.DISCONNECTED) {
      channel.setVideoCallStatus(false);
      room.disconnect();
      disconnectedFromOnDestroy = true;
    }

    /*
     * Release the local audio and video tracks ensuring any memory allocated to audio
     * or video is freed.
     */
    if (localAudioTrack != null) {
      localAudioTrack.release();
      localAudioTrack = null;
    }

    if (localVideoTrack != null) {
      localVideoTrack.release();
      localVideoTrack = null;
    }

    super.onDestroy();
  }

  /**
   * Checks permissions from user to grant camera and microphone in device.
   *
   * @return boolean if the permission was granted.
   */
  private boolean checkPermissionForCameraAndMicrophone() {
    int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
    int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
    return resultCamera == PackageManager.PERMISSION_GRANTED
        && resultMic == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * Requests permissions for camera and microphone in voice call.
   */
  private void requestPermissionForCameraAndMicrophone() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
        || ActivityCompat.shouldShowRequestPermissionRationale(
        this, Manifest.permission.RECORD_AUDIO)) {
      Toast.makeText(this, R.string.permissions_needed, Toast.LENGTH_LONG).show();
    } else {
      ActivityCompat.requestPermissions(
          this,
          new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
          CAMERA_MIC_PERMISSION_REQUEST_CODE);
    }
  }

  /**
   * Sets up microphone and camera within Video call.
   */
  private void createAudioAndVideoTracks() {
    // Share your microphone
    localAudioTrack = LocalAudioTrack.create(this, true, LOCAL_AUDIO_TRACK_NAME);

    // Share your camera
    cameraCapturerCompat = new CameraCapturerCompat(this, getAvailableCameraSource());
    localVideoTrack =
        LocalVideoTrack.create(
            this, true, cameraCapturerCompat.getVideoCapturer(), LOCAL_VIDEO_TRACK_NAME);
    primaryVideoView.setMirror(true);
    localVideoTrack.addRenderer(primaryVideoView);
    localVideoView = primaryVideoView;
  }

  private CameraSource getAvailableCameraSource() {
    return CameraCapturer.isSourceAvailable(CameraSource.FRONT_CAMERA)
        ? CameraSource.FRONT_CAMERA
        : CameraSource.BACK_CAMERA;
  }

  private void setAccessToken() {
    retrieveAccessTokenfromServer();
  }

  private void connectToRoom(String roomName) {
    inChannel = true;
    configureAudio(true);
    ConnectOptions.Builder connectOptionsBuilder =
        new ConnectOptions.Builder(accessToken).roomName(roomName);

    // Add local audio track to connect options to share with participants.
    if (localAudioTrack != null) {
      connectOptionsBuilder.audioTracks(Collections.singletonList(localAudioTrack));
    }

    // Add local video track to connect options to share with participants.
    if (localVideoTrack != null) {
      connectOptionsBuilder.videoTracks(Collections.singletonList(localVideoTrack));
    }

    // Set the preferred audio and video codec for media.
    connectOptionsBuilder
        .preferAudioCodecs(Collections.singletonList(audioCodec))
        .preferVideoCodecs(Collections.singletonList(videoCodec))
        .encodingParameters(encodingParameters);

    room = Video.connect(this, connectOptionsBuilder.build(), roomListener());
    setDisconnectAction();
  }

  /**
   * The initial state when there is no active room.
   */
  private void intializeUI() {
    connectActionFab.setImageDrawable(
        ContextCompat.getDrawable(this, R.drawable.ic_video_call_white_24dp));
    connectActionFab.show();
    connectActionFab.setOnClickListener(connectActionClickListener());
    switchCameraActionFab.show();
    switchCameraActionFab.setOnClickListener(switchCameraClickListener());
    localVideoActionFab.show();
    localVideoActionFab.setOnClickListener(localVideoClickListener());
    muteActionFab.show();
    muteActionFab.setOnClickListener(muteClickListener());
  }

  /**
   * The actions performed during disconnect.
   */
  private void setDisconnectAction() {
    connectActionFab.setImageDrawable(
        ContextCompat.getDrawable(this, R.drawable.ic_call_end_white_24px));
    //    connectActionFab.setBackgroundColor(0xfa091d);
    connectActionFab.show();
    connectActionFab.setOnClickListener(disconnectClickListener());
  }

  /**
   * Creates an connect UI dialog
   */
  public void showConnectDialog() {
    TextView roomEditText = new TextView(this);
    roomEditText.setText(channelId);
    roomEditText.setVisibility(View.INVISIBLE);

    connectDialog = Dialog.createConnectDialog(
        roomEditText,
        connectClickListener(roomEditText),
        cancelConnectDialogClickListener(),
        this);
    connectDialog.show();
  }

  /**
   * Called when remote participant joins the room
   */
  private void addRemoteParticipant(RemoteParticipant remoteParticipant) {
    // This app only displays video for one additional participant per Room
    if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
      Snackbar.make(
          connectActionFab,
          "Multiple participants are not currently support in this UI",
          Snackbar.LENGTH_LONG)
          .setAction("Action", null)
          .show();
      return;
    }
    remoteParticipantIdentity = remoteParticipant.getIdentity();
    // todo delete
    videoStatusTextView.setText("User joined");

    // Add remote participant renderer
    if (!remoteParticipant.getRemoteVideoTracks().isEmpty()) {
      RemoteVideoTrackPublication remoteVideoTrackPublication =
          remoteParticipant.getRemoteVideoTracks().get(0);

      // Only render video tracks that are subscribed to
      if (remoteVideoTrackPublication.isTrackSubscribed()) {
        addRemoteParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
      }
    }

    remoteParticipant.setListener(remoteParticipantListener());
  }

  /**
   * Set primary view as renderer for participant video track
   */
  public void addRemoteParticipantVideo(VideoTrack videoTrack) {
    moveLocalVideoToThumbnailView();
    primaryVideoView.setMirror(false);
    videoTrack.addRenderer(primaryVideoView);
  }

  /**
   * Changes the local video thumbnail view for the camera.
   */
  private void moveLocalVideoToThumbnailView() {
    if (thumbnailVideoView.getVisibility() == View.GONE) {
      thumbnailVideoView.setVisibility(View.VISIBLE);
      if (localVideoTrack != null) {
        localVideoTrack.removeRenderer(primaryVideoView);
        localVideoTrack.addRenderer(thumbnailVideoView);
      }
      localVideoView = thumbnailVideoView;
      thumbnailVideoView.setMirror(
          cameraCapturerCompat.getCameraSource() == CameraSource.FRONT_CAMERA);
    }
  }

  /**
   * Called when remote participant leaves the room
   */
  private void removeRemoteParticipant(RemoteParticipant remoteParticipant) {
    channel.setVideoCallStatus(false);
    inChannel = false;
    room.disconnect();
    // todo delete
    videoStatusTextView.setText("User Left.");

    if (!remoteParticipant.getIdentity().equals(remoteParticipantIdentity)) {
      return;
    }

    // Remove remote participant renderer
    if (!remoteParticipant.getRemoteVideoTracks().isEmpty()) {
      RemoteVideoTrackPublication remoteVideoTrackPublication =
          remoteParticipant.getRemoteVideoTracks().get(0);

      // Remove video only if subscribed to participant track
      if (remoteVideoTrackPublication.isTrackSubscribed()) {
        removeParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
      }
    }
    moveLocalVideoToPrimaryView();
  }

  /**
   * remove the other users videotrack when they leave the channel
   *
   * @param videoTrack
   */
  public void removeParticipantVideo(VideoTrack videoTrack) {
    videoTrack.removeRenderer(primaryVideoView);
  }

  /**
   * when their is no one on the call display own video
   */
  private void moveLocalVideoToPrimaryView() {
    if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
      thumbnailVideoView.setVisibility(View.GONE);
      if (localVideoTrack != null) {
        localVideoTrack.removeRenderer(thumbnailVideoView);
        localVideoTrack.addRenderer(primaryVideoView);
      }
      localVideoView = primaryVideoView;
      primaryVideoView.setMirror(
          cameraCapturerCompat.getCameraSource() == CameraSource.FRONT_CAMERA);
    }
  }

  /**
   * Listen to the various actions that can be performed on the room
   * most importantly to detect when another user joins or leaves the room
   *
   * @return
   */
  private Room.Listener roomListener() {
    return new Room.Listener() {

      @Override
      public void onConnected(Room room) {
        localParticipant = room.getLocalParticipant();
        videoStatusTextView.setText("Connected to Channel video Chat");
        // todo delete this line
//        setTitle(room.getName());
        channel.setVideoCallStatus(true);
        if (!room.getRemoteParticipants().isEmpty()) {
          addRemoteParticipant(room.getRemoteParticipants().get(0));
        }
      }

      /**
       * Called when a room has succeeded.
       *
       * @param room The room to connect to.
       * @param e The twillio exception triggered.
       */
      @Override
      public void onConnectFailure(Room room, TwilioException e) {
        videoStatusTextView.setText("Failed to connect");
        configureAudio(false);
        intializeUI();
      }

      /**
       * Called when room is disconnected.
       *
       * @param room The room to connect to.
       * @param e The twillio exception triggered.
       */
      @Override
      public void onDisconnected(Room room, TwilioException e) {
        localParticipant = null;
        videoStatusTextView.setText("Disconnected from Channel Video Chat");
        VideoActivity.this.room = null;
        // Only reinitialize the UI if disconnect was not called from onDestroy()
        if (!disconnectedFromOnDestroy) {
          configureAudio(false);
          intializeUI();
          moveLocalVideoToPrimaryView();
        }
      }

      /**
       * Called when a room has been disconnected from.
       *
       * <p>Documentation: https://media.twiliocdn.com/sdk/android/video/releases/1.0.0-beta6/docs/
       * com/twilio/video/Room.Listener.html#
       * onParticipantConnected-com.twilio.video.Room-com.twilio.video.Participant-
       *
       * @param room The room to connect to.
       * @param remoteParticipant The remote participant to connect with.
       */
      @Override
      public void onParticipantConnected(Room room, RemoteParticipant remoteParticipant) {
        addRemoteParticipant(remoteParticipant);
      }

      /**
       * Called when a participant has disconnected from a room.
       *
       * <p>Documentation: Called when a participant has disconnected from a room.
       *
       * @param room The room to disconnect from.
       * @param remoteParticipant The remote participant to connect with.
       */
      @Override
      public void onParticipantDisconnected(Room room, RemoteParticipant remoteParticipant) {
        removeRemoteParticipant(remoteParticipant);
      }

      /**
       * Called when the media being shared to a Room is being recorded.
       *
       * <p>Documentation: https://media.twiliocdn.com/sdk/android/video/releases/1.0.0-beta17/
       * docs/com/twilio/video/Room.Listener.html#onRecordingStarted-com.twilio.video.Room-
       *
       * @param room The room to connect to.
       */
      @Override
      public void onRecordingStarted(Room room) {
        Log.d(TAG, "onRecordingStarted");
      }

      /**
       * Called when the media being shared to a Room is no longer being recorded.
       *
       * @param room The room to connect to.
       */
      @Override
      public void onRecordingStopped(Room room) {
        Log.d(TAG, "onRecordingStopped");
      }
    };
  }

  /**
   * Sets up the remote listener for the participant.
   *
   * @return RemoteParticipant.Listener The listener to be used.
   */
  private RemoteParticipant.Listener remoteParticipantListener() {
    return new VideoRemoteParticipant(this);
  }

  /**
   * Displays the state of the room at the top of the activity
   *
   * @param roomEditText
   * @return
   */
  private DialogInterface.OnClickListener connectClickListener(final TextView roomEditText) {
    return (dialog, which) -> connectToRoom(roomEditText.getText().toString());
  }

  /**
   * Button that allows user to disconnect from channel
   *
   * @return
   */
  private View.OnClickListener disconnectClickListener() {
    return v -> {
      if (room != null) {
        channel.setVideoCallStatus(false);
        inChannel = false;
        room.disconnect();
      }
      intializeUI();
    };
  }

  /**
   * Called when connect button is clicked and shows confirmation dialog
   *
   * @return
   */
  private View.OnClickListener connectActionClickListener() {
    return v -> showConnectDialog();
  }

  private DialogInterface.OnClickListener cancelConnectDialogClickListener() {
    return (dialog, which) -> {
      intializeUI();
      connectDialog.dismiss();
    };
  }

  /**
   * This allows the user to switch between forward and back facing camera
   *
   * @return
   */
  private View.OnClickListener switchCameraClickListener() {
    return v -> {
      if (cameraCapturerCompat != null) {
        CameraSource cameraSource = cameraCapturerCompat.getCameraSource();
        cameraCapturerCompat.switchCamera();
        if (thumbnailVideoView.getVisibility() == View.VISIBLE) {
          thumbnailVideoView.setMirror(cameraSource == CameraSource.BACK_CAMERA);
        } else {
          primaryVideoView.setMirror(cameraSource == CameraSource.BACK_CAMERA);
        }
      }
    };
  }

  /**
   * This allows the user to hide their camera from view
   *
   * @return
   */
  private View.OnClickListener localVideoClickListener() {
    return v -> {
      // Enable/disable the local video track
      if (localVideoTrack != null) {
        boolean enable = !localVideoTrack.isEnabled();
        localVideoTrack.enable(enable);
        int icon;
        if (enable) {
          icon = R.drawable.ic_videocam_white_24dp;
          switchCameraActionFab.show();
        } else {
          icon = R.drawable.ic_videocam_off_black_24dp;
          switchCameraActionFab.hide();
        }
        localVideoActionFab.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, icon));
      }
    };
  }

  /**
   * @return Enable/disable the local audio track. The results of this operation are signaled to
   * other Participants in the same Room. When an audio track is disabled, the audio is muted.
   */
  private View.OnClickListener muteClickListener() {
    return v -> {
      if (localAudioTrack != null) {
        boolean enable = !localAudioTrack.isEnabled();
        localAudioTrack.enable(enable);
        int icon = enable ? R.drawable.ic_mic_white_24dp : R.drawable.ic_mic_off_black_24dp;
        muteActionFab.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, icon));
      }
    };
  }

  /** */
  private void retrieveAccessTokenfromServer() {
    Ion.with(this)
        .load(String.format("%s?identity=%s", ACCESS_TOKEN_SERVER, UUID.randomUUID().toString()))
        .asString()
        .setCallback(
            (e, token) -> {
              if (e == null) {
                VideoActivity.this.accessToken = token;
                Log.d("TOKEN", token);
              } else {
                Toast.makeText(
                    VideoActivity.this,
                    R.string.error_retrieving_access_token,
                    Toast.LENGTH_LONG)
                    .show();
              }
            });
  }

  /**
   * Configures audio when enabled.
   *
   * @param enable Detects whether the audio is enabled.
   */
  private void configureAudio(boolean enable) {
    if (enable) {
      previousAudioMode = audioManager.getMode();
      // Request audio focus before making any device switch
      requestAudioFocus();
      audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
      // Always disable microphone mute during a WebRTC call.
      previousMicrophoneMute = audioManager.isMicrophoneMute();
      audioManager.setMicrophoneMute(false);
    } else {
      audioManager.setMode(previousAudioMode);
      audioManager.abandonAudioFocus(null);
      audioManager.setMicrophoneMute(previousMicrophoneMute);
    }
  }

  /**
   * Requests audio focus on video.
   */
  private void requestAudioFocus() {
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
  }

  /**
   * Creates a floating action button.
   *
   * @return FloatingActionButton The button to be shown.
   */
  public FloatingActionButton getConnectActionFab() {
    return connectActionFab;
  }

  @Override
  public void dataChanged() {
    if (room == null) {

      Log.d("YOLO ", "HERE");
      if (channel.getVideoCallStatus() && !inChannel)
        finish();
    }
  }

  /**
   * Called when the activity has detected the user's press of the back key.
   *
   * <p>Documentation: https://developer.android.com/reference/android/app/Activity#onBackPressed()
   */
  @Override
  public void onBackPressed() {
    finish();
  }

  public String getChannelId() {
    return channelId;
  }
}
