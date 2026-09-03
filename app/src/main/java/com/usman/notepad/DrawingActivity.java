package com.usman.notepad;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import com.usman.notepad.ui.DrawingView;
import java.io.File;
import java.io.FileOutputStream;

public class DrawingActivity extends Activity {
    private DrawingView drawing;
    @Override protected void onCreate(Bundle b){super.onCreate(b);long noteId=getIntent().getLongExtra("note_id",-1);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);drawing=new DrawingView(this);root.addView(drawing,new LinearLayout.LayoutParams(-1,0,1));LinearLayout actions=new LinearLayout(this);Button undo=new Button(this);undo.setText("Undo");Button clear=new Button(this);clear.setText("Clear");Button save=new Button(this);save.setText("Save drawing");actions.addView(undo,new LinearLayout.LayoutParams(0,-2,1));actions.addView(clear,new LinearLayout.LayoutParams(0,-2,1));actions.addView(save,new LinearLayout.LayoutParams(0,-2,1));root.addView(actions);setContentView(root);undo.setOnClickListener(v->drawing.undo());clear.setOnClickListener(v->drawing.clearAll());save.setOnClickListener(v->{try{drawing.setDrawingCacheEnabled(true);android.graphics.Bitmap bmp=android.graphics.Bitmap.createBitmap(drawing.getWidth(),drawing.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);android.graphics.Canvas c=new android.graphics.Canvas(bmp);drawing.draw(c);File dir=new File(getFilesDir(),"drawings");dir.mkdirs();File f=new File(dir,"drawing-"+System.currentTimeMillis()+".png");try(FileOutputStream out=new FileOutputStream(f)){bmp.compress(android.graphics.Bitmap.CompressFormat.PNG,95,out);}new com.usman.notepad.data.NoteRepository(this).addAttachment(noteId,"drawing",f.getAbsolutePath());finish();}catch(Exception e){new android.app.AlertDialog.Builder(this).setMessage("Could not save drawing: "+e.getMessage()).setPositiveButton("OK",null).show();}});}
}
