package bwlodarski.courseworkapplication.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import bwlodarski.courseworkapplication.Helpers.ImageHandler;
import bwlodarski.courseworkapplication.R;

public class PhotoDetailsActivity extends FragmentActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_details);

		Intent intent = getIntent();
		String image = intent.getStringExtra("photo");

		Bitmap imgBitmap = ImageHandler.getBitmap(image);

		ImageView bigPhoto = findViewById(R.id.big_photo);
		bigPhoto.setImageBitmap(imgBitmap);
	}
}
