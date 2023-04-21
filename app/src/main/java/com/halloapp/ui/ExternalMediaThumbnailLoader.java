package com.halloapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.FileStore;
import com.halloapp.content.Media;
import com.halloapp.media.Downloader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.props.ServerProps;
import com.halloapp.util.RandomId;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class ExternalMediaThumbnailLoader extends MediaThumbnailLoader {

    public ExternalMediaThumbnailLoader(@NonNull Context context, int dimensionLimit) {
        super(context, dimensionLimit);
    }

    @MainThread
    public void load(@NonNull ImageView view, @NonNull Media media, @NonNull ViewDataLoader.Displayer<ImageView, Bitmap> displayer, @Nullable Runnable completion) {
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
        if (media.file.equals(view.getTag()) && view.getDrawable() != null) {
            if (completion != null) {
                completion.run();
            }
            return; // bitmap can be out of cache, but still attached to image view; since media images are stable we can assume the whatever is loaded for current tag would'n change
        }
        final Callable<Bitmap> loader = () -> {
            Bitmap bitmap = null;
            if (media.url != null && !media.file.exists()) {
                boolean isStreamingVideo = media.blobVersion == Media.BLOB_VERSION_CHUNKED && media.type == Media.MEDIA_TYPE_VIDEO && media.blobSize > ServerProps.getInstance().getStreamingInitialDownloadSize();
                if (isStreamingVideo) {
                    Downloader.runForInitialChunks(media.rowId, media.url, media.encKey, media.chunkSize, media.blobSize, media.file, downloadListener);
                } else {
                    final File encFile = media.encFile != null ? media.encFile : FileStore.getInstance().getTmpFile(RandomId.create() + ".enc");
                    media.encFile = encFile;
                    Downloader.run(media.url, media.encKey, media.encSha256hash, media.type, media.blobVersion, media.chunkSize, media.blobSize, encFile, media.file, downloadListener, mediaLogId);
                    if (!encFile.delete()) {
                        Log.w("MediaThumbnailLoader: failed to delete temp enc file for " + mediaLogId);
                    }
                }
            }
            if (media.file.exists()) {
                bitmap = MediaUtils.decode(media.file, media.type, dimensionLimit);
            } else {
                Log.i("MediaThumbnailLoader:load file " + media.file.getAbsolutePath() + " doesn't exist");
            }
            if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
                Log.i("MediaThumbnailLoader:load cannot decode " + media.file);
                return INVALID_BITMAP;
            } else {
                return bitmap;
            }
        };
        load(view, loader, displayer, media.file, cache);
    }
}
