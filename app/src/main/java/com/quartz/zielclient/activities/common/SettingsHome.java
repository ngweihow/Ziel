package com.quartz.zielclient.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.assisted.AssistedHomePageActivity;
import com.quartz.zielclient.activities.carer.CarerHomepageActivity;
import com.quartz.zielclient.activities.signup.SignUpActivity;
import com.quartz.zielclient.user.AuthorisationException;
import com.quartz.zielclient.user.User;
import com.quartz.zielclient.user.UserController;
import com.quartz.zielclient.user.UserFactory;

/**
 * Navigates to edit profile and feedback activities.
 *
 * @author alexvosnakis
 */
public class SettingsHome extends AppCompatActivity implements View.OnClickListener, ValueEventListener {

  private static final String TAG = SettingsHome.class.getSimpleName();

  private boolean settingUp = true;
  private User user;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_homepage);

    Button editProfile = findViewById(R.id.editProfile);
    editProfile.setOnClickListener(this);

    Button feedback = findViewById(R.id.feedback);
    feedback.setOnClickListener(this);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle("Settings");
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    setupUser(getIntent());
  }

  /**
   * Sets up the user with a bundle or, if hte bundle doesn't have a user, fetches it from the
   * database.
   *
   * @param intent The intent holding the user.
   */
  private void setupUser(Intent intent) {
    Bundle userBundle = intent.getBundleExtra("user");
    if (userBundle != null) {
      user = UserFactory.getUser(userBundle);
      settingUp = false;
    } else {
      try {
        settingUp = true;
        UserController.fetchThisUser(this);
      } catch (AuthorisationException e) {
        Log.e(TAG, "User signed out", e);
        sendHome();
      }
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    setupUser(intent);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home && !settingUp) {
      // Respond to the action bar's Up/Home button
      startActivity(goHomeIntent());
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    startActivity(goHomeIntent());
    finish();
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.editProfile) {
      Intent intent = new Intent(this, SettingsActivity.class);
      intent.putExtra("user", user.toBundle());
      startActivity(intent);
    } else if (v.getId() == R.id.feedback) {
      Intent intent = new Intent(this, FeedbackActivity.class);
      startActivity(intent);
    }
  }

  /**
   * Go back to the appropriate home page.
   */
  private Intent goHomeIntent() {
    Class<? extends AppCompatActivity> homePage = user.isAssisted()
        ? AssistedHomePageActivity.class
        : CarerHomepageActivity.class;
    Intent intent = new Intent(this, homePage);
    intent.putExtra("user", user.toBundle());
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    return intent;
  }

  @Override
  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    user = UserFactory.getUser(dataSnapshot);
    settingUp = false;
  }

  @Override
  public void onCancelled(@NonNull DatabaseError databaseError) {
    Log.e(TAG, "Error fetching from database", databaseError.toException());
  }

  /**
   * Send the user back to sign up if they aren't logged in.
   */
  private void sendHome() {
    Toast.makeText(this, "User signed out", Toast.LENGTH_LONG).show();
    startActivity(new Intent(this, SignUpActivity.class));
    finish();
  }
}