package com.halloapp.ui.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.ReplyPreview;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.mentions.TextContent;
import com.halloapp.util.ViewDataLoader;

import java.util.List;
import java.util.concurrent.Callable;

class ReplyLoader extends ViewDataLoader<View, ReplyLoader.Result, Long> {

    private final ContentDb contentDb;
    private final ContactsDb contactsDb;
    private final int dimensionLimit;
    private final LruCache<Long, Result> cache = new LruCache<>(32);

    @MainThread
    ReplyLoader(@NonNull Context context, int dimensionLimit) {
        this.dimensionLimit = dimensionLimit;
        this.contentDb = ContentDb.getInstance(context);
        this.contactsDb = ContactsDb.getInstance(context);
    }

    @MainThread
    public void load(@NonNull View view, @NonNull Message message, @NonNull ViewDataLoader.Displayer<View, Result> displayer) {
        final Callable<Result> loader = () -> {

            String name = null;
            if (message.isOutgoing()) {
                name = contactsDb.getContact(new UserId(message.chatId)).getDisplayName();
            }
            final ReplyPreview replyPreview = contentDb.getReplyPreview(message.rowId);
            if (replyPreview == null) {
                return new Result(name, null, null, Media.MEDIA_TYPE_UNKNOWN, null);
            }
            Bitmap thumb = null;
            if (replyPreview.file != null) {
                thumb = MediaUtils.decodeImage(replyPreview.file, dimensionLimit);
            }
            return new Result(name, replyPreview.text, replyPreview.mentions, replyPreview.mediaType, thumb);
        };
        load(view, loader, displayer, message.rowId, cache);
    }

    static class Result implements TextContent {
        final String name;
        final String text;
        final Bitmap thumb;
        final @Media.MediaType int mediaType;
        final @Nullable List<Mention> mentions;

        Result(String name, String text, @Nullable List<Mention> mentions, @Media.MediaType int mediaType, Bitmap thumb) {
            this.name = name;
            this.thumb = thumb;
            this.mediaType = mediaType;
            this.mentions = mentions;
            this.text = text;
        }

        @Override
        public List<Mention> getMentions() {
            return mentions;
        }

        @Override
        public String getText() {
            return text;
        }
    }
}
