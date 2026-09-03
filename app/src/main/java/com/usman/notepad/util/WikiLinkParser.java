package com.usman.notepad.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WikiLinkParser {
    private static final Pattern P = Pattern.compile("\\[\\[([^\\]]{1,120})\\]\\]");
    private WikiLinkParser() {}
    public static List<String> parse(String body) {
        List<String> out = new ArrayList<>();
        Matcher m = P.matcher(body == null ? "" : body);
        while (m.find()) {
            String title = m.group(1).trim();
            if (!title.isEmpty() && !out.contains(title)) out.add(title);
        }
        return out;
    }
}
