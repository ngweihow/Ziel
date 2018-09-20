package com.quartz.zielclient.models;

import android.os.Bundle;

public class Channel implements Model {

  private String assistedId;
  private String carerId;
  private String channelId;

  public Channel(String assistedId, String carerId, String channelId) {
    this.assistedId = assistedId;
    this.carerId = carerId;
    this.channelId = channelId;
  }

  public String getAssistedId() {
    return assistedId;
  }

  public String getCarerId() {
    return carerId;
  }

  public String getChannelId() {
    return channelId;
  }

  @Override
  public Bundle toBundle() {
    return null;
  }
}
