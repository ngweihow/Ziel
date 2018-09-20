package com.quartz.zielclient;

import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

import com.quartz.zielclient.user.SystemService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static android.Manifest.permission.READ_PHONE_STATE;

/**
 * Tests the retrieval of the device's phone number from the
 */
@RunWith(MockitoJUnitRunner.class)
public class RetrievePhoneNumberTest {

  private static final String TEST_NUMBER = "+123456789";

  private Context context;

  /**
   * Set up the application context to return the specified test phone number.
   */
  @Before
  public void setup() {
    context = Mockito.mock(Context.class);
    TelephonyManager telephonyManager = Mockito.mock(TelephonyManager.class);
    Mockito.when(telephonyManager.getLine1Number()).thenReturn(TEST_NUMBER);
    Mockito.when(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager);
  }

  /**
   * Test that, when the application has the appropriate permissions, it can successfully retrieve
   * and return the phone number.
   */
  @Test
  public void phoneNumber_isPresent() {
    // Grant the phone number permission.
    Mockito.when(context.checkSelfPermission(READ_PHONE_STATE))
        .thenReturn(PackageManager.PERMISSION_GRANTED);

    Optional<String> phoneNumber = SystemService.retrieveSystemPhoneNumber(context);
    Assert.assertTrue(phoneNumber.isPresent());
    Assert.assertEquals(TEST_NUMBER, phoneNumber.get());
  }

  /**
   * Test that, when the application has the appropriate permissions, it cannot retrieve the phone
   * number.
   */
  @Test
  public void phoneNumber_isNotPresent() {
    // Deny the phone number permission.
    Mockito.when(context.checkSelfPermission(READ_PHONE_STATE))
        .thenReturn(PackageManager.PERMISSION_DENIED);

    Optional<String> phoneNumber = SystemService.retrieveSystemPhoneNumber(context);
    Assert.assertFalse(phoneNumber.isPresent());
  }
}