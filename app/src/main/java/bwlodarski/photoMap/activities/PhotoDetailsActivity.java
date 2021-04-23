package bwlodarski.photoMap.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.fragments.PhotoDetailsFragment;
import bwlodarski.photoMap.helpers.DatabaseHandler;

/**
 * Photo details activity
 * Used for displaying a large version of a photo that was clicked as well as the details.
 */
public class PhotoDetailsActivity extends FragmentActivity {

	private static final String TAG = "PhotoDetailsActivity";
	SQLiteDatabase db;

	/**
	 * Fills the fragment with the photo and its associated data.
	 */
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_details);

		// Getting a database handler
		DatabaseHandler handler = new DatabaseHandler(getApplicationContext());
		db = handler.getReadableDatabase();

		// Reading intent data
		Intent intent = getIntent();
		int imageId = intent.getIntExtra("photo", -1);

		// Getting the details fragment and setting its data by the image ID passed
		PhotoDetailsFragment fragment = (PhotoDetailsFragment)
				getSupportFragmentManager().findFragmentById(R.id.detail_fragment);
		assert fragment != null;
		fragment.setDetails(imageId);
	}
}
