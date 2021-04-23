package bwlodarski.photoMap.helpers;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.espresso.internal.inject.InstrumentationContext;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class DatabaseHandlerTest {

	Context context;

	@Before
	public void setup() {
		context = InstrumentationRegistry.getInstrumentation().getTargetContext();
	}

	@Test
	public void createHandler() {
		DatabaseHandler handler = new DatabaseHandler(context);
	}

	@Test
	public void createDB() {
		DatabaseHandler handler = new DatabaseHandler(context);
		SQLiteDatabase db = handler.getReadableDatabase();
	}

	@Test(expected = IllegalStateException.class)
	public void deleteInvalidTable() {
		DatabaseHandler handler = new DatabaseHandler(context);
		handler.deleteEntry("zoo", 1);
	}

	@Test
	public void deleteValidTable() {
		DatabaseHandler handler = new DatabaseHandler(context);
		handler.deleteEntry(DatabaseHandler.Photos.TABLE, 1);
		handler.deleteEntry(DatabaseHandler.Users.TABLE, 1);
		handler.deleteEntry(DatabaseHandler.UserPhotos.TABLE, 1);
	}
}