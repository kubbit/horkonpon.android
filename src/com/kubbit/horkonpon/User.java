package com.kubbit.horkonpon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.Date;

import org.json.JSONObject;

import com.kubbit.horkonpon.db.Schema;

public class User extends DBObject<User>
{
	private String remoteId = null;
	private String fullname = null;
	private String mail = null;
	private String phone = null;

	public String getRemoteId()
	{
		if (this.exists && this.id == Schema.USER_CUSTOM_ID)
			return Settings.getCustomUserId();

		return this.remoteId;
	}
	public void setRemoteId(String value)
	{
		this.remoteId = value;
	}
	public String getName()
	{
		return this.fullname;
	}
	public String getMail()
	{
		return this.mail;
	}
	public String getPhone()
	{
		return this.phone;
	}

	public Boolean isAnonimous()
	{
		return this.id == Schema.USER_ANONYMOUS_ID;
	}

	public User(Context context)
	{
		super(context);

		this.tablename = Schema.UserTable.TABLE_NAME;
		this.columnId = Schema.UserTable._ID;
	}

	@Override
	public JSONObject asJSON(Date date) throws Exception
	{
		JSONObject json = new JSONObject();

		if (this.remoteId != null && !this.remoteId.equals(""))
			json.put("id", this.remoteId);

		if (this.fullname != null && !this.fullname.equals(""))
			json.put("fullname", this.getName());

		if (this.mail != null && !this.mail.equals(""))
			json.put("mail", this.getMail());

		if (this.phone != null && !this.phone.equals(""))
			json.put("phone", this.getPhone());

		return json;
	}

	@Override
	public void fromJSON(JSONObject json) throws Exception
	{
		if (json.has("id"))
			this.remoteId = json.getString("id");
		if (json.has("fullname"))
			this.fullname = json.getString("fullname");
		if (json.has("mail"))
			this.mail = json.getString("mail");
		if (json.has("phone"))
			this.phone = json.getString("phone");
	}

	@Override
	public void load(Cursor cursor)
	{
		super.load(cursor);

		this.remoteId = cursor.getString(cursor.getColumnIndex(Schema.UserTable.COLUMN_REMOTE_ID));
		this.fullname = cursor.getString(cursor.getColumnIndex(Schema.UserTable.COLUMN_FULLNAME));
		this.mail = cursor.getString(cursor.getColumnIndex(Schema.UserTable.COLUMN_MAIL));
		this.phone = cursor.getString(cursor.getColumnIndex(Schema.UserTable.COLUMN_PHONE));
	}

	@Override
	protected void storeValues(ContentValues values)
	{
		values.put(Schema.UserTable.COLUMN_REMOTE_ID, this.remoteId);
		values.put(Schema.UserTable.COLUMN_FULLNAME, this.fullname);
		values.put(Schema.UserTable.COLUMN_MAIL, this.mail);
		values.put(Schema.UserTable.COLUMN_PHONE, this.phone);
	}

	@Override
	public Boolean isDuplicate(User user)
	{
		return this.getRemoteId() != null && this.getRemoteId().equals(user.getRemoteId());
	}
}
