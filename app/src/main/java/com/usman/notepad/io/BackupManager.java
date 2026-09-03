package com.usman.notepad.io;

import android.content.Context;
import android.database.Cursor;
import com.usman.notepad.data.NotepadDb;
import com.usman.notepad.model.Note;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class BackupManager {
    private BackupManager() {}

    public static String encodeNotes(List<Note> notes) throws Exception {
        JSONArray a=new JSONArray();
        for(Note n:notes){JSONObject o=new JSONObject();o.put("id",n.id);o.put("title",n.title);o.put("body",n.body);o.put("mode",n.mode);o.put("createdAt",n.createdAt);o.put("updatedAt",n.updatedAt);o.put("pinned",n.pinned);o.put("favorite",n.favorite);o.put("archived",n.archived);o.put("deleted",n.deleted);o.put("deletedAt",n.deletedAt);o.put("locked",n.locked);o.put("theme",n.themeKey);o.put("unlockAt",n.unlockAt);o.put("expiresAt",n.expiresAt);o.put("inbox",n.inbox);o.put("quickCopy",n.quickCopy);a.put(o);}
        JSONObject root=new JSONObject();root.put("format","UsmanNotepadBackup");root.put("version",2);root.put("notes",a);return root.toString(2);
    }

    public static List<Note> decodeNotes(String raw) throws Exception {
        List<Note>out=new ArrayList<>();JSONObject root=new JSONObject(raw);if(!"UsmanNotepadBackup".equals(root.optString("format")))throw new IllegalArgumentException("Not an UsmanNotepad backup");JSONArray a=root.getJSONArray("notes");
        for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);Note n=new Note();n.id=o.optLong("id");n.title=o.optString("title");n.body=o.optString("body");n.mode=o.optString("mode","text");n.createdAt=o.optLong("createdAt");n.updatedAt=o.optLong("updatedAt");n.pinned=o.optBoolean("pinned");n.favorite=o.optBoolean("favorite");n.archived=o.optBoolean("archived");n.deleted=o.optBoolean("deleted");n.deletedAt=o.optLong("deletedAt");n.locked=o.optBoolean("locked");n.themeKey=o.optString("theme","system");n.unlockAt=o.optLong("unlockAt");n.expiresAt=o.optLong("expiresAt");n.inbox=o.optBoolean("inbox");n.quickCopy=o.optBoolean("quickCopy");out.add(n);}return out;
    }

    public static List<Note> loadAll(Context context){
        List<Note> out=new ArrayList<>();
        try(Cursor c=new NotepadDb(context).getReadableDatabase().rawQuery("SELECT * FROM notes ORDER BY updated_at DESC",null)){
            while(c.moveToNext()){Note n=new Note();n.id=l(c,"id");n.title=s(c,"title");n.body=s(c,"body");n.mode=s(c,"mode");n.createdAt=l(c,"created_at");n.updatedAt=l(c,"updated_at");int fi=c.getColumnIndex("folder_id");n.folderId=fi>=0&&!c.isNull(fi)?c.getLong(fi):null;n.pinned=l(c,"is_pinned")!=0;n.favorite=l(c,"is_favorite")!=0;n.archived=l(c,"is_archived")!=0;n.deleted=l(c,"is_deleted")!=0;n.deletedAt=l(c,"deleted_at");n.locked=l(c,"is_locked")!=0;n.themeKey=s(c,"theme_key");n.unlockAt=l(c,"unlock_at");n.expiresAt=l(c,"expires_at");n.inbox=l(c,"is_inbox")!=0;n.quickCopy=l(c,"is_quick_copy")!=0;out.add(n);}
        }
        return out;
    }
    private static long l(Cursor c,String x){int i=c.getColumnIndex(x);return i<0||c.isNull(i)?0:c.getLong(i);} private static String s(Cursor c,String x){int i=c.getColumnIndex(x);return i<0||c.isNull(i)?"":c.getString(i);}
}
