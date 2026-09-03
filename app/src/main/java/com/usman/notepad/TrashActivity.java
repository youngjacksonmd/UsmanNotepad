package com.usman.notepad;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.usman.notepad.data.NoteRepository;
import com.usman.notepad.model.Note;
import java.util.ArrayList;
import java.util.List;

public class TrashActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);NoteRepository repo=new NoteRepository(this);List<Note> notes=repo.list("","trash");List<String> rows=new ArrayList<>();for(Note n:notes)rows.add(n.displayTitle()+"\nTap: restore • Hold: delete forever");ListView list=new ListView(this);list.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,rows));setContentView(list);list.setOnItemClickListener((a,v,pos,id)->{repo.restore(notes.get(pos).id);recreate();});list.setOnItemLongClickListener((a,v,pos,id)->{repo.purge(notes.get(pos).id);recreate();return true;});}
}
