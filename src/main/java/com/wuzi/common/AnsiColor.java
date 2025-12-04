package com.wuzi.common;

public class AnsiColor {
    public static final String RESET = "\u001B[0m";
    
    // Text Colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Bold
    public static final String BOLD = "\u001B[1m";
    
    // Backgrounds
    public static final String BG_RED = "\u001B[41m";
    
    public static String color(String text, String colorCode) {
        return colorCode + text + RESET;
    }
    
    public static String info(String text) {
        return color(text, CYAN);
    }
    
    public static String success(String text) {
        return color(text, GREEN);
    }
    
    public static String error(String text) {
        return color(text, RED);
    }
    
    public static String warn(String text) {
        return color(text, YELLOW);
    }
    
    public static String bold(String text) {
        return color(text, BOLD);
    }
}
