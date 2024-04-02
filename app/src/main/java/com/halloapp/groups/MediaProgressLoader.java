package com.halloapp.groups;

import androidx.annotation.NonNull;

import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.util.HashSet;
import java.util.Set;

public class MediaProgressLoader {

    public static abstract class MediaProgressCallback {
        public abstract String getContentItemId();
        public abstract void onPostProgress(int percent);
    }

    private final ContentDb.Observer observer = new ContentDb.DefaultObserver() {
        @Override
        public void onMediaPercentTransferred(@NonNull ContentItem contentItem, @NonNull Media media, int percent) {
            synchronized (callbacks) {
                for (MediaProgressCallback callback : callbacks) {
                    if (contentItem.id.equals(callback.getContentItemId())) {
                        processCallback(contentItem, callback);
                    }
                }
            }
        }
    };

    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final Set<MediaProgressCallback> callbacks = new HashSet<>();

    public MediaProgressLoader() {
        this.bgWorkers = BgWorkers.getInstance();
        this.contentDb = ContentDb.getInstance();
        contentDb.addObserver(observer);
    }

    private void processCallback(@NonNull ContentItem contentItem, @NonNull MediaProgressCallback callback) {
        // TODO: Support incoming content as well
        if (!contentItem.isOutgoing()) {
            return;
        }
        long totalSize = 0;
        long totalSent = 0;
        for (Media m : contentItem.media) {
            if (m.file == null) {
                callback.onPostProgress(0);
                return;
            }
            long fileSize = m.file.length();
            totalSize += fileSize;
            int mPercent = contentDb.getMediaPercentTransferred(m.rowId);
            totalSent += fileSize * mPercent / 100;
        }

        if (totalSize > 0) {
            long resultingPercent = totalSent * 100 / totalSize;
            callback.onPostProgress((int) resultingPercent);
        } else {
            callback.onPostProgress(0);
        }
    }

    public void registerCallback(MediaProgressCallback callback) {
        synchronized (callbacks) {
            callbacks.add(callback);
        }
        bgWorkers.execute(() -> {
            Post post = contentDb.getPost(callback.getContentItemId());
            if (post != null) {
                processCallback(post, callback);
            }
        });
    }

    public void removeCallback(MediaProgressCallback callback) {
        synchronized (callbacks) {
            callbacks.remove(callback);
        }
    }

    public void destroy() {
        contentDb.removeObserver(observer);
        synchronized (callbacks) {
            callbacks.clear();
        }
    }
}
