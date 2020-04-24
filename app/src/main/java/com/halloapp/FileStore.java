package com.halloapp;

import android.content.Context;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.util.Log;

import java.io.File;

public class FileStore {

    private static FileStore instance;

    private final File mediaDir;
    private final File tmpDir;
    private final File cameraDir;
    private final File avatarDir;

    public static FileStore getInstance(final @NonNull Context context) {
        if (instance == null) {
            synchronized(FileStore.class) {
                if (instance == null) {
                    instance = new FileStore(context);
                }
            }
        }
        return instance;
    }

    private FileStore(@NonNull Context context) {
        final StrictMode.ThreadPolicy threadPolicy = StrictMode.getThreadPolicy();
        StrictMode.allowThreadDiskReads();
        StrictMode.allowThreadDiskWrites();
        try {
            mediaDir = prepareDir(new File(context.getFilesDir(), "media"));
            tmpDir = prepareDir(new File(context.getCacheDir(), "media"));
            cameraDir = prepareDir(new File(context.getCacheDir(), "camera"));
            avatarDir = prepareDir(new File(context.getFilesDir(), "avatars"));
        } finally {
            StrictMode.setThreadPolicy(threadPolicy);
        }
    }

    private File prepareDir(@NonNull File dir) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("FileStore: cannot create " + dir.getAbsolutePath());
            }
        }
        return dir;
    }

    public File getMediaDir() {
        return mediaDir;
    }

    public File getMediaFile(@Nullable String name) {
        return name == null ? null : new File(getMediaDir(), name);
    }

    public File getTmpDir() {
        return tmpDir;
    }

    public File getTmpFile(@NonNull String name) {
        return new File(getTmpDir(), name);
    }

    public File getImageCaptureFile() {
        return new File(cameraDir, "capture.jpg");
    }

    public File getAvatarFile(String jid) {
        return new File(avatarDir, jid + ".jpg");
    }

    @WorkerThread
    public void cleanup() {
        cleanupDir(tmpDir);
        cleanupDir(cameraDir);
    }

    private void cleanupDir(@NonNull File dir) {
        final File [] files = dir.listFiles();
        if (files == null) {
            Log.w("FileStore.cleanupDir: no files in " + dir.getAbsolutePath());
            return;
        }
        int deleteCount = 0;
        for (File file : files) {
            if (file.lastModified() < System.currentTimeMillis() - Constants.POSTS_EXPIRATION) {
                if (!file.delete()) {
                    Log.e("FileStore.cleanupDir: cannot delete " + file.getAbsolutePath());
                } else {
                    deleteCount++;
                }
            }
        }
        Log.i("FileStore.cleanupDir: " + deleteCount + " file(s) deleted from " + dir.getAbsolutePath());
    }
}
