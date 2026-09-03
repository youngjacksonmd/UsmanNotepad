package com.usman.notepad;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL="notes";
    @Override public void onReceive(Context context,Intent intent){long id=intent.getLongExtra("note_id",-1);NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(nm==null)return;if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Note reminders",NotificationManager.IMPORTANCE_DEFAULT));Intent open=new Intent(context,EditorActivity.class).putExtra("note_id",id);PendingIntent pi=PendingIntent.getActivity(context,(int)id,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);android.app.Notification.Builder b=Build.VERSION.SDK_INT>=26?new android.app.Notification.Builder(context,CHANNEL):new android.app.Notification.Builder(context);b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Usman Notepad reminder").setContentText("Tap to open your note").setAutoCancel(true).setContentIntent(pi);nm.notify((int)(id^(id>>>32)),b.build());}
}
