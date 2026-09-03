package com.usman.notepad.editor;
import com.usman.notepad.model.Note;
import org.junit.Test;
import static org.junit.Assert.*;
public class SmartExtractorsTest {
    @Test public void findsActionItems(){Note n=new Note(1,"","hello\nTODO buy milk\n☐ call Sam\nrandom",1,1);assertEquals(2,SmartExtractors.actionItems(n).size());}
    @Test public void summaryIsBounded(){Note n=new Note(1,"","First line.\nSecond line.\nThird line.\nFourth line.",1,1);assertTrue(SmartExtractors.quickSummary(n).contains("First line."));assertFalse(SmartExtractors.quickSummary(n).contains("Fourth line."));}
}
