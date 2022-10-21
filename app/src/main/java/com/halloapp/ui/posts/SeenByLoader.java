package com.halloapp.ui.posts;

import android.util.Pair;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Reaction;
import com.halloapp.id.UserId;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.AvatarsLayout;

import java.util.List;
import java.util.concurrent.Callable;

public class SeenByLoader extends ViewDataLoader<AvatarsLayout, Pair<List<UserId>, List<Reaction>>, String> {

    private final LruCache<String, Pair<List<UserId>, List<Reaction>>> cache = new LruCache<>(512);
    private final ContentDb contentDb;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            cache.remove(postId);
        }

        @Override
        public void onReactionAdded(@NonNull Reaction reaction, @NonNull ContentItem contentItem) {
            cache.remove(reaction.contentId);
        }
    };

    public SeenByLoader() {
        contentDb = ContentDb.getInstance();
        contentDb.addObserver(contentObserver);
    }

    @MainThread
    public void load(@NonNull AvatarsLayout view, @NonNull String postId) {
        final Callable<Pair<List<UserId>, List<Reaction>>> loader = () -> new Pair<>(contentDb.getPostSeenByUsers(postId), contentDb.getReactions(postId));
        final ViewDataLoader.Displayer<AvatarsLayout, Pair<List<UserId>, List<Reaction>>> displayer = new ViewDataLoader.Displayer<AvatarsLayout, Pair<List<UserId>, List<Reaction>>>() {

            @Override
            public void showResult(@NonNull AvatarsLayout view, Pair<List<UserId>, List<Reaction>> pair) {
                view.setUsersAndReactions(pair.first, pair.second);
            }

            @Override
            public void showLoading(@NonNull AvatarsLayout view) {
                view.setAllImageResource(R.drawable.avatar_person);
                view.clearReactions();
            }
        };
        load(view, loader, displayer, postId, cache);
    }

    @Override
    public void destroy() {
        super.destroy();
        contentDb.removeObserver(contentObserver);
    }
}
