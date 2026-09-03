package com.usman.notepad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.usman.notepad.data.NoteRepository;
import com.usman.notepad.model.Note;
import java.util.ArrayList;
import java.util.List;

public class TemplatesActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);NoteRepository repo=new NoteRepository(this);List<String> raw=repo.templates();List<String> labels=new ArrayList<>();for(String s:raw){String[]p=s.split("\\|",4);labels.add(p.length>1?p[1]:s);}ListView list=new ListView(this);list.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,labels));setContentView(list);list.setOnItemClickListener((a,v,pos,id)->{String[]p=raw.get(pos).split("\\|",4);Note n=new Note();n.title=p[1];n.mode=p.length>2?p[2]:"text";n.body=p.length>3?p[3]:"";repo.save(n,false);startActivity(new Intent(this,EditorActivity.class).putExtra("note_id",n.id));});}
}
