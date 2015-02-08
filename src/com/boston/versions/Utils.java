package com.boston.versions;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Utils {
	public static void displayNotification(String title, String text, int id, long rev, Context context)
	{
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Notification notification = new Notification(R.drawable.ic_launcher, title, System.currentTimeMillis());
	
		// The PendingIntent will launch activity if the user selects this notification
		Intent changes = new Intent(context, SeeFilesFromRevisionActivity.class);
		
		changes.putExtra("com.boston.versions.PEEK_ID", id);
		changes.putExtra("com.boston.versions.REV_NUM", rev);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, changes, 0);
	
		notification.setLatestEventInfo(context, title, text, contentIntent);
	
		manager.notify(Notification.FLAG_AUTO_CANCEL, notification);

	}
	
	public static void displayOGNotification(String title, String text, Context context)
	{
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_launcher, title, System.currentTimeMillis());
	
		// The PendingIntent will launch activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);

		notification.setLatestEventInfo(context, title, text, contentIntent);
	

		manager.notify(Notification.FLAG_ONGOING_EVENT, notification);

	}
}
