package com.quartz.zielclient.review;

import android.os.Bundle;

import com.quartz.zielclient.models.Model;

/**
 * Model representation of a user review.
 *
 * @author alexvosnakis
 */
public class Review implements Model {
  private float simpleStars;
  private float usefulStars;
  private String reviewText;

  public Review() {
    // Intentionally empty
  }

  public Review(float simpleStars, float usefulStars, String reviewText) {
    this.simpleStars = simpleStars;
    this.usefulStars = usefulStars;
    this.reviewText = reviewText;
  }

  public float getSimpleStars() {
    return simpleStars;
  }

  public void setSimpleStars(float simpleStars) {
    this.simpleStars = simpleStars;
  }

  public float getUsefulStars() {
    return usefulStars;
  }

  public void setUsefulStars(float usefulStars) {
    this.usefulStars = usefulStars;
  }

  public String getReviewText() {
    return reviewText;
  }

  public void setReviewText(String reviewText) {
    this.reviewText = reviewText;
  }

  @Override
  public Bundle toBundle() {
    Bundle bundle = new Bundle();
    bundle.putFloat("simpleStars", simpleStars);
    bundle.putFloat("usefulStars", usefulStars);
    bundle.putString("reviewText", reviewText);
    return bundle;
  }
}
