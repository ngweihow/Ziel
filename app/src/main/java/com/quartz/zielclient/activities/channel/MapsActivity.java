package com.quartz.zielclient.activities.channel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.assisted.AssistedHomePageActivity;
import com.quartz.zielclient.channel.ChannelController;
import com.quartz.zielclient.channel.ChannelData;
import com.quartz.zielclient.channel.ChannelListener;
import com.quartz.zielclient.map.FetchUrl;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_CYAN;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_MAGENTA;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;

/**
 * This class is responsible for handling all map activities.
 *
 * <p>Courtesy : https://stackoverflow.com/questions/44992014/
 * how-to-get-current-location-in-googlemap-using-fusedlocationproviderclient/44993694#44993694
 *
 * @author Armaan McLeod
 * @version 1.1 19/09/2018
 */
public class MapsActivity extends AppCompatActivity
    implements OnMapReadyCallback, ChannelListener, View.OnClickListener {

  private static final int DEFAULT_ZOOM = 15;
  private static final String API_URL = "https://maps.googleapis.com/maps/api/directions/json?";
  private static final String STREETVIEW_URL = "https://maps.googleapis.com/maps/api/streetview?";

  private static final long UPDATE_INTERVAL = 10000;  /* 10 secs */
  private static final long FASTEST_INTERVAL = 2000; /* 2 sec */

  private final String activity = this.getClass().getSimpleName();
  private final LocationCallback mLocationCallback = locationCallBackMaker();
  private GoogleMap mGoogleMap;
  private TextView waitingMessage;

  private LocationRequest mLocationRequest;
  private FusedLocationProviderClient mFusedLocationClient;
  private int seenMessages = 0;
  private Button toVideoChatButton;
  private Button toTextChatButton;
  private Button toVoiceChatButton;
  private Button endChannelButton;
  private Button toRedrawRouteButton;
  private ImageView newMessageIcon;
  private LatLng source;
  private boolean isAssisted;

  private List<Marker> sourceDestinationMarkers = new ArrayList<>();
  private List<Marker> dropMarkers = new ArrayList<>();

  private LatLng destination;

  private LatLng currentDestination;
  private String channelId;

  private AlertDialog alertDialog;
  private ChannelData channel;
  private static Boolean previousActivityWasTextChat = false;

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
    setContentView(R.layout.activity_maps);

    // Initialise channel
    channelId = getIntent().getStringExtra(getResources().getString(R.string.channel_key));
    if (channelId != null) {
      channel = ChannelController.retrieveChannel(channelId, this);
    }

    Intent intentVoice = new Intent(MapsActivity.this, VoiceActivity.class);
    intentVoice.putExtra("initiate", 1);

    isAssisted = getIntent().getBooleanExtra("isAssisted",false);
    startActivity(intentVoice);
    alertDialog = makeVideoAlert();

    // Create buttons and listeners below
    endChannelButton = findViewById(R.id.endChannelButton);

    toRedrawRouteButton = findViewById(R.id.toRedrawRouteButton);
    toRedrawRouteButton.setOnClickListener(this);

    waitingMessage = findViewById(R.id.waitForCarerMessage);
    toVideoChatButton = findViewById(R.id.toVideoChatButton);
    toVideoChatButton.setVisibility(View.INVISIBLE);
    toVideoChatButton.setOnClickListener(this);

    toTextChatButton = findViewById(R.id.toTextChat);
    toTextChatButton.setOnClickListener(this);

    toVoiceChatButton = findViewById(R.id.toVoiceChat);
    toVoiceChatButton.setVisibility(View.INVISIBLE);
    toVoiceChatButton.setOnClickListener(this);
    newMessageIcon = findViewById(R.id.newMessage);
    readMessages();
    Button toTakePhotoButton = findViewById(R.id.toTakePhotoButton);
    toTakePhotoButton.setOnClickListener(this);

    // Get bundle of arguments passed from Home Page Activity
    Bundle bundle = getIntent().getExtras();
    if (bundle != null) {
      destination = bundle.getParcelable("destination");
    }

    // Create autocomplete bar
    PlaceAutocompleteFragment placeAutoComplete =
        (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete);
    Objects.requireNonNull(placeAutoComplete.getView()).setBackgroundColor(Color.WHITE);

    // Listen for new places queried in search bar
    placeAutoComplete.setOnPlaceSelectedListener(
        new PlaceSelectionListener() {

          @Override
          public void onPlaceSelected(Place place) {
            // Clear all previous points on map
            mGoogleMap.clear();

            // Update destination
            Log.d(activity, "Place selected: " + place.getLatLng());
            currentDestination = null;
            destination = place.getLatLng();

            // Zoom in on map location
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, DEFAULT_ZOOM));
          }

          @Override
          public void onError(@NonNull Status status) {
            Log.d(activity, "An error occurred: " + status);
          }
        });

    // Restrict search results only to Australia
    AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
        .setCountry("AU")
        .build();

    placeAutoComplete.setFilter(typeFilter);

    // Create fused location client to interact with API
    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    // Place map in application
    SupportMapFragment mapFrag =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

    if (mapFrag != null) {
      mapFrag.getMapAsync(this);
    }

    // Allow user to see street view suggestion
    Toast streetviewSuggestion = Toast.makeText(this,
        "Click on a marker to see street view", Toast.LENGTH_LONG);
    streetviewSuggestion.setGravity(Gravity.BOTTOM, 0, 250);
    streetviewSuggestion.show();
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
   * Draws route between two points on the map
   */
  private void drawRoute() {

    // Compute path to destination
    String directionsURL = getDirectionsUrl();

    FetchUrl fetchUrl = new FetchUrl(mGoogleMap);
    fetchUrl.execute(directionsURL);

    // Send directions to channel
    if (channel != null) {
      channel.setDirectionsURL(directionsURL);
    }
  }

  /**
   * Gets the address of a location.
   *
   * @param location This is the location.
   * @return String This is the address in String format.
   */
  private String getAddress(@NonNull LatLng location) {
    String address = "";

    // Create address geo coder
    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
    try {
      // Only retrieve the rop result
      List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
      if (!addresses.isEmpty()) {
        address = addresses.get(0).getAddressLine(0);
      }
    } catch (IOException e) {
      Log.e(activity, "getAddress: Cannot fetch address", e);
    }

    return address;
  }

  /**
   * This initialises map activities and checks permissions.
   *
   * @param googleMap This is the Google Map
   */
  @Override
  public void onMapReady(@NonNull GoogleMap googleMap) {

    // Initialise Google map
    mGoogleMap = googleMap;

    // Set listener for markers
    mGoogleMap.setOnMarkerClickListener(
        marker -> {
          marker.showInfoWindow();

          LatLng location = marker.getPosition();

          // Create street view url
          String parameters = "location=" + location.latitude + "," + location.longitude;
          String key = "&key=" + getBaseContext().getString(R.string.google_api_key);
          String streetViewSize = "&size=600x400";
          String url = STREETVIEW_URL + parameters + streetViewSize + key;

          // Setup image view
          View view = View.inflate(this, R.layout.dialog_layout, null);
          ImageView imageView = view.findViewById(R.id.dialog_imageview);
          Picasso.get().load(url).into(imageView);

          // Prompt Street view
          new AlertDialog.Builder(this)
              .setIcon(R.drawable.street_view_logo)
              .setTitle("Google Maps Street View")
              .setMessage("Show street view?")

              // Start Street view activity when pressed
              .setPositiveButton("Yes", (dialog, which) -> {
                Intent intent = new Intent(MapsActivity.this, StreetViewActivity.class);
                intent.putExtra("destination", marker.getPosition());
                startActivity(intent);
              })
              .setNegativeButton("No", (dialog, which) -> {

              })
              .setView(view)
              .show();
          return true;
        });


    // Setup location request and intervals between requests
    mLocationRequest = new LocationRequest();
    mLocationRequest.setInterval(UPDATE_INTERVAL); // two minute interval
    mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    mLocationRequest.setPriority(PRIORITY_BALANCED_POWER_ACCURACY);

    requestLocation();
  }

  /**
   * This enables the location to be shown on the map.
   */
  private void requestLocation() {
    // Permission was granted so we can enable user location
    if (checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
      mFusedLocationClient.requestLocationUpdates(
          mLocationRequest, mLocationCallback, Looper.myLooper());
      mGoogleMap.setMyLocationEnabled(true);
    }
  }

  /**
   * Builds URL from Directions API web service.
   *
   * @return String This is the new url pointing to the API endpoint.
   */
  private String getDirectionsUrl() {

    // Source and destination formats
    String strSource = "origin=" + source.latitude + "," + source.longitude;
    String strDestination = "destination=" + destination.latitude + "," + destination.longitude;

    // Sensor initialisation
    String travelMode = "mode=walking";
    String sensor = "sensor=false";
    String key = "&key=" + getBaseContext().getString(R.string.google_api_key);
    // Building the parameters to the web service
    String parameters = strSource + "&" + strDestination + "&" + sensor + '&' + travelMode + key;

    // Add parameters to api url
    String apiRequest = API_URL + parameters;

    Log.d(activity, "Directions request sent to " + apiRequest);

    // Building the url to the web service
    return apiRequest;
  }

  /**
   * CChecks video call status for channel.
   */
  @Override
  public void dataChanged() {
    // notify user about new messages
    if (channel != null) {
      if (channel.getVideoCallStatus()) {
        alertDialog.show();
      } else {
        alertDialog.cancel();
      }

      if (channel.isChannelEnded() && !this.isFinishing()) {
        // this is set to null on purpose and will not cause an error.
        makeChannelEndedAlert(null);
      }
      if (seenMessages < channel.getMessages().size()) {
        unReadMessages();
      }
      if (channel.getCarerStatus()) {
        toTextChatButton.setVisibility(View.VISIBLE);
        toVideoChatButton.setVisibility(View.VISIBLE);
        toVoiceChatButton.setVisibility(View.VISIBLE);
        waitingMessage.setVisibility(View.INVISIBLE);
      }
    }
  }

  /**
   * Called when a view has been clicked.
   *
   * @param view This is the view that was clicked.
   */
  @Override
  public void onClick(@NonNull View view) {
    switch (view.getId()) {

      case R.id.toTextChat:
        Intent intentToTextChat = new Intent(MapsActivity.this, TextChatActivity.class);
        intentToTextChat.putExtra(getResources().getString(R.string.channel_key), channelId);
        intentToTextChat.putExtra("isAssisted",isAssisted);
        readMessages();
        startActivity(intentToTextChat);
        break;

      case R.id.toVoiceChat:
        Intent intentVoice = new Intent(MapsActivity.this, VoiceActivity.class);
        if (channel != null && channel.getCarer() != null) {
          intentVoice.putExtra(getResources().getString(R.string.channel_key), channelId);
          intentVoice.putExtra("initiate", 0);
          intentVoice.putExtra("CallId", channel.getCarer());
          startActivity(intentVoice);
        }

        break;

      case R.id.toVideoChatButton:
        Intent intentToVideo = new Intent(MapsActivity.this, VideoActivity.class);
        intentToVideo.putExtra(getResources().getString(R.string.channel_key), channelId);
        startActivity(intentToVideo);
        break;

      case R.id.toTakePhotoButton:
        Intent intentToPhoto = new Intent(MapsActivity.this, TakePhotosActivity.class);
        startActivity(intentToPhoto);
        break;

      // Clear the map if we want to redraw route
      case R.id.toRedrawRouteButton:
        mGoogleMap.clear();
        currentDestination = null;
        break;
      default:
        break;
    }
  }

  @Override
  public void onBackPressed() {
    VoiceActivity.endCall();
    channel.endChannel();
    // purposely null will not cause an error.
    makeChannelEndedAlert(null);
  }

  /**
   * Makes alert for assisted to join carer in video call.
   *
   * @return AlertDialog A alert dialog box for the assisted to see.
   */
  public AlertDialog makeVideoAlert() {
    alertDialog = new AlertDialog.Builder(this).create();
    alertDialog.setTitle("Video Share?");
    alertDialog.setMessage("Carer wants to share video with you  please also join the channel");
    alertDialog.setButton(
        AlertDialog.BUTTON_NEUTRAL,
        "OK",
        (dialog, which) -> {
          Intent intentToVideo = new Intent(MapsActivity.this, VideoActivity.class);
          intentToVideo.putExtra(
              getApplicationContext().getResources().getString(R.string.channel_key), channelId);
          startActivity(intentToVideo);
        });
    return alertDialog;
  }


  /**
   * Make channel end with dialog showed to the user.
   * @param v The current view.
   */
  public void makeChannelEndedAlert(View v){
    alertDialog = new AlertDialog.Builder(this).create();
    alertDialog.setTitle("Channel has finished");
    alertDialog.setMessage("This channel has been ended. Will now return to home page");
    alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
    alertDialog.setButton(
        AlertDialog.BUTTON_NEUTRAL,
        "OK",
        (dialog, which) -> {
          channel.endChannel();
          alertDialog.dismiss();
          VoiceActivity.endCall();
          Intent intent = new Intent(getApplicationContext(), AssistedHomePageActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          startActivity(intent);
          finish();

        });

    alertDialog.show();

  }

  /**
   * Draws markers to map from the carer.
   *
   * @param coordinates This is the coordinates passed from the carer.
   */
  private void drawMarkers(List<LatLng> coordinates) {
    dropMarkers.addAll(
        coordinates.stream()
            .map(coord -> createMarker(coord, HUE_CYAN))
            .collect(Collectors.toList())
    );
  }

  /**
   * Deletes markers from a list.
   *
   * @param markers the markers stored in the list.
   */
  private void deleteMarkers(List<Marker> markers) {
    markers.forEach(Marker::remove);
    markers.clear();
  }

  /**
   * Creates a marker and shows it on the Google map.
   *
   * @param location The location of marker.
   * @param colour   The colour of marker.
   * @return Marker The marker object.
   */
  private Marker createMarker(LatLng location, float colour) {
    MarkerOptions markerOptions = new MarkerOptions()
        .position(location)
        .title(getAddress(location))
        .icon(BitmapDescriptorFactory.defaultMarker(colour));
    return mGoogleMap.addMarker(markerOptions);
  }

  // Location callback that continually polls Google services API for location updates.
  private LocationCallback locationCallBackMaker() {
    return new LocationCallback() {

      /**
       * Moves camera to last known location of user.
       *
       * @param locationResult location results fetched from API.
       */
      @Override
      public void onLocationResult(LocationResult locationResult) {

        // All previous locations
        List<Location> locationList = locationResult.getLocations();

        // If one location exists
        if (!locationList.isEmpty()) {

          // The last location in the list is the newest
          Location location = locationList.get(locationList.size() - 1);
          Log.i(activity, "Location: " + location.getLatitude() + " " + location.getLongitude());

          LatLng newSource = new LatLng(location.getLatitude(), location.getLongitude());

          // Only draw onto map for first callback or if source location has changed.
          // Ensures directions api doesn't get called too many times on start up.
          // draw both source and destination markers to map screen
          // Execute channel is available
          if (channel != null) {
            channel.setAssistedLocation(location);
            deleteMarkers(dropMarkers);
            drawMarkers(channel.getCarerMarkerList());
          }

          source = newSource;

          // clear destination and source
          deleteMarkers(sourceDestinationMarkers);

          // Source and Destination markers
          Marker sourceMarker = createMarker(source, HUE_MAGENTA);
          sourceDestinationMarkers.add(sourceMarker);

          Marker destinationMarker = createMarker(destination, HUE_RED);
          sourceDestinationMarkers.add(destinationMarker);

          Log.d("DESTINATION CHANGE", destination.toString());
          if (!destination.equals(currentDestination)) {
            currentDestination = destination;
            drawRoute();

            // Zoom in on map location
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newSource, DEFAULT_ZOOM));
          }
        }
      }

    };
  }


  /**
   * indicates that messages have been read
   */
  public void readMessages() {
    newMessageIcon.setVisibility(View.INVISIBLE);
  }

  /**
   * New Messages have arrived
   */
  public  void unReadMessages(){
    newMessageIcon.bringToFront();
    newMessageIcon.setVisibility(View.VISIBLE);

  }

  /**
   * Sets previous activity to text chat
   * @param previousActivityWasTextChat This indicates if last activity was text chat.
   */
  public static void setPreviousActivityWasTextChat(Boolean previousActivityWasTextChat) {
    MapsActivity.previousActivityWasTextChat = previousActivityWasTextChat;
  }
}
