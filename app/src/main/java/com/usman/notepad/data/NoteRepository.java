package com.usman.notepad.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.usman.notepad.editor.WikiLinkParser;
import com.usman.notepad.model.Note;

import java.util.ArrayList;
import java.util.List;

public final class NoteRepository {
    private final NotepadDbHelper helper;

    public NoteRepository(Context context) {
        Context app=context.getApplicationContext();
        helper=new NotepadDbHelper(app);
        V1Migration.migrateIfNeeded(app,helper.getWritableDatabase());
    }

    public long createNote(String title,String body){
        long now=System.currentTimeMillis();
        ContentValues v=new ContentValues();
        v.put("id",now);v.put("title",safe(title));v.put("body",safe(body));v.put("created_at",now);v.put("updated_at",now);
        helper.getWritableDatabase().insertOrThrow("notes",null,v);return now;
    }

    public long createSpecial(String title,String body,String mode){
        long id=createNote(title,body);ContentValues v=new ContentValues();v.put("mode",mode);
        helper.getWritableDatabase().update("notes",v,"id=?",new String[]{String.valueOf(id)});return id;
    }

    public long getOrCreateScratch(){
        try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id FROM notes WHERE mode='scratch' AND is_deleted=0 LIMIT 1",null)){if(c.moveToFirst())return c.getLong(0);}
        return createSpecial("Scratch Pad","","scratch");
    }

    public long getOrCreateDaily(String dateKey){
        String title="Daily Note — "+dateKey;
        try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id FROM notes WHERE mode='daily' AND title=? AND is_deleted=0 LIMIT 1",new String[]{title})){if(c.moveToFirst())return c.getLong(0);}
        return createSpecial(title,"","daily");
    }

    public Note getNote(long id){
        maintain();
        try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT * FROM notes WHERE id=?",new String[]{String.valueOf(id)})){return c.moveToFirst()?fromCursor(c):null;}
    }

    public void updateNote(Note n){
        if(n==null)return;long now=System.currentTimeMillis();
        Note old=getNote(n.id);
        if(old!=null&&(!safe(old.title).equals(safe(n.title))||!safe(old.body).equals(safe(n.body))))snapshot(old,now);
        n.updatedAt=now;helper.getWritableDatabase().update("notes",values(n),"id=?",new String[]{String.valueOf(n.id)});
        rebuildLinksForNote(n.id,n.body);
    }

    public void softDelete(long id){ContentValues v=new ContentValues();v.put("is_deleted",1);v.put("deleted_at",System.currentTimeMillis());helper.getWritableDatabase().update("notes",v,"id=?",new String[]{String.valueOf(id)});}
    public void restore(long id){ContentValues v=new ContentValues();v.put("is_deleted",0);v.put("deleted_at",0);helper.getWritableDatabase().update("notes",v,"id=?",new String[]{String.valueOf(id)});}

    public void permanentDelete(long id){
        SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();
        try{
            String[] a={String.valueOf(id)};db.delete("note_tags","note_id=?",a);db.delete("revisions","note_id=?",a);db.delete("attachments","note_id=?",a);db.delete("reminders","note_id=?",a);
            db.delete("note_links","source_note_id=? OR target_note_id=?",new String[]{String.valueOf(id),String.valueOf(id)});db.delete("notes","id=?",a);db.setTransactionSuccessful();
        }finally{db.endTransaction();}
    }

    public List<Note> listNotes(String query,String filter){
        maintain();List<String> args=new ArrayList<>();StringBuilder w=new StringBuilder("1=1");
        if("deleted".equals(filter))w.append(" AND is_deleted=1");
        else{w.append(" AND is_deleted=0");if("favorites".equals(filter))w.append(" AND is_favorite=1");else if("pinned".equals(filter))w.append(" AND is_pinned=1");else if("inbox".equals(filter))w.append(" AND is_inbox=1");else if("archived".equals(filter))w.append(" AND is_archived=1");else w.append(" AND is_archived=0");}
        if(query!=null&&!query.trim().isEmpty()){w.append(" AND (title LIKE ? OR (is_locked=0 AND body LIKE ?))");String q="%"+query.trim()+"%";args.add(q);args.add(q);}
        List<Note> out=new ArrayList<>();
        try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT * FROM notes WHERE "+w+" ORDER BY is_pinned DESC,updated_at DESC",args.toArray(new String[0]))){while(c.moveToNext())out.add(fromCursor(c));}
        return out;
    }

    public List<Note> allNotes(){List<Note> out=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT * FROM notes WHERE is_deleted=0 ORDER BY updated_at DESC",null)){while(c.moveToNext())out.add(fromCursor(c));}return out;}

    public void setFlag(long id,String column,boolean value){
        if(!("is_pinned".equals(column)||"is_favorite".equals(column)||"is_archived".equals(column)||"quick_copy".equals(column)))return;
        ContentValues v=new ContentValues();v.put(column,value?1:0);v.put("updated_at",System.currentTimeMillis());helper.getWritableDatabase().update("notes",v,"id=?",new String[]{String.valueOf(id)});
    }
    public void setInbox(long id,boolean value){ContentValues v=new ContentValues();v.put("is_inbox",value?1:0);helper.getWritableDatabase().update("notes",v,"id=?",new String[]{String.valueOf(id)});}
    public void setFolder(long noteId,Long folderId){ContentValues v=new ContentValues();if(folderId==null)v.putNull("folder_id");else v.put("folder_id",folderId);helper.getWritableDatabase().update("notes",v,"id=?",new String[]{String.valueOf(noteId)});}

    public long createFolder(String name,Long parentId){ContentValues v=new ContentValues();v.put("name",name.trim());if(parentId==null)v.putNull("parent_id");else v.put("parent_id",parentId);v.put("created_at",System.currentTimeMillis());return helper.getWritableDatabase().insert("folders",null,v);}
    public List<String[]> listFolders(){List<String[]> out=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name,parent_id FROM folders ORDER BY name COLLATE NOCASE",null)){while(c.moveToNext())out.add(new String[]{String.valueOf(c.getLong(0)),c.getString(1),c.isNull(2)?"":String.valueOf(c.getLong(2))});}return out;}
    public void deleteFolderMoveNotesToRoot(long id){SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();try{ContentValues a=new ContentValues();a.putNull("folder_id");db.update("notes",a,"folder_id=?",new String[]{String.valueOf(id)});ContentValues b=new ContentValues();b.putNull("parent_id");db.update("folders",b,"parent_id=?",new String[]{String.valueOf(id)});db.delete("folders","id=?",new String[]{String.valueOf(id)});db.setTransactionSuccessful();}finally{db.endTransaction();}}

    public void setTags(long noteId,String csv){
        SQLiteDatabase db=helper.getWritableDatabase();db.beginTransaction();
        try{db.delete("note_tags","note_id=?",new String[]{String.valueOf(noteId)});if(csv!=null)for(String raw:csv.split(",")){String name=raw.trim();if(name.isEmpty())continue;ContentValues t=new ContentValues();t.put("name",name);db.insertWithOnConflict("tags",null,t,SQLiteDatabase.CONFLICT_IGNORE);long tagId=-1;try(Cursor c=db.rawQuery("SELECT id FROM tags WHERE name=? COLLATE NOCASE",new String[]{name})){if(c.moveToFirst())tagId=c.getLong(0);}if(tagId>=0){ContentValues nt=new ContentValues();nt.put("note_id",noteId);nt.put("tag_id",tagId);db.insertWithOnConflict("note_tags",null,nt,SQLiteDatabase.CONFLICT_IGNORE);}}db.setTransactionSuccessful();}finally{db.endTransaction();}
    }
    public String getTags(long noteId){StringBuilder s=new StringBuilder();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT t.name FROM tags t JOIN note_tags nt ON nt.tag_id=t.id WHERE nt.note_id=? ORDER BY t.name",new String[]{String.valueOf(noteId)})){while(c.moveToNext()){if(s.length()>0)s.append(", ");s.append(c.getString(0));}}return s.toString();}
    public List<String> listTags(){List<String> out=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT name FROM tags ORDER BY name COLLATE NOCASE",null)){while(c.moveToNext())out.add(c.getString(0));}return out;}
    public List<Note> notesForTag(String tag){List<Note> out=new ArrayList<>();String sql="SELECT n.* FROM notes n JOIN note_tags nt ON nt.note_id=n.id JOIN tags t ON t.id=nt.tag_id WHERE n.is_deleted=0 AND t.name=? COLLATE NOCASE ORDER BY n.is_pinned DESC,n.updated_at DESC";try(Cursor c=helper.getReadableDatabase().rawQuery(sql,new String[]{tag})){while(c.moveToNext())out.add(fromCursor(c));}return out;}
    public List<Note> notesForFolder(long folderId){List<Note> out=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT * FROM notes WHERE is_deleted=0 AND folder_id=? ORDER BY is_pinned DESC,updated_at DESC",new String[]{String.valueOf(folderId)})){while(c.moveToNext())out.add(fromCursor(c));}return out;}

    public List<String[]> revisions(long noteId){List<String[]> out=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,title,body,created_at FROM revisions WHERE note_id=? ORDER BY created_at DESC LIMIT 30",new String[]{String.valueOf(noteId)})){while(c.moveToNext())out.add(new String[]{String.valueOf(c.getLong(0)),c.getString(1),c.getString(2),String.valueOf(c.getLong(3))});}return out;}
    public void restoreRevision(long revisionId,long noteId){try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT title,body FROM revisions WHERE id=?",new String[]{String.valueOf(revisionId)})){if(!c.moveToFirst())return;Note n=getNote(noteId);if(n==null)return;n.title=c.getString(0);n.body=c.getString(1);updateNote(n);}}

    public long createFromTemplate(long id){try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT title,body,mode FROM templates WHERE id=?",new String[]{String.valueOf(id)})){if(c.moveToFirst())return createSpecial(c.getString(0),c.getString(1),c.getString(2));}return createNote("","");}
    public List<String[]> templates(){List<String[]> out=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT id,name FROM templates ORDER BY is_builtin DESC,name",null)){while(c.moveToNext())out.add(new String[]{String.valueOf(c.getLong(0)),c.getString(1)});}return out;}
    public void saveAsTemplate(Note n,String name){ContentValues v=new ContentValues();v.put("name",name);v.put("title",n.title);v.put("body",n.body);v.put("mode",n.mode);v.put("is_builtin",0);helper.getWritableDatabase().insert("templates",null,v);}

    public void addAttachment(long noteId,String type,String path){ContentValues v=new ContentValues();v.put("note_id",noteId);v.put("type",type);v.put("local_path",path);v.put("created_at",System.currentTimeMillis());helper.getWritableDatabase().insert("attachments",null,v);}
    public List<String[]> attachments(long noteId){List<String[]> out=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT type,local_path FROM attachments WHERE note_id=? ORDER BY created_at",new String[]{String.valueOf(noteId)})){while(c.moveToNext())out.add(new String[]{c.getString(0),c.getString(1)});}return out;}

    public void rebuildLinksForNote(long noteId,String body){SQLiteDatabase db=helper.getWritableDatabase();db.delete("note_links","source_note_id=?",new String[]{String.valueOf(noteId)});for(String title:WikiLinkParser.extractTitles(body)){try(Cursor c=db.rawQuery("SELECT id FROM notes WHERE is_deleted=0 AND title=? COLLATE NOCASE ORDER BY updated_at DESC LIMIT 1",new String[]{title})){if(c.moveToFirst()){ContentValues v=new ContentValues();v.put("source_note_id",noteId);v.put("target_note_id",c.getLong(0));v.put("link_text",title);db.insertWithOnConflict("note_links",null,v,SQLiteDatabase.CONFLICT_IGNORE);}}}}
    public List<Note> backlinks(long noteId){List<Note> out=new ArrayList<>();String sql="SELECT n.* FROM notes n JOIN note_links l ON l.source_note_id=n.id WHERE l.target_note_id=? AND n.is_deleted=0 ORDER BY n.updated_at DESC";try(Cursor c=helper.getReadableDatabase().rawQuery(sql,new String[]{String.valueOf(noteId)})){while(c.moveToNext())out.add(fromCursor(c));}return out;}
    public List<long[]> allLinks(){List<long[]> out=new ArrayList<>();try(Cursor c=helper.getReadableDatabase().rawQuery("SELECT source_note_id,target_note_id FROM note_links",null)){while(c.moveToNext())out.add(new long[]{c.getLong(0),c.getLong(1)});}return out;}

    public void updateEncrypted(long noteId,boolean locked,String body,String encBody,String encIv){ContentValues v=new ContentValues();v.put("is_locked",locked?1:0);v.put("body",body==null?"":body);if(encBody==null)v.putNull("encrypted_body");else v.put("encrypted_body",encBody);if(encIv==null)v.putNull("encrypted_iv");else v.put("encrypted_iv",encIv);v.put("updated_at",System.currentTimeMillis());helper.getWritableDatabase().update("notes",v,"id=?",new String[]{String.valueOf(noteId)});}
    public SQLiteDatabase db(){return helper.getWritableDatabase();}

    private void snapshot(Note n,long now){SQLiteDatabase db=helper.getWritableDatabase();long last=0;try(Cursor c=db.rawQuery("SELECT created_at FROM revisions WHERE note_id=? ORDER BY created_at DESC LIMIT 1",new String[]{String.valueOf(n.id)})){if(c.moveToFirst())last=c.getLong(0);}if(now-last<15000)return;ContentValues v=new ContentValues();v.put("note_id",n.id);v.put("title",n.title);v.put("body",n.body);v.put("created_at",now);db.insert("revisions",null,v);db.execSQL("DELETE FROM revisions WHERE note_id=? AND id NOT IN (SELECT id FROM revisions WHERE note_id=? ORDER BY created_at DESC LIMIT 30)",new Object[]{n.id,n.id});}

    public void maintain(){long now=System.currentTimeMillis();SQLiteDatabase db=helper.getWritableDatabase();ContentValues v=new ContentValues();v.put("is_deleted",1);v.put("deleted_at",now);db.update("notes",v,"is_deleted=0 AND expires_at>0 AND expires_at<=?",new String[]{String.valueOf(now)});long cutoff=now-30L*24L*60L*60L*1000L;List<Long> ids=new ArrayList<>();try(Cursor c=db.rawQuery("SELECT id FROM notes WHERE is_deleted=1 AND deleted_at>0 AND deleted_at<?",new String[]{String.valueOf(cutoff)})){while(c.moveToNext())ids.add(c.getLong(0));}for(Long id:ids)permanentDelete(id);}

    private ContentValues values(Note n){ContentValues v=new ContentValues();v.put("title",safe(n.title));v.put("body",safe(n.body));v.put("mode",safe(n.mode));v.put("created_at",n.createdAt==0?System.currentTimeMillis():n.createdAt);v.put("updated_at",n.updatedAt==0?System.currentTimeMillis():n.updatedAt);if(n.folderId==null)v.putNull("folder_id");else v.put("folder_id",n.folderId);v.put("is_pinned",n.pinned?1:0);v.put("is_favorite",n.favorite?1:0);v.put("is_archived",n.archived?1:0);v.put("is_deleted",n.deleted?1:0);v.put("deleted_at",n.deletedAt);v.put("is_locked",n.locked?1:0);v.put("theme_key",safe(n.themeKey));v.put("unlock_at",n.unlockAt);v.put("expires_at",n.expiresAt);v.put("is_inbox",n.inbox?1:0);v.put("quick_copy",n.quickCopy?1:0);if(n.encryptedBody==null)v.putNull("encrypted_body");else v.put("encrypted_body",n.encryptedBody);if(n.encryptedIv==null)v.putNull("encrypted_iv");else v.put("encrypted_iv",n.encryptedIv);return v;}
    private Note fromCursor(Cursor c){Note n=new Note();n.id=c.getLong(c.getColumnIndexOrThrow("id"));n.title=c.getString(c.getColumnIndexOrThrow("title"));n.body=c.getString(c.getColumnIndexOrThrow("body"));n.mode=c.getString(c.getColumnIndexOrThrow("mode"));n.createdAt=c.getLong(c.getColumnIndexOrThrow("created_at"));n.updatedAt=c.getLong(c.getColumnIndexOrThrow("updated_at"));int f=c.getColumnIndexOrThrow("folder_id");n.folderId=c.isNull(f)?null:c.getLong(f);n.pinned=c.getInt(c.getColumnIndexOrThrow("is_pinned"))!=0;n.favorite=c.getInt(c.getColumnIndexOrThrow("is_favorite"))!=0;n.archived=c.getInt(c.getColumnIndexOrThrow("is_archived"))!=0;n.deleted=c.getInt(c.getColumnIndexOrThrow("is_deleted"))!=0;n.deletedAt=c.getLong(c.getColumnIndexOrThrow("deleted_at"));n.locked=c.getInt(c.getColumnIndexOrThrow("is_locked"))!=0;n.themeKey=c.getString(c.getColumnIndexOrThrow("theme_key"));n.unlockAt=c.getLong(c.getColumnIndexOrThrow("unlock_at"));n.expiresAt=c.getLong(c.getColumnIndexOrThrow("expires_at"));n.inbox=c.getInt(c.getColumnIndexOrThrow("is_inbox"))!=0;n.quickCopy=c.getInt(c.getColumnIndexOrThrow("quick_copy"))!=0;n.encryptedBody=c.getString(c.getColumnIndexOrThrow("encrypted_body"));n.encryptedIv=c.getString(c.getColumnIndexOrThrow("encrypted_iv"));return n;}
    private static String safe(String s){return s==null?"":s;}
}
