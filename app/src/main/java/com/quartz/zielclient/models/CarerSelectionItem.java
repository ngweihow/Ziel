package com.quartz.zielclient.models;

import android.os.Bundle;

import java.util.Objects;

public class CarerSelectionItem implements Model {

  private static final String NAME_KEY = "firstName";
  private static final String LAST_NAME_KEY = "lastName";
  private static final String NUMBER_KEY = "phoneNumber";
  private static final String CARER_ID = "carer_id";

  private String firstName;
  private String phoneNumber;
  private String carerId;
  private String lastName;

  public CarerSelectionItem() {}

  public CarerSelectionItem(String name, String phoneNumber, String carerId, String lastName) {
    this.firstName = name;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
    this.carerId = carerId;
  }

  public CarerSelectionItem(CarerSelectionItem carerSelectionItem) {
    this.firstName = carerSelectionItem.firstName;
    this.firstName = carerSelectionItem.lastName;
    this.phoneNumber = carerSelectionItem.phoneNumber;
    this.carerId = carerSelectionItem.carerId;
  }

  @Override
  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putString(LAST_NAME_KEY,lastName);
    bundle.putString(NAME_KEY, firstName);
    bundle.putString(NUMBER_KEY, phoneNumber);
    bundle.putString(CARER_ID, phoneNumber);
    return bundle;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String name) {
    this.firstName = name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstName, phoneNumber);
  }

  public String getCarerId() {
    return carerId;
  }

  public void setCarerId(String carerId) {
    this.carerId = carerId;
  }
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

}
