package com.usman.notepad.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG); private final List<Path> paths=new ArrayList<>(); private Path current;
    public DrawingView(Context c){super(c);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(6f);paint.setStrokeCap(Paint.Cap.ROUND);setBackgroundColor(0xfffaf8f2);}
    @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);for(Path p:paths)canvas.drawPath(p,paint);if(current!=null)canvas.drawPath(current,paint);}
    @Override public boolean onTouchEvent(MotionEvent e){float x=e.getX(),y=e.getY();switch(e.getAction()){case MotionEvent.ACTION_DOWN:current=new Path();current.moveTo(x,y);invalidate();return true;case MotionEvent.ACTION_MOVE:if(current!=null)current.lineTo(x,y);invalidate();return true;case MotionEvent.ACTION_UP:if(current!=null){current.lineTo(x,y);paths.add(current);current=null;}invalidate();return true;default:return true;}}
    public void undo(){if(!paths.isEmpty())paths.remove(paths.size()-1);invalidate();} public void clearAll(){paths.clear();current=null;invalidate();}
}
