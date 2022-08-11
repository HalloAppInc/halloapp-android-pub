package com.halloapp.autodownload;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.work.ListenableWorker;

import com.halloapp.AppContext;
import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.FileStore;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DownloadableAssetManager {

    public static final String PREFS_NAME = "downloadable-store";
    public static final String MANIFEST_FILE_DIRECTORY_PATH = "download";
    public static final String MANIFEST_FILE_NAME = "configuration.json";
    public static final String MANIFEST_PREF_KEY = "/configuration.json";
    public static final String DOWNLOAD_BASE_URL =
            (BuildConfig.DEBUG ? "https://halloapp.dev/download" : "https://halloapp.com/download");
    public static final String REMOTE_MANIFEST_PATH = DOWNLOAD_BASE_URL + "/" + MANIFEST_FILE_NAME;

    private ManifestHelper manifestHelper;
    private static DownloadableAssetManager instance;
    private static int DOWNLOAD_THREAD_POOL_SIZE = 3;

    public static DownloadableAssetManager getInstance() {
        if (instance == null) {
            synchronized (DownloadableAssetManager.class) {
                if (instance == null) {
                    instance = new DownloadableAssetManager(AppContext.getInstance(), FileStore.getInstance());
                }
            }
        }
        return instance;
    }

    private final Map<String, DownloadableAsset> DownloadableAssets = new HashMap<>() ;
    private final Map<String, DownloadableAsset> fileToBeDownload = new HashMap<>();
    private final List<ManifestObserver> observers = new ArrayList<>();
    private final AppContext appContext;
    private final FileStore fileStore;
    private final ExecutorService executor;
    private final CompletionService<Boolean> completionService;
    private SharedPreferences sharedPreferences;

    @WorkerThread
    private synchronized SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = appContext.get().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }
    private DownloadableAssetManager(@NonNull AppContext context, @NonNull FileStore fileStoreInstance) {
        appContext = context;
        fileStore = fileStoreInstance;
        executor = Executors.newFixedThreadPool(DOWNLOAD_THREAD_POOL_SIZE);
        completionService = new ExecutorCompletionService<Boolean>(executor);
    }

    @WorkerThread
    public void init(@NonNull Context context) {
        Log.i("DownloadableAssetManager/init");
        manifestHelper = new ManifestHelper(fileStore);

        synchronized (this) {
            List<DownloadableAsset> localAssets = loadAssetsFromManifest();
            if (localAssets != null && localAssets.size() != 0) {
                for (DownloadableAsset localAssetItem :localAssets) {
                    DownloadableAssets.put(localAssetItem.getKey(), localAssetItem);
                }
            }
            DownloadableManifestSyncWorker.schedule(context);
        }
    }

    @WorkerThread
    public synchronized List<DownloadableAsset> loadAssetsFromManifest() {
        List<DownloadableAsset> localAssets = null;
        try {
            localAssets = manifestHelper.load();
        } catch (IOException exception) {
            Log.e("DownloadableAssetManager/getLocalAssets Local configuration json file for auto downloading doesn't exist");
        }
        return localAssets;
    }

    private void saveManifestETag(String manifestETag)  {
        SharedPreferences sharedPreferences = getSharedPreferences();
        sharedPreferences.edit().putString(MANIFEST_PREF_KEY, manifestETag).commit();
    }

    public String getManifestETag() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        String manifestETag = sharedPreferences.getString(DownloadableAssetManager.MANIFEST_PREF_KEY, "");
        return manifestETag;
    }

    private void lookUpChangedFiles(List<DownloadableAsset> latestAssets) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        for (DownloadableAsset asset :latestAssets) {
            if (!sharedPreferences.contains(asset.getKey())) {
                fileToBeDownload.put(asset.getKey(), asset);
            } else{
                if ( !sharedPreferences.getString(asset.getKey(), null).equals(asset.getChecksum()) ) {
                    fileToBeDownload.put(asset.getKey(), asset);
                }
            }
        }
        Log.i("DownloadAssetManager/lookUpChangedFiles fileToBeDownload size = " + fileToBeDownload.size());
        if (fileToBeDownload.size() > 0) {
            notifyFilesChanged();
        }
    }

    public Future<Boolean> submitFileDownloader(@Nullable String path) {
        DownloadableAsset targetAsset = null;
        if (fileToBeDownload.containsKey(path)) {
            targetAsset = fileToBeDownload.get(path);
            Future<Boolean> fileDownloader = completionService.submit(new FileDownloader(targetAsset, fileStore));
            return fileDownloader;
        } else{
            Log.i("DownloadableAssetManager/submitFileDownloader Latest " + path + " file is already downloaded");
            return null;
        }
    }

    public List<Future<Boolean>> submitFileDownloaders(List<String> paths) {
        List<Future<Boolean>> submittedFileDownloaders = new ArrayList<>();
        for (String path :paths) {
            submittedFileDownloaders.add(submitFileDownloader(path));
        }
        return submittedFileDownloaders;
    }

    public String downloadManifest(String remotePath, String localPath, @Nullable String manifestETag) throws IOException {
        return downloadFile(remotePath, localPath, manifestETag, null);
    }

    public String downloadFile(String remotePath, String localPath, String expectedFileHash) throws IOException {
        return downloadFile(remotePath, localPath, null, expectedFileHash);
    }

    public String downloadFile(String remotePath, String localPath, @Nullable String manifestETag, @Nullable String expectedFileHash) throws IOException {
        File destination = fileStore.getDownloadableAssetsFile(localPath);
        File tmpDestination = fileStore.getDownloadableAssetsTmpFile(localPath);
        ThreadUtils.setSocketTag();
        InputStream inStream = null;
        HttpURLConnection connection = null;
        String hash = null;
        try {
            final URL url = new URL(remotePath);

            connection = (HttpURLConnection) url.openConnection();
            resetHeaderParams(connection);
            if (manifestETag != null) {
                connection.setRequestProperty("If-None-Match", "\"" + manifestETag + "\"");
            }
            long existingBytes = 0;
            if (tmpDestination.exists()) {
                existingBytes = tmpDestination.length();
            }
            if (existingBytes > 0) {
                connection.setRequestProperty("Range", "bytes=" + existingBytes + "-");
            }
            connection.connect();
            if (connection.getResponseCode() == 304) {
                Log.i("DownloadableManifestSyncWorker/doWork Latest manifest file is already downloaded");
                return manifestETag;
            } else if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
                throw new IOException();
            }

            int contentLength = connection.getContentLength();
            Log.i("DownloadableAssetManager/downloadFile content length for " + remotePath + ": " + contentLength);
            Log.i("DownloadableAssetManager/downloadFile full headers for " + remotePath + ": " + connection.getHeaderFields());

            String remoteHash = connection.getHeaderField("ETag");
            remoteHash = remoteHash.substring(1, remoteHash.length() - 1);
            if (manifestETag != null) {
                Log.i("DownloadableManifestSyncWorker/doWork Latest hashing of manifest file " + remoteHash +
                        " mismatches with local hashing " + manifestETag);
            }
            inStream = connection.getInputStream();
            downloadPlaintext(inStream, destination, tmpDestination);
            hash = getFileHash(destination);

            if (hash.equals(expectedFileHash)) {
                return hash;
            }

            if (!hash.equals(remoteHash)) {
                return "";
            }
        } finally {
            FileUtils.closeSilently(inStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
        return hash;
    }

    private void resetHeaderParams(HttpURLConnection connection) {
        connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
        connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
        connection.setRequestProperty("Pragma", "no-cache");
        connection.setRequestProperty("Expires", "0");
        connection.setAllowUserInteraction(false);
        connection.setConnectTimeout(30_000);
        connection.setReadTimeout(30_000);
    }

    @WorkerThread
    private void downloadPlaintext(@NonNull InputStream inStream, @NonNull File unencryptedFile, @NonNull File tmpFile
                                   ) throws IOException {
        Log.i("DownloadableAssetManager/downloadFile try to download file to:" + unencryptedFile.getPath());
        File baseDirectory = unencryptedFile.getParentFile();
        if (!baseDirectory.exists()) {
            boolean result = baseDirectory.mkdirs();
            Log.i("DownloadableAssetManager/downloadPlaintext create base directory for files, Status = " + (result ? "success" : "failure"));
        }

        File tmpDirectory = tmpFile.getParentFile();
        if (!tmpDirectory.exists()) {
            boolean result = tmpDirectory.mkdirs();
            Log.i("DownloadableAssetManager/downloadPlaintext create tmp directory for tmp files, Status = " + (result ? "success" : "failure"));
        }
        OutputStream outStream = null;
        try {
            outStream = new BufferedOutputStream(new FileOutputStream(tmpFile, unencryptedFile.exists()));
            FileUtils.copyFile(inStream, outStream);
            unencryptedFile.delete();
            tmpFile.renameTo(unencryptedFile);
            inStream.close();
            outStream.close();
        } finally {
            FileUtils.closeSilently(inStream);
            FileUtils.closeSilently(outStream);
        }
    }

    private static String getFileHash(File file) {
        try {
            byte[] md5 = FileUtils.getFileMd5(file);
            return StringUtils.bytesToHexString(md5);
        } catch (IOException | NoSuchAlgorithmException e) {
            Log.e("DownloadableTask/getFileHash failed to get hash", e);
        }
        return null;
    }

    public void addObserver(ManifestObserver observer) {
        observers.add(observer);
    }

    public void notifyETagChange(String updatedManifestETag) {
        saveManifestETag(updatedManifestETag);
    }

    public void notifyFilesChanged() {
        for (ManifestObserver observer :observers) {
            observer.onManifestChanged();
        }
    }

    private void saveProp(@NonNull String key, @NonNull String val) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        if (!sharedPreferences.edit().putString(key, val).commit()) {
            Log.e("DownloadableAssetManager/saveProp: failed to save property");
        }
    }

    private void saveProp(@NonNull String key, @NonNull long val) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        if (!sharedPreferences.edit().putLong(key, val).commit()) {
            Log.e("DownloadableAssetManager/saveProp: failed to save property");
        }
    }

    private void clearProp(@NonNull String key) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        if (!sharedPreferences.edit().remove(key).commit()) {
            Log.e("DownloadableAssetManager/clearProp: failed to remove property with key: " + key);
        }
    }

    public static String getManifestFilePath() {
        return MANIFEST_FILE_DIRECTORY_PATH + "/" + MANIFEST_FILE_NAME;
    }

    public ListenableWorker.Result downloadManifest() {
        String manifestETag = getManifestETag();
        Log.i("DownloadableManifestSyncWorker/doWork manifestETag = " + manifestETag);
        String eTag = null;
        try {
            eTag = downloadManifest(REMOTE_MANIFEST_PATH, getManifestFilePath(), manifestETag);
            if (TextUtils.isEmpty(eTag)) {
                return ListenableWorker.Result.retry();
            }
        } catch (IOException e) {
            Log.e("DownloadableManifestSyncWorker/doFile failed", e);
            return ListenableWorker.Result.retry();
        }

        notifyETagChange(eTag);
        List<DownloadableAsset> latestAssets = loadAssetsFromManifest();
        lookUpChangedFiles(latestAssets);
        return ListenableWorker.Result.success();
    }

    private class FileDownloader implements Callable<Boolean> {
        private String localBasePath = "download";
        private String remotePath;
        private String localPath;
        private DownloadableAsset asset;

        private final FileStore fileStore;

        private static final boolean DOWNLOAD_SUCCESS = true;
        private static final boolean DOWNLOAD_FAILURE = false;

        public FileDownloader(DownloadableAsset downloadableAsset, FileStore fileStoreInstance) {
            remotePath = getRemotePath(downloadableAsset);
            localPath = getLocalPath(downloadableAsset);
            asset = downloadableAsset;
            fileStore = fileStoreInstance;
        }

        @Override
        public Boolean call() throws Exception {
            String downloadedFileHash = downloadFile(remotePath, localPath, asset.getChecksum());
            if (!TextUtils.isEmpty(downloadedFileHash)) {
                fileToBeDownload.remove(asset.getKey());
                saveProp(asset.getKey(), downloadedFileHash);
                return DOWNLOAD_SUCCESS;
            }
            return  DOWNLOAD_FAILURE;
        }

        private String getRemotePath(DownloadableAsset downloadableAsset) {
            return DOWNLOAD_BASE_URL + downloadableAsset.getKey();
        }

        private String getLocalPath(DownloadableAsset downloadableAsset) {
            return localBasePath + downloadableAsset.getKey();
        }

    }
}
