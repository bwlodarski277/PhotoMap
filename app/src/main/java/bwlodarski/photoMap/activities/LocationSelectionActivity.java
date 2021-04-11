package bwlodarski.photoMap.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.icu.number.Precision;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
import java.util.List;

import bwlodarski.photoMap.BuildConfig;
import bwlodarski.photoMap.R;

public class LocationSelectionActivity extends AppCompatActivity implements OnMapReadyCallback {

	private static final int LOCATION_PERMISSION = 555;
	private static final int DEFAULT_ZOOM = 15;
	MaterialButton locationButton;
	FusedLocationProviderClient client;
	TextInputEditText addressInput;
	Geocoder geocoder;
	private TextView latLongView;
	private GoogleMap map;
	private Marker selectionMarker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_selection);

		locationButton = findViewById(R.id.gps);
		addressInput = findViewById(R.id.address);
		latLongView = findViewById(R.id.lat_long_view);

		Places.initialize(getApplicationContext(), BuildConfig.MAPS_API_KEY);
		Places.createClient(this);

		client = LocationServices.getFusedLocationProviderClient(this);

		geocoder = new Geocoder(getApplicationContext());

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		assert mapFragment != null;

		mapFragment.getMapAsync(this);

		locationButton.setOnClickListener(v -> requestLocationPerm());
	}

	private void requestLocationPerm() {
		String[] permissions = {
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION
		};
		ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION);
	}

	private void setPinLocation() {
		if (map != null && client != null
				&& ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_COARSE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			client.getLastLocation().addOnCompleteListener(task -> {
				Location location = task.getResult();
				if (location != null) {
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
	public void onRequestPermissionsResult(int requestCode,
	                                       @NonNull String[] permissions,
	                                       @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == LOCATION_PERMISSION) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED
					&& grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				client = LocationServices.getFusedLocationProviderClient(this);
				setPinLocation();
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
				updateAddressUsingMarker(marker);
				setLatLongUsingMarker(marker);
			}
		});
	}

	private void _setLatLong(Marker marker) {
		LatLng latLng = marker.getPosition();
		String newString = latLng.latitude + ", " + latLng.longitude;
		latLongView.setText(newString);
	}

	private void setLatLong() {
		_setLatLong(selectionMarker);
	}

	private void setLatLongUsingMarker(Marker marker) {
		_setLatLong(marker);
	}

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

	private void updateAddress() {
		_updateAddress(selectionMarker);
	}

	private void updateAddressUsingMarker(Marker marker) {
		_updateAddress(marker);
	}
}