package com.quartz.zielclient.user;

/**
 * This exception is intended to represent a failure in some sort of authentication process;
 * for example, not being able to find a logged-in user.
 */
public class AuthorisationException extends Exception {
  AuthorisationException(String message) {
    super(message);
  }
}