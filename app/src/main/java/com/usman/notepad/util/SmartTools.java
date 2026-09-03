package com.usman.notepad.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SmartTools {
    private SmartTools() {}

    public static String quickSummary(String body) {
        if (body == null) return "";
        String[] lines = body.trim().split("\\n+");
        StringBuilder out = new StringBuilder();
        for (String line : lines) {
            String s = line.trim();
            if (s.isEmpty()) continue;
            if (out.length() > 0) out.append(" • ");
            out.append(s.replaceFirst("^[#>*•\\-\\s]+", ""));
            if (out.length() >= 220 || out.toString().split(" • ").length >= 3) break;
        }
        String x = out.toString();
        return x.length() > 260 ? x.substring(0, 260) + "…" : x;
    }

    public static List<String> actionItems(String body) {
        List<String> out = new ArrayList<>();
        if (body == null) return out;
        for (String line : body.split("\\n")) {
            String s = line.trim();
            String lower = s.toLowerCase(Locale.ROOT);
            if (s.startsWith("☐") || lower.startsWith("todo") || lower.startsWith("action:") || lower.startsWith("next:")) out.add(s);
        }
        return out;
    }

    public static int relatedScore(String titleA, String bodyA, String titleB, String bodyB) {
        Set<String> a = tokens((titleA == null ? "" : titleA) + " " + (bodyA == null ? "" : bodyA));
        Set<String> b = tokens((titleB == null ? "" : titleB) + " " + (bodyB == null ? "" : bodyB));
        int common = 0;
        for (String t : a) if (b.contains(t)) common++;
        return common;
    }

    private static Set<String> tokens(String s) {
        Set<String> out = new HashSet<>();
        for (String t : s.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}_]+")) if (t.length() >= 3) out.add(t);
        return out;
    }
}
