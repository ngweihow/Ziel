package com.quartz.zielclient.activities.carer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.signup.SignUpActivity;
import com.quartz.zielclient.channel.ChannelController;
import com.quartz.zielclient.channel.ChannelData;
import com.quartz.zielclient.channel.ChannelListener;
import com.quartz.zielclient.exceptions.AuthorisationException;
import com.quartz.zielclient.user.UserController;

/**
 * This activity allows the Carer to accept a session with an assisted and allows them to establish
 * redimentary communication.
 *
 * @author Bilal Shehata
 */
public class CarerChannel extends AppCompatActivity
        implements View.OnClickListener, ChannelListener {

  private static final String TAG = CarerChannel.class.getSimpleName();

  private TextView status;
  private Button acceptButton;
  // initialise Notification manager will allow push-notifcations on the device

  private String channelId;
  private String id;
  private ChannelData channelData;
  private Button toTrackingButton;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.acitivty_carer_session);
    toTrackingButton = findViewById(R.id.toTrackingActivity);
    toTrackingButton.setOnClickListener(this);
    toTrackingButton.setVisibility(View.INVISIBLE);
    try {
      id = UserController.retrieveFirebaseUser().getUid();
    } catch (AuthorisationException e) {
      Log.w(TAG, "User not signed in.");
      startActivity(new Intent(this, SignUpActivity.class));
      finish();
    }

    channelId = getIntent().getStringExtra(getResources().getString(R.string.channel_key));

    acceptButton = findViewById(R.id.acceptButton);
    acceptButton.setOnClickListener(this);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    status = findViewById(R.id.channelStatus);

    Toast.makeText(getApplicationContext(), "request has been made", Toast.LENGTH_LONG).show();

    // begin listening to the session
    channelData = ChannelController.retrieveChannel(channelId, this);
  }

  @Override
  public void onBackPressed() {
    if (channelData != null) {
      channelData.setCarerStatus(false);
    }
    super.onBackPressed();
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.acceptButton:
        channelData.setCarerStatus(true);
        break;
      case R.id.toTrackingActivity:
        Intent intentToTracking = new Intent(CarerChannel.this, CarerMapsActivity.class);
        intentToTracking.putExtra(getResources().getString(R.string.channel_key), channelId);
        startActivity(intentToTracking);
        break;
      default:
        break;
    }
  }

  /**
   * notification that the data has changed on the channelData
   */
  @Override
  public void dataChanged() {
    Log.i(TAG, "Channel data has changed.");
    // check values on channelData and modify state accordingly
    if (channelData.getAssistedStatus()) {
      status.setText("ChannelData is active ");
      toTrackingButton.setVisibility(View.VISIBLE);

    } else {
      status.setText("ChannelData is inactive");
    }

    if (channelData.getPing()) {
      channelData.setPing(false);
      Toast.makeText(getApplicationContext(), "Assisted has waved", Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public String getAssistedId() {
    return null;
  }

  @Override
  public String getCarerId() {
    return id;
  }
}
