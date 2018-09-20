package com.quartz.zielclient.exceptions;

/**
 * This exception is intended to represent a failure in some sort of authentication process;
 * for example, not being able to find a logged-in user.
 */
public class AuthorisationException extends Exception {
  public AuthorisationException(String message) {
    super(message);
  }
}