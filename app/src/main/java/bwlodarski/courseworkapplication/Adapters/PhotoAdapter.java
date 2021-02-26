package bwlodarski.courseworkapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import bwlodarski.courseworkapplication.Models.Photo;
import bwlodarski.courseworkapplication.R;

public class PhotoAdapter extends ArrayAdapter<Photo> {

	ArrayList<Photo> photos;

	public PhotoAdapter(Context context, int resourceId, ArrayList<Photo> objects) {
		super(context, resourceId, objects);
		photos = objects;
	}

	@Override
	public int getCount() {
		return super.getCount();
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//		return super.getView(position, convertView, parent);
		View v = convertView;
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			v = inflater.inflate(R.layout.grid_photo, parent, false);
		ImageView image = v.findViewById(R.id.grid_photo);
		image.setImageResource(photos.get(position).getImage());

		return v;
	}
}
