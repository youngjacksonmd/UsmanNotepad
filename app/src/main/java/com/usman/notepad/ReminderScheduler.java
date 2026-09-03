package com.usman.notepad;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public final class ReminderScheduler {
    private ReminderScheduler() {}
    public static void schedule(Context c,long noteId,long triggerAt){
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if(am==null||triggerAt<=System.currentTimeMillis())return;
        PendingIntent pi=PendingIntent.getBroadcast(c,(int)(noteId ^ (noteId>>>32)),new Intent(c,ReminderReceiver.class).putExtra("note_id",noteId),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,triggerAt,pi);
    }
    public static void cancel(Context c,long noteId){AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(am==null)return;PendingIntent pi=PendingIntent.getBroadcast(c,(int)(noteId^(noteId>>>32)),new Intent(c,ReminderReceiver.class).putExtra("note_id",noteId),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);am.cancel(pi);}
}
