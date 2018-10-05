package com.quartz.zielclient.models;

import android.os.Bundle;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Objects;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=26)
public class CarerSelectionItemTest {
  CarerSelectionItem carerSelectionItem =
      new CarerSelectionItem("name", "phoneNumber", "carerId", "lastName");

  @Test
  public void testCreationNameValue() throws Exception {

    Assert.assertEquals("name", carerSelectionItem.getFirstName());
  }

  @Test
  public void testCreationPhoneValue() throws Exception {

    Assert.assertEquals("phoneNumber", carerSelectionItem.getPhoneNumber());
  }

  @Test
  public void testToBundle() throws Exception {
    Bundle result;
    result = carerSelectionItem.toBundle();
    Assert.assertEquals("name", result.get("firstName"));
  }

  @Test
  public void testHashCode() throws Exception {
    int result = carerSelectionItem.hashCode();
    Assert.assertEquals(Objects.hash("name", "phoneNumber"), result);
  }
}
