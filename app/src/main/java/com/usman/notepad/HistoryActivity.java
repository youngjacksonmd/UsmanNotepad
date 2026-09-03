package com.usman.notepad;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.usman.notepad.data.NoteRepository;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);long noteId=getIntent().getLongExtra("note_id",-1);NoteRepository repo=new NoteRepository(this);List<String> raw=repo.revisions(noteId);List<String> rows=new ArrayList<>();for(String x:raw){String[]p=x.split("\\|",4);rows.add(DateFormat.getDateTimeInstance().format(new Date(Long.parseLong(p[1])))+"\n"+(p.length>2?p[2]:""));}ListView list=new ListView(this);list.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,rows));setContentView(list);list.setOnItemClickListener((a,v,pos,id)->{long revision=Long.parseLong(raw.get(pos).split("\\|",2)[0]);repo.restoreRevision(revision,noteId);finish();});}
}
