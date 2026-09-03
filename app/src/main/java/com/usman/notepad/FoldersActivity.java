package com.usman.notepad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.usman.notepad.data.NoteRepository;
import java.util.ArrayList;
import java.util.List;

public class FoldersActivity extends Activity {
    private NoteRepository repo; private ListView list; private List<String> rows=new ArrayList<>();
    @Override protected void onCreate(Bundle b){super.onCreate(b);repo=new NoteRepository(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);Button add=new Button(this);add.setText("+ New root folder");root.addView(add);list=new ListView(this);root.addView(list,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);add.setOnClickListener(v->createFolder(null));list.setOnItemClickListener((a,v,pos,id)->{String[]p=rows.get(pos).split("\\|",3);startActivity(new Intent(this,MainActivity.class).putExtra("search_query",p.length>1?p[1]:""));});list.setOnItemLongClickListener((a,v,pos,id)->{String[]p=rows.get(pos).split("\\|",3);long fid=Long.parseLong(p[0]);new android.app.AlertDialog.Builder(this).setTitle(p.length>1?p[1]:"Folder").setItems(new String[]{"Create child folder","Rename","Delete"},(d,which)->{if(which==0)createFolder(fid);else if(which==1){EditText x=new EditText(this);x.setText(p.length>1?p[1]:"");new android.app.AlertDialog.Builder(this).setTitle("Rename folder").setView(x).setPositiveButton("Save",(dd,ww)->{repo.renameFolder(fid,x.getText().toString());refresh();}).setNegativeButton("Cancel",null).show();}else{repo.deleteFolder(fid);refresh();}}).show();return true;});refresh();}
    private void createFolder(Long parent){EditText input=new EditText(this);input.setHint("Folder name");new android.app.AlertDialog.Builder(this).setTitle(parent==null?"New folder":"New child folder").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{if(!input.getText().toString().trim().isEmpty())repo.createFolder(input.getText().toString(),parent);refresh();}).show();}
    private void refresh(){rows=repo.folderRows();List<String>labels=new ArrayList<>();for(String row:rows){String[]p=row.split("\\|",3);String name=p.length>1?p[1]:row;String parent=p.length>2?p[2]:"";labels.add((parent.isEmpty()?"📁 ":"↳ 📁 ")+name);}list.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,labels));}
}
