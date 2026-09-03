package com.usman.notepad.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CommandEngine {
    private CommandEngine() {}
    public static String apply(String command, String body, long now) {
        String c = command == null ? "" : command.trim().toLowerCase(Locale.ROOT);
        String b = body == null ? "" : body;
        switch (c) {
            case "check": return b + (b.endsWith("\n") || b.isEmpty() ? "" : "\n") + "☐ ";
            case "date": return b + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(now));
            case "time": return b + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(now));
            case "heading": return b + (b.endsWith("\n") || b.isEmpty() ? "" : "\n") + "## ";
            case "divider": return b + (b.endsWith("\n") || b.isEmpty() ? "" : "\n") + "────────────";
            case "quote": return b + (b.endsWith("\n") || b.isEmpty() ? "" : "\n") + "> ";
            case "link": return b + "[[]]";
            default: return b;
        }
    }
}
