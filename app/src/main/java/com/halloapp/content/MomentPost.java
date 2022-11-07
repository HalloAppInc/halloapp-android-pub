package com.halloapp.content;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.RandomId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Comparator;

public class MomentPost extends Post {

    public UserId unlockedUserId;
    public @ScreenshotState int screenshotted;
    public int selfieMediaIndex;
    public String location;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SCREENSHOT_NO, SCREENSHOT_YES_PENDING, SCREENSHOT_YES})
    public @interface ScreenshotState {}
    public static final int SCREENSHOT_NO = 0;
    public static final int SCREENSHOT_YES_PENDING = 1;
    public static final int SCREENSHOT_YES = 2;

    public MomentPost(long rowId, UserId senderUserId, String postId, long timestamp, int transferred, int seen, int type, String text) {
        super(rowId, senderUserId, postId, timestamp, transferred, seen, type, text);
        expirationTime = timestamp + Constants.MOMENT_EXPIRATION;
    }

    public MomentPost(long rowId, UserId senderUserId, String postId, long timestamp, int transferred, int seen, String text) {
        this(rowId, senderUserId, postId, timestamp, transferred, seen, TYPE_MOMENT, text);
    }

    @Nullable
    public Media getSelfie() {
        if (0 <= selfieMediaIndex && selfieMediaIndex < media.size()) {
            return media.get(selfieMediaIndex);
        }

        return null;
    }

    @Override
    public boolean shouldSend() {
        return isOutgoing() && transferred == TRANSFERRED_NO;
    }

    @WorkerThread
    @NonNull
    public File createThumb(int maxDimension) throws IOException {
        File targetFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));

        if (canMergeMedia()) {
            mergeImages(media.get(0).file, media.get(1).file, targetFile, maxDimension);
        } else {
            MediaUtils.createThumb(media.get(0).file, targetFile, Media.MEDIA_TYPE_IMAGE, maxDimension);
        }

        return targetFile;
    }

    public boolean canMergeMedia() {
        return media.size() > 1 &&
               media.get(0).type == Media.MEDIA_TYPE_IMAGE &&
               media.get(1).type == Media.MEDIA_TYPE_IMAGE;
    }

    @WorkerThread
    private void mergeImages(@NonNull File firstFile, @NonNull File secondFile, @NonNull File targetFile, int maxDimension) throws IOException {
        Bitmap bitmap = MediaUtils.decodeImage(firstFile, maxDimension);
        if (bitmap == null) {
            throw new IOException("cannot decode the first image");
        }

        int width = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() * 2 : bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap result = Bitmap.createBitmap(width, height, MediaUtils.getBitmapConfig(bitmap.getConfig()));
        Canvas canvas = new Canvas(result);

        // draw the image on the left
        int left = Math.max(bitmap.getWidth() / 2 - result.getWidth() / 4, 0);
        int right = Math.min(bitmap.getWidth() / 2 + result.getWidth() / 4, bitmap.getWidth());
        Rect source = new Rect(left, 0, right, bitmap.getHeight());
        Rect target = new Rect(0, 0, result.getWidth() / 2, result.getHeight());
        canvas.drawBitmap(bitmap, source, target, null);
        bitmap.recycle();

        bitmap = MediaUtils.decodeImage(secondFile, maxDimension);
        if (bitmap == null) {
            throw new IOException("cannot decode the second image");
        }

        // draw the image on the right
        left = Math.max(bitmap.getWidth() / 2 - result.getWidth() / 4, 0);
        right = Math.min(bitmap.getWidth() / 2 + result.getWidth() / 4, bitmap.getWidth());
        source = new Rect(left, 0, right, bitmap.getHeight());
        target = new Rect(result.getWidth() / 2, 0, result.getWidth(), result.getHeight());
        canvas.drawBitmap(bitmap, source, target, null);
        bitmap.recycle();

        try (final FileOutputStream output = new FileOutputStream(targetFile)) {
            result.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, output);
        }
    }

    // order moments by own moment, unseen moments, seen moments
    public static Comparator<MomentPost> comparator = (momentFirst, momentSecond) -> {
        if (momentFirst.isOutgoing() && momentSecond.isOutgoing()) {
            return Long.compare(momentFirst.timestamp, momentSecond.timestamp);
        }

        if (momentFirst.isOutgoing()) {
            return -1;
        }

        if (momentSecond.isOutgoing()) {
            return 1;
        }

        if (!momentFirst.isSeen() && !momentSecond.isSeen()) {
            return Long.compare(momentFirst.timestamp, momentSecond.timestamp);
        }

        if (!momentFirst.isSeen()) {
            return -1;
        }

        if (!momentSecond.isSeen()) {
            return 1;
        }

        return Long.compare(momentFirst.timestamp, momentSecond.timestamp);
    };
}
