package com.usman.notepad.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.usman.notepad.R;

public class QuickNoteWidgetProvider extends AppWidgetProvider {
    @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids){RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget_quick);v.setOnClickPendingIntent(R.id.widget_new,pending(c,1,"new"));v.setOnClickPendingIntent(R.id.widget_scratch,pending(c,2,"scratch"));v.setOnClickPendingIntent(R.id.widget_daily,pending(c,3,"daily"));m.updateAppWidget(id,v);}}
    private PendingIntent pending(Context c,int request,String action){Intent i=new Intent(c,WidgetRouterActivity.class).putExtra("widget_action",action);return PendingIntent.getActivity(c,request,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
}
