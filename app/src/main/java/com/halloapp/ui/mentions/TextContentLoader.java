package com.halloapp.ui.mentions;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Mention;
import com.halloapp.util.ViewDataLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class TextContentLoader extends ViewDataLoader<TextView, List<Mention>, TextContent> {

    private final Me me;
    private final ContactsDb contactsDb;

    private LruCache<TextContent, List<Mention>> cache = new LruCache<>(512);

    public TextContentLoader(@NonNull Context context) {
        me = Me.getInstance(context);
        contactsDb = ContactsDb.getInstance(context);
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull TextContent textContent) {
        List<Mention> mentions = textContent.getMentions();
        String text = textContent.getText();
        if (mentions.isEmpty()) {
            view.setText(text);
            return;
        }
        
        final Callable<List<Mention>> loader = () -> {
            List<Mention> ret = new ArrayList<>();
            for (Mention mention : mentions) {
                Contact contact = contactsDb.getContact(mention.userId);
                String mentionText;
                if (mention.userId.isMe()) {
                    mentionText = me.getName();
                } else {
                    if (!TextUtils.isEmpty(mention.fallbackName)) {
                        contact.fallbackName = mention.fallbackName;
                    }
                    mentionText = contact.getDisplayName();
                }
                ret.add(new Mention(mention.index, mention.userId, mentionText));
            }
            return ret;
        };
        final ViewDataLoader.Displayer<TextView, List<Mention>> displayer = new ViewDataLoader.Displayer<TextView, List<Mention>>() {

            @Override
            public void showResult(@NonNull TextView view, @Nullable List<Mention> result) {
                if (result == null) {
                    view.setText(text);
                    return;
                }
                view.setText(MentionsFormatter.insertMentions(text, result));
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setText(MentionsFormatter.insertMentions(text, mentions));
            }
        };
        load(view, loader, displayer, textContent, cache);
    }
}
