package com.quartz.zielclient.user;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Optional;

public final class UserController {

  private static final String TAG = UserController.class.getSimpleName();

  /**
   * Path to the users in the database.
   */
  private static final String USER_DATABASE_PATH = "users";

  private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
  private static final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

  private UserController() {
    // Intentionally empty
  }

  /**
   * Attempts to return the currently logged in Firebase user.
   *
   * @return The Firebase user.
   * @throws AuthorisationException If the FirebaseUser could not be found.
   */
  public static FirebaseUser retrieveFirebaseUser() throws AuthorisationException {
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    if (firebaseUser == null) {
      throw new AuthorisationException("Failed to find user.");
    }
    return firebaseUser;
  }

  public static Optional<String> retrieveUid() {
    if (isSignedIn()) {
      return Optional.of(firebaseAuth.getCurrentUser().getUid());
    } else {
      return Optional.empty();
    }
  }

  public static boolean isSignedIn() {
    return firebaseAuth.getCurrentUser() != null;
  }

  /**
   * Creates a user in the database. To keep it synced with the authentication database, the key for
   * each user matches the UID given to each FirebaseUser.
   *
   * @param firebaseUser A reference to the Firebase authenticated user.
   * @param firstName    The user's first name.
   * @param lastName     The user's last name.
   * @param assisted     Whether this user is an assisted.
   */
  public static User createUser(@NonNull FirebaseUser firebaseUser,
                                String firstName,
                                String lastName,
                                boolean assisted) {
    String userId = firebaseUser.getUid();
    Log.i(TAG, "Creating user " + userId);
    String phoneNumber = firebaseUser.getPhoneNumber();
    User user = new User(firstName, lastName, phoneNumber, assisted);

    DatabaseReference ref = firebaseDatabase.getReference(USER_DATABASE_PATH);
    ref.child(userId).setValue(user);
    return user;
  }

  /**
   * Locates and performs an action with a user from the database.
   *
   * @param userId The UID of the user to look up.
   */
  public static void fetchUser(final String userId, ValueEventListener listener) {
    DatabaseReference ref = firebaseDatabase.getReference(userIdPath(userId));
    ref.addListenerForSingleValueEvent(listener);
  }

  public static void fetchThisUser(ValueEventListener listener) throws AuthorisationException {
    FirebaseUser firebaseUser = retrieveFirebaseUser();
    fetchUser(firebaseUser.getUid(), listener);
  }

  public static void updateSelf(final User user, ValueEventListener listener) {
    Optional<String> userId = retrieveUid();
    userId.ifPresent(id -> {
      DatabaseReference ref = firebaseDatabase.getReference(USER_DATABASE_PATH).child(id);
      ref.addListenerForSingleValueEvent(listener);
      ref.setValue(user);
    });
  }

  private static String userIdPath(String userId) {
    return USER_DATABASE_PATH + "/" + userId;
  }
}