package com.usman.notepad.editor;

public final class CommandParser {
    public enum Command { CHECK, DATE, TIME, HEADING, DIVIDER, QUOTE, REMINDER, LINK, UNKNOWN }
    private CommandParser(){}
    public static Command parse(String token) {
        if(token==null) return Command.UNKNOWN;
        String t=token.trim().toLowerCase();
        if(t.startsWith("/")) t=t.substring(1);
        switch(t){
            case "check": return Command.CHECK;
            case "date": return Command.DATE;
            case "time": return Command.TIME;
            case "heading": return Command.HEADING;
            case "divider": return Command.DIVIDER;
            case "quote": return Command.QUOTE;
            case "reminder": return Command.REMINDER;
            case "link": return Command.LINK;
            default:return Command.UNKNOWN;
        }
    }
}
