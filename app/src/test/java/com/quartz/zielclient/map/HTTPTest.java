package com.quartz.zielclient.map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.*;

public class HTTPTest {

  private String[] testUrls;

  @Before
  public void setUp() {

    // Test API urls from https://jsonplaceholder.typicode.com/
    // Fake Online REST API
    testUrls = new String[] {
        "https://jsonplaceholder.typicode.com/posts",
        "https://jsonplaceholder.typicode.com/comments",
        "https://jsonplaceholder.typicode.com/albums",
        "https://jsonplaceholder.typicode.com/photos",
        "https://jsonplaceholder.typicode.com/todos",
        "https://jsonplaceholder.typicode.com/users"
    };
  }

  @After
  public void tearDown() {
    testUrls = null;
  }

  @Test
  public void testUrls() {
    System.out.println("Running: testUrls");

    for (String strUrl : testUrls) {

      try {
        URL url = new URL(strUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.connect();
        assertEquals(HttpURLConnection.HTTP_OK, urlConnection.getResponseCode());
        System.out.println(strUrl + " PASSED");

      } catch (IOException e) {
        System.out.println(strUrl + " FAILED: Error creating HTTP connection");
        e.printStackTrace();
      }
    }
  }

  @Test
  public void downloadUrls() {
    System.out.println("Running: downloadUrls");

    for (String url : testUrls) {

      try {
        String result = HTTP.downloadUrl(url);
        assertFalse(result.isEmpty());
        System.out.println(url + " PASSED");
      } catch (IOException e) {
        System.out.println(url + " FAILED: Error downloading URL");
        e.printStackTrace();
      }
    }
  }
}