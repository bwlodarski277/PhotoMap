package bwlodarski.photoMap.fragments;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.Objects;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.helpers.DatabaseHandler;
import bwlodarski.photoMap.helpers.ImageHandler;

/**
 * Responsible for displaying the details of a single photo.
 */
public class PhotoDetailsFragment extends Fragment {

	private static final String TAG = "PhotoDetailsFragment";
	SQLiteDatabase db;
	ImageView bigPhoto;

	public PhotoDetailsFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new DatabaseHandler(requireContext().getApplicationContext()).getReadableDatabase();
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_details, container, false);
		bigPhoto = view.findViewById(R.id.big_photo);
		return view;
	}

	public void setDetails(int imageId) {
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
					Toast.makeText(getContext(),
							"There was an error when reading photos.",
							Toast.LENGTH_LONG).show();
					Log.e(TAG, exception.toString());
				}

				photoBitmap = ImageHandler.bytesToBitmap(photo);
			} else {
				Toast.makeText(getContext(), "This photo does not exist!",
						Toast.LENGTH_LONG).show();
			}
		} catch (SQLException exception) {
			Toast.makeText(getContext(), "There was an error when loading the photo.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, exception.toString());
		}

		// Setting the image in the fragment
		bigPhoto.setImageBitmap(photoBitmap);
	}


}
