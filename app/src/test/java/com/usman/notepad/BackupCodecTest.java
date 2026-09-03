package com.usman.notepad;

import org.junit.Test;
import static org.junit.Assert.*;
import com.usman.notepad.io.BackupManager;
import com.usman.notepad.model.Note;
import java.util.Arrays;
import java.util.List;

public class BackupCodecTest {
    @Test public void roundTripsNotes() throws Exception {Note n=new Note(7,"Title","Body",123);n.mode="journal";List<Note> decoded=BackupManager.decodeNotes(BackupManager.encodeNotes(Arrays.asList(n)));assertEquals(1,decoded.size());assertEquals("Title",decoded.get(0).title);assertEquals("Body",decoded.get(0).body);assertEquals("journal",decoded.get(0).mode);}
}
