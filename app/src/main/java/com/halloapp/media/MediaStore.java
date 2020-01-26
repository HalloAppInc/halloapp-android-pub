package com.halloapp.media;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.util.Log;

import java.io.File;

public class MediaStore {

    private static MediaStore instance;

    private final File mediaDir;
    private final File tmpDir;

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

    private MediaStore(Context context) {
        mediaDir = new File(context.getFilesDir(), "media");
        tmpDir = new File(context.getCacheDir(), "media");
    }

    public File getMediaDir() {
        if (!mediaDir.exists()) {
            if (!mediaDir.mkdirs()) {
                Log.e("MediaStore: cannot create " + mediaDir.getAbsolutePath());
            }
        }
        return mediaDir;
    }

    public File getMediaFile(@Nullable String name) {
        return name == null ? null : new File(getMediaDir(), name);
    }

    public File getTmpDir() {
        if (!tmpDir.exists()) {
            if (!tmpDir.mkdirs()) {
                Log.e("MediaStore: cannot create " + tmpDir.getAbsolutePath());
            }
        }
        return mediaDir;
    }

    public File getTmpFile(@NonNull String name) {
        return new File(getTmpDir(), name);
    }

}
