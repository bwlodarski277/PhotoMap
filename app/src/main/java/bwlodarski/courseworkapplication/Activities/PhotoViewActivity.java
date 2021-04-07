package bwlodarski.courseworkapplication.Activities;

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

import bwlodarski.courseworkapplication.Fragments.PhotoGridFragment;
import bwlodarski.courseworkapplication.Fragments.PhotoMultiViewFragment;
import bwlodarski.courseworkapplication.Helpers.DatabaseHandler;
import bwlodarski.courseworkapplication.Helpers.ImageHandler;
import bwlodarski.courseworkapplication.R;
import bwlodarski.courseworkapplication.Static.UserPrefs;

public class PhotoViewActivity extends AppCompatActivity {

	private static final int CAMERA_PERMISSION = 22;
	private static final int CAMERA_REQUEST = 222;

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
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(cameraIntent, CAMERA_REQUEST);
		} else {
			requestPermission();
		}
		addPhotoPopup.dismiss();
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

	public void requestPermission() {
		String[] permissions = {Manifest.permission.CAMERA};
		ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == CAMERA_PERMISSION) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
				startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST);
			else {
				View view = findViewById(R.id.add_photo);
				Snackbar.make(view, "Permission not granted", Snackbar.LENGTH_LONG)
						.setAction("Retry", v -> requestPermission()).show();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
			assert data != null;
			addPhotoPopup.dismiss();
			Bitmap image = (Bitmap) data.getExtras().get("data");
			addImage(image);
		}
	}

	private void addImage(Bitmap image) {
		// Converting the byte array to a string safe for the DB
		String imgString = ImageHandler.getString(image);

		ContentValues imageData = new ContentValues();
		imageData.put(DatabaseHandler.Photos.PHOTO, imgString);

		try {
			long imageId = db.insertOrThrow(
					DatabaseHandler.Photos.TABLE, null, imageData);

			ContentValues userImageData = new ContentValues();
			userImageData.put(DatabaseHandler.UserPhotos.USER_KEY, userId);
			userImageData.put(DatabaseHandler.UserPhotos.PHOTO_KEY, imageId);

			db.insertOrThrow(DatabaseHandler.UserPhotos.TABLE, null, userImageData);

			setFragment();
		} catch (SQLException exception) {
			Toast.makeText(this, "There was an error with storing the image.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, exception.toString());
		}
	}
}