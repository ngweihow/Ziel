package com.quartz.zielclient;

import com.quartz.zielclient.channel.ChannelRequestFactory;
import com.quartz.zielclient.models.ChannelRequest;
import com.quartz.zielclient.user.User;
import com.quartz.zielclient.user.UserFactory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ChannelRequestTest {

  private User user;
  private ChannelRequest testRequest;

  @Before
  public void setup() {
    user = UserFactory.getUser("Test", "User", "+123456789", true);
    testRequest = ChannelRequestFactory.getChannelRequest(user, "ABCDE", "test");
  }

  @Test
  public void channelRequest_timeStampFormatted() {
    ChannelRequest timeStampTestReq = ChannelRequestFactory.getChannelRequest(testRequest);
    timeStampTestReq.setTimestamp(1537362683L * 1000);
    assertEquals("23:11, 19/09/2018", timeStampTestReq.formattedTimestamp());
  }
}
