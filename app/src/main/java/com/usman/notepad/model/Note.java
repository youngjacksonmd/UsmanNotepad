package com.usman.notepad.model;

public class Note {
    public long id;
    public String title = "";
    public String body = "";
    public String mode = "text";
    public long createdAt;
    public long updatedAt;
    public Long folderId;
    public boolean pinned;
    public boolean favorite;
    public boolean archived;
    public boolean deleted;
    public long deletedAt;
    public boolean locked;
    public String themeKey = "system";
    public long unlockAt;
    public long expiresAt;
    public boolean inbox;
    public boolean quickCopy;
    public boolean scratch;
    public boolean daily;

    public Note() {}

    public Note(long id, String title, String body, long updatedAt) {
        this.id = id;
        this.title = title == null ? "" : title;
        this.body = body == null ? "" : body;
        this.createdAt = updatedAt;
        this.updatedAt = updatedAt;
    }

    public String displayTitle() {
        String t = title == null ? "" : title.trim();
        return t.isEmpty() ? "Untitled note" : t;
    }
}
