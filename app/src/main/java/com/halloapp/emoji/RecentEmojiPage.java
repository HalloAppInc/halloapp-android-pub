package com.halloapp.emoji;

import androidx.lifecycle.LiveData;

import java.util.List;

public class RecentEmojiPage extends EmojiPage {

    private final LiveData<List<Emoji>> emojiListLiveData;
    public RecentEmojiPage(int icon, LiveData<List<Emoji>> emojiListLiveData) {
        super(icon);
        this.emojiListLiveData = emojiListLiveData;
    }

    @Override
    public List<Emoji> getEmojis() {
        return emojiListLiveData.getValue();
    }
}
