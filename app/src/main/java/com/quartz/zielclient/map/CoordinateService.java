package com.quartz.zielclient.map;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for operations with coordinates, primarily the deserialisation of them.
 *
 * @author alexvosnakis
 */
public class CoordinateService {

  private static final String TAG = CoordinateService.class.getSimpleName();

  private CoordinateService() {
    // Intentionally empty
  }

  /**
   * Deserialises a collection of carer markers.
   * <p>
   * Expects them in a form:
   * <p>
   * {
   * string: {
   * xCoord: string,
   * yCoord: string,
   * },
   * string: {
   * ...
   * }...
   * }
   *
   * @param rawCarerMarkers A representation of carer markers as a nullable object.
   * @return A list of parsed locations. Should there be an error, then an empty list will be
   * return instead.
   */
  @SuppressWarnings("unchecked")
  public static List<LatLng> deserialiseCarerMarkers(@Nullable Object rawCarerMarkers) {
    if (rawCarerMarkers == null) {
      return Collections.emptyList();
    }

    try {
      Map<String, Map<String, String>> carerMarkers = (Map<String, Map<String, String>>) rawCarerMarkers;
      return carerMarkers.values()
          .stream()
          .map(CoordinateService::parseMarker)
          .collect(Collectors.toList());
    } catch (ClassCastException e) {
      Log.e(TAG, "Error parsing carer markers", e);
      return Collections.emptyList();
    }
  }

  /**
   * Deserialises a single marker, with input that may be faulty.
   *
   * @param rawMarker A raw marker object.
   * @return The deserialised LatLng object, otherwise a LatLng representing Null Island should
   * there be an error.
   */
  @SuppressWarnings("unchecked")
  public static LatLng deserialiseMarker(@Nullable Object rawMarker) {
    if (rawMarker == null) {
      return new LatLng(0, 0);
    }

    try {
      Map<String, String> markerData = (Map<String, String>) rawMarker;
      return parseMarker(markerData);
    } catch (ClassCastException e) {
      Log.e(TAG, "Error parsing marker", e);
      return new LatLng(0, 0);
    }
  }

  /**
   * Parses a single marker, with known valid input.
   *
   * @param markerData A map with marker data.
   * @return A LatLng representation of the marker.
   */
  private static LatLng parseMarker(@NonNull Map<String, String> markerData) {
    double x = Double.parseDouble(markerData.get("xCoord"));
    double y = Double.parseDouble(markerData.get("yCoord"));
    return new LatLng(x, y);
  }
}
