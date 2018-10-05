package com.quartz.zielclient.models;

import android.os.Bundle;
import android.support.annotation.NonNull;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * Model representation of a channel request.
 *
 * @author wei how ng
 * @author alexvosnakis
 */
public class ChannelRequest implements Model, Comparable<ChannelRequest> {

  private static final String NAME_KEY = "name";
  private static final String CHANNEL_ID_KEY = "channelId";
  private static final String DESCRIPTION_KEY = "description";

  private String name;
  private String channelId;
  private String description;

  /**
   * Timestamp in Unix time (seconds since January 1 1970).
   */
  private long timestamp;

  public ChannelRequest() {
    // Intentionally empty
  }

  public ChannelRequest(String assistedName, String channelId, String description) {
    this.name = assistedName;
    this.channelId = channelId;
    this.description = description;
    // Convert Unix time from ms to s
    this.timestamp = System.currentTimeMillis();
  }

  public ChannelRequest(ChannelRequest channelRequest) {
    this.name = channelRequest.name;
    this.channelId = channelRequest.channelId;
    this.description = channelRequest.description;
    this.timestamp = channelRequest.timestamp;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getChannelId() {
    return channelId;
  }

  public void setChannelId(String channelId) {
    this.channelId = channelId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Formats the timestamp to a string representation.
   * @return A formatted timestamp.
   */
  public String formattedTimestamp() {
    Timestamp time = new Timestamp(timestamp);
    return new SimpleDateFormat("HH:mm, dd/MM/yyyy").format(time);
  }

  @Override
  public int compareTo(@NonNull ChannelRequest o) {
    return Long.compare(o.timestamp,timestamp);
  }

  @Override
  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putString(NAME_KEY, name);
    bundle.putString(CHANNEL_ID_KEY, channelId);
    bundle.putString(DESCRIPTION_KEY, description);
    return bundle;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ChannelRequest that = (ChannelRequest) o;
    return timestamp == that.timestamp &&
        Objects.equals(name, that.name) &&
        Objects.equals(channelId, that.channelId) &&
        Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, channelId, description, timestamp);
  }
}
