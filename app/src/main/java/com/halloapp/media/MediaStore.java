package com.halloapp.media;

import android.content.Context;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.util.Log;

import java.io.File;

public class MediaStore {

    private static MediaStore instance;

    private final File mediaDir;
    private final File tmpDir;
    private final File cameraDir;
    private final File avatarDir;

    public static MediaStore getInstance(final @NonNull Context context) {
        if (instance == null) {
            synchronized(MediaStore.class) {
                if (instance == null) {
                    instance = new MediaStore(context);
                }
            }
        }
        return instance;
    }

    private MediaStore(@NonNull Context context) {
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
                Log.e("MediaStore: cannot create " + dir.getAbsolutePath());
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
        return new File(avatarDir, jid + ".png");
    }

    @WorkerThread
    public void cleanup() {
        cleanupDir(mediaDir);
        cleanupDir(tmpDir);
        cleanupDir(cameraDir);
    }

    private void cleanupDir(@NonNull File dir) {
        final File [] files = dir.listFiles();
        if (files == null) {
            Log.w("MediaStore.cleanupDir: no files in " + dir.getAbsolutePath());
            return;
        }
        int deleteCount = 0;
        for (File file : files) {
            if (file.lastModified() < System.currentTimeMillis() - Constants.POSTS_EXPIRATION) {
                if (!file.delete()) {
                    Log.e("MediaStore.cleanupDir: cannot delete " + file.getAbsolutePath());
                } else {
                    deleteCount++;
                }
            }
        }
        Log.i("MediaStore.cleanupDir: " + deleteCount + " file(s) deleted from " + dir.getAbsolutePath());
    }
}
