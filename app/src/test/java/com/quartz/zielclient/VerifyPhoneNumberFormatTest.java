package com.quartz.zielclient;

import com.quartz.zielclient.user.SystemService;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VerifyPhoneNumberFormatTest {

  @Test
  public void test_plus() {
    assertFalse(SystemService.verifyNumberFormat("1234567890"));
    assertTrue(SystemService.verifyNumberFormat("+1234567890"));
  }

  @Test
  public void test_numLength() {
    assertFalse(SystemService.verifyNumberFormat("+1"));
    assertTrue(SystemService.verifyNumberFormat("+1234567890"));
  }
}
