package com.quartz.zielclient.channel;

/**
 * interface for objects that wish to listen to a channel
 *
 * @author Bilal Shehata
 */
public interface ChannelListener {

  /**
   * object needs to receive updates when values in the channel have been updated
   */
  void dataChanged();

  String getAssistedId();

  String getCarerId();
}