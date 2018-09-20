package com.quartz.zielclient.channel;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.quartz.zielclient.messages.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * This Object abstracts away the communication with the database Channels
 * It requires a Chanlelistener to be able to pass updates to the user.
 *
 * @author Bilal Shehata
 */
public class ChannelData implements ValueEventListener {
  // contains current channel values in database
  private Map<String, Object> channelValues;
  // reference to the database
  private DatabaseReference channelReference;
  // the object that wants to listen to this channel
  private ChannelListener channelListener;
  // the ID of the channel;
  private String channelKey;

  /**
   * @param channelReference - location in database where channel exists
   * @param channelListener  - the object that wants to listen to the channel
   * @param channelKey
   */
  public ChannelData(DatabaseReference channelReference, ChannelListener channelListener, String channelKey) {
    this.channelReference = channelReference;
    this.channelListener = channelListener;
    this.channelKey = channelKey;
    channelReference.addValueEventListener(this);
  }


  public void setAssistedLocation(Location location) {
    final String xCoord = String.valueOf(location.getLatitude());
    final String yCoord = String.valueOf(location.getLongitude());

    this.channelReference.child("assistedLocation").child("xCoord").setValue(xCoord);
    this.channelReference.child("assistedLocation").child("yCoord").setValue(yCoord);
  }

  /**
   * method returns a LatLng Object since this is more practical for the google maps API.
   *
   * @return A LatLng object representing the user's location.
   */
  @SuppressWarnings("unchecked")
  public LatLng getAssistedLocation() {
    if (this.channelValues != null) {
      Map<String, String> assistedLocationCordinates = ((Map<String, String>) this.channelValues.get("assistedLocation"));

      double xCoord = Double.parseDouble(assistedLocationCordinates.get("xCoord"));
      double yCoord = Double.parseDouble(assistedLocationCordinates.get("yCoord"));
      return new LatLng(xCoord, yCoord);
    }

    // Default to Null Island
    return new LatLng(0, 0);
  }

  public void setMessages(Map<String, String> messages) {
    channelReference.child("messages").setValue(messages);
  }

  public void sendMessage(Message message) {
    Map<String, String> messageObject = new HashMap<>();

    messageObject.put("messageType", message.getType().toString());
    messageObject.put("messageValue", message.getMessageValue());
    channelReference.child("messages").push().setValue(messageObject);
  }

  /**
   * Retrieve all the messages from this channel.
   * <p>
   * Downcasting is required to serialise the JSON representaiton of the messages to a Java Map.
   *
   * @return A map of the messages.
   */
  @SuppressWarnings("unchecked")
  public Map<String, String> getMessages() {
    if (channelValues.get("messages") != null) {
      return (Map<String, String>) channelValues.get("messages");
    }

    Map<String, String> messageObject = new HashMap<>();
    messageObject.put("messageType", "TEXT");
    messageObject.put("messageValue", "this chatroom has no messages");
    return messageObject;
  }

  /**
   * recieve update from database and update the listener that some data has changed
   *
   * @param dataSnapshot The datasnaphot to update the client based on.
   */
  @Override
  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    GenericTypeIndicator<Map<String, Object>> t = new GenericTypeIndicator<Map<String, Object>>() {
    };
    channelValues = dataSnapshot.getValue(t);
    channelListener.dataChanged();
  }

  @Override
  public void onCancelled(@NonNull DatabaseError databaseError) {
    //todo
  }

  public String getDirectionsURL() {
    return channelValues.get("directionsURL").toString();
  }

  public void setDirectionsURL(String directionsURL) {
    channelReference.child("directionsURL").setValue(directionsURL);
  }

  public String getAssisted() {
    return channelValues.get("assisted").toString();
  }

  public void setAssisted(String assisted) {
    channelReference.child("assisted").setValue(assisted);
  }

  public boolean getAssistedStatus() {
    return channelValues.get("assistedStatus").equals(true);
  }

  public void setAssistedStatus(boolean assistedStatus) {
    channelReference.child("assistedStatus").setValue(assistedStatus);
  }

  public String getCarer() {
    return channelValues.get("carer").toString();
  }

  public void setCarer(String carer) {
    this.channelReference.child("carer").setValue(carer);
  }

  public boolean getCarerStatus() {
    return channelValues.get("carerStatus").equals(true);
  }

  public void setCarerStatus(boolean carerStatus) {
    channelReference.child("carerStatus").setValue(carerStatus);
  }

  public boolean getPing() {
    return channelValues.get("Ping").equals(true);
  }

  public void setPing(Boolean ping) {
    channelReference.child("Ping").setValue(ping);
  }

  public void setChannelKey(String channelKey) {
    this.channelKey = channelKey;
  }

  public String getChannelKey() {
    return channelKey;
  }
}
