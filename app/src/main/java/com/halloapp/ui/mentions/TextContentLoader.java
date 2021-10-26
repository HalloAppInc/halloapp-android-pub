package com.halloapp.ui.mentions;

import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.halloapp.Me;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Mention;
import com.halloapp.ui.markdown.MarkdownUtils;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.ViewDataLoader;

import java.util.List;
import java.util.concurrent.Callable;

public class TextContentLoader extends ViewDataLoader<TextView, List<Mention>, TextContent> {

    private final Me me;
    private final ContactsDb contactsDb;

    private final LruCache<TextContent, List<Mention>> cache = new LruCache<>(512);

    public TextContentLoader() {
        me = Me.getInstance();
        contactsDb = ContactsDb.getInstance();
    }

    public interface TextDisplayer {
        void showResult(TextView tv, CharSequence text);
        void showPreview(TextView tv, CharSequence text);
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull TextContent textContent) {
        load(view, textContent, new TextDisplayer() {
            @Override
            public void showResult(TextView tv, CharSequence text) {
                tv.setText(text);
            }

            @Override
            public void showPreview(TextView tv, CharSequence text) {
                tv.setText(text);
            }
        });
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull TextContent textContent, boolean showAt) {
        load(view, textContent, showAt, new TextDisplayer() {
            @Override
            public void showResult(TextView tv, CharSequence text) {
                tv.setText(text);
            }

            @Override
            public void showPreview(TextView tv, CharSequence text) {
                tv.setText(text);
            }
        });
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull TextContent textContent, @NonNull TextDisplayer textDisplayer) {
        load(view, textContent, true, textDisplayer);
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull TextContent textContent, boolean showAt, @NonNull TextDisplayer textDisplayer) {
        List<Mention> mentions = textContent.getMentions();
        String text = textContent.getText();
        if (mentions.isEmpty()) {
            textDisplayer.showResult(view, MarkdownUtils.formatMarkdownWithMentions(view.getContext(), text, mentions));
            return;
        }
        final Callable<List<Mention>> loader = () -> MentionsLoader.loadMentionNames(me, contactsDb, mentions);
        final ViewDataLoader.Displayer<TextView, List<Mention>> displayer = new ViewDataLoader.Displayer<TextView, List<Mention>>() {

            @Override
            public void showResult(@NonNull TextView view, @Nullable List<Mention> result) {
                if (result == null) {
                    textDisplayer.showResult(view, text);
                    return;
                }
                textDisplayer.showResult(view, MarkdownUtils.formatMarkdownWithMentions(view.getContext(), text, result, showAt, (v, mention) -> {
                    v.getContext().startActivity(ViewProfileActivity.viewProfile(v.getContext(), mention.userId));
                }));
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                textDisplayer.showPreview(view, MarkdownUtils.formatMarkdownWithMentions(view.getContext(), text, mentions, showAt, null));
            }
        };
        load(view, loader, displayer, textContent, cache);
    }
}
