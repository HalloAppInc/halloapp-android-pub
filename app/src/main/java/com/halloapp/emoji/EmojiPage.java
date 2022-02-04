package com.halloapp.emoji;

import androidx.annotation.DrawableRes;

import com.halloapp.R;

import java.util.Arrays;
import java.util.List;

public class EmojiPage {

    public final @DrawableRes int iconRes;
    private List<Emoji> emojis;

    public EmojiPage(@DrawableRes int icon, Emoji[] emojis) {
        this.iconRes = icon;
        this.emojis = Arrays.asList(emojis);
    }

    public EmojiPage(@DrawableRes int icon) {
        this.iconRes = icon;
    }

    public List<Emoji> getEmojis() {
        return emojis;
    }

    public static @DrawableRes int getDrawable(String category) {
        @DrawableRes int iconRes = R.drawable.ic_emoji_smilies;
        switch (category) {
            case "people":
                iconRes = R.drawable.ic_emoji_smilies;
                break;
            case "nature":
                iconRes = R.drawable.ic_emoji_nature;
                break;
            case "foods":
                iconRes = R.drawable.ic_emoji_food;
                break;
            case "places":
                iconRes = R.drawable.ic_emoji_places;
                break;
            case "activity":
                iconRes = R.drawable.ic_emoji_events;
                break;
            case "objects":
                iconRes = R.drawable.ic_emoji_objects;
                break;
            case "symbols":
                iconRes = R.drawable.ic_emoji_symbols;
                break;
            case "flags":
                iconRes = R.drawable.ic_emoji_flag;
                break;
        }
        return iconRes;
    }
}
