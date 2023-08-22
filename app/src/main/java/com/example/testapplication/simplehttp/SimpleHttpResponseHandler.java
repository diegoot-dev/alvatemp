package com.example.testapplication.simplehttp;

/**
 * HTTP response handler.
 * @author Diego Olguin
 * @version 1.0
 */
public interface SimpleHttpResponseHandler {
  public void onResponse(int responseCode, String responseBody);
}
