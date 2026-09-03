package com.usman.notepad.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.usman.notepad.data.NoteRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends Activity {
    private NoteRepository repo;private long noteId;private List<String[]> revisions;
    @Override protected void onCreate(Bundle b){super.onCreate(b);noteId=getIntent().getLongExtra("note_id",-1);repo=new NoteRepository(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(16,16,16,16);TextView h=new TextView(this);h.setText("Note History");h.setTextSize(24);root.addView(h);revisions=repo.revisions(noteId);ListView list=new ListView(this);String[] labels=new String[revisions.size()];SimpleDateFormat df=new SimpleDateFormat("MMM d, yyyy h:mm a",Locale.getDefault());for(int i=0;i<revisions.size();i++){String[] r=revisions.get(i);labels[i]=df.format(new Date(Long.parseLong(r[3])))+" — "+(r[1].trim().isEmpty()?"Untitled":r[1]);}list.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,labels));list.setOnItemClickListener((p,v,pos,id)->showRevision(pos));root.addView(list,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1));TextView note=new TextView(this);note.setText("Up to 30 recent meaningful versions are kept.");note.setGravity(Gravity.CENTER);root.addView(note);setContentView(root);}
    private void showRevision(int pos){String[] r=revisions.get(pos);String preview=(r[1].trim().isEmpty()?"Untitled":r[1])+"\n\n"+r[2];new AlertDialog.Builder(this).setTitle("Previous version").setMessage(preview).setNegativeButton("Cancel",null).setPositiveButton("Restore",(d,w)->{repo.restoreRevision(Long.parseLong(r[0]),noteId);Toast.makeText(this,"Version restored",Toast.LENGTH_SHORT).show();finish();}).show();}
}
