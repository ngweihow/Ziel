package com.quartz.zielclient;


import com.google.android.gms.maps.model.LatLng;
import com.quartz.zielclient.map.CoordinateService;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class CoordinateServiceTest {

  Map<String, Map<String, String>> successMessage;
  List<LatLng> successResults;
  Integer failedObject;

  @Before
  public void setup() {
    failedObject = 1;

    successMessage = new HashMap<>();
    successMessage.put(UUID.randomUUID().toString(), generateLatLng(1, 1));
    successMessage.put(UUID.randomUUID().toString(), generateLatLng(5, 5.1));
    successMessage.put(UUID.randomUUID().toString(), generateLatLng(20, 20));
    successMessage.put(UUID.randomUUID().toString(), generateLatLng(-40.5, 30));
    successMessage.put(UUID.randomUUID().toString(), generateLatLng(12, 51));

    successResults = new ArrayList<>();
    successResults.add(new LatLng(1, 1));
    successResults.add(new LatLng(5, 5.1));
    successResults.add(new LatLng(20, 20));
    successResults.add(new LatLng(-40.5, 30));
    successResults.add(new LatLng(12, 51));
    successResults.sort(Comparator.comparingInt(LatLng::hashCode));
  }

  @Test
  public void test_deserialiseSuccess() {
    List<LatLng> result = CoordinateService.deserialiseCarerMarkers(successMessage);
    result.sort(Comparator.comparingInt(LatLng::hashCode));
    assertEquals(successResults, result);
  }

  @Test
  public void test_deserialiseFailure() {
    List<LatLng> res = CoordinateService.deserialiseCarerMarkers(failedObject);
    assertEquals(Collections.emptyList(), res);
  }

  @Test
  public void test_nullFailure() {
    List<LatLng> res = CoordinateService.deserialiseCarerMarkers(null);
    assertEquals(Collections.emptyList(), res);
  }

  @Test
  public void test_nullSingleFailure() {
    LatLng res = CoordinateService.deserialiseMarker(null);
    assertEquals(new LatLng(0, 0), res);
  }

  @Test
  public void test_deserialiseObjectFailure() {
    LatLng res = CoordinateService.deserialiseMarker(failedObject);
    assertEquals(new LatLng(0, 0), res);
  }

  private static Map<String, String> generateLatLng(double x, double y) {
    Map<String, String> map = new HashMap<>();
    map.put("xCoord", String.valueOf(x));
    map.put("yCoord", String.valueOf(y));
    return map;
  }
}
