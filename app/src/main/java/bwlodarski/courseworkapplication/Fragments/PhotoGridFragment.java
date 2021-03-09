package bwlodarski.courseworkapplication.Fragments;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import bwlodarski.courseworkapplication.Activities.PhotoDetailsActivity;
import bwlodarski.courseworkapplication.Activities.PhotoViewActivity;
import bwlodarski.courseworkapplication.Adapters.PhotoAdapter;
import bwlodarski.courseworkapplication.Models.Photo;
import bwlodarski.courseworkapplication.R;

public class PhotoGridFragment extends Fragment {

	GridView photoGrid;
	ArrayList<Photo> photos = new ArrayList<>();

	public PhotoGridFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Inserting temporary photos
		{
			photos.add(new Photo(R.drawable.img_1));
			photos.add(new Photo(R.drawable.img_2));
			photos.add(new Photo(R.drawable.img_3));
			photos.add(new Photo(R.drawable.img_4));
			photos.add(new Photo(R.drawable.img_5));

			photos.add(new Photo(R.drawable.img_1));
			photos.add(new Photo(R.drawable.img_2));
			photos.add(new Photo(R.drawable.img_3));
			photos.add(new Photo(R.drawable.img_4));
			photos.add(new Photo(R.drawable.img_5));

			photos.add(new Photo(R.drawable.img_1));
			photos.add(new Photo(R.drawable.img_2));
			photos.add(new Photo(R.drawable.img_3));
			photos.add(new Photo(R.drawable.img_4));
			photos.add(new Photo(R.drawable.img_5));

			photos.add(new Photo(R.drawable.img_1));
			photos.add(new Photo(R.drawable.img_2));
			photos.add(new Photo(R.drawable.img_3));
			photos.add(new Photo(R.drawable.img_4));
			photos.add(new Photo(R.drawable.img_5));

			photos.add(new Photo(R.drawable.img_1));
			photos.add(new Photo(R.drawable.img_2));
			photos.add(new Photo(R.drawable.img_3));
			photos.add(new Photo(R.drawable.img_4));
			photos.add(new Photo(R.drawable.img_5));

			photos.add(new Photo(R.drawable.img_1));
			photos.add(new Photo(R.drawable.img_2));
			photos.add(new Photo(R.drawable.img_3));
			photos.add(new Photo(R.drawable.img_4));
			photos.add(new Photo(R.drawable.img_5));
		}
	}


	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_photo_grid, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		photoGrid = getActivity().findViewById(R.id.photo_grid);

		PhotoAdapter photoAdapter = new PhotoAdapter(getActivity(), R.layout.grid_photo, photos);
		photoGrid.setAdapter(photoAdapter);

		photoGrid.setOnItemClickListener((parent, view, position, id) -> {
			if (getActivity() instanceof PhotoViewActivity) {
				Intent intent = new Intent(getActivity(), PhotoDetailsActivity.class);
				intent.putExtra("photo", photos.get(position).getImage());
				startActivity(intent);
			}

			int orientation = getResources().getConfiguration().orientation;

			if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				// If orientation is portrait, display on new activity
				Intent intent = new Intent(getActivity(), PhotoDetailsActivity.class);
				intent.putExtra("photo", photos.get(position).getImage());
				startActivity(intent);
			} else {
				// If landscape, can be displayed on the same activity.
				
			}
			Toast.makeText(getActivity(), Integer.toString(position), Toast.LENGTH_SHORT).show();
		});
	}
}
