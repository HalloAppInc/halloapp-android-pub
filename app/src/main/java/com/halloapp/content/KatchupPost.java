package com.halloapp.content;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import com.halloapp.Constants;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.proto.server.MomentInfo;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Comparator;

public class KatchupPost extends Post {

    public String location;
    public float selfieX;
    public float selfieY;
    public long notificationTimestamp;
    public long notificationId;
    public int numTakes;
    public int numSelfieTakes;
    public long timeTaken;
    public String serverScore;
    public MomentInfo.ContentType contentType;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CONTENT_TYPE_UNKNOWN, CONTENT_TYPE_IMAGE, CONTENT_TYPE_VIDEO, CONTENT_TYPE_TEXT})
    public @interface ContentType {}
    public static final int CONTENT_TYPE_UNKNOWN = -1;
    public static final int CONTENT_TYPE_IMAGE = 0;
    public static final int CONTENT_TYPE_VIDEO = 1;
    public static final int CONTENT_TYPE_TEXT = 2;

    public static @ContentType int fromProtoContentType(MomentInfo.ContentType contentType) {
        if (contentType == MomentInfo.ContentType.IMAGE) {
            return CONTENT_TYPE_IMAGE;
        } else if (contentType == MomentInfo.ContentType.VIDEO) {
            return CONTENT_TYPE_VIDEO;
        } else if (contentType == MomentInfo.ContentType.TEXT) {
            return CONTENT_TYPE_TEXT;
        }
        Log.w("Unrecognized content type " + contentType);
        return CONTENT_TYPE_UNKNOWN;
    }
    public static MomentInfo.ContentType getProtoContentType(@ContentType int contentType) {
        if (contentType == CONTENT_TYPE_VIDEO) {
            return MomentInfo.ContentType.VIDEO;
        } else if (contentType == CONTENT_TYPE_TEXT) {
            return MomentInfo.ContentType.TEXT;
        }
        return MomentInfo.ContentType.IMAGE;
    }

    public KatchupPost(long rowId, UserId senderUserId, String postId, long timestamp, int transferred, int seen, int type, String text) {
        super(rowId, senderUserId, postId, timestamp, transferred, seen, type, text);
        expirationTime = timestamp + Constants.KATCHUP_DEFAULT_EXPIRATION;
    }

    public KatchupPost(long rowId, UserId senderUserId, String postId, long timestamp, int transferred, int seen, String text) {
        this(rowId, senderUserId, postId, timestamp, transferred, seen, TYPE_KATCHUP, text);
    }

    @Nullable
    public Media getSelfie() {
        if (!media.isEmpty()) {
            return media.get(0);
        }

        return null;
    }

    @Nullable
    public Media getContent() {
        if (media.size() > 1) {
            return media.get(1);
        }
        return null;
    }

    @Override
    public boolean shouldSend() {
        return isOutgoing() && transferred == TRANSFERRED_NO;
    }

    // order moments by own moment, unseen moments, seen moments
    public static Comparator<KatchupPost> comparator = (momentFirst, momentSecond) -> {
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
