package com.quartz.zielclient.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;

import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.carer.CarerHomepageActivity;
import com.quartz.zielclient.activities.carer.CarerMapsActivity;
import com.quartz.zielclient.channel.ChannelController;
import com.quartz.zielclient.channel.ChannelData;
import com.quartz.zielclient.models.ChannelRequest;
import com.quartz.zielclient.voip.SoundPoolManager;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.view.View.VISIBLE;

/**
 * This object handles delivering notifications to the Android device.
 *
 * @author Bilal Shehata
 */
public class NotificationHandler {

  private final CarerHomepageActivity carerHomepageActivity;
  private NotificationManager notificationManager;
  private Vibrator vibrator;
  private SoundPoolManager soundPoolManager;
  private AlertDialog alertUser;
  private ChannelRequest channelRequest;

  public NotificationHandler(CarerHomepageActivity carerHomepageActivity) {
    this.carerHomepageActivity = carerHomepageActivity;
    vibrator = (Vibrator) carerHomepageActivity.getSystemService(Context.VIBRATOR_SERVICE);
    soundPoolManager = SoundPoolManager.getInstance(carerHomepageActivity);
  }

  public static PendingIntent newLauncherIntent(final Context context) {
    Intent notificationIntent = new Intent(context, CarerHomepageActivity.class);
    notificationIntent.setAction(Intent.ACTION_MAIN);
    notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    return PendingIntent.getActivity(context, 0, notificationIntent, 0);
  }

  public void createNotificationChannel() {
    // get the system service for notifications
    notificationManager =
        (NotificationManager) carerHomepageActivity.getSystemService(NOTIFICATION_SERVICE);
    // begin creating notification channels
    String id = "helpChannel";
    // The user-visible name of the channel.
    CharSequence name = "Assisted";
    // The user-visible description of the channel.
    String description = carerHomepageActivity.getString(R.string.notificationChannelDescription);
    NotificationChannel mChannel =
        new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
    // Configure the notification channel.
    mChannel.setDescription(description);
    mChannel.enableLights(true);
    mChannel.setLockscreenVisibility(VISIBLE);
    // Sets the notification light color for notifications posted to this
    // channel, if the device supports this feature.

    notificationManager.createNotificationChannel(mChannel);
  }

  public void notifyUserToOpenApp(ChannelRequest channelRequest) {
    // creates a notification for the user
    this.channelRequest = channelRequest;
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(carerHomepageActivity);
    alertDialogBuilder.setIcon(R.drawable.ic_call_black_24dp);
    alertDialogBuilder.setTitle("Help Wanted!");
    alertDialogBuilder.setPositiveButton("Accept", answerHelpClickListener());
    alertDialogBuilder.setNegativeButton("Reject", declineHelpToListener());
    alertDialogBuilder.setMessage(channelRequest.getName() + "Needs your help");

    alertUser = alertDialogBuilder.create();
    alertUser.show();

    vibrator.vibrate(VibrationEffect.createWaveform(new long[] {1000, 1000, 1000, 1000, 1000}, 0));
    if (soundPoolManager != null) {
      soundPoolManager.playRinging();
    }
    NotificationCompat.Builder notificationBuilder =
        new NotificationCompat.Builder(carerHomepageActivity, "helpChannel")
            .setSmallIcon(R.mipmap.ziel_logo) // your app icon
            .setBadgeIconType(R.mipmap.ziel_logo) // your app icon
            .setChannelId("helpChannel")
            .setContentTitle(channelRequest.getName() + " Requires your assistance")
            .setAutoCancel(true)
            .setNumber(1)
            .setContentIntent(newLauncherIntent(carerHomepageActivity))
            .setContentText("Please open App")
            .setWhen(System.currentTimeMillis())
            .setVibrate(new long[] {1000, 1000, 1000, 1000, 1000})
            .setLights(Color.RED, 3000, 3000)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
    notificationManager.notify(1, notificationBuilder.build());
  }

  public void stopVibratingDevice() {
    vibrator.cancel();
    soundPoolManager.stopRinging();
    alertUser.dismiss();
  }

  private DialogInterface.OnClickListener answerHelpClickListener() {
    return (dialog, which) -> {
      Intent intent = new Intent(carerHomepageActivity, CarerMapsActivity.class);
      intent.putExtra(
          carerHomepageActivity.getResources().getString(R.string.channel_key), channelRequest.getChannelId());
      carerHomepageActivity.startActivity(intent);
      stopVibratingDevice();
    };
  }

  private DialogInterface.OnClickListener declineHelpToListener() {
    return (dialog, which) -> {
      ChannelData channel =
          ChannelController.retrieveChannel(channelRequest.getChannelId(), () -> {});
      if (channel != null) {
        channel.endChannel();
      }
      stopVibratingDevice();
    };
  }
}
