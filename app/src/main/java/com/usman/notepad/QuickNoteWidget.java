package com.usman.notepad;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class QuickNoteWidget extends AppWidgetProvider {
    @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids){RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.notepad_widget);v.setOnClickPendingIntent(R.id.widget_new,pending(c,"new",10));v.setOnClickPendingIntent(R.id.widget_scratch,pending(c,"scratch",11));v.setOnClickPendingIntent(R.id.widget_daily,pending(c,"daily",12));m.updateAppWidget(id,v);}}
    private PendingIntent pending(Context c,String action,int req){Intent i=new Intent(c,MainActivity.class).putExtra("widget_action",action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);return PendingIntent.getActivity(c,req,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
}
