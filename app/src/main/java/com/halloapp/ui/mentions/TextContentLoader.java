package com.halloapp.ui.mentions;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.halloapp.Me;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Mention;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.ViewDataLoader;

import java.util.List;
import java.util.concurrent.Callable;

public class TextContentLoader extends ViewDataLoader<TextView, List<Mention>, TextContent> {

    private final Me me;
    private final ContactsDb contactsDb;

    private LruCache<TextContent, List<Mention>> cache = new LruCache<>(512);

    public TextContentLoader(@NonNull Context context) {
        me = Me.getInstance();
        contactsDb = ContactsDb.getInstance();
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull TextContent textContent) {
        List<Mention> mentions = textContent.getMentions();
        String text = textContent.getText();
        if (mentions.isEmpty()) {
            view.setText(text);
            return;
        }
        
        final Callable<List<Mention>> loader = () -> MentionsLoader.loadMentionNames(me, contactsDb, mentions);
        final ViewDataLoader.Displayer<TextView, List<Mention>> displayer = new ViewDataLoader.Displayer<TextView, List<Mention>>() {

            @Override
            public void showResult(@NonNull TextView view, @Nullable List<Mention> result) {
                if (result == null) {
                    view.setText(text);
                    return;
                }
                view.setText(MentionsFormatter.insertMentions(text, result, ((v, mention) -> {
                    v.getContext().startActivity(ViewProfileActivity.viewProfile(v.getContext(), mention.userId));
                })));
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setText(MentionsFormatter.insertMentions(text, mentions));
            }
        };
        load(view, loader, displayer, textContent, cache);
    }
}
