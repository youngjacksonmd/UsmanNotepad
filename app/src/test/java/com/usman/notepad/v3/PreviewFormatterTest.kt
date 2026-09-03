package com.usman.notepad.v3

import com.usman.notepad.v3.data.PreviewFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewFormatterTest {
    @Test fun locked_notes_never_expose_body() {
        val preview = PreviewFormatter.preview("Secret body", locked = true)
        assertEquals("Protected note", preview)
        assertFalse(preview.contains("Secret"))
    }

    @Test fun blank_title_falls_back_cleanly() {
        assertEquals("Untitled note", PreviewFormatter.title("   "))
    }

    @Test fun preview_collapses_whitespace_and_limits_length() {
        val raw = "first\n\nsecond   third " + "x".repeat(200)
        val preview = PreviewFormatter.preview(raw, locked = false)
        assertFalse(preview.contains("\n"))
        assertFalse(preview.contains("  "))
        assertTrue(preview.length <= 123)
    }

    @Test fun checklist_progress_counts_common_markers() {
        val progress = PreviewFormatter.checklistProgress("☑ Done\n✓ Also done\n☐ Waiting\n- plain")
        assertEquals(2, progress.first)
        assertEquals(3, progress.second)
    }
}
