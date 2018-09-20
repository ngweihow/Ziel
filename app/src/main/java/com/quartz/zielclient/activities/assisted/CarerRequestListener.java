package com.quartz.zielclient.activities.assisted;

/**
 * interface for objects that want to add users by phone number
 *
 * @author Bilal Shehata
 */
public interface CarerRequestListener {

  void userNotFound();

  void userFound();
}
