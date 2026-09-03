package com.usman.notepad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.usman.notepad.data.NoteRepository;
import com.usman.notepad.model.Note;
import com.usman.notepad.ui.GraphView;
import java.util.List;

public class GraphActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);NoteRepository repo=new NoteRepository(this);List<Note> notes=repo.list("","all");if(notes.size()>80)notes=notes.subList(0,80);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);TextView h=new TextView(this);h.setText("Note Graph • tap a node to open");h.setTextSize(20);h.setPadding(16,16,16,16);root.addView(h);GraphView graph=new GraphView(this);graph.setData(notes,repo.links());root.addView(graph,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);graph.setOnClickListener(v->{long id=graph.consumeTappedNote();if(id>0)startActivity(new Intent(this,EditorActivity.class).putExtra("note_id",id));});}
}
