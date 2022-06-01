package com.halloapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.collection.LruCache;

import com.halloapp.content.Media;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.ViewDataLoader;

import java.io.File;
import java.util.concurrent.Callable;

public class DocumentPreviewLoader extends ViewDataLoader<ImageView, DocumentPreviewLoader.DocumentPreview, File> {

    protected final LruCache<File, DocumentPreviewLoader.DocumentPreview> cache;

    private int dimensionLimit;

    public static class DocumentPreview {
        public long fileSize;
        public int numPages;
        public Bitmap thumbnail;
    }

    public DocumentPreviewLoader(int dimensionalLimit) {
        this.dimensionLimit = dimensionalLimit;
        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        cache = new LruCache<File, DocumentPreviewLoader.DocumentPreview>(cacheSize) {

            @Override
            protected int sizeOf(@NonNull File key, @NonNull DocumentPreviewLoader.DocumentPreview preview) {
                // The cache size will be measured in kilobytes rather than number of items
                if (preview.thumbnail == null) {
                    return 0;
                }
                return preview.thumbnail.getByteCount() / 1024;
            }
        };
    }

    @MainThread
    public void loadNoThumbnail(@NonNull ImageView view, @NonNull File file, @NonNull ViewDataLoader.Displayer<ImageView, DocumentPreviewLoader.DocumentPreview> displayer) {
        Callable<DocumentPreviewLoader.DocumentPreview> loader = () -> {
            DocumentPreviewLoader.DocumentPreview preview = new DocumentPreview();
            if (file.exists()) {
                preview.fileSize = file.length();
            }
            return preview;
        };
        load(view, loader, displayer, file, cache);
    }

    @MainThread
    public void loadPdf(@NonNull ImageView view, @NonNull File file, @NonNull ViewDataLoader.Displayer<ImageView, DocumentPreviewLoader.DocumentPreview> displayer) {
        Callable<DocumentPreviewLoader.DocumentPreview> loader = () -> {
            DocumentPreviewLoader.DocumentPreview preview = new DocumentPreview();
            if (file.exists()) {
                PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
                final int pageCount = renderer.getPageCount();
                if(pageCount > 0){
                    PdfRenderer.Page page = renderer.openPage(0);
                    int width = page.getWidth();
                    int height = page.getHeight();
                    while (1.1f * width > 2 * dimensionLimit || 1.1f * height > 2 * dimensionLimit) {
                        width /= 2;
                        height /= 2;
                    }
                    preview.thumbnail = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(preview.thumbnail);
                    canvas.drawColor(Color.WHITE);
                    preview.numPages = pageCount;
                    page.render(preview.thumbnail, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    page.close();
                    renderer.close();
                }
                preview.fileSize = file.length();
            }
            return preview;
        };
        load(view, loader, displayer, file, cache);
    }

    @MainThread
    public void loadImage(@NonNull ImageView view, @NonNull File file, @NonNull ViewDataLoader.Displayer<ImageView, DocumentPreviewLoader.DocumentPreview> displayer) {
        if (file.equals(view.getTag()) && view.getDrawable() != null) {
            return; // bitmap can be out of cache, but still attached to image view; since media images are stable we can assume the whatever is loaded for current tag would'n change
        }
        Callable<DocumentPreviewLoader.DocumentPreview> loader = () -> {
            DocumentPreviewLoader.DocumentPreview preview = new DocumentPreview();
            if (file.exists()) {
                preview.fileSize = file.length();
                preview.thumbnail = MediaUtils.decode(file, Media.MEDIA_TYPE_IMAGE, dimensionLimit);
            }
            return preview;
        };
        load(view, loader, displayer, file, cache);
    }
}
