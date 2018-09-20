package com.quartz.zielclient.models;

import android.os.Bundle;

/**
 * Interface for all model classes. Intends to allow models to be serialised to a bundle to allow
 * it to easily pass it to other activities.
 */
public interface Model {

  /**
   * Translates a model to a Bundle object to allow the Android OS to easily use it.
   * @return A bundle representation of this object.
   */
  Bundle toBundle();
}