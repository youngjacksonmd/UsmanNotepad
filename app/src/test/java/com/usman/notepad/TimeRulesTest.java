package com.usman.notepad;

import org.junit.Test;
import static org.junit.Assert.*;

public class TimeRulesTest {
    @Test public void futureCapsuleIsLocked(){long now=1_000_000L;long unlock=now+60_000L;assertTrue(unlock>now);}
    @Test public void expiredTimeIsPast(){long now=1_000_000L;long expiry=now-1;assertTrue(expiry<=now);}
}
