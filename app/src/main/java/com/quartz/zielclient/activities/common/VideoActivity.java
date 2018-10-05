package com.quartz.zielclient.activities.common;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.quartz.zielclient.activities.common.dialogue.Dialog;
import com.quartz.zielclient.activities.common.util.CameraCapturerCompat;
import com.quartz.zielclient.activities.common.util.SettingsActivity;
import com.quartz.zielclient.channel.ChannelController;
import com.quartz.zielclient.channel.ChannelData;
import com.quartz.zielclient.channel.ChannelListener;
import com.quartz.zielclient.voipUtilities.VideoRemoteParticipant;
import com.twilio.video.AudioCodec;
import com.twilio.video.CameraCapturer;
import com.twilio.video.CameraCapturer.CameraSource;
import com.twilio.video.ConnectOptions;
import com.twilio.video.EncodingParameters;
import com.twilio.video.G722Codec;
import com.twilio.video.H264Codec;
import com.twilio.video.IsacCodec;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.OpusCodec;
import com.twilio.video.PcmaCodec;
import com.twilio.video.PcmuCodec;
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
import com.twilio.video.Vp9Codec;

import java.util.Collections;
import java.util.UUID;

import static com.quartz.zielclient.R.drawable.ic_phonelink_ring_white_24dp;
import static com.quartz.zielclient.R.drawable.ic_volume_up_white_24dp;

public class VideoActivity extends AppCompatActivity implements ChannelListener {
  private static final int CAMERA_MIC_PERMISSION_REQUEST_CODE = 1;
  private static final String TAG = "VideoActivity";
  private static final String LOCAL_AUDIO_TRACK_NAME = "mic";
  private static final String LOCAL_VIDEO_TRACK_NAME = "camera";
  private static final String ACCESS_TOKEN_SERVER = "http://35.189.54.26:3001/token";
  private String accessToken;
  private Room room;
  private LocalParticipant localParticipant;

  private AudioCodec audioCodec;
  private VideoCodec videoCodec;

  private EncodingParameters encodingParameters;

  private com.twilio.video.VideoView primaryVideoView;
  private com.twilio.video.VideoView thumbnailVideoView;
  private SharedPreferences preferences;

  /*
   * Android application UI elements
   */
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
  private ChannelData channelData;


  private String channelId;
  private ChannelData channel;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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


    /*
     * Get shared preferences to read settings
     */
    preferences = PreferenceManager.getDefaultSharedPreferences(this);

    /*
     * Enable changing the volume using the up/down keys during a conversation
     */
    setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

    /*
     * Needed for setting/abandoning audio focus during call
     */
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    audioManager.setSpeakerphoneOn(true);

    /*
     * Check camera and microphone permissions. Needed in Android M.
     */
    if (!checkPermissionForCameraAndMicrophone()) {
      requestPermissionForCameraAndMicrophone();
    } else {
      createAudioAndVideoTracks();
      setAccessToken();
    }

    /*
     * Set the initial state of the UI
     */
    intializeUI();
    showConnectDialog();

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_video_activity, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_settings:
        startActivity(
            new Intent(this, com.quartz.zielclient.activities.common.util.SettingsActivity.class));
        return true;
      case R.id.speaker_menu_item:
        if (audioManager.isSpeakerphoneOn()) {
          audioManager.setSpeakerphoneOn(false);
          item.setIcon(ic_phonelink_ring_white_24dp);
        } else {
          audioManager.setSpeakerphoneOn(true);
          item.setIcon(ic_volume_up_white_24dp);
        }
        return true;
      default:
        return false;
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == CAMERA_MIC_PERMISSION_REQUEST_CODE) {
      boolean cameraAndMicPermissionGranted = true;

      for (int grantResult : grantResults) {
        cameraAndMicPermissionGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
      }

      if (cameraAndMicPermissionGranted) {
        createAudioAndVideoTracks();
        setAccessToken();
      } else {
        Toast.makeText(this, R.string.permissions_needed, Toast.LENGTH_LONG).show();
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    /*
     * Update preferred audio and video codec in case changed in settings
     */
    audioCodec =
        getAudioCodecPreference(
            com.quartz.zielclient.activities.common.util.SettingsActivity.PREF_AUDIO_CODEC,
            com.quartz.zielclient.activities.common.util.SettingsActivity.PREF_AUDIO_CODEC_DEFAULT);
    videoCodec =
        getVideoCodecPreference(
            com.quartz.zielclient.activities.common.util.SettingsActivity.PREF_VIDEO_CODEC,
            com.quartz.zielclient.activities.common.util.SettingsActivity.PREF_VIDEO_CODEC_DEFAULT);

    /*
     * Get latest encoding parameters
     */
    final EncodingParameters newEncodingParameters = getEncodingParameters();

    /*
     * If the local video track was released when the app was put in the background, recreate.
     */
    if (localVideoTrack == null && checkPermissionForCameraAndMicrophone()) {
      localVideoTrack =
          LocalVideoTrack.create(
              this, true, cameraCapturerCompat.getVideoCapturer(), LOCAL_VIDEO_TRACK_NAME);
      localVideoTrack.addRenderer(localVideoView);

      /*
       * If connected to a Room then share the local video track.
       */
      if (localParticipant != null) {
        localParticipant.publishTrack(localVideoTrack);

        /*
         * Update encoding parameters if they have changed.
         */
        if (!newEncodingParameters.equals(encodingParameters)) {
          localParticipant.setEncodingParameters(newEncodingParameters);
        }
      }
    }

    /*
     * Update encoding parameters
     */
    encodingParameters = newEncodingParameters;
  }

  @Override
  protected void onPause() {
    /*
     * Release the local video track before going in the background. This ensures that the
     * camera can be used by other applications while this app is in the background.
     */
    if (localVideoTrack != null) {
      /*
       * If this local video track is being shared in a Room, unpublish from room before
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

  private boolean checkPermissionForCameraAndMicrophone() {
    int resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
    int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
    return resultCamera == PackageManager.PERMISSION_GRANTED
        && resultMic == PackageManager.PERMISSION_GRANTED;
  }

  private void requestPermissionForCameraAndMicrophone() {
    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
        || ActivityCompat.shouldShowRequestPermissionRationale(
            this, Manifest.permission.RECORD_AUDIO)) {
      Toast.makeText(this, R.string.permissions_needed, Toast.LENGTH_LONG).show();
    } else {
      ActivityCompat.requestPermissions(
          this,
          new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
          CAMERA_MIC_PERMISSION_REQUEST_CODE);
    }
  }

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
    return (CameraCapturer.isSourceAvailable(CameraSource.FRONT_CAMERA))
        ? (CameraSource.FRONT_CAMERA)
        : (CameraSource.BACK_CAMERA);
  }

  private void setAccessToken() {

    retrieveAccessTokenfromServer();
  }

  private void connectToRoom(String roomName) {
    configureAudio(true);
    ConnectOptions.Builder connectOptionsBuilder =
        new ConnectOptions.Builder(accessToken).roomName(roomName);

    /*
     * Add local audio track to connect options to share with participants.
     */
    if (localAudioTrack != null) {
      connectOptionsBuilder.audioTracks(Collections.singletonList(localAudioTrack));
    }

    /*
     * Add local video track to connect options to share with participants.
     */
    if (localVideoTrack != null) {
      connectOptionsBuilder.videoTracks(Collections.singletonList(localVideoTrack));
    }

    /*
     * Set the preferred audio and video codec for media.
     */
    connectOptionsBuilder.preferAudioCodecs(Collections.singletonList(audioCodec));
    connectOptionsBuilder.preferVideoCodecs(Collections.singletonList(videoCodec));

    /*
     * Set the sender side encoding parameters.
     */
    connectOptionsBuilder.encodingParameters(encodingParameters);

    room = Video.connect(this, connectOptionsBuilder.build(), roomListener());
    setDisconnectAction();
  }

  /*
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

  /*
   * Get the preferred audio codec from shared preferences
   */
  private AudioCodec getAudioCodecPreference(String key, String defaultValue) {
    final String audioCodecName = preferences.getString(key, defaultValue);

    switch (audioCodecName) {
      case IsacCodec.NAME:
        return new IsacCodec();
      case OpusCodec.NAME:
        return new OpusCodec();
      case PcmaCodec.NAME:
        return new PcmaCodec();
      case PcmuCodec.NAME:
        return new PcmuCodec();
      case G722Codec.NAME:
        return new G722Codec();
      default:
        return new OpusCodec();
    }
  }

  /*
   * Get the preferred video codec from shared preferences
   */
  private VideoCodec getVideoCodecPreference(String key, String defaultValue) {
    final String videoCodecName = preferences.getString(key, defaultValue);

    switch (videoCodecName) {
      case Vp8Codec.NAME:
        boolean simulcast =
            preferences.getBoolean(
                com.quartz.zielclient.activities.common.util.SettingsActivity.PREF_VP8_SIMULCAST,
                com.quartz.zielclient.activities.common.util.SettingsActivity
                    .PREF_VP8_SIMULCAST_DEFAULT);
        return new Vp8Codec(simulcast);
      case H264Codec.NAME:
        return new H264Codec();
      case Vp9Codec.NAME:
        return new Vp9Codec();
      default:
        return new Vp8Codec();
    }
  }

  private EncodingParameters getEncodingParameters() {
    final int maxAudioBitrate =
        Integer.parseInt(
            preferences.getString(
                com.quartz.zielclient.activities.common.util.SettingsActivity
                    .PREF_SENDER_MAX_AUDIO_BITRATE,
                com.quartz.zielclient.activities.common.util.SettingsActivity
                    .PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT));
    final int maxVideoBitrate =
        Integer.parseInt(
            preferences.getString(
                com.quartz.zielclient.activities.common.util.SettingsActivity
                    .PREF_SENDER_MAX_VIDEO_BITRATE,
                SettingsActivity.PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT));

    return new EncodingParameters(maxAudioBitrate, maxVideoBitrate);
  }

  /*
   * The actions performed during disconnect.
   */
  private void setDisconnectAction() {
    connectActionFab.setImageDrawable(
        ContextCompat.getDrawable(this, R.drawable.ic_call_end_white_24px));
    connectActionFab.show();
    connectActionFab.setOnClickListener(disconnectClickListener());
  }

  /*
   * Creates an connect UI dialog
   */
  public void showConnectDialog() {

    TextView roomEditText = new TextView(this);
    roomEditText.setText(channelId);
    roomEditText.setVisibility(View.INVISIBLE);

    connectDialog =
        Dialog.createConnectDialog(
            roomEditText,
            connectClickListener(roomEditText),
            cancelConnectDialogClickListener(),
            this);
    connectDialog.show();
  }

  /*
   * Called when remote participant joins the room
   */
  private void addRemoteParticipant(RemoteParticipant remoteParticipant) {
    /*
     * This app only displays video for one additional participant per Room
     */
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
    videoStatusTextView.setText("User joined");

    /*
     * Add remote participant renderer
     */
    if (remoteParticipant.getRemoteVideoTracks().size() > 0) {
      RemoteVideoTrackPublication remoteVideoTrackPublication =
          remoteParticipant.getRemoteVideoTracks().get(0);

      /*
       * Only render video tracks that are subscribed to
       */
      if (remoteVideoTrackPublication.isTrackSubscribed()) {
        addRemoteParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
      }
    }

    /*
     * Start listening for participant events
     */
    remoteParticipant.setListener(remoteParticipantListener());
  }

  /*
   * Set primary view as renderer for participant video track
   */
  public void addRemoteParticipantVideo(VideoTrack videoTrack) {
    moveLocalVideoToThumbnailView();
    primaryVideoView.setMirror(false);
    videoTrack.addRenderer(primaryVideoView);
  }

  private void moveLocalVideoToThumbnailView() {
    if (thumbnailVideoView.getVisibility() == View.GONE) {
      thumbnailVideoView.setVisibility(View.VISIBLE);
      localVideoTrack.removeRenderer(primaryVideoView);
      localVideoTrack.addRenderer(thumbnailVideoView);
      localVideoView = thumbnailVideoView;
      thumbnailVideoView.setMirror(
          cameraCapturerCompat.getCameraSource() == CameraSource.FRONT_CAMERA);
    }
  }

  /*
   * Called when remote participant leaves the room
   */
  private void removeRemoteParticipant(RemoteParticipant remoteParticipant) {
    channel.setVideoCallStatus(false);
    room.disconnect();
    videoStatusTextView.setText("User Left.");

    if (!remoteParticipant.getIdentity().equals(remoteParticipantIdentity)) {
      return;
    }

    /*
     * Remove remote participant renderer
     */
    if (!remoteParticipant.getRemoteVideoTracks().isEmpty()) {
      RemoteVideoTrackPublication remoteVideoTrackPublication =
          remoteParticipant.getRemoteVideoTracks().get(0);

      /*
       * Remove video only if subscribed to participant track
       */
      if (remoteVideoTrackPublication.isTrackSubscribed()) {
        removeParticipantVideo(remoteVideoTrackPublication.getRemoteVideoTrack());
      }
    }
    moveLocalVideoToPrimaryView();
  }

  public void removeParticipantVideo(VideoTrack videoTrack) {
    videoTrack.removeRenderer(primaryVideoView);
  }

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

  /*
   * Room events listener
   */
  private Room.Listener roomListener() {
    return new Room.Listener() {
      @Override
      public void onConnected(Room room) {
        localParticipant = room.getLocalParticipant();
        videoStatusTextView.setText("Connected to  Channel video Chat" );
        setTitle(room.getName());
        channel.setVideoCallStatus(true);

        for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {
          addRemoteParticipant(remoteParticipant);
          break;
        }
      }

      @Override
      public void onConnectFailure(Room room, TwilioException e) {
        videoStatusTextView.setText("Failed to connect");
        configureAudio(false);
        intializeUI();
      }

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

      @Override
      public void onParticipantConnected(Room room, RemoteParticipant remoteParticipant) {
        addRemoteParticipant(remoteParticipant);
      }

      @Override
      public void onParticipantDisconnected(Room room, RemoteParticipant remoteParticipant) {
        removeRemoteParticipant(remoteParticipant);
      }

      @Override
      public void onRecordingStarted(Room room) {
        /*
         * Indicates when media shared to a Room is being recorded. Note that
         * recording is only available in our Group Rooms developer preview.
         */
        Log.d(TAG, "onRecordingStarted");
      }

      @Override
      public void onRecordingStopped(Room room) {
        /*
         * Indicates when media shared to a Room is no longer being recorded. Note that
         * recording is only available in our Group Rooms developer preview.
         */
        Log.d(TAG, "onRecordingStopped");
      }
    };
  }

  private RemoteParticipant.Listener remoteParticipantListener() {
    return new VideoRemoteParticipant(this);
  }

  private DialogInterface.OnClickListener connectClickListener(final TextView roomEditText) {
    return (dialog, which) -> {
      /*
       * Connect to room
       */
      connectToRoom(roomEditText.getText().toString());
    };
  }

  private View.OnClickListener disconnectClickListener() {
    return v -> {
      /*
       * Disconnect from room
       */
      if (room != null) {
        channel.setVideoCallStatus(false);
        room.disconnect();
      }
      intializeUI();
    };
  }

  private View.OnClickListener connectActionClickListener() {
    return v -> showConnectDialog();
  }

  private DialogInterface.OnClickListener cancelConnectDialogClickListener() {
    return (dialog, which) -> {
      intializeUI();
      connectDialog.dismiss();
    };
  }

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

  private View.OnClickListener localVideoClickListener() {
    return v -> {
      /*
       * Enable/disable the local video track
       */
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

  private View.OnClickListener muteClickListener() {
    return v -> {
      /*
       * Enable/disable the local audio track. The results of this operation are
       * signaled to other Participants in the same Room. When an audio track is
       * disabled, the audio is muted.
       */
      if (localAudioTrack != null) {
        boolean enable = !localAudioTrack.isEnabled();
        localAudioTrack.enable(enable);
        int icon = enable ? R.drawable.ic_mic_white_24dp : R.drawable.ic_mic_off_black_24dp;
        muteActionFab.setImageDrawable(ContextCompat.getDrawable(VideoActivity.this, icon));
      }
    };
  }

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

  private void configureAudio(boolean enable) {
    if (enable) {
      previousAudioMode = audioManager.getMode();
      // Request audio focus before making any device switch
      requestAudioFocus();
      audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
      /*
       * Always disable microphone mute during a WebRTC call.
       */
      previousMicrophoneMute = audioManager.isMicrophoneMute();
      audioManager.setMicrophoneMute(false);
    } else {
      audioManager.setMode(previousAudioMode);
      audioManager.abandonAudioFocus(null);
      audioManager.setMicrophoneMute(previousMicrophoneMute);
    }
  }

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
              .setOnAudioFocusChangeListener(
                      i -> {})
              .build();
      audioManager.requestAudioFocus(focusRequest);
    } else {
      audioManager.requestAudioFocus(
          null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }
  }

  public TextView getVideoStatusTextView() {
    return videoStatusTextView;
  }

  public FloatingActionButton getConnectActionFab() {
    return connectActionFab;
  }

  @Override
  public void dataChanged() {
    //
  }
  @Override
  public void onBackPressed(){
    if (channel != null) {
      channel.setVideoCallStatus(false);
      }
    super.onBackPressed();
  }

  public String getChannelId() {
    return channelId;
  }
}
