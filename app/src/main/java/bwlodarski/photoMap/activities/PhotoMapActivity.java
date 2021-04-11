package bwlodarski.photoMap.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.models.UserPrefs;

public class PhotoMapActivity extends AppCompatActivity implements OnMapReadyCallback {

	private static final String TAG = "PhotoMapActivity";

	private GoogleMap mMap;
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
		mMap = googleMap;

		// Add a marker in Sydney and move the camera
		LatLng sydney = new LatLng(-34, 151);
		mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
		mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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
		}
		else {
			Log.e(TAG, "Invalid menu item selected");
			return false;
		}
		return true;
	}
}