package com.usman.notepad.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import org.json.JSONArray;
import org.json.JSONObject;

public final class V1Migrator {
    private static final String PREFS = "notepad_data";
    private static final String KEY = "notes";
    private static final String MIGRATION_PREFS = "v2_migration";
    private static final String DONE = "shared_prefs_notes_done";

    private V1Migrator() {}

    public static JSONArray parseArray(String raw) {
        try {
            return new JSONArray(raw == null || raw.trim().isEmpty() ? "[]" : raw);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    public static int countValid(String raw) {
        JSONArray a = parseArray(raw);
        int count = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.optJSONObject(i) != null) count++;
        }
        return count;
    }

    public static void migrateIfNeeded(Context context, SQLiteDatabase db) {
        SharedPreferences migration = context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE);
        if (migration.getBoolean(DONE, false)) return;
        String raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]");
        JSONArray array = parseArray(raw);
        db.beginTransaction();
        try {
            SQLiteStatement stmt = db.compileStatement("INSERT OR IGNORE INTO notes(id,title,body,mode,created_at,updated_at) VALUES(?,?,?,?,?,?)");
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.optJSONObject(i);
                if (o == null) continue;
                long id = o.optLong("id", 0L);
                if (id <= 0) id = System.currentTimeMillis() + i;
                long updated = o.optLong("updatedAt", System.currentTimeMillis());
                stmt.clearBindings();
                stmt.bindLong(1, id);
                stmt.bindString(2, o.optString("title", ""));
                stmt.bindString(3, o.optString("body", ""));
                stmt.bindString(4, "text");
                stmt.bindLong(5, updated);
                stmt.bindLong(6, updated);
                stmt.executeInsert();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        migration.edit().putBoolean(DONE, true).apply();
    }
}
