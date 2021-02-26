package bwlodarski.courseworkapplication.Activities;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.GridView;

import java.util.ArrayList;

import bwlodarski.courseworkapplication.Adapters.PhotoAdapter;
import bwlodarski.courseworkapplication.Models.Photo;
import bwlodarski.courseworkapplication.R;

public class PhotoViewActivity extends AppCompatActivity {


	GridView photoGrid;
	ArrayList<Photo> photos = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_view);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = findViewById(R.id.addPhoto);
		fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show());

		photoGrid = findViewById(R.id.photo_grid);
		photos.add(new Photo(R.drawable.img_1));
		photos.add(new Photo(R.drawable.img_2));
		photos.add(new Photo(R.drawable.img_3));
		photos.add(new Photo(R.drawable.img_4));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));
		photos.add(new Photo(R.drawable.img_5));


		PhotoAdapter photoAdapter = new PhotoAdapter(this, R.layout.grid_photo, photos);
		photoGrid.setAdapter(photoAdapter);
	}
}