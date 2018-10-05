package com.quartz.zielclient.request;

import android.os.Looper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddCarerRequestHandlerTest {

  @Before
  public void setUp() {}

  @Test
  public void testAddCarer() throws Exception {
    AddCarerRequestHandler addCarerRequestHandler = Mockito.mock(AddCarerRequestHandler.class);
    Looper.prepare();
    //todo
  }

  @Test
  public void testOnDataChange() throws Exception {
    AddCarerRequestHandler addCarerRequestHandler = Mockito.mock(AddCarerRequestHandler.class);
    Looper.prepare();
    addCarerRequestHandler.onDataChange(null);
    //todo
  }

  @Test
  public void testOnCancelled() throws Exception {
    AddCarerRequestHandler addCarerRequestHandler = Mockito.mock(AddCarerRequestHandler.class);
    Looper.prepare();
    addCarerRequestHandler.onCancelled(null);
    //todo
  }
}
