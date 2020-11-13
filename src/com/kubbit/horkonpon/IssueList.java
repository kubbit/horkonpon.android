package com.kubbit.horkonpon;

import android.content.Context;
import android.content.Intent;

import com.kubbit.horkonpon.db.Schema;

public class IssueList extends DBObjectList<Issue>
{
	private static IssueList instance;
	private Issue selected;

	public IssueList(Context context)
	{
		super(context);
	}

	public Issue getSelected()
	{
		if (this.isEmpty())
			return null;

		return this.selected;
	}
	public void setSelected(Issue issue)
	{
		this.selected = issue;
	}

	public static IssueList getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new IssueList(context.getApplicationContext());
			instance.load();
		}

		return instance;
	}

	@Override
	protected Issue newObject()
	{
		return new Issue(this.context);
	}

	@Override
	public void load()
	{
		String strSQL = String.format("SELECT * FROM %s", Schema.IssueTable.TABLE_NAME);

		this.load(strSQL, null);
	}

	public void sync()
	{
		// if there is an ongoing queue, do not create new
		if (Sender.getActive())
			return;

		Intent intent = new Intent(this.context, Sender.class);
		this.context.startService(intent);
	}
}
