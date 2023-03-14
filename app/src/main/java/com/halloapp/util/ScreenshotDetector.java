package com.halloapp.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.HashSet;
import java.util.Locale;

public class ScreenshotDetector extends ContentObserver {

    private final Context context;

    private ScreenshotListener listener;

    public interface ScreenshotListener {
        void onScreenshot();
    }

    private final HashSet<String> notifiedUris = new HashSet<>();

    public ScreenshotDetector(@NonNull Context context, @NonNull Handler handler) {
        super(handler);

        this.context = context;
    }

    public void setListener(@Nullable ScreenshotListener listener) {
        this.listener = listener;
    }

    @Override
    public void onChange(boolean selfChange, @Nullable Uri uri) {
        super.onChange(selfChange, uri);
        if (uri != null) {
            queryScreenshots(uri);
        }
    }

    private void queryScreenshots(@NonNull Uri uri) {
        if (Build.VERSION.SDK_INT >= 29) {
            queryRelativeDataColumn(uri);
        } else {
            queryDataColumn(uri);
        }
    }

    private void queryDataColumn(@NonNull Uri uri) {
        boolean screenshot = false;
        ContentResolver contentResolver = context.getContentResolver();
        try (Cursor cursor = contentResolver.query(
                uri,
                new String[]{MediaStore.Images.Media.DATA},
                null,
                null,
                null)) {
            int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            while (cursor.moveToNext()) {
                String path = cursor.getString(dataColumn).toLowerCase(Locale.ROOT);
                if (path.contains("screenshot")) {
                    if (!notifiedUris.contains(path)) {
                        screenshot = true;
                        notifiedUris.add(path);
                        break;
                    }
                }
            }
        }
        if (screenshot) {
            onScreenshot();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void queryRelativeDataColumn(@NonNull Uri uri) {
        boolean screenshot = false;
        // ContentResolver.query needs READ_EXTERNAL_STORAGE to actually read data.
        try (Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.RELATIVE_PATH},
                null,
                null,
                null)) {
            int relativePathColumn =
                    cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH);
            int displayNameColumn =
                    cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            while (cursor.moveToNext()) {
                String name = cursor.getString(displayNameColumn).toLowerCase(Locale.ROOT);
                String relativePath = cursor.getString(relativePathColumn).toLowerCase(Locale.ROOT);
                if (name.contains("screenshot") || relativePath.contains("screenshot")){
                    if (!notifiedUris.contains(name)) {
                        screenshot = true;
                        notifiedUris.add(name);
                        break;
                    }
                }

            }
        }
        if (screenshot) {
            onScreenshot();
        }
    }

    public void start() {
        context.getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, this);
    }

    public void stop() {
        context.getContentResolver().unregisterContentObserver(this);
    }

    private void onScreenshot() {
        if (listener != null) {
            listener.onScreenshot();
        }
    }
}
