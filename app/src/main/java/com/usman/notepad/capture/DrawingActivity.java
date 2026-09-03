package com.usman.notepad.capture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DrawingActivity extends Activity {
    private DrawingView drawing;private long noteId;
    @Override protected void onCreate(Bundle b){super.onCreate(b);noteId=getIntent().getLongExtra("note_id",-1);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);drawing=new DrawingView();root.addView(drawing,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1));LinearLayout bar=new LinearLayout(this);Button undo=new Button(this);undo.setText("Undo");undo.setOnClickListener(v->drawing.undo());bar.addView(undo);Button clear=new Button(this);clear.setText("Clear");clear.setOnClickListener(v->drawing.clear());bar.addView(clear);Button save=new Button(this);save.setText("Save");save.setOnClickListener(v->save());bar.addView(save);root.addView(bar);setContentView(root);}
    private void save(){try{File dir=new File(getFilesDir(),"drawings");dir.mkdirs();File f=new File(dir,"drawing_"+noteId+"_"+System.currentTimeMillis()+".png");Bitmap b=Bitmap.createBitmap(Math.max(1,drawing.getWidth()),Math.max(1,drawing.getHeight()),Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);drawing.draw(c);try(FileOutputStream out=new FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,out);}setResult(RESULT_OK,new Intent().putExtra("path",f.getAbsolutePath()));finish();}catch(Exception e){Toast.makeText(this,"Could not save drawing",Toast.LENGTH_LONG).show();}}
    private final class DrawingView extends View {private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final List<Path> paths=new ArrayList<>();private Path current;DrawingView(){super(DrawingActivity.this);paint.setColor(Color.BLACK);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(6);paint.setStrokeCap(Paint.Cap.ROUND);setBackgroundColor(Color.WHITE);}@Override protected void onDraw(Canvas c){super.onDraw(c);for(Path p:paths)c.drawPath(p,paint);if(current!=null)c.drawPath(current,paint);}@Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){current=new Path();current.moveTo(e.getX(),e.getY());invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){if(current!=null)current.lineTo(e.getX(),e.getY());invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_UP){if(current!=null){current.lineTo(e.getX(),e.getY());paths.add(current);current=null;}invalidate();return true;}return true;}void undo(){if(!paths.isEmpty())paths.remove(paths.size()-1);invalidate();}void clear(){paths.clear();current=null;invalidate();}}
}
