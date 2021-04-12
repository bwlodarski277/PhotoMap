package bwlodarski.photoMap.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import bwlodarski.photoMap.R;
import bwlodarski.photoMap.helpers.DatabaseHandler;

import static bwlodarski.photoMap.activities.LoginActivity.minPassLength;
import static bwlodarski.photoMap.activities.LoginActivity.minUserLength;

public class RegisterActivity extends AppCompatActivity {

	private static final String TAG = "RegisterActivity";
	EditText userEditText, emailEditText, passwordEditText;
	FirebaseAuth auth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		userEditText = findViewById(R.id.username);
		emailEditText = findViewById(R.id.email);
		passwordEditText = findViewById(R.id.password);

		auth = FirebaseAuth.getInstance();

		Intent login = getIntent();
		emailEditText.setText(login.getStringExtra("EMAIL"));
	}

	/**
	 * Registers a new user.
	 *
	 * @param view view to read data from (login screen)
	 */
	public void register(View view) {
		String user = userEditText.getText().toString();
		String email = emailEditText.getText().toString();
		String password = passwordEditText.getText().toString();

		Context context = getApplicationContext();

		// Checking the email and password length
		if (email.length() < minUserLength) {
			String msg = String.format("Username needs to be at least %s long.", minUserLength);
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			return;
		}

		if (password.length() < minPassLength) {
			String msg = String.format("Password needs to be at least %s long.", minPassLength);
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			return;
		}

		// Registering the user on Firebase
		auth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, task -> {
					if (task.isSuccessful()) {

						// Adding the user to the local DB
						addUserToDB(user, email);

						FirebaseUser firebaseUser = auth.getCurrentUser();
						assert firebaseUser != null;
						firebaseUser.sendEmailVerification();

						Intent login = new Intent(getApplicationContext(), LoginActivity.class);
						login.putExtra("EMAIL", email);
						login.putExtra("PASS", password);

						Toast.makeText(getApplicationContext(),
								"Please verify your email, check your inbox.",
								Toast.LENGTH_SHORT).show();

						setResult(Activity.RESULT_OK, login);
						finish();
					} else {
						Log.e(TAG, "User was not registered", task.getException());
						Toast.makeText(getApplicationContext(),
								"Registering user failed (double-check your email address.)",
								Toast.LENGTH_LONG).show();
					}
				});
	}

	/**
	 * Adds a username and password to the database.
	 *
	 * @param user  Username to add
	 * @param email email of the user being added
	 */
	private void addUserToDB(String user, String email) {
		DatabaseHandler handler = new DatabaseHandler(this);
		SQLiteDatabase db = handler.getWritableDatabase();

		String[] cols = {DatabaseHandler.Users.KEY};
		String selection = String.format("%s = ?", DatabaseHandler.Users.EMAIL);
		String[] args = {email};

		// Selecting user IDs where email and password match
		try (Cursor cursor = db.query(DatabaseHandler.Users.TABLE, cols, selection, args,
				null, null, null)) {
			if (!cursor.moveToFirst()) {
				// The email was not found in the database, so create the new user.
				ContentValues userData = new ContentValues();
				userData.put(DatabaseHandler.Users.EMAIL, email);
				userData.put(DatabaseHandler.Users.USERNAME, user);

				// Adding the new user to the DB
				db.insertOrThrow(DatabaseHandler.Users.TABLE, null, userData);

				Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();

			} else {
				Toast.makeText(this, "This email is taken.",
						Toast.LENGTH_SHORT).show();
			}
		} catch (SQLException exception) {
			Toast.makeText(this, "There was an error when registering.",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, exception.toString());
		}
	}
}