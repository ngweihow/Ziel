package com.quartz.zielclient.activities.carer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.common.TextChatActivity;
import com.quartz.zielclient.channel.ChannelController;
import com.quartz.zielclient.channel.ChannelData;
import com.quartz.zielclient.channel.ChannelListener;
import com.quartz.zielclient.map.FetchUrl;

/**
 * This activity Reads coordinate and route information from a channel and displays a Map accordigly
 * THis allows a carer to have upto date locative information from the assisted
 *
 * @author Bilal Shehata
 */
public class CarerMapsActivity extends AppCompatActivity
    implements OnMapReadyCallback, ChannelListener, View.OnClickListener {

  // These constants are displayed until map syncronizes (only momentarily)
  // This prevents the default usage of  0,0
  private final double MELBOURNEUNILAT = -37.7964;
  private final double MELBOURNEUNILONG = 144.9612;
  // initialize assisted location marker
  private final MarkerOptions assistedMarkerOptions = new MarkerOptions();
  private String channelId;
  private GoogleMap mGoogleMap;
  private String currentDestinationURL = "none";
  // default to melbourne uni
  // list of Assisted movements
  private Double[] latitude = {MELBOURNEUNILAT};
  private Double[] longitude = {MELBOURNEUNILONG};
  private Button toTextChat;
  private Marker assistedMarker;

  // debug channel to be replaced with the current channel that was handled by a previous activity.
  private ChannelData channel;
  /**
   * Initizialise the activity
   *
   * @param savedInstanceState
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // set xml view file
    setContentView(R.layout.activity_carer_maps);
    toTextChat = findViewById(R.id.toTextChat);
    toTextChat.setOnClickListener(this);
    channelId = getIntent().getStringExtra(getApplicationContext().getString(R.string.channel_key));
    channel = ChannelController.retrieveChannel(channelId, this);

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }
  }

  /**
   * Manipulates the map once available. Once map is ready add a temporary marker (once again The
   * University of Melbourne is used temporarily rather than the default NUll-Island)
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    mGoogleMap = googleMap;
    // place a marker on melbourne uni whilst synchronization occurs
    assistedMarkerOptions.position(new LatLng(MELBOURNEUNILAT, MELBOURNEUNILONG));
    assistedMarkerOptions.title("Assisted Location");
    assistedMarker = mGoogleMap.addMarker(assistedMarkerOptions);
    updateMapCoords();
  }

  /** Update the Coordinates based on the latest Assisted's location */
  public void updateMapCoords() {
    LatLng assistedLocation = new LatLng(latitude[0], longitude[0]);
    // Safety check
    // Asynchronous map may lead to error this ensures that the database call
    // does update if map is not ready
    if (assistedMarker != null) {
      assistedMarker.setPosition(assistedLocation);
      mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(assistedLocation));
      mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(assistedLocation, 13f));
    }
  }

  /** THis listens to changes in the channel Once a change occurs update the long/lat values */
  @Override
  public void dataChanged() {
    // Update location
    latitude[0] = channel.getAssistedLocation().latitude;
    longitude[0] = channel.getAssistedLocation().longitude;
    updateMapCoords();

    // if the assisted has entered a route then generate that same route
    if ((channel.getDirectionsURL() != null) && !channel.getDirectionsURL().equals("none")) {
      // if the route is already the current route then don't update
      if (!channel.getDirectionsURL().equals(currentDestinationURL)) {
        // update the route
        Log.d("DIRECTIONS", channel.getDirectionsURL());
        FetchUrl fetchUrl = new FetchUrl(mGoogleMap);
        fetchUrl.execute(channel.getDirectionsURL());
        currentDestinationURL = channel.getDirectionsURL();
      }
    }
  }

  // return the carer and assisted associated to this activtiy
  // should become getUser.Id and getAssisted.ID
  @Override
  public String getAssistedId() {
    return "Assisted1";
  }

  @Override
  public String getCarerId() {
    return "carer1";
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.toTextChat:
        Intent intentToTextChat = new Intent(CarerMapsActivity.this, TextChatActivity.class);
        intentToTextChat.putExtra(
            getApplicationContext().getString(R.string.channel_key), channelId);
        startActivity(intentToTextChat);
        break;
      default:
        break;
    }
  }
}
