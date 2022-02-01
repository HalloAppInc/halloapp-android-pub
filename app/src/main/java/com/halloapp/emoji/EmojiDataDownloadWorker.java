package com.halloapp.emoji;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Preferences;
import com.halloapp.props.ServerProps;
import com.halloapp.util.FileUtils;
import com.halloapp.util.StringUtils;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class EmojiDataDownloadWorker extends Worker {

    private static final String EMOJI_DOWNLOAD_WORKER_ID = "emoji-download-worker";
    
    private static final String EMOJI_BASE_URL = "https://halloapp.com/emoji/";

    private static final String EMOJI_FONT_FILE = "emoji_font.ttf";
    private static final String EMOJI_DATA_FILE = "emoji_data.json";

    public static void schedule(@NonNull Context context) {
        Constraints.Builder constraintBuilder = new Constraints.Builder();
        constraintBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
        final OneTimeWorkRequest workRequest = (new OneTimeWorkRequest.Builder(EmojiDataDownloadWorker.class))
                .setConstraints(constraintBuilder.build())
                .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                        TimeUnit.MILLISECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork(EMOJI_DOWNLOAD_WORKER_ID, ExistingWorkPolicy.KEEP, workRequest);
    }

    private final FileStore fileStore;
    private final Preferences preferences;
    private final ServerProps serverProps;
    private final EmojiManager emojiManager;

    public EmojiDataDownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

        fileStore = FileStore.getInstance();
        preferences = Preferences.getInstance();
        serverProps = ServerProps.getInstance();
        emojiManager = EmojiManager.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        int serverEmojiVersion = serverProps.getEmojiVersion();
        int localEmojiVersion = preferences.getLocalEmojiVersion();
        if (serverEmojiVersion <= localEmojiVersion) {
            Log.i("EmojiDataDownloadWorker/doWork current emoji version up to date local=" + localEmojiVersion + "; server=" + serverEmojiVersion);
            return Result.success();
        }
        Log.i("EmojiDataDownloadWorker/doWork new downloading new emoji version local=" + localEmojiVersion + "; server=" + serverEmojiVersion);
        File emojiJson = fileStore.getTmpFile(getTempEmojiDataFile(serverEmojiVersion));
        try {
            emojiJson.delete();
            downloadJson(serverEmojiVersion, emojiJson);
        } catch (IOException e) {
            Log.e("EmojiDataDownloadWorker/doWork failed to download emoji data", e);
            return Result.retry();
        }
        EmojiPickerData emojiPickerData = null;
        try {
            emojiPickerData = EmojiPickerData.parse(emojiJson);
        } catch (IOException e) {
            Log.e("EmojiDataDownloadWorker/doWork failed to parse emoji picker data, aborting");
            return Result.failure();
        }
        File tempFontFile = fileStore.getTmpFile(getTempFontFile(serverEmojiVersion));
        try {
            downloadFont(serverEmojiVersion, emojiPickerData.fontHash, tempFontFile);
        } catch (IOException e) {
            Log.e("EmojiDataDownloadWorker/doWork failed to download emoji font", e);
            return Result.retry();
        }
        if (!emojiManager.updateEmojis(serverEmojiVersion, emojiJson, tempFontFile)) {
            return Result.retry();
        }
        return Result.success();
    }

    private void downloadFont(int serverEmojiVersion, @Nullable String expectedMd5, File destination) throws IOException {
        if (destination.exists()) {
            String md5 = getFileHash(destination);
            if (Objects.equals(expectedMd5, md5)) {
                Log.i("EmojiDataDownloadWorker/downloadFont font already downloaded");
                return;
            }
        }
        String remotePath = EMOJI_BASE_URL + serverEmojiVersion + "/" + EMOJI_FONT_FILE;
        ThreadUtils.setSocketTag();
        InputStream inStream = null;
        HttpURLConnection connection = null;
        try {
            long existingBytes = 0;
            if (destination.exists()) {
                existingBytes = destination.length();
            }
            final URL url = new URL(remotePath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Expires", "0");
            if (existingBytes > 0) {
                connection.setRequestProperty("Range", "bytes=" + existingBytes + "-");
            }
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(30_000);
            connection.setReadTimeout(30_000);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                throw new IOException();
            }
            inStream = connection.getInputStream();

            int contentLength = connection.getContentLength();
            Log.i("Downloader: content length for " + remotePath + ": " + contentLength);
            Log.i("Downloader: full headers for " + remotePath + ": " + connection.getHeaderFields());
            downloadPlaintext(inStream, destination);
            String md5 = getFileHash(destination);
            if (!Objects.equals(expectedMd5, md5)) {
                Log.e("EmojiDataDownloadWorker/downloadFont md5 mismatch expected=" + expectedMd5 + " actual=" + md5);
                destination.delete();
                throw new IOException("Md5 mismatch");
            }

        } finally {
            FileUtils.closeSilently(inStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void downloadJson(int serverEmojiVersion, File destination) throws IOException {
        String remotePath = EMOJI_BASE_URL + serverEmojiVersion + "/" + EMOJI_DATA_FILE;
        ThreadUtils.setSocketTag();
        InputStream inStream = null;
        HttpURLConnection connection = null;
        try {
            long existingBytes = 0;
            if (destination.exists()) {
                existingBytes = destination.length();
            }
            final URL url = new URL(remotePath);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("Expires", "0");
            if (existingBytes > 0) {
                connection.setRequestProperty("Range", "bytes=" + existingBytes + "-");
            }
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(30_000);
            connection.setReadTimeout(30_000);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                throw new IOException();
            }
            inStream = connection.getInputStream();

            int contentLength = connection.getContentLength();
            Log.i("Downloader: content length for " + remotePath + ": " + contentLength);
            Log.i("Downloader: full headers for " + remotePath + ": " + connection.getHeaderFields());

            // We make the assumption that the ETag will be the MD5 hash of the json file.
            // On AWS this is true as long as the file is in 1 chunk, which the min size is
            // 5mb, so we expect this to be a fairly low risk assumption
            String etag = connection.getHeaderField("ETag");
            downloadPlaintext(inStream, destination);

            if (etag != null) {
                String hash = getFileHash(destination);
                if (!etag.replace("\"", "").equals(hash)) {
                    Log.e("EmojiDataDownloadWorker Md5 mismatch: etag=" + etag + "; md5=" + hash);
                    destination.delete();
                    throw new IOException();
                }
            }
        } finally {
            FileUtils.closeSilently(inStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String getFileHash(File file) {
        try {
            byte[] md5 = FileUtils.getFileMd5(file);
            return StringUtils.bytesToHexString(md5);
        } catch (IOException | NoSuchAlgorithmException e) {
            Log.e("EmojiDataDownloadWorker/getFileHash failed to get hash", e);
        }
        return null;
    }

    @WorkerThread
    private static void downloadPlaintext(@NonNull InputStream inStream, @NonNull File unencryptedFile) throws IOException {
        Log.i("EmojiDataDownloadWorker/downloadPlaintext");
        OutputStream outStream = null;
        try {
            outStream = new BufferedOutputStream(new FileOutputStream(unencryptedFile, unencryptedFile.exists()));
            int byteRead;
            final byte[] buffer = new byte[1024];
            while ((byteRead = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, byteRead);
            }
            inStream.close();
            outStream.close();
        } finally {
            FileUtils.closeSilently(inStream);
            FileUtils.closeSilently(outStream);
        }
    }

    private String getTempEmojiDataFile(int version) {
        return "tmp-emoji-data-" + version + ".json";
    }

    private String getTempFontFile(int version) {
        return "tmp-emoji-font-" + version + ".ttf";
    }
}
