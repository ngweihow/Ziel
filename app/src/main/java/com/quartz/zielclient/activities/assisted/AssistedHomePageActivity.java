package com.quartz.zielclient.activities.assisted;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.common.SettingsHome;

import java.util.Objects;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

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

  private static final int REQUEST_LOCATION_PERMISSION = 1;

  private boolean permissionGranted;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.HomeTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home_page);

    ImageButton settingsPageButton = findViewById(R.id.settingsPageButton);
    settingsPageButton.setOnClickListener(v -> {
      Intent intent = new Intent(AssistedHomePageActivity.this, SettingsHome.class);
      intent.putExtra("user", getIntent().getBundleExtra("user"));
      startActivity(intent);
      finish();
    });

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

    // Restrict search results only to Australia
    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
        .setCountry("AU")
        .build();

    placeAutoComplete.setFilter(typeFilter);

    // Check if direction button has been pressed
    Button directMeButton = findViewById(R.id.directMeButton);
    directMeButton.setOnClickListener(
        v -> {
          // First make sure permission is granted before continuing
          if (!permissionGranted) {
            requestLocationPermission();
          } else {
            // If destination exists, start MapsActivity
            if (destination != null) {
              Intent intent = new Intent(AssistedHomePageActivity.this,
                  AssistedSelectCarerActivity.class);
              intent.putExtra("destination", destination);
              startActivity(intent);
            } else {
              Toast.makeText(this, "Please select a place before proceeding",
                  Toast.LENGTH_LONG)
                  .show();
            }
          }
        });

    // Verify location permission anyways
    requestLocationPermission();
  }

  /**
   * This is a callback for requesting and checking the result of a permission.
   *
   * <p>Documentation : https://developer.android.com/reference/android/support/v4/app/
   * ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult
   *
   * @param requestCode  This is the request code passed to requestPermissions.
   * @param permissions  This is the permissions.
   * @param grantResults This is results for granted or un-granted permissions.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    // Forward results to EasyPermissions
    EasyPermissions.onRequestPermissionsResult(requestCode,
        permissions, grantResults, this);
  }

  /**
   * Check location permissions before showing user location.
   */
  @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
  public void requestLocationPermission() {
    String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
    if (EasyPermissions.hasPermissions(this, perms)) {
      Log.i(activity, "Location Permission already granted");
      permissionGranted = true;
    } else {
      EasyPermissions.requestPermissions(this,
          "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
      permissionGranted = false;
    }
  }

  /**
   * Called when the activity has detected the user's press of the back key.
   * The default implementation simply finishes the current activity,
   * but you can override this to do whatever you want.
   *
   * Documentation: https://developer.android.com/reference/android/app/Activity.html#onBackPressed()
   */
  @Override
  public void onBackPressed() {
    // Intentionally empty
  }
}
