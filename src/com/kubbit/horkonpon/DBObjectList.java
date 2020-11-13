package com.kubbit.horkonpon;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.json.JSONArray;

import com.kubbit.horkonpon.db.Schema;

public abstract class DBObjectList<T extends DBObject> extends ArrayList<T>
{
	protected final Context context;
	protected DBObject parent = null;

	public DBObjectList(Context context)
	{
		this.context = context;
	}

	public DBObjectList(Context context, DBObject parent)
	{
		this(context);

		this.parent = parent;
	}

	public T getById(Long id)
	{
		for (T o: this)
		{
			if (o.getId().equals(id))
				return o;
		}

		return null;
	}

	public Boolean exists(T object)
	{
		for (T o: this)
			if (o.isDuplicate(object))
				return true;

		return false;
	}

	@Override
	public boolean add(T object)
	{
		if (this.exists(object))
			return false;

		object.parent = this.parent;

		return super.add(object);
	}

	public T first()
	{
		if (this.size() > 0)
			return this.get(0);

		return null;
	}

	public T last()
	{
		if (this.size() > 0)
			return this.get(this.size() - 1);

		return null;
	}

	protected abstract T newObject();

	public JSONArray asJSON(Date date) throws Exception
	{
		JSONArray result = new JSONArray();

		for (T o: this)
		{
			if (date != null && o.getDate().before(date))
				continue;

			result.put(o.asJSON());
		}

		return result;
	}

	public void fromJSON(JSONArray json) throws Exception
	{
		for (int i = 0; i < json.length(); i++)
		{
			T object = this.newObject();

			object.fromJSON(json.getJSONObject(i));

			this.add(object);
		}
	}

	public abstract void load();

	protected void load(String query, String[] whereArgs)
	{
		Cursor cursor = Schema.getInstance(context).getReadableDatabase().rawQuery(query, whereArgs);
		try
		{
			this.clear();

			cursor.moveToFirst();
			while (!cursor.isAfterLast())
			{
				T o = this.newObject();
				this.add(o);

				o.load(cursor);

				cursor.moveToNext();
			}
		}
		finally
		{
			cursor.close();
		}
	}

	public void save()
	{
		for (T object: this)
			object.save();
	}

	public void sort()
	{
		this.sort(true);
	}
	public void sort(Boolean ascending)
	{
		Collections.sort(this);

		if (!ascending)
			Collections.reverse(this);
	}
}
