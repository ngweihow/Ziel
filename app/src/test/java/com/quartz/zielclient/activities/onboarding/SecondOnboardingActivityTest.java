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
public class SecondOnboardingActivityTest {

  @Test
  public void clickingNextShouldOpenNextActivity() {
    SecondOnboardingActivity activity = Robolectric.setupActivity(SecondOnboardingActivity.class);
    activity.findViewById(R.id.next2).performClick();
    Intent expectedIntent = new Intent(activity, FinalOnboardingActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }

  @Test
  public void clickingSkipShouldOpenSignup() {
    SecondOnboardingActivity activity = Robolectric.setupActivity(SecondOnboardingActivity.class);
    activity.findViewById(R.id.signup2).performClick();
    Intent expectedIntent = new Intent(activity, SignUpActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }
}