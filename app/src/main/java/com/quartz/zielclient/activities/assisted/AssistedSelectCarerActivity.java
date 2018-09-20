package com.quartz.zielclient.activities.assisted;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.quartz.zielclient.R;
import com.quartz.zielclient.adapters.CarerSelectListAdapter;
import com.quartz.zielclient.models.CarerSelectionItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This activity allows the user to select another user to estabish a channel with
 *
 * @author Bilal
 */
public class AssistedSelectCarerActivity extends AppCompatActivity implements ValueEventListener {
  private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
  private RecyclerView mRecyclerView;
  private RecyclerView.Adapter mAdapter;
  private RecyclerView.LayoutManager mLayoutManager;
  private List<CarerSelectionItem> listItems;
  private DatabaseReference requestsReference;
  private LatLng destination;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_assisted_select_carer2);
    Bundle bundle = getIntent().getExtras();

    if (bundle != null) {
      destination = bundle.getParcelable("destination");
    }

    requestsReference =
        FirebaseDatabase.getInstance().getReference("relationships/" + firebaseAuth.getUid());
    requestsReference.addValueEventListener(this);

    // Initialising RecyclerView
    mRecyclerView = findViewById(R.id.carer_list_recycler_view);

    // Each entry has fixed size.
    mRecyclerView.setHasFixedSize(true);

    // Use a linear layout manager
    mLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLayoutManager);
  }

  /**
   * Initialise the recycler view with some elements
   *
   * @param items
   */
  private void initData(HashMap<String, CarerSelectionItem> items) {
    List<CarerSelectionItem> list = new ArrayList<CarerSelectionItem>();
    items.forEach(
        (key, value) -> {
          value.setCarerId(key);
          list.add(value);
        });

    // Using the Adapter to convert the data into the recycler view
    mAdapter = new CarerSelectListAdapter(list, this);
    mRecyclerView.setAdapter(mAdapter);
  }

  /**
   * update the recycler view with user data
   *
   * @param dataSnapshot
   */
  @Override
  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    GenericTypeIndicator<HashMap<String, CarerSelectionItem>> t =
        new GenericTypeIndicator<HashMap<String, CarerSelectionItem>>() {};
    HashMap<String, CarerSelectionItem> carerSelectionItems = dataSnapshot.getValue(t);
    if (carerSelectionItems != null) {
      initData(carerSelectionItems);
    }
  }

  @Override
  public void onCancelled(@NonNull DatabaseError databaseError) {}
}
