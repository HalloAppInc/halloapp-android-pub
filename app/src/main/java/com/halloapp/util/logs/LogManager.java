package com.halloapp.util.logs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.FileStore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
    public void zipLocalLogs(@NonNull Context context, File output) {
        File logDir = FileStore.getInstance().getLogDir();
        File[] logFiles = logDir.listFiles();
        if (logFiles == null) {
            Log.e("LogManager/zipCrashlyticsLogs no logs to zip up");
            return;
        }
        zip(logFiles, output);
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
