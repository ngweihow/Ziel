package com.quartz.zielclient.messages;

/**
 * Message class acts as a wrapper around messages.
 *
 * @author Bilal Shehata
 */
public class Message {


  public enum MessageType {TEXT, IMAGE, VIDEO}

  private MessageType type;
  private String messageValue;

  Message(MessageType type, String messageValue) {
    this.type = type;
    this.messageValue = messageValue;
  }

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

}
