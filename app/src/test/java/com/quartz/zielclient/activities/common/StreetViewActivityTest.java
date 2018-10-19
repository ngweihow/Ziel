package com.quartz.zielclient.activities.common;

import com.quartz.zielclient.activities.channel.StreetViewActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class StreetViewActivityTest {
  StreetViewActivity streetViewActivity = Robolectric.buildActivity(StreetViewActivity.class).create().get();

  // Check that its not null
  @Test
  public void testActivityNotNull() {
    assertNotNull(streetViewActivity);
  }

  // Tests the dialogue which opens
  @Test
  public void testDialogueAppearsOnOpening() {
    assertNotNull(ShadowAlertDialog.getShownDialogs());
  }

  // Tests the back close button
  @Test
  public void testBackButtonClosesActivity() throws Exception {

    ShadowActivity activityShadow = Shadows.shadowOf(streetViewActivity);
    streetViewActivity.onBackPressed();
    assertTrue(activityShadow.isFinishing());
  }
}