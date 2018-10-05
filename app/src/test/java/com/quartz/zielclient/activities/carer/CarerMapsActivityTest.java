package com.quartz.zielclient.activities.carer;

import android.content.Intent;

import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.common.TextChatActivity;
import com.quartz.zielclient.activities.common.VideoActivity;
import com.quartz.zielclient.activities.common.VoiceActivity;

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
public class CarerMapsActivityTest {
  @Test
  public void testActivityNotNull() {
    CarerMapsActivity activity = Robolectric.setupActivity(CarerMapsActivity.class);
    assertNotNull(activity);
  }

  @Test
  public void clickingTextChatShouldOpenActivity() {
    CarerMapsActivity activity = Robolectric.setupActivity(CarerMapsActivity.class);
    activity.findViewById(R.id.toTextChat).performClick();
    Intent expectedIntent = new Intent(activity, TextChatActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }

  @Test
  public void clickingVideoChatShouldOpenActivity() {
    CarerMapsActivity activity = Robolectric.setupActivity(CarerMapsActivity.class);
    activity.findViewById(R.id.toVideoActivity).performClick();
    Intent expectedIntent = new Intent(activity, VideoActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }

  @Test
  public void clickingVoiceChatShouldOpenActivity() {
    CarerMapsActivity activity = Robolectric.setupActivity(CarerMapsActivity.class);
    activity.findViewById(R.id.toVoiceChat).performClick();
    Intent expectedIntent = new Intent(activity, VoiceActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }
}