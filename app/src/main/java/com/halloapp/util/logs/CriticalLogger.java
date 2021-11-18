package com.halloapp.util.logs;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.FileStore;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CriticalLogger {

    private final SimpleDateFormat logDateTimeFormatter;
    private final File logFile;

    private FileWriter outputStream;

    public CriticalLogger(@NonNull FileStore fileStore) {
        logDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z", Locale.getDefault());
        logFile = fileStore.getCriticalLogFile();
    }

    @WorkerThread
    public void log(@NonNull String message, @Nullable Throwable t) throws IOException {
        if (outputStream == null) {
            outputStream = new FileWriter(logFile, true);
        }
        String tag = Thread.currentThread().getName();
        writeLogToDisk(tag, message, t);
    }

    private void writeLogToDisk(@NonNull String tag, @NonNull String message, @Nullable Throwable t) throws IOException {
        outputStream.write(logDateTimeFormatter.format(new Date()));
        outputStream.write(" ");
        outputStream.write(tag);
        outputStream.write("/C: ");
        outputStream.write(message);
        outputStream.write('\n');
        if (t != null) {
            outputStream.write(Log.getStackTraceString(t));
            outputStream.write('\n');
        }
        outputStream.flush();
    }
}
