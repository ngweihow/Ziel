package com.quartz.zielclient.activities.signup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.carer.CarerHomepageActivity;
import com.quartz.zielclient.activities.SplashScreenActivity;
import com.quartz.zielclient.activities.common.LaunchPadActivity;
import com.quartz.zielclient.user.UserController;
import com.quartz.zielclient.exceptions.AuthorisationException;
import com.quartz.zielclient.user.User;

import static android.view.View.OnClickListener;

public class AccountCreationActivity extends AppCompatActivity implements OnClickListener {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_account_creation);

    Button accountCreationButton = findViewById(R.id.completeAccountCreation);
    accountCreationButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    // We only want to listen on the account creation button
    if (v.getId() != R.id.completeAccountCreation) {
      return;
    }

    RadioGroup buttonGroup = findViewById(R.id.carerOrAssistedGroup);
    TextView firstNameView = findViewById(R.id.firstNameEntry);
    TextView lastNameView = findViewById(R.id.lastNameEntry);

    final String firstName = firstNameView.getText().toString();
    final String lastName = lastNameView.getText().toString();
    final boolean isAssisted = buttonGroup.getCheckedRadioButtonId() == R.id.assistedChoice;

    try {
      FirebaseUser firebaseUser = UserController.retrieveFirebaseUser();
      User user = UserController.createUser(firebaseUser, firstName, lastName, isAssisted);
      completeAccountCreation(user);
    } catch (AuthorisationException e) {
      handleLoginFailure();
    }
  }

  private void completeAccountCreation(User user) {
    Intent intent = new Intent();
    intent.putExtra("user", user.toBundle());

    // Redirect the user to the appropriate home page
    if (user.isAssisted()) {
      intent.setClass(this, LaunchPadActivity.class);
    } else {
      intent.setClass(this, CarerHomepageActivity.class);
    }

    startActivity(intent);
    finish();
  }

  private void handleLoginFailure() {
    Toast toast = Toast.makeText(this, R.string.failed_creation, Toast.LENGTH_SHORT);
    toast.show();

    Intent intent = new Intent(this, SplashScreenActivity.class);
    startActivity(intent);
    finish();
  }
}