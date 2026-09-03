package com.usman.notepad.privacy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class AppLockManager {
    private static final String PREF="privacy";
    private static final String PIN="pin_hash";
    private static final String ENABLED="app_lock";
    private AppLockManager(){}

    public interface AuthCallback { void onResult(boolean ok); }

    public static boolean isEnabled(Context c){ return c.getSharedPreferences(PREF,Context.MODE_PRIVATE).getBoolean(ENABLED,false); }
    public static boolean hasPin(Context c){ return !c.getSharedPreferences(PREF,Context.MODE_PRIVATE).getString(PIN,"").isEmpty(); }
    public static void setEnabled(Context c,boolean enabled){ c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putBoolean(ENABLED,enabled).apply(); }

    public static void setupPin(Activity a, AuthCallback cb){
        EditText e=pinField(a,"Create 4+ digit PIN");
        new AlertDialog.Builder(a).setTitle("Set privacy PIN").setView(e)
                .setNegativeButton("Cancel",(d,w)->cb.onResult(false))
                .setPositiveButton("Save",(d,w)->{
                    String p=e.getText().toString();
                    if(p.length()<4){ Toast.makeText(a,"PIN must be at least 4 digits",Toast.LENGTH_SHORT).show(); cb.onResult(false); return; }
                    a.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString(PIN,hash(p)).apply();
                    cb.onResult(true);
                }).show();
    }

    public static void authenticate(Activity a,AuthCallback cb){
        if(!hasPin(a)){ setupPin(a,cb); return; }
        EditText e=pinField(a,"Enter PIN");
        new AlertDialog.Builder(a).setTitle("Unlock Usman Notepad").setView(e)
                .setNegativeButton("Cancel",(d,w)->cb.onResult(false))
                .setPositiveButton("Unlock",(d,w)->{
                    String expected=a.getSharedPreferences(PREF,Context.MODE_PRIVATE).getString(PIN,"");
                    boolean ok=expected.equals(hash(e.getText().toString()));
                    if(!ok) Toast.makeText(a,"Wrong PIN",Toast.LENGTH_SHORT).show();
                    cb.onResult(ok);
                }).show();
    }

    private static EditText pinField(Context c,String hint){
        EditText e=new EditText(c); e.setHint(hint);
        e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        return e;
    }
    private static String hash(String s){
        try{
            MessageDigest d=MessageDigest.getInstance("SHA-256");
            byte[] b=d.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder o=new StringBuilder();
            for(byte x:b)o.append(String.format("%02x",x));
            return o.toString();
        }catch(Exception e){return "";}
    }
}
