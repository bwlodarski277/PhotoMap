package bwlodarski.courseworkapplication.Activities;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import bwlodarski.courseworkapplication.Fragments.PhotoGridFragment;
import bwlodarski.courseworkapplication.Fragments.PhotoMultiViewFragment;
import bwlodarski.courseworkapplication.R;

public class PhotoViewActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_view);
		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle("Your photos");
		setSupportActionBar(toolbar);

		int orientation = getResources().getConfiguration().orientation;

		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			// Replacing `photo_view` with the photo grid fragment
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.photo_view, new PhotoGridFragment()).commit();
		} else {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.photo_view, new PhotoMultiViewFragment()).commit();
		}

		FloatingActionButton fab = findViewById(R.id.addPhoto);
		fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show());

	}


}