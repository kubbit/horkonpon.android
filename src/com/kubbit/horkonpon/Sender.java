package com.kubbit.horkonpon;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.kubbit.net.Net;
import com.kubbit.utils.Notification;

public class Sender extends IntentService
{
	Long currentId = Long.MIN_VALUE;
	private API api;
	private static Boolean active = false;
	public final static Integer NOTIFICATION_ID_SYNC = 0;
	public final static Integer NOTIFICATION_ID_UNREAD = 1;
	private static Boolean progressVisible = false;

	public static Boolean getActive()
	{
		return active;
	}

	public Sender()
	{
		this(Sender.class.getName());
	}

	public Sender(String name)
	{
		super(name);

		this.api = new API(this);
	}

	protected void sync(Long id, Boolean force)
	{
		if (!Net.isNetworkAvailable(this))
			return;

		try
		{
			if (id != null)
			{
				Issue issue = IssueList.getInstance(this).getById(id);
				this.sync(issue, force);
			}
			else
			{
				Integer totalNewUnread = 0;

				for (Issue i: IssueList.getInstance(this))
				{
					Integer prevUnread = i.unread();

					this.sync(i, false);

					if (prevUnread < i.unread())
						totalNewUnread += i.unread() - prevUnread;
				}

				if (totalNewUnread > 0)
					this.showUnreadNotification(NOTIFICATION_ID_UNREAD, getResources().getQuantityString(R.plurals.new_messages, totalNewUnread, totalNewUnread), totalNewUnread);
			}
		}
		finally
		{
			this.removeProgressNotification();
		}
	}

	protected void sync(Issue issue, Boolean force)
	{
		if (!force && !issue.isOutdated())
			return;

		this.showProgressNotification();

		this.process(issue);
	}

	protected void process(Issue issue)
	{
		if (issue == null)
			return;

		if (!Net.isNetworkAvailable(this))
			return;

		this.currentId = issue.getId();

		if (issue.hasUnsentData())
		{
			if (issue.getRemoteId() == null)
				this.api.sendNew(issue);
			else
				this.api.sendUpdate(issue);
		}
		else
			this.api.getUpdates(issue);
	}

	protected void showProgressNotification()
	{
		if (progressVisible)
			return;

		progressVisible = true;

		Notification.Data notif = new Notification.Data();

		notif.id = NOTIFICATION_ID_SYNC;
		notif.title = getString(R.string.app_name);
		notif.ticker = getString(R.string.sync_progress);
		notif.text = getString(R.string.sync_progress);
		notif.progress = true;

		Notification.Notify(this, notif, null);
	}

	protected void removeProgressNotification()
	{
		Notification.remove(this, NOTIFICATION_ID_SYNC);

		progressVisible = false;
	}

	protected void showUnreadNotification(int id, String message, int count)
	{
		Notification.Data notif = new Notification.Data();

		notif.id = id;
		notif.title = getString(R.string.app_name);
		notif.ticker = message;
		notif.text = message;
		notif.number = count;
		notif.progress = false;

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

		Notification.Notify(this, notif, contentIntent);
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		active = true;

		Long id = intent.getLongExtra("id", -1);
		if (id == -1)
			id = null;

		Boolean force = intent.getBooleanExtra("force", false);

		this.sync(id, force);

		// notify other activities when sync process has finished
		Intent localIntent = new Intent(Constants.BROADCAST_KEY);
		LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
	}

	@Override
	public void onTaskRemoved(Intent rootIntent)
	{
		this.removeProgressNotification();

		if (this.currentId != Long.MIN_VALUE)
			Notification.remove(this, this.currentId.intValue());
	}

	@Override
	public void onDestroy()
	{
		active = false;
	}
}
