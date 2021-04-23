package bwlodarski.photoMap.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import bwlodarski.photoMap.BuildConfig;
import bwlodarski.photoMap.R;

/**
 * Location selection activity
 * Used for setting the location of where a photo was taken.
 */
public class LocationSelectionActivity
		extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

	private static final int LOCATION_PERMISSION = 555;
	private static final int DEFAULT_ZOOM = 15;
	private static final String TAG = "LocationSelectActivity";
	private SensorManager sensorManager;
	private FusedLocationProviderClient client;
	private TextInputEditText addressInput;
	private Geocoder geocoder;
	private TextView latLongView;
	private GoogleMap map;
	private Marker selectionMarker;
	private float temperatureVal;
	private float lightVal;

	public static String _setLatLong(Marker marker) {
		if (marker == null) {
			return "0.0, 0.0";
		}
		LatLng latLng = marker.getPosition();
		return latLng.latitude + ", " + latLng.longitude;
	}

	@Override
	protected void onStop() {
		sensorManager.unregisterListener(this);
		super.onStop();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// Reading ambient and light sensors
		if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
			temperatureVal = event.values[0];
		} else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			lightVal = event.values[0];
		} else {
			Log.w(TAG, "Invalid sensor");
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_selection);

		MaterialButton locationButton = findViewById(R.id.gps);
		MaterialButton updateButton = findViewById(R.id.update_address);
		addressInput = findViewById(R.id.address);
		latLongView = findViewById(R.id.lat_long_view);

		// Setting up sensor reading
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		// setting up listener for light sensor
		if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
			Sensor light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			Toast.makeText(this, "No light sensor", Toast.LENGTH_SHORT).show();
		}

		// Setting up listener for temperature sensor
		if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
			Sensor temperature = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
			sensorManager.registerListener(this, temperature, SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			Toast.makeText(this, "No temperature sensor", Toast.LENGTH_SHORT).show();
		}

		Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
		Places.createClient(this);

		// Setting up location services
		client = LocationServices.getFusedLocationProviderClient(this);

		// Setting up GeoCoder for converting addresses to coordinates and vice versa
		geocoder = new Geocoder(getApplicationContext());

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		assert mapFragment != null;

		mapFragment.getMapAsync(this);

		// Get location permission access
		locationButton.setOnClickListener(v -> requestLocationPerm());

		// Set marker position using address typed in by the user
		updateButton.setOnClickListener(v -> {
			if (map != null && client != null) {
				try {
					// Getting the address the user typed in
					Editable addressEditable = addressInput.getText();
					if (addressEditable != null) {
						// Use the address to get a full address closest to what user typed in
						String addressString = addressEditable.toString();
						Address address = geocoder.getFromLocationName(addressString, 1).get(0);
						if (address != null) {
							// Get latitude and longitude from the address
							LatLng newPos = new LatLng(address.getLatitude(), address.getLongitude());
							// Set the marker location
							selectionMarker.setPosition(newPos);
							updateAddress();
							map.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, DEFAULT_ZOOM));
						}
					}
				} catch (IOException exception) {
					Log.e(TAG, exception.toString());
				}
			}
		});

		MaterialButton finishButton = findViewById(R.id.set_location);
		finishButton.setOnClickListener(v -> {
			// Getting ID passed to this activity
			Intent previous = getIntent();
			long photoId = previous.getLongExtra("ID", -1);
			// Creating new Intent
			Intent photoView = new Intent(getApplicationContext(), PhotoViewActivity.class);
			photoView.putExtra("ID", photoId);
			photoView.putExtra("TEMP", temperatureVal);
			photoView.putExtra("LIGHT", lightVal);
			// Putting LatLng into bundle
			LatLng finalLoc = selectionMarker.getPosition();
			Bundle bundle = new Bundle();
			bundle.putParcelable("LatLng", finalLoc);
			// Putting bundle into Intent and finishing this activity
			photoView.putExtra("LatLng", bundle);
			setResult(Activity.RESULT_OK, photoView);
			finish();
		});
	}

	/**
	 * Requesting location permission (fine and coarse)
	 */
	private void requestLocationPerm() {
		String[] permissions = {
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION
		};
		// Once permission is allowed, onRequestPermissionResult is called
		ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION);
	}

	/**
	 * Sets the pin location based on the user's last known location
	 */
	private void setPinLocation() {
		// Making sure the map is set up and location client is set up
		if (map != null && client != null
				&& ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_COARSE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			// Getting last known location
			client.getLastLocation().addOnCompleteListener(task -> {
				Location location = task.getResult();
				if (location != null) {
					// Getting latitude and longitude from location
					LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
					selectionMarker.setPosition(latLng);
					updateAddress();
					setLatLong();
				}
			});
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == LOCATION_PERMISSION) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED
					&& grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				// Once location permission is granted, set up the location client
				client = LocationServices.getFusedLocationProviderClient(this);
				// Set pin location using last known location
				setPinLocation();
				// Get pin location's address
				updateAddress();
			} else {
				View view = findViewById(R.id.location_root);
				Snackbar.make(view, "Permission not granted", Snackbar.LENGTH_LONG)
						.setAction("Retry", v -> requestLocationPerm()).show();
			}
		}
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
		map = googleMap;

		// Add a marker in Sydney and move the camera
		LatLng defaultLoc = new LatLng(52.3, 1);
		selectionMarker = map.addMarker(
				new MarkerOptions().position(defaultLoc).title("New photo location"));
		selectionMarker.setDraggable(true);
		map.moveCamera(CameraUpdateFactory.newLatLng(defaultLoc));

		if (ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_COARSE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			// If permission is granted, set pin location to user's last known location
			setPinLocation();
		}

		map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
			@Override
			public void onMarkerDragStart(Marker marker) {
			}

			@Override
			public void onMarkerDrag(Marker marker) {
			}

			@Override
			public void onMarkerDragEnd(Marker marker) {
				// When the marker is dragged, update the address displayed
				updateAddressUsingMarker(marker);
				setLatLongUsingMarker(marker);
			}
		});
	}

	/**
	 * Set latitude and longitude using class' marker position
	 * Used when we explicitly know what marker object to use.
	 */
	private void setLatLong() {
		latLongView.setText(_setLatLong(selectionMarker));
	}

	/**
	 * Set latitude and longitude using marker passed to the method
	 * Used in onMarkerDragEnd, so it takes in the marker object passed from the listener.
	 *
	 * @param marker marker passed from setOnMarkerDragListener
	 */
	private void setLatLongUsingMarker(Marker marker) {
		latLongView.setText(_setLatLong(marker));
	}

	/**
	 * Update the address displayed based on the position of a map marker
	 * @param marker map marker to get location from
	 */
	private void _updateAddress(Marker marker) {
		try {
			LatLng markerPos = marker.getPosition();
			Address address = geocoder.getFromLocation(
					markerPos.latitude, markerPos.longitude, 1).get(0);

			String fullAddress = address.getAddressLine(0);
			addressInput.setText(fullAddress);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Update address using class' selection marker.
	 * Used when we know explicitly what object to use.
	 */
	private void updateAddress() {
		_updateAddress(selectionMarker);
	}

	/**
	 * Update address using class' selection marker.
	 * Used in onMarkerDragEnd listener, which passes the marker that was dragged.
	 * @param marker marker passed from listener
	 */
	private void updateAddressUsingMarker(Marker marker) {
		_updateAddress(marker);
	}

}