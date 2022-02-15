package com.halloapp;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.media.Downloader;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.RandomId;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class UrlPreviewLoader extends ViewDataLoader<View, UrlPreview, String> {

    private final List<ContentItem> waitingContentItems = new ArrayList<>();

    @MainThread
    public void load(@NonNull View view, @NonNull String url, @NonNull ViewDataLoader.Displayer<View, UrlPreview> displayer) {
        final Callable<UrlPreview> loader = () -> {
            if (TextUtils.isEmpty(url)) {
                return null;
            }
            String userAgent = Constants.URL_PREVIEW_USER_AGENT;
            Document document = Jsoup.connect(url).userAgent(userAgent).get();
            if (document == null) {
                return null;
            }
            UrlPreview preview = UrlPreview.parseFromDocument(url, document);
            String imgUrl = preview.getPreviewImageUrl();
            if (imgUrl != null) {
                final File file = FileStore.getInstance().getTmpFile(RandomId.create());
                try {
                    Downloader.runExternal(imgUrl, file, new Downloader.DownloadListener() {
                        @Override
                        public boolean onProgress(long bytesWritten) {
                            return true;
                        }
                    }, imgUrl);
                    MediaUtils.transcodeImage(file, file, null, Constants.MAX_IMAGE_DIMENSION / 2, Constants.JPEG_QUALITY, true);
                    preview.imageMedia = Media.createFromFile(Media.MEDIA_TYPE_IMAGE, file);
                } catch (Exception e) {
                    Log.e("UrlPreviewLoader/load error fetching preview image", e);
                }
            }
            return preview;
        };
        load(view, loader, displayer, url, null);
    }

    @Override
    protected void executeShowResult(@NonNull View view, @NonNull Displayer<View, List<UrlPreview>> displayer, @NonNull List<String> keys, @Nullable List<UrlPreview> result) {
        super.executeShowResult(view, displayer, keys, result);
        mainHandler.post(() -> {
            ListIterator<ContentItem> iterator = waitingContentItems.listIterator();
            while (iterator.hasNext()) {
                final ContentItem item = iterator.next();
                String url = item.getLoadingUrlPreview();
                if (url == null) {
                    iterator.remove();
                    continue;
                }
                int keyIndex = keys.indexOf(url);
                if (keyIndex == -1) {
                    continue;
                }
                final UrlPreview urlPreview;
                if (result != null && result.size() > keyIndex) {
                    urlPreview = result.get(keyIndex);
                } else {
                    urlPreview = null;
                }
                if (urlPreview != null && urlPreview.imageMedia != null) {
                    BgWorkers.getInstance().execute(() -> {
                        final File imagePreview = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));
                        try {
                            MediaUtils.transcodeImage(urlPreview.imageMedia.file, imagePreview, null, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY, false);
                            urlPreview.imageMedia.file = imagePreview;
                        } catch (IOException e) {
                            Log.e("failed to transcode url preview image", e);
                            urlPreview.imageMedia = null;
                        }
                        item.updateUrlPreview(urlPreview);
                    });
                } else {
                    item.updateUrlPreview(urlPreview);
                }
            }
        });
    }

    @MainThread
    public void cancel(@NonNull View view, boolean forceCompletion) {
        if (forceCompletion) {
            queue.remove(view);
            view.setTag(null);
        } else {
            super.cancel(view);
        }
    }

    @MainThread
    public void destroy() {
        executor.shutdown();
    }

    @MainThread
    public void addWaitingContentItem(@NonNull ContentItem contentItem) {
        waitingContentItems.add(contentItem);
    }
}
