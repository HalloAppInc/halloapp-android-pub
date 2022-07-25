package com.halloapp.content;

import androidx.annotation.IntDef;

import com.halloapp.Constants;
import com.halloapp.id.UserId;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MomentPost extends Post {

    public UserId unlockedUserId;
    public @ScreenshotState int screenshotted;

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

    @Override
    public boolean isAllMediaTransferred() {
        if (media.isEmpty()) {
            return true;
        }
        Media mediaItem = media.get(0);
        if (mediaItem.transferred != Media.TRANSFERRED_YES && mediaItem.transferred != Media.TRANSFERRED_PARTIAL_CHUNKED) {
            return false;
        }
        return true;
    }

    @Override
    public boolean shouldSend() {
        return isOutgoing() && transferred == TRANSFERRED_NO;
    }
}
