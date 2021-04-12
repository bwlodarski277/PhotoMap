package bwlodarski.photoMap.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Handles image conversions.
 * Can convert images to bitmaps and base-64 encoded strings.
 */
public final class ImageHandler {
	/**
	 * Generates a Bitmap from an image string from the database.
	 *
	 * @param imageString image string from the database
	 * @return Bitmap created from the image
	 */
	public static Bitmap stringToBitmap(String imageString) {
		byte[] bytes = Base64.decode(imageString, Base64.URL_SAFE);
		return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	}

	/**
	 * Generates database-safe string from an image bitmap.
	 *
	 * @param imageBitmap image bitmap to convert
	 * @return String created from the bitmap
	 */
	public static String bitmapToString(Bitmap imageBitmap) {
		// Converting the Bitmap image to a byte array
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
		byte[] array = outputStream.toByteArray();

		// Converting the byte array to a string safe for the DB
		return Base64.encodeToString(array, Base64.URL_SAFE);
	}

	public static byte[] bitmapToBytes(Bitmap imageBitmap) {
		// Converting the Bitmap image to a byte array
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
		return outputStream.toByteArray();
	}

	public static Bitmap bytesToBitmap(byte[] imageBytes) {
		return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
	}

	public static BitmapDescriptor bytesToBitmapDescriptor(byte[] imageBytes) {
		Bitmap image = bytesToBitmap(imageBytes);
		image = Bitmap.createScaledBitmap(image,100,100, false);
		return BitmapDescriptorFactory.fromBitmap(image);
	}

	public static FileReturn readFromFile(String path) throws IOException {
		byte[] photo;
		File photoFile = new File(path);
		photo = new byte[(int) photoFile.length()];

		FileInputStream stream = new FileInputStream(path);
		int bytesRead = stream.read(photo);

		return new FileReturn(photo, bytesRead);
	}

	public static class FileReturn {
		private final byte[] photo;
		private final int bytesRead;

		public FileReturn(byte[] photo, int bytesRead) {
			this.photo = photo;
			this.bytesRead = bytesRead;
		}

		public byte[] getPhoto() {
			return photo;
		}

		public int getBytesRead() {
			return bytesRead;
		}
	}
}
