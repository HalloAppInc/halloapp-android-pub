package com.halloapp.content;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.halloapp.Constants;
import com.halloapp.id.UserId;

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
