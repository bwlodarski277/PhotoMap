package bwlodarski.photoMap.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
	 * Converts a bitmap to bytes.
	 * Bytes are compressed to 90% quality.
	 *
	 * @param imageBitmap bitmap to convert to bytes
	 * @return byte array of converted bitmap
	 */
	public static byte[] bitmapToBytes(Bitmap imageBitmap) {
		// Converting the Bitmap image to a byte array
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream);
		return outputStream.toByteArray();
	}

	/**
	 * Converts a byte array to bitmap object.
	 *
	 * @param imageBytes byte array to convert to bitmap
	 * @return bitmap object from byte array
	 */
	public static Bitmap bytesToBitmap(byte[] imageBytes) {
		return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
	}

	/**
	 * Converts a byte array to a BitmapDescriptor (for use in map icons).
	 *
	 * @param imageBytes byte array to convert
	 * @return BitmapDescriptor constructed from bytes
	 */
	public static BitmapDescriptor bytesToBitmapDescriptor(byte[] imageBytes) {
		Bitmap image = bytesToBitmap(imageBytes);
		image = Bitmap.createScaledBitmap(image, 100, 100, false);
		return BitmapDescriptorFactory.fromBitmap(image);
	}

	/**
	 * Reads a file from internal storage.
	 *
	 * @param path path of file to read (internal storage)
	 * @return FileReturn object containing size and bytes read
	 * @throws IOException if file does not exist, or for any other IO reason.
	 * @see ImageHandler.FileReturn
	 */
	public static FileReturn readFromFile(String path) throws IOException {
		byte[] photo;
		File photoFile = new File(path);
		photo = new byte[(int) photoFile.length()];

		FileInputStream stream = new FileInputStream(path);
		int bytesRead = stream.read(photo);

		return new FileReturn(photo, bytesRead);
	}

	/**
	 * FileReturn object, returned in readFromFile method.
	 * @see ImageHandler#readFromFile
	 */
	public static class FileReturn {
		private final byte[] photo;
		private final int bytesRead;

		public FileReturn(byte[] photo, int bytesRead) {
			this.photo = photo;
			this.bytesRead = bytesRead;
		}

		/**
		 * Gets the photo bytes.
		 * @return photo bytes
		 */
		public byte[] getPhoto() {
			return photo;
		}

		/**
		 * Gets the bytes read when reading file.
		 * @return number of bytes
		 */
		public int getBytesRead() {
			return bytesRead;
		}
	}
}
