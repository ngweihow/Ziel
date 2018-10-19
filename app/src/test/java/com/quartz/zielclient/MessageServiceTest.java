package com.quartz.zielclient;

import com.quartz.zielclient.messages.Message;
import com.quartz.zielclient.messages.MessageService;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MessageServiceTest {

  private Map<String, Map<String, Object>> testMessageData;
  private Map<String, Map<String, Object>> testMessageData2;

  @Before
  public void setup() {
    // Testing for correct input
    Map<String, Map<String, Object>> test1 = new HashMap<>();
    Map<String, Object> test1Message = new HashMap<>();
    test1Message.put("userName", "wei");
    test1Message.put("messageValue", "this code sucks");
    test1Message.put("type", "TEXT");
    test1Message.put("messageTime", 12345L);
    test1.put("testMessage", test1Message);
    testMessageData = test1;

    // Testing for incorrect input
    Map<String, Map<String, Object>> test2 = new HashMap<>();
    Map<String, Object> test2Message = new HashMap<>();
    test2Message.put("userName", "Tom");
    test2Message.put("messageValue", "TEXT");
    test2Message.put("messageTime", 124444L);
    test2Message.put("type", "TEXT");
    test2Message.put("Text", "I agree");
    test2.put("testMessage2", test2Message);
    testMessageData2 = test2;
  }

  /**
   * Test 1 that should pass the Message Services
   */
  @Test
  public void deserialise_test() {
    // Test 1
    Map<String, Message> testMessage = MessageService.deserialiseMessages(testMessageData);
    assertEquals("this code sucks", testMessage.get("testMessage").getMessageValue());
  }
  
  /**
   * Test 2 which tests the wrong message string for the Message Services
   */
  @Test
  public void deserialise_test2() {
    Map<String, Message> testMessage = MessageService.deserialiseMessages(testMessageData2);
    assertNotEquals("I disagree", testMessage.get("testMessage2").getMessageValue());
  }
}