package bwlodarski.photoMap.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.helpers.DatabaseHandler;
import bwlodarski.photoMap.models.SettingsPrefs;
import bwlodarski.photoMap.models.UserPrefs;

/**
 * Settings activity
 * Used for modifying various settings within the application.
 */
public class SettingsActivity extends AppCompatActivity {

	private static final String TAG = "SettingsActivity";

	TextView loggedInAs;
	SeekBar colsBar;
	MaterialButton logout;
	MaterialButton deleteAcc;
	SQLiteDatabase db;
	FirebaseUser user;
	FirebaseAuth auth;
	int userId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		// Setting up firebase
		auth = FirebaseAuth.getInstance();
		user = auth.getCurrentUser();

		// Getting DB handler
		db = new DatabaseHandler(getApplicationContext()).getWritableDatabase();

		// Getting the user's shared preferences
		SharedPreferences preferences = getSharedPreferences(UserPrefs.userPrefFile, MODE_PRIVATE);
		String username = preferences.getString(UserPrefs.usernameKey, "");
		userId = preferences.getInt(UserPrefs.userIdKey, -1);

		// Setting "logged in as" text
		loggedInAs = findViewById(R.id.logged_in_as);
		String newText = "Logged in as " + username;
		loggedInAs.setText(newText);

		// When the user clicks the logout button, sign them out and send them to login screen
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

		// When the user clicks the delete account button, ask them to confirm the deletion.
		// Once it is confirmed, delete the user data from Firebase and then locally.
		deleteAcc = findViewById(R.id.settings_delete_acc);
		deleteAcc.setOnClickListener(v ->
				new AlertDialog.Builder(this)
						.setTitle("Delete account")
						.setMessage("Are you sure you want to delete your account?")
						.setPositiveButton("Yes", ((dialog, which) ->
								new AlertDialog.Builder(this)
										.setTitle("Are you sure?")
										.setMessage("This action cannot be undone!")
										.setPositiveButton("Yes", ((dialog1, which1) ->
												user.delete()
														.addOnCompleteListener(task ->
																deleteUser(userId, task)
														))
										)
										.setNegativeButton("No", null)
										.show()))
						.setNegativeButton("No", null)
						.show());

		SharedPreferences settings = getSharedPreferences(
				SettingsPrefs.settingsPrefFile, Context.MODE_PRIVATE);
		int cols = settings.getInt("COLS", 3);
		colsBar = findViewById(R.id.grid_size);
		colsBar.setProgress(cols - 1);

		// When the user changes the columns progress bar (which decides how many columns grid has)
		colsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					// Setting the columns shared preference
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

	/**
	 * Deletes the user locally.
	 * @param userId ID of user to delete from the database.
	 * @param task user deletion task passed from Firebase
	 */
	private void deleteUser(int userId, Task<Void> task) {
		// If user was deleted, remove local data.
		if (task.isSuccessful()) {
			clearPrefs();
			deleteUserPhotos(userId);
			cascadeDelete(userId);
			auth.signOut();
			Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
			logout(this);
		} else {
			// Happens when password hasn't been type in a while.
			// Ask the user to re-enter the password and try deleting the account again.
			askForCredentials();
		}
	}

	/**
	 * If the user has not logged in for a while, ask them to re-enter their password.
	 * Without this, Firebase will not delete the account.
	 */
	private void askForCredentials() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Enter password");
		EditText password = new EditText(this);
		password.setHint("password");
		password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		builder.setView(password);
		builder.setPositiveButton("Confirm", (dialog, which) -> {
			String passwordStr = password.getText().toString();
			String email = user.getEmail();
			assert email != null;
			// Re-verify the user
			AuthCredential credential = EmailAuthProvider.getCredential(email, passwordStr);
			// Once verified (or not), try the deleteUser function again to delete local data.
			user.reauthenticate(credential).addOnCompleteListener(task -> deleteUser(userId, task));
		});
		builder.create().show();
	}

	/**
	 * Deletes a given user's photos from the database.
	 * @param userId ID of the user to delete photos for
	 */
	private void deleteUserPhotos(int userId) {
		// Selecting all of the user's photos using the link table that
		// joins the users and photos table.
		String getPhotos = String.format(
				"SELECT * FROM %s WHERE %s IN (SELECT a.%s FROM %s AS a JOIN %s AS b ON a.%s = b.%s WHERE b.%s = ?)",
				DatabaseHandler.Photos.TABLE, DatabaseHandler.Photos.KEY,
				DatabaseHandler.Photos.KEY, DatabaseHandler.Photos.TABLE,
				DatabaseHandler.UserPhotos.TABLE, DatabaseHandler.Photos.KEY,
				DatabaseHandler.UserPhotos.PHOTO_KEY, DatabaseHandler.UserPhotos.USER_KEY);

		String[] whereArgs = {String.valueOf(userId)};

		// For each photo in the DB, delete the local file associated with it.
		try (Cursor cursor = db.rawQuery(getPhotos, whereArgs)) {
			if (cursor.moveToFirst()) {
				do {
					int photoCol = cursor.getColumnIndexOrThrow(DatabaseHandler.Photos.PHOTO);
					String photoPath = cursor.getString(photoCol);

					File image = new File(photoPath);
					boolean deleted = image.delete();
					if (!deleted) {
						Log.w(TAG, "A photo was not deleted: " + photoPath);
					}

				} while (cursor.moveToNext());
			}
		} catch (SQLException exception) {
			Toast.makeText(this, "There was an error when deleting the account.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, exception.toString());
		}
	}

	/**
	 * Clears the user's shared preferences.
	 */
	private void clearPrefs() {
		SharedPreferences preferences =
				getSharedPreferences(UserPrefs.userPrefFile, MODE_PRIVATE);
		preferences.edit().clear().apply();
	}

	/**
	 * Logs out the user
	 * @param context context in which the function was called
	 */
	private void logout(Context context) {
		// Send user to login screen
		Intent loginIntent = new Intent(context, LoginActivity.class);
		startActivity(loginIntent);
		finish();
	}

	/**
	 * Deleting all the data associated with the user.
	 * @param userId ID of the user to remove from the database.
	 */
	@SuppressLint("Recycle") // We don't need the output of the raw query or delete queries.
	private void cascadeDelete(int userId) {
		// Deleting the user's photos by getting a list of their photos from the link table
		String delPhotos = String.format(
				"DELETE FROM %s WHERE %s IN (SELECT a.%s FROM %s AS a JOIN %s AS b ON a.%s = b.%s WHERE b.%s = ?)",
				DatabaseHandler.Photos.TABLE, DatabaseHandler.Photos.KEY,
				DatabaseHandler.Photos.KEY, DatabaseHandler.Photos.TABLE,
				DatabaseHandler.UserPhotos.TABLE, DatabaseHandler.Photos.KEY,
				DatabaseHandler.UserPhotos.PHOTO_KEY, DatabaseHandler.UserPhotos.USER_KEY);

		String[] whereArgs = {String.valueOf(userId)};

		// Deleting userPhotos links
		String delUserPhotos = String.format("%s = ?", DatabaseHandler.UserPhotos.USER_KEY);

		// Deleting user record
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