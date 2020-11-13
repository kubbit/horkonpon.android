package com.kubbit.horkonpon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONObject;

import com.kubbit.horkonpon.db.Schema;
import com.kubbit.lists.ListAdaptable;
import com.kubbit.utils.Utils;

public class Message extends DBObject<Message> implements ListAdaptable, Comparable<Message>
{
	private User user = null;
	private String text = null;

	public User getUser()
	{
		return this.user;
	}
	public String getText()
	{
		return this.text;
	}
	public void setText(String text)
	{
		this.text = text;
	}
	public Issue getIssue()
	{
		if (this.parent instanceof Issue)
			return (Issue)this.parent;

		return null;
	}

	public Message(Context context)
	{
		this(context, null);
	}

	public Message(Context context, User user)
	{
		super(context);

		this.tablename = Schema.MessageTable.TABLE_NAME;
		this.columnId = Schema.MessageTable._ID;

		this.user = user;
	}

	@Override
	public JSONObject asJSON(Date date) throws Exception
	{
		JSONObject json = new JSONObject();

		json.put("date", Utils.dateToStr(this.getDate(), API.DATE_FORMAT));
		json.put("text", this.getText());

		return json;
	}

	@Override
	public void fromJSON(JSONObject json) throws Exception
	{
		this.setText(json.getString("text"));
		this.setDate(Utils.strToDate(json.getString("date"), API.DATE_FORMAT));
		this.user = UserList.getInstance(this.context).getByRemoteId(json.getString("user"));

		super.fromJSON(json);
	}

	@Override
	public void load(Cursor cursor)
	{
		super.load(cursor);

		if (!cursor.isNull(cursor.getColumnIndex(Schema.MessageTable.COLUMN_DATE)))
			this.setDate(new Date(cursor.getLong(cursor.getColumnIndex(Schema.MessageTable.COLUMN_DATE))));
		if (!cursor.isNull(cursor.getColumnIndex(Schema.MessageTable.COLUMN_USER)))
			this.user = UserList.getInstance(this.context).getById(cursor.getLong(cursor.getColumnIndex(Schema.MessageTable.COLUMN_USER)));
		this.parent = IssueList.getInstance(this.context).getById(cursor.getLong(cursor.getColumnIndex(Schema.MessageTable.COLUMN_ISSUE)));
		this.text = cursor.getString(cursor.getColumnIndex(Schema.MessageTable.COLUMN_TEXT));
	}

	@Override
	protected void storeValues(ContentValues values)
	{
		if (this.user == null)
			this.user = this.getIssue().getUser();

		values.put(Schema.MessageTable.COLUMN_DATE, this.getDate().getTime());
		values.put(Schema.MessageTable.COLUMN_USER, this.user.getId());
		values.put(Schema.MessageTable.COLUMN_ISSUE, this.parent.getId());
		values.put(Schema.MessageTable.COLUMN_TEXT, this.text);
	}

	@Override
	public void save()
	{
		// do not save if issue has not been saved yed
		if (this.parent == null || !this.parent.getExists())
			return;

		super.save();
	}

	@Override
	public Boolean isDuplicate(Message message)
	{
		return Utils.equals(this.text, message.text)
		 && Utils.equals(this.getDate(), message.getDate())
		 && Utils.equals(this.user, message.user);
	}

	public Boolean isMine()
	{
		return this.user == this.getIssue().getUser();
	}

	@Override
	public String toListText()
	{
		return this.getText();
	}

	@Override
	public String toListSubText()
	{
		SimpleDateFormat formatter;

		if (DateUtils.isToday(this.getDate().getTime()))
			formatter = Utils.NLSimpleDateFormat("HH:mm");
		else
			formatter = Utils.NLSimpleDateFormat("MMM dd");

		return formatter.format(this.getDate());
	}

	@Override
	public Bitmap toPicture()
	{
		return null;
	}

	@Override
	public int compareTo(Message message)
	{
		return this.getDate().compareTo(message.getDate());
	}
}
