package com.halloapp.katchup.ui;

import androidx.annotation.ColorRes;

import com.halloapp.R;

public class Colors {
    public static final int[] COMMENT_COLORS = {
            R.color.comment_text_color_1,
            R.color.comment_text_color_2,
            R.color.comment_text_color_3,
            R.color.comment_text_color_4,
            R.color.comment_text_color_5,
            R.color.comment_text_color_6,
            R.color.comment_text_color_7,
            R.color.comment_text_color_8,
            R.color.comment_text_color_9
    };

    public static final int[] AVATAR_BG_COLORS = {
            R.color.avatar_bg_color_1,
            R.color.avatar_bg_color_2,
            R.color.avatar_bg_color_3,
            R.color.avatar_bg_color_4,
            R.color.avatar_bg_color_5,
            R.color.avatar_bg_color_6,
            R.color.avatar_bg_color_7,
            R.color.avatar_bg_color_8,
            R.color.avatar_bg_color_9,
            R.color.avatar_bg_color_10,
            R.color.avatar_bg_color_11
    };

    public static @ColorRes int getAvatarBgColor(int index) {
        return safeIndex(AVATAR_BG_COLORS, index);
    }

    public static @ColorRes int getCommentColor(int index) {
        return safeIndex(COMMENT_COLORS, index);
    }

    private static @ColorRes int safeIndex(int[] arr, int index) {
        index = index % arr.length;
        if (index < 0) {
            index += arr.length;
        }
        return arr[index];
    }
}
