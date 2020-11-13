package com.kubbit.horkonpon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Date;

import org.json.JSONObject;

import com.kubbit.horkonpon.db.Schema;
import com.kubbit.utils.Log;

public abstract class DBObject<T extends DBObject> implements Comparable<T>
{
	protected Long id;
	private Date date = null;
	protected Boolean exists = false;
	protected DBObject parent = null;

	protected String tablename = null;
	protected String columnId = null;

	protected final Context context;

	public Long getId()
	{
		return this.id;
	}
	public Date getDate()
	{
		return this.date;
	}
	public void setDate(Date date)
	{
		// remove milliseconds
		date.setTime(1000 * (date.getTime() / 1000));

		this.date = date;
	}
	public Boolean getExists()
	{
		return this.exists;
	}

	public DBObject(Context context)
	{
		this.context = context;
	}
	public DBObject(Context context, DBObject parent)
	{
		this(context);

		this.parent = parent;
	}

	protected void clear()
	{
		this.exists = false;

		this.id = null;
	}

	public Boolean validate()
	{
		return true;
	}

	public JSONObject asJSON() throws Exception
	{
		return this.asJSON(null);
	}

	public abstract JSONObject asJSON(Date date) throws Exception;

	public void fromJSON(String json) throws Exception
	{
		this.fromJSON(new JSONObject(json));
	}

	public void fromJSON(JSONObject json) throws Exception
	{
		this.save();
	}

	public void load(Cursor cursor)
	{
		this.id = cursor.getLong(cursor.getColumnIndex(this.columnId));

		this.exists = true;
	}

	protected abstract void storeValues(ContentValues values);

	public void save()
	{
		if (this.date == null)
			this.setDate(new Date());

		SQLiteDatabase db = Schema.getInstance(this.context).getWritableDatabase();
		try
		{
			ContentValues values = new ContentValues();

			this.storeValues(values);

			if (!this.exists)
			{
				this.id = db.insert(this.tablename, null, values);

				// check for errors
				if (this.id == -1)
					Log.error("Error while saving to database");

				this.exists = true;
			}
			else
			{
				String whereClause = this.columnId + " = ?";
				String[] whereArgs = { String.valueOf(this.id) };

				if (db.update(this.tablename, values, whereClause, whereArgs) != 1)
					Log.error("Error while updating database");
			}
		}
		finally
		{
			db.close();
		}
	}

	public abstract Boolean isDuplicate(T object);

	@Override
	public int compareTo(T obj)
	{
		return this.getDate().compareTo(obj.getDate());
	}
}
