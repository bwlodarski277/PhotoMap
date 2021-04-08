package bwlodarski.photoMap.activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.fragments.PhotoGridFragment;
import bwlodarski.photoMap.fragments.PhotoMultiViewFragment;
import bwlodarski.photoMap.helpers.DatabaseHandler;
import bwlodarski.photoMap.helpers.ImageHandler;
import bwlodarski.photoMap.models.UserPrefs;

public class PhotoViewActivity extends AppCompatActivity {

	private static final int CAMERA_PERMISSION = 22;
	private static final int STORAGE_PERMISSION = 33;
	private static final int TAKE_PICTURE = 222;
	private static final int SELECT_PICTURE = 333;

	private static final String TAG = "PhotoViewActivity";
	private SQLiteDatabase db;
	private int userId;
	private PopupWindow addPhotoPopup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		setContentView(R.layout.activity_photo_view);
		Toolbar toolbar = findViewById(R.id.toolbar);
		// Reading intent to get username
		Intent intent = getIntent();
		String username = intent.getStringExtra(UserPrefs.usernameKey);
		toolbar.setTitle(String.format("%s's Photos", username));
		setSupportActionBar(toolbar);

		userId = intent.getIntExtra(UserPrefs.userIdKey, 0);

		DatabaseHandler handler = new DatabaseHandler(this);
		db = handler.getWritableDatabase();

		setFragment();

		FloatingActionButton fab = findViewById(R.id.add_photo);

		fab.setOnClickListener(view -> {
			/* Original
			int permission = ContextCompat.checkSelfPermission(
					view.getContext(), Manifest.permission.CAMERA);

			if (permission == PackageManager.PERMISSION_GRANTED) {
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, CAMERA_REQUEST);
			} else {
				requestPermission();
			}
			 */

			View root = findViewById(R.id.photo_view_root).getRootView();

			LayoutInflater inflater = getLayoutInflater();
			View popup = inflater.inflate(R.layout.fragment_popup, (ViewGroup) root, false);
			addPhotoPopup = new PopupWindow(popup,
					ActionBar.LayoutParams.MATCH_PARENT,
					ActionBar.LayoutParams.MATCH_PARENT, true);

			addPhotoPopup.setAnimationStyle(R.style.popup_fade_anim);

			addPhotoPopup.showAtLocation(findViewById(R.id.photo_view_root), Gravity.CENTER,
					ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
		});
	}

	public void leavePopup(View view) {
		addPhotoPopup.dismiss();
	}

	public void takePicture(View view) {
		int permission = ContextCompat.checkSelfPermission(
				view.getContext(), Manifest.permission.CAMERA);

		if (permission == PackageManager.PERMISSION_GRANTED) {
			_takePicture();
		} else {
			requestCameraPerm();
		}
		addPhotoPopup.dismiss();
	}

	private void _takePicture() {
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, TAKE_PICTURE);
	}

	public void selectPicture(View view) {
		int permission = ContextCompat.checkSelfPermission(
				view.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

		if (permission == PackageManager.PERMISSION_GRANTED) {
			_selectPicture();
		} else {
			requestStoragePerm();
		}
	}

	private void _selectPicture() {
		Intent storageIntent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(
				Intent.createChooser(storageIntent, "Select photo to add"), SELECT_PICTURE);
	}

	private void setFragment() {
		Fragment photoViewFragment;
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			photoViewFragment = new PhotoGridFragment();
		} else {
			photoViewFragment = new PhotoMultiViewFragment();
		}
		// Replacing `photo_view` with the photo grid fragment
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.photo_view, photoViewFragment).commit();
	}

	public void requestCameraPerm() {
		String[] permissions = {Manifest.permission.CAMERA};
		ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION);
	}

	public void requestStoragePerm() {
		String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
		ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION);
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
//		if (requestCode == CAMERA_PERMISSION) {
//			if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
//				_takePicture();
//			else {
//				View view = findViewById(R.id.add_photo);
//				Snackbar.make(view, "Permission not granted", Snackbar.LENGTH_LONG)
//						.setAction("Retry", v -> requestCameraPerm()).show();
//			}
//		}
		switch (requestCode) {
			case CAMERA_PERMISSION:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
					_takePicture();
				else {
					View view = findViewById(R.id.add_photo);
					Snackbar.make(view, "Permission not granted", Snackbar.LENGTH_LONG)
							.setAction("Retry", v -> requestCameraPerm()).show();
				}
				break;
			case STORAGE_PERMISSION:
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
					_selectPicture();
				else {
					View view = findViewById(R.id.add_photo);
					Snackbar.make(view, "Permission not granted", Snackbar.LENGTH_LONG)
							.setAction("Retry", v -> requestCameraPerm()).show();
				}
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		addPhotoPopup.dismiss();
		// Reading the photo taken by the user and adding the image to the DB.
		if (resultCode == Activity.RESULT_OK) {
			assert data != null;
			switch (requestCode) {
				case TAKE_PICTURE:
					Bitmap photoTaken = (Bitmap) data.getExtras().get("data");
					addImage(photoTaken);
					break;
				case SELECT_PICTURE:

					Uri imageUri = data.getData();
					InputStream stream;
					try {
						stream = getContentResolver().openInputStream(imageUri);
						Bitmap imageFromGallery = BitmapFactory.decodeStream(stream);
						addImage(imageFromGallery);
					} catch (FileNotFoundException exception) {
						Toast.makeText(this,
								"There was an error with storing the image.",
								Toast.LENGTH_LONG).show();
						Log.e(TAG, exception.toString());
					}

			}
		}
//		if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
//			assert data != null;
//			addPhotoPopup.dismiss();
//			Bitmap image = (Bitmap) data.getExtras().get("data");
//			addImage(image);
//		}
	}

	/**
	 * Adds a new photo to the database.
	 *
	 * @param image bitmap version of the image to insert into the database.
	 */
	private void addImage(Bitmap image) {
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
			} catch (SQLException exception) {
				Toast.makeText(this, "There was an error when writing to the database.",
						Toast.LENGTH_LONG).show();
				Log.e(TAG, exception.toString());
			}
		}
	}
}