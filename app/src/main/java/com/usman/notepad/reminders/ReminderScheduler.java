package com.usman.notepad.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public final class ReminderScheduler {
    public static final String EXTRA_NOTE_ID="note_id";
    public static final String EXTRA_TITLE="title";
    private ReminderScheduler(){}

    public static void schedule(Context c,long noteId,String title,long triggerAt){
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        Intent i=new Intent(c,ReminderReceiver.class);
        i.putExtra(EXTRA_NOTE_ID,noteId);
        i.putExtra(EXTRA_TITLE,title==null?"Usman Notepad":title);
        PendingIntent pi=PendingIntent.getBroadcast(c,(int)(noteId^(noteId>>>32)),i,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        if(am!=null) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,triggerAt,pi);
    }

    public static void cancel(Context c,long noteId){
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        Intent i=new Intent(c,ReminderReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(c,(int)(noteId^(noteId>>>32)),i,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        if(am!=null) am.cancel(pi);
    }
}
