package com.usman.notepad.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.usman.notepad.backup.BackupManager;
import com.usman.notepad.data.NoteRepository;
import com.usman.notepad.model.Note;
import com.usman.notepad.privacy.AppLockManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_EXPORT_ALL=701,REQ_IMPORT=702;
    private NoteRepository repo;private ListView list;private TextView empty;private SearchView search;private String filter="all";private List<Note> notes=new ArrayList<>();private boolean appAuthenticated=false;

    @Override protected void onCreate(Bundle b){super.onCreate(b);repo=new NoteRepository(this);buildUi();if(AppLockManager.isEnabled(this)){AppLockManager.authenticate(this,ok->{appAuthenticated=ok;if(ok)refresh();else finish();});}else{appAuthenticated=true;refresh();}}

    private void buildUi(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(12),dp(12),dp(12),dp(8));LinearLayout titleRow=new LinearLayout(this);titleRow.setGravity(Gravity.CENTER_VERTICAL);TextView title=new TextView(this);title.setText("Usman Notepad  V2.00");title.setTextSize(25);title.setTypeface(Typeface.DEFAULT_BOLD);titleRow.addView(title,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));Button menu=new Button(this);menu.setText("☰");menu.setOnClickListener(this::showMainMenu);titleRow.addView(menu);root.addView(titleRow);
        search=new SearchView(this);search.setQueryHint("Search titles and note text");search.setIconifiedByDefault(false);search.setOnQueryTextListener(new SearchView.OnQueryTextListener(){public boolean onQueryTextSubmit(String q){refresh();return true;}public boolean onQueryTextChange(String q){refresh();return true;}});root.addView(search);
        HorizontalScrollView hsv=new HorizontalScrollView(this);LinearLayout quick=new LinearLayout(this);quick.setOrientation(LinearLayout.HORIZONTAL);quick.addView(btn("+ New",v->openNote(repo.createNote("",""))));quick.addView(btn("Scratch",v->openNote(repo.getOrCreateScratch())));quick.addView(btn("Daily",v->openNote(repo.getOrCreateDaily(new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(new Date())))));quick.addView(btn("Templates",v->chooseTemplate()));quick.addView(btn("Folders",v->foldersDialog()));quick.addView(btn("Tags",v->tagsDialog()));hsv.addView(quick);root.addView(hsv);
        TextView hint=new TextView(this);hint.setText("Tap note to edit • swipe right = pin • swipe left = archive • hold = actions");hint.setTextSize(12);hint.setPadding(2,dp(6),2,dp(6));root.addView(hint);empty=new TextView(this);empty.setGravity(Gravity.CENTER);empty.setTextSize(16);list=new ListView(this);root.addView(empty,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1));root.addView(list,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1));list.setOnItemClickListener((p,v,pos,id)->openNote(notes.get(pos).id));list.setOnItemLongClickListener((p,v,pos,id)->{noteActions(v,notes.get(pos));return true;});setContentView(root);}

    private Button btn(String text,View.OnClickListener l){Button b=new Button(this);b.setText(text);b.setOnClickListener(l);return b;}
    private void showMainMenu(View anchor){PopupMenu p=new PopupMenu(this,anchor);String[] items={"All Notes","Pinned","Favorites","Inbox","Archived","Recycle Bin","Graph","Export backup","Import backup / text",AppLockManager.isEnabled(this)?"Disable app lock":"Enable app lock"};for(String s:items)p.getMenu().add(s);p.setOnMenuItemClickListener(item->{String s=item.getTitle().toString();if(s.equals("Graph"))startActivity(new Intent(this,GraphActivity.class));else if(s.equals("Export backup"))exportAll();else if(s.equals("Import backup / text"))importFile();else if(s.contains("app lock"))toggleAppLock();else{if(s.equals("All Notes"))filter="all";else if(s.equals("Pinned"))filter="pinned";else if(s.equals("Favorites"))filter="favorites";else if(s.equals("Inbox"))filter="inbox";else if(s.equals("Archived"))filter="archived";else filter="deleted";refresh();}return true;});p.show();}
    private void toggleAppLock(){if(AppLockManager.isEnabled(this)){AppLockManager.authenticate(this,ok->{if(ok){AppLockManager.setEnabled(this,false);Toast.makeText(this,"App lock disabled",Toast.LENGTH_SHORT).show();}});}else{AppLockManager.setupPin(this,ok->{if(ok){AppLockManager.setEnabled(this,true);Toast.makeText(this,"App lock enabled",Toast.LENGTH_SHORT).show();}});}}
    private void noteActions(View anchor,Note n){PopupMenu p=new PopupMenu(this,anchor);if(n.deleted){p.getMenu().add("Restore");p.getMenu().add("Delete permanently");}else{p.getMenu().add(n.pinned?"Unpin":"Pin");p.getMenu().add(n.favorite?"Remove favorite":"Favorite");p.getMenu().add(n.archived?"Unarchive":"Archive");p.getMenu().add("Quick Copy");p.getMenu().add("Delete");}p.setOnMenuItemClickListener(i->{String s=i.getTitle().toString();if(s.equals("Restore"))repo.restore(n.id);else if(s.equals("Delete permanently"))confirmPermanent(n);else if(s.equals("Pin")||s.equals("Unpin"))repo.setFlag(n.id,"is_pinned",!n.pinned);else if(s.equals("Favorite")||s.equals("Remove favorite"))repo.setFlag(n.id,"is_favorite",!n.favorite);else if(s.equals("Archive")||s.equals("Unarchive"))repo.setFlag(n.id,"is_archived",!n.archived);else if(s.equals("Quick Copy"))copy(n.body);else if(s.equals("Delete"))repo.softDelete(n.id);refresh();return true;});p.show();}
    private void confirmPermanent(Note n){new AlertDialog.Builder(this).setTitle("Delete permanently?").setMessage("This cannot be undone.").setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{repo.permanentDelete(n.id);refresh();}).show();}

    private void foldersDialog(){List<String[]> folders=repo.listFolders();List<String> labels=new ArrayList<>();labels.add("+ Create folder");for(String[] f:folders)labels.add((f[2].isEmpty()?"":"↳ ")+f[1]);new AlertDialog.Builder(this).setTitle("Folders").setItems(labels.toArray(new String[0]),(d,which)->{if(which==0)createFolderDialog(folders);else{long id=Long.parseLong(folders.get(which-1)[0]);notes=repo.notesForFolder(id);showNotes("Folder: "+folders.get(which-1)[1]);}}).show();}
    private void createFolderDialog(List<String[]> existing){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),0,dp(16),0);EditText name=new EditText(this);name.setHint("Folder name");box.addView(name);EditText parent=new EditText(this);parent.setHint("Optional parent folder ID");parent.setInputType(InputType.TYPE_CLASS_NUMBER);box.addView(parent);StringBuilder htxt=new StringBuilder("Parent IDs: ");for(String[] f:existing)htxt.append(f[0]).append("=").append(f[1]).append("  ");TextView h=new TextView(this);h.setText(htxt);h.setTextSize(11);box.addView(h);new AlertDialog.Builder(this).setTitle("Create nested folder").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{if(name.getText().toString().trim().isEmpty())return;Long par=null;try{if(!parent.getText().toString().trim().isEmpty())par=Long.parseLong(parent.getText().toString().trim());}catch(Exception ignored){}repo.createFolder(name.getText().toString(),par);}).show();}
    private void tagsDialog(){List<String> tags=repo.listTags();if(tags.isEmpty()){Toast.makeText(this,"Add tags from a note's editor.",Toast.LENGTH_SHORT).show();return;}new AlertDialog.Builder(this).setTitle("Tags").setItems(tags.toArray(new String[0]),(d,w)->{notes=repo.notesForTag(tags.get(w));showNotes("#"+tags.get(w));}).show();}
    private void chooseTemplate(){List<String[]> t=repo.templates();String[] names=new String[t.size()];for(int i=0;i<t.size();i++)names[i]=t.get(i)[1];new AlertDialog.Builder(this).setTitle("New from template").setItems(names,(d,w)->openNote(repo.createFromTemplate(Long.parseLong(t.get(w)[0])))).show();}
    private void openNote(long id){startActivity(new Intent(this,EditorActivity.class).putExtra("note_id",id));}
    private void refresh(){if(!appAuthenticated)return;String q=search==null?"":search.getQuery().toString();notes=repo.listNotes(q,filter);showNotes(filter.equals("all")?"All Notes":filter);}
    private void showNotes(String label){boolean none=notes.isEmpty();empty.setText(none?"No notes in "+label+".":label+" • "+notes.size()+" notes");empty.setVisibility(none?View.VISIBLE:View.GONE);list.setVisibility(none?View.GONE:View.VISIBLE);list.setAdapter(new NoteAdapter());}
    @Override protected void onResume(){super.onResume();if(repo!=null&&appAuthenticated)refresh();}
    private void copy(String text){ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);if(cm!=null)cm.setPrimaryClip(ClipData.newPlainText("Usman Notepad",text==null?"":text));Toast.makeText(this,"Copied",Toast.LENGTH_SHORT).show();}
    private void exportAll(){startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").putExtra(Intent.EXTRA_TITLE,"UsmanNotepad-V2-backup.json"),REQ_EXPORT_ALL);}
    private void importFile(){startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE),REQ_IMPORT);}
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data==null||data.getData()==null)return;Uri u=data.getData();try{if(requestCode==REQ_EXPORT_ALL){try(OutputStream out=getContentResolver().openOutputStream(u)){BackupManager.exportAll(this,out);}Toast.makeText(this,"Backup exported",Toast.LENGTH_SHORT).show();}else if(requestCode==REQ_IMPORT){String type=getContentResolver().getType(u);try(InputStream in=getContentResolver().openInputStream(u)){BackupManager.ImportResult r;if(type!=null&&(type.contains("text/plain")||type.contains("markdown")))r=BackupManager.importText(this,in);else r=BackupManager.importBackup(this,in);Toast.makeText(this,"Imported "+r.imported+" notes",Toast.LENGTH_LONG).show();}refresh();}}catch(Exception e){Toast.makeText(this,"File error: "+e.getMessage(),Toast.LENGTH_LONG).show();}}

    private final class NoteAdapter extends BaseAdapter {public int getCount(){return notes.size();}public Object getItem(int p){return notes.get(p);}public long getItemId(int p){return notes.get(p).id;}public View getView(int pos,View convert,ViewGroup parent){Note n=notes.get(pos);LinearLayout row=new LinearLayout(MainActivity.this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(dp(12),dp(10),dp(12),dp(10));TextView t=new TextView(MainActivity.this);t.setTextSize(17);t.setTypeface(Typeface.DEFAULT_BOLD);String icons=(n.pinned?"📌 ":"")+(n.favorite?"★ ":"")+(n.locked?"🔒 ":"")+(n.inbox?"📥 ":"");t.setText(icons+(n.title.trim().isEmpty()?"Untitled note":n.title));TextView pv=new TextView(MainActivity.this);pv.setTextSize(13);String preview=n.locked?"Protected note":n.body.replace('\n',' ').trim();if(preview.length()>90)preview=preview.substring(0,90)+"…";pv.setText(preview+"\n"+new SimpleDateFormat("MMM d, h:mm a",Locale.getDefault()).format(new Date(n.updatedAt)));row.addView(t);row.addView(pv);row.setOnTouchListener(new View.OnTouchListener(){float downX;public boolean onTouch(View v,MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();return false;}if(e.getAction()==MotionEvent.ACTION_UP){float dx=e.getX()-downX;if(dx>180){repo.setFlag(n.id,"is_pinned",!n.pinned);refresh();return true;}if(dx<-180&&!n.deleted){repo.setFlag(n.id,"is_archived",!n.archived);refresh();return true;}}return false;}});return row;}}
    private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
}
