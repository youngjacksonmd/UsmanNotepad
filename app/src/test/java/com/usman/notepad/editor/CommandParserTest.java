package com.usman.notepad.editor;
import org.junit.Test;
import static org.junit.Assert.*;
public class CommandParserTest {
    @Test public void parsesSlashCommands(){assertEquals(CommandParser.Command.CHECK,CommandParser.parse("/check"));assertEquals(CommandParser.Command.REMINDER,CommandParser.parse("reminder"));assertEquals(CommandParser.Command.UNKNOWN,CommandParser.parse("/nope"));}
}
