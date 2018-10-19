package com.quartz.zielclient.activities.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import com.quartz.zielclient.R;

/**
 * This class is responsible for creating alert dialogues for the participant to see.
 */
public class Dialog {

  /**
   * Creates connected dialogue for the participant.
   * @param participantEditText The text view visible to the participant.
   * @param callParticipantsClickListener The listener attached waiting for events.
   * @param cancelClickListener The listener waiting to cancel the activity.
   * @param context The current context of the app.
   * @return AlertDialog The alert dialogue to be created.
   */
  public static AlertDialog createConnectDialog(TextView participantEditText,
                                                DialogInterface.OnClickListener callParticipantsClickListener,
                                                DialogInterface.OnClickListener cancelClickListener,
                                                Context context) {
    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

    // Setup dialogue attributes
    alertDialogBuilder.setIcon(R.drawable.ic_video_call_white_24dp);
    alertDialogBuilder.setTitle("Start a video share?");
    alertDialogBuilder.setPositiveButton("Connect", callParticipantsClickListener);
    alertDialogBuilder.setNegativeButton("Cancel", cancelClickListener);
    alertDialogBuilder.setCancelable(false);

    setRoomNameFieldInDialog(participantEditText, alertDialogBuilder, context);

    return alertDialogBuilder.create();
  }

  /**
   * Sets text field in room dialog.
   * @param roomNameEditText The text view holding the room name.
   * @param alertDialogBuilder The alert dialog
   * @param context The current context of the app
   */
  @SuppressLint("RestrictedApi")
  private static void setRoomNameFieldInDialog(TextView roomNameEditText,
                                               AlertDialog.Builder alertDialogBuilder,
                                               Context context) {
    roomNameEditText.setHint("room name");
    int horizontalPadding = context.getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
    int verticalPadding = context.getResources().getDimensionPixelOffset(R.dimen.activity_vertical_margin);
    alertDialogBuilder.setView(roomNameEditText,
            horizontalPadding,
            verticalPadding,
            horizontalPadding,
            0);
  }

}
