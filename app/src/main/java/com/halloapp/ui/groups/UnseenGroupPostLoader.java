package com.halloapp.ui.groups;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.transition.TransitionManager;

import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.util.ViewDataLoader;

import java.util.concurrent.Callable;

public class UnseenGroupPostLoader extends ViewDataLoader<View, Post, ChatId> {

    private final ContentDb contentDb;

    public UnseenGroupPostLoader(@NonNull Context context) {
        contentDb = ContentDb.getInstance(context);
    }

    @MainThread
    public void load(@NonNull View view, @NonNull ChatId chatId) {
        final Callable<Post> loader = () -> {
            if (chatId instanceof GroupId) {
                return contentDb.getLastUnseenGroupPost((GroupId) chatId);
            }
            return null;
        };
        final ViewDataLoader.Displayer<View, Post> displayer = new ViewDataLoader.Displayer<View, Post>() {

            @Override
            public void showResult(@NonNull View view, Post lastPost) {
                TransitionManager.beginDelayedTransition((ViewGroup) view.getParent());
                if (lastPost == null) {
                    view.setVisibility(View.GONE);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void showLoading(@NonNull View view) {
                view.setVisibility(View.GONE);
            }
        };
        load(view, loader, displayer, chatId, null);
    }
}
