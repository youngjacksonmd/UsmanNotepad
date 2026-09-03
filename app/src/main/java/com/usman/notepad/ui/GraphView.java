package com.usman.notepad.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import com.usman.notepad.model.Note;
import java.util.ArrayList;
import java.util.List;

public class GraphView extends View {
    private final Paint edge=new Paint(Paint.ANTI_ALIAS_FLAG); private final Paint node=new Paint(Paint.ANTI_ALIAS_FLAG); private List<Note> notes=new ArrayList<>(); private List<long[]> links=new ArrayList<>(); private long tapped=-1;
    public GraphView(Context c){super(c);edge.setStrokeWidth(2f);edge.setColor(0xff999999);node.setColor(0xff333333);}
    public void setData(List<Note> n,List<long[]> l){notes=n==null?new ArrayList<>():n;links=l==null?new ArrayList<>():l;invalidate();}
    public long consumeTappedNote(){long x=tapped;tapped=-1;return x;}
    private float[] point(int index){int count=Math.max(1,notes.size());float cx=getWidth()/2f,cy=getHeight()/2f,r=Math.max(60,Math.min(getWidth(),getHeight())*.38f);double a=(Math.PI*2*index)/count;return new float[]{cx+(float)Math.cos(a)*r,cy+(float)Math.sin(a)*r};}
    private int indexOf(long id){for(int i=0;i<notes.size();i++)if(notes.get(i).id==id)return i;return -1;}
    @Override protected void onDraw(Canvas c){super.onDraw(c);for(long[] l:links){int a=indexOf(l[0]),b=indexOf(l[1]);if(a<0||b<0)continue;float[]p=point(a),q=point(b);c.drawLine(p[0],p[1],q[0],q[1],edge);}for(int i=0;i<notes.size();i++){float[]p=point(i);c.drawCircle(p[0],p[1],14,node);}}
    @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float best=48*getResources().getDisplayMetrics().density;int bi=-1;for(int i=0;i<notes.size();i++){float[]p=point(i);float dx=e.getX()-p[0],dy=e.getY()-p[1],d=(float)Math.sqrt(dx*dx+dy*dy);if(d<best){best=d;bi=i;}}if(bi>=0){tapped=notes.get(bi).id;performClick();}return true;} @Override public boolean performClick(){super.performClick();return true;}
}
