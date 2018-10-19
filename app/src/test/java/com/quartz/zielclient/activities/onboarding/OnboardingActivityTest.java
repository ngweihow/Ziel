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
@Config(sdk=26)
public class OnboardingActivityTest {

  @Test
  public void clickingNextShouldOpenNextActivity() {
    OnboardingActivity  activity = Robolectric.setupActivity(OnboardingActivity.class);
    activity.findViewById(R.id.next1).performClick();
    Intent expectedIntent = new Intent(activity, SecondOnboardingActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }

  @Test
  public void clickingSkipShouldOpenSignup() {
    OnboardingActivity activity = Robolectric.setupActivity(OnboardingActivity.class);
    activity.findViewById(R.id.signup).performClick();
    Intent expectedIntent = new Intent(activity, SignUpActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }
}