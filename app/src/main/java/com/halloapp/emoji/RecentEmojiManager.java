package com.halloapp.emoji;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.gson.Gson;
import com.halloapp.Preferences;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class RecentEmojiManager {

    private final RecentEmojis recentEmojiSet;

    private final Preferences preferences;

    private static RecentEmojiManager instance;

    public static RecentEmojiManager getInstance() {
        if (instance == null) {
            synchronized (RecentEmojiManager.class) {
                if (instance == null) {
                    instance = new RecentEmojiManager();
                }
            }
        }
        return instance;
    }

    private static final int PERSIST_DELAY_MS = 2000;

    private static final int MAX_RECENT_EMOJIS = 75;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final RecentEmojiLiveData recentEmojiLiveData;

    public class RecentEmojiLiveData extends LiveData<List<Emoji>> {

        private boolean active = false;

        @Override
        protected void onActive() {
            active = true;
            update();
        }

        @Override
        protected void onInactive() {
            active = false;
        }

        public void update() {
            if (active) {
                setValue(recentEmojiSet.getList());
            }
        }
    }

    private RecentEmojiManager() {
        preferences = Preferences.getInstance();
        recentEmojiSet = loadRecentEmojis();

        recentEmojiLiveData = new RecentEmojiLiveData();
    }

    @NonNull
    public LiveData<List<Emoji>> getRecentEmojiList() {
        return recentEmojiLiveData;
    }

    @NonNull
    private RecentEmojis loadRecentEmojis() {
        Gson gson = new Gson();
        RecentEmojis re = gson.fromJson(preferences.getRecentEmojis(), RecentEmojis.class);
        if (re == null) {
            re = new RecentEmojis();
        }
        return re;
    }

    private final Runnable saveEmojis = this::saveRecentEmojis;

    private static class RecentEmojis {
        public LinkedHashSet<String> emojiSet;

        public RecentEmojis() {
            emojiSet = new LinkedHashSet<>();
        }

        public List<Emoji> getList() {
            ArrayList<Emoji> emojiList = new ArrayList<>(emojiSet.size());
            for (String s : emojiSet) {
                emojiList.add(new Emoji(s));
            }
            Collections.reverse(emojiList);
            return emojiList;
        }

        public void onEmojiUsed(@NonNull Emoji emoji) {
            emojiSet.remove(emoji.getUnicode());
            emojiSet.add(emoji.getUnicode());
            if (emojiSet.size() > MAX_RECENT_EMOJIS) {
                Iterator<String> iterator = emojiSet.iterator();
                iterator.next();
                iterator.remove();
            }
        }
    }

    public void onEmoji(@NonNull Emoji emoji) {
        recentEmojiSet.onEmojiUsed(emoji);
        recentEmojiLiveData.update();
        persistRecents();
    }

    private synchronized void persistRecents() {
        mainHandler.removeCallbacks(saveEmojis);
        mainHandler.postDelayed(saveEmojis, PERSIST_DELAY_MS);
    }

    private void saveRecentEmojis() {
        Gson gson = new Gson();
        String json = gson.toJson(recentEmojiSet);
        Log.i("RecentEmojiManager/saveRecentEmojis: " + json);
        preferences.setRecentEmojis(json);
    }
}
