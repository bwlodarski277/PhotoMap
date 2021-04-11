package bwlodarski.photoMap.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.helpers.DatabaseHandler;
import bwlodarski.photoMap.models.SettingsPrefs;
import bwlodarski.photoMap.models.UserPrefs;

public class SettingsActivity extends AppCompatActivity {

	private static final String TAG = "SettingsActivity";

	TextView loggedInAs;
	SeekBar colsBar;
	MaterialButton logout;
	MaterialButton deleteAcc;
	SQLiteDatabase db;
	FirebaseUser user;
	FirebaseAuth auth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		auth = FirebaseAuth.getInstance();
		user = auth.getCurrentUser();

		db = new DatabaseHandler(getApplicationContext()).getWritableDatabase();

		SharedPreferences preferences = getSharedPreferences(UserPrefs.userPrefFile, MODE_PRIVATE);
		String username = preferences.getString(UserPrefs.usernameKey, "");
		int userId = preferences.getInt(UserPrefs.userIdKey, -1);

		loggedInAs = findViewById(R.id.logged_in_as);
		String newText = "Logged in as " + username;
		loggedInAs.setText(newText);

		logout = findViewById(R.id.settings_logout);
		logout.setOnClickListener(v ->
				new AlertDialog.Builder(this)
						.setTitle("Log out")
						.setMessage("Are you sure you want to log out?")
						.setPositiveButton("Yes", (dialog, which) -> {
							clearPrefs();
							auth.signOut();
							logout(this);
						})
						.setNegativeButton("No", null)
						.show());

		deleteAcc = findViewById(R.id.settings_delete_acc);
		deleteAcc.setOnClickListener(v ->
				new AlertDialog.Builder(this)
						.setTitle("Delete account")
						.setMessage("Are you sure you want to delete your account?")
						.setPositiveButton("Yes", ((dialog, which) ->
								new AlertDialog.Builder(this)
										.setTitle("Are you sure?")
										.setMessage("This action cannot be undone!")
										.setPositiveButton("Yes", ((dialog1, which1) -> {
											clearPrefs();
											cascadeDelete(userId);
											user.delete();
											auth.signOut();
											logout(this);
										}))
										.setNegativeButton("No", null)
										.show()))
						.setNegativeButton("No", null)
						.show());

		SharedPreferences settings = getSharedPreferences(
				SettingsPrefs.settingsPrefFile, Context.MODE_PRIVATE);
		int cols = settings.getInt("COLS", 3);
		colsBar = findViewById(R.id.grid_size);
		colsBar.setProgress(cols - 1);

		colsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					settings.edit().putInt("COLS", progress + 1).apply();
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	private void clearPrefs() {
		SharedPreferences preferences =
				getSharedPreferences(UserPrefs.userPrefFile, MODE_PRIVATE);
		preferences.edit().clear().apply();
	}

	private void logout(Context context) {
		Intent loginIntent = new Intent(context, LoginActivity.class);
		startActivity(loginIntent);
		finish();
	}

	@SuppressLint("Recycle")
	private void cascadeDelete(int userId) {
//		String delPhotos = "DELETE FROM %s AS a JOIN %s AS b ON a.%s = b.%s WHERE b.%s = ?";
		String delPhotos = "DELETE FROM " + DatabaseHandler.Photos.TABLE +
				" WHERE " + DatabaseHandler.Photos.KEY + " IN (SELECT a." +
				DatabaseHandler.Photos.KEY + " FROM " + DatabaseHandler.Photos.TABLE +
				" AS a JOIN " + DatabaseHandler.UserPhotos.TABLE + " AS b ON a." +
				DatabaseHandler.Photos.KEY + " = b." + DatabaseHandler.UserPhotos.PHOTO_KEY +
				" WHERE b." + DatabaseHandler.UserPhotos.USER_KEY + " = ?)";
		String[] whereArgs = {String.valueOf(userId)};

		String delUserPhotos = String.format("%s = ?", DatabaseHandler.UserPhotos.USER_KEY);
		String delUser = String.format("%s = ?", DatabaseHandler.Users.KEY);
		try {
			db.rawQuery(delPhotos, whereArgs);
			db.delete(DatabaseHandler.UserPhotos.TABLE, delUserPhotos, whereArgs);
			db.delete(DatabaseHandler.Users.TABLE, delUser, whereArgs);
		} catch (SQLException exception) {
			Toast.makeText(this, "There was an error when deleting the account.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, exception.toString());
		}
	}
}