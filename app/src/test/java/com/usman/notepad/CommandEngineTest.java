package com.usman.notepad;

import org.junit.Test;
import static org.junit.Assert.*;
import com.usman.notepad.util.CommandEngine;

public class CommandEngineTest {
    @Test public void insertsChecklist(){assertEquals("Hello\n☐ ",CommandEngine.apply("check","Hello",0));}
    @Test public void insertsLink(){assertEquals("[[]]",CommandEngine.apply("link","",0));}
}
