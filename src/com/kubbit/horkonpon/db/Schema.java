package com.kubbit.horkonpon.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.kubbit.utils.Utils;

public class Schema extends SQLiteOpenHelper
{
	public static final String DATABASE_NAME = "horkonpon.db";
	public static final int DATABASE_VERSION = 1;

	public static final String USER_ANONYMOUS_REMOTE_ID = "anonymous";
	public static final long USER_ANONYMOUS_ID = 0;
	public static final long USER_MYSELF_ID = 1;
	public static final long USER_CUSTOM_ID = 2;

	private static Schema instance;

	public static final String ISSUE_CREATE = String.format("CREATE TABLE %s", IssueTable.TABLE_NAME)
	 + "("
	 + String.format("%s INTEGER PRIMARY KEY,", IssueTable._ID)
	 + String.format("%s VARCHAR NULL,", IssueTable.COLUMN_REMOTE_ID)
	 + String.format("%s DATE NOT NULL,", IssueTable.COLUMN_DATE)
	 + String.format("%s INTEGER NOT NULL,", IssueTable.COLUMN_USER)
	 + String.format("%s REAL NOT NULL,", IssueTable.COLUMN_LATITUDE)
	 + String.format("%s REAL NOT NULL,", IssueTable.COLUMN_LONGITUDE)
	 + String.format("%s INTEGER NOT NULL,", IssueTable.COLUMN_ACCURACY)
	 + String.format("%s VARCHAR NULL,", IssueTable.COLUMN_LOCALITY)
	 + String.format("%s INTEGER NULL,", IssueTable.COLUMN_STATUS)
	 + String.format("%s DATE NOT NULL,", IssueTable.COLUMN_UPDATE_DATE)
	 + String.format("%s DATE NULL", IssueTable.COLUMN_LAST_SYNC)
	 + ")";

	public static final String MESSAGE_CREATE = String.format("CREATE TABLE %s", MessageTable.TABLE_NAME)
	 + "("
	 + String.format("%s INTEGER PRIMARY KEY,", MessageTable._ID)
	 + String.format("%s INTEGER NULL,", MessageTable.COLUMN_ISSUE)
	 + String.format("%s DATE NOT NULL,", MessageTable.COLUMN_DATE)
	 + String.format("%s INTEGER NOT NULL,", MessageTable.COLUMN_USER)
	 + String.format("%s VARCHAR NOT NULL", MessageTable.COLUMN_TEXT)
	 + ")";

	public static final String FILE_CREATE = String.format("CREATE TABLE %s", FileTable.TABLE_NAME)
	 + "("
	 + String.format("%s INTEGER PRIMARY KEY,", FileTable._ID)
	 + String.format("%s INTEGER NULL,", FileTable.COLUMN_ISSUE)
	 + String.format("%s DATE NOT NULL,", FileTable.COLUMN_DATE)
	 + String.format("%s INTEGER NOT NULL,", FileTable.COLUMN_USER)
	 + String.format("%s VARCHAR NOT NULL", FileTable.COLUMN_NAME)
	 + ")";

	public static final String USER_CREATE = String.format("CREATE TABLE %s", UserTable.TABLE_NAME)
	 + "("
	 + String.format("%s INTEGER PRIMARY KEY,", UserTable._ID)
	 + String.format("%s VARCHAR NULL,", UserTable.COLUMN_REMOTE_ID)
	 + String.format("%s VARCHAR NULL,", UserTable.COLUMN_FULLNAME)
	 + String.format("%s VARCHAR NULL,", UserTable.COLUMN_MAIL)
	 + String.format("%s VARCHAR NULL", UserTable.COLUMN_PHONE)
	 + ")";

	public static final String USER_INSERT_ANONYMOUS = Utils.NLStringFormat("INSERT INTO %s (%s, %s) VALUES (%d, '%s')",
	 UserTable.TABLE_NAME, UserTable._ID, UserTable.COLUMN_REMOTE_ID, USER_ANONYMOUS_ID, USER_ANONYMOUS_REMOTE_ID);
	public static final String USER_INSERT_MYSELF = Utils.NLStringFormat("INSERT INTO %s (%s) VALUES (%d)",
	 UserTable.TABLE_NAME, UserTable._ID, USER_MYSELF_ID);
	public static final String USER_INSERT_CUSTOM = Utils.NLStringFormat("INSERT INTO %s (%s) VALUES (%d)",
	 UserTable.TABLE_NAME, UserTable._ID, USER_CUSTOM_ID);

	public static abstract class IssueTable implements BaseColumns
	{
		public static final String TABLE_NAME = "issue";

		public static final String COLUMN_REMOTE_ID = "rid";
		public static final String COLUMN_DATE = "date";
		public static final String COLUMN_USER = "user";
		public static final String COLUMN_LATITUDE = "latitude";
		public static final String COLUMN_LONGITUDE = "longitude";
		public static final String COLUMN_ACCURACY = "accuracy";
		public static final String COLUMN_LOCALITY = "locality";
		public static final String COLUMN_ADDRESS = "address";
		public static final String COLUMN_STATUS = "status";
		public static final String COLUMN_UPDATE_DATE = "updated";
		public static final String COLUMN_LAST_SYNC = "sync";
	}

	public static abstract class MessageTable implements BaseColumns
	{
		public static final String TABLE_NAME = "message";

		public static final String COLUMN_ISSUE = "issue";
		public static final String COLUMN_DATE = "date";
		public static final String COLUMN_USER = "user";
		public static final String COLUMN_TEXT = "text";
	}

	public static abstract class FileTable implements BaseColumns
	{
		public static final String TABLE_NAME = "file";

		public static final String COLUMN_ISSUE = "issue";
		public static final String COLUMN_DATE = "date";
		public static final String COLUMN_USER = "user";
		public static final String COLUMN_NAME = "name";
	}

	public static abstract class UserTable implements BaseColumns
	{
		public static final String TABLE_NAME = "user";

		public static final String COLUMN_REMOTE_ID = "rid";
		public static final String COLUMN_FULLNAME = "fullname";
		public static final String COLUMN_MAIL = "mail";
		public static final String COLUMN_PHONE = "phone";
	}

	public static Schema getInstance(Context context)
	{
		if (instance == null)
			instance = new Schema(context.getApplicationContext());

		return instance;
	}

	private Schema(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(ISSUE_CREATE);
		db.execSQL(MESSAGE_CREATE);
		db.execSQL(FILE_CREATE);
		db.execSQL(USER_CREATE);

		db.execSQL(USER_INSERT_ANONYMOUS);
		db.execSQL(USER_INSERT_MYSELF);
		db.execSQL(USER_INSERT_CUSTOM);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
