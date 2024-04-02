package com.halloapp.content;

import android.graphics.Color;

import androidx.annotation.ColorInt;

import com.halloapp.id.UserId;
import com.halloapp.util.StringUtils;

public class KatchupStickerComment extends Comment {

    private String stickerText;
    private @ColorInt int color;

    public KatchupStickerComment(long rowId, String postId, UserId senderUserId, String commentId, String parentCommentId, long timestamp, int transferred, boolean seen, String text, @ColorInt int color) {
        super(rowId, postId, senderUserId, commentId, parentCommentId, timestamp, transferred, seen, combineTextAndColor(text, color));

        this.stickerText = text;
        this.color = color;
        this.type = TYPE_STICKER;
    }

    public KatchupStickerComment(long rowId, String postId, UserId senderUserId, String commentId, String parentCommentId, long timestamp, int transferred, boolean seen, String text) {
        super(rowId, postId, senderUserId, commentId, parentCommentId, timestamp, transferred, seen, text);
        this.type = TYPE_STICKER;
        if (text != null && text.length() > 7) {
            this.color = Color.parseColor(text.substring(0, 7));
            this.stickerText = text.substring(7);
        } else {
            this.color = 0x9BDA91; // TODO: Deal with this class in main depending on Colors.java from katchup (Colors.getDefaultStickerColor())
            this.stickerText = text;
        }
    }

    public String getStickerText() {
        return stickerText;
    }

    public String getColorString() {
        return StringUtils.convertColorToHexString(color);
    }

    private static String combineTextAndColor(String text, @ColorInt int color) {
        return StringUtils.convertColorToHexString(color) + text;
    }
}
