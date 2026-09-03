package com.usman.notepad.capture;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.usman.notepad.data.NoteRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ShareCaptureActivity extends Activity {
    private NoteRepository repo;private Uri imageUri;
    @Override protected void onCreate(Bundle b){super.onCreate(b);repo=new NoteRepository(this);Intent in=getIntent();String text=in.getStringExtra(Intent.EXTRA_TEXT);if(text==null)text="";if(in.getType()!=null&&in.getType().startsWith("image/"))imageUri=in.getParcelableExtra(Intent.EXTRA_STREAM);show(text);}
    private void show(String shared){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(24,0,24,0);EditText title=new EditText(this);title.setHint("Title");box.addView(title);EditText body=new EditText(this);body.setHint("Shared content");body.setMinLines(6);body.setText(shared);box.addView(body);new AlertDialog.Builder(this).setTitle("Save to UsmanNotepad Inbox").setView(box).setNegativeButton("Cancel",(d,w)->finish()).setPositiveButton("Save",(d,w)->{long id=repo.createNote(title.getText().toString(),body.getText().toString());repo.setInbox(id,true);if(imageUri!=null)copyImage(id,imageUri);Toast.makeText(this,"Saved to Inbox",Toast.LENGTH_SHORT).show();finish();}).setOnCancelListener(d->finish()).show();}
    private void copyImage(long noteId,Uri u){try{File dir=new File(getFilesDir(),"attachments");dir.mkdirs();File f=new File(dir,"shared_"+noteId+"_"+System.currentTimeMillis()+".bin");try(InputStream in=getContentResolver().openInputStream(u);FileOutputStream out=new FileOutputStream(f)){byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);}repo.addAttachment(noteId,"image",f.getAbsolutePath());}catch(Exception ignored){}}
}
