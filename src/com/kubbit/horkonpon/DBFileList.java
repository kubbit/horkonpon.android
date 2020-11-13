package com.kubbit.horkonpon;

import android.content.Context;

import java.util.Date;

import org.json.JSONArray;

import com.kubbit.horkonpon.db.Schema;

public class DBFileList extends DBObjectList<DBFile>
{
	public DBFileList(Context context)
	{
		super(context);
	}
	public DBFileList(Context context, DBObject parent)
	{
		super(context, parent);
	}

	@Override
	protected DBFile newObject()
	{
		return new DBFile(this.context);
	}

	@Override
	public JSONArray asJSON(Date date) throws Exception
	{
		JSONArray result = new JSONArray();

		for (DBFile f: this)
		{
			// do not add files not created by us
			if (f.getUser() != f.getIssue().getUser())
				continue;

			if (date != null && f.getDate().before(date))
				continue;

			result.put(f.asJSON());
		}

		return result;
	}

	@Override
	public void load()
	{
		String strSQL = String.format("SELECT * FROM %s", Schema.FileTable.TABLE_NAME);

		this.load(strSQL, null);
	}

	public void load(Issue issue)
	{
		String strSQL = String.format("SELECT * FROM %s WHERE %s = ?", Schema.FileTable.TABLE_NAME, Schema.FileTable.COLUMN_ISSUE);
		String[] whereArgs = { String.valueOf(issue.getId()) };

		this.load(strSQL, whereArgs);
	}
}
