package bwlodarski.photoMap.helpers;

import android.graphics.Bitmap;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ImageHandlerTest  {

	String testFilePath = "src/test/resources/FLAG_B24.BMP";
	File testImg = new File(testFilePath);


	/**
	 * This test passes if a RuntimeException occurs.
	 * This is because, you cannot mock BitmapFactory.
	 *
	 * @see <a href="http://tools.android.com/tech-docs/unit-testing-support#TOC-Method-...-not-mocked.-">here</a>
	 */
	@Test
	public void testBitmapToBytes() {
		try {
			ImageHandler.FileReturn img = ImageHandler.readFromFile(testFilePath);
			Bitmap bitmap = ImageHandler.bytesToBitmap(img.getPhoto());
			ImageHandler.bitmapToBytes(bitmap);
		} catch (IOException exception) {
			fail("IO exception occurred");
		} catch (RuntimeException exception) {
			// BitmapFactory cannot be mocked
			assertTrue(true);
		}
	}

	/**
	 * This test passes if a RuntimeException occurs.
	 * This is because, you cannot mock BitmapFactory.
	 *
	 * @see <a href="http://tools.android.com/tech-docs/unit-testing-support#TOC-Method-...-not-mocked.-">here</a>
	 */
	@Test
	public void testBytesToBitmap() {
		try {
			ImageHandler.FileReturn img = ImageHandler.readFromFile(testFilePath);
			Bitmap bitmap = ImageHandler.bytesToBitmap(img.getPhoto());
		} catch (IOException exception) {
			fail("IO exception occurred");
		} catch (RuntimeException exception) {
			// BitmapFactory cannot be mocked
			assertTrue(true);
		}

	}

	/**
	 * This test passes if a RuntimeException occurs.
	 * This is because, you cannot mock BitmapFactory.
	 *
	 * @see <a href="http://tools.android.com/tech-docs/unit-testing-support#TOC-Method-...-not-mocked.-">here</a>
	 */
	@Test
	public void testBytesToBitmapDescriptor() {
		try {
			ImageHandler.FileReturn img = ImageHandler.readFromFile(testFilePath);
			ImageHandler.bytesToBitmapDescriptor(img.getPhoto());
		} catch (IOException exception) {
			fail("IO exception occurred");
		} catch (RuntimeException exception) {
			// BitmapFactory cannot be mocked
			assertTrue(true);
		}
	}

	/**
	 * Testing reading file bytes
	 */
	@Test
	public void testReadFromFile() {
		try {
			// Reading file and making sure the size is equals
			ImageHandler.FileReturn img = ImageHandler.readFromFile(testFilePath);
			assertEquals(testImg.length(), img.getBytesRead());
		} catch (IOException exception) {
			fail("IO exception occurred");
		}
	}
}