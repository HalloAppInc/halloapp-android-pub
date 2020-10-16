package com.halloapp.util.logs;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.halloapp.FileStore;
import com.halloapp.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FileLogger {

    private static final int MAX_LOG_LENGTH = 4000;
    private static final int MAX_LOG_QUEUE = 1000;

    private final FileStore fileStore;

    private LinkedBlockingQueue<LogLine> logQueue;

    private boolean logging;

    private LoggingThread loggingThread;

    public FileLogger(@NonNull FileStore fileStore) {
        this.fileStore = fileStore;

        logQueue = new LinkedBlockingQueue<>(MAX_LOG_QUEUE);
        logging = true;
        loggingThread = new LoggingThread();
        loggingThread.start();
    }

    protected final char convertPriorityToChar(int priority) {
        switch (priority) {
            default:
            case Log.VERBOSE:
                return 'V';
            case Log.DEBUG:
                return 'D';
            case Log.INFO:
                return 'I';
            case Log.WARN:
                return 'W';
            case Log.ERROR:
                return 'E';
            case Log.ASSERT:
                return 'A';
        }
    }

    public void log(int priority, @NonNull String message, @Nullable Throwable t) {
        if (!logging) {
            return;
        }
        String tag = Thread.currentThread().getName();
        try {
            logQueue.add(new LogLine(priority, tag, message, t));
        } catch (IllegalStateException e) {
            // Queue has reached capacity
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private static class LogLine {

        public LogLine(int priority, @NonNull String tag, @NonNull String message, @Nullable Throwable t) {
            this.priority = priority;
            this.tag = tag;
            this.message = message;
            this.t = t;
        }

        final int priority;
        final @NonNull String tag;
        final @NonNull String message;
        final @Nullable Throwable t;
    }

    private class LoggingThread extends Thread {

        private String fileTimestamp;
        private FileWriter outputStream;

        private SimpleDateFormat timeFormatter;


        private LoggingThread() {
            timeFormatter = new SimpleDateFormat("yyyy-MM-dd",
                    Locale.getDefault());
        }

        @Override
        public void run() {
            fileStore.purgeOldLogFiles();

            while (logging || !logQueue.isEmpty()) {
                try {
                    LogLine logLine = logQueue.poll(5, TimeUnit.SECONDS);
                    if (logLine == null) {
                        continue;
                    }
                    prepareLogFile();
                    writeLogToDisk(logLine);
                } catch (InterruptedException | IOException e) {
                }
            }
            try {
                if (outputStream != null) {
                    outputStream.flush();
                }
                FileUtils.closeSilently(outputStream);
            } catch (Exception e) {
            }
        }

        private void prepareLogFile() throws IOException {
            String output = getFileTimestamp();
            if (!output.equals(fileTimestamp)) {
                File logFile = fileStore.getLogFile(output);
                FileUtils.closeSilently(outputStream);
                outputStream = null;
                outputStream = new FileWriter(logFile, true);
                fileTimestamp = output;
                fileStore.purgeOldLogFiles();
            }
        }

        private void writePriority(int priority) throws IOException {
            outputStream.write('/');
            outputStream.write(convertPriorityToChar(priority));
            outputStream.write(": ");
        }

        private void writeLogToDisk(@NonNull LogLine logLine) throws IOException {
            if (logLine.message.length() < MAX_LOG_LENGTH) {
                outputStream.write(logLine.tag);
                writePriority(logLine.priority);
                outputStream.write(logLine.message);
                outputStream.write('\n');
                if (logLine.t != null) {
                    outputStream.write(android.util.Log.getStackTraceString(logLine.t));
                }
                return;
            }

            // Split by line, then ensure each line can fit into Log's maximum length.
            for (int i = 0, length = logLine.message.length(); i < length; i++) {
                int newline = logLine.message.indexOf('\n', i);
                newline = newline != -1 ? newline : length;
                do {
                    int end = Math.min(newline, i + MAX_LOG_LENGTH);
                    String part = logLine.message.substring(i, end);
                    outputStream.write(logLine.tag);
                    writePriority(logLine.priority);
                    outputStream.write(part);
                    outputStream.write('\n');
                    i = end;
                } while (i < newline);
            }
            if (logLine.t != null) {
                outputStream.write(android.util.Log.getStackTraceString(logLine.t));
            }
        }

        private String getFileTimestamp() {
            return timeFormatter.format(new Date());
        }
    }

    @WorkerThread
    public void flushLogs() {
        logging = false;
        try {
            loggingThread.join();
        } catch (InterruptedException e) {
        }
    }
}
