package com.quartz.zielclient.user;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;

import java.util.Optional;

import static android.Manifest.permission.READ_PHONE_STATE;

public final class SystemService {

  private SystemService() {
    // Intentionally empty
  }

  /**
   * Attempts to access the phone number, and then set it in the text box. If it's unable (due to
   * lacking permissions or some other problem) it will simply return an empty Optional.
   * <p>
   * The phone number will be prepended with a '+'.
   */
  public static Optional<String> retrieveSystemPhoneNumber(Context context) {
    Optional<String> phoneNumber = Optional.empty();
    if (context.checkSelfPermission(READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
      TelephonyManager mgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
      if (mgr != null) {
        phoneNumber = Optional.of(mgr.getLine1Number());
      }
    }
    return phoneNumber;
  }

  /**
   * Verifies that the format of the phone number obeys Firebase's formatting rules.
   *
   * @param phoneNumber The phone number to verify.
   * @return Whether the phone number is formatted as Firebase expects.
   */
  public static boolean verifyNumberFormat(@NonNull final String phoneNumber) {
    return phoneNumber.matches("^\\+\\d{10,14}$");
  }
}