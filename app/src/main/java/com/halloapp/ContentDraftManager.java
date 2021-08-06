package com.halloapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.ChatId;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ContentDraftManager {

    private static ContentDraftManager instance;

    public static ContentDraftManager getInstance() {
        if (instance == null) {
            synchronized (ContentDraftManager.class) {
                if (instance == null) {
                    instance = new ContentDraftManager();
                }
            }
        }
        return instance;
    }

    private Map<ChatId, String> messageDrafts = new HashMap<>();
    private Map<ChatId, File> audioDrafts = new HashMap<>();

    private Map<String, File> postAudioDrafts = new HashMap<>();

    public File getCommentAudioDraft(@NonNull String postId) {
        return postAudioDrafts.remove(postId);
    }

    public void setCommentAudioDraft(@NonNull String postId, @Nullable File file) {
        postAudioDrafts.put(postId, file);
    }

    public File getAudioDraft(@NonNull ChatId chatId) {
        return audioDrafts.remove(chatId);
    }

    public String getTextDraft(@NonNull ChatId chatId) {
        return messageDrafts.remove(chatId);
    }

    public void setTextDraft(@NonNull ChatId chatId, @Nullable String text) {
        messageDrafts.put(chatId, text);
    }

    public void setAudioDraft(@NonNull ChatId chatId, @Nullable File file) {
        audioDrafts.put(chatId, file);
    }

    public void clearTextDraft(@NonNull ChatId chatId) {
        messageDrafts.remove(chatId);
    }

}
