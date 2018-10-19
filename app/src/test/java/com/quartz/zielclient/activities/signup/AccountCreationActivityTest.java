package com.quartz.zielclient.activities.signup;

import android.content.Intent;

import com.quartz.zielclient.activities.assisted.AssistedHomePageActivity;
import com.quartz.zielclient.activities.carer.CarerHomepageActivity;
import com.quartz.zielclient.user.User;

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
public class AccountCreationActivityTest {

  @Test
  public void testActivityNotNull() {
    AccountCreationActivity activity = Robolectric.setupActivity(AccountCreationActivity.class);
    assertNotNull(activity);
  }

  @Test
  public void testAssisted() {
    User testAssisted = new User();
    testAssisted.setAssisted(true);
    AccountCreationActivity activity = Robolectric.setupActivity(AccountCreationActivity.class);
    Intent expectedIntent = new Intent(activity, AssistedHomePageActivity.class);
    activity.completeAccountCreation(testAssisted);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());

  }


  @Test
  public void testCarer() {
    User testAssisted = new User();
    testAssisted.setAssisted(false);
    AccountCreationActivity activity = Robolectric.setupActivity(AccountCreationActivity.class);
    Intent expectedIntent = new Intent(activity, CarerHomepageActivity.class);
    activity.completeAccountCreation(testAssisted);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());

  }
}