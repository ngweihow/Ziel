package com.quartz.zielclient.activities.assisted;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.quartz.zielclient.models.CarerSelectionItem;

public class CarerSelectionFactory {
  private CarerSelectionFactory() {
    // Intentionally empty
  }

  public static CarerSelectionItem getChannelRequest(
      String name, String phoneNumber, String carerId) {
    return new CarerSelectionItem(name, phoneNumber, carerId);
  }

  /**
   * Construct a new ChannelRequest from a DataSnapshot.
   *
   * @param dataSnapshot A DataSnapshot representing a ChannelRequest.
   * @return A ChannelRequest model.
   */
  public static CarerSelectionItem getChannelRequest(@NonNull DataSnapshot dataSnapshot) {
    return dataSnapshot.getValue(CarerSelectionItem.class);
  }

  public static CarerSelectionItem getChannelRequest(CarerSelectionItem carerSelectionItem) {
    return new CarerSelectionItem(carerSelectionItem);
  }
}
