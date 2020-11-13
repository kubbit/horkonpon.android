package com.kubbit.horkonpon;

import android.content.Context;

import com.kubbit.horkonpon.db.Schema;

public class UserList extends DBObjectList<User>
{
	private static UserList instance;

	public UserList(Context context)
	{
		super(context);
	}

	public static UserList getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new UserList(context.getApplicationContext());
			instance.load();
		}

		return instance;
	}

	public User getByRemoteId(String rid)
	{
		for (User u: this)
		{
			if (u.getRemoteId() != null && u.getRemoteId().equals(rid))
				return u;
		}

		return null;
	}

	public User getMyUser()
	{
		if (Settings.getAnonymous())
			return this.getById(Schema.USER_ANONYMOUS_ID);
		else if (Settings.getAdvanced() && Settings.getCustomUserId() != null)
			return this.getById(Schema.USER_CUSTOM_ID);

		return this.getById(Schema.USER_MYSELF_ID);
	}

	@Override
	protected User newObject()
	{
		return new User(this.context);
	}

	@Override
	public void load()
	{
		String strSQL = String.format("SELECT * FROM %s", Schema.UserTable.TABLE_NAME);

		this.load(strSQL, null);
	}
}
