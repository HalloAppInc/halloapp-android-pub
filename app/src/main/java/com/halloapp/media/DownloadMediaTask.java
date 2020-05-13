package com.halloapp.media;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.halloapp.FileStore;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;

import java.io.File;
import java.io.IOException;

public class DownloadMediaTask extends AsyncTask<Void, Void, Boolean> {

    private final ContentItem contentItem;

    private final FileStore fileStore;
    private final ContentDb contentDb;

    public DownloadMediaTask(@NonNull ContentItem contentItem, @NonNull FileStore fileStore, @NonNull ContentDb contentDb) {
        this.contentItem = contentItem;
        this.fileStore = fileStore;
        this.contentDb = contentDb;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Log.i("DownloadMediaTask " + contentItem);
        for (Media media : contentItem.media) {
            if (media.transferred) {
                continue;
            }
            final Downloader.DownloadListener downloadListener = percent -> true;
            try {
                final File file = fileStore.getMediaFile(RandomId.create() + "." + Media.getFileExt(media.type));
                Downloader.run(media.url, media.encKey, media.sha256hash, media.type, file, downloadListener);
                if (!file.setLastModified(contentItem.timestamp)) {
                    Log.w("DownloadMediaTask: failed to set last modified to " + file.getAbsolutePath());
                }
                media.file = file;
                media.transferred = true;
                contentItem.setMediaTransferred(media, contentDb);
            } catch (IOException e) {
                Log.e("DownloadMediaTask: " + media.url, e);
                return null;
            }
        }
        return null;
    }
}
