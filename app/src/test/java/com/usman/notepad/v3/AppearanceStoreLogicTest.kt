package com.usman.notepad.v3

import com.usman.notepad.v3.data.AppearanceLogic
import com.usman.notepad.v3.data.V3Accent
import com.usman.notepad.v3.data.V3Density
import com.usman.notepad.v3.data.V3ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceStoreLogicTest {
    @Test fun defaults_are_stable() {
        val p = AppearanceLogic.defaults()
        assertEquals(V3ThemeMode.SYSTEM, p.themeMode)
        assertEquals(V3Accent.INDIGO, p.accent)
        assertEquals(V3Density.MODERN, p.density)
        assertEquals(1.0f, p.textScale)
    }

    @Test fun text_scale_is_clamped() {
        assertEquals(0.85f, AppearanceLogic.clampTextScale(0.5f))
        assertEquals(1.25f, AppearanceLogic.clampTextScale(1.8f))
        assertEquals(1.1f, AppearanceLogic.clampTextScale(1.1f))
    }

    @Test fun invalid_names_fall_back_to_defaults() {
        assertEquals(V3ThemeMode.SYSTEM, AppearanceLogic.themeFrom("nope"))
        assertEquals(V3Accent.INDIGO, AppearanceLogic.accentFrom("nope"))
        assertEquals(V3Density.MODERN, AppearanceLogic.densityFrom("nope"))
    }
}
