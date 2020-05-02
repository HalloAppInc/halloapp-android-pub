package com.halloapp.ui.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.ReplyPreview;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.ViewDataLoader;

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
                return new Result(name, null, Media.MEDIA_TYPE_UNKNOWN, null);
            }
            Bitmap thumb = null;
            if (replyPreview.file != null) {
                thumb = MediaUtils.decode(replyPreview.file, replyPreview.mediaType, dimensionLimit);
            }
            return new Result(name, replyPreview.text, replyPreview.mediaType, thumb);
        };
        load(view, loader, displayer, message.rowId, cache);
    }

    static class Result {
        final String name;
        final String text;
        final Bitmap thumb;
        final @Media.MediaType int mediaType;

        Result(String name, String text, @Media.MediaType int mediaType, Bitmap thumb) {
            this.name = name;
            this.thumb = thumb;
            this.mediaType = mediaType;
            this.text = text;
        }
    }
}
