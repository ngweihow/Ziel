package com.quartz.zielclient.activities.common;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.quartz.zielclient.R;


/**
 * This class is responsible for showing a street view of a location
 *
 * @author Armaan McLeod
 * @version 1.0- 1
 * 9/09/2018
 */
public class StreetViewActivity extends AppCompatActivity implements OnStreetViewPanoramaReadyCallback {

  private LatLng destination;

  private static final int DURATION = 2000;

  private final String activity = this.getClass().getSimpleName();

  /**
   * Creates a street view of a map location.
   * <p>
   * Documentation : https://developer.android.com/reference/android/app/
   * Activity.html#onCreate(android.os.Bundle)
   *
   * @param savedInstanceState This is responsible for saving state of map activities.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_street_view);

    // Get bundle of arguments passed to this activity
    Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      destination = bundle.getParcelable("destination");
    }

    // Create toolbar
    Toolbar tb = findViewById(R.id.toolbar);
    tb.setSubtitle("Google Maps Street View");

    // Create street view fragment
    StreetViewPanoramaFragment streetViewPanoramaFragment =
        (StreetViewPanoramaFragment) getFragmentManager()
            .findFragmentById(R.id.street_view_panorama);
    streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);
  }

  /**
   * Called when the Street View panorama is ready to be used.
   * <p>
   * Documentation : https://developers.google.com/android/reference/com/google/android/gms/maps/
   * OnStreetViewPanoramaReadyCallback
   *
   * @param streetViewPanorama This is the street view panorama
   */
  @Override
  public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
    Log.d(activity, "Street view enabled for location: " + destination.toString());

    // Set camera properties
    streetViewPanorama.setPosition(destination);
    streetViewPanorama.setStreetNamesEnabled(true);
    streetViewPanorama.setPanningGesturesEnabled(true);
    streetViewPanorama.setZoomGesturesEnabled(true);
    streetViewPanorama.setUserNavigationEnabled(true);

    // Create camera
    StreetViewPanoramaCamera camera =
        new StreetViewPanoramaCamera.Builder()
            .zoom(streetViewPanorama.getPanoramaCamera().zoom)
            .tilt(streetViewPanorama.getPanoramaCamera().tilt)
            .bearing(streetViewPanorama.getPanoramaCamera().bearing - 60)
            .build();

    // Move camera to location
    streetViewPanorama.animateTo(camera, DURATION);
  }
}
