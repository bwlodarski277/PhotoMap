package bwlodarski.photoMap.activities;

import org.junit.Assert;
import org.junit.Test;

public class LoginActivityTest {

	@Test
	public void tooShortEmail() {
		int returnStatus = LoginActivity.checkEmailAndPassword("a", "a");
		Assert.assertEquals(-1, returnStatus);
	}

	@Test
	public void tooShortPassword() {
		int returnStatus = LoginActivity.checkEmailAndPassword("TestUser", "a");
		Assert.assertEquals(-2, returnStatus);
	}

	@Test
	public void bothValid() {
		int returnStatus = LoginActivity.checkEmailAndPassword("TestUser", "TestPassword");
		Assert.assertEquals(0, returnStatus);
	}
}