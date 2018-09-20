package com.quartz.zielclient.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.carer.CarerHomepageActivity;
import com.quartz.zielclient.activities.HomePageActivity;
import com.quartz.zielclient.activities.signup.SignUpActivity;


/**
 * This activity is a temporary showcase of the features of the application
 * it acts as a navigation board to explore features and allow for continous integretion
 * <p>
 * THis activity will not be present in the final product.
 *
 * @author Bilal Shehata
 */
public class LaunchPadActivity extends AppCompatActivity implements View.OnClickListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_launch_pad);
    Button signIn = findViewById(R.id.signInButton);
    Button signUp = findViewById(R.id.signUpButton);
    Button sessionMaker = findViewById(R.id.sessionMakerButton);
    Button assistedHome = findViewById(R.id.assistedHome);
    Button carerHome = findViewById(R.id.carerHome);
    signIn.setOnClickListener(this);
    signUp.setOnClickListener(this);
    sessionMaker.setOnClickListener(this);
    assistedHome.setOnClickListener(this);
    carerHome.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.signInButton:
        // do your code
        break;
      case R.id.signUpButton:
        startActivity(new Intent(LaunchPadActivity.this, SignUpActivity.class));
        break;
      case R.id.sessionMakerButton:
        startActivity(new Intent(LaunchPadActivity.this, ManualRedirect.class));
        break;
      case R.id.assistedHome:
        startActivity(new Intent(LaunchPadActivity.this, HomePageActivity.class));
        break;
      case R.id.carerHome:
        startActivity(new Intent(LaunchPadActivity.this, CarerHomepageActivity.class));
        break;
      default:
        break;
    }
  }

  //  private void watchNotificationChange() {
  //    firebaseDatabase = FirebaseDatabase.getInstance();
  //    DatabaseReference notifcationRef = firebaseDatabase.getReference("users/" + id);
  //    notifcationRef.child(getResources().getString(R.string.current_channel)).setValue(getResources().getString(R.string.waiting));
  //    notifcationRef.addValueEventListener(this);
  //  }
}