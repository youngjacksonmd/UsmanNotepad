package com.usman.notepad;

import org.junit.Test;
import static org.junit.Assert.*;

public class RepositoryRulesTest {
    @Test public void retentionWindowIsThirtyDays(){assertEquals(2592000000L,30L*24L*60L*60L*1000L);}
}
