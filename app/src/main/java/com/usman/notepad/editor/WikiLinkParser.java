package com.usman.notepad.editor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WikiLinkParser {
    private static final Pattern P=Pattern.compile("\\[\\[([^\\[\\]]{1,160})\\]\\]");
    private WikiLinkParser(){}
    public static List<String> extractTitles(String body){
        Set<String> out=new LinkedHashSet<>();
        Matcher m=P.matcher(body==null?"":body);
        while(m.find()){
            String t=m.group(1).trim();
            if(!t.isEmpty()) out.add(t);
        }
        return new ArrayList<>(out);
    }
}
