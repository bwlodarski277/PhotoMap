package bwlodarski.photoMap.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;

import java.io.IOException;
import java.util.List;

import bwlodarski.photoMap.BuildConfig;
import bwlodarski.photoMap.R;
import bwlodarski.photoMap.helpers.DatabaseHandler;
import bwlodarski.photoMap.helpers.ImageHandler;

/**
 * Photo Details Fragment
 * Responsible for displaying the details of a single photo.
 */
public class PhotoDetailsFragment extends Fragment {

	private static final String TAG = "PhotoDetailsFragment";
	SQLiteDatabase db;
	LinearLayout details;
	ImageView bigPhoto;
	AppCompatTextView locationView;
	AppCompatTextView temperatureView;
	AppCompatTextView lightView;
	FusedLocationProviderClient client;
	Geocoder geocoder;

	public PhotoDetailsFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		db = new DatabaseHandler(requireContext().getApplicationContext()).getReadableDatabase();

		// Initialising Places API
		Context context = requireActivity().getApplicationContext();
		Places.initialize(context, BuildConfig.MAPS_API_KEY);
		Places.createClient(context);

		// Initialising location sensor
		client = LocationServices.getFusedLocationProviderClient(context);

		// Initialising GeoCoding API
		geocoder = new Geocoder(context);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_details, container, false);
		bigPhoto = view.findViewById(R.id.big_photo);
		locationView = view.findViewById(R.id.location);
		temperatureView = view.findViewById(R.id.temperature);
		lightView = view.findViewById(R.id.light);
		details = view.findViewById(R.id.photo_details);
		details.setVisibility(View.GONE);
		return view;
	}

	/**
	 * Setting the fragment data based on an image ID from the database.
	 * @param imageId image ID to fetch data by.
	 */
	public void setDetails(int imageId) {

		details.setVisibility(View.VISIBLE);
		String selection = String.format("%s = ?", DatabaseHandler.Photos.KEY);
		String[] args = {String.valueOf(imageId)};

		// Querying the photos table for image with ID passed to the method
		try (Cursor cursor = db.query(DatabaseHandler.Photos.TABLE, null,
				selection, args, null, null, null)) {
			if (cursor.moveToFirst()) {
				// Getting latitude and longitude
				int latCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.LAT);
				float lat = cursor.getFloat(latCol);
				int lonCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.LON);
				float lon = cursor.getFloat(lonCol);

				// Getting photo location from lat-long
				List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
				if (addresses.size() > 0) {
					String addressString = addresses.get(0).getAddressLine(0);
					locationView.setText(addressString);
				} else {
					locationView.setText("");
				}

				// Getting temperature
				int tempCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.TEMP);
				String temperature = cursor.getFloat(tempCol) + " °C";

				temperatureView.setText(temperature);

				// Getting light reading
				int lightCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.LIGHT);
				String light = cursor.getFloat(lightCol) + " lx";

				lightView.setText(light);

				// Getting the photo path in the device
				int photoCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.PHOTO);
				String photoPath = cursor.getString(photoCol);

				// Getting photo from device
				ImageHandler.FileReturn data = ImageHandler.readFromFile(photoPath);
				if (data.getBytesRead() == -1) Log.e(TAG, "No bytes read from file.");

				byte[] photo = data.getPhoto();
				Bitmap photoBitmap = ImageHandler.bytesToBitmap(photo);

				// Setting the image in the fragment
				bigPhoto.setImageBitmap(photoBitmap);

			} else {
				Toast.makeText(getContext(), "This photo does not exist!",
						Toast.LENGTH_LONG).show();
			}
		} catch (SQLException | IOException exception) {
			Toast.makeText(getContext(), "There was an error when loading the photo.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, exception.toString());
		}
	}


}
