package com.quartz.zielclient.messages;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Service layer for messages.
 *
 * @author Wei How Ng
 */
public class MessageService {

  private MessageService() {
    // Intentionally empty
  }

  /**
   * Deserialise a nested structure of raw message data from FireBase.
   * The structure is:
   *
   *   messageKey: {
   *     messageText: "string"
   *     messageTime: "long"
   *     messageType: "string"
   *     username: "string"
   *   }
   * @param rawMessageData The raw message data.
   * @return A map of message keys to message objects.
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Message> deserialiseMessages(@NonNull Object rawMessageData) {
    Map<String, Message> messages = new HashMap<>();
    Map<String, Map<String, Object>> messageData = (Map<String, Map<String, Object>>) rawMessageData;

    // yikes
    for (Map.Entry<String, Map<String, Object>> messageEntry : messageData.entrySet()) {
      Message message = MessageFactory.makeMessage(messageEntry.getValue());
      messages.put(messageEntry.getKey(), message);
    }

    return messages;
  }
}
