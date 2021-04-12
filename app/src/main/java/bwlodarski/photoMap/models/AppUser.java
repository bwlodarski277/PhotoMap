package bwlodarski.photoMap.models;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AppUser class
 *
 * used to store and retrieve information about a user.
 */
public class AppUser {
	private final String username;
	private final String password;
	private final String createdOn;

	/**
	 * AppUser constructor
	 * @param username new user's username
	 * @param password new user's password
	 */
	public AppUser(String username, String password) {
		this.username = username;
		this.password = password;

		String pattern = "HH:mm:ss dd/MM/yyyy";
		SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.getDefault());
		this.createdOn = formatter.format(new Date());
	}

	/**
	 * Gets the user's username.
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Gets the user's password.
	 * @return password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Generates an AppUser String.
	 */
	@NonNull
	@Override
	public String toString() {
		return "User " + username + " created on " + createdOn;
	}
}
