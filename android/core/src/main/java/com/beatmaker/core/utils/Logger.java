package com.beatmaker.core.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

    public static void d(String tag, String text) {
        out("D", tag, text);
    }

    public static void i(String tag, String text) {
        out("I", tag, text);
    }

    public static void w(String tag, String text) {
        out("W", tag, text);
    }

    public static void e(String tag, String text) {
        out("E", tag, text);
    }

    public static void f(String tag, String text) {
        out("F", tag, text);
    }

    private static String currentTimestamp() {
        return dateFormat.format(new Date());
    }

    private static void out(String prefix, String tag, String text) {
        String timestamp = currentTimestamp();
        out(timestamp + " " + prefix + "/" + tag + ": " + text);
    }

    private static void out(String output) {
        System.out.println(output);
    }
}
