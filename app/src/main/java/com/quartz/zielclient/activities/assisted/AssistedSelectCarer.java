package com.quartz.zielclient.activities.assisted;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.signup.SignUpActivity;
import com.quartz.zielclient.exceptions.AuthorisationException;
import com.quartz.zielclient.user.UserController;
import com.quartz.zielclient.user.User;
import com.quartz.zielclient.user.UserFactory;

import java.util.Optional;

public class AssistedSelectCarer extends AppCompatActivity implements View.OnClickListener, ValueEventListener {

  private static final String TAG = AssistedSelectCarer.class.getSimpleName();

  private TextView carerEntry;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_assisted_select_carer);

    carerEntry = findViewById(R.id.carerEntry);
    Button button = findViewById(R.id.confirmCarerButton);
    button.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    if (v.getId() != R.id.confirmCarerButton) {
      return;
    }

    Log.i(TAG, "Confirming carer");
    String carerId = carerEntry.getText().toString();
    UserController.fetchUser(carerId, this);
  }

  @Override
  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    if (!dataSnapshot.exists()) {
      carerEntry.setError("Invalid user");
      return;
    }

    final String carerId = carerEntry.getText().toString();
    try {
      String thisUserId = UserController.retrieveFirebaseUser().getUid();
      Log.i(TAG, "Creating channel");

      Intent intent = new Intent(this, AssistedChannel.class);
      intent.putExtra("assisted", thisUserId);
      intent.putExtra("carer", carerId);

      startActivity(intent);
      finish();
    } catch (AuthorisationException e) {
      // In case of authorisation error go back to sign in
      Log.e(TAG, "Invalid login");
      Intent intent = new Intent(AssistedSelectCarer.this, SignUpActivity.class);
      startActivity(intent);
      finish();
    }
  }

  @Override
  public void onCancelled(@NonNull DatabaseError databaseError) {
    carerEntry.setError("Unknown error");
    Log.e(TAG, "Database error", databaseError.toException());
  }
}