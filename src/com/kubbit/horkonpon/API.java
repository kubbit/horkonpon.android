package com.kubbit.horkonpon;

import android.content.Context;

import java.util.Date;

import org.json.JSONObject;

import com.kubbit.horkonpon.db.Schema;
import com.kubbit.net.HttpClient;
import com.kubbit.utils.Log;

public class API
{
	public class StatusCodes
	{
		static final int NO_ERROR = 0;
	}

	final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	final static int HORKONPON_API_MESSAGE_VERSION = 3;

	private final Context context;

	public API(Context context)
	{
		this.context = context;
	}

	protected void registerUser()
	{
		final String path = "/users/";

		try
		{
			JSONObject user = Settings.asJSON();

			Log.debug(user.toString(3));
			String response = this.request(path, HttpClient.RequestMethod.POST, user.toString());
			Log.debug(response);

			JSONObject jsResponse = new JSONObject(response);
			String id = jsResponse.getString("id");
			Settings.setUserId(id);

			User myself = UserList.getInstance(this.context).getMyUser();
			myself.setRemoteId(id);
			myself.save();

			Settings.setModified(false);
		}
		catch (Exception e)
		{
			Log.error(e.getMessage());
		}
	}

	protected void updateUser()
	{
		final String path = "/users/%s";

		try
		{
			JSONObject user = Settings.asJSON();

			User myself = UserList.getInstance(this.context).getMyUser();

			this.request(String.format(path, myself.getRemoteId()), HttpClient.RequestMethod.POST, user.toString(), this.getUsername(myself), this.getPassword(myself));

			Settings.setModified(false);
		}
		catch (Exception e)
		{
			Log.error(e.getMessage());
		}
	}

	public void sendNew(Issue issue)
	{
		final String path = "/issues/";

		try
		{
			Date date = new Date();

			String response = this.request(path, HttpClient.RequestMethod.POST, issue.asJSON().toString(), issue.getUser());
			Log.debug(response);

			issue.fromJSON(response);
			issue.setLastSync(date);
			issue.save();
		}
		catch (Exception e)
		{
			Log.error(e.getMessage());
		}
	}

	public void sendUpdate(Issue issue)
	{
		final String path = "/issues/%s";

		try
		{
			Date date = new Date();

			/* PATCH method not supported by HttpClient */
			String response = this.request(String.format(path, issue.getRemoteId()), HttpClient.RequestMethod.POST, issue.asJSON(issue.getLastSync()).toString(), issue.getUser());
			Log.debug(response);

			issue.fromJSON(response);
			issue.setLastSync(date);
			issue.save();
		}
		catch (Exception e)
		{
			Log.error(e.getMessage());
		}
	}

	public void getUpdates(Issue issue)
	{
		final String path = "/issues/%s/";

		try
		{
			Date date = new Date();

			String response = this.request(String.format(path, issue.getRemoteId()), HttpClient.RequestMethod.GET, null, issue.getUser());
			Log.debug(response);

			issue.fromJSON(response);
			issue.setLastSync(date);
			issue.save();
		}
		catch (Exception ex)
		{
			Log.error(ex.getMessage());
		}
	}

	private String request(String path, String method, String data)
	{
		return this.request(path, method, data, null, null);
	}

	private String request(String path, String method, String data, User user)
	{
		if (user.getId() == Schema.USER_MYSELF_ID)
		{
			if (!Settings.isUserRegistered())
				this.registerUser();
			else if (Settings.getModified())
				this.updateUser();
		}

		return this.request(path, method, data, this.getUsername(user), this.getPassword(user));
	}

	private String request(String path, String method, String data, String user, String password)
	{
		HttpClient http = new HttpClient(Settings.getApiUrl() + path);

		http.clearParams();

		if (data != null)
			http.addBody("application/json", data);

		return http.connect(method, user, password);
	}

	private String getUsername(User user)
	{
		if (user == null || user.isAnonimous())
			return null;
		
		if (user.getId() == Schema.USER_MYSELF_ID)
			return Settings.getUserId();

		return Settings.getCustomUserId();
	}

	private String getPassword(User user)
	{
		if (user == null || user.isAnonimous())
			return null;
		
		if (user.getId() == Schema.USER_MYSELF_ID)
			return Settings.getUserKey();

		return Settings.getCustomUserPass();
	}
}
