package com.halloapp.ui;

import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Reaction;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.ReactionsLayout;

import java.util.List;
import java.util.concurrent.Callable;

public class ReactionLoader extends ViewDataLoader<ReactionsLayout, List<Reaction>, String> {

    private final LruCache<String, List<Reaction>> cache = new LruCache<>(512);
    private final ContentDb contentDb;

    private final ContentDb.Observer reactionObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onReactionAdded(@NonNull Reaction reaction, @NonNull ContentItem contentItem) {
            cache.remove(reaction.contentId);
        }
    };

    public ReactionLoader() {
        contentDb = ContentDb.getInstance();
        contentDb.addObserver(reactionObserver);
    }

    @MainThread
    public void load(@NonNull ReactionsLayout view, String contentId) {
        final Callable<List<Reaction>> loader = () -> contentDb.getReactions(contentId);
        final ViewDataLoader.Displayer<ReactionsLayout, List<Reaction>> displayer = new ViewDataLoader.Displayer<ReactionsLayout, List<Reaction>>() {

            @Override
            public void showResult(@NonNull ReactionsLayout view, List<Reaction> reactions) {
                if (reactions != null && !reactions.isEmpty()) {
                    view.setReactions(reactions);
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.GONE);
                }
            }

            @Override
            public void showLoading(@NonNull ReactionsLayout view) {
                view.setVisibility(View.GONE);
            }
        };

        load(view, loader, displayer, contentId, cache);
    }

    @Override
    public void destroy() {
        super.destroy();
        contentDb.removeObserver(reactionObserver);
    }
}
