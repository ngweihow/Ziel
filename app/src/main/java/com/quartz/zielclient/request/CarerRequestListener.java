package com.quartz.zielclient.request;

/**
 * interface for objects that want to add users by phone number
 *
 * @author Bilal Shehata
 */
public interface CarerRequestListener {

  void userNotFound();

  void userFound();
}
