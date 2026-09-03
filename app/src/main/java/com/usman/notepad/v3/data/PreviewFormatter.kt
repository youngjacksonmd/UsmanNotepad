package com.usman.notepad.v3.data

object PreviewFormatter {
    fun title(raw: String?): String = raw.orEmpty().trim().ifEmpty { "Untitled note" }

    fun preview(raw: String?, locked: Boolean): String {
        if (locked) return "Protected note"
        val compact = raw.orEmpty().replace(Regex("\\s+"), " ").trim()
        return if (compact.length <= 120) compact else compact.take(120).trimEnd() + "…"
    }

    fun checklistProgress(raw: String?): Pair<Int, Int> {
        var done = 0
        var total = 0
        raw.orEmpty().lineSequence().forEach { line ->
            val t = line.trimStart()
            when {
                t.startsWith("☑") || t.startsWith("✓") -> { done++; total++ }
                t.startsWith("☐") -> total++
            }
        }
        return done to total
    }
}