package com.usman.notepad;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.usman.notepad.data.NoteRepository;
import com.usman.notepad.model.Note;
import com.usman.notepad.security.AppLock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private NoteRepository repo; private ListView list; private EditText search; private TextView empty; private List<Note> notes=new ArrayList<>(); private String filter="all"; private float downX; private int downPosition=-1;
    @Override protected void onCreate(Bundle b){super.onCreate(b);repo=new NoteRepository(this);if(AppLock.appLockEnabled(this)&&AppLock.hasPin(this)){AppLock.promptPin(this,"Unlock Usman Notepad",this::buildUi);}else buildUi();}
    private void buildUi(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(14),dp(12),dp(10));TextView title=new TextView(this);title.setText("Usman Notepad V2.00");title.setTextSize(27);root.addView(title);search=new EditText(this);search.setHint("Search notes, folders, tags…");search.setText(getIntent().getStringExtra("search_query") == null ? "" : getIntent().getStringExtra("search_query"));root.addView(search);LinearLayout quick=new LinearLayout(this);quick.setOrientation(LinearLayout.HORIZONTAL);String[] labels={"+ Note","Scratch","Daily","More"};for(String label:labels){Button x=new Button(this);x.setText(label);quick.addView(x,new LinearLayout.LayoutParams(0,-2,1));if(label.equals("+ Note"))x.setOnClickListener(v->openNew());else if(label.equals("Scratch"))x.setOnClickListener(v->open(repo.getOrCreateScratch().id));else if(label.equals("Daily"))x.setOnClickListener(v->openDaily());else x.setOnClickListener(v->showMore());}root.addView(quick);LinearLayout filters=new LinearLayout(this);filters.setOrientation(LinearLayout.HORIZONTAL);for(String f:new String[]{"All","Pinned","Favorites","Inbox"}){Button x=new Button(this);x.setText(f);filters.addView(x,new LinearLayout.LayoutParams(0,-2,1));x.setOnClickListener(v->{filter=f.toLowerCase(Locale.ROOT);refresh();});}root.addView(filters);empty=new TextView(this);empty.setText("No notes here yet ✍");empty.setTextSize(17);empty.setPadding(8,32,8,32);root.addView(empty);list=new ListView(this);root.addView(list,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){refresh();}public void afterTextChanged(Editable e){}});list.setOnItemClickListener((a,v,pos,id)->{Note n=notes.get(pos);if(n.quickCopy){ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(cm!=null)cm.setPrimaryClip(ClipData.newPlainText(n.displayTitle(),n.locked?"":n.body));android.widget.Toast.makeText(this,n.locked?"Locked note not copied":"Copied",android.widget.Toast.LENGTH_SHORT).show();}else open(n.id);});list.setOnItemLongClickListener((a,v,pos,id)->{showActions(notes.get(pos));return true;});list.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downPosition=list.pointToPosition((int)e.getX(),(int)e.getY());}else if(e.getAction()==MotionEvent.ACTION_UP&&downPosition>=0&&downPosition<notes.size()){float dx=e.getX()-downX;if(Math.abs(dx)>dp(90)){Note n=notes.get(downPosition);if(dx>0){repo.togglePin(n.id);android.widget.Toast.makeText(this,"Pin toggled",android.widget.Toast.LENGTH_SHORT).show();}else{repo.archive(n.id);android.widget.Toast.makeText(this,"Archived",android.widget.Toast.LENGTH_SHORT).show();}refresh();return true;}}return false;});handleLaunchAction();refresh();}
    private void handleLaunchAction(){String a=getIntent().getStringExtra("widget_action");if("new".equals(a))openNew();else if("scratch".equals(a))open(repo.getOrCreateScratch().id);else if("daily".equals(a))openDaily();}
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);if(list!=null)handleLaunchAction();}
    @Override protected void onResume(){super.onResume();if(list!=null)refresh();}
    private void openNew(){startActivity(new Intent(this,EditorActivity.class));} private void open(long id){startActivity(new Intent(this,EditorActivity.class).putExtra("note_id",id));} private void openDaily(){String d=new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(new Date());open(repo.getOrCreateDaily(d).id);}
    private void refresh(){notes=repo.list(search==null?"":search.getText().toString(),filter);List<String> rows=new ArrayList<>();for(Note n:notes){String title=n.locked&&AppLock.hideLockedTitles(this)?"🔒 Locked note":n.displayTitle();String preview=n.locked?"🔒 Protected":(n.body==null?"":n.body.trim().replace('\n',' '));if(preview.length()>90)preview=preview.substring(0,90)+"…";rows.add((n.pinned?"📌 ":"")+(n.favorite?"★ ":"")+title+"\n"+preview+(n.quickCopy?"\n⚡ Quick Copy":""));}list.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,rows));empty.setVisibility(notes.isEmpty()?View.VISIBLE:View.GONE);list.setVisibility(notes.isEmpty()?View.GONE:View.VISIBLE);}
    private void showActions(Note n){String[] a={n.pinned?"Unpin":"Pin",n.favorite?"Unfavorite":"Favorite",n.quickCopy?"Disable Quick Copy":"Quick Copy","Archive","Delete","Open"};new android.app.AlertDialog.Builder(this).setTitle(n.displayTitle()).setItems(a,(d,w)->{if(w==0)repo.togglePin(n.id);else if(w==1)repo.toggleFavorite(n.id);else if(w==2)repo.toggleQuickCopy(n.id);else if(w==3)repo.archive(n.id);else if(w==4)repo.trash(n.id);else open(n.id);refresh();}).show();}
    private void showMore(){String[] a={"Folders","Tags","Templates","Recycle Bin","Note Graph","Settings / Backup","Archived"};new android.app.AlertDialog.Builder(this).setTitle("Library").setItems(a,(d,w)->{if(w==0)startActivity(new Intent(this,FoldersActivity.class));else if(w==1)startActivity(new Intent(this,TagsActivity.class));else if(w==2)startActivity(new Intent(this,TemplatesActivity.class));else if(w==3)startActivity(new Intent(this,TrashActivity.class));else if(w==4)startActivity(new Intent(this,GraphActivity.class));else if(w==5)startActivity(new Intent(this,SettingsActivity.class));else{filter="archived";refresh();}}).show();}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
