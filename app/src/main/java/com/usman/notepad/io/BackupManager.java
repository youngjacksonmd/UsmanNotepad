package com.usman.notepad.io;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.usman.notepad.data.NotepadDb;
import org.json.JSONArray;
import org.json.JSONObject;

public final class BackupManager {
    private static final String FORMAT = "UsmanNotepadBackup";
    private BackupManager() {}

    public static String exportAll(Context context) throws Exception {
        SQLiteDatabase db = new NotepadDb(context).getReadableDatabase();
        JSONObject root = new JSONObject();
        root.put("format", FORMAT);
        root.put("version", 2);
        root.put("exportedAt", System.currentTimeMillis());
        root.put("notes", dump(db, "SELECT * FROM notes", new String[]{"id","title","body","mode","created_at","updated_at","folder_id","is_pinned","is_favorite","is_archived","is_deleted","deleted_at","is_locked","theme_key","unlock_at","expires_at","is_inbox","is_quick_copy","is_scratch","is_daily"}));
        root.put("folders", dump(db, "SELECT * FROM folders", new String[]{"id","name","parent_id","created_at"}));
        root.put("tags", dump(db, "SELECT * FROM tags", new String[]{"id","name"}));
        root.put("noteTags", dump(db, "SELECT * FROM note_tags", new String[]{"note_id","tag_id"}));
        root.put("revisions", dump(db, "SELECT * FROM note_revisions", new String[]{"id","note_id","title","body","created_at"}));
        root.put("links", dump(db, "SELECT * FROM note_links", new String[]{"source_note_id","target_note_id","link_text"}));
        root.put("reminders", dump(db, "SELECT * FROM reminders", new String[]{"id","note_id","trigger_at","latitude","longitude","radius_m","enabled"}));
        root.put("attachments", dump(db, "SELECT * FROM attachments", new String[]{"id","note_id","type","local_path","created_at"}));
        root.put("templates", dump(db, "SELECT * FROM templates", new String[]{"id","name","body","mode"}));
        root.put("mediaNotice", "Attachment metadata is included. App-owned media files remain on-device and should be copied separately when moving between devices.");
        return root.toString(2);
    }

    public static int restoreAll(Context context, String raw) throws Exception {
        JSONObject root = new JSONObject(raw);
        if (!FORMAT.equals(root.optString("format"))) throw new IllegalArgumentException("Not an UsmanNotepad backup");
        SQLiteDatabase db = new NotepadDb(context).getWritableDatabase();
        db.beginTransaction();
        int restored = 0;
        try {
            IdMap noteIds = new IdMap();
            IdMap folderIds = new IdMap();
            IdMap tagIds = new IdMap();
            JSONArray folders = root.optJSONArray("folders");
            if (folders != null) {
                for (int i=0;i<folders.length();i++) {
                    JSONObject o=folders.getJSONObject(i); long old=o.optLong("id"); ContentValues v=new ContentValues(); v.put("name",o.optString("name","Folder")); v.put("created_at",o.optLong("created_at",System.currentTimeMillis())); long id=db.insert("folders",null,v); folderIds.put(old,id);
                }
                for (int i=0;i<folders.length();i++) {JSONObject o=folders.getJSONObject(i);if(!o.isNull("parent_id")){long child=folderIds.get(o.optLong("id"));long parent=folderIds.get(o.optLong("parent_id"));if(child>0&&parent>0){ContentValues v=new ContentValues();v.put("parent_id",parent);db.update("folders",v,"id=?",new String[]{String.valueOf(child)});}}}
            }
            JSONArray tags=root.optJSONArray("tags"); if(tags!=null)for(int i=0;i<tags.length();i++){JSONObject o=tags.getJSONObject(i);long old=o.optLong("id");ContentValues v=new ContentValues();v.put("name",uniqueTagName(db,o.optString("name","tag")));long id=db.insert("tags",null,v);tagIds.put(old,id);}
            JSONArray notes=root.optJSONArray("notes"); if(notes!=null)for(int i=0;i<notes.length();i++){JSONObject o=notes.getJSONObject(i);long old=o.optLong("id");long id=allocateNoteId(db,old,i);ContentValues v=noteValues(o,id,folderIds);db.insertOrThrow("notes",null,v);noteIds.put(old,id);restored++;}
            restoreJoin(db,root.optJSONArray("noteTags"),noteIds,tagIds);
            restoreRevisions(db,root.optJSONArray("revisions"),noteIds);
            restoreLinks(db,root.optJSONArray("links"),noteIds);
            restoreReminders(db,root.optJSONArray("reminders"),noteIds);
            restoreAttachments(db,root.optJSONArray("attachments"),noteIds);
            restoreTemplates(db,root.optJSONArray("templates"));
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
        return restored;
    }

    private static JSONArray dump(SQLiteDatabase db,String sql,String[] cols)throws Exception{JSONArray out=new JSONArray();try(Cursor c=db.rawQuery(sql,null)){while(c.moveToNext()){JSONObject o=new JSONObject();for(String col:cols){int idx=c.getColumnIndex(col);if(idx<0||c.isNull(idx)){o.put(col,JSONObject.NULL);continue;}int type=c.getType(idx);if(type==Cursor.FIELD_TYPE_INTEGER)o.put(col,c.getLong(idx));else if(type==Cursor.FIELD_TYPE_FLOAT)o.put(col,c.getDouble(idx));else o.put(col,c.getString(idx));}out.put(o);}}return out;}
    private static ContentValues noteValues(JSONObject o,long id,IdMap folders){ContentValues v=new ContentValues();v.put("id",id);v.put("title",o.optString("title",""));v.put("body",o.optString("body",""));v.put("mode",o.optString("mode","text"));v.put("created_at",o.optLong("created_at",System.currentTimeMillis()));v.put("updated_at",o.optLong("updated_at",System.currentTimeMillis()));if(!o.isNull("folder_id")){long f=folders.get(o.optLong("folder_id"));if(f>0)v.put("folder_id",f);}copyLong(v,o,"is_pinned");copyLong(v,o,"is_favorite");copyLong(v,o,"is_archived");copyLong(v,o,"is_deleted");copyLong(v,o,"deleted_at");copyLong(v,o,"is_locked");v.put("theme_key",o.optString("theme_key","system"));copyLong(v,o,"unlock_at");copyLong(v,o,"expires_at");copyLong(v,o,"is_inbox");copyLong(v,o,"is_quick_copy");copyLong(v,o,"is_scratch");copyLong(v,o,"is_daily");return v;}
    private static void copyLong(ContentValues v,JSONObject o,String k){if(!o.isNull(k))v.put(k,o.optLong(k));}
    private static long allocateNoteId(SQLiteDatabase db,long preferred,int salt){if(preferred>0&&!exists(db,"notes","id",preferred))return preferred;long id=System.currentTimeMillis()+salt;while(exists(db,"notes","id",id))id++;return id;}
    private static boolean exists(SQLiteDatabase db,String table,String col,long id){try(Cursor c=db.rawQuery("SELECT 1 FROM "+table+" WHERE "+col+"=? LIMIT 1",new String[]{String.valueOf(id)})){return c.moveToFirst();}}
    private static String uniqueTagName(SQLiteDatabase db,String base){String name=base.trim().isEmpty()?"tag":base.trim();String candidate=name;int n=2;while(true){try(Cursor c=db.rawQuery("SELECT 1 FROM tags WHERE name=?",new String[]{candidate})){if(!c.moveToFirst())return candidate;}candidate=name+" ("+(n++)+")";}}
    private static void restoreJoin(SQLiteDatabase db,JSONArray a,IdMap notes,IdMap tags)throws Exception{if(a==null)return;for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long n=notes.get(o.optLong("note_id")),t=tags.get(o.optLong("tag_id"));if(n>0&&t>0){ContentValues v=new ContentValues();v.put("note_id",n);v.put("tag_id",t);db.insertWithOnConflict("note_tags",null,v,SQLiteDatabase.CONFLICT_IGNORE);}}}
    private static void restoreRevisions(SQLiteDatabase db,JSONArray a,IdMap notes)throws Exception{if(a==null)return;for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long n=notes.get(o.optLong("note_id"));if(n<=0)continue;ContentValues v=new ContentValues();v.put("note_id",n);v.put("title",o.optString("title",""));v.put("body",o.optString("body",""));v.put("created_at",o.optLong("created_at",System.currentTimeMillis()));db.insert("note_revisions",null,v);}}
    private static void restoreLinks(SQLiteDatabase db,JSONArray a,IdMap notes)throws Exception{if(a==null)return;for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long s=notes.get(o.optLong("source_note_id")),t=notes.get(o.optLong("target_note_id"));if(s<=0||t<=0)continue;ContentValues v=new ContentValues();v.put("source_note_id",s);v.put("target_note_id",t);v.put("link_text",o.optString("link_text",""));db.insertWithOnConflict("note_links",null,v,SQLiteDatabase.CONFLICT_IGNORE);}}
    private static void restoreReminders(SQLiteDatabase db,JSONArray a,IdMap notes)throws Exception{if(a==null)return;for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long n=notes.get(o.optLong("note_id"));if(n<=0)continue;ContentValues v=new ContentValues();v.put("note_id",n);v.put("trigger_at",o.optLong("trigger_at"));if(!o.isNull("latitude"))v.put("latitude",o.optDouble("latitude"));if(!o.isNull("longitude"))v.put("longitude",o.optDouble("longitude"));if(!o.isNull("radius_m"))v.put("radius_m",o.optLong("radius_m"));v.put("enabled",o.optLong("enabled",1));db.insert("reminders",null,v);}}
    private static void restoreAttachments(SQLiteDatabase db,JSONArray a,IdMap notes)throws Exception{if(a==null)return;for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);long n=notes.get(o.optLong("note_id"));if(n<=0)continue;ContentValues v=new ContentValues();v.put("note_id",n);v.put("type",o.optString("type","file"));v.put("local_path",o.optString("local_path",""));v.put("created_at",o.optLong("created_at",System.currentTimeMillis()));db.insert("attachments",null,v);}}
    private static void restoreTemplates(SQLiteDatabase db,JSONArray a)throws Exception{if(a==null)return;for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);ContentValues v=new ContentValues();v.put("name",o.optString("name","Template"));v.put("body",o.optString("body",""));v.put("mode",o.optString("mode","text"));db.insertWithOnConflict("templates",null,v,SQLiteDatabase.CONFLICT_IGNORE);}}
    private static final class IdMap{private final java.util.HashMap<Long,Long>m=new java.util.HashMap<>();void put(long a,long b){m.put(a,b);}long get(long a){Long b=m.get(a);return b==null?-1:b;}}
}
