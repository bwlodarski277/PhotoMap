package bwlodarski.photoMap.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseHandlerTest {

	@Mock
	Context context;

	@Test
	public void createHandler() {
		DatabaseHandler handler = new DatabaseHandler(context);
	}

	@Test(expected = RuntimeException.class)
	public void createDB() {
		SQLiteDatabase db = new DatabaseHandler(context).getReadableDatabase();
	}

	@Test(expected = IllegalStateException.class)
	public void deleteEntryInvalid() {
		DatabaseHandler handler = new DatabaseHandler(context);
		handler.deleteEntry("zoo", 1);
	}

	@Test(expected = RuntimeException.class)
	public void deleteEntryValid1() {
		DatabaseHandler handler = new DatabaseHandler(context);
		handler.deleteEntry(DatabaseHandler.UserPhotos.TABLE, 1);
	}

	@Test(expected = RuntimeException.class)
	public void deleteEntryValid2() {
		DatabaseHandler handler = new DatabaseHandler(context);
		handler.deleteEntry(DatabaseHandler.Photos.TABLE, 1);
	}
	@Test(expected = RuntimeException.class)
	public void deleteEntryValid3() {
		DatabaseHandler handler = new DatabaseHandler(context);
		handler.deleteEntry(DatabaseHandler.Users.TABLE, 1);
	}

	@Test
	public void testStaticVars() {
		String createUsers = DatabaseHandler.Users.CREATE_USERS;
		String createPhotos = DatabaseHandler.Photos.CREATE_PHOTOS;
		String createUserPhotos = DatabaseHandler.UserPhotos.CREATE_LINK;
	}
}