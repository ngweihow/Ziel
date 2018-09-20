package com.quartz.zielclient.activities.signup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quartz.zielclient.R;
import com.quartz.zielclient.user.SystemService;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.view.View.OnClickListener;

/**
 * @author alexvosnakis
 * <p>
 * Activity for inputting and confirming (by the user) of a phone number.
 */
public class VerifyPhoneNumberActivity extends AppCompatActivity implements OnClickListener {

  private TextView phoneNumberEntry;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_verify_phone_number);

    requestPermissions(new String[]{READ_PHONE_STATE}, 1);
    populatePhoneNumber();

    Button confirmButton = findViewById(R.id.confirmNumber);
    confirmButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    final int clickedId = view.getId();
    if (clickedId != R.id.confirmNumber) {
      // We only want to handle this for confirmations.
      return;
    }

    Dialog yesNoDialog = buildConfirmationPrompt();
    yesNoDialog.show();
  }

  /**
   * Populates the phone number text view if able to, otherwise sets it to an empty string.
   */
  private void populatePhoneNumber() {
    phoneNumberEntry = findViewById(R.id.phoneEntry);
    phoneNumberEntry.setText(SystemService
        .retrieveSystemPhoneNumber(this)
        .orElse("")
    );
  }

  /**
   * @return A dialog that confirms the user's phone number.
   */
  private Dialog buildConfirmationPrompt() {
    return new AlertDialog.Builder(this)
        .setTitle("Confirm your number")
        .setMessage(String.format("Is %s your phone number?", phoneNumberEntry.getText()))
        .setPositiveButton(android.R.string.yes, yesCallback())
        .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.dismiss())
        .create();
  }

  /**
   * Defines the set of actions for the dialog to take once the user has confirmed their phone
   * number; it passes the phone number onto the next activity.
   *
   * @return A callback which executes these actions.
   */
  private DialogInterface.OnClickListener yesCallback() {
    return (dialog, which) -> {
      Intent intent = new Intent(VerifyPhoneNumberActivity.this, ConfirmationCodeActivity.class);
      intent.putExtra("phoneNumber", phoneNumberEntry.getText().toString());
      dialog.dismiss();
      startActivity(intent);
    };
  }
}