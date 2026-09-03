package com.usman.notepad;

import org.junit.Test;
import static org.junit.Assert.*;
import com.usman.notepad.util.SmartTools;

public class SmartToolsTest {
    @Test public void extractsActions(){assertEquals(2,SmartTools.actionItems("TODO call Sam\nnormal line\n☐ buy milk").size());}
    @Test public void relatedScoreFindsOverlap(){assertTrue(SmartTools.relatedScore("Android app","build offline notes","Android project","offline app ideas")>=2);}
    @Test public void summaryIsBounded(){assertTrue(SmartTools.quickSummary("First important line\nSecond line\nThird line\nFourth line").length()<=261);}
}
