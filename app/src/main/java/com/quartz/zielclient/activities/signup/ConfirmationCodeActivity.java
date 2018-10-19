package com.quartz.zielclient.activities.signup;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.quartz.zielclient.R;
import com.quartz.zielclient.user.AuthorisationController;

import static com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken;
import static com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks;

/**
 * This class is responsible for confirming sign up for the user.
 */
public class ConfirmationCodeActivity extends AppCompatActivity implements View.OnClickListener, OnCompleteListener<AuthResult> {

  private static final String TAG = ConfirmationCodeActivity.class.getSimpleName();

  private AuthorisationController authController;

  private String mVerificationId;
  private ForceResendingToken mResendingToken;

  private TextView verificationField;

  private final OnVerificationStateChangedCallbacks callbacks = new OnVerificationStateChangedCallbacks() {
    private final ConfirmationCodeActivity outer = ConfirmationCodeActivity.this;

    @Override
    public void onVerificationCompleted(PhoneAuthCredential credential) {
      Log.d(TAG, "onVerificationCompleted:" + credential);

      authController.signInWithPhoneAuthCredential(credential, outer);
      Toast toast = Toast.makeText(outer, R.string.sending_confirmation_code, Toast.LENGTH_SHORT);
      toast.show();
    }

    @Override
    public void onVerificationFailed(FirebaseException e) {
      Toast feedback = Toast.makeText(outer, "An error occurred.", Toast.LENGTH_SHORT);
      Log.e("Code exception", "An error occurred:", e);
      feedback.show();
    }

    @Override
    public void onCodeSent(String verificationId, ForceResendingToken token) {
      // The SMS verification code has been sent to the provided phone number, we
      // now need to ask the user to enter the code and then construct a credential
      // by combining the code with a verification ID.
      Log.d(TAG, "onCodeSent:" + verificationId);

      // Save verification ID and resending token so we can use them later
      mVerificationId = verificationId;
      mResendingToken = token;
    }
  };

  /**
   * Called when the activity is starting.
   * <p>
   * Documentation: https://developer.android.com/reference/android/app/Activity.html#
   * onCreate(android.os.Bundle)
   *
   * @param savedInstanceState If the activity is being re-initialized after previously being shut
   *                           down then this Bundle contains the data it most recently
   *                           supplied in onSaveInstanceState(Bundle)
   */
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_confirmation_code);
    verificationField = findViewById(R.id.confirmationCodeEntry);

    Button confirmationButton = findViewById(R.id.confirmCodeButton);
    Button resendCodeButton = findViewById(R.id.resendButton);

    confirmationButton.setOnClickListener(this);
    resendCodeButton.setOnClickListener(this);

    String phoneNumber = getIntent().getStringExtra("phoneNumber");
    Log.d("Phone number", phoneNumber);

    authController = new AuthorisationController(phoneNumber, this);
    authController.sendConfirmationCode(callbacks);
  }

  /**
   * Called when a view has been clicked.
   * <p>
   * Documentation: https://developer.android.com/reference/android/view/V
   * iew.OnClickListener.html#onClick(android.view.View)
   *
   * @param view The view that was clicked.
   */
  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.confirmCodeButton:
        verifyPhoneNumberWithCode();
        break;
      case R.id.resendButton:
        authController.resendConfirmationCode(callbacks, mResendingToken);
        break;
      default:
        break;
    }
  }

  @Override
  public void onComplete(@NonNull Task<AuthResult> task) {
    if (task.isSuccessful()) {
      // Sign in success, update UI with the signed-in user's information
      Log.d(TAG, "signInWithCredential:success");
      Intent intent = new Intent(this, AccountCreationActivity.class);
      startActivity(intent);
      finish();
    } else {
      // Sign in failed, display a message and update the UI
      Log.w(TAG, "signInWithCredential:failure", task.getException());
      if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
        verificationField.setError("Invalid code.");
      }
    }
  }

  /**
   * Verifies credentials of phone number with code.
   */
  private void verifyPhoneNumberWithCode() {
    String code = verificationField.getText().toString();
    if (code.length() > 1) {
      PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
      authController.signInWithPhoneAuthCredential(credential, this);
    }
  }
}