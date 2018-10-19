package com.quartz.zielclient.messages;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Message class acts as a wrapper around messages.
 *
 * @author Wei How Ng
 */
public class Message implements Comparable<Message> {


  public enum MessageType {TEXT, IMAGE, VIDEO}

  private MessageType type;
  private String messageValue;
  private String userName;
  private long messageTime;

  Message(MessageType type, String messageValue, String userName) {
    this.type = type;
    this.messageValue = messageValue;
    this.userName = userName;
    this.messageTime = new Date().getTime();
  }

  public Message(MessageType type, String messageValue, String userName, long messageTime) {
    this.type = type;
    this.messageValue = messageValue;
    this.userName = userName;
    this.messageTime = messageTime;
  }

  @Override
  public int compareTo(@NonNull Message o) {
    return (int) (this.messageTime - o.messageTime);
  }

  // Getters and setters
  public String getMessageValue() {
    return messageValue;
  }

  public void setMessageValue(String messageValue) {
    this.messageValue = messageValue;
  }

  public MessageType getType() {
    return type;
  }

  public void setType(MessageType type) {
    this.type = type;
  }

  public long getMessageTime() {
    return messageTime;
  }

  public void setMessageTime(long messageTime) {
    this.messageTime = messageTime;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }
}
