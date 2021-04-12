package bwlodarski.photoMap.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.helpers.DatabaseHandler;
import bwlodarski.photoMap.helpers.ImageHandler;
import bwlodarski.photoMap.models.UserPrefs;

public class PhotoMapActivity extends AppCompatActivity implements OnMapReadyCallback {

	private static final String TAG = "PhotoMapActivity";
	SQLiteDatabase db;
	private String username;
	private int userId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_map);
		Toolbar toolbar = findViewById(R.id.toolbar);

		Intent intent = getIntent();
		username = intent.getStringExtra(UserPrefs.usernameKey);
		userId = intent.getIntExtra(UserPrefs.userIdKey, -1);

		db = new DatabaseHandler(getApplicationContext()).getReadableDatabase();

		toolbar.setTitle(String.format("%s's Photos", username));
		toolbar.setSubtitle("Photo Map");
		setSupportActionBar(toolbar);
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		assert mapFragment != null;
		mapFragment.getMapAsync(this);
	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {

//		// Add a marker in Sydney and move the camera
//		LatLng sydney = new LatLng(-34, 151);
//		googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//		googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

		String getPhotos = String.format(
				"SELECT * FROM %s WHERE %s IN (SELECT a.%s FROM %s AS a JOIN %s AS b ON a.%s = b.%s WHERE b.%s = ?)",
				DatabaseHandler.Photos.TABLE, DatabaseHandler.Photos.KEY,
				DatabaseHandler.Photos.KEY, DatabaseHandler.Photos.TABLE,
				DatabaseHandler.UserPhotos.TABLE, DatabaseHandler.Photos.KEY,
				DatabaseHandler.UserPhotos.PHOTO_KEY, DatabaseHandler.UserPhotos.USER_KEY);

		String[] whereArgs = {String.valueOf(userId)};

		List<Pair<Marker, Integer>> markers = new ArrayList<>();

		LatLng last = null;
		try (Cursor cursor = db.rawQuery(getPhotos, whereArgs)) {
			if (cursor.moveToFirst()) {
				do {
					int idCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.KEY);
					int photoCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.PHOTO);
					int latCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.LAT);
					int lonCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.LON);

					int id = cursor.getInt(idCol);
					String photoPath = cursor.getString(photoCol);
					long lat = cursor.getLong(latCol);
					long lon = cursor.getLong(lonCol);
					LatLng position = new LatLng(lat, lon);

					ImageHandler.FileReturn data = ImageHandler.readFromFile(photoPath);
					if (data.getBytesRead() == -1) Log.e(TAG, "No bytes read from file.");

					byte[] photo = data.getPhoto();

					Marker marker = googleMap.addMarker(new MarkerOptions()
							.position(position)
							.icon(ImageHandler.bytesToBitmapDescriptor(photo))
					);
					markers.add(new Pair<>(marker, id));

					last = position;

				} while (cursor.moveToNext());
			}
		} catch (SQLException exception) {
			Log.e(TAG, "Could not load photos", exception);
			Toast.makeText(this, "Could not load photos", Toast.LENGTH_SHORT).show();
		} catch (IOException exception) {
			Toast.makeText(this, "There was an error when reading photos.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, exception.toString());
		}
		if (last != null) {
			googleMap.moveCamera(CameraUpdateFactory.newLatLng(last));
		}

		Context context = this;
		googleMap.setOnMarkerClickListener(marker -> {
			for (Pair<Marker, Integer> m : markers) {
				if (m.first != null && m.second != null) {
					Marker current = m.first;
					int id = m.second;
					if (current.equals(marker)) {
						Intent photoDetails = new Intent(context, PhotoDetailsActivity.class);
						photoDetails.putExtra("photo", id);
						startActivity(photoDetails);
						return true;
					}
				}
			}
			return false;
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_map_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.menu_grid_button) {
			Intent photoViewIntent = new Intent(this, PhotoViewActivity.class);
			photoViewIntent.putExtra(UserPrefs.usernameKey, username);
			photoViewIntent.putExtra(UserPrefs.userIdKey, userId);
			startActivity(photoViewIntent);
			finish();
		} else if (itemId == R.id.menu_settings) {
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingsIntent);
		} else {
			Log.e(TAG, "Invalid menu item selected");
			return false;
		}
		return true;
	}
}