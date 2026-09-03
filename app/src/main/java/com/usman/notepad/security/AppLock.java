package com.usman.notepad.security;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.InputType;
import android.widget.EditText;

public final class AppLock {
    private static final String PREFS = "security";
    private AppLock() {}

    public static void setPin(Context c, String pin) { c.getSharedPreferences(PREFS,0).edit().putString("pin", pin == null ? "" : pin).apply(); }
    public static boolean hasPin(Context c) { return !c.getSharedPreferences(PREFS,0).getString("pin","").isEmpty(); }
    public static boolean verifyPin(Context c,String pin){ return c.getSharedPreferences(PREFS,0).getString("pin","").equals(pin); }
    public static boolean appLockEnabled(Context c){ return c.getSharedPreferences(PREFS,0).getBoolean("app_lock",false); }
    public static void setAppLock(Context c,boolean enabled){ c.getSharedPreferences(PREFS,0).edit().putBoolean("app_lock",enabled).apply(); }
    public static boolean hideLockedTitles(Context c){return c.getSharedPreferences(PREFS,0).getBoolean("hide_titles",false);}    
    public static void setHideLockedTitles(Context c,boolean b){c.getSharedPreferences(PREFS,0).edit().putBoolean("hide_titles",b).apply();}

    public static void requestDeviceCredential(Activity a, int requestCode) {
        if (Build.VERSION.SDK_INT >= 21) {
            KeyguardManager km = (KeyguardManager)a.getSystemService(Context.KEYGUARD_SERVICE);
            Intent i = km == null ? null : km.createConfirmDeviceCredentialIntent("Unlock Usman Notepad", "Confirm your device lock");
            if (i != null) { a.startActivityForResult(i, requestCode); return; }
        }
        a.startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
    }

    public interface PinCallback { void onSuccess(); }
    public static void promptPin(Activity activity, String title, PinCallback cb) {
        EditText input = new EditText(activity); input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(activity).setTitle(title).setView(input).setNegativeButton("Cancel", null)
                .setPositiveButton("Unlock", (d,w)-> { if (verifyPin(activity,input.getText().toString())) cb.onSuccess(); else new AlertDialog.Builder(activity).setMessage("Incorrect PIN").setPositiveButton("OK",null).show(); }).show();
    }
}
