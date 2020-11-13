package com.kubbit.horkonpon;

import android.content.Context;

import java.util.Date;

import org.json.JSONArray;

import com.kubbit.horkonpon.db.Schema;

public class MessageList extends DBObjectList<Message>
{
	public MessageList(Context context)
	{
		super(context);
	}
	public MessageList(Context context, DBObject parent)
	{
		super(context, parent);
	}

	@Override
	protected Message newObject()
	{
		return new Message(this.context);
	}

	@Override
	public JSONArray asJSON(Date date) throws Exception
	{
		JSONArray result = new JSONArray();

		for (Message m: this)
		{
			// do not add messages not created by us
			if (m.getUser() != m.getIssue().getUser())
				continue;

			if (date != null && m.getDate().before(date))
				continue;

			result.put(m.asJSON());
		}

		return result;
	}

	@Override
	public void load()
	{
		String strSQL = String.format("SELECT * FROM %s", Schema.MessageTable.TABLE_NAME);

		this.load(strSQL, null);
	}

	public void load(Issue issue)
	{
		String strSQL = String.format("SELECT * FROM %s WHERE %s = ?", Schema.MessageTable.TABLE_NAME, Schema.MessageTable.COLUMN_ISSUE);
		String[] whereArgs = { String.valueOf(issue.getId()) };

		this.load(strSQL, whereArgs);
	}
}
