package com.usman.notepad;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.usman.notepad.data.NoteRepository;

public class TagsActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);NoteRepository repo=new NoteRepository(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);EditText input=new EditText(this);input.setHint("Create tag");Button add=new Button(this);add.setText("Add tag");ListView list=new ListView(this);root.addView(input);root.addView(add);root.addView(list,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);Runnable refresh=()->list.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,repo.allTags()));add.setOnClickListener(v->{if(!input.getText().toString().trim().isEmpty()){repo.ensureTag(input.getText().toString());input.setText("");refresh.run();}});refresh.run();}
}
