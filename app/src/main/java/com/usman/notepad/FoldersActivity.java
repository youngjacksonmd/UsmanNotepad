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
import java.util.List;

public class FoldersActivity extends Activity {
    private NoteRepository repo; private ListView list;
    @Override protected void onCreate(Bundle b){super.onCreate(b);repo=new NoteRepository(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);Button add=new Button(this);add.setText("+ New folder");root.addView(add);list=new ListView(this);root.addView(list,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);add.setOnClickListener(v->{EditText input=new EditText(this);input.setHint("Folder name");new android.app.AlertDialog.Builder(this).setTitle("New folder").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{if(!input.getText().toString().trim().isEmpty())repo.createFolder(input.getText().toString(),null);refresh();}).show();});list.setOnItemLongClickListener((a,v,pos,id)->{String row=repo.folderRows().get(pos);long fid=Long.parseLong(row.split("\\|",2)[0]);new android.app.AlertDialog.Builder(this).setItems(new String[]{"Rename","Delete"},(d,which)->{if(which==0){EditText x=new EditText(this);new android.app.AlertDialog.Builder(this).setTitle("Rename folder").setView(x).setPositiveButton("Save",(dd,ww)->{repo.renameFolder(fid,x.getText().toString());refresh();}).setNegativeButton("Cancel",null).show();}else{repo.deleteFolder(fid);refresh();}}).show();return true;});refresh();}
    private void refresh(){List<String> rows=repo.folderRows();list.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,rows));}
}
