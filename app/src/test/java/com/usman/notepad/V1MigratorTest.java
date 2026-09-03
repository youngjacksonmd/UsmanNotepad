package com.usman.notepad;

import org.junit.Test;
import static org.junit.Assert.*;
import com.usman.notepad.data.V1Migrator;

public class V1MigratorTest {
    @Test public void countsValidV1Notes(){assertEquals(2,V1Migrator.countValid("[{\"id\":1,\"title\":\"A\"},{\"id\":2,\"body\":\"اردو\"}]"));}
    @Test public void malformedInputDoesNotCrash(){assertEquals(0,V1Migrator.countValid("not-json"));}
}
