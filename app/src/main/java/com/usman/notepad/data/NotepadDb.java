package com.usman.notepad.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NotepadDb extends SQLiteOpenHelper {
    public static final String DB_NAME = "usman_notepad_v2.db";
    public static final int DB_VERSION = 1;

    public NotepadDb(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notes (id INTEGER PRIMARY KEY, title TEXT NOT NULL DEFAULT '', body TEXT NOT NULL DEFAULT '', mode TEXT NOT NULL DEFAULT 'text', created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, folder_id INTEGER, is_pinned INTEGER NOT NULL DEFAULT 0, is_favorite INTEGER NOT NULL DEFAULT 0, is_archived INTEGER NOT NULL DEFAULT 0, is_deleted INTEGER NOT NULL DEFAULT 0, deleted_at INTEGER NOT NULL DEFAULT 0, is_locked INTEGER NOT NULL DEFAULT 0, theme_key TEXT NOT NULL DEFAULT 'system', unlock_at INTEGER NOT NULL DEFAULT 0, expires_at INTEGER NOT NULL DEFAULT 0, is_inbox INTEGER NOT NULL DEFAULT 0, is_quick_copy INTEGER NOT NULL DEFAULT 0, is_scratch INTEGER NOT NULL DEFAULT 0, is_daily INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE folders (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, parent_id INTEGER, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE tags (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL)");
        db.execSQL("CREATE TABLE note_tags (note_id INTEGER NOT NULL, tag_id INTEGER NOT NULL, UNIQUE(note_id, tag_id))");
        db.execSQL("CREATE TABLE note_revisions (id INTEGER PRIMARY KEY AUTOINCREMENT, note_id INTEGER NOT NULL, title TEXT NOT NULL, body TEXT NOT NULL, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE note_links (source_note_id INTEGER NOT NULL, target_note_id INTEGER NOT NULL, link_text TEXT NOT NULL, UNIQUE(source_note_id, target_note_id, link_text))");
        db.execSQL("CREATE TABLE attachments (id INTEGER PRIMARY KEY AUTOINCREMENT, note_id INTEGER NOT NULL, type TEXT NOT NULL, local_path TEXT NOT NULL, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE reminders (id INTEGER PRIMARY KEY AUTOINCREMENT, note_id INTEGER NOT NULL, trigger_at INTEGER NOT NULL DEFAULT 0, latitude REAL, longitude REAL, radius_m INTEGER, enabled INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE templates (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL, body TEXT NOT NULL, mode TEXT NOT NULL DEFAULT 'text')");
        db.execSQL("CREATE INDEX idx_notes_updated ON notes(updated_at DESC)");
        db.execSQL("CREATE INDEX idx_notes_deleted ON notes(is_deleted, deleted_at)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
