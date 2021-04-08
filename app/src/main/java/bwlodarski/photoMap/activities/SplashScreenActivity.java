package bwlodarski.photoMap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import bwlodarski.photoMap.R;

public class SplashScreenActivity extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		setTheme(R.style.splash_screen);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);

		Handler goToLogin = new Handler();
		goToLogin.postDelayed(() -> {
			Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(loginIntent);
			finish();
		}, 1200);
	}
}
