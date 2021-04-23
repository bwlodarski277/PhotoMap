package bwlodarski.photoMap.activities;

import com.google.android.gms.maps.model.Marker;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocationSelectionActivityTest {

	Marker marker;

	@Test
	public void testMarkerOutput() {
		String result = LocationSelectionActivity._setLatLong(marker);
		Assert.assertEquals("0.0, 0.0", result);
	}
}