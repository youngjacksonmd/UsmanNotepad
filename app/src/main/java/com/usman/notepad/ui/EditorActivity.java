package com.usman.notepad.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.usman.notepad.capture.DrawingActivity;
import com.usman.notepad.data.NoteRepository;
import com.usman.notepad.editor.SmartExtractors;
import com.usman.notepad.model.Note;
import com.usman.notepad.privacy.AppLockManager;
import com.usman.notepad.privacy.CryptoManager;
import com.usman.notepad.reminders.ReminderScheduler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditorActivity extends Activity {
    private static final int REQ_VOICE=801,REQ_IMAGE=802,REQ_EXPORT=803,REQ_DRAW=804,REQ_NOTIFY=805;
    private NoteRepository repo;private Note note;private EditText titleInput,bodyInput,tagsInput;private TextView saveState,meta;private LinearLayout root,header,actions;private final Handler handler=new Handler();private boolean loading=true,commandDialogOpen=false,focus=false,authenticated=false;private Runnable pendingSave;

    @Override protected void onCreate(Bundle b){super.onCreate(b);repo=new NoteRepository(this);long id=getIntent().getLongExtra("note_id",-1);if(id<0)id=repo.createNote("","");note=repo.getNote(id);if(note==null){finish();return;}if(note.unlockAt>System.currentTimeMillis()){new AlertDialog.Builder(this).setTitle("Time Capsule").setMessage("This note unlocks "+fmt(note.unlockAt)+".").setPositiveButton("OK",(d,w)->finish()).setOnCancelListener(d->finish()).show();return;}if(note.locked){AppLockManager.authenticate(this,ok->{if(!ok){finish();return;}authenticated=true;decryptAndBuild();});}else{authenticated=true;buildUi();}}
    private void decryptAndBuild(){try{if(note.encryptedBody!=null&&note.encryptedIv!=null)note.body=new CryptoManager().decrypt(note.encryptedBody,note.encryptedIv);buildUi();}catch(Exception e){Toast.makeText(this,"Could not decrypt protected note.",Toast.LENGTH_LONG).show();finish();}}

    private void buildUi(){root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(10),dp(12),dp(8));header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.addView(button("←",v->{saveNow();finish();}));TextView h=new TextView(this);h.setText(note.mode.equals("scratch")?"Scratch Pad":note.mode.equals("daily")?note.title:"Note Editor");h.setTextSize(20);header.addView(h,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));saveState=new TextView(this);saveState.setText("Saved");header.addView(saveState);header.addView(button("⋮",this::moreMenu));root.addView(header);
        titleInput=new EditText(this);titleInput.setHint("Title");titleInput.setTextSize(21);titleInput.setSingleLine(true);root.addView(titleInput);tagsInput=new EditText(this);tagsInput.setHint("Tags: work, idea, personal");tagsInput.setSingleLine(true);tagsInput.setTextSize(13);root.addView(tagsInput);meta=new TextView(this);meta.setTextSize(11);meta.setPadding(4,0,4,dp(5));root.addView(meta);
        bodyInput=new EditText(this);bodyInput.setHint("Write your note here…  Type / for commands");bodyInput.setGravity(Gravity.TOP|Gravity.START);bodyInput.setTextSize(17);bodyInput.setMinLines(14);bodyInput.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);ScrollView sc=new ScrollView(this);sc.addView(bodyInput,new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT,ScrollView.LayoutParams.WRAP_CONTENT));root.addView(sc,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1));
        HorizontalScrollView hsv=new HorizontalScrollView(this);actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);actions.addView(button("Save",v->saveNow()));actions.addView(button("Mode",v->modeDialog()));actions.addView(button("Folder",v->folderDialog()));actions.addView(button("Reminder",v->reminderDialog()));actions.addView(button("History",v->startActivity(new Intent(this,HistoryActivity.class).putExtra("note_id",note.id))));actions.addView(button("Links",v->linksDialog()));actions.addView(button("Voice",v->voice()));actions.addView(button("Image",v->image()));actions.addView(button("Draw",v->draw()));hsv.addView(actions);root.addView(hsv);setContentView(root);
        titleInput.setText(note.title);bodyInput.setText(note.body);tagsInput.setText(repo.getTags(note.id));meta.setText(metaText());applyTheme();loading=false;TextWatcher w=new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){if(!loading)changed();}public void afterTextChanged(Editable e){}};titleInput.addTextChangedListener(w);bodyInput.addTextChangedListener(w);tagsInput.addTextChangedListener(w);bodyInput.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){if(!loading&&!commandDialogOpen&&(s.toString().equals("/")||s.toString().endsWith("\n/")))commandPalette();}public void afterTextChanged(Editable e){}});}

    private void changed(){saveState.setText("Saving…");if(pendingSave!=null)handler.removeCallbacks(pendingSave);pendingSave=this::saveNow;handler.postDelayed(pendingSave,700);}
    private void saveNow(){if(!authenticated||titleInput==null)return;if(pendingSave!=null)handler.removeCallbacks(pendingSave);note.title=titleInput.getText().toString();note.body=bodyInput.getText().toString();repo.setTags(note.id,tagsInput.getText().toString());try{if(note.locked){CryptoManager.EncryptedPayload p=new CryptoManager().encrypt(note.body);note.encryptedBody=p.cipherText;note.encryptedIv=p.iv;String plain=note.body;note.body="";repo.updateNote(note);note.body=plain;}else repo.updateNote(note);repo.rebuildLinksForNote(note.id,note.body);saveState.setText("Saved");meta.setText(metaText());}catch(Exception e){saveState.setText("Save failed");Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();}}
    private String metaText(){StringBuilder s=new StringBuilder(note.mode.toUpperCase(Locale.ROOT));if(note.pinned)s.append(" • PINNED");if(note.favorite)s.append(" • FAVORITE");if(note.locked)s.append(" • PROTECTED");if(note.expiresAt>0)s.append(" • Expires ").append(fmt(note.expiresAt));return s.toString();}

    private void moreMenu(View anchor){final String[] opts={note.pinned?"Unpin":"Pin",note.favorite?"Remove favorite":"Favorite",note.quickCopy?"Disable Quick Copy":"Enable Quick Copy",note.locked?"Unlock note":"Protect note","Focus mode","Theme","Time Capsule","Expiry","Quick summary","Action items","Save as template","Export .md","Graph","Delete"};new AlertDialog.Builder(this).setTitle("Note actions").setItems(opts,(d,w)->{String s=opts[w];if(s.equals("Pin")||s.equals("Unpin")){note.pinned=!note.pinned;repo.setFlag(note.id,"is_pinned",note.pinned);meta.setText(metaText());}else if(s.equals("Favorite")||s.equals("Remove favorite")){note.favorite=!note.favorite;repo.setFlag(note.id,"is_favorite",note.favorite);meta.setText(metaText());}else if(s.contains("Quick Copy")){note.quickCopy=!note.quickCopy;repo.setFlag(note.id,"quick_copy",note.quickCopy);if(note.quickCopy)copyBody();}else if(s.equals("Protect note")||s.equals("Unlock note"))toggleProtection();else if(s.equals("Focus mode"))toggleFocus();else if(s.equals("Theme"))themeDialog();else if(s.equals("Time Capsule"))timeCapsuleDialog();else if(s.equals("Expiry"))expiryDialog();else if(s.equals("Quick summary"))summaryDialog();else if(s.equals("Action items"))actionsDialog();else if(s.equals("Save as template"))saveTemplateDialog();else if(s.equals("Export .md"))exportNote();else if(s.equals("Graph"))startActivity(new Intent(this,GraphActivity.class).putExtra("note_id",note.id));else if(s.equals("Delete"))confirmDelete();}).show();}
    private void toggleProtection(){AppLockManager.authenticate(this,ok->{if(!ok)return;if(note.locked){note.locked=false;note.encryptedBody=null;note.encryptedIv=null;repo.updateEncrypted(note.id,false,bodyInput.getText().toString(),null,null);Toast.makeText(this,"Note protection removed",Toast.LENGTH_SHORT).show();}else{note.locked=true;saveNow();Toast.makeText(this,"Note encrypted locally",Toast.LENGTH_SHORT).show();}meta.setText(metaText());});}
    private void toggleFocus(){focus=!focus;header.setVisibility(focus?View.GONE:View.VISIBLE);actions.setVisibility(focus?View.GONE:View.VISIBLE);tagsInput.setVisibility(focus?View.GONE:View.VISIBLE);meta.setVisibility(focus?View.GONE:View.VISIBLE);Toast.makeText(this,focus?"Focus mode — press Back to exit":"Focus mode off",Toast.LENGTH_SHORT).show();}

    private void commandPalette(){commandDialogOpen=true;String[] commands={"/check","/date","/time","/heading","/divider","/quote","/reminder","/link"};new AlertDialog.Builder(this).setTitle("Command Palette").setItems(commands,(d,w)->{replaceTrailingSlash(commandText(commands[w]));commandDialogOpen=false;}).setOnCancelListener(d->commandDialogOpen=false).show();}
    private String commandText(String c){Date now=new Date();if(c.equals("/check"))return "☐ ";if(c.equals("/date"))return new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(now);if(c.equals("/time"))return new SimpleDateFormat("h:mm a",Locale.getDefault()).format(now);if(c.equals("/heading"))return "# ";if(c.equals("/divider"))return "──────────";if(c.equals("/quote"))return "> ";if(c.equals("/reminder")){handler.postDelayed(this::reminderDialog,200);return "";}if(c.equals("/link"))return "[[Note Title]]";return "";}
    private void replaceTrailingSlash(String text){String s=bodyInput.getText().toString();if(s.endsWith("/"))s=s.substring(0,s.length()-1);bodyInput.setText(s+text);bodyInput.setSelection(bodyInput.length());}

    private void modeDialog(){String[] modes={"Text","Checklist","Meeting","Journal","Shopping"};new AlertDialog.Builder(this).setTitle("Smart Note Mode").setItems(modes,(d,w)->{note.mode=modes[w].toLowerCase(Locale.ROOT);if(bodyInput.getText().toString().trim().isEmpty()){if(note.mode.equals("checklist")||note.mode.equals("shopping"))bodyInput.setText("☐ ");else if(note.mode.equals("meeting"))bodyInput.setText("Date:\nPeople:\nTopic:\n\nKey Points:\n• \n\nAction Items:\n☐ ");else if(note.mode.equals("journal"))bodyInput.setText("Date:\nMood:\n\nToday:\n");}saveNow();}).show();}
    private void folderDialog(){List<String[]> folders=repo.listFolders();String[] names=new String[folders.size()+1];names[0]="No folder";for(int i=0;i<folders.size();i++)names[i+1]=(folders.get(i)[2].isEmpty()?"":"↳ ")+folders.get(i)[1];new AlertDialog.Builder(this).setTitle("Move to folder").setItems(names,(d,w)->{note.folderId=w==0?null:Long.parseLong(folders.get(w-1)[0]);repo.setFolder(note.id,note.folderId);}).show();}
    private void reminderDialog(){String[] x={"10 minutes","1 hour","Tomorrow","1 week","Cancel reminder"};new AlertDialog.Builder(this).setTitle("Reminder").setItems(x,(d,w)->{if(w==4){ReminderScheduler.cancel(this,note.id);return;}long delta=w==0?10*60_000L:w==1?60*60_000L:w==2?24*60*60_000L:7*24*60*60_000L;if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ_NOTIFY);ReminderScheduler.schedule(this,note.id,note.title,System.currentTimeMillis()+delta);Toast.makeText(this,"Reminder set",Toast.LENGTH_SHORT).show();}).show();}
    private void timeCapsuleDialog(){String[] x={"Unlock in 1 hour","Tomorrow","1 week","Remove time capsule"};new AlertDialog.Builder(this).setTitle("Time Capsule").setMessage("This is a UI time lock, not a cryptographic clock lock.").setItems(x,(d,w)->{long now=System.currentTimeMillis();note.unlockAt=w==0?now+3600_000L:w==1?now+24*3600_000L:w==2?now+7*24*3600_000L:0;saveNow();if(note.unlockAt>0)Toast.makeText(this,"Capsule set. It will lock after you leave.",Toast.LENGTH_LONG).show();}).show();}
    private void expiryDialog(){String[] x={"Expire in 1 hour","1 day","1 week","30 days","Never expire"};new AlertDialog.Builder(this).setTitle("Expiring Note").setItems(x,(d,w)->{long now=System.currentTimeMillis();long[] deltas={3600_000L,24*3600_000L,7*24*3600_000L,30*24*3600_000L,0};note.expiresAt=deltas[w]==0?0:now+deltas[w];saveNow();}).show();}
    private void themeDialog(){String[] t={"system","paper","warm","dark","high-contrast"};new AlertDialog.Builder(this).setTitle("Note Theme").setItems(t,(d,w)->{note.themeKey=t[w];applyTheme();saveNow();}).show();}
    private void applyTheme(){if(root==null)return;String t=note.themeKey==null?"system":note.themeKey;int bg=Color.WHITE,fg=Color.BLACK;if(t.equals("warm"))bg=Color.rgb(255,248,225);else if(t.equals("paper"))bg=Color.rgb(250,250,245);else if(t.equals("dark")){bg=Color.rgb(33,33,33);fg=Color.WHITE;}else if(t.equals("high-contrast")){bg=Color.BLACK;fg=Color.WHITE;}root.setBackgroundColor(bg);titleInput.setTextColor(fg);bodyInput.setTextColor(fg);tagsInput.setTextColor(fg);meta.setTextColor(fg);}

    private void linksDialog(){List<Note> backlinks=repo.backlinks(note.id);StringBuilder b=new StringBuilder();if(backlinks.isEmpty())b.append("No backlinks yet.\n\nType [[Exact Note Title]] to connect notes.");else{b.append("Backlinks:\n");for(Note n:backlinks)b.append("• ").append(n.title).append("\n");}new AlertDialog.Builder(this).setTitle("Linked Notes").setMessage(b.toString()).setPositiveButton("OK",null).show();}
    private void summaryDialog(){new AlertDialog.Builder(this).setTitle("Quick summary (offline)").setMessage(SmartExtractors.quickSummary(currentDraft())).setPositiveButton("OK",null).show();}
    private void actionsDialog(){List<String> a=SmartExtractors.actionItems(currentDraft());StringBuilder s=new StringBuilder();for(String x:a){if(s.length()>0)s.append("\n");s.append(x);}new AlertDialog.Builder(this).setTitle("Action items (offline)").setMessage(a.isEmpty()?"No explicit TODO/checklist items found.":s.toString()).setPositiveButton("OK",null).show();}
    private Note currentDraft(){return new Note(note.id,titleInput.getText().toString(),bodyInput.getText().toString(),note.createdAt,System.currentTimeMillis());}
    private void saveTemplateDialog(){EditText e=new EditText(this);e.setHint("Template name");new AlertDialog.Builder(this).setTitle("Save as template").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{note.title=titleInput.getText().toString();note.body=bodyInput.getText().toString();repo.saveAsTemplate(note,e.getText().toString().trim().isEmpty()?"My Template":e.getText().toString().trim());}).show();}
    private void copyBody(){ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);if(cm!=null)cm.setPrimaryClip(ClipData.newPlainText("Usman Notepad",bodyInput.getText().toString()));Toast.makeText(this,"Copied",Toast.LENGTH_SHORT).show();}
    private void voice(){Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak your note");try{startActivityForResult(i,REQ_VOICE);}catch(Exception e){Toast.makeText(this,"Speech recognition provider is unavailable on this device.",Toast.LENGTH_LONG).show();}}
    private void image(){startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE),REQ_IMAGE);}
    private void draw(){saveNow();startActivityForResult(new Intent(this,DrawingActivity.class).putExtra("note_id",note.id),REQ_DRAW);}
    private void exportNote(){startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("text/markdown").putExtra(Intent.EXTRA_TITLE,(note.title.trim().isEmpty()?"note":note.title.replaceAll("[^a-zA-Z0-9._ -]","_"))+".md"),REQ_EXPORT);}

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data==null)return;try{if(requestCode==REQ_VOICE){ArrayList<String> r=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(r!=null&&!r.isEmpty()){int st=bodyInput.getSelectionStart();bodyInput.getText().insert(Math.max(0,st),r.get(0));}}else if(requestCode==REQ_IMAGE&&data.getData()!=null){Uri u=data.getData();File dir=new File(getFilesDir(),"attachments");dir.mkdirs();File f=new File(dir,"img_"+note.id+"_"+System.currentTimeMillis()+".bin");try(InputStream in=getContentResolver().openInputStream(u);FileOutputStream out=new FileOutputStream(f)){byte[] buf=new byte[8192];int n;while((n=in.read(buf))>0)out.write(buf,0,n);}repo.addAttachment(note.id,"image",f.getAbsolutePath());Toast.makeText(this,"Image attached locally",Toast.LENGTH_SHORT).show();}else if(requestCode==REQ_DRAW){String path=data.getStringExtra("path");if(path!=null){repo.addAttachment(note.id,"drawing",path);Toast.makeText(this,"Drawing attached",Toast.LENGTH_SHORT).show();}}else if(requestCode==REQ_EXPORT&&data.getData()!=null){try(OutputStream out=getContentResolver().openOutputStream(data.getData())){String md="# "+titleInput.getText()+"\n\n"+bodyInput.getText();out.write(md.getBytes(java.nio.charset.StandardCharsets.UTF_8));}Toast.makeText(this,"Note exported",Toast.LENGTH_SHORT).show();}}catch(Exception e){Toast.makeText(this,"Action failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
    private void confirmDelete(){new AlertDialog.Builder(this).setTitle("Move to Recycle Bin?").setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{repo.softDelete(note.id);finish();}).show();}
    @Override public void onBackPressed(){if(focus){toggleFocus();return;}saveNow();super.onBackPressed();}
    @Override protected void onPause(){super.onPause();saveNow();}
    private Button button(String text,View.OnClickListener l){Button b=new Button(this);b.setText(text);b.setOnClickListener(l);return b;}private String fmt(long t){return new SimpleDateFormat("MMM d, yyyy h:mm a",Locale.getDefault()).format(new Date(t));}private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
}
