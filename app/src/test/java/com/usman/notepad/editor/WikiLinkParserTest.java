package com.usman.notepad.editor;
import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;
public class WikiLinkParserTest {
    @Test public void extractsUniqueLinks(){assertEquals(Arrays.asList("Alpha","Beta"),WikiLinkParser.extractTitles("[[Alpha]] x [[Beta]] y [[Alpha]]"));}
    @Test public void ignoresMalformed(){assertTrue(WikiLinkParser.extractTitles("[[bad [ x ]] and []").isEmpty());}
}
