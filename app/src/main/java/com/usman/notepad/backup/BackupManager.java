package com.usman.notepad.backup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.usman.notepad.data.NoteRepository;
import com.usman.notepad.model.Note;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class BackupManager {
    private BackupManager(){}
    public static final class ImportResult { public final int imported,skipped; public ImportResult(int i,int s){imported=i;skipped=s;} }

    public static void exportAll(Context c,OutputStream out) throws Exception {
        NoteRepository repo=new NoteRepository(c);JSONObject root=new JSONObject();root.put("format","UsmanNotepad-V2");root.put("version",2);JSONArray notes=new JSONArray();
        for(Note n:repo.allNotes()){
            JSONObject o=new JSONObject();o.put("id",n.id);o.put("title",n.title);o.put("body",n.locked?"":n.body);o.put("mode",n.mode);o.put("createdAt",n.createdAt);o.put("updatedAt",n.updatedAt);o.put("pinned",n.pinned);o.put("favorite",n.favorite);o.put("archived",n.archived);o.put("theme",n.themeKey);o.put("unlockAt",n.unlockAt);o.put("expiresAt",n.expiresAt);o.put("inbox",n.inbox);o.put("quickCopy",n.quickCopy);o.put("tags",repo.getTags(n.id));o.put("protected",n.locked);notes.put(o);
        }
        root.put("notes",notes);out.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
    }

    public static ImportResult importBackup(Context c,InputStream in) throws Exception {
        JSONObject root=new JSONObject(readAll(in));if(!"UsmanNotepad-V2".equals(root.optString("format")))throw new IllegalArgumentException("Not an UsmanNotepad V2 backup");JSONArray a=root.getJSONArray("notes");NoteRepository repo=new NoteRepository(c);SQLiteDatabase db=repo.db();int imported=0,skipped=0;db.beginTransaction();
        try{for(int i=0;i<a.length();i++){try{JSONObject o=a.getJSONObject(i);if(o.optBoolean("protected",false)){skipped++;continue;}long id=o.optLong("id",0);if(id<=0)id=System.currentTimeMillis()+i;try(Cursor cur=db.rawQuery("SELECT 1 FROM notes WHERE id=?",new String[]{String.valueOf(id)})){if(cur.moveToFirst())id=System.currentTimeMillis()+i;}ContentValues v=new ContentValues();v.put("id",id);v.put("title",o.optString("title",""));v.put("body",o.optString("body",""));v.put("mode",o.optString("mode","text"));v.put("created_at",o.optLong("createdAt",System.currentTimeMillis()));v.put("updated_at",o.optLong("updatedAt",System.currentTimeMillis()));v.put("is_pinned",o.optBoolean("pinned")?1:0);v.put("is_favorite",o.optBoolean("favorite")?1:0);v.put("is_archived",o.optBoolean("archived")?1:0);v.put("theme_key",o.optString("theme","system"));v.put("unlock_at",o.optLong("unlockAt",0));v.put("expires_at",o.optLong("expiresAt",0));v.put("is_inbox",o.optBoolean("inbox")?1:0);v.put("quick_copy",o.optBoolean("quickCopy")?1:0);if(db.insert("notes",null,v)==-1){skipped++;continue;}repo.setTags(id,o.optString("tags",""));imported++;}catch(Exception bad){skipped++;}}db.setTransactionSuccessful();}finally{db.endTransaction();}
        return new ImportResult(imported,skipped);
    }

    public static ImportResult importText(Context c,InputStream in) throws Exception {String text=readAll(in),title="",body=text;if(text.startsWith("# ")){int nl=text.indexOf('\n');if(nl>0){title=text.substring(2,nl).trim();body=text.substring(nl+1).trim();}}new NoteRepository(c).createNote(title,body);return new ImportResult(1,0);}
    public static String readAll(InputStream in) throws Exception {ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toString(StandardCharsets.UTF_8.name());}
}
