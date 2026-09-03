package com.usman.notepad.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.usman.notepad.data.NoteRepository;
import com.usman.notepad.model.Note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);NoteRepository repo=new NoteRepository(this);List<Note> notes=repo.allNotes();if(notes.isEmpty()){TextView t=new TextView(this);t.setText("No notes to graph yet.");t.setGravity(17);setContentView(t);return;}setContentView(new GraphView(repo,notes.size()>80?notes.subList(0,80):notes));}
    private final class GraphView extends View {private final List<Note> notes;private final List<long[]> links;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final Map<Long,float[]> pos=new HashMap<>();private float offsetX=0,offsetY=0,lastX,lastY,scale=1f;GraphView(NoteRepository repo,List<Note> n){super(GraphActivity.this);notes=new ArrayList<>(n);links=repo.allLinks();p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);}
        @Override protected void onDraw(Canvas c){super.onDraw(c);c.drawColor(Color.rgb(248,248,248));int w=getWidth(),h=getHeight();float cx=w/2f+offsetX,cy=h/2f+offsetY;float radius=Math.max(100,Math.min(w,h)*0.34f)*scale;pos.clear();for(int i=0;i<notes.size();i++){double a=2*Math.PI*i/Math.max(1,notes.size());float x=cx+(float)Math.cos(a)*radius;float y=cy+(float)Math.sin(a)*radius;pos.put(notes.get(i).id,new float[]{x,y});}p.setStrokeWidth(2);p.setColor(Color.LTGRAY);for(long[] e:links){float[] a=pos.get(e[0]),b=pos.get(e[1]);if(a!=null&&b!=null)c.drawLine(a[0],a[1],b[0],b[1],p);}for(Note n:notes){float[] q=pos.get(n.id);if(q==null)continue;p.setColor(n.locked?Color.DKGRAY:Color.rgb(70,110,160));c.drawCircle(q[0],q[1],18*scale,p);p.setColor(Color.BLACK);p.setTextSize(Math.max(10,12*scale));String s=n.title.trim().isEmpty()?"Untitled":n.title;if(s.length()>16)s=s.substring(0,16)+"…";c.drawText(s,q[0]+22*scale,q[1]+5,p);}}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){lastX=e.getX();lastY=e.getY();return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){offsetX+=e.getX()-lastX;offsetY+=e.getY()-lastY;lastX=e.getX();lastY=e.getY();invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_UP){float best=45*scale;Note hit=null;for(Note n:notes){float[] q=pos.get(n.id);if(q==null)continue;float dx=e.getX()-q[0],dy=e.getY()-q[1];float d=(float)Math.sqrt(dx*dx+dy*dy);if(d<best){best=d;hit=n;}}if(hit!=null)startActivity(new Intent(GraphActivity.this,EditorActivity.class).putExtra("note_id",hit.id));return true;}return true;}}
}
