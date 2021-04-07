package bwlodarski.courseworkapplication.Helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Handles image conversions.
 * Can convert images to bitmaps and base-64 encoded strings.
 */
public final class ImageHandler {
	/**
	 * Generates a Bitmap from an image string from the database.
	 * @param imageString image string from the database
	 * @return Bitmap created from the image
	 */
	public static Bitmap getBitmap(String imageString) {
		byte[] bytes = Base64.decode(imageString, Base64.URL_SAFE);
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}

	/**
	 * Generates database-safe string from an image bitmap.
	 * @param imageBitmap image bitmap to convert
	 * @return String created from the bitmap
	 */
	public static String getString(Bitmap imageBitmap) {
		// Converting the Bitmap image to a byte array
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
		byte[] array = outputStream.toByteArray();

		// Converting the byte array to a string safe for the DB
		return Base64.encodeToString(array, Base64.URL_SAFE);
	}
}
