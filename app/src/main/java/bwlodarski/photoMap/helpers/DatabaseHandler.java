package bwlodarski.photoMap.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database handler class for interactions with the database.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

	/**
	 * Database version.
	 */
	public static final int DATABASE_VERSION = 1;

	/**
	 * Database name.
	 */
	public static final String DATABASE_NAME = "PhotoMap";
	public Context context;

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	/**
	 * Generates an SQL statement that drops the specified table.
	 *
	 * @param table table name string
	 * @return SQL statement in the form of a String
	 */
	private String dropTableSQL(String table) {
		return "DROP TABLE IF EXISTS " + table + ";";
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Users.CREATE_USERS);
		db.execSQL(Photos.CREATE_PHOTOS);
		db.execSQL(UserPhotos.CREATE_LINK);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Dropping the three tables
		db.execSQL(dropTableSQL(UserPhotos.TABLE));
		db.execSQL(dropTableSQL(Users.TABLE));
		db.execSQL(dropTableSQL(Photos.TABLE));

		// Creating the tables again
		onCreate(db);
	}

	/**
	 * Deletes an entry from a table in the database.
	 *
	 * @param table table to delete a row from.
	 * @param row   row ID to delete.
	 */
	public void deleteEntry(String table, long row) {
		String tableName, keyId;
		switch (table) {
			case Users.TABLE:
				tableName = Users.TABLE;
				keyId = Users.KEY;
				break;
			case Photos.TABLE:
				tableName = Photos.TABLE;
				keyId = Photos.KEY;
				break;
			case UserPhotos.TABLE:
				tableName = UserPhotos.TABLE;
				keyId = UserPhotos.PHOTO_KEY;
				break;
			default: // If the table name is not in the DB, throw an error
				throw new IllegalStateException("Illegal enum value");
		}
		SQLiteDatabase db = getWritableDatabase();
		db.delete(tableName, keyId + "=" + row, null);
	}

	/**
	 * User class representing columns in the users table.
	 */
	public static final class Users {
		/**
		 * Table name
		 */
		public static final String TABLE = "users";
		/**
		 * ID column
		 */
		public static final String KEY = "id";
		/**
		 * Username column
		 */
		public static final String USERNAME = "username";
		/**
		 * Email column
		 */
		public static final String EMAIL = "email";

		/**
		 * User table creation SQL.
		 */
		public static final String CREATE_USERS =
				String.format(
						"CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
								"%s TEXT UNIQUE NOT NULL, %s TEXT NOT NULL);",
						TABLE, KEY, EMAIL, USERNAME);
	}

	/**
	 * Photo class representing columns in the photos table.
	 */
	public static final class Photos {
		/**
		 * Table name
		 */
		public static final String TABLE = "photos";
		/**
		 * ID column
		 */
		public static final String KEY = "id";
		/**
		 * Photo string column
		 */
		public static final String PHOTO = "photo";

		/**
		 * Latitude of photo
		 */
		public static final String LAT = "lat";

		/**
		 * Longitude of photo
		 */
		public static final String LON = "lon";

		/**
		 * Temperature reading when the photo was taken
		 */
		public static final String TEMP = "temp";

		/**
		 * Ambient light reading when the photo was taken
		 */
		public static final String LIGHT = "light";

		/**
		 * Photo table creation SQL.
		 */
		public static final String CREATE_PHOTOS =
				String.format(
						"CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
								"%s BLOB, %s REAL, %s FLOAT, %s FLOAT, %s FLOAT);",
						TABLE, KEY, PHOTO, LAT, LON, TEMP, LIGHT);
	}

	/**
	 * User Photo class representing columns in the userPhotos link table.
	 */
	public static final class UserPhotos {
		/**
		 * Table name
		 */
		public static final String TABLE = "userPhotos";
		/**
		 * User ID column
		 */
		public static final String USER_KEY = "userId";
		/**
		 * Photo ID column
		 */
		public static final String PHOTO_KEY = "photoId";

		/**
		 * UserPhoto table creation SQL.
		 */
		public static final String CREATE_LINK =
				String.format("CREATE TABLE %s (%s INT NOT NULL, %s INT NOT NULL,FOREIGN KEY (%s) " +
								"REFERENCES users(%s), FOREIGN KEY (%s) REFERENCES photos(%s), " +
								"PRIMARY KEY (%s, %s));",
						TABLE, USER_KEY, PHOTO_KEY, USER_KEY, Users.KEY,
						PHOTO_KEY, Photos.KEY, USER_KEY, PHOTO_KEY);
	}
}
