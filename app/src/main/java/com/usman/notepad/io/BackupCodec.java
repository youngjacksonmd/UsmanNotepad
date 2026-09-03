package com.usman.notepad.io;

import com.usman.notepad.model.Note;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class BackupCodec {
    private BackupCodec() {}
    public static String encodeNotes(List<Note> notes) throws Exception {
        JSONArray a=new JSONArray();
        for(Note n:notes){JSONObject o=new JSONObject();o.put("id",n.id);o.put("title",n.title);o.put("body",n.body);o.put("mode",n.mode);o.put("createdAt",n.createdAt);o.put("updatedAt",n.updatedAt);a.put(o);}
        JSONObject root=new JSONObject();root.put("format","UsmanNotepadBackup");root.put("version",2);root.put("notes",a);return root.toString();
    }
    public static List<Note> decodeNotes(String raw) throws Exception {
        JSONObject root=new JSONObject(raw);if(!"UsmanNotepadBackup".equals(root.optString("format")))throw new IllegalArgumentException("Invalid backup");
        List<Note> out=new ArrayList<>();JSONArray a=root.getJSONArray("notes");
        for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);Note n=new Note();n.id=o.optLong("id");n.title=o.optString("title");n.body=o.optString("body");n.mode=o.optString("mode","text");n.createdAt=o.optLong("createdAt");n.updatedAt=o.optLong("updatedAt");out.add(n);}return out;
    }
}
