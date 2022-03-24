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
    private static final int MAX_LOG_QUEUE = 5000;

    private final FileStore fileStore;

    private final LinkedBlockingQueue<LogLine> logQueue;

    private boolean logging;

    private final LoggingThread loggingThread;

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

        private String fileDate;
        private FileWriter outputStream;

        private final SimpleDateFormat logDateFormatter;
        private final SimpleDateFormat logTimeFormatter;

        private LoggingThread() {
            logDateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            logTimeFormatter = new SimpleDateFormat("HH:mm:ss.SSS Z", Locale.US);
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
                } catch (InterruptedException | IOException ignored) {
                }
            }
            try {
                if (outputStream != null) {
                    outputStream.flush();
                }
                FileUtils.closeSilently(outputStream);
            } catch (Exception ignored) {
            }
        }

        private void prepareLogFile() throws IOException {
            String output = getFileDate();
            if (!output.equals(fileDate)) {
                File logFile = fileStore.getLogFile(output);
                FileUtils.closeSilently(outputStream);
                outputStream = null;
                outputStream = new FileWriter(logFile, true);
                fileDate = output;
                fileStore.purgeOldLogFiles();
            }
        }

        private void writeTimestamp() throws IOException {
            outputStream.write(getLineTime());
            outputStream.write(" ");
        }

        private void writePriority(int priority) throws IOException {
            outputStream.write('/');
            outputStream.write(convertPriorityToChar(priority));
            outputStream.write(": ");
        }

        private void writeLogToDisk(@NonNull LogLine logLine) throws IOException {
            if (logLine.message.length() < MAX_LOG_LENGTH) {
                writeTimestamp();
                outputStream.write(logLine.tag);
                writePriority(logLine.priority);
                outputStream.write(logLine.message);
                outputStream.write('\n');
                if (logLine.t != null) {
                    outputStream.write(android.util.Log.getStackTraceString(logLine.t));
                    outputStream.write('\n');
                }
                outputStream.flush();
                return;
            }

            // Split by line, then ensure each line can fit into Log's maximum length.
            for (int i = 0, length = logLine.message.length(); i < length; i++) {
                if (i == 0) {
                    writeTimestamp();
                } else {
                    outputStream.write("    ");
                }

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
                outputStream.write('\n');
            }
            outputStream.flush();
        }

        private String getFileDate() {
            return logDateFormatter.format(new Date());
        }

        private String getLineTime() {
            return logTimeFormatter.format(new Date());
        }
    }

    @WorkerThread
    public void flushLogs() {
        logging = false;
        try {
            loggingThread.join();
        } catch (InterruptedException ignored) {
        }
    }
}
