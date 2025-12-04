package com.wuzi.server;

import com.wuzi.common.AnsiColor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerLogger {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String getTimestamp() {
        return AnsiColor.color("[" + LocalDateTime.now().format(formatter) + "]", AnsiColor.WHITE);
    }

    public static void info(String message) {
        System.out.println(getTimestamp() + " " + AnsiColor.info("[INFO] " + message));
    }

    public static void success(String message) {
        System.out.println(getTimestamp() + " " + AnsiColor.success("[SUCCESS] " + message));
    }

    public static void warn(String message) {
        System.out.println(getTimestamp() + " " + AnsiColor.warn("[WARN] " + message));
    }

    public static void error(String message) {
        System.err.println(getTimestamp() + " " + AnsiColor.error("[ERROR] " + message));
    }
}
