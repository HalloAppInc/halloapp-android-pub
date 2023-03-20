package com.halloapp.content;

import android.content.Context;
import android.text.ParcelableSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.proto.server.MomentInfo;
import com.halloapp.util.RandomId;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Comparator;
import java.util.Locale;

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
    public @ScreenshotState int screenshotted;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SCREENSHOT_NO, SCREENSHOT_YES_PENDING, SCREENSHOT_YES})
    public @interface ScreenshotState {}
    public static final int SCREENSHOT_NO = 0;
    public static final int SCREENSHOT_YES_PENDING = 1;
    public static final int SCREENSHOT_YES = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CONTENT_TYPE_UNKNOWN, CONTENT_TYPE_IMAGE, CONTENT_TYPE_VIDEO, CONTENT_TYPE_TEXT, CONTENT_TYPE_ALBUM_IMAGE})
    public @interface ContentType {}
    public static final int CONTENT_TYPE_UNKNOWN = -1;
    public static final int CONTENT_TYPE_IMAGE = 0;
    public static final int CONTENT_TYPE_VIDEO = 1;
    public static final int CONTENT_TYPE_TEXT = 2;
    public static final int CONTENT_TYPE_ALBUM_IMAGE = 3;

    public static @ContentType int fromProtoContentType(MomentInfo.ContentType contentType) {
        if (contentType == MomentInfo.ContentType.IMAGE) {
            return CONTENT_TYPE_IMAGE;
        } else if (contentType == MomentInfo.ContentType.VIDEO) {
            return CONTENT_TYPE_VIDEO;
        } else if (contentType == MomentInfo.ContentType.TEXT) {
            return CONTENT_TYPE_TEXT;
        } else if (contentType == MomentInfo.ContentType.ALBUM_IMAGE) {
            return CONTENT_TYPE_ALBUM_IMAGE;
        }
        Log.w("Unrecognized content type " + contentType);
        return CONTENT_TYPE_UNKNOWN;
    }

    public static MomentInfo.ContentType getProtoContentType(@ContentType int contentType) {
        if (contentType == CONTENT_TYPE_VIDEO) {
            return MomentInfo.ContentType.VIDEO;
        } else if (contentType == CONTENT_TYPE_TEXT) {
            return MomentInfo.ContentType.TEXT;
        } else if (contentType == CONTENT_TYPE_IMAGE) {
            return MomentInfo.ContentType.IMAGE;
        } else if (contentType == CONTENT_TYPE_ALBUM_IMAGE) {
            return MomentInfo.ContentType.ALBUM_IMAGE;
        }
        return MomentInfo.ContentType.UNRECOGNIZED;
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

    public Spanned formatPostHeaderText(@NonNull Context context, @NonNull String shortName, @NonNull String onTimeSuffix, @NonNull ParcelableSpan nameSpan, @NonNull ParcelableSpan timeAndLocationSpan) {
        final SpannableStringBuilder headerText = new SpannableStringBuilder();
        final SpannableString name = new SpannableString(shortName);
        name.setSpan(nameSpan, 0, shortName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        headerText.append(name);
        headerText.append(" ");
        final CharSequence timeText = TimeFormatter.formatMessageTime(context, timestamp);
        final SpannableString time = new SpannableString(timeText);
        time.setSpan(timeAndLocationSpan, 0, timeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        headerText.append(time);
        final boolean isOnTime = timestamp - notificationTimestamp <= Constants.LATE_POST_THRESHOLD_MS;

        if (location != null) {
            headerText.append(" ");
            final String locText = context.getString(R.string.moment_location, location.toLowerCase(Locale.getDefault()));
            final SpannableString loc = new SpannableString(locText);
            loc.setSpan(timeAndLocationSpan, 0, locText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            headerText.append(loc);
        } else if (isOnTime) {
            headerText.append(onTimeSuffix);
        }
        return headerText;
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
