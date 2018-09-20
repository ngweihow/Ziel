package com.quartz.zielclient.user;

import android.content.Context;
import android.content.pm.PackageManager;
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
   *
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
}