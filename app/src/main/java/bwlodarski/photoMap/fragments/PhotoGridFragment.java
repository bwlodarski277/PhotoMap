package bwlodarski.photoMap.fragments;

import android.content.Intent;
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
import bwlodarski.photoMap.models.UserPrefs;

public class PhotoGridFragment extends Fragment {

	private static final String TAG = "PhotoGridFragment";
	private final ArrayList<Photo> photos = new ArrayList<>();
	public PhotoAdapter adapter;
	private RecyclerView recyclerView;
	private TextView hintText;
	private int userId;

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
		View view = inflater.inflate(R.layout.fragment_photo_grid, container, false);
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
		int columns;
		// Changing the number of columns depending on the orientation
		if (orientation == Configuration.ORIENTATION_PORTRAIT) columns = 3;
		else columns = 2;
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

		try (Cursor cursor = db.rawQuery(rawQuery, queryParams)) {
			if (cursor.moveToFirst()) {
				do {
					int idCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.KEY);
					int photoCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.PHOTO);

					int id = cursor.getInt(idCol);
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
			adapter = new PhotoAdapter(getContext(), photos, db, handler);
			recyclerView.setAdapter(adapter);
		}
//
//		String[] cols = {DatabaseHandler.Photos.KEY, DatabaseHandler.Photos.PHOTO};
//
//
//		// Reading data from the database
//		//		int idCol, photoCol;
////		int id;
////		String photo;
//		// Loading photos from the database
//		try (Cursor cursor = db.query(DatabaseHandler.Photos.TABLE, cols,
//				null, null, null, null, null)) {
//			while (cursor.moveToNext()) {
//				int idCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.KEY);
//				int photoCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.PHOTO);
//
//				int id = cursor.getInt(idCol);
//				String photo = cursor.getString(photoCol);
//
//				photos.add(new Photo(id, photo));
//			}
//		} catch (SQLException exception) {
//			Toast.makeText(getContext(), "There was an error when loading photos.",
//					Toast.LENGTH_LONG).show();
//			Log.e(TAG, exception.toString());
//		}
//		// If there are no photos, disable the recycler view
//		if (photos.size() == 0) recyclerView.setVisibility(View.GONE);
//		else {
//			// Otherwise, fill the photo adapter and set the recycler view adapter
//			adapter = new PhotoAdapter(getContext(), photos, db, handler);
//			recyclerView.setAdapter(adapter);
//		}
	}
}
