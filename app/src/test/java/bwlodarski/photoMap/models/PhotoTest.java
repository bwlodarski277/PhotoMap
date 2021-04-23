package bwlodarski.photoMap.models;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

public class PhotoTest {

	@Test
	public void testGetId() {
		Photo photo = new Photo(1, new byte[]{1,2,3}, "test");
		Assert.assertEquals(1, photo.getId());
	}

	@Test
	public void testGetImage() {
		byte[] bytes = new byte[]{1,2,3};
		Photo photo = new Photo(1, bytes, "test");
		Assert.assertEquals(bytes, photo.getImage());
	}

	@Test
	public void testGetPath() {
		Photo photo = new Photo(1, new byte[]{1,2,3}, "test");
		Assert.assertEquals("test", photo.getPath());
	}
}