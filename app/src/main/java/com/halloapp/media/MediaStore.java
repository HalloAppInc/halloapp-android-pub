package com.halloapp.media;

import android.content.Context;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.util.Log;

import java.io.File;

public class MediaStore {

    private static MediaStore instance;

    private final File mediaDir;
    private final File tmpDir;
    private final File cameraDir;

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
        } finally {
            StrictMode.setThreadPolicy(threadPolicy);
        }
    }

    private File prepareDir(@NonNull File dir) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("MediaStore: cannot create " + mediaDir.getAbsolutePath());
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
}
