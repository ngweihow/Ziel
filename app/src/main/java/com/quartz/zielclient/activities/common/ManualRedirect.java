package com.quartz.zielclient.activities.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.carer.CarerHomepageActivity;
import com.quartz.zielclient.activities.assisted.AssistedSelectCarer;

/**
 * Temporary Activity to explore and integrate session creation into the application
 *
 * @author Bilal Shehata
 */
public class ManualRedirect extends AppCompatActivity implements View.OnClickListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_manual_redirect);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    Button toCarerSession = findViewById(R.id.toCarerSession);
    Button toAssistedSession = findViewById(R.id.toAssistedActivity);
    toCarerSession.setOnClickListener(this);
    toAssistedSession.setOnClickListener(this);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.toCarerSession:
        startActivity(new Intent(ManualRedirect.this, CarerHomepageActivity.class));
        break;
      case R.id.toAssistedActivity:
        startActivity(new Intent(ManualRedirect.this, AssistedSelectCarer.class));
        break;
      default:
        break;
    }
  }
}