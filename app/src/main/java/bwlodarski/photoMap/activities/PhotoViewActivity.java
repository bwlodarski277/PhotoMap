package bwlodarski.photoMap.activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.fragments.PhotoGridFragment;
import bwlodarski.photoMap.helpers.DatabaseHandler;
import bwlodarski.photoMap.helpers.ImageHandler;
import bwlodarski.photoMap.models.UserPrefs;

/**
 * Photo view activity
 * Used display the photo grid (and photo details if in landscape)
 */
public class PhotoViewActivity extends AppCompatActivity {

	private static final int CAMERA_PERMISSION = 22;
	private static final int TAKE_PICTURE = 222;
	private static final int SELECT_PICTURE = 333;
	private static final int GET_LOCATION = 444;

	private static final String TAG = "PhotoViewActivity";
	FirebaseUser user;
	private SQLiteDatabase db;
	private int userId;
	private String username;
	private PopupWindow addPhotoPopup;

	@Override
	protected void onResume() {
		super.onResume();
		// Making sure we re-render if any settings are changed
		setFragment();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		setContentView(R.layout.activity_photo_view);
		Toolbar toolbar = findViewById(R.id.toolbar);

		// Getting firebase authentication
		FirebaseAuth auth = FirebaseAuth.getInstance();
		user = auth.getCurrentUser();

		// Reading intent to get username
		Intent intent = getIntent();
		username = intent.getStringExtra(UserPrefs.usernameKey);
		toolbar.setTitle(String.format("%s's Photos", username));
		toolbar.setSubtitle("All photos");
		setSupportActionBar(toolbar);

		// Getting the user ID from the login activity
		userId = intent.getIntExtra(UserPrefs.userIdKey, 0);

		DatabaseHandler handler = new DatabaseHandler(this);
		db = handler.getWritableDatabase();

		setFragment();

		FloatingActionButton fab = findViewById(R.id.add_photo);

		// When the user clicks the fab in the bottom right, display a popup window that
		// lets the user choose whether they want to take a photo or select one from their device.
		fab.setOnClickListener(view -> {
			View root = findViewById(R.id.photo_view_root).getRootView();

			LayoutInflater inflater = getLayoutInflater();
			View popup = inflater.inflate(R.layout.fragment_popup, (ViewGroup) root, false);
			addPhotoPopup = new PopupWindow(popup,
					ActionBar.LayoutParams.MATCH_PARENT,
					ActionBar.LayoutParams.MATCH_PARENT, true);

			// Setting popup animation so that the popup fades in and out
			addPhotoPopup.setAnimationStyle(R.style.popup_fade_anim);

			// Displaying the popup
			addPhotoPopup.showAtLocation(findViewById(R.id.photo_view_root), Gravity.CENTER,
					ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
		});
	}

	/**
	 * Closes the popup
	 * @param view view that called this function
	 */
	public void leavePopup(View view) {
		addPhotoPopup.dismiss();
	}

	/**
	 * Requests the camera permission and takes a photo if it is granted
	 * @param view view that called this function
	 */
	public void takePicture(View view) {
		// Checking permission
		int permission = ContextCompat.checkSelfPermission(
				view.getContext(), Manifest.permission.CAMERA);

		if (permission == PackageManager.PERMISSION_GRANTED) {
			_takePicture();
		} else {
			requestCameraPerm();
		}
		// Close popup once user clicks something
		addPhotoPopup.dismiss();
	}

	/**
	 * Takes a picture by starting a camera intent
	 */
	private void _takePicture() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, TAKE_PICTURE);
	}

	/**
	 * Selects a picture from the gallery to add to the photo grid.
	 * @param view view that called this function
	 */
	public void selectPicture(View view) {
		_selectPicture();
	}

	/**
	 * Starts the storage intent to select a photo from the gallery
	 */
	private void _selectPicture() {
		Intent storageIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(
				Intent.createChooser(storageIntent, "Select photo to add"), SELECT_PICTURE);
	}

	/**
	 * Placing the photo grid fragment in the view
	 */
	private void setFragment() {
		Fragment photoViewFragment = new PhotoGridFragment();
		// Replacing `photo_view` with the photo grid fragment
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.photo_view, photoViewFragment).commit();
	}

	/**
	 * Requests camera permission.
	 * Once granted, the result is sent to onRequestPermissionResult
	 */
	public void requestCameraPerm() {
		String[] permissions = {Manifest.permission.CAMERA};
		ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION);
	}

	/**
	 * Requesting camera permissions.
	 * If permission is not granted, displays a Snackbar, allowing the user to retry.
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == CAMERA_PERMISSION) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
				_takePicture();
			else {
				View view = findViewById(R.id.add_photo);
				Snackbar.make(view, "Permission not granted", Snackbar.LENGTH_LONG)
						.setAction("Retry", v -> requestCameraPerm()).show();
			}
		}
		// Closing the popup window
		addPhotoPopup.dismiss();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_photo_grid, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.menu_map_button) {
			// When the user clicks the photo map icon, take them to the photo map activity
			Intent photoMapIntent = new Intent(this, PhotoMapActivity.class);
			photoMapIntent.putExtra(UserPrefs.usernameKey, username);
			photoMapIntent.putExtra(UserPrefs.userIdKey, userId);
			startActivity(photoMapIntent);
			finish();
		} else if (itemId == R.id.menu_settings) {
			// When the user clicks the settings icon, take them to the settings activity
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
		} else {
			Log.e(TAG, "Invalid menu item selected");
			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		addPhotoPopup.dismiss();
		// Reading the photo taken by the user and adding the image to the DB.
		if (resultCode == Activity.RESULT_OK) {
			assert data != null;
			Intent locationIntent;
			switch (requestCode) {
				// When the user decides to take a picture
				case TAKE_PICTURE:
					Bitmap photoTaken = (Bitmap) data.getExtras().get("data");
					// Adding the photo to the database. Later we will
					// add the location and sensors.
					long photoId = addImage(photoTaken);

					locationIntent = new Intent(
							this, LocationSelectionActivity.class);
					locationIntent.putExtra("ID", photoId);
					// Once the photo is taken, take user to activity that lets them pick
					// where the photo was taken
					startActivityForResult(locationIntent, GET_LOCATION);

					break;
				// When the user decides to select a picture from their device
				case SELECT_PICTURE:
					Uri imageUri = data.getData();
					InputStream stream;
					try {
						// Reading the selected photo from the storage
						stream = getContentResolver().openInputStream(imageUri);
						Bitmap imageFromGallery = BitmapFactory.decodeStream(stream);
						// Adding the photo to the database. Later we will
						// add the location and sensors.
						long imageId = addImage(imageFromGallery);

						locationIntent = new Intent(
								this, LocationSelectionActivity.class);
						locationIntent.putExtra("ID", imageId);
						// Once the photo is taken, take user to activity that lets them pick
						// where the photo was taken
						startActivityForResult(locationIntent, GET_LOCATION);

					} catch (FileNotFoundException exception) {
						Toast.makeText(getApplicationContext(),
								"There was an error with storing the image.",
								Toast.LENGTH_LONG).show();
						Log.e(TAG, exception.toString());
					}
					break;
				// When the user returns from the screen which lets them select the photo location
				case GET_LOCATION:
					Bundle bundle = data.getBundleExtra("LatLng");
					LatLng newLoc = bundle.getParcelable("LatLng");
					long photo = data.getLongExtra("ID", -1);
					float temperature = data.getFloatExtra("TEMP", 0);
					float light = data.getFloatExtra("LIGHT", 0);
					// Adding the new photo along with its location and sensor readings to DB
					setPhotoData(photo, newLoc, temperature, light);
			}
		}
	}

	/**
	 * Assigns extra data to a photo row in the database.
	 * @param photoId ID of photo row to modify
	 * @param loc location to set
	 * @param temperature temperature to set
	 * @param light light level to set
	 */
	private void setPhotoData(long photoId, LatLng loc, float temperature, float light) {
		String where = String.format("%s = ?", DatabaseHandler.Photos.KEY);
		String[] whereArgs = {String.valueOf(photoId)};

		ContentValues contentValues = new ContentValues();
		contentValues.put(DatabaseHandler.Photos.LAT, (float) loc.latitude);
		contentValues.put(DatabaseHandler.Photos.LON, (float) loc.longitude);
		contentValues.put(DatabaseHandler.Photos.TEMP, temperature);
		contentValues.put(DatabaseHandler.Photos.LIGHT, light);

		try {
			// Inserting the values to the row of the photo that was just taken
			db.update(DatabaseHandler.Photos.TABLE, contentValues, where, whereArgs);
		} catch (SQLException exception) {
			Log.e(TAG, exception.toString());
			Toast.makeText(this, "There was an error when setting photo location.",
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Adds a new photo to the database.
	 *
	 * @param image bitmap version of the image to insert into the database.
	 * @return image ID
	 */
	private long addImage(Bitmap image) {
		// Converting the byte array to a string safe for the DB
		byte[] imgBytes = ImageHandler.bitmapToBytes(image);

		String path = null;

		// Creating an internal images directory
		File imageDir = new File(getFilesDir(), "images");
		boolean created = false;
		// If the directory doesn't exist yet, make it
		if (!imageDir.exists()) created = imageDir.mkdir();
		if (created || imageDir.exists()) {
			// Generating a random image name
			String fileName = UUID.randomUUID().toString() + ".png";
			File imageFile = new File(imageDir, fileName);
			try {
				// Writing image bytes to the file
				path = imageFile.getPath();
				FileOutputStream stream = new FileOutputStream(path);
				stream.write(imgBytes);
			} catch (IOException exception) {
				Toast.makeText(this, "There was an error with storing the image.",
						Toast.LENGTH_LONG).show();
				Log.e(TAG, exception.toString());
			}

			// Creating ContentValues object to insert data into
			ContentValues imageData = new ContentValues();
			imageData.put(DatabaseHandler.Photos.PHOTO, path);

			try {
				// Inserting the photo itself into the database
				long imageId = db.insertOrThrow(
						DatabaseHandler.Photos.TABLE, null, imageData);

				ContentValues userImageData = new ContentValues();
				userImageData.put(DatabaseHandler.UserPhotos.USER_KEY, userId);
				userImageData.put(DatabaseHandler.UserPhotos.PHOTO_KEY, imageId);

				// Using ID of inserted photo and user ID to associate image with user.
				db.insertOrThrow(
						DatabaseHandler.UserPhotos.TABLE, null, userImageData);

				setFragment();
				return imageId;
			} catch (SQLException exception) {
				Toast.makeText(this, "There was an error when writing to the database.",
						Toast.LENGTH_LONG).show();
				Log.e(TAG, exception.toString());
			}
		}
		return 0;
	}
}