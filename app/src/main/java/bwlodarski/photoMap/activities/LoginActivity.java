package bwlodarski.photoMap.activities;

import android.app.Activity;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.helpers.DatabaseHandler;
import bwlodarski.photoMap.models.UserPrefs;

/**
 * Login Activity.
 * Allows users to log in or be taken to the registration screen.
 * Interacts with the database (Users table)
 *
 * @see DatabaseHandler
 */
public class LoginActivity extends AppCompatActivity {

	public static final int minUserLength = 6;
	public static final int minPassLength = 8;
	public static final int REGISTER = 123;
	private static final String TAG = "LoginActivity";
	private EditText emailEditText, passwordEditText;
	private SharedPreferences preferences;
	private FirebaseAuth auth;

	/**
	 * Checks whether an email and password are strong enough.
	 *
	 * @param email    email address to check
	 * @param password password to check
	 * @return integer, indicating whether the email and password are strong enough
	 */
	public static int checkEmailAndPassword(String email, String password) {
		// Checking the email and password length
		if (email.length() < minUserLength) {
			return -1; // Indicating that the email is too short
		}

		if (password.length() < minPassLength) {
			return -2; // Indicating that the password is too short
		}
		return 0; // Indicating that both fields are OK
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		emailEditText = findViewById(R.id.email);
		passwordEditText = findViewById(R.id.password);

		// Reading shared preferences
		preferences = getSharedPreferences(UserPrefs.userPrefFile, MODE_PRIVATE);

		// If the user is logged in, skip the login screen.
//		if (preferences.contains(UserPrefs.userIdKey) &&
//				preferences.contains(UserPrefs.usernameKey)) {
//			sendToPhotoView();
//		}

		FirebaseApp.initializeApp(this);
		auth = FirebaseAuth.getInstance();

		FirebaseUser user = auth.getCurrentUser();
		if (user != null) {
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
		String email = emailEditText.getText().toString();
		// Go to the registration screen and wait for a result
		Intent registerIntent = new Intent(view.getContext(), RegisterActivity.class);
		registerIntent.putExtra("EMAIL", email);
		startActivityForResult(registerIntent, REGISTER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REGISTER) {
				assert data != null;
				// Once user is registered, fill the fields in the login form
				emailEditText.setText(data.getStringExtra("EMAIL"));
				passwordEditText.setText(data.getStringExtra("PASS"));
			} else {
				Log.e(TAG, "Unknown request code");
				Toast.makeText(getBaseContext(), "Something went wrong!",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Logs in a user.
	 *
	 * @param view view to read data from (login screen)
	 */
	public void login(View view) {
		String email = emailEditText.getText().toString();
		String password = passwordEditText.getText().toString();

		Context context = getApplicationContext();
		int result = checkEmailAndPassword(email, password);
		if (result == -1) {
			String msg = String.format("Username needs to be at least %s long.", minUserLength);
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			return;
		} else if (result == -2) {
			String msg = String.format("Password needs to be at least %s long.", minPassLength);
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			return;
		}

		auth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(task -> {
					if (task.isSuccessful()) {
						FirebaseUser user = auth.getCurrentUser();
						assert user != null;
						if (user.isEmailVerified()) {
							getUserData(email);
							sendToPhotoView();
						} else {
							user.sendEmailVerification();
							Toast.makeText(
									getBaseContext(),
									"You need to authenticate your email. Check your inbox.",
									Toast.LENGTH_LONG)
									.show();
						}

					} else {
						Toast.makeText(view.getContext(), "Email or password invalid",
								Toast.LENGTH_SHORT).show();
						Log.w(TAG, "Invalid username or password");
					}
				});
	}

	/**
	 * Gets user data based on the user's email address.
	 *
	 * @param email email address to get user data from.
	 */
	public void getUserData(String email) {
		DatabaseHandler handler = new DatabaseHandler(this);
		SQLiteDatabase db = handler.getWritableDatabase();

		// Finding user where email and password match
		String selection = String.format("%s = ?", DatabaseHandler.Users.EMAIL);

//		// Arguments to use in the selection
		String[] args = {email};

		try (Cursor cursor = db.query(DatabaseHandler.Users.TABLE, null, selection,
				args, null, null, null)) {
			if (cursor.moveToFirst()) {
				// If cursor returns true, set the shared preferences to the user.

				int idCol = cursor.getColumnIndex(DatabaseHandler.Users.KEY);
				int usernameCol = cursor.getColumnIndex(DatabaseHandler.Users.USERNAME);

				int id = cursor.getInt(idCol);
				String username = cursor.getString(usernameCol);

				SharedPreferences.Editor editor = preferences.edit();
				editor.putInt(UserPrefs.userIdKey, id);
				editor.putString(UserPrefs.usernameKey, username);
				editor.apply();

				Toast.makeText(this, "Successfully logged in!",
						Toast.LENGTH_SHORT).show();

				sendToPhotoView();
			} else {
				// This occurs when the email and password don't match
				// (We check the password in the query, so this will be empty if not matched)
				Toast.makeText(this, "Username or password incorrect.",
						Toast.LENGTH_LONG).show();
			}
		} catch (SQLException exception) {
			Toast.makeText(this, "There was an error when logging in.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, exception.toString());
		}
	}

	/**
	 * Sends a reset password email using Firebase.
	 *
	 * @param view view that called this method
	 */
	public void resetPassword(View view) {
		String email = emailEditText.getText().toString();
		auth.sendPasswordResetEmail(email)
				.addOnCompleteListener(task -> {
					// If email reset is successfully sent, make toast
					if (task.isSuccessful()) {
						Toast.makeText(getApplicationContext(),
								"A password reset link has been sent to your email.",
								Toast.LENGTH_LONG).show();
					} else {
						// If the email is not sent (i.e. the email is wrong)
						Toast.makeText(getApplicationContext(),
								"Could not send password reset email. " +
										"Double-check your email address.",
								Toast.LENGTH_LONG).show();
						Log.w(TAG, "Password was not reset.");
					}
				});
	}
}