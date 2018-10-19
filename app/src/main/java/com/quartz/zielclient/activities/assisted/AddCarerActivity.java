package com.quartz.zielclient.activities.assisted;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quartz.zielclient.R;
import com.quartz.zielclient.request.AddCarerRequestHandler;
import com.quartz.zielclient.request.CarerRequestListener;
import com.quartz.zielclient.user.SystemService;

/**
 * Activity allows user to add a permanent carer.
 *
 * @author Bilal Shehata
 */
public class AddCarerActivity extends AppCompatActivity
    implements View.OnClickListener, CarerRequestListener {
  // Input for the Number should be in +61.... format
  private EditText inputNumber;
  // Request handler allows this activity to know whether the request passed or failed
  private AddCarerRequestHandler addCarerRequestHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addCarerRequestHandler = new AddCarerRequestHandler();
    // set the current layout file
    setContentView(R.layout.activity_add_carer);
    Button sendRequest = findViewById(R.id.sendRequestButton);
    sendRequest.setOnClickListener(this);
    inputNumber = findViewById(R.id.carerNumberInput);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  /**
   * when the button is clicked a request is sent based on the input number
   * @param view This is the view being clicked
   */
  @Override
  public void onClick(View view) {
    int i = view.getId();
    if (i == R.id.sendRequestButton) {
      // Pass this as an argument to allow for callback to be made
      String phoneNumber = inputNumber.getText().toString();
      if (!SystemService.verifyNumberFormat(phoneNumber)) {
        inputNumber.setError("Invalid number format.");
      } else {
        addCarerRequestHandler.addCarer(inputNumber.getText().toString(), this);
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  /** display error if user is not found in DB */
  @Override
  public void userNotFound() {
    Toast.makeText(getApplicationContext(), R.string.userNotFound, Toast.LENGTH_LONG).show();
  }

  /** Return to home if user is found and added */
  @Override
  public void userFound() {
    Toast.makeText(getApplicationContext(), R.string.userAdded, Toast.LENGTH_LONG).show();
    onBackPressed();
  }
}
