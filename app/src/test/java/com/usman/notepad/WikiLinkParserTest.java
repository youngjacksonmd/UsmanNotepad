package com.usman.notepad;

import org.junit.Test;
import static org.junit.Assert.*;
import com.usman.notepad.util.WikiLinkParser;

public class WikiLinkParserTest {
    @Test public void parsesUniqueLinks(){assertEquals(2,WikiLinkParser.parse("See [[Project]] and [[Ideas]] then [[Project]]").size());}
    @Test public void trimsTitle(){assertEquals("Project",WikiLinkParser.parse("[[  Project  ]]").get(0));}
}
