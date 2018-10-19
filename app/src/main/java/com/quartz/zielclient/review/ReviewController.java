package com.quartz.zielclient.review;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.quartz.zielclient.user.UserController;

import java.util.Optional;

/**
 * Controller for reviews.
 */
public class ReviewController {
  private static final String TAG = ReviewController.class.getSimpleName();

  private static final String REVIEWS_PATH = "reviews";
  private static final FirebaseDatabase database = FirebaseDatabase.getInstance();

  private ReviewController() {
    // Intentionally empty
  }

  /**
   * Uploads a user's review.
   *
   * @param simpleStars The number of stars for simplicity given to the review.
   * @param usefulStars The number of stars for usefulness given to the review.
   * @param reviewText  The user's review comment.
   * @param listener    The listener waiting for an update.
   */
  public static void uploadReview(float simpleStars, float usefulStars, String reviewText, ValueEventListener listener) {
    Log.i(TAG, "Uploading review.");
    Review review = new Review(simpleStars, usefulStars, reviewText);
    Optional<String> maybeId = UserController.retrieveUid();
    maybeId.ifPresent(userId -> {
      DatabaseReference ref = database.getReference(REVIEWS_PATH);
      ref.addListenerForSingleValueEvent(listener);
      ref.child(userId).push().setValue(review);
    });
  }
}