package com.kubbit.horkonpon;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kubbit.horkonpon.db.Schema;
import com.kubbit.lists.ListAdaptable;
import com.kubbit.utils.Log;
import com.kubbit.utils.Utils;

public class Issue extends DBObject<Issue> implements ListAdaptable
{
	private static final int OUTDATED_MEASURE_DIVISOR = 5;
	private static final int OUTDATED_MIN_SYNC_INTERVAL = 60 * 1000;

	private String remoteId;
	private User user = null;
	private double latitude = 0;
	private double longitude = 0;
	private double accuracy = Float.MAX_VALUE;
	private String locality = "";
	private final MessageList messages = new MessageList(this.context, this);
	private final DBFileList files = new DBFileList(this.context, this);
	private Integer status = null;
	private Date updated = null;
	private Date lastSync = null;
	private Integer unread = 0;

	public String getRemoteId()
	{
		return this.remoteId;
	}
	public void setRemoteId(String uid)
	{
		this.remoteId = uid;

		this.updated = new Date();
	}
	public User getUser()
	{
		return this.user;
	}
	public DBFile getImage()
	{
		if (this.files.isEmpty())
			return null;

		return this.files.get(0);
	}
	public void setImage(DBFile file) throws Exception
	{
		if (this.exists)
			throw new Exception("Unable to replace image in a saved issue.");

		// delete previous image
		this.deleteFile(this.getImage());

		this.addFile(file);
	}
	public double getLatitude()
	{
		return this.latitude;
	}
	public void setLatitude(double latitude)
	{
		this.latitude = latitude;

		this.updated = new Date();
	}
	public double getLongitude()
	{
		return this.longitude;
	}
	public void setLongitude(double longitude)
	{
		this.longitude = longitude;

		this.updated = new Date();
	}
	public double getAccuracy()
	{
		return this.accuracy;
	}
	public void setAccuracy(double accuracy)
	{
		this.accuracy = accuracy;

		this.updated = new Date();
	}
	public String getLocality()
	{
		if (this.locality == null || this.locality.isEmpty())
			return Utils.NLStringFormat("[%f, %f]", this.latitude, this.longitude);

		return this.locality;
	}
	public void setLocality(String locality)
	{
		this.locality = locality;

		this.updated = new Date();
	}
	public Integer getStatus()
	{
		return this.status;
	}
	public void setStatus(Integer status)
	{
		this.status = status;
	}
	public Date getUpdateDate()
	{
		return this.updated;
	}
	public Date getLastSync()
	{
		return this.lastSync;
	}
	public void setLastSync(Date date)
	{
		this.lastSync = date;
	}
	public Integer unread()
	{
		return this.unread;
	}
	public void read()
	{
		this.unread = 0;
	}
	public Boolean hasUnsentData()
	{
		return this.getLastSync() == null || this.getUpdateDate().after(this.getLastSync());
	}
	public MessageList getMessages()
	{
		return this.messages;
	}
	public DBFileList getFiles()
	{
		return this.files;
	}

	public String getPicturesDir()
	{
		return this.getPicturesDir(false);
	}
	public String getPicturesDir(Boolean thumbnails)
	{
		if (thumbnails)
			return Utils.NLStringFormat("%s/.thumbnails/%d", this.context.getExternalCacheDir(), this.getId());
		else
			return Utils.NLStringFormat("%s/%d", this.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), this.getId());
	}

	public User getInterlocutor()
	{
		for (Message m: this.getMessages())
			if (!m.isMine())
				return m.getUser();

		return null;
	}

	public String getInterlocutorText()
	{
		User u = this.getInterlocutor();

		if (u != null)
			return u.getName();

		return this.getLocality();
	}

	public Issue(Context context)
	{
		super(context);

		this.tablename = Schema.IssueTable.TABLE_NAME;
		this.columnId = Schema.IssueTable._ID;
	}

	public Boolean hasPhoto()
	{
		return !this.files.isEmpty();
	}

	@Override
	protected void clear()
	{
		super.clear();

		// delete photos if issue has not been saved
		for (DBFile f: this.files)
			f.delete();

		this.latitude = 0;
		this.longitude = 0;
		this.accuracy = Float.MAX_VALUE;
		this.locality = "";
		this.messages.clear();
		this.status = null;
		this.updated = null;
		this.unread = 0;
	}

	@Override
	public Boolean validate()
	{
		if (!this.hasPhoto() && this.messages.isEmpty())
		{
			Toast.makeText(this.context, R.string.validate_gertakaria_no_data, Toast.LENGTH_LONG).show();
			return false;
		}

		if (this.latitude == 0 && this.longitude == 0)
		{
			Toast.makeText(this.context, R.string.validate_gertakaria_no_gps, Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}

	public void addMessage(String text)
	{
		if (text == null || text.isEmpty())
			return;

		Message message = new Message(this.context);
		if (!this.messages.add(message))
			return;

		message.setDate(new Date());
		message.setText(text);

		message.save();

		this.updated = new Date();
		this.save();
	}

	public void addFile(DBFile file) throws Exception
	{
		if (file == null)
			return;

		this.files.add(file);

		file.setDate(new Date());
		file.setName(file.getFile().getName());

		file.save();

		this.updated = new Date();
		this.save();
	}

	protected void deleteFile(DBFile file)
	{
		// do not allow deleting files from saved issues
		if (this.exists || file == null)
			return;

		this.files.remove(file);

		file.delete();

		this.updated = new Date();
	}

	@Override
	public JSONObject asJSON(Date date) throws Exception
	{
		JSONObject json = new JSONObject();

		if (date == null)
		{
			json.put("date", Utils.dateToStr(this.getDate(), API.DATE_FORMAT));

			JSONObject app = new JSONObject();
			app.put("os", Utils.getOSVersion());
			app.put("version", Utils.getVersion(context));
			json.put("app", app);
		}

		if (this.hasPhoto())
		{
			JSONArray jFiles = this.files.asJSON(date);

			if (jFiles.length() > 0)
				json.put("files", jFiles);
		}

		if (date == null)
		{
			JSONObject location = new JSONObject();

			if (this.latitude != 0 || this.longitude != 0)
			{
				JSONObject gps = new JSONObject();
				gps.put("latitude", this.latitude);
				gps.put("longitude", this.longitude);
				gps.put("accuracy", this.accuracy);
				location.put("gps", gps);
			}

			if (this.locality != null && !this.locality.equals(""))
				location.put("locality", this.locality);

			if (location.length() > 0)
				json.put("location", location);
		}

		if (!this.getMessages().isEmpty())
		{
			JSONArray jsMessages = this.messages.asJSON(date);

			if (jsMessages.length() > 0)
				json.put("messages", jsMessages);
		}

		Log.debug(json.toString(3));

		return json;
	}

	@Override
	public void fromJSON(JSONObject json) throws Exception
	{
		int messagesCount = this.messages.size();

		if (json.has("id") && (this.getRemoteId() == null || !this.getRemoteId().equals(json.getString("id"))))
		{
			this.setRemoteId(json.getString("id"));

			if (this.status == Constants.IssueStatus.NEW)
				this.setStatus(Constants.IssueStatus.SENT);
		}

		if ((this.locality == null || this.locality.isEmpty())
		 && json.has("location") && json.getJSONObject("location").has("locality"))
			this.setLocality(json.getJSONObject("location").getString("locality"));

		if (json.has("users"))
			UserList.getInstance(this.context).fromJSON(json.getJSONArray("users"));

		if (json.has("messages"))
			this.messages.fromJSON(json.getJSONArray("messages"));

		this.unread += this.messages.size() - messagesCount;

		if (this.unread > 0)
			this.updated = new Date();

		super.fromJSON(json);
	}

	@Override
	public void load(Cursor cursor)
	{
		super.load(cursor);

		this.remoteId = cursor.getString(cursor.getColumnIndex(Schema.IssueTable.COLUMN_REMOTE_ID));
		if (!cursor.isNull(cursor.getColumnIndex(Schema.IssueTable.COLUMN_DATE)))
			this.setDate(new Date(cursor.getLong(cursor.getColumnIndex(Schema.IssueTable.COLUMN_DATE))));
		this.user = UserList.getInstance(this.context).getById(cursor.getLong(cursor.getColumnIndex(Schema.IssueTable.COLUMN_USER)));
		this.latitude = cursor.getDouble(cursor.getColumnIndex(Schema.IssueTable.COLUMN_LATITUDE));
		this.longitude = cursor.getDouble(cursor.getColumnIndex(Schema.IssueTable.COLUMN_LONGITUDE));
		this.accuracy = cursor.getDouble(cursor.getColumnIndex(Schema.IssueTable.COLUMN_ACCURACY));
		this.locality = cursor.getString(cursor.getColumnIndex(Schema.IssueTable.COLUMN_LOCALITY));
		this.messages.load(this);
		this.status = cursor.getInt(cursor.getColumnIndex(Schema.IssueTable.COLUMN_STATUS));
		this.updated = new Date(cursor.getLong(cursor.getColumnIndex(Schema.IssueTable.COLUMN_UPDATE_DATE)));
		if (!cursor.isNull(cursor.getColumnIndex(Schema.IssueTable.COLUMN_LAST_SYNC)))
			this.lastSync = new Date(cursor.getLong(cursor.getColumnIndex(Schema.IssueTable.COLUMN_LAST_SYNC)));

		this.files.load(this);
	}

	@Override
	protected void storeValues(ContentValues values)
	{
		if (this.user == null)
			this.user = UserList.getInstance(this.context).getMyUser();

		values.put(Schema.IssueTable.COLUMN_REMOTE_ID, this.remoteId);
		values.put(Schema.IssueTable.COLUMN_DATE, this.getDate().getTime());
		values.put(Schema.IssueTable.COLUMN_USER, this.user.getId());
		values.put(Schema.IssueTable.COLUMN_LATITUDE, this.latitude);
		values.put(Schema.IssueTable.COLUMN_LONGITUDE, this.longitude);
		values.put(Schema.IssueTable.COLUMN_ACCURACY, this.accuracy);
		values.put(Schema.IssueTable.COLUMN_LOCALITY, this.locality);
		values.put(Schema.IssueTable.COLUMN_STATUS, this.status);
		values.put(Schema.IssueTable.COLUMN_UPDATE_DATE, this.updated.getTime());
		if (this.lastSync == null)
			values.putNull(Schema.IssueTable.COLUMN_LAST_SYNC);
		else
			values.put(Schema.IssueTable.COLUMN_LAST_SYNC, this.lastSync.getTime());
	}

	@Override
	public void save()
	{
		if (this.status == null)
			return;

		if (!this.validate())
			return;

		// add new issue to list of loaded issues
		if (!IssueList.getInstance(this.context).contains(this))
			IssueList.getInstance(this.context).add(this);

		super.save();

		this.messages.save();
		this.files.save();

		UserList.getInstance(this.context).save();

		this.sync();
	}

	@Override
	public Boolean isDuplicate(Issue issue)
	{
		return this.getRemoteId() != null && this.getRemoteId().equals(issue.getRemoteId());
	}

	/**
	 * Checks if issue has not been sincronized with server for a while
	 *
	 * @return true if issue needs to check for updates with server, false otherwise
	 */
	public Boolean isOutdated()
	{
		if (this.hasUnsentData())
			return true;

		if (this.getLastSync() == null)
			return true;

		long now = (new Date()).getTime();

		long sinceSync = now - this.getLastSync().getTime();
		if (sinceSync < OUTDATED_MIN_SYNC_INTERVAL)
			return false;

		long sinceUpdate = now - this.getUpdateDate().getTime();

		return sinceSync > sinceUpdate / OUTDATED_MEASURE_DIVISOR;
	}

	public void sync()
	{
		this.sync(false);
	}
	public void sync(Boolean force)
	{
		if (!force && !this.isOutdated())
			return;

		// if there is an ongoing queue, do not create new
		if (Sender.getActive())
			return;

		Intent intent = new Intent(this.context, Sender.class);
		intent.putExtra("id", this.getId());
		if (force)
			intent.putExtra("force", true);

		this.context.startService(intent);
	}

	@Override
	public int compareTo(Issue obj)
	{
		return this.getUpdateDate().compareTo(obj.getUpdateDate());
	}

	@Override
	public String toListText()
	{
		if (!this.messages.isEmpty())
			return this.messages.last().getText();

		return null;
	}

	@Override
	public String toListSubText()
	{
		SimpleDateFormat formatter = Utils.NLSimpleDateFormat("HH:mm, MMM dd");
		String subtext = "";

		subtext = Utils.NLStringFormat("%s\n%s", this.getLocality(), formatter.format(this.updated));

		if (this.unread > 0)
			subtext = subtext + Utils.NLStringFormat(" (%d)", this.unread);

		return subtext;
	}

	@Override
	public Bitmap toPicture()
	{
		if (this.getImage() != null)
			return this.getImage().getThumbnail();

		return null;
	}
}
