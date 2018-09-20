package com.quartz.zielclient.user;

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import static com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken;
import static com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks;

public final class AuthorisationController {

  private static final String TAG = "AuthorisationController";
  private static final long TIMEOUT = 60;
  private static final TimeUnit TIMEOUT_UNITS = TimeUnit.SECONDS;

  private Activity activity;
  private String phoneNumber;

  private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
  private PhoneAuthProvider phoneAuthProvider = PhoneAuthProvider.getInstance();

  public AuthorisationController(String phoneNumber, Activity activity) {
    this.activity = activity;
    this.phoneNumber = phoneNumber;
  }

  public void sendConfirmationCode(OnVerificationStateChangedCallbacks callbacks) {
    Log.i(TAG, "Sending confirmation code to number: " + phoneNumber);
    phoneAuthProvider.verifyPhoneNumber(phoneNumber, TIMEOUT, TIMEOUT_UNITS, activity, callbacks);
  }

  public void resendConfirmationCode(OnVerificationStateChangedCallbacks callbacks,
                                     ForceResendingToken resendingToken) {
    Log.i(TAG, "Sending confirmation code to number: " + phoneNumber);
    phoneAuthProvider.verifyPhoneNumber(
        phoneNumber, TIMEOUT, TIMEOUT_UNITS, activity, callbacks, resendingToken
    );
  }

  public void signInWithPhoneAuthCredential(PhoneAuthCredential credential,
                                            OnCompleteListener<AuthResult> listener) {
    Log.i(TAG, "Signing in user.");
    firebaseAuth.signInWithCredential(credential)
        .addOnCompleteListener(activity, listener);
  }
}
