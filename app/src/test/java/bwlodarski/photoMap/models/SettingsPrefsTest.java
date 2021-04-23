package bwlodarski.photoMap.models;

import junit.framework.TestCase;

public class SettingsPrefsTest extends TestCase {
	public void testValues() {
		assertEquals("SETTINGS", SettingsPrefs.settingsPrefFile);
		assertEquals("COLS", SettingsPrefs.colsKey);
	}
}