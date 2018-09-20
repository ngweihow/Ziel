package com.quartz.zielclient.map;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for parsing JSON and extracting the directions of the paths.
 * <p>
 * Courtesy : https://github.com/gripsack/android/blob/master/app/src/main/java/com/github/
 * gripsack/android/data/model/DirectionsJSONParser.java
 *
 * @author Armaan McLeod
 * @version 1.0- 1
 * 01/09/2018
 */
public class DirectionsJSONParser {

  /**
   * This receives a JSONObject and returns a list of routes.
   *
   * @param jObject The JSON object to parse.
   * @return This is the list of routes.
   */
  public List<List<Map<String, String>>> parse(JSONObject jObject) {
    List<List<Map<String, String>>> routes = new ArrayList<>();

    try {

      // Extract routes from JSON
      JSONArray jRoutes = jObject.getJSONArray("routes");

      // Traverse all routes and extract legs from route
      for (int i = 0; i < jRoutes.length(); i++) {
        JSONArray jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
        List<Map<String, String>> path = new ArrayList<>();

        // Traverse all legs and extract steps from i-th route
        for (int j = 0; j < jLegs.length(); j++) {
          JSONArray jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

          // Traverse all steps and extract polylines from j-th leg
          for (int k = 0; k < jSteps.length(); k++) {
            String polyline = (String) ((JSONObject) ((JSONObject) jSteps
                .get(k))
                .get("polyline"))
                .get("points");

            // Decode poly points into path
            List<LatLng> polyPoints = decodePoly(polyline);

            // Traverse points from k-th step and add latitudes and longitudes to path
            for (int l = 0; l < polyPoints.size(); l++) {
              Map<String, String> hm = new HashMap<>();
              hm.put("lat", Double.toString((polyPoints.get(l)).latitude));
              hm.put("lng", Double.toString((polyPoints.get(l)).longitude));
              path.add(hm);
            }
          }

          routes.add(path);
        }
      }

    } catch (JSONException e) {
      e.printStackTrace();
    }

    return routes;
  }

  /**
   * This is responsible for decoding poly points
   * <p>
   * Courtesy : http://jeffreysambells.com/2010/05/27/
   * decoding-polylines-from-google-maps-direction-api-with-java
   *
   * @param encoded The encoded poly path to decode.
   * @return List<LatLng> A list of poly points.
   */
  private List<LatLng> decodePoly(String encoded) {
    List<LatLng> poly = new ArrayList<>();

    int index = 0;
    int len = encoded.length();
    int lat = 0;
    int lng = 0;

    while (index < len) {
      int b;
      int shift = 0;
      int result = 0;

      do {
        b = encoded.charAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);

      int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
      lat += dlat;

      shift = 0;
      result = 0;

      do {
        b = encoded.charAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);

      int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
      lng += dlng;

      LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);

      poly.add(p);
    }

    return poly;
  }
}