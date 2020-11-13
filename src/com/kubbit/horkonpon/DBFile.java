package com.kubbit.horkonpon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.json.JSONObject;

import com.kubbit.horkonpon.db.Schema;
import com.kubbit.media.Image;
import com.kubbit.utils.Log;
import com.kubbit.utils.Utils;

public class DBFile extends DBObject<DBFile>
{
	private User user = null;
	private String name = null;

	private Bitmap thumbnail = null;

	private File file = null;

	public User getUser()
	{
		return this.user;
	}
	public String getName()
	{
		return this.name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public Issue getIssue()
	{
		if (this.parent instanceof Issue)
			return (Issue)this.parent;

		return null;
	}

	public File getFile() throws IOException
	{
		if (this.file == null)
			this.createFile();

		return this.file;
	}
	public String getFullPath()
	{
		try
		{
			return this.getFile().getCanonicalPath();
		}
		catch (IOException ex)
		{
			Log.error("Error getting file's full path: " + ex.getMessage());

			return null;
		}
	}

	public Bitmap getThumbnail()
	{
		if (this.thumbnail == null)
			this.loadThumbnail();

		return this.thumbnail;
	}

	public DBFile(Context context)
	{
		this(context, null);
	}

	public DBFile(Context context, User user)
	{
		super(context);

		this.tablename = Schema.FileTable.TABLE_NAME;
		this.columnId = Schema.FileTable._ID;

		this.user = user;
	}

	@Override
	public JSONObject asJSON(Date date) throws Exception
	{
		JSONObject json = new JSONObject();

		json.put("date", Utils.dateToStr(this.getDate(), API.DATE_FORMAT));
		json.put("filename", this.getName());
		json.put("content", this.asBase64());

		return json;
	}

	@Override
	public void load(Cursor cursor)
	{
		super.load(cursor);

		if (!cursor.isNull(cursor.getColumnIndex(Schema.FileTable.COLUMN_DATE)))
			this.setDate(new Date(cursor.getLong(cursor.getColumnIndex(Schema.FileTable.COLUMN_DATE))));
		if (!cursor.isNull(cursor.getColumnIndex(Schema.FileTable.COLUMN_USER)))
			this.user = UserList.getInstance(this.context).getById(cursor.getLong(cursor.getColumnIndex(Schema.FileTable.COLUMN_USER)));
		this.parent = IssueList.getInstance(this.context).getById(cursor.getLong(cursor.getColumnIndex(Schema.MessageTable.COLUMN_ISSUE)));
		this.name = cursor.getString(cursor.getColumnIndex(Schema.FileTable.COLUMN_NAME));

		if (this.getIssue() != null)
		{
			String path = String.format("%s/%s", this.getIssue().getPicturesDir(), this.name);
			this.file = new File(path);
		}
	}

	@Override
	protected void storeValues(ContentValues values)
	{
		if (this.user == null && this.getIssue() != null)
			this.user = this.getIssue().getUser();

		values.put(Schema.FileTable.COLUMN_DATE, this.getDate().getTime());
		values.put(Schema.FileTable.COLUMN_USER, this.user.getId());
		values.put(Schema.FileTable.COLUMN_ISSUE, this.parent.getId());
		values.put(Schema.FileTable.COLUMN_NAME, this.name);
	}

	@Override
	public void save()
	{
		// do not save if issue has not been saved yed
		if (this.parent == null || !this.parent.getExists())
			return;

		this.relocateFile();

		super.save();
	}

	@Override
	public Boolean isDuplicate(DBFile file)
	{
		// prevent creating temporary file
		if (this.file == null || file.file == null)
			return false;

		return this.getFullPath().equals(file.getFullPath());
	}

	public void delete()
	{
		// do not allow deleting files in saved issues
		if (this.exists)
			return;

		if  (this.file != null && this.file.exists())
			this.file.delete();
	}

	private void createFile() throws IOException
	{
		this.file = Utils.getTemporaryFile(this.context, Constants.IMAGE_EXTENSION);
	}

	private void loadThumbnail()
	{
		if (this.file == null)
			return;

		try
		{
			if (this.getIssue() == null)
				return;

			Image img = null;

			// check if file exists in thumbnails directory
			String path = String.format("%s/%s", this.getIssue().getPicturesDir(true), this.name);
			File thumbFile = new File(path);
			if (!thumbFile.exists())
			{
				// create from original image and save in cache for future usage
				img = new Image(this.file);
				img.resize(Constants.THUMBNAIL_MIN_SIZE);
				img.writeJPG(thumbFile, Constants.THUMBNAIL_QUALITY);
			}
			else
				// load from thumbnails directory
				img = new Image(thumbFile);

			this.thumbnail = img.getBitmap();
		}
		catch (Exception e)
		{
			Log.error(e.getMessage());
		}
	}

	/**
	 * move file to subdir with issue id
	 */
	private void relocateFile()
	{
		// rename photo to subdir with issue id
		String newPath = String.format("%s/%s", this.getIssue().getPicturesDir(), this.file.getName());

		this.file = Utils.renameFile(this.file, newPath);
	}

	public String asBase64() throws Exception
	{
		return Utils.toBase64(this.file);
	}
}
