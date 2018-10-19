package com.quartz.zielclient.activities.carer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.channel.StreetViewActivity;
import com.quartz.zielclient.activities.channel.TextChatActivity;
import com.quartz.zielclient.activities.channel.VideoActivity;
import com.quartz.zielclient.activities.channel.VoiceActivity;
import com.quartz.zielclient.channel.ChannelController;
import com.quartz.zielclient.channel.ChannelData;
import com.quartz.zielclient.channel.ChannelListener;
import com.quartz.zielclient.map.FetchUrl;
import com.quartz.zielclient.map.HTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import static android.view.View.OnClickListener;

/**
 * This activity Reads coordinate and route information from a channel and displays a Map
 * accordingly THis allows a carer to have upto date locative information from the assisted
 *
 * @author Bilal Shehata
 */
public class CarerMapsActivity extends AppCompatActivity
    implements OnMapReadyCallback, ChannelListener, OnClickListener, OnMapClickListener {

  private static boolean previousActivityWasTextChat;
  // These constants are displayed until map syncronizes (only momentarily)
  // This prevents the default usage of  0,0
  private static final double MELBOURNEUNILAT = -37.7964;
  private static final double MELBOURNEUNILONG = 144.9612;

  // initialize assisted location marker
  private final MarkerOptions assistedMarkerOptions = new MarkerOptions();

  private AlertDialog alertDialog;
  private AlertDialog endChannelAlertDialog;
  private String channelId;
  private GoogleMap mGoogleMap;
  private String currentDestinationURL = "none";
  private ImageView newMessageIcon;
  private int seenMessages = 0;
  // default to melbourne uni
  // list of Assisted movements
  private Double[] latitude = {MELBOURNEUNILAT};
  private Double[] longitude = {MELBOURNEUNILONG};
  private String key;
  private List<Marker> markers;
  private Marker assistedMarker;
  // debug channel to be replaced with the current channel that was handled by a previous activity.
  private ChannelData channel;

  /**
   * setter useful in order to not register new messages if they have already been opened.
   *
   * @param previousActivityWasTextChat The indication if the previous activity was a text chat
   */
  public static void setPreviousActivityWasTextChat(boolean previousActivityWasTextChat) {
    CarerMapsActivity.previousActivityWasTextChat = previousActivityWasTextChat;
  }


  private final String ACTIVITY = this.getClass().getSimpleName();

  /**
   * Creates map along with its attributes.
   *
   * <p>Documentation : https://developer.android.com/reference/android/app/
   * Activity.html#onCreate(android.os.Bundle)
   *
   * @param savedInstanceState This is responsible for saving state of map activities.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // set xml view file
    setContentView(R.layout.activity_carer_maps);
    key = "&key=" + getApplicationContext().getString(R.string.google_api_key);
    markers = new ArrayList<>();

    // Create buttons
    Button toTextChat = findViewById(R.id.toTextChat);
    Button toVoiceChat = findViewById(R.id.toVoiceChat);
    Button dropMarkers = findViewById(R.id.dropMarker);
    Button clearMarkers = findViewById(R.id.clearMarker);
    Button toVideoChat = findViewById(R.id.toVideoActivity);
    newMessageIcon = findViewById(R.id.newMessageIcon);
    readMessages();

    // Setup listeners
    dropMarkers.setOnClickListener(this);
    clearMarkers.setOnClickListener(this);
    toVideoChat.setOnClickListener(this);
    toVoiceChat.setOnClickListener(this);
    alertDialog = makeVideoAlert();
    toTextChat.setOnClickListener(this);

    // Extract the channel id from the bundle
    channelId = getIntent().getStringExtra(getApplicationContext().getString(R.string.channel_key));
    if (channelId != null) {
      channel = ChannelController.retrieveChannel(channelId, this);
    }

    Intent intentVoice = new Intent(CarerMapsActivity.this, VoiceActivity.class);
    intentVoice.putExtra("initiate", 1);
    startActivity(intentVoice);

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    if (mapFragment != null) {
      mapFragment.getMapAsync(this);
    }
  }

  @Override
  public void onStart() {
    if (previousActivityWasTextChat) {
      readMessages();
      if (channel != null) {
        seenMessages = channel.getMessages().size();
      }
      previousActivityWasTextChat = false;
    }
    super.onStart();
  }

  /**
   * Manipulates the map once available. Once map is ready add a temporary marker (once again The
   * University of Melbourne is used temporarily rather than the default NUll-Island)
   *
   * @param googleMap This is the current Google Map fragment
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    mGoogleMap = googleMap;
    // place a marker on melbourne uni whilst synchronization occurs
    assistedMarkerOptions.position(new LatLng(MELBOURNEUNILAT, MELBOURNEUNILONG));
    assistedMarkerOptions.title("Assisted Location");
    assistedMarker = mGoogleMap.addMarker(assistedMarkerOptions);
    updateMapCoords();
    mGoogleMap.setOnMarkerClickListener(
        marker -> {
          marker.showInfoWindow();

          // Prompt Street view
          new AlertDialog.Builder(this)
              .setIcon(R.drawable.street_view_logo)
              .setTitle("Google Maps Street View")
              .setMessage("Show street view?")

              // Start Street view activity when pressed
              .setPositiveButton(
                  "Yes",
                  (dialog, which) -> {
                    Intent intent = new Intent(CarerMapsActivity.this, StreetViewActivity.class);
                    intent.putExtra("destination", marker.getPosition());
                    startActivity(intent);
                  })
              .setNegativeButton("No", (dialog, which) -> {
              })
              .show();
          return true;
        });
  }

  /**
   * Update the Coordinates based on the latest Assisted's location
   */
  public void updateMapCoords() {
    LatLng assistedLocation = new LatLng(latitude[0], longitude[0]);
    // Safety check
    // Asynchronous map may lead to error this ensures that the database call
    // does update if map is not ready
    if (assistedMarker != null) {
      assistedMarker.remove();
      assistedMarkerOptions.position(assistedLocation);
      assistedMarker = mGoogleMap.addMarker(assistedMarkerOptions);
      mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(assistedLocation));
      mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(assistedLocation, 15));
    }
  }

  /**
   * This listens to changes in the channel Once a change occurs update the long/lat values
   */
  @Override
  public void dataChanged() {
    // Update location
    if (channel != null) {
      latitude[0] = channel.getAssistedLocation().latitude;
      longitude[0] = channel.getAssistedLocation().longitude;
      updateMapCoords();
      channel.setCarerStatus(true);

      // if the assisted has entered a route then generate that same route
      if ((channel.getDirectionsURL() != null)
          && !channel.getDirectionsURL().equals("none")
          && !channel.isChannelEnded()) {
        // if the route is already the current route then don't update
        if (!channel.getDirectionsURL().equals(currentDestinationURL) && mGoogleMap != null) {
          // update the route
          mGoogleMap.clear();
          deleteMarkers();
          updateMapCoords();

          Log.d("DIRECTIONS", channel.getDirectionsURL());
          FetchUrl fetchUrl = new FetchUrl(mGoogleMap);
          fetchUrl.execute(channel.getDirectionsURL() + key);
          currentDestinationURL = channel.getDirectionsURL();
        }
      }

      if (seenMessages < channel.getMessages().size()) {
        unReadMessages();
      }

      if (channel.isChannelEnded()) {
        Log.d("ENDED", "CHANNEL ENDED");
        if (!this.isFinishing()) {
          makeChannelEndedAlert();
        }
      }

      if (channel.getVideoCallStatus()) {
        alertDialog.show();
      } else {
        alertDialog.cancel();
      }
    }
  }

  /**
   * Listens for click events for the buttons.
   *
   * <p>Documentation: https://developer.android.com/reference/android/view/
   * View.OnClickListener#onClick(android.view.View)
   *
   * @param view The view that was clicked.
   */
  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.toTextChat:
        Intent intentToTextChat = new Intent(CarerMapsActivity.this, TextChatActivity.class);
        readMessages();
        intentToTextChat.putExtra(
            getApplicationContext().getString(R.string.channel_key), channelId);
        startActivity(intentToTextChat);
        break;
      case R.id.toVoiceChat:
        Intent intentVoice = new Intent(CarerMapsActivity.this, VoiceActivity.class);
        if (channel != null && channel.getAssisted() != null) {
          intentVoice.putExtra(getResources().getString(R.string.channel_key), channelId);
          intentVoice.putExtra("initiate", 0);
          intentVoice.putExtra("CallId", channel.getAssisted());
          startActivity(intentVoice);
        }
        break;
      case R.id.toVideoActivity:
        Intent intentToVideo = new Intent(CarerMapsActivity.this, VideoActivity.class);
        intentToVideo.putExtra(getResources().getString(R.string.channel_key), channelId);
        startActivity(intentToVideo);
        break;
      case R.id.dropMarker:
        mGoogleMap.setOnMapClickListener(this);
        Toast.makeText(this, "Press on map now", Toast.LENGTH_LONG).show();
        break;
      case R.id.clearMarker:
        deleteMarkers();
        break;
      default:
        break;
    }
  }

  /**
   * Handle back press from a user.
   */
  @Override
  public void onBackPressed() {
    VoiceActivity.endCall();
    alertDialog.dismiss();
    channel = null;
    finish();
  }

  /**
   * Create a video alert when video activity starts.
   *
   * @return AlertDialog The dialog to show up on the screen.
   */
  public AlertDialog makeVideoAlert() {
    alertDialog = new AlertDialog.Builder(CarerMapsActivity.this).create();
    alertDialog.setTitle("Video Share?");
    alertDialog.setMessage("Carer wants to share video with you  please also join the channel");
    if (alertDialog.getWindow() != null) {
      alertDialog
          .getWindow()
          .setFlags(
              WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
              WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
    }
    alertDialog.setButton(
        AlertDialog.BUTTON_NEUTRAL,
        "OK",
        (dialog, which) -> {
          Intent intentToVideo = new Intent(CarerMapsActivity.this, VideoActivity.class);
          intentToVideo.putExtra(
              getApplicationContext().getResources().getString(R.string.channel_key), channelId);
          startActivity(intentToVideo);
        });
    return alertDialog;
  }

  /**
   * Deletes all markers from a list and map.
   */
  private void deleteMarkers() {
    markers.forEach(Marker::remove);
    markers.clear();
    channel.clearMarkers();
  }

  /**
   * Creates a marker and shows it on the Google map.
   *
   * @param location The location of marker.
   */
  private void placeMarker(LatLng location) {
    // Get snapped location for road
    Optional<LatLng> maybeLocation = getSnappedLocation(location);
    LatLng markerLocation = maybeLocation.orElse(location);

    MarkerOptions markerOptions = new MarkerOptions()
        .position(markerLocation)
        .icon(BitmapDescriptorFactory.defaultMarker((float) 255));
    Marker newMarker = mGoogleMap.addMarker(markerOptions);
    markers.add(newMarker);
  }

  /**
   * Gets snapped road location of road.
   *
   * @param location The current location
   * @return LatLng The snapped location
   */
  private Optional<LatLng> getSnappedLocation(LatLng location) {
    String url = getNearstRoadUrl(location);

    // creates a pool of threads for the Future to draw from
    ExecutorService pool = Executors.newFixedThreadPool(2);

    // Execute threads
    Future<Optional<LatLng>> value = pool.submit(() -> {
      String data = getNearestRoadData(url);

      // Parse nearest road
      return parseNearestRoad(data);
    });

    try {
      return value.get();
    } catch (ExecutionException | InterruptedException e) {
      Log.e(ACTIVITY, "Error fetching road data", e);
      return Optional.empty();
    }
  }

  /**
   * Gets nearest road from JSON string
   *
   * @param data the JSON data
   * @return LatLng The nearest road.
   */
  private Optional<LatLng> parseNearestRoad(String data) {
    try {

      // Get the first nearest road from the JSON
      JSONObject jsonObject = new JSONObject(data);
      JSONArray snappedPoints = jsonObject.getJSONArray("snappedPoints");
      JSONObject firstRoad = snappedPoints.getJSONObject(0);
      JSONObject location = firstRoad.getJSONObject("location");

      return Optional.of(
          new LatLng(location.getDouble("latitude"), location.getDouble("longitude"))
      );

    } catch (JSONException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  /**
   * Gets nearest road JSON data in String format.
   *
   * @param url The String API url.
   * @return String the JSON data,
   */
  private String getNearestRoadData(String url) {
    String data = "";
    try {
      data = HTTP.downloadUrl(url);
    } catch (IOException e) {
      Log.d(ACTIVITY, e.toString());
    }

    return data;
  }

  /**
   * Create Nearest road URL from a location
   *
   * @param location The location of the marker.
   * @return String The API url to submit.
   */
  private String getNearstRoadUrl(LatLng location) {
    String coordinates = "points=" + location.latitude + "," + location.longitude;
    String nearestRoadApi = "https://roads.googleapis.com/v1/nearestRoads?";
    return nearestRoadApi + coordinates + key;
  }

  /**
   * Handle map clicks for markers.
   *
   * @param latLng The coordinate of the marker.
   */
  @Override
  public void onMapClick(LatLng latLng) {
    channel.addMarker(latLng);
    placeMarker(latLng);
    mGoogleMap.setOnMapClickListener(null);
  }

  /**
   * Alerts user when the channel is ended.
   */
  public void makeChannelEndedAlert() {
    Log.d("CREATING DIALOG", "DIALOG BUILD");
    endChannelAlertDialog = new AlertDialog.Builder(CarerMapsActivity.this).create();
    endChannelAlertDialog.setTitle("Channel has finished");
    endChannelAlertDialog.setMessage("This channel has been ended. Will now return to home page");
    endChannelAlertDialog
        .getWindow()
        .setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
    endChannelAlertDialog.setButton(
        AlertDialog.BUTTON_NEUTRAL,
        "OK",
        (dialog, which) -> {
          channel.endChannel();
          endChannelAlertDialog.dismiss();
          setPreviousActivityWasTextChat(false);
          VoiceActivity.endCall();
          Intent intent = new Intent(CarerMapsActivity.this, CarerHomepageActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          startActivity(intent);
          finish();
        });

    if (!isFinishing()) {
      endChannelAlertDialog.show();
    }
  }

  /**
   * Make message icon invisible is all messages have been read
   */
  public void readMessages() {
    newMessageIcon.setVisibility(View.INVISIBLE);
  }

  /**
   * Make messages icon invisible if all messages have been read
   */
  public void unReadMessages() {
    newMessageIcon.setVisibility(View.VISIBLE);
  }
}
