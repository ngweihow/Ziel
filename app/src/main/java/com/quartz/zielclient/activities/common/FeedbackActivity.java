package com.quartz.zielclient.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.quartz.zielclient.R;
import com.quartz.zielclient.review.ReviewController;

/**
 * Activity that allows users to submit app reviews.
 *
 * @author alexvosnakis
 */
public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener, ValueEventListener {

  private static final String TAG = FeedbackActivity.class.getSimpleName();

  private RatingBar simpleBar;
  private RatingBar usefulBar;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feedback_form);

    Button submitReviewButton = findViewById(R.id.submitReview);
    submitReviewButton.setOnClickListener(this);

    simpleBar = findViewById(R.id.simpleBar);
    usefulBar = findViewById(R.id.usefulBar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  /**
   * Attempt to submit the user's review. Exits early if the view has presses 0 stars.
   */
  private void attemptSubmit() {
    float simpleStars = simpleBar.getRating();
    float usefulStars = usefulBar.getRating();

    TextView reviewBox = findViewById(R.id.reviewInput);
    String reviewText = reviewBox.getText().toString();
    ReviewController.uploadReview(simpleStars, usefulStars, reviewText, this);
  }

  /**
   * On click listener. The submit review button attempts to submit the review, otherwise the star
   * components
   *
   * @param v The view that was clicked.
   */
  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.submitReview) {
      attemptSubmit();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      // Respond to the action bar's Up/Home button
      Intent intent = new Intent(this, SettingsHome.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      startActivity(intent);
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    Log.i(TAG, "Successfully submitted review.");
    Toast.makeText(this, "Review submitted!", Toast.LENGTH_LONG).show();
  }

  @Override
  public void onCancelled(@NonNull DatabaseError databaseError) {
    Log.e(TAG, "Error uploading review", databaseError.toException());
    Toast.makeText(this, "Error submitting review.", Toast.LENGTH_LONG).show();
  }
}
