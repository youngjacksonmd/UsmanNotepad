package com.usman.notepad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.usman.notepad.data.NoteRepository;
import com.usman.notepad.io.BackupManager;
import com.usman.notepad.model.Note;
import com.usman.notepad.security.AppLock;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SettingsActivity extends Activity {
    private static final int EXPORT_BACKUP=801,IMPORT_BACKUP=802,IMPORT_TEXT=803; private NoteRepository repo;
    @Override protected void onCreate(Bundle b){super.onCreate(b);repo=new NoteRepository(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(24,24,24,24);TextView h=new TextView(this);h.setText("Usman Notepad V2.00 Settings");h.setTextSize(24);root.addView(h);EditText pin=new EditText(this);pin.setHint(AppLock.hasPin(this)?"Change PIN":"Set PIN");root.addView(pin);Button save=new Button(this);save.setText("Save PIN");root.addView(save);CheckBox lock=new CheckBox(this);lock.setText("App lock on launch");lock.setChecked(AppLock.appLockEnabled(this));root.addView(lock);CheckBox hide=new CheckBox(this);hide.setText("Hide titles of locked notes");hide.setChecked(AppLock.hideLockedTitles(this));root.addView(hide);Button device=new Button(this);device.setText("Test device credential / biometric");root.addView(device);Button export=new Button(this);export.setText("Export structured local backup (.json)");root.addView(export);Button restore=new Button(this);restore.setText("Restore app backup");root.addView(restore);Button importText=new Button(this);importText.setText("Import .txt / .md note");root.addView(importText);TextView privacy=new TextView(this);privacy.setText("Privacy: no account, no analytics, no OpenAI/external AI API. Protected notes use Android Keystore encryption. Backup includes notes, folders, tags, links, history and reminder metadata; attached media files stay local and should be copied separately when moving devices.");privacy.setPadding(0,24,0,0);root.addView(privacy);setContentView(root);save.setOnClickListener(v->{AppLock.setPin(this,pin.getText().toString());pin.setText("");Toast.makeText(this,"PIN saved",Toast.LENGTH_SHORT).show();});lock.setOnCheckedChangeListener((b1,c)->AppLock.setAppLock(this,c));hide.setOnCheckedChangeListener((b1,c)->AppLock.setHideLockedTitles(this,c));device.setOnClickListener(v->AppLock.requestDeviceCredential(this,700));export.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").putExtra(Intent.EXTRA_TITLE,"UsmanNotepad-V2-Backup.json");startActivityForResult(i,EXPORT_BACKUP);});restore.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,IMPORT_BACKUP);});importText.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("text/*").addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,IMPORT_TEXT);});}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data==null||data.getData()==null)return;try{if(requestCode==EXPORT_BACKUP){String raw=BackupManager.exportAll(this);try(OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(raw.getBytes(StandardCharsets.UTF_8));}Toast.makeText(this,"Backup exported",Toast.LENGTH_SHORT).show();}else if(requestCode==IMPORT_BACKUP){int count=BackupManager.restoreAll(this,read(data));Toast.makeText(this,"Restored "+count+" notes and related metadata",Toast.LENGTH_LONG).show();}else if(requestCode==IMPORT_TEXT){Note n=new Note();n.title=data.getData().getLastPathSegment()==null?"Imported note":data.getData().getLastPathSegment();n.body=read(data);repo.save(n,false);startActivity(new Intent(this,EditorActivity.class).putExtra("note_id",n.id));}}catch(Exception e){Toast.makeText(this,"Operation failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
    private String read(Intent data)throws Exception{try(InputStream in=getContentResolver().openInputStream(data.getData());ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[]buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);return out.toString(StandardCharsets.UTF_8.name());}}
}
