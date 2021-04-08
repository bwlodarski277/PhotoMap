package bwlodarski.photoMap.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.io.IOException;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.helpers.DatabaseHandler;
import bwlodarski.photoMap.helpers.ImageHandler;

/**
 * Photo details activity
 * Used for displaying a large version of a photo that was clicked as well as the details.
 */
public class PhotoDetailsActivity extends FragmentActivity {

	private static final String TAG = "PhotoDetailsActivity";

	/**
	 * Fills the fragment with the photo and its associated data.
	 */
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_details);

		DatabaseHandler handler = new DatabaseHandler(this);
		SQLiteDatabase db = handler.getReadableDatabase();

		// Reading intent data
		Intent intent = getIntent();
		int imageId = (intent.getIntExtra("photo", -1));

		String selection = String.format("%s = ?", DatabaseHandler.Photos.KEY);
		String[] args = {String.valueOf(imageId)};

		Bitmap photoBitmap = null;
		try (Cursor cursor = db.query(DatabaseHandler.Photos.TABLE, null,
				selection, args, null, null, null)) {
			if (cursor.moveToFirst()) {
				int photoCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.PHOTO);
				String photoPath = cursor.getString(photoCol);

				byte[] photo = null;
				try {
					ImageHandler.FileReturn data = ImageHandler.readFromFile(photoPath);
					if (data.getBytesRead() == -1) Log.e(TAG, "No bytes read from file.");

					photo = data.getPhoto();

				} catch (IOException exception) {
					Toast.makeText(getParent(),
							"There was an error when reading photos.",
							Toast.LENGTH_LONG).show();
					Log.e(TAG, exception.toString());
				}

				photoBitmap = ImageHandler.bytesToBitmap(photo);
			} else {
				Toast.makeText(this, "This photo does not exist!",
						Toast.LENGTH_LONG).show();
			}
		} catch (SQLException exception) {
			Toast.makeText(this, "There was an error when loading the photo.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, exception.toString());
		}

		// Setting the image in the fragment
		ImageView bigPhoto = findViewById(R.id.big_photo);
		bigPhoto.setImageBitmap(photoBitmap);
	}
}
