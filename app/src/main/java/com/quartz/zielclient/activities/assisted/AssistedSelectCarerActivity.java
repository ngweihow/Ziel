package com.quartz.zielclient.activities.assisted;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.common.SettingsHome;
import com.quartz.zielclient.adapters.CarerSelectListAdapter;
import com.quartz.zielclient.models.CarerSelectionItem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This activity allows the user to select another user to establish a channel with
 *
 * @author Bilal
 */
public class AssistedSelectCarerActivity extends AppCompatActivity implements ValueEventListener {

  private FirebaseAuth firebaseAuth = initFirebaseAuth();
  private RecyclerView mRecyclerView;

  private final String activity = this.getClass().getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_assisted_select_carer);

    Button addCarerActivity = findViewById(R.id.addCarerButton);
    addCarerActivity.setOnClickListener(
        v -> startActivity(new Intent(AssistedSelectCarerActivity.this, AddCarerActivity.class)));

    // Initialising RecyclerView
    mRecyclerView = findViewById(R.id.carer_list_recycler_view);

    // Each entry has fixed size.
    mRecyclerView.setHasFixedSize(true);

    // Use a linear layout manager
    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);

    DatabaseReference requestsReference = FirebaseDatabase.getInstance()
        .getReference("relationships/" + firebaseAuth.getUid());
    requestsReference.addValueEventListener(this);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  /**
   * Initialise the recycler view with some elements.
   *
   * @param items This is the carer selection items
   */
  private void initData(Map<String, CarerSelectionItem> items) {
    List<CarerSelectionItem> list = items.entrySet()
        .stream()
        .peek(entry -> entry.getValue().setCarerId(entry.getKey()))
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());

    // Using the Adapter to convert the data into the recycler view
    RecyclerView.Adapter mAdapter = new CarerSelectListAdapter(list, this);
    mRecyclerView.setAdapter(mAdapter);
  }

  /**
   * This method will be called with a snapshot of the data at this location.
   * It will also be called each time that data changes.
   * <p>
   * Documentation: https://www.firebase.com/docs/java-api/javadoc/com/firebase/client/
   * ValueEventListener.html
   *
   * @param dataSnapshot The data snapshot.
   */
  @Override
  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    GenericTypeIndicator<Map<String, CarerSelectionItem>> t =
        new GenericTypeIndicator<Map<String, CarerSelectionItem>>() {
        };
    Map<String, CarerSelectionItem> carerSelectionItems = dataSnapshot.getValue(t);
    if (carerSelectionItems != null) {
      initData(carerSelectionItems);
    }
  }

  /**
   * This method will be triggered in the event that this listener either failed at the server,
   * or is removed as a result of the security and Firebase rules.
   * @param databaseError A description of the error that occurred
   */
  @Override
  public void onCancelled(@NonNull DatabaseError databaseError) {
  }

  /**
   * Initialises firebase authentication for the user logged in.
   *
   * @return The firebase auth object strored in the database.
   */
  private FirebaseAuth initFirebaseAuth() {
    try {
      return FirebaseAuth.getInstance();
    } catch (IllegalStateException e) {
      Log.d(activity, e.toString());
      return null;
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      // Respond to the action bar's Up/Home button
      Intent intent = new Intent(this, AssistedHomePageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      startActivity(intent);
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}