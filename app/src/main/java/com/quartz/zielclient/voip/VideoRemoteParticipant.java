package com.quartz.zielclient.voip;

import android.support.design.widget.Snackbar;
import android.util.Log;

import com.quartz.zielclient.activities.channel.VideoActivity;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.TwilioException;

import static android.support.constraint.Constraints.TAG;

public class VideoRemoteParticipant implements RemoteParticipant.Listener {

  private VideoActivity videoActivity;

  public VideoRemoteParticipant(VideoActivity videoActivity) {
    this.videoActivity = videoActivity;
  }

  @Override
  public void onAudioTrackPublished(RemoteParticipant remoteParticipant,
                                    RemoteAudioTrackPublication remoteAudioTrackPublication) {
    Log.i(TAG, String.format("onAudioTrackPublished: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteAudioTrackPublication: sid=%s, enabled=%b, " +
            "subscribed=%b, name=%s]",
        remoteParticipant.getIdentity(),
        remoteAudioTrackPublication.getTrackSid(),
        remoteAudioTrackPublication.isTrackEnabled(),
        remoteAudioTrackPublication.isTrackSubscribed(),
        remoteAudioTrackPublication.getTrackName()));
  }

  @Override
  public void onAudioTrackUnpublished(RemoteParticipant remoteParticipant,
                                      RemoteAudioTrackPublication remoteAudioTrackPublication) {
    Log.i(TAG, String.format("onAudioTrackUnpublished: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteAudioTrackPublication: sid=%s, enabled=%b, " +
            "subscribed=%b, name=%s]",
        remoteParticipant.getIdentity(),
        remoteAudioTrackPublication.getTrackSid(),
        remoteAudioTrackPublication.isTrackEnabled(),
        remoteAudioTrackPublication.isTrackSubscribed(),
        remoteAudioTrackPublication.getTrackName()));
  }

  @Override
  public void onDataTrackPublished(RemoteParticipant remoteParticipant,
                                   RemoteDataTrackPublication remoteDataTrackPublication) {
    Log.i(TAG, String.format("onDataTrackPublished: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteDataTrackPublication: sid=%s, enabled=%b, " +
            "subscribed=%b, name=%s]",
        remoteParticipant.getIdentity(),
        remoteDataTrackPublication.getTrackSid(),
        remoteDataTrackPublication.isTrackEnabled(),
        remoteDataTrackPublication.isTrackSubscribed(),
        remoteDataTrackPublication.getTrackName()));
  }

  @Override
  public void onDataTrackUnpublished(RemoteParticipant remoteParticipant,
                                     RemoteDataTrackPublication remoteDataTrackPublication) {
    Log.i(TAG, String.format("onDataTrackUnpublished: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteDataTrackPublication: sid=%s, enabled=%b, " +
            "subscribed=%b, name=%s]",
        remoteParticipant.getIdentity(),
        remoteDataTrackPublication.getTrackSid(),
        remoteDataTrackPublication.isTrackEnabled(),
        remoteDataTrackPublication.isTrackSubscribed(),
        remoteDataTrackPublication.getTrackName()));
  }

  @Override
  public void onVideoTrackPublished(RemoteParticipant remoteParticipant,
                                    RemoteVideoTrackPublication remoteVideoTrackPublication) {
    Log.i(TAG, String.format("onVideoTrackPublished: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteVideoTrackPublication: sid=%s, enabled=%b, " +
            "subscribed=%b, name=%s]",
        remoteParticipant.getIdentity(),
        remoteVideoTrackPublication.getTrackSid(),
        remoteVideoTrackPublication.isTrackEnabled(),
        remoteVideoTrackPublication.isTrackSubscribed(),
        remoteVideoTrackPublication.getTrackName()));
  }

  @Override
  public void onVideoTrackUnpublished(RemoteParticipant remoteParticipant,
                                      RemoteVideoTrackPublication remoteVideoTrackPublication) {
    Log.i(TAG, String.format("onVideoTrackUnpublished: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteVideoTrackPublication: sid=%s, enabled=%b, " +
            "subscribed=%b, name=%s]",
        remoteParticipant.getIdentity(),
        remoteVideoTrackPublication.getTrackSid(),
        remoteVideoTrackPublication.isTrackEnabled(),
        remoteVideoTrackPublication.isTrackSubscribed(),
        remoteVideoTrackPublication.getTrackName()));
  }

  @Override
  public void onAudioTrackSubscribed(RemoteParticipant remoteParticipant,
                                     RemoteAudioTrackPublication remoteAudioTrackPublication,
                                     RemoteAudioTrack remoteAudioTrack) {
    Log.i(TAG, String.format("onAudioTrackSubscribed: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
        remoteParticipant.getIdentity(),
        remoteAudioTrack.isEnabled(),
        remoteAudioTrack.isPlaybackEnabled(),
        remoteAudioTrack.getName()));
  }

  @Override
  public void onAudioTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                       RemoteAudioTrackPublication remoteAudioTrackPublication,
                                       RemoteAudioTrack remoteAudioTrack) {
    Log.i(TAG, String.format("onAudioTrackUnsubscribed: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
        remoteParticipant.getIdentity(),
        remoteAudioTrack.isEnabled(),
        remoteAudioTrack.isPlaybackEnabled(),
        remoteAudioTrack.getName()));
  }

  @Override
  public void onAudioTrackSubscriptionFailed(RemoteParticipant remoteParticipant,
                                             RemoteAudioTrackPublication remoteAudioTrackPublication,
                                             TwilioException twilioException) {
    Log.i(TAG, String.format("onAudioTrackSubscriptionFailed: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteAudioTrackPublication: sid=%b, name=%s]" +
            "[TwilioException: code=%d, message=%s]",
        remoteParticipant.getIdentity(),
        remoteAudioTrackPublication.getTrackSid(),
        remoteAudioTrackPublication.getTrackName(),
        twilioException.getCode(),
        twilioException.getMessage()));
  }

  @Override
  public void onDataTrackSubscribed(RemoteParticipant remoteParticipant,
                                    RemoteDataTrackPublication remoteDataTrackPublication,
                                    RemoteDataTrack remoteDataTrack) {
    Log.i(TAG, String.format("onDataTrackSubscribed: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteDataTrack: enabled=%b, name=%s]",
        remoteParticipant.getIdentity(),
        remoteDataTrack.isEnabled(),
        remoteDataTrack.getName()));
  }

  @Override
  public void onDataTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                      RemoteDataTrackPublication remoteDataTrackPublication,
                                      RemoteDataTrack remoteDataTrack) {
    Log.i(TAG, String.format("onDataTrackUnsubscribed: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteDataTrack: enabled=%b, name=%s]",
        remoteParticipant.getIdentity(),
        remoteDataTrack.isEnabled(),
        remoteDataTrack.getName()));
  }

  @Override
  public void onDataTrackSubscriptionFailed(RemoteParticipant remoteParticipant,
                                            RemoteDataTrackPublication remoteDataTrackPublication,
                                            TwilioException twilioException) {
    Log.i(TAG, String.format("onDataTrackSubscriptionFailed: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteDataTrackPublication: sid=%b, name=%s]" +
            "[TwilioException: code=%d, message=%s]",
        remoteParticipant.getIdentity(),
        remoteDataTrackPublication.getTrackSid(),
        remoteDataTrackPublication.getTrackName(),
        twilioException.getCode(),
        twilioException.getMessage()));
  }

  @Override
  public void onVideoTrackSubscribed(RemoteParticipant remoteParticipant,
                                     RemoteVideoTrackPublication remoteVideoTrackPublication,
                                     RemoteVideoTrack remoteVideoTrack) {
    Log.i(TAG, String.format("onVideoTrackSubscribed: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteVideoTrack: enabled=%b, name=%s]",
        remoteParticipant.getIdentity(),
        remoteVideoTrack.isEnabled(),
        remoteVideoTrack.getName()));
    videoActivity.addRemoteParticipantVideo(remoteVideoTrack);
  }

  @Override
  public void onVideoTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                       RemoteVideoTrackPublication remoteVideoTrackPublication,
                                       RemoteVideoTrack remoteVideoTrack) {
    Log.i(TAG, String.format("onVideoTrackUnsubscribed: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteVideoTrack: enabled=%b, name=%s]",
        remoteParticipant.getIdentity(),
        remoteVideoTrack.isEnabled(),
        remoteVideoTrack.getName()));
    videoActivity.removeParticipantVideo(remoteVideoTrack);
  }

  @Override
  public void onVideoTrackSubscriptionFailed(RemoteParticipant remoteParticipant,
                                             RemoteVideoTrackPublication remoteVideoTrackPublication,
                                             TwilioException twilioException) {
    Log.i(TAG, String.format("onVideoTrackSubscriptionFailed: " +
            "[RemoteParticipant: identity=%s], " +
            "[RemoteVideoTrackPublication: sid=%b, name=%s]" +
            "[TwilioException: code=%d, message=%s]",
        remoteParticipant.getIdentity(),
        remoteVideoTrackPublication.getTrackSid(),
        remoteVideoTrackPublication.getTrackName(),
        twilioException.getCode(),
        twilioException.getMessage()));
    Snackbar.make(videoActivity.getConnectActionFab(),
        String.format("Failed to subscribe to %s video track",
            remoteParticipant.getIdentity()),
        Snackbar.LENGTH_LONG)
        .show();
  }

  @Override
  public void onAudioTrackEnabled(RemoteParticipant remoteParticipant,
                                  RemoteAudioTrackPublication remoteAudioTrackPublication) {

  }

  @Override
  public void onAudioTrackDisabled(RemoteParticipant remoteParticipant,
                                   RemoteAudioTrackPublication remoteAudioTrackPublication) {

  }

  @Override
  public void onVideoTrackEnabled(RemoteParticipant remoteParticipant,
                                  RemoteVideoTrackPublication remoteVideoTrackPublication) {

  }

  @Override
  public void onVideoTrackDisabled(RemoteParticipant remoteParticipant,
                                   RemoteVideoTrackPublication remoteVideoTrackPublication) {

  }
}

