package com.quartz.zielclient.activities.channel;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.quartz.zielclient.R;
import com.quartz.zielclient.activities.common.SettingsHome;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static com.wonderkiln.camerakit.CameraKit.Constants.FLASH_OFF;
import static com.wonderkiln.camerakit.CameraKit.Constants.FACING_BACK;
import static com.wonderkiln.camerakit.CameraKit.Constants.FOCUS_CONTINUOUS;
import static com.wonderkiln.camerakit.CameraKit.Constants.METHOD_STANDARD;
import static com.wonderkiln.camerakit.CameraKit.Constants.ZOOM_PINCH;
import static com.wonderkiln.camerakit.CameraKit.Constants.PERMISSIONS_STRICT;

import static android.graphics.Bitmap.CompressFormat.JPEG;

/**
 * This class is responsible for setting up a camera view to take photos of a location.
 * This class uses the CameraKit API.
 * <p>
 * Documentation: https://github.com/CameraKit/camerakit-android
 *
 * @author Armaan McLeod
 * @version 1.0
 * 9/10/2018
 */
public class TakePhotosActivity extends AppCompatActivity {

  private static final int REQUEST_WRITE_PERMISSION = 1;
  private static final int PICK_IMAGE = 1;
  private final String activity = this.getClass().getSimpleName();

  private CameraView cameraView;

  private boolean canTakePicture;
  private ImageView imageView;

  private String currentPhotoPath;

  private File storageDir;

  private boolean permissionGranted;

  /**
   * Camera event listener which listens for camera events.
   */
  private final CameraKitEventListener cameraListener = new CameraKitEventListener() {

    /**
     * Checks if camera is opened or closed.
     * @param cameraKitEvent The camera event kit.
     */
    @Override
    public void onEvent(CameraKitEvent cameraKitEvent) {
      String s = cameraKitEvent.getType();
      if (CameraKitEvent.TYPE_CAMERA_OPEN.equals(s)) {
        Log.d(activity, "Camera enabled");
        canTakePicture = true;

      } else if (CameraKitEvent.TYPE_CAMERA_CLOSE.equals(s)) {
        Log.d(activity, "Camera disabled");
        canTakePicture = false;
      }
    }

    /**
     * Checks if camera had any errors executing any tasks.
     * @param cameraKitError The camera event kit.
     */
    @Override
    public void onError(CameraKitError cameraKitError) {
      Log.d(activity, "Error opening camera");
    }

    /**
     * Extracts image taken into Bitmap form.
     * @param cameraKitImage The camera event kit.
     */
    @Override
    public void onImage(CameraKitImage cameraKitImage) {
      Log.d(activity, "Taking picture");

      // Extract bitmap picture
      byte[] picture = cameraKitImage.getJpeg();
      Bitmap bitmap = BitmapFactory.decodeByteArray(picture, 0, picture.length);

      // Run firebase ML recognition
      imageView.setImageBitmap(bitmap);
      runLandMarkRecognition(bitmap);

      // Save the file to photos path
      try {
        saveImageFile(bitmap);
        Log.d(activity, "Saving file to " + currentPhotoPath);
      } catch (IOException e) {
        Log.e(activity, "Error saving file", e);
      }
    }

    /**
     * Event listen for videos, out of scope for this feature.
     * @param cameraKitVideo The camera event kit.
     */
    @Override
    public void onVideo(CameraKitVideo cameraKitVideo) {
      Log.d(activity, "Taking video");
    }

  };

  /**
   * Detects landmark in photo taken.
   */
  private void runLandMarkRecognition(Bitmap bitmap) {

    // Use latest model options
    FirebaseVisionCloudDetectorOptions options = new FirebaseVisionCloudDetectorOptions.Builder()
        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
        .setMaxResults(15)
        .build();

    // Convert to firebase image
    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

    // Create landmark detector
    FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
        .getVisionCloudLandmarkDetector(options);

    // Check to see if it is a landmark
    detector.detectInImage(image)
        .addOnSuccessListener(firebaseVisionCloudLandmarks -> {
          Log.d(activity, "Listener success");
          Log.d(activity, Integer.toString(firebaseVisionCloudLandmarks.size()));

          if (firebaseVisionCloudLandmarks.isEmpty()) {
            Toast.makeText(TakePhotosActivity.this,
                "Landmark not recognised",
                Toast.LENGTH_LONG).show();
          } else {

            // Show the first landmark given
            Toast.makeText(TakePhotosActivity.this,
                "Landmark: " + firebaseVisionCloudLandmarks.get(0).getLandmark(),
                Toast.LENGTH_LONG).show();
          }
        })
        .addOnFailureListener(e -> {
          Log.d(activity, "Listener failure");
          Toast.makeText(TakePhotosActivity.this,
              "Landmark recognition not operation currently",
              Toast.LENGTH_LONG).show();
        });
  }

  /**
   * Called when the activity is first created.
   * This is where you should do all of your normal static set up:
   * create views, bind data to lists, etc.
   * This method also provides you with a Bundle containing the activity's
   * previously frozen state, if there was one.
   * <p>
   * Documentation: https://developer.android.com/reference/android/app/Activity.html
   * #onCreate(android.os.Bundle)
   *
   * @param savedInstanceState This is the data it most recently supplied in
   *                           onSaveInstanceState(Bundle).
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.take_photos);

    String packageName = getApplicationContext().getPackageName();

    // Storage directory for photos taken
    storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), packageName);

    imageView = findViewById(R.id.photo);

    // Create camera view
    cameraView = findViewById(R.id.camera);
    cameraView.addCameraKitListener(cameraListener);

    // Initialise preferred settings
    cameraView.setFlash(FLASH_OFF);
    cameraView.setFacing(FACING_BACK);
    cameraView.setFocus(FOCUS_CONTINUOUS);
    cameraView.setMethod(METHOD_STANDARD);
    cameraView.setZoom(ZOOM_PINCH);
    cameraView.setPermissions(PERMISSIONS_STRICT);
    cameraView.setJpegQuality(100);

    // button which captures photo
    Button takePhotoButton = findViewById(R.id.camera_button);
    takePhotoButton.setOnClickListener(v -> {
      Log.d(activity, Boolean.toString(canTakePicture));
      Log.d(activity, "Clicked photo button");

      // Only proceed until permissions are granted
      if (!permissionGranted) {
        requestStoragePermission();
      } else {
        if (canTakePicture) {
          Log.d(activity, "Captured image");
          cameraView.captureImage();
        }
      }
    });

    // Shows gallery of recently taken photos
    Button galleryPhotoButton = findViewById(R.id.gallery_button);
    galleryPhotoButton.setOnClickListener(v -> {
      Log.d(activity, "Clicked gallery button");
      pickImageGallery();
    });

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    // Request initial permissions
    requestStoragePermission();
  }

  /**
   * Opens up image gallery to select a photo.
   */
  private void pickImageGallery() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
  }

  /**
   * Notifies when image has been selected from gallery
   *
   * @param requestCode This is the request code passed through to verify activity.
   * @param resultCode  This is the result code is the activity was created.
   * @param data        The data extracted from activity
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
      Uri selectedImage = data.getData();

      try {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
        imageView.setImageBitmap(bitmap);
        runLandMarkRecognition(bitmap);
      } catch (IOException e) {
        Log.d(activity, e.toString());
      }
    }
  }

  /**
   * This is a callback for requesting and checking the result of a permission.
   *
   * <p>Documentation : https://developer.android.com/reference/android/support/v4/app/
   * ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult
   *
   * @param requestCode  This is the request code passed to requestPermissions.
   * @param permissions  This is the permissions.
   * @param grantResults This is results for granted or un-granted permissions.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    // Forward results to EasyPermissions
    EasyPermissions.onRequestPermissionsResult(requestCode,
        permissions, grantResults, this);
  }

  /**
   * Check location permissions before showing user location.
   */
  @AfterPermissionGranted(REQUEST_WRITE_PERMISSION)
  public void requestStoragePermission() {
    String[] perms = {WRITE_EXTERNAL_STORAGE};
    if (EasyPermissions.hasPermissions(this, perms)) {
      permissionGranted = true;
    } else {
      EasyPermissions.requestPermissions(this,
          "Please grant the storage permission", REQUEST_WRITE_PERMISSION, perms);
      permissionGranted = false;
    }
  }

  /**
   * Saves an image in storage location.
   *
   * @param image The image to save.
   * @throws IOException Throws exception if file cannot write out.
   */
  private void saveImageFile(Bitmap image) throws IOException {
    boolean success = true;

    // Create the directory if it doesn't exist
    if (!storageDir.exists()) {
      success = storageDir.mkdirs();
    }

    if (success) {
      // Create an image file
      String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
          .format(new Date());
      String imageFileName = "JPEG_" + timeStamp + "_";
      File imageFile = File.createTempFile(
          imageFileName,  /* prefix */
          ".jpg",         /* suffix */
          storageDir      /* directory */
      );

      currentPhotoPath = imageFile.getAbsolutePath();

      // Write file to directory
      writeImage(imageFile, image);

      // Add file to gallery
      addImageToGallery(getContentResolver(), "jpg", imageFile);

      Toast.makeText(this, "Photo taken. You can also select photo from gallery", Toast.LENGTH_LONG).show();
    } else {
      Log.d(activity, "Directory could not be created");
    }
  }

  /**
   * Writes image to device storage.
   *
   * @param imageFile The image file to process.
   * @param image     The bitmap picture.
   */
  private void writeImage(File imageFile, Bitmap image) {
    // Write file to directory
    try (OutputStream fileOut = new FileOutputStream(imageFile)) {
      image.compress(JPEG, 100, fileOut);
    } catch (Exception e) {
      Log.d(activity, e.toString());
    }
  }

  /**
   * Adds image to media store to be visible by gallery.
   *
   * @param cr       THe content resolver manager which manages media files.
   * @param imgType  The image extension to set the mime type.
   * @param filepath The filepath of the image stored on the device.
   */
  public void addImageToGallery(ContentResolver cr, String imgType, File filepath) {
    ContentValues values = new ContentValues();

    // Set properties for media file
    values.put(MediaStore.Images.Media.TITLE, filepath.getName());
    values.put(MediaStore.Images.Media.DISPLAY_NAME, filepath.getName());
    values.put(MediaStore.Images.Media.DESCRIPTION, "ZielClient photo");
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + imgType);
    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
    values.put(MediaStore.Images.Media.DATA, filepath.toString());

    // Insert properties into media store
    cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
  }

  /**
   * Called when the activity will start interacting with the user.
   * At this point your activity is at the top of the activity stack, with user input going to it.
   * <p>
   * Documentation: https://developer.android.com/reference/android/app/Activity.html#onResume()
   */
  @Override
  protected void onResume() {
    super.onResume();
    cameraView.start();
  }

  /**
   * Called when the system is about to start resuming a previous activity.
   * This is typically used to commit unsaved changes to persistent data,
   * stop animations and other things that may be consuming CPU, etc.
   * Implementations of this method must be very quick because the next activity will
   * not be resumed until this method returns.
   * <p>
   * Documentation: https://developer.android.com/reference/android/app/Activity.html#onPause()
   */
  @Override
  protected void onPause() {
    cameraView.stop();
    super.onPause();
  }

  /**
   * This hook is called whenever an item in your options menu is selected.
   * <p>
   * Documentation: https://developer.android.com/reference/android/app/Activity#
   * onOptionsItemSelected(android.view.MenuItem)
   *
   * @param item The menu item selected
   * @return boolean If the option was selected.
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}