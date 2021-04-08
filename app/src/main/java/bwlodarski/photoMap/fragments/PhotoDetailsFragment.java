package bwlodarski.photoMap.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import bwlodarski.photoMap.R;

/**
 * Responsible for displaying the details of a single photo.
 */
public class PhotoDetailsFragment extends Fragment {

	public PhotoDetailsFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_photo_grid, container, false);
	}
}
