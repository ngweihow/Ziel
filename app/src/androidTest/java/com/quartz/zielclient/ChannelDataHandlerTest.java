package com.quartz.zielclient;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.quartz.zielclient.channel.ChannelData;
import com.quartz.zielclient.channel.ChannelController;
import com.quartz.zielclient.channel.ChannelListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class ChannelDataHandlerTest {

  /**
   * Test that when a channel is requested it is created.
   */
  @Test
  public void testCreateChannel() {
    ChannelListener channelListener = Mockito.mock(ChannelListener.class);
    Mockito.when(channelListener.getAssistedId()).thenReturn("testAssistedId");
    Mockito.when(channelListener.getCarerId()).thenReturn("testCarerId");
    ChannelData testChannelData = ChannelController.createChannel(channelListener);
    Mockito.verify(channelListener, VerificationModeFactory.atLeastOnce()).getAssistedId();
    Mockito.verify(channelListener, VerificationModeFactory.atLeastOnce()).getCarerId();
    assertNotNull(testChannelData);
  }

  /**
   * Test to see whether channels can be retrieved
   */
  @Test
  public void retrieveChannel() {
    DatabaseReference testChannelsReference = Mockito.mock(DatabaseReference.class);
    DatabaseReference testChannelReference = Mockito.mock(DatabaseReference.class);
    ChannelListener channelListener = Mockito.mock(ChannelListener.class);
    String testChannelID = "123456789";
    Mockito.when(testChannelsReference.child(testChannelID)).thenReturn(testChannelReference);
    ChannelData channelData = ChannelController.retrieveChannel(testChannelID, channelListener);
    assertNotNull(channelData);
  }
}
