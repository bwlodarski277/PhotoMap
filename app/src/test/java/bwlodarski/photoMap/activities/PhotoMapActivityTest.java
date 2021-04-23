package bwlodarski.photoMap.activities;

import androidx.core.util.Pair;

import com.google.android.gms.maps.model.Marker;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PhotoMapActivityTest {

	Marker marker;

	@Test
	public void checkMarkerId() {
		List<Pair<Marker, Integer>> markers = new ArrayList<>();
		markers.add(new Pair<>(marker, 1));
		int markerId = PhotoMapActivity.checkMarkerId(markers, marker);
		Assert.assertEquals(-1, markerId);
	}
}