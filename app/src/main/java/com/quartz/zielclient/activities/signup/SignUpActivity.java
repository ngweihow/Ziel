package com.quartz.zielclient.activities.signup;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.quartz.zielclient.R;

import static android.Manifest.permission.READ_PHONE_STATE;

/**
 * This class is responsible for handling the sign up of the app, which handles permissions and
 * proceeds to the Verify Phone number activity afterwards.
 */
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

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
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_signup);

    Button button = findViewById(R.id.signup);
    button.setOnClickListener(this);

    requestPermissions(new String[]{READ_PHONE_STATE}, 1);
  }

  /**
   * Called when a view has been clicked.
   * <p>
   * Documentation: https://developer.android.com/reference/android/view/V
   * iew.OnClickListener.html#onClick(android.view.View)
   *
   * @param view The view that was clicked.
   */
  @Override
  public void onClick(View view) {
    int clickedId = view.getId();
    if (clickedId != R.id.signup) {
      return;
    }

    Intent intent = new Intent(SignUpActivity.this, VerifyPhoneNumberActivity.class);
    startActivity(intent);
  }
}