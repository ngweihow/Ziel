package com.quartz.zielclient.activities.signup;

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
@Config(sdk=26)
public class SignUpActivityTest {

  @Test
  public void testActivityNotNull() {
    SignUpActivity activity = Robolectric.setupActivity(SignUpActivity.class);
    assertNotNull(activity);
  }


  @Test
  public void clickingTextChatShouldOpenActivity() {
    SignUpActivity activity = Robolectric.setupActivity(SignUpActivity.class);
    activity.findViewById(R.id.signup).performClick();
    Intent expectedIntent = new Intent(activity, VerifyPhoneNumberActivity.class);
    Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertEquals(expectedIntent.getComponent(), actual.getComponent());
  }

}