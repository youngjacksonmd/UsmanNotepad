package com.usman.notepad.widget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.usman.notepad.data.NoteRepository;
import com.usman.notepad.ui.EditorActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WidgetRouterActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);String action=getIntent().getStringExtra("widget_action");NoteRepository repo=new NoteRepository(this);long id;if("scratch".equals(action))id=repo.getOrCreateScratch();else if("daily".equals(action))id=repo.getOrCreateDaily(new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(new Date()));else id=repo.createNote("","");startActivity(new Intent(this,EditorActivity.class).putExtra("note_id",id));finish();}
}
