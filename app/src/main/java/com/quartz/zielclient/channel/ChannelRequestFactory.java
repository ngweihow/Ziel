package com.quartz.zielclient.channel;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.quartz.zielclient.models.ChannelRequest;
import com.quartz.zielclient.user.User;

public class ChannelRequestFactory {
  private ChannelRequestFactory() {
    // Intentionally empty
  }

  /**
   * Construct a new ChannelRequest.
   *
   * @param assisted  The assisted user making the request.
   * @param channelId The ID of the channel.
   * @param desc      The description of the channel.
   * @return A new ChannelRequest model.
   */
  public static ChannelRequest getChannelRequest(User assisted, String channelId, String desc) {
    return new ChannelRequest(
        assisted.fullName(),
        channelId,
        desc
    );
  }

  /**
   * Construct a new ChannelRequest from a DataSnapshot.
   *
   * @param dataSnapshot A DataSnapshot representing a ChannelRequest.
   * @return A ChannelRequest model.
   */
  public static ChannelRequest getChannelRequest(@NonNull DataSnapshot dataSnapshot) {
    return dataSnapshot.getValue(ChannelRequest.class);
  }

  public static ChannelRequest getChannelRequest(ChannelRequest channelRequest) {
    return new ChannelRequest(channelRequest);
  }
}
