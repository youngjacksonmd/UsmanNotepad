package com.usman.notepad.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public final class NotepadDbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "usman_notepad_v2.db";
    public static final int DB_VERSION = 1;

    public NotepadDbHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE notes (" +
                "id INTEGER PRIMARY KEY," +
                "title TEXT NOT NULL DEFAULT ''," +
                "body TEXT NOT NULL DEFAULT ''," +
                "mode TEXT NOT NULL DEFAULT 'text'," +
                "created_at INTEGER NOT NULL," +
                "updated_at INTEGER NOT NULL," +
                "folder_id INTEGER," +
                "is_pinned INTEGER NOT NULL DEFAULT 0," +
                "is_favorite INTEGER NOT NULL DEFAULT 0," +
                "is_archived INTEGER NOT NULL DEFAULT 0," +
                "is_deleted INTEGER NOT NULL DEFAULT 0," +
                "deleted_at INTEGER NOT NULL DEFAULT 0," +
                "is_locked INTEGER NOT NULL DEFAULT 0," +
                "theme_key TEXT NOT NULL DEFAULT 'system'," +
                "unlock_at INTEGER NOT NULL DEFAULT 0," +
                "expires_at INTEGER NOT NULL DEFAULT 0," +
                "is_inbox INTEGER NOT NULL DEFAULT 0," +
                "quick_copy INTEGER NOT NULL DEFAULT 0," +
                "encrypted_body TEXT," +
                "encrypted_iv TEXT" +
                ")");
        db.execSQL("CREATE INDEX idx_notes_updated ON notes(updated_at DESC)");
        db.execSQL("CREATE INDEX idx_notes_state ON notes(is_deleted,is_archived,is_pinned,is_favorite,is_inbox)");
        db.execSQL("CREATE TABLE folders(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,parent_id INTEGER,created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE tags(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT UNIQUE NOT NULL COLLATE NOCASE)");
        db.execSQL("CREATE TABLE note_tags(note_id INTEGER NOT NULL,tag_id INTEGER NOT NULL,UNIQUE(note_id,tag_id))");
        db.execSQL("CREATE TABLE revisions(id INTEGER PRIMARY KEY AUTOINCREMENT,note_id INTEGER NOT NULL,title TEXT NOT NULL,body TEXT NOT NULL,created_at INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_revisions_note ON revisions(note_id,created_at DESC)");
        db.execSQL("CREATE TABLE note_links(source_note_id INTEGER NOT NULL,target_note_id INTEGER NOT NULL,link_text TEXT NOT NULL,UNIQUE(source_note_id,target_note_id,link_text))");
        db.execSQL("CREATE TABLE attachments(id INTEGER PRIMARY KEY AUTOINCREMENT,note_id INTEGER NOT NULL,type TEXT NOT NULL,local_path TEXT NOT NULL,created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE reminders(id INTEGER PRIMARY KEY AUTOINCREMENT,note_id INTEGER NOT NULL,trigger_at INTEGER NOT NULL DEFAULT 0,enabled INTEGER NOT NULL DEFAULT 1)");
        db.execSQL("CREATE TABLE templates(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,title TEXT NOT NULL DEFAULT '',body TEXT NOT NULL DEFAULT '',mode TEXT NOT NULL DEFAULT 'text',is_builtin INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE TABLE meta(k TEXT PRIMARY KEY,v TEXT)");
        seedTemplates(db);
    }

    private void seedTemplates(SQLiteDatabase db) {
        db.execSQL("INSERT INTO templates(name,title,body,mode,is_builtin) VALUES('Meeting','','Date:\\nPeople:\\nTopic:\\n\\nKey Points:\\n• \\n\\nAction Items:\\n☐ ','meeting',1)");
        db.execSQL("INSERT INTO templates(name,title,body,mode,is_builtin) VALUES('Journal','','Date:\\nMood:\\n\\nToday:\\n','journal',1)");
        db.execSQL("INSERT INTO templates(name,title,body,mode,is_builtin) VALUES('Shopping','','☐ ','shopping',1)");
        db.execSQL("INSERT INTO templates(name,title,body,mode,is_builtin) VALUES('Project','','Goal:\\n\\nNotes:\\n\\nNext:\\n','text',1)");
        db.execSQL("INSERT INTO templates(name,title,body,mode,is_builtin) VALUES('Daily log','','Highlights:\\n\\nTODO:\\n','journal',1)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
