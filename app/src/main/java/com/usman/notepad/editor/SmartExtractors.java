package com.usman.notepad.editor;

import com.usman.notepad.model.Note;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SmartExtractors {
    private SmartExtractors(){}
    public static String quickSummary(Note n){
        String b=n==null||n.body==null?"":n.body.trim();
        if(b.isEmpty()) return "No content to summarize.";
        String[] lines=b.split("\\r?\\n");
        StringBuilder s=new StringBuilder();
        int count=0;
        for(String line:lines){
            String x=line.trim();
            if(x.isEmpty()) continue;
            if(s.length()>0) s.append(" ");
            s.append(x);
            if(++count>=3 || s.length()>260) break;
        }
        if(s.length()>280) return s.substring(0,280)+"…";
        return s.toString();
    }
    public static List<String> actionItems(Note n){
        List<String> out=new ArrayList<>();
        if(n==null||n.body==null) return out;
        for(String line:n.body.split("\\r?\\n")){
            String x=line.trim();
            String low=x.toLowerCase(Locale.ROOT);
            if(x.startsWith("☐")||x.startsWith("[ ]")||low.startsWith("todo")||low.startsWith("action:")||low.startsWith("next:")) out.add(x);
        }
        return out;
    }
    public static int score(Note a, Note b, String tagsA, String tagsB, boolean linked){
        if(a==null||b==null||a.id==b.id) return -1;
        int score=linked?100:0;
        Set<String> ta=tokens((a.title+" "+a.body).toLowerCase(Locale.ROOT));
        Set<String> tb=tokens((b.title+" "+b.body).toLowerCase(Locale.ROOT));
        for(String t:ta) if(t.length()>3&&tb.contains(t)) score+=2;
        if(tagsA!=null&&tagsB!=null){
            for(String t:tagsA.split(",")) if(!t.trim().isEmpty()&&tagsB.contains(t.trim())) score+=15;
        }
        return score;
    }
    private static Set<String> tokens(String s){
        Set<String> out=new LinkedHashSet<>();
        for(String t:s.split("[^a-z0-9]+")) if(t.length()>2) out.add(t);
        return out;
    }
}
