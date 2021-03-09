package bwlodarski.courseworkapplication.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import bwlodarski.courseworkapplication.Models.AppUser;
import bwlodarski.courseworkapplication.R;

public class LoginActivity extends AppCompatActivity {

	private ArrayList<AppUser> users;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		users = new ArrayList<>();

		Intent photoView = new Intent(getApplicationContext(), PhotoViewActivity.class);
		startActivity(photoView);
		finish();
	}

	public void register(View view) {
		EditText usernameEditText = (EditText) findViewById(R.id.username);
		EditText passwordEditText = (EditText) findViewById(R.id.password);

		String username = usernameEditText.getText().toString();
		String password = passwordEditText.getText().toString();

		Context context = getApplicationContext();
		if(username.length() < 8 || password.length() < 8) {
			String msg = "Username and password must be at least 8 characters long.";
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			return;
		}

		for (AppUser user : users) {
			if (user.getUsername().equals(username)) {
				String msg = "This username is taken";
				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
				return;
			}
		}

		AppUser user = new AppUser(username, password);
		users.add(user);

		String msg = "Account created!";
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}

	public void login(View view) {
		EditText usernameEditText = (EditText) findViewById(R.id.username);
		EditText passwordEditText = (EditText) findViewById(R.id.password);

		String username = usernameEditText.getText().toString();
		String password = passwordEditText.getText().toString();

		AppUser found = null;
		for (AppUser user : users) {
			assert user.getUsername() != null && user.getPassword() != null;
			if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
				found = user;
				break;
			}
		}

		String msg;
		if (found != null) {
			Intent photoView = new Intent(getApplicationContext(), PhotoViewActivity.class);
			startActivity(photoView);
			finish();
		} else {
			msg = "Invalid login";
			Context context = getApplicationContext();
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}


	}
}