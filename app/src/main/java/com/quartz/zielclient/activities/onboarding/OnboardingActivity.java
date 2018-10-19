package com.quartz.zielclient.activities.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.signup.SignUpActivity;

/**
 * This class is responsible for displaying the on boarding activity page. This consists of a next
 * button leading to the second onboarding activity, or a skip button to proceed to app sign up.
 */
public class OnboardingActivity extends AppCompatActivity implements View.OnClickListener {

  /**
   * Called when the activity is starting.
   * <p>
   * Documentation: https://developer.android.com/reference/android/app/Activity.html#
   * onCreate(android.os.Bundle)
   *
   * @param savedInstanceState If the activity is being re-initialized after previously being shut
   *                           down then this Bundle contains the data it most recently
   *                           supplied in onSaveInstanceState(Bundle)
   */
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_help_onboarding_page1);

    Button nextButton = findViewById(R.id.next1);
    nextButton.setOnClickListener(this);

    Button skipButton = findViewById(R.id.signup);
    skipButton.setOnClickListener(this);
  }

  /**
   * Called when a view has been clicked.
   * <p>
   * Documentation: https://developer.android.com/reference/android/view/V
   * iew.OnClickListener.html#onClick(android.view.View)
   *
   * @param v The view that was clicked.
   */
  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.next1) {
      startActivity(new Intent(this, SecondOnboardingActivity.class));
    } else if (v.getId() == R.id.signup) {
      startActivity(new Intent(this, SignUpActivity.class));
    }
  }
}
