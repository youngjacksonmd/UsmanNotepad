package com.usman.notepad.reminders;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.usman.notepad.ui.EditorActivity;

public final class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL="notes";
    @Override public void onReceive(Context c,Intent intent){
        NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        if(nm==null)return;
        if(Build.VERSION.SDK_INT>=26){
            nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Note reminders",NotificationManager.IMPORTANCE_DEFAULT));
        }
        long noteId=intent.getLongExtra(ReminderScheduler.EXTRA_NOTE_ID,-1);
        Intent open=new Intent(c,EditorActivity.class).putExtra("note_id",noteId).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi=PendingIntent.getActivity(c,(int)(noteId^(noteId>>>32)),open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        android.app.Notification.Builder b=Build.VERSION.SDK_INT>=26?new android.app.Notification.Builder(c,CHANNEL):new android.app.Notification.Builder(c);
        b.setSmallIcon(android.R.drawable.ic_menu_edit)
                .setContentTitle(intent.getStringExtra(ReminderScheduler.EXTRA_TITLE))
                .setContentText("Reminder from Usman Notepad")
                .setContentIntent(pi).setAutoCancel(true);
        nm.notify((int)(noteId^(noteId>>>32)),b.build());
    }
}
