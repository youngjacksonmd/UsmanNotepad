package com.usman.notepad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.usman.notepad.data.NoteRepository;
import com.usman.notepad.model.Note;

public class ShareCaptureActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);Intent in=getIntent();String text=in.getStringExtra(Intent.EXTRA_TEXT);Note n=new Note();n.title="Inbox";n.body=text==null?"":text;n.inbox=true;new NoteRepository(this).save(n,false);startActivity(new Intent(this,EditorActivity.class).putExtra("note_id",n.id));finish();}
}
