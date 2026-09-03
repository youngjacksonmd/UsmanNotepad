package com.usman.notepad.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.usman.notepad.model.Note;
import java.util.ArrayList;
import java.util.List;

public class NoteRepository {
    private final Context context;
    private final NotepadDb helper;

    public NoteRepository(Context context) {
        this.context = context.getApplicationContext();
        this.helper = new NotepadDb(this.context);
        SQLiteDatabase db = helper.getWritableDatabase();
        V1Migrator.migrateIfNeeded(this.context, db);
        seedTemplates(db);
        runMaintenance();
    }

    private void seedTemplates(SQLiteDatabase db) {
        String[][] t = new String[][]{
                {"Meeting", "Date:\nPeople:\nTopic:\n\nKey Points:\n• \n\nAction Items:\n☐ ", "meeting"},
                {"Journal", "How do I feel?\n\nWhat happened today?\n\nWhat am I grateful for?\n", "journal"},
                {"Shopping", "☐ ", "shopping"},
                {"Project", "Goal:\n\nNext steps:\n☐ \n\nNotes:\n", "text"},
                {"Daily log", "Top priorities:\n☐ \n\nNotes:\n\nWins:\n", "journal"}
        };
        for (String[] x : t) {
            ContentValues v = new ContentValues();
            v.put("name", x[0]); v.put("body", x[1]); v.put("mode", x[2]);
            db.insertWithOnConflict("templates", null, v, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public synchronized long save(Note n, boolean snapshot) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long now = System.currentTimeMillis();
        if (n.id <= 0) n.id = now;
        if (n.createdAt <= 0) n.createdAt = now;
        n.updatedAt = now;
        if (snapshot) snapshot(db, n.id);
        ContentValues v = toValues(n);
        db.insertWithOnConflict("notes", null, v, SQLiteDatabase.CONFLICT_REPLACE);
        pruneRevisions(db, n.id);
        return n.id;
    }

    private ContentValues toValues(Note n) {
        ContentValues v = new ContentValues();
        v.put("id", n.id); v.put("title", safe(n.title)); v.put("body", safe(n.body)); v.put("mode", safe(n.mode));
        v.put("created_at", n.createdAt); v.put("updated_at", n.updatedAt);
        if (n.folderId == null) v.putNull("folder_id"); else v.put("folder_id", n.folderId);
        v.put("is_pinned", n.pinned ? 1 : 0); v.put("is_favorite", n.favorite ? 1 : 0); v.put("is_archived", n.archived ? 1 : 0);
        v.put("is_deleted", n.deleted ? 1 : 0); v.put("deleted_at", n.deletedAt); v.put("is_locked", n.locked ? 1 : 0);
        v.put("theme_key", safe(n.themeKey)); v.put("unlock_at", n.unlockAt); v.put("expires_at", n.expiresAt); v.put("is_inbox", n.inbox ? 1 : 0);
        v.put("is_quick_copy", n.quickCopy ? 1 : 0); v.put("is_scratch", n.scratch ? 1 : 0); v.put("is_daily", n.daily ? 1 : 0);
        return v;
    }

    private void snapshot(SQLiteDatabase db, long noteId) {
        Note old = findInternal(db, noteId);
        if (old == null) return;
        ContentValues r = new ContentValues();
        r.put("note_id", noteId); r.put("title", old.title); r.put("body", old.body); r.put("created_at", System.currentTimeMillis());
        db.insert("note_revisions", null, r);
    }

    private void pruneRevisions(SQLiteDatabase db, long noteId) {
        db.execSQL("DELETE FROM note_revisions WHERE note_id=? AND id NOT IN (SELECT id FROM note_revisions WHERE note_id=? ORDER BY created_at DESC LIMIT 30)", new Object[]{noteId, noteId});
    }

    public Note find(long id) { return findInternal(helper.getReadableDatabase(), id); }

    private Note findInternal(SQLiteDatabase db, long id) {
        try (Cursor c = db.rawQuery("SELECT * FROM notes WHERE id=?", new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    public List<Note> list(String query, String filter) {
        runMaintenance();
        String q = query == null ? "" : query.trim();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT n.* FROM notes n LEFT JOIN folders f ON f.id=n.folder_id LEFT JOIN note_tags nt ON nt.note_id=n.id LEFT JOIN tags t ON t.id=nt.tag_id WHERE 1=1 ");
        List<String> args = new ArrayList<>();
        if ("trash".equals(filter)) sql.append("AND n.is_deleted=1 "); else sql.append("AND n.is_deleted=0 ");
        if ("favorites".equals(filter)) sql.append("AND n.is_favorite=1 ");
        if ("pinned".equals(filter)) sql.append("AND n.is_pinned=1 ");
        if ("inbox".equals(filter)) sql.append("AND n.is_inbox=1 ");
        if ("archived".equals(filter)) sql.append("AND n.is_archived=1 "); else if (!"trash".equals(filter)) sql.append("AND n.is_archived=0 ");
        if (!q.isEmpty()) {
            sql.append("AND (n.title LIKE ? OR (n.is_locked=0 AND n.body LIKE ?) OR f.name LIKE ? OR t.name LIKE ?) ");
            String like = "%" + q + "%";
            args.add(like); args.add(like); args.add(like); args.add(like);
        }
        sql.append("ORDER BY n.is_pinned DESC, n.updated_at DESC");
        List<Note> out = new ArrayList<>();
        try (Cursor c = helper.getReadableDatabase().rawQuery(sql.toString(), args.toArray(new String[0]))) {
            while (c.moveToNext()) out.add(fromCursor(c));
        }
        return out;
    }

    public void togglePin(long id) { toggle(id, "is_pinned"); }
    public void toggleFavorite(long id) { toggle(id, "is_favorite"); }
    public void toggleQuickCopy(long id) { toggle(id, "is_quick_copy"); }
    private void toggle(long id, String column) { helper.getWritableDatabase().execSQL("UPDATE notes SET " + column + "=CASE " + column + " WHEN 0 THEN 1 ELSE 0 END, updated_at=? WHERE id=?", new Object[]{System.currentTimeMillis(), id}); }
    public void archive(long id) { helper.getWritableDatabase().execSQL("UPDATE notes SET is_archived=1, updated_at=? WHERE id=?", new Object[]{System.currentTimeMillis(), id}); }
    public void trash(long id) { helper.getWritableDatabase().execSQL("UPDATE notes SET is_deleted=1, deleted_at=?, updated_at=? WHERE id=?", new Object[]{System.currentTimeMillis(), System.currentTimeMillis(), id}); }
    public void restore(long id) { helper.getWritableDatabase().execSQL("UPDATE notes SET is_deleted=0, deleted_at=0, updated_at=? WHERE id=?", new Object[]{System.currentTimeMillis(), id}); }
    public void purge(long id) { SQLiteDatabase db = helper.getWritableDatabase(); db.delete("note_tags", "note_id=?", new String[]{String.valueOf(id)}); db.delete("note_revisions", "note_id=?", new String[]{String.valueOf(id)}); db.delete("note_links", "source_note_id=? OR target_note_id=?", new String[]{String.valueOf(id),String.valueOf(id)}); db.delete("attachments", "note_id=?", new String[]{String.valueOf(id)}); db.delete("reminders", "note_id=?", new String[]{String.valueOf(id)}); db.delete("notes", "id=?", new String[]{String.valueOf(id)}); }

    public long createFolder(String name, Long parent) { ContentValues v = new ContentValues(); v.put("name", name.trim()); if (parent == null) v.putNull("parent_id"); else v.put("parent_id", parent); v.put("created_at", System.currentTimeMillis()); return helper.getWritableDatabase().insert("folders", null, v); }
    public void renameFolder(long id, String name) { ContentValues v = new ContentValues(); v.put("name", name.trim()); helper.getWritableDatabase().update("folders", v, "id=?", new String[]{String.valueOf(id)}); }
    public void deleteFolder(long id) { SQLiteDatabase db=helper.getWritableDatabase(); ContentValues n=new ContentValues(); n.putNull("folder_id"); db.update("notes", n, "folder_id=?", new String[]{String.valueOf(id)}); ContentValues f=new ContentValues(); f.putNull("parent_id"); db.update("folders", f, "parent_id=?", new String[]{String.valueOf(id)}); db.delete("folders","id=?",new String[]{String.valueOf(id)}); }
    public List<String> folderRows() { List<String> out=new ArrayList<>(); try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,parent_id FROM folders ORDER BY name",null)){ while(c.moveToNext()) out.add(c.getLong(0)+"|"+c.getString(1)+"|"+(c.isNull(2)?"":c.getLong(2))); } return out; }
    public void moveToFolder(long noteId, Long folderId) { ContentValues v=new ContentValues(); if(folderId==null)v.putNull("folder_id");else v.put("folder_id",folderId); helper.getWritableDatabase().update("notes",v,"id=?",new String[]{String.valueOf(noteId)}); }

    public long ensureTag(String name) { SQLiteDatabase db=helper.getWritableDatabase(); ContentValues v=new ContentValues(); v.put("name",name.trim()); db.insertWithOnConflict("tags",null,v,SQLiteDatabase.CONFLICT_IGNORE); try(Cursor c=db.rawQuery("SELECT id FROM tags WHERE name=?",new String[]{name.trim()})){ return c.moveToFirst()?c.getLong(0):-1; } }
    public void addTag(long noteId,String name){ long tag=ensureTag(name); if(tag<0)return; ContentValues v=new ContentValues();v.put("note_id",noteId);v.put("tag_id",tag);helper.getWritableDatabase().insertWithOnConflict("note_tags",null,v,SQLiteDatabase.CONFLICT_IGNORE); }
    public List<String> tagsFor(long noteId){ List<String>o=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT t.name FROM tags t JOIN note_tags nt ON nt.tag_id=t.id WHERE nt.note_id=? ORDER BY t.name",new String[]{String.valueOf(noteId)})){while(c.moveToNext())o.add(c.getString(0));}return o;}
    public List<String> allTags(){List<String>o=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT name FROM tags ORDER BY name",null)){while(c.moveToNext())o.add(c.getString(0));}return o;}

    public List<String> revisions(long noteId){ List<String>o=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,title,body,created_at FROM note_revisions WHERE note_id=? ORDER BY created_at DESC",new String[]{String.valueOf(noteId)})){while(c.moveToNext())o.add(c.getLong(0)+"|"+c.getLong(3)+"|"+c.getString(1)+"|"+c.getString(2));}return o;}
    public void restoreRevision(long revisionId,long noteId){ SQLiteDatabase db=helper.getWritableDatabase(); Note current=find(noteId); if(current!=null)snapshot(db,noteId); try(Cursor c=db.rawQuery("SELECT title,body FROM note_revisions WHERE id=? AND note_id=?",new String[]{String.valueOf(revisionId),String.valueOf(noteId)})){if(c.moveToFirst()){ContentValues v=new ContentValues();v.put("title",c.getString(0));v.put("body",c.getString(1));v.put("updated_at",System.currentTimeMillis());db.update("notes",v,"id=?",new String[]{String.valueOf(noteId)});}}}

    public List<String> templates(){List<String>o=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,body,mode FROM templates ORDER BY name",null)){while(c.moveToNext())o.add(c.getLong(0)+"|"+c.getString(1)+"|"+c.getString(3)+"|"+c.getString(2));}return o;}
    public void saveAsTemplate(String name,Note n){ContentValues v=new ContentValues();v.put("name",name.trim());v.put("body",safe(n.body));v.put("mode",safe(n.mode));helper.getWritableDatabase().insertWithOnConflict("templates",null,v,SQLiteDatabase.CONFLICT_REPLACE);}

    public Note getOrCreateScratch(){try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT * FROM notes WHERE is_scratch=1 AND is_deleted=0 LIMIT 1",null)){if(c.moveToFirst())return fromCursor(c);}Note n=new Note();n.title="Scratch Pad";n.scratch=true;save(n,false);return n;}
    public Note getOrCreateDaily(String date){try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT * FROM notes WHERE is_daily=1 AND title=? AND is_deleted=0 LIMIT 1",new String[]{"Daily • "+date})){if(c.moveToFirst())return fromCursor(c);}Note n=new Note();n.title="Daily • "+date;n.daily=true;n.mode="journal";save(n,false);return n;}

    public void setLinkRows(long sourceId,List<String> titles){SQLiteDatabase db=helper.getWritableDatabase();db.delete("note_links","source_note_id=?",new String[]{String.valueOf(sourceId)});for(String title:titles){Note target=findByTitle(title);if(target==null)continue;ContentValues v=new ContentValues();v.put("source_note_id",sourceId);v.put("target_note_id",target.id);v.put("link_text",title);db.insertWithOnConflict("note_links",null,v,SQLiteDatabase.CONFLICT_IGNORE);}}
    public Note findByTitle(String title){try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT * FROM notes WHERE lower(title)=lower(?) AND is_deleted=0 LIMIT 1",new String[]{title})){return c.moveToFirst()?fromCursor(c):null;}}
    public List<Note> backlinks(long noteId){List<Note>o=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT n.* FROM notes n JOIN note_links l ON l.source_note_id=n.id WHERE l.target_note_id=? AND n.is_deleted=0 ORDER BY n.updated_at DESC",new String[]{String.valueOf(noteId)})){while(c.moveToNext())o.add(fromCursor(c));}return o;}
    public List<long[]> links(){List<long[]>o=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT source_note_id,target_note_id FROM note_links",null)){while(c.moveToNext())o.add(new long[]{c.getLong(0),c.getLong(1)});}return o;}

    public void addAttachment(long noteId,String type,String path){ContentValues v=new ContentValues();v.put("note_id",noteId);v.put("type",type);v.put("local_path",path);v.put("created_at",System.currentTimeMillis());helper.getWritableDatabase().insert("attachments",null,v);}
    public List<String> attachments(long noteId){List<String>o=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,type,local_path FROM attachments WHERE note_id=? ORDER BY created_at",new String[]{String.valueOf(noteId)})){while(c.moveToNext())o.add(c.getLong(0)+"|"+c.getString(1)+"|"+c.getString(2));}return o;}

    public void setReminder(long noteId,long triggerAt){SQLiteDatabase db=helper.getWritableDatabase();db.delete("reminders","note_id=?",new String[]{String.valueOf(noteId)});if(triggerAt>0){ContentValues v=new ContentValues();v.put("note_id",noteId);v.put("trigger_at",triggerAt);v.put("enabled",1);db.insert("reminders",null,v);}}
    public long reminderFor(long noteId){try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT trigger_at FROM reminders WHERE note_id=? AND enabled=1 ORDER BY id DESC LIMIT 1",new String[]{String.valueOf(noteId)})){return c.moveToFirst()?c.getLong(0):0L;}}

    public void runMaintenance(){SQLiteDatabase db=helper.getWritableDatabase();long now=System.currentTimeMillis();db.execSQL("UPDATE notes SET is_deleted=1,deleted_at=?,updated_at=? WHERE is_deleted=0 AND expires_at>0 AND expires_at<=?",new Object[]{now,now,now});long cutoff=now-30L*24*60*60*1000;db.delete("notes","is_deleted=1 AND deleted_at>0 AND deleted_at<?",new String[]{String.valueOf(cutoff)});}

    private Note fromCursor(Cursor c){Note n=new Note();n.id=gL(c,"id");n.title=gS(c,"title");n.body=gS(c,"body");n.mode=gS(c,"mode");n.createdAt=gL(c,"created_at");n.updatedAt=gL(c,"updated_at");int fi=c.getColumnIndex("folder_id");n.folderId=fi>=0&&!c.isNull(fi)?c.getLong(fi):null;n.pinned=gL(c,"is_pinned")!=0;n.favorite=gL(c,"is_favorite")!=0;n.archived=gL(c,"is_archived")!=0;n.deleted=gL(c,"is_deleted")!=0;n.deletedAt=gL(c,"deleted_at");n.locked=gL(c,"is_locked")!=0;n.themeKey=gS(c,"theme_key");n.unlockAt=gL(c,"unlock_at");n.expiresAt=gL(c,"expires_at");n.inbox=gL(c,"is_inbox")!=0;n.quickCopy=gL(c,"is_quick_copy")!=0;n.scratch=gL(c,"is_scratch")!=0;n.daily=gL(c,"is_daily")!=0;return n;}
    private long gL(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?0L:c.getLong(i);}private String gS(Cursor c,String col){int i=c.getColumnIndex(col);return i<0||c.isNull(i)?"":c.getString(i);}private static String safe(String s){return s==null?"":s;}
}
