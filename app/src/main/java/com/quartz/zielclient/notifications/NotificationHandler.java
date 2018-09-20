package com.quartz.zielclient.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import com.quartz.zielclient.R;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.view.View.VISIBLE;

/**
 * This object handles delivering notifications to the Android device.
 *
 * @author Bilal Shehata
 */
public class NotificationHandler {

  private final Context context;
  private NotificationManager notificationManager;

  public NotificationHandler(Context context) {
    this.context = context;
  }

  public void createNotificationChannel() {
    //get the system service for notifications
    notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    //begin creating notification channels
    String id = "helpChannel";
    // The user-visible name of the channel.
    CharSequence name = "Assisted";
    // The user-visible description of the channel.
    String description = context.getString(R.string.notificationChannelDescription);
    NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
    // Configure the notification channel.
    mChannel.setDescription(description);
    mChannel.enableLights(true);
    mChannel.setLockscreenVisibility(VISIBLE);
    // Sets the notification light color for notifications posted to this
    // channel, if the device supports this feature.
    mChannel.setLightColor(Color.RED);
    notificationManager.createNotificationChannel(mChannel);
  }

  public void notifyUserToOpenApp() {
    //creates a notification for the user
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "helpChannel")
        .setSmallIcon(R.drawable.ic_launcher_background) //your app icon
        .setBadgeIconType(R.drawable.ic_launcher_background) //your app icon
        .setChannelId("helpChannel")
        .setContentTitle("Someone Requires your assistance")
        .setAutoCancel(true)
        .setNumber(1)
        .setColor(255)
        .setContentText("Please open App")
        .setWhen(System.currentTimeMillis());
    notificationManager.notify(1, notificationBuilder.build());
  }

}