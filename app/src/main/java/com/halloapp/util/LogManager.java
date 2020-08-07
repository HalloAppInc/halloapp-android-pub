package com.halloapp.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.firebase.crashlytics.internal.Logger;
import com.google.firebase.crashlytics.internal.persistence.FileStoreImpl;
import com.halloapp.util.crashlytics.QueueFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LogManager {

    private static LogManager instance;

    public static LogManager getInstance() {
        if (instance == null) {
            synchronized (LogManager.class) {
                if (instance == null) {
                    instance = new LogManager();
                }
            }
        }
        return instance;
    }

    @WorkerThread
    public void zipCrashlyticsLogs(@NonNull Context context, File output) {
        FileStoreImpl crashLogsStore = new FileStoreImpl(context);
        File crashLogsDir = crashLogsStore.getFilesDir();
        if (crashLogsDir == null) {
            Log.e("LogManager/zipCrashlyticsLogs Failed to open crashlytics directory");
            return;
        }
        File[] filesFromCrashlytics = crashLogsDir.listFiles();
        if (filesFromCrashlytics == null) {
            Log.e("LogManager/zipCrashlyticsLogs no logs to zip up");
            return;
        }
        zip(filesFromCrashlytics, output);
    }

    public void zip(@NonNull File[] files, @NonNull File zipFile) {
        try {
            FileOutputStream dest = new FileOutputStream(zipFile);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte[] buffer = new byte[2048];

            for (File f : files) {
                addFile(out, f, buffer);
            }
            out.close();
        } catch (Exception e) {
            Log.e("LogManager/zip failed to zip directory", e);
        }
    }

    private void addFile(ZipOutputStream out, File file, byte[] buffer) throws IOException {
        addFile(out, "", file, buffer);
    }

    private void addFile(ZipOutputStream out, @NonNull String parentPath, File file, byte[] buffer) throws IOException {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            File[] dirFiles = file.listFiles();
            if (dirFiles == null) {
                return;
            }
            for (File f : dirFiles) {
                addFile(out, parentPath + file.getName() + "/", f, buffer);
            }
            return;
        }
        final String fileName = file.getName();
        if (fileName.endsWith(".temp")) {
            QueueFile queueFile = new QueueFile(file);
            final int[] lengthHolder = new int[]{0};
            final byte[] logBytes = new byte[queueFile.usedBytes()];

            try {
                queueFile.forEach((in, length) -> {
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        in.read(logBytes, lengthHolder[0], length);
                        lengthHolder[0] += length;
                    } finally {
                        in.close();
                    }

                });
            } catch (IOException e) {
                Log.e("A problem occurred while reading the Crashlytics log file.", e);
                return;
            }
            String logFileName = fileName.substring(0, fileName.length() - 5); // 5 for length of .temp
            ZipEntry entry = new ZipEntry(parentPath + logFileName + ".log");
            out.putNextEntry(entry);
            out.write(logBytes, 0, lengthHolder[0]);

        } else {
            BufferedInputStream origin;
            FileInputStream fi = new FileInputStream(file);
            origin = new BufferedInputStream(fi, buffer.length);
            ZipEntry entry = new ZipEntry(parentPath + file.getName());
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, count);
            }
            origin.close();
        }
    }
}
