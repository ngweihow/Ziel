package com.quartz.zielclient.activities.common;

import android.content.Intent;

import com.quartz.zielclient.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class MapsActivityTest {

  @Test
  public void testActivityNotNull() {
    MapsActivity activity = Robolectric.setupActivity(MapsActivity.class);
    assertNotNull(activity);
  }


  @Test
  public void clickingTextChatShouldOpenActivity() {
    MapsActivity activity = Robolectric.setupActivity(MapsActivity.class);
    activity.findViewById(R.id.toTextChat).performClick();
    Intent expectedIntent = new Intent(activity, TextChatActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }

  @Test
  public void clickingVideoChatShouldOpenActivity() {
    MapsActivity activity = Robolectric.setupActivity(MapsActivity.class);
    activity.findViewById(R.id.toVideoChatButton).performClick();
    Intent expectedIntent = new Intent(activity, VideoActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }

  @Test
  public void clickingVoiceChatShouldOpenActivity() {
    MapsActivity activity = Robolectric.setupActivity(MapsActivity.class);
    activity.findViewById(R.id.toVoiceChat).performClick();
    Intent expectedIntent = new Intent(activity, VoiceActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }
}
