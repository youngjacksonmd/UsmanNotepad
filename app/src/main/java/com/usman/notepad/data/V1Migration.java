package com.usman.notepad.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

public final class V1Migration {
    private static final String META_KEY = "v1_migrated";
    private V1Migration() {}

    public static final class Result {
        public final int parsed, inserted, skipped;
        public final boolean success;
        Result(int p, int i, int s, boolean ok) { parsed=p; inserted=i; skipped=s; success=ok; }
    }

    public static Result migrateIfNeeded(Context context, SQLiteDatabase db) {
        try (Cursor c = db.rawQuery("SELECT v FROM meta WHERE k=?", new String[]{META_KEY})) {
            if (c.moveToFirst() && "1".equals(c.getString(0))) return new Result(0,0,0,true);
        }
        SharedPreferences prefs = context.getSharedPreferences("notepad_data", Context.MODE_PRIVATE);
        String raw = prefs.getString("notes", "[]");
        int parsed=0, inserted=0, skipped=0;
        db.beginTransaction();
        try {
            JSONArray a = new JSONArray(raw == null ? "[]" : raw);
            parsed = a.length();
            for (int i=0;i<a.length();i++) {
                try {
                    JSONObject o=a.getJSONObject(i);
                    long id=o.optLong("id",0);
                    if (id<=0) { skipped++; continue; }
                    ContentValues v=new ContentValues();
                    v.put("id",id);
                    v.put("title",o.optString("title",""));
                    v.put("body",o.optString("body",""));
                    long updated=o.optLong("updatedAt",System.currentTimeMillis());
                    v.put("created_at",updated);
                    v.put("updated_at",updated);
                    long r=db.insertWithOnConflict("notes",null,v,SQLiteDatabase.CONFLICT_IGNORE);
                    if (r!=-1) inserted++;
                } catch (Exception bad) { skipped++; }
            }
            ContentValues m=new ContentValues(); m.put("k",META_KEY); m.put("v","1");
            db.insertWithOnConflict("meta",null,m,SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
            return new Result(parsed,inserted,skipped,true);
        } catch (Exception e) {
            return new Result(parsed,inserted,skipped,false);
        } finally { db.endTransaction(); }
    }
}
