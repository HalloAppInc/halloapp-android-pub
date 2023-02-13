package com.halloapp.katchup.media;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.FileStore;
import com.halloapp.content.Media;
import com.halloapp.media.Downloader;
import com.halloapp.util.RandomId;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContentPlayerView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class ExternalSelfieLoader extends ViewDataLoader<ContentPlayerView, Media, String> {

    protected final LruCache<String, Media> cache;

    public ExternalSelfieLoader() {
        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("MediaThumbnailLoader: create " + cacheSize + "KB cache for post images");
        cache = new LruCache<String, Media>(cacheSize) {

            @Override
            protected int sizeOf(@NonNull String key, @NonNull Media value) {
                // The cache size will be measured in kilobytes rather than number of items
                return 1;
            }
        };
    }

    @MainThread
    public void load(@NonNull ContentPlayerView view, @NonNull Media media, @NonNull ViewDataLoader.Displayer<ContentPlayerView, Media> displayer) {
        String id;
        try {
            id = URLEncoder.encode(media.url, StandardCharsets.US_ASCII.displayName());
        } catch (UnsupportedEncodingException e) {
            Log.e("Failed to encode url", e);
            id = RandomId.create();
        }
        String mediaLogId = "external-" + id;
        media.file = FileStore.getInstance().getTmpFile(id);
        Downloader.DownloadListener downloadListener = new Downloader.DownloadListener() {
            @Override
            public boolean onProgress(long bytes) {
                return true;
            }
        };
        final Callable<Media> loader = () -> {
            if (media.url != null && !media.file.exists()) {
                final File encFile = media.encFile != null ? media.encFile : FileStore.getInstance().getTmpFile(RandomId.create() + ".enc");
                media.encFile = encFile;
                Downloader.run(media.url, media.encKey, media.encSha256hash, media.type, media.blobVersion, media.chunkSize, media.blobSize, encFile, media.file, downloadListener, mediaLogId);
                if (!encFile.delete()) {
                    Log.w("MediaThumbnailLoader: failed to delete temp enc file for " + mediaLogId);
                }
            }
            return media;
        };
        load(view, loader, displayer, id, cache);
    }
}
