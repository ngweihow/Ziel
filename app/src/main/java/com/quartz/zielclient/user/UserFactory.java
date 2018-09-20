package com.quartz.zielclient.user;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.Map;
import java.util.Optional;

import static com.quartz.zielclient.user.User.FIRST_NAME_KEY;
import static com.quartz.zielclient.user.User.IS_ASSISTED_KEY;
import static com.quartz.zielclient.user.User.LAST_NAME_KEY;
import static com.quartz.zielclient.user.User.PHONE_NUMBER_KEY;

/**
 * Factory for constructing user models.
 */
public class UserFactory {

  private UserFactory() {
    // Intentionally empty
  }

  public static User getUser(String firstName, String lastName, String phoneNumber, boolean assisted) {
    return new User(firstName, lastName, phoneNumber, assisted);
  }

  /**
   * Constructs a user from raw data extracted from Firebase.
   * @param rawUserData Raw user data as a map object.
   * @return A user model.
   */
  public static User getUser(Map<String, Object> rawUserData) {
    return new User(
        (String) rawUserData.get(FIRST_NAME_KEY),
        (String) rawUserData.get(LAST_NAME_KEY),
        (String) rawUserData.get(PHONE_NUMBER_KEY),
        (Boolean) rawUserData.get(IS_ASSISTED_KEY)
    );
  }

  /**
   * Constructs a user from a Bundle object.
   * @param bundle The bundle representation of a user.
   * @return A user model.
   */
  public static User getUser(Bundle bundle) {
    return new User(
        bundle.getString(FIRST_NAME_KEY),
        bundle.getString(LAST_NAME_KEY),
        bundle.getString(PHONE_NUMBER_KEY),
        bundle.getBoolean(IS_ASSISTED_KEY)
    );
  }

  /**
   * Constructs a user from a data snapshot of the Firebase database, intended to allow for
   * simplification of various Firebase callback methods.
   * @param dataSnapshot The data snapshot from Firebase.
   * @return A user model.
   */
  public static User getUser(DataSnapshot dataSnapshot) {
    return dataSnapshot.getValue(User.class);
  }
}