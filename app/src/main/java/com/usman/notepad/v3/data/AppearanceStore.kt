package com.usman.notepad.v3.data

import android.content.Context

enum class V3ThemeMode { SYSTEM, LIGHT, DARK, OLED }
enum class V3Accent { INDIGO, VIOLET, SAGE, ROSE, SKY, GRAPHITE }
enum class V3Density { CALM, MODERN, COMPACT }

data class AppearancePrefs(
    val themeMode: V3ThemeMode = V3ThemeMode.SYSTEM,
    val accent: V3Accent = V3Accent.INDIGO,
    val density: V3Density = V3Density.MODERN,
    val textScale: Float = 1.0f
)

object AppearanceLogic {
    fun defaults() = AppearancePrefs()
    fun clampTextScale(value: Float) = value.coerceIn(0.85f, 1.25f)
    fun themeFrom(raw: String?): V3ThemeMode = runCatching { V3ThemeMode.valueOf(raw ?: "") }.getOrDefault(V3ThemeMode.SYSTEM)
    fun accentFrom(raw: String?): V3Accent = runCatching { V3Accent.valueOf(raw ?: "") }.getOrDefault(V3Accent.INDIGO)
    fun densityFrom(raw: String?): V3Density = runCatching { V3Density.valueOf(raw ?: "") }.getOrDefault(V3Density.MODERN)
}

object AppearanceStore {
    private const val PREFS = "v3_appearance"
    fun load(context: Context): AppearancePrefs {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return AppearancePrefs(
            themeMode = AppearanceLogic.themeFrom(p.getString("theme", null)),
            accent = AppearanceLogic.accentFrom(p.getString("accent", null)),
            density = AppearanceLogic.densityFrom(p.getString("density", null)),
            textScale = AppearanceLogic.clampTextScale(p.getFloat("text_scale", 1.0f))
        )
    }

    fun save(context: Context, prefs: AppearancePrefs) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("theme", prefs.themeMode.name)
            .putString("accent", prefs.accent.name)
            .putString("density", prefs.density.name)
            .putFloat("text_scale", AppearanceLogic.clampTextScale(prefs.textScale))
            .apply()
    }
}