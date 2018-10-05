package com.quartz.zielclient.channel;

import android.location.Location;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

/**
 * Session handler object handlers the creation of sessions and notifying the relevant users that a
 * session has been created for them.
 *
 * @author Bilal Shehata
 */
public final class ChannelController {

  private static final String TAG = ChannelController.class.getSimpleName();
  private static DatabaseReference channelsReference = staticFirebaseInit();

  private ChannelController() {
    // Intentionally empty
  }

  /**
   * create and initialise a channel
   *
   * @param listener Callback that listens on the channel's database entry.
   * @return A new channel.
   */
  public static ChannelData createChannel(
      ChannelListener listener, String carerId, String assistedId) {
    if (channelsReference != null) {
      final String channelKey = UUID.randomUUID().toString();
      ChannelData channelData =
          new ChannelData(channelsReference.child(channelKey), listener, channelKey);
      Location initialLocation = new Location("");
      initialLocation.setLongitude(0);
      initialLocation.setLongitude(0);
      channelData.setVideoCallStatus(false);
      channelData.setAssistedLocation(initialLocation);
      channelData.setAssisted(assistedId);
      channelData.setCarer(carerId);
      channelData.setAssistedStatus(true);
      channelData.setCarerStatus(false);
      channelData.setChannelKey(channelKey);
      channelData.setDirectionsURL("none");

      Log.i("ChannelController", String.format("Creating new channel %s", channelKey));
      return channelData;
    }
    return null;
  }

  /**
   * Use this if you dont want to create a channel but you want to retrieve an already created
   * channel
   *
   * @param channelId ID of the channel.
   * @param channelListener Callback that listens on the channel's database entry.
   * @return The existing channel
   */
  public static ChannelData retrieveChannel(String channelId, ChannelListener channelListener) {
    Log.i(TAG, String.format("Retrieving channel %s", channelId));
    if (channelsReference != null) {
      return new ChannelData(channelsReference.child(channelId), channelListener, channelId);
    }
    return null;
  }

  private static DatabaseReference staticFirebaseInit() {
    try {
      return FirebaseDatabase.getInstance().getReference("channels/");
    } catch (IllegalStateException e) {
      return null;
    }
  }
}
