package bwlodarski.courseworkapplication.Activities;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import bwlodarski.courseworkapplication.Helpers.DatabaseHandler;
import bwlodarski.courseworkapplication.R;
import bwlodarski.courseworkapplication.Static.UserPrefs;

public class LoginActivity extends AppCompatActivity {

//	private static final String USER_PREF_FILE = "USER";
//	private static final String USER_ID = "ID_KEY";
//	private static final String USERNAME = "NAME_KEY";

	private static final String TAG = "LoginActivity";

	private EditText usernameEditText, passwordEditText;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		usernameEditText = findViewById(R.id.username);
		passwordEditText = findViewById(R.id.password);

		// Reading shared preferences
		preferences = getSharedPreferences(UserPrefs.userPrefFile, MODE_PRIVATE);

		// If the user is logged in, skip the login screen.
		if (preferences.contains(UserPrefs.userIdKey) &&
				preferences.contains(UserPrefs.usernameKey)) {
			sendToPhotoView();
		}
	}

	/**
	 * Sends the user to the photo view screen.
	 */
	public void sendToPhotoView() {
		// Creating a new intent to go to the photo photo view
		Intent photoView = new Intent(getApplicationContext(), PhotoViewActivity.class);
		// Reading the user ID and username from shared preferences
		int id = preferences.getInt(UserPrefs.userIdKey, 0);
		String username = preferences.getString(UserPrefs.usernameKey, "user");
		// Sending ID and username to photo view
		photoView.putExtra(UserPrefs.userIdKey, id);
		photoView.putExtra(UserPrefs.usernameKey, username);
		startActivity(photoView);
		finish(); // Making sure you can't go back to the login screen
	}

	/**
	 * Registers a new user.
	 *
	 * @param view view to read data from (login screen)
	 */
	public void register(View view) {
		String username = usernameEditText.getText().toString();
		String password = passwordEditText.getText().toString();

		Context context = getApplicationContext();

		// Checking the username and password length
		if (username.length() < 8 || password.length() < 8) {
			String msg = "Username and password must be at least 8 characters long.";
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			return;
		}

		DatabaseHandler handler = new DatabaseHandler(this);
		SQLiteDatabase db = handler.getWritableDatabase();

		String[] cols = {DatabaseHandler.Users.KEY};
		String selection = String.format("%s = ?", DatabaseHandler.Users.USERNAME);
		String[] args = {username};

		// Selecting user IDs where username and password match
		try (Cursor cursor = db.query(DatabaseHandler.Users.TABLE, cols, selection, args,
				null, null, null)) {
			if (!cursor.moveToFirst()) {
				// The username was not found in the database, so create the new user.
				ContentValues userData = new ContentValues();
				userData.put(DatabaseHandler.Users.USERNAME, username);
				userData.put(DatabaseHandler.Users.PASSWORD, password);

				// Adding the new user to the DB
				db.insertOrThrow(DatabaseHandler.Users.TABLE, null, userData);

				Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(this, "This username is taken.",
						Toast.LENGTH_SHORT).show();
			}
		} catch (SQLException exception) {
			Toast.makeText(this, "There was an error when registering.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, exception.toString());
		}

//		// Checking if the username is already taken
//		for (AppUser user : users) {
//			if (user.getUsername().equals(username)) {
//				String msg = "This username is taken";
//				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
//				return;
//			}
//		}
//
//		AppUser user = new AppUser(username, password);
//		users.add(user);
//
//		String msg = "Account created!";
//		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}

	/**
	 * Logs in a user.
	 *
	 * @param view view to read data from (login screen)
	 */
	public void login(View view) {
		String username = usernameEditText.getText().toString();
		String password = passwordEditText.getText().toString();

		Context context = getApplicationContext();
		// Checking the username and password length
		if (username.length() < 8 || password.length() < 8) {
			String msg = "Username and password must be at least 8 characters long.";
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			return;
		}

		DatabaseHandler handler = new DatabaseHandler(this);
		SQLiteDatabase db = handler.getWritableDatabase();

		// Finding user where username and password match
		String selection = String.format(
				"%s = ? AND %s = ?",
				DatabaseHandler.Users.USERNAME,
				DatabaseHandler.Users.PASSWORD);

//		// Arguments to use in the selection
		String[] args = {username, password};

		try (Cursor cursor = db.query(DatabaseHandler.Users.TABLE, null, selection,
				args, null, null, null)) {
			if (cursor.moveToFirst()) {
				// If cursor returns true, set the shared preferences to the user.

				int idCol = cursor.getColumnIndex(DatabaseHandler.Users.KEY);
				int usernameCol = cursor.getColumnIndex(DatabaseHandler.Users.USERNAME);

				int id = cursor.getInt(idCol);
				String _username = cursor.getString(usernameCol); // _ as the name is in use

				SharedPreferences.Editor editor = preferences.edit();
				editor.putInt(UserPrefs.userIdKey, id);
				editor.putString(UserPrefs.usernameKey, _username);
				editor.apply();

				Toast.makeText(this, "Successfully logged in!",
						Toast.LENGTH_SHORT).show();

				sendToPhotoView();
			} else {
				// This occurs when the username and password don't match
				// (We check the password in the query, so this will be empty if not matched)
				Toast.makeText(this, "Username or password incorrect.",
						Toast.LENGTH_LONG).show();
			}
		} catch (SQLException exception) {
			Toast.makeText(this, "There was an error when logging in.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, exception.toString());
		}

//		AppUser found = null;
//		for (AppUser user : users) {
//			assert user.getUsername() != null && user.getPassword() != null;
//			if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
//				found = user;
//				break;
//			}
//		}
//
//		String msg;
//		if (found != null) {
//			Intent photoView = new Intent(getApplicationContext(), PhotoViewActivity.class);
//			startActivity(photoView);
//			finish();
//		} else {
//			msg = "Invalid login";
//			Context context = getApplicationContext();
//			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
//		}


	}
}