package com.quartz.zielclient.activities.assisted;

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
public class AssistedSelectCarerActivityTest {

  @Test
  public void testActivityNotNull() {
    AssistedSelectCarerActivity activity =
        Robolectric.setupActivity(AssistedSelectCarerActivity.class);
    assertNotNull(activity);
  }

  @Test
  public void clickingAddCarerShouldOpenActivity() {
    AssistedSelectCarerActivity activity = Robolectric.setupActivity(AssistedSelectCarerActivity.class);
    activity.findViewById(R.id.addCarerButton).performClick();
    Intent expectedIntent = new Intent(activity, AddCarerActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }
}
