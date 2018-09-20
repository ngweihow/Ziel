package com.quartz.zielclient.map;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class is responsible for fetching the path JSON data from the API endpoint.
 *
 * @author Armaan McLeod
 * @version 1.0- 1
 * 01/09/2018
 */
public class FetchUrl extends AsyncTask<String, Void, String> {

  private final String activity = this.getClass().getSimpleName();

  private final GoogleMap googleMap;

  public FetchUrl(GoogleMap googleMap) {
    super();
    this.googleMap = googleMap;
  }

  /**
   * This downloads the specified URL in a background thread.
   * <p>
   * Documentation : https://developer.android.com/reference/android/os/
   * AsyncTask.html#doInBackground(Params...)
   *
   * @param url This is the URL to download.
   * @return String This is the downloaded data in String format.
   */
  @Override
  protected String doInBackground(@NonNull String... url) {
    String data = "";

    // Attempt to download the URL
    try {
      data = downloadUrl(url[0]);
      Log.d(activity, data);
    } catch (Exception e) {
      Log.d(activity, e.toString());
    }

    return data;
  }

  /**
   * This is responsible for running the parser task on the
   * download URL from doInBackground(Params...)
   * <p>
   * Documentation : https://developer.android.com/reference/android/os/
   * AsyncTask.html#onPostExecute(Result)
   *
   * @param result This is the result to execute.
   */
  @Override
  protected void onPostExecute(@NonNull String result) {
    super.onPostExecute(result);

    ParserTask parserTask = new ParserTask(googleMap);
    parserTask.execute(result);
  }

  /**
   * This is responsible for downloading the JSON data from an API endpoint with HTTP requests.
   *
   * @param strUrl The url to download data from.
   * @return String This is the JSON data in String format.
   * @throws IOException This is the IO exception that triggers when reading the file fails.
   */
  private String downloadUrl(@NonNull String strUrl) throws IOException {
    String data = "";
    InputStream iStream = null;
    HttpURLConnection urlConnection = null;

    try {

      // Open connection with endpoint
      URL url = new URL(strUrl);
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.connect();

      // Setup input stream ready to buffer data
      iStream = urlConnection.getInputStream();
      try (BufferedReader br = new BufferedReader(new InputStreamReader(iStream))) {
        StringBuilder sb = new StringBuilder();

        // Read data line by line and append to buffer
        String line;
        while ((line = br.readLine()) != null) {
          sb.append(line);
        }

        data = sb.toString();
        Log.d(activity, data);
      }

    } catch (MalformedURLException e) {
      Log.d(activity, e.toString());

    } finally {
      if (iStream != null) {
        iStream.close();
      }

      if (urlConnection != null) {
        urlConnection.disconnect();
      }
    }

    return data;
  }
}
