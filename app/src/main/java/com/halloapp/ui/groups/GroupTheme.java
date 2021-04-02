package com.halloapp.ui.groups;

import androidx.annotation.ColorRes;

import com.halloapp.R;

public class GroupTheme {

    public static GroupTheme getTheme(int index) {
        return new GroupTheme(index);
    }

    private GroupTheme(int index) {
        @ColorRes int bgRes = R.color.window_background;
        @ColorRes int titleRes = R.color.primary_text;
        switch (index) {
            case 1:
                bgRes = R.color.group_theme_1_bg;
                titleRes = R.color.group_theme_1_text;
                break;
            case 2:
                bgRes = R.color.group_theme_2_bg;
                titleRes = R.color.group_theme_2_text;
                break;
            case 3:
                bgRes = R.color.group_theme_3_bg;
                titleRes = R.color.group_theme_3_text;
                break;
            case 4:
                bgRes = R.color.group_theme_4_bg;
                titleRes = R.color.group_theme_4_text;
                break;
            case 5:
                bgRes = R.color.group_theme_5_bg;
                titleRes = R.color.group_theme_5_text;
                break;
            case 6:
                bgRes = R.color.group_theme_6_bg;
                titleRes = R.color.group_theme_6_text;
                break;
            case 7:
                bgRes = R.color.group_theme_7_bg;
                titleRes = R.color.group_theme_7_text;
                break;
            case 8:
                bgRes = R.color.group_theme_8_bg;
                titleRes = R.color.group_theme_8_text;
                break;
            case 9:
                bgRes = R.color.group_theme_9_bg;
                titleRes = R.color.group_theme_9_text;
                break;
            case 10:
                bgRes = R.color.group_theme_10_bg;
                titleRes = R.color.group_theme_10_text;
                break;
        }
        bgColor = bgRes;
        textColor = titleRes;

    }

    public final @ColorRes int bgColor;
    public final @ColorRes int textColor;
}
