package com.quartz.zielclient.exceptions;

/**
 * This exception is intended to represent a user not being found.
 */
public class UserNotFoundException extends Exception {
  public UserNotFoundException(String message) {
    super(message);
  }
}
