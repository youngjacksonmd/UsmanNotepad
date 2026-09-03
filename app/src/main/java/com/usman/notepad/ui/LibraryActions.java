package com.usman.notepad.ui;

import com.usman.notepad.data.NoteRepository;

public final class LibraryActions {
    private LibraryActions() {}
    public static void pin(NoteRepository r,long id){r.togglePin(id);}    
    public static void favorite(NoteRepository r,long id){r.toggleFavorite(id);}    
    public static void archive(NoteRepository r,long id){r.archive(id);}    
    public static void trash(NoteRepository r,long id){r.trash(id);}    
}
