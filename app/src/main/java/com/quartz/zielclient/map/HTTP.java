package com.quartz.zielclient.map;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is responsible for handling HTTP requests.
 *
 * @author Armaan McLeod
 * @version 1.0
 * 18/10/2018
 */
public class HTTP {

  public HTTP() {
    // Intentionally empty
  }

  private static final String ACTIVITY = HTTP.class.getSimpleName();

  /**
   * This is responsible for downloading the JSON data from an API endpoint with HTTP requests.
   *
   * @param strUrl The url to download data from.
   * @return String This is the JSON data in String format.
   * @throws IOException This is the IO exception that triggers when reading the file fails.
   */
  public static String downloadUrl(@NonNull String strUrl) throws IOException {
    String data = "";
    HttpURLConnection urlConnection;

    // Open connection with endpoint
    URL url = new URL(strUrl);
    urlConnection = (HttpURLConnection) url.openConnection();
    urlConnection.connect();

    try (InputStream iStream = urlConnection.getInputStream()) {

      // Setup input stream ready to buffer data
      try (BufferedReader br = new BufferedReader(new InputStreamReader(iStream))) {
        StringBuilder sb = new StringBuilder();

        // Read data line by line and append to buffer
        String line;
        while ((line = br.readLine()) != null) {
          sb.append(line);
        }

        data = sb.toString();
        Log.d(ACTIVITY, data);
      }

    } catch (MalformedURLException e) {
      Log.d(ACTIVITY, e.toString());

    } finally {
      urlConnection.disconnect();
    }

    return data;
  }
}
