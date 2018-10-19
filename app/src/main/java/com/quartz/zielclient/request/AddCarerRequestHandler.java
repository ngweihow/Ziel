package com.quartz.zielclient.request;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * this class handles Allowing an assisted to add a carer to their list
 *
 * @author Bilal Shehata
 */
public class AddCarerRequestHandler implements ValueEventListener {
  private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
  private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
  private DatabaseReference relationshipReference = firebaseDatabase.getReference("relationships/");
  private DatabaseReference userReference = firebaseDatabase.getReference("users/");
  private CarerRequestListener carerRequestListener;

  public AddCarerRequestHandler() {
    super();
  }

  public void addCarer(String phoneNumber, CarerRequestListener requestListener) {
    carerRequestListener = requestListener;
    userReference
        .orderByChild("phoneNumber")
        .equalTo(phoneNumber)
        .addListenerForSingleValueEvent(this);
  }

  @Override
  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    // since mobile numbers are unique this should only return a single user
    if (dataSnapshot.exists()) {
      dataSnapshot
          .getChildren()
          .forEach(
              (user) -> {
                String carerId = user.getKey();
                if (firebaseAuth.getUid() != null && carerId !=null) {
                  relationshipReference
                      .child(firebaseAuth.getUid())
                      .child(carerId)
                      .setValue(user.getValue());
                }
              });
      carerRequestListener.userFound();
    } else {
      carerRequestListener.userNotFound();
    }
  }

  @Override
  public void onCancelled(@NonNull DatabaseError databaseError) {}
}
