package com.quartz.zielclient.user;

import android.os.Bundle;

import com.quartz.zielclient.models.Model;

/**
 * Model representing a user.
 */
public class User implements Model {

  static final String FIRST_NAME_KEY = "firstName";
  static final String LAST_NAME_KEY = "lastName";
  static final String PHONE_NUMBER_KEY = "phoneNumber";
  static final String IS_ASSISTED_KEY = "assisted";

  private String firstName;
  private String lastName;
  private String phoneNumber;
  private boolean assisted;

  public User() {
    // Intentionally empty
  }

  User(String firstName, String lastName, String phoneNumber, boolean assisted) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
    this.assisted = assisted;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public boolean isAssisted() {
    return assisted;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public void setAssisted(boolean assisted) {
    this.assisted = assisted;
  }

  public String fullName() {
    return firstName + " " + lastName;
  }

  @Override
  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putString(FIRST_NAME_KEY, firstName);
    bundle.putString(LAST_NAME_KEY, lastName);
    bundle.putString(PHONE_NUMBER_KEY, phoneNumber);
    bundle.putBoolean(IS_ASSISTED_KEY, assisted);
    return bundle;
  }

  @Override
  public String toString() {
    return "User{" +
        "firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", phoneNumber='" + phoneNumber + '\'' +
        ", assisted=" + assisted +
        '}';
  }
}