package com.kubbit.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.core.app.NotificationCompat;

import com.kubbit.horkonpon.R;

public class Notification
{
	public static class Data
	{
		public int id;
		public String ticker;
		public String title;
		public String text;
		public int number;
		public Boolean progress;
	}

	public static void Notify(Context context, Data data, PendingIntent intent)
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

		builder.setTicker(data.ticker)
		 .setContentTitle(data.title)
		 .setContentText(data.text)
		 .setNumber(data.number)
		 .setAutoCancel(true)
		 .setSmallIcon(R.drawable.stat_notify_sync);

		if (android.os.Build.VERSION.SDK_INT >= 16)
			builder.setPriority(android.app.Notification.PRIORITY_HIGH);

		if (data.progress)
		{
			builder.setProgress(0, 0, true);
			builder.setOngoing(true);
		}
		else
			builder.setSmallIcon(R.drawable.stat_notify_chat);

		Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.horkonpon);
		builder.setLargeIcon(largeIcon);

		if (intent != null)
			builder.setContentIntent(intent);

		android.app.Notification notification = builder.build();

		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(data.id, notification);
	}

	public static void remove(Context context, int id)
	{
		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(id);
	}
}
