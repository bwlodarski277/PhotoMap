package bwlodarski.photoMap.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.adapters.PhotoAdapter;
import bwlodarski.photoMap.helpers.DatabaseHandler;
import bwlodarski.photoMap.helpers.ImageHandler;
import bwlodarski.photoMap.models.Photo;
import bwlodarski.photoMap.models.SettingsPrefs;
import bwlodarski.photoMap.models.UserPrefs;

public class PhotoGridFragment extends Fragment {

	private static final String TAG = "PhotoGridFragment";
	private final ArrayList<Photo> photos = new ArrayList<>();
	public PhotoAdapter adapter;
	private RecyclerView recyclerView;
	private TextView hintText;
	private int userId;
	PhotoDetailsFragment details;

	// Default constructor
	public PhotoGridFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Reading user ID and username
		Intent intent = requireActivity().getIntent();
		userId = intent.getIntExtra(UserPrefs.userIdKey, -1);
	}


	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View view;
		// Determining what to do based on the orientation.
		int orientation = getResources().getConfiguration().orientation;
		// If portrait, we will start a new activity to display photo details
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			view = inflater.inflate(R.layout.fragment_photo_grid, container, false);
		} else {
			// If landscape, the photo details fragment is in the same activity, side by side.
			// So we will be calling a function in the detail fragment.
			view = inflater.inflate(R.layout.fragment_photo_multi_view, container, false);
			details = (PhotoDetailsFragment) getChildFragmentManager().findFragmentById(R.id.detail_fragment);
		}
		recyclerView = view.findViewById(R.id.photo_grid);
		hintText = view.findViewById(R.id.hint_text);
		fillGrid();
		return view;
	}

	/**
	 * Fills the photo grid.
	 * This method reads the photos from the database and fills the grid.
	 */
	private void fillGrid() {
		int orientation = getResources().getConfiguration().orientation;

		SharedPreferences settings = requireActivity()
				.getSharedPreferences(SettingsPrefs.settingsPrefFile, Context.MODE_PRIVATE);
		int columns = settings.getInt("COLS", 3);

		recyclerView.setLayoutManager(new GridLayoutManager(getContext(), columns));

		DatabaseHandler handler = new DatabaseHandler(getContext());
		SQLiteDatabase db = handler.getReadableDatabase();

		// Raw query to get all the photos associated with a user by the user's ID
		String rawQuery = String.format(
				"SELECT * FROM %s AS a JOIN %s AS b ON a.%s = b.%s WHERE b.%s = ?",
				DatabaseHandler.Photos.TABLE, DatabaseHandler.UserPhotos.TABLE,
				DatabaseHandler.Photos.KEY, DatabaseHandler.UserPhotos.PHOTO_KEY,
				DatabaseHandler.UserPhotos.USER_KEY);

		String[] queryParams = {String.valueOf(userId)};

		// Getting all the user's photos
		try (Cursor cursor = db.rawQuery(rawQuery, queryParams)) {
			if (cursor.moveToFirst()) {
				do {
					int idCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.KEY);
					int photoCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.PHOTO);

					int id = cursor.getInt(idCol);
					String photoPath = cursor.getString(photoCol);

					byte[] photo = null;
					try {
						// Reading photo from the DB
						ImageHandler.FileReturn data = ImageHandler.readFromFile(photoPath);
						if (data.getBytesRead() == -1) Log.e(TAG, "No bytes read from file.");

						photo = data.getPhoto();
					} catch (IOException exception) {
						Toast.makeText(getContext(),
								"There was an error when reading photos.",
								Toast.LENGTH_LONG).show();
						Log.e(TAG, exception.toString());
					}
					// Adding the user's photo to the grid
					photos.add(new Photo(id, photo, photoPath));
				} while (cursor.moveToNext());
			}
		} catch (SQLException exception) {
			Toast.makeText(getContext(), "There was an error when loading photos.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, exception.toString());
		}

		if (photos.size() == 0) {
			recyclerView.setVisibility(View.GONE);
			hintText.setVisibility(View.VISIBLE);
		} else {
			// Otherwise, fill the photo adapter and set the recycler view adapter
			if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				// If portrait, start a new activity to view photo details
				adapter = new PhotoAdapter(getActivity(), photos, db, handler);
			} else {
				// If landscape, photo details fragment is in the same activity, so
				// we want to show the photo details in that.
				adapter = new PhotoAdapter(getActivity(), photos, db, handler, details);
			}
			recyclerView.setAdapter(adapter);
		}
	}
}
