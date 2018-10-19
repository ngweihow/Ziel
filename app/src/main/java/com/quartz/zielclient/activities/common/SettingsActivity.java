package com.quartz.zielclient.activities.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.signup.SignUpActivity;
import com.quartz.zielclient.user.AuthorisationException;
import com.quartz.zielclient.user.User;
import com.quartz.zielclient.user.UserController;
import com.quartz.zielclient.user.UserFactory;

import static android.view.View.OnClickListener;
import static android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * Activity to update account details.
 *
 * @author alexvosnakis
 */
public class SettingsActivity extends AppCompatActivity
    implements ValueEventListener, OnClickListener, OnCheckedChangeListener {

  private static final String TAG = SettingsActivity.class.getSimpleName();

  private boolean nullSetup = false;
  private boolean updating = false;

  // Current user state
  private User user;

  private TextView firstNameEntry;
  private TextView lastNameEntry;
  private Switch roleSwitch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_ACTION_BAR);
    setContentView(R.layout.settings_activity);

    setupUser(getIntent());
  }

  private void setupUser(Intent intent) {
    Bundle userBundle = intent.getBundleExtra("user");
    if (userBundle == null) {
      // if we don't receive a bundle, fetch the user from the DB
      updating = true;
      nullSetup = true;
      try {
        UserController.fetchThisUser(this);
      } catch (AuthorisationException e) {
        Log.e(TAG, "User not signed in", e);
        sendHome();
      }
    } else {
      user = UserFactory.getUser(userBundle);
      populateUi();
      activateBar();
    }
  }

  @Override
  public void onBackPressed() {
    if (updating || nullSetup) {
      return;
    }

    Intent intent = new Intent(this, SettingsHome.class);
    intent.putExtra("user", user.toBundle());
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(intent);
    super.onBackPressed();
  }

  /**
   * Send the user back to sign up if they aren't logged in.
   */
  private void sendHome() {
    Toast.makeText(this, "User signed out", Toast.LENGTH_LONG).show();
    startActivity(new Intent(this, SignUpActivity.class));
    finish();
  }

  /**
   * Set up the action bar.
   */
  private void activateBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle("Account Settings");
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  /**
   * Populate the UI based on the current user model.
   */
  private void populateUi() {
    firstNameEntry = findViewById(R.id.settingFirstName);
    firstNameEntry.setText(user.getFirstName());

    lastNameEntry = findViewById(R.id.settingLastName);
    lastNameEntry.setText(user.getLastName());

    String roleString = user.isAssisted() ? "Assisted" : "Carer";
    roleSwitch = findViewById(R.id.roleSwitch);
    roleSwitch.setChecked(user.isAssisted());
    roleSwitch.setOnCheckedChangeListener(this);
    roleSwitch.setText(roleString);

    Button confirmButton = findViewById(R.id.settingsConfirm);
    confirmButton.setOnClickListener(this);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    setupUser(intent);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      if (updating || nullSetup) {
        return false;
      }

      Intent intent = new Intent(this, SettingsHome.class);
      intent.putExtra("user", user.toBundle());
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      startActivity(intent);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Updates the database with the current UI values.
   */
  private void confirmChanges() {
    updating = true;
    String firstName = firstNameEntry.getText().toString();
    String lastName = lastNameEntry.getText().toString();
    boolean isAssisted = roleSwitch.isChecked();

    User updatedUser = UserFactory.getUser(firstName, lastName, user.getPhoneNumber(), isAssisted);
    UserController.updateSelf(updatedUser, this);
  }

  /**
   * @return A dialog that confirms that the user wants to change their account details.
   */
  private Dialog buildConfirmationPrompt() {
    return new AlertDialog.Builder(this)
        .setTitle("Confirm your changes")
        .setMessage("Are you sure you want to update your account details?")
        .setPositiveButton(android.R.string.yes, ((dialog, which) -> {
          confirmChanges();
          dialog.dismiss();
        }))
        .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss())
        .create();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.settingsConfirm:
        buildConfirmationPrompt().show();
        break;
      default:
        break;
    }
  }

  /**
   * Updates the text next to the role switch.
   *
   * @param buttonView The role switch view.
   * @param isChecked  Whether or not the switch has been checked.
   */
  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (isChecked) {
      buttonView.setText("Assisted");
    } else {
      buttonView.setText("Carer");
    }
  }

  @Override
  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    user = UserFactory.getUser(dataSnapshot);
    updating = false;
    activateBar();
    populateUi();

    if (!nullSetup) {
      Toast.makeText(this, "Updated account settings.", Toast.LENGTH_SHORT).show();
    } else {
      nullSetup = false;
      populateUi();
      activateBar();
    }
  }

  @Override
  public void onCancelled(@NonNull DatabaseError databaseError) {
    Log.e(TAG, "Database error", databaseError.toException());
    Toast.makeText(this, "Error updating account", Toast.LENGTH_LONG).show();
  }
}