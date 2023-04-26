package com.halloapp.katchup;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.util.ViewDataLoader;

import java.util.concurrent.Callable;

public class GeotagLoader extends ViewDataLoader<TextView, String, UserId> {

    private final LruCache<UserId, String> cache = new LruCache<>(512);

    public void load(@NonNull TextView view, @NonNull UserId userId) {
        Callable<String> loader = () -> ContactsDb.getInstance().readGeotag(userId);
        Displayer<TextView, String> displayer = new Displayer<TextView, String>() {
            @Override
            public void showResult(@NonNull TextView view, @Nullable String result) {
                view.setText(result);
                view.setVisibility(result != null ? View.VISIBLE : View.GONE);
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setVisibility(View.GONE);
            }
        };
        super.load(view, loader, displayer, userId, cache);
    }
}
