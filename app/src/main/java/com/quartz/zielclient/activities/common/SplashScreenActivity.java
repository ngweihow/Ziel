package com.quartz.zielclient.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.assisted.AssistedHomePageActivity;
import com.quartz.zielclient.activities.carer.CarerHomepageActivity;
import com.quartz.zielclient.activities.onboarding.OnboardingActivity;
import com.quartz.zielclient.activities.signup.SignUpActivity;
import com.quartz.zielclient.user.AuthorisationException;
import com.quartz.zielclient.user.User;
import com.quartz.zielclient.user.UserController;
import com.quartz.zielclient.user.UserFactory;

/**
 * Activity shows Splash Screen when authorising user into the app.
 *
 * @author Wei How Ng
 */
public class SplashScreenActivity extends AppCompatActivity implements ValueEventListener {

  private static final String TAG = SplashScreenActivity.class.getSimpleName();
  private ImageView logo;

  /**
   * Creates splash screen along with its attributes.
   *
   * <p>Documentation : https://developer.android.com/reference/android/app/
   * Activity.html#onCreate(android.os.Bundle)
   *
   * @param savedInstanceState This is responsible for saving state of the splash screen.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.SplashTheme);
    super.onCreate(savedInstanceState);
    try {
      UserController.fetchThisUser(this);
    } catch (AuthorisationException e) {
      Log.e(TAG, "Error when authorising user", e);

      Intent intent = new Intent(this, OnboardingActivity.class);
      startActivity(intent);
      finish();
    }
  }

  /**
   * This method will be called with a snapshot of the data at this location.
   * It will also be called each time that data changes.
   * <p>
   * Documentation: https://www.firebase.com/docs/java-api/javadoc/com/firebase/client/
   * \ValueEventListener.html
   *
   * @param dataSnapshot The current data at the location
   */
  @Override
  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    // Check if user hasn't completed signup
    if (!dataSnapshot.exists()) {
      goToSignin();
    }

    User user = UserFactory.getUser(dataSnapshot);
    redirect(user);
  }

  @Override
  public void onCancelled(@NonNull DatabaseError databaseError) {
    Log.e(TAG, "Database error:", databaseError.toException());
    goToSignin();
  }

  private void goToSignin() {
    Intent intent = new Intent(this, SignUpActivity.class);
    startActivity(intent);
    finish();
  }

  private void redirect(User user) {
    Class<? extends AppCompatActivity> homePage = user.isAssisted()
        ? AssistedHomePageActivity.class
        : CarerHomepageActivity.class;
    Intent intent = new Intent(this, homePage);
    intent.putExtra("user", user.toBundle());
    startActivity(intent);
    finish();
  }
}