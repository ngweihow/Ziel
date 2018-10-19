package com.quartz.zielclient.activities.carer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.common.SettingsActivity;
import com.quartz.zielclient.activities.common.SettingsHome;
import com.quartz.zielclient.activities.signup.SignUpActivity;
import com.quartz.zielclient.adapters.RequestListAdapter;
import com.quartz.zielclient.models.ChannelRequest;
import com.quartz.zielclient.notifications.NotificationHandler;
import com.quartz.zielclient.user.UserController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Activity to display a carer's home page.
 *
 * @author Wei How Ng
 */
public class CarerHomepageActivity extends AppCompatActivity
    implements ValueEventListener, View.OnClickListener {

  private RecyclerView mRecyclerView;
  private NotificationHandler notificationHandler;
  private DatabaseReference requestsReference;

  private boolean initialisedList = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_carer_homepage);
    Window window = getWindow();
    window.addFlags(
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    Optional<String> maybeId = UserController.retrieveUid();
    String userID;
    if (maybeId.isPresent()) {
      userID = maybeId.get();
    } else {
      startActivity(new Intent(this, SignUpActivity.class));
      finish();
      return;
    }

    ImageButton settingsButton = findViewById(R.id.carerSettingsButton);
    settingsButton.setOnClickListener(this);

    notificationHandler = new NotificationHandler(CarerHomepageActivity.this);
    notificationHandler.createNotificationChannel();
    // Getting requestsReference from FireBase
    requestsReference = FirebaseDatabase.getInstance().getReference("channelRequests/" + userID);
    requestsReference.addValueEventListener(this);

    // Initialising RecyclerView
    mRecyclerView = findViewById(R.id.my_recycler_view);

    // Each entry has fixed size.
    mRecyclerView.setHasFixedSize(true);

    // Use a linear layout manager
    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);
  }

  /**
   * Fetches the data as JSON files to
   *
   * @param channelRequestsData Collection of all appropriate channel requests.
   */
  private void initData(List<ChannelRequest> channelRequestsData) {
    Collections.sort(channelRequestsData);
    if (!initialisedList) {
      initialisedList = true;
    } else {
      notificationHandler.notifyUserToOpenApp(channelRequestsData.get(0));
    }

    // Using the Adapter to convert the data into the recycler view
    RecyclerView.Adapter mAdapter = new RequestListAdapter(channelRequestsData, this);
    mRecyclerView.setAdapter(mAdapter);
  }

  @Override
  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    // Getting the channel data and calling the rendering method on it
    // Nasty generic types needed unfortunately
    GenericTypeIndicator<List<ChannelRequest>> t =
        new GenericTypeIndicator<List<ChannelRequest>>() {
        };
    List<ChannelRequest> channelRequestsData = dataSnapshot.getValue(t);
    if (channelRequestsData != null) {
      initData(channelRequestsData);
    }
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.carerSettingsButton) {
      Intent intent = new Intent(this, SettingsHome.class);
      intent.putExtra("user", getIntent().getBundleExtra("user"));
      requestsReference.removeEventListener(this);
      startActivity(intent);
      finish();
    }
  }

  @Override
  public void onCancelled(@NonNull DatabaseError databaseError) {
  }

  // prevent going back on home page
  @Override
  public void onBackPressed() {

  }
}
