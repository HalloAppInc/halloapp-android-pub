package com.halloapp;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.ChatId;
import com.halloapp.util.BgWorkers;
import com.halloapp.drafts.db.DraftsDb;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContentDraftManager {

    private static ContentDraftManager instance;
    private final DraftsDb draftsDb;
    private final BgWorkers bgWorkers;
    private final Set<ContentDraftObserver> observers = new HashSet<>();

    private ContentDraftManager (final @NonNull AppContext appContext, final @NonNull BgWorkers bgWorkersInstance) {
        draftsDb = new DraftsDb(appContext);
        bgWorkers = bgWorkersInstance;
    }

    public static ContentDraftManager getInstance() {
        if (instance == null) {
            synchronized (ContentDraftManager.class) {
                if (instance == null) {
                    instance = new ContentDraftManager(AppContext.getInstance(), BgWorkers.getInstance());
                }
            }
        }
        return instance;
    }

    private final Map<ChatId, String> messageDrafts = new HashMap<>();
    private final Map<ChatId, File> audioDrafts = new HashMap<>();

    private final Map<String, File> postAudioDrafts = new HashMap<>();

    private final Map<String, String> postCommentDrafts = new HashMap<>();

    @AnyThread
    public void init() {
        bgWorkers.execute(() -> {
            messageDrafts.putAll(draftsDb.getAllChatDraftText());
            postCommentDrafts.putAll(draftsDb.getAllCommentDraftText());
        });
    }

    public String getPostCommentDraft(@NonNull String postId) {
        return postCommentDrafts.get(postId);
    }

    public void setPostCommentDraft(@NonNull String postId, @Nullable String post) {
        postCommentDrafts.put(postId, post);
        draftsDb.insertCommentDraftRecord(postId, post);
    }

    public File getCommentAudioDraft(@NonNull String postId) {
        return postAudioDrafts.remove(postId);
    }

    public void setCommentAudioDraft(@NonNull String postId, @Nullable File file) {
        postAudioDrafts.put(postId, file);
    }

    public File getAudioDraft(@NonNull ChatId chatId) {
        return audioDrafts.get(chatId);
    }

    public String getTextDraft(@NonNull ChatId chatId) {
        return messageDrafts.get(chatId);
    }

    public void setTextDraft(@NonNull ChatId chatId, @Nullable String text) {
        messageDrafts.put(chatId, text);
        draftsDb.insertChatDraftRecord(chatId, text);
        notifyDraftUpdated();
    }

    public void setAudioDraft(@NonNull ChatId chatId, @Nullable File file) {
        audioDrafts.put(chatId, file);
    }

    public void clearPostCommentDraft(@NonNull String postId) {
        postCommentDrafts.remove(postId);
        draftsDb.deleteCommentDraftRecord(postId);
    }

    public void clearTextDraft(@NonNull ChatId chatId) {
        messageDrafts.remove(chatId);
        draftsDb.deleteChatDraftRecord(chatId);
        notifyDraftUpdated();
    }

    public void addObserver(@NonNull ContentDraftObserver observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void removeObserver(@NonNull ContentDraftObserver observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    private void notifyDraftUpdated() {
        synchronized (observers) {
            for (ContentDraftObserver observer : observers) {
                observer.onDraftUpdated();
            }
        }
    }

    public interface ContentDraftObserver {
        void onDraftUpdated();
    }

}
