package com.quartz.zielclient.activities.common;

import android.content.Intent;

import com.quartz.zielclient.R;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowApplication;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class VideoActivityTest {
  VideoActivity videoActivity = Robolectric.buildActivity(VideoActivity.class).create().get();

  // Check that its not null
  @Test
  public void testActivityNotNull() {
    assertNotNull(videoActivity);
  }

  // Tests the dialogue which opens
  @Test
  public void testDialogueAppearsOnOpening() {
    assertNotNull(ShadowAlertDialog.getShownDialogs());
  }

  // Tests the back close button
  @Test
  public void testBackButtonClosesActivity() throws Exception {

    ShadowActivity activityShadow = Shadows.shadowOf(videoActivity);
    videoActivity.onBackPressed();
    assertTrue(activityShadow.isFinishing());
  }

  // Test for the right channel id
  @Test
  public void testChannelId() {
    Intent intent =
        new Intent(ShadowApplication.getInstance().getApplicationContext(), VideoActivity.class);
    intent.putExtra(
        ShadowApplication.getInstance().getApplicationContext().getString(R.string.channel_key),
        "testId");
    VideoActivity videoActivity =
        Robolectric.buildActivity(VideoActivity.class, intent).create().get();

    assertEquals("testId", videoActivity.getChannelId());
  }
}