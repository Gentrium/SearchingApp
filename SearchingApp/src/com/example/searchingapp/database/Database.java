package com.example.searchingapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * The Class  for Database.
 */
public class Database {

	/** The Constant DATABASE_NAME. */
	private static final String DATABASE_NAME = "MySearchingAppDatabase";

	/** The Constant DATABASE_VERSION. */
	private static final int DATABASE_VERSION = 1;

	/** The constant DATABASE_TABLE. */
	private static final String DATABASE_TABLE = "searchingresults";

	/** The constant COLUMN_ID. */
	public static final String COLUMN_ID = "_id";

	/** The constant COLUMN_VALUE. */
	public static final String COLUMN_VALUE = "value";

	/** The constant COLUMN_RESULT. */
	public static final String COLUMN_RESULT = "result";

	/** The constant to create database. */
	private static final String DATABASE_CREATE = "create table "
			+ DATABASE_TABLE + "(" + COLUMN_ID
			+ " integer primary key autoincrement, "
			+ COLUMN_VALUE + " text, "
			+ COLUMN_RESULT + " text);";

	/** The my context. */
	private final Context myContext;

	/** The my database helper. */
	private DatabaseHelper myDatabaseHelper;

	/** The my database. */
	private SQLiteDatabase myDatabase;

	/**
	 * Instantiates a new database.
	 * 
	 * @param ctx context
	 */
	public Database(final Context ctx) {
		myContext = ctx;
	}

	/**
	 * Open.
	 */
	public final void open() {
		myDatabaseHelper = new DatabaseHelper(myContext, 
				DATABASE_NAME,
				null,
				DATABASE_VERSION);
		myDatabase = myDatabaseHelper.getWritableDatabase();
	}

	/**
	 * Close.
	 */
	public final void close() {
		if (myDatabaseHelper != null) {
			myDatabaseHelper.close();
		}
	}

	/**
	 * Gets all data.
	 * 
	 * @return all data
	 */
	public final Cursor getAllData() {
		return myDatabase.query(DATABASE_TABLE,
				null,
				null,
				null,
				null,
				null,
				null);
	}

	/**
	 * Adds searching item to history.
	 * 
	 * @param value search value.
	 * @param result search result.
	 */
	public final void addRec(final String value, final String result) {
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_VALUE, value);
		cv.put(COLUMN_RESULT, result);
		myDatabase.insert(DATABASE_TABLE, null, cv);
	}

	/**
	 * Delete all.
	 */
	public final void deleteAll() {
		myDatabase.delete(DATABASE_TABLE, null, null);
	}

	/**
	 * The Class DatabaseHelper.
	 */
	private class DatabaseHelper extends SQLiteOpenHelper {

		/**
		 * Instantiates a new database helper.
		 * 
		 * @param context database context
		 * @param name database name
		 * @param factory database factory
		 * @param version database version
		 */
		public DatabaseHelper(final Context context, final String name,
				final CursorFactory factory, final int version) {
			super(context, name, factory, version);
		}

		/**
		 * Creates database.
		 */
		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		/**
		 * Upgrades existing database if needed.
		 */
		@Override
		public void onUpgrade(final SQLiteDatabase db,
				final int oldVersion,
				final int newVersion) {

		}
	}
}
