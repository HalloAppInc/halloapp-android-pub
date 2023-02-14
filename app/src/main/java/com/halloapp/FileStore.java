package com.halloapp;

import android.content.Context;
import android.net.Uri;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileStore {

    private static final int MAX_LOG_FILES = 7;
    private static final int MAX_FILE_NAME_LENGTH = 255;

    private static final String CRITICAL_LOG_FILE = "critical.perm";

    private static FileStore instance;

    private final File mediaDir;
    private final File tmpDir;
    private final File cameraDir;
    private final File shareDir;
    private final File avatarDir;
    private final File logDir;
    private final File exportDir;
    private final File emojiDir;
    private final File downloadableAssetDir;
    private final File downloadableAssetTmpDir;

    public static FileStore getInstance() {
        if (instance == null) {
            synchronized(FileStore.class) {
                if (instance == null) {
                    instance = new FileStore(AppContext.getInstance());
                }
            }
        }
        return instance;
    }

    public static String fileNameFromUri(@NonNull Uri uri, @Nullable String suffix) {
        int maxBaseLen = MAX_FILE_NAME_LENGTH - (suffix == null ? 0 : suffix.length()) - 1;
        String baseName = Base64.encodeToString(uri.toString().getBytes(), Base64.URL_SAFE);
        baseName = baseName.length() > maxBaseLen ? baseName.substring(0, maxBaseLen) : baseName;
        return TextUtils.isEmpty(suffix) ? baseName : String.format("%s-%s", baseName, suffix);
    }

    private FileStore(@NonNull AppContext appContext) {
        Context context = appContext.get();
        final StrictMode.ThreadPolicy threadPolicy = StrictMode.getThreadPolicy();
        StrictMode.allowThreadDiskReads();
        StrictMode.allowThreadDiskWrites();
        try {
            mediaDir = prepareDir(new File(context.getFilesDir(), "media"));
            tmpDir = prepareDir(new File(context.getCacheDir(), "media"));
            cameraDir = prepareDir(new File(context.getCacheDir(), "camera"));
            shareDir = prepareDir(new File(context.getCacheDir(), "share"));
            avatarDir = prepareDir(new File(context.getFilesDir(), "avatars"));
            logDir = prepareDir(new File(context.getFilesDir(), "logs"));
            exportDir = prepareDir(new File(context.getCacheDir(), "export"));
            emojiDir = prepareDir(new File(context.getFilesDir(), "emoji"));
            downloadableAssetDir = prepareDir(new File(context.getFilesDir(), "download_framework"));
            downloadableAssetTmpDir = prepareDir(new File(context.getCacheDir(), "download_tmp"));
        } finally {
            StrictMode.setThreadPolicy(threadPolicy);
        }
    }

    public void ensureCacheDirs() {
        prepareDir(tmpDir);
        prepareDir(cameraDir);
        prepareDir(exportDir);
        prepareDir(shareDir);
    }

    private File prepareDir(@NonNull File dir) {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e("FileStore: cannot create " + dir.getAbsolutePath());
            }
        }
        return dir;
    }

    public File getShareFile(@NonNull String contentId) {
        return new File(shareDir, contentId);
    }

    public File getMediaDir() {
        return mediaDir;
    }

    public File getMediaFile(@Nullable String name) {
        return name == null ? null : new File(getMediaDir(), name);
    }

    public File getEmojiDir() {
        return emojiDir;
    }

    public File getDownloadableAssetDir() { return downloadableAssetDir; }

    public File getDownloadableAssetTmpDir() { return downloadableAssetTmpDir; }

    public void purgeOldLogFiles() {
        File[] fileArr = logDir.listFiles();
        List<File> logFiles = new ArrayList<>();
        if (fileArr != null) {
            for (File f : fileArr) {
                if (f.isFile() && f.getName().endsWith(".log")) {
                    logFiles.add(f);
                }
            }
        }
        Collections.sort(logFiles);
        if (logFiles.size() > MAX_LOG_FILES) {
            while (logFiles.size() > MAX_LOG_FILES) {
                File f = logFiles.remove(0);
                if (!f.delete()) {
                    Log.e("Failed to delete log file");
                }
            }
        }
    }

    // For debugging
    public void purgeAllLogFiles() {
        File[] fileArr = logDir.listFiles();
        if (fileArr != null) {
            for (File f : fileArr) {
                if (f.isFile() && f.getName().endsWith(".log")) {
                    if (!f.delete()) {
                        Log.e("Failed to delete log file");
                    }
                }
            }
        }
    }

    public File getCameraDir() { return cameraDir; }

    public File getCameraFile(@Nullable String name) {
        return name == null ? null : new File(getCameraDir(), name);
    }

    public File getTmpDir() {
        return tmpDir;
    }

    public File getTmpFile(@Nullable String name) {
        if (name == null) {
            return null;
        }
        return new File(getTmpDir(), name);
    }

    public File getDownloadableAssetsFile(@Nullable String name) {
        if (name == null) {
            return null;
        }
        return new File(getDownloadableAssetDir(), name);
    }

    public File getDownloadableAssetsTmpFile(@Nullable String name) {
        if (name == null) {
            return null;
        }
        return new File(getDownloadableAssetTmpDir(), name + ".tmp");
    }

    public File getTmpFileForUri(@NonNull Uri uri, @Nullable String suffix) {
        return getTmpFile(fileNameFromUri(uri, suffix));
    }

    public File getImageCaptureFile() {
        return new File(cameraDir, "capture.jpg");
    }

    public File getAvatarFile(String jid) {
        return getAvatarFile(jid, false);
    }

    public File getAvatarFile(String jid, boolean large) {
        return new File(avatarDir, jid + (large ? "-large" : "") + ".jpg");
    }

    public File getLogDir() {
        return logDir;
    }

    public File getLogFile(String timestamp) {
        return new File(logDir, "halloapp-" + timestamp + ".log");
    }

    public File getCriticalLogFile() {
        return new File(logDir, CRITICAL_LOG_FILE);
    }

    public File getExportDataFile() {
        return new File(exportDir, "export-data.json");
    }

    public File getTempRecordingLocation() {
        return getTmpFile(RandomId.create() + ".aac");
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
        long expirationTimeMs = BuildConfig.IS_KATCHUP ? Constants.KATCHUP_DEFAULT_EXPIRATION : Constants.POSTS_EXPIRATION;
        for (File file : files) {
            if (file.lastModified() < System.currentTimeMillis() - expirationTimeMs) {
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
