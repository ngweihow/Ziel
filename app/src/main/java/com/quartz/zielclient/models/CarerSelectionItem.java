package com.quartz.zielclient.models;

import android.os.Bundle;

import java.util.Objects;

public class CarerSelectionItem implements Model {

  private static final String NAME_KEY = "name";
  private static final String NUMBER_KEY = "phoneNumber";
  private static final String CARER_ID = "carer_id";

  private String name;
  private String phoneNumber;
  private String carerId;

  public CarerSelectionItem() {}

  public CarerSelectionItem(String name, String phoneNumber, String carerId) {
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.carerId = carerId;
  }

  public CarerSelectionItem(CarerSelectionItem carerSelectionItem) {
    this.name = carerSelectionItem.name;
    this.phoneNumber = carerSelectionItem.phoneNumber;
    this.carerId = carerSelectionItem.carerId;
  }

  @Override
  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putString(NAME_KEY, name);
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, phoneNumber);
  }

  public String getCarerId() {
    return carerId;
  }

  public void setCarerId(String carerId) {
    this.carerId = carerId;
  }
}
