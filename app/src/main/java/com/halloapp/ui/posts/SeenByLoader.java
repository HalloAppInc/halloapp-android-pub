package com.halloapp.ui.posts;

import android.content.Context;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.content.ContentDb;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.AvatarsLayout;

import java.util.List;
import java.util.concurrent.Callable;

public class SeenByLoader extends ViewDataLoader<AvatarsLayout, List<UserId>, String> {

    private final LruCache<String, List<UserId>> cache = new LruCache<>(512);
    private final ContentDb contentDb;

    public SeenByLoader(@NonNull Context context) {
        contentDb = ContentDb.getInstance(context);
    }

    @MainThread
    public void load(@NonNull AvatarsLayout view, @NonNull String postId) {
        final Callable<List<UserId>> loader = () -> contentDb.getPostSeenBy(postId);
        final ViewDataLoader.Displayer<AvatarsLayout, List<UserId>> displayer = new ViewDataLoader.Displayer<AvatarsLayout, List<UserId>>() {

            @Override
            public void showResult(@NonNull AvatarsLayout view, List<UserId> users) {
                view.setUsers(users);
            }

            @Override
            public void showLoading(@NonNull AvatarsLayout view) {
                view.setImageResource(R.drawable.avatar_person);
            }
        };
        load(view, loader, displayer, postId, cache);

    }
}
