package com.bgeradz.multiplexer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppLogger implements Logger {
    private enum Level {
    	DEBUG,
    	INFO,
    	WARN,
    	ERROR
    }
    
    private static SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.getDefault());
    private String tag;

    public AppLogger(String tag) {
        this.tag = tag;
    }

    @Override
    public void debug(String message) {
        log(Level.DEBUG, tag, message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        log(Level.DEBUG, tag, message, throwable);
    }

    @Override
    public void info(String message) {
        log(Level.INFO, tag, message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        log(Level.INFO, tag, message, throwable);
    }

    @Override
    public void warn(String message) {
        log(Level.WARN, tag, message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        log(Level.WARN, tag, message, throwable);
    }

    @Override
    public void error(String message) {
        log(Level.ERROR, tag, message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log(Level.ERROR, tag, message, throwable);
    }

    
    private static synchronized void log(Level level, String tag, String message) {
        System.out.println(formatMessage(level, tag, message));
    }

    private static void log(Level level, String tag, String message, Throwable throwable) {
        log(level, tag, message + '\n' + getStackTraceString(throwable));
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    
    // Formats message for logging to file.
    private static String formatMessage(Level level, String tag, String msg) {
        StringBuilder sb = new StringBuilder();
        String timestamp = timestampFormatter.format(new Date());
        String[] lines = msg.split("\n");
        for (String line : lines) {
            sb.append(timestamp)
                    .append(' ')
                    .append(level)
                    .append(' ')
                    .append(tag)
                    .append(": ")
                    .append(line)
                    .append('\n');
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
