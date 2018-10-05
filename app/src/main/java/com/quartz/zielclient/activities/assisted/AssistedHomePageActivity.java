package com.quartz.zielclient.activities.assisted;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.quartz.zielclient.R;

import java.util.Objects;

/**
 * This class is responsible for handling home page activities. This includes prompting the assisted
 * to enter in a location they wish to travel to, and passing that destination to the Maps Activity.
 *
 * @author Wei How Ng and Armaan McLeod
 * @version 1.1 19/09/2018
 */
public class AssistedHomePageActivity extends AppCompatActivity {

  private final String activity = this.getClass().getSimpleName();
  private LatLng destination;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.HomeTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_page);


    // Create autocomplete bar
    PlaceAutocompleteFragment placeAutoComplete =
        (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.editText);
    Objects.requireNonNull(placeAutoComplete.getView()).setBackgroundColor(Color.WHITE);
    placeAutoComplete.setHint("Search Place");


    // Listen for user entering place
    placeAutoComplete.setOnPlaceSelectedListener(
        new PlaceSelectionListener() {
          @Override
          public void onPlaceSelected(Place place) {
            Log.d(activity, "Place selected: " + place.getLatLng());
            destination = place.getLatLng();
          }

          @Override
          public void onError(Status status) {
            Log.d(activity, "An error occurred: " + status);
          }
        });

    // Check if direction button has been pressed
    Button directMeButton = findViewById(R.id.directMeButton);
    directMeButton.setOnClickListener(
        v -> {
          // If destination exists, start MapsActivity
          if (destination != null) {
            Intent intent = new Intent(AssistedHomePageActivity.this, AssistedSelectCarerActivity.class);
            intent.putExtra("destination", destination);
            startActivity(intent);
          } else {
            Toast.makeText(this, "Please select a place before proceeding", Toast.LENGTH_LONG)
                .show();
          }
        });
  }
}
