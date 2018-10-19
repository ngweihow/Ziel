package com.quartz.zielclient.activities.onboarding;

import android.content.Intent;

import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.signup.SignUpActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 26)
public class FinalOnboardingActivityTest {

  @Test
  public void clickingSkipShouldOpenSignup() {
    FinalOnboardingActivity activity = Robolectric.setupActivity(FinalOnboardingActivity.class);
    activity.findViewById(R.id.signup3).performClick();
    Intent expectedIntent = new Intent(activity, SignUpActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }
}