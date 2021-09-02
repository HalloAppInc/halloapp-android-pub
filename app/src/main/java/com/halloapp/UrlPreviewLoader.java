package com.halloapp;

import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.halloapp.content.Media;
import com.halloapp.media.Downloader;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.ViewDataLoader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.concurrent.Callable;

public class UrlPreviewLoader extends ViewDataLoader<View, UrlPreview, String> {
    @MainThread
    public void load(@NonNull View view, @NonNull String url, @NonNull ViewDataLoader.Displayer<View, UrlPreview> displayer) {
        final Callable<UrlPreview> loader = () -> {
            if (TextUtils.isEmpty(url)) {
                return null;
            }
            Document document = Jsoup.connect(url).get();
            if (document == null) {
                return null;
            }
            UrlPreview preview = UrlPreview.parseFromDocument(url, document);
            String imgUrl = preview.getPreviewImageUrl();
            if (imgUrl != null) {
                final File file = FileStore.getInstance().getTmpFile(RandomId.create());
                Downloader.run(imgUrl, null, null, Media.MEDIA_TYPE_UNKNOWN, null, file, new Downloader.DownloadListener() {
                    @Override
                    public boolean onProgress(long bytesWritten) {
                        return true;
                    }
                }, imgUrl);
                MediaUtils.transcodeImage(file, file, null, Constants.MAX_IMAGE_DIMENSION / 2, Constants.JPEG_QUALITY, true);
                preview.imageMedia = Media.createFromFile(Media.MEDIA_TYPE_IMAGE, file);
            }
            return preview;
        };
        load(view, loader, displayer, url, null);
    }
}
