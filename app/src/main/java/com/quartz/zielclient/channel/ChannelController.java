package com.quartz.zielclient.channel;

import android.location.Location;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Session handler object handlers the creation of sessions and notifying the relevant users
 * that a session has been created for them.
 *
 * @author Bilal Shehata
 */
public final class ChannelController {

  private static final String TAG = ChannelController.class.getSimpleName();

  private ChannelController() {
    // Intentionally empty
  }

  private static DatabaseReference channelsReference = FirebaseDatabase.getInstance().getReference("channels/");

  /**
   * create and initialise a channel
   *
   * @param channelListener Callback that listens on the channel's database entry.
   * @return A new channel.
   */
  public static ChannelData createChannel(ChannelListener channelListener) {
    final String channelKey = UUID.randomUUID().toString();
    ChannelData channelData = new ChannelData(channelsReference.child(channelKey), channelListener, channelKey);
    Location initialLocation = new Location("");
    initialLocation.setLongitude(0);
    initialLocation.setLongitude(0);
    channelData.setAssistedLocation(initialLocation);
    channelData.setAssisted(channelListener.getAssistedId());
    channelData.setCarer(channelListener.getCarerId());
    channelData.setAssistedStatus(true);
    channelData.setCarerStatus(false);
    Map<String, String> initialMessage = new HashMap<>();
    initialMessage.put("TEXT", "welcome to the chat");
    channelData.setChannelKey(channelKey);
    channelData.setDirectionsURL("none");
    channelData.setMessages(initialMessage);
    channelData.setPing(false);
    Log.i("ChannelController", String.format("Creating new channel %s", channelKey));
    return channelData;
  }

  /**
   * Use this if you dont want to create a channel but you want to retrieve an already created channel
   *
   * @param channelId       ID of the channel.
   * @param channelListener Callback that listens on the channel's database entry.
   * @return The existing channel
   */
  public static ChannelData retrieveChannel(String channelId, ChannelListener channelListener) {
    Log.i(TAG, String.format("Retrieving channel %s", channelId));

    return new ChannelData(channelsReference.child(channelId), channelListener, channelId);
  }
}