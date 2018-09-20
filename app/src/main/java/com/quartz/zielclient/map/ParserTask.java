package com.quartz.zielclient.map;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for extracting information from JSON responses into paths.
 *
 * @author Armaan McLeod
 * @version 1.0- 1
 * 01/09/2018
 */
public class ParserTask extends AsyncTask<String, Integer, List<List<Map<String, String>>>> {

  private final String activity = this.getClass().getSimpleName();

  private static final int POLYLINE_WIDTH = 16;

  // Light blue color
  private static final String POLYLINE_COLOR = "#2196F3";

  private final GoogleMap googleMap;

  ParserTask(GoogleMap googleMap) {
    this.googleMap = googleMap;
  }

  /**
   * This does the parsing in a background thread.
   * <p>
   * Documentation : https://developer.android.com/reference/android/os/
   * AsyncTask.html#doInBackground(Params...)
   *
   * @param jsonData This is the JSON data to parse.
   * @return The routes extracted from JSON data.
   */
  @Override
  protected List<List<Map<String, String>>> doInBackground(@NonNull String... jsonData) {
    List<List<Map<String, String>>> routes = Collections.emptyList();

    // If possible, extract routes from JSON object
    try {
      JSONObject jObject = new JSONObject(jsonData[0]);
      Log.d(activity, jsonData[0]);

      DirectionsJSONParser parser = new DirectionsJSONParser();
      Log.d(activity, parser.toString());

      routes = parser.parse(jObject);
      Log.d(activity, "Executing routes");
      Log.d(activity, routes.toString());

    } catch (Exception e) {
      Log.d(activity, e.toString());
    }

    return routes;
  }

  /**
   * Decodes line options into a visible path on the map.
   * <p>
   * Documentation : https://developer.android.com/reference/android/os/
   * AsyncTask.html#onPostExecute(Result)
   *
   * @param result This is result from doInBackground(Params...).
   */
  @Override
  protected void onPostExecute(@NonNull List<List<Map<String, String>>> result) {
    PolylineOptions lineOptions = null;

    for (List<Map<String, String>> path : result) {
      List<LatLng> points = new ArrayList<>();
      lineOptions = new PolylineOptions();

      for (Map<String, String> point : path) {
        double latitude = Double.parseDouble(point.get("lat"));
        double longitude = Double.parseDouble(point.get("lng"));

        LatLng position = new LatLng(latitude, longitude);
        points.add(position);
      }

      // Add points and colour line
      lineOptions.addAll(points);
      lineOptions.width(POLYLINE_WIDTH);
      lineOptions.color(Color.parseColor(POLYLINE_COLOR)).geodesic(true).zIndex(8);

      Log.d(activity, "lineOptions decoded");
    }

    // Ensure polylines available to draw on screen
    if (lineOptions != null) {
      googleMap.addPolyline(lineOptions);
    } else {
      Log.d(activity, "No polylines drawn");
    }
  }
}
