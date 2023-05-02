package com.halloapp.ui.chat.chat;

import android.graphics.Bitmap;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
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
    ReplyLoader(int dimensionLimit) {
        this.dimensionLimit = dimensionLimit;
        this.contentDb = ContentDb.getInstance();
        this.contactsDb = ContactsDb.getInstance();
    }

    @MainThread
    public void load(@NonNull View view, @NonNull Message message, @NonNull ViewDataLoader.Displayer<View, Result> displayer) {
        final Callable<Result> loader = () -> {
            final ReplyPreview replyPreview = contentDb.getReplyPreview(message.rowId);
            String name = null;
            if (message.replyPostId != null) {
                Post replyPost = contentDb.getPost(message.replyPostId);
                if (replyPost != null) {
                    if (replyPost.senderUserId.isMe()) {
                        name = view.getContext().getString(R.string.me);
                    } else {
                        name = contactsDb.getContact(replyPost.senderUserId).getDisplayName();
                    }
                } else {
                    if (message.replyMessageSenderId.isMe()) {
                        name = view.getContext().getString(R.string.me);
                    } else {
                        name = contactsDb.getContact(message.replyMessageSenderId).getDisplayName();
                    }
                    if (replyPreview != null && replyPreview.postType != null && (replyPreview.postType == Post.TYPE_MOMENT || replyPreview.postType == Post.TYPE_MOMENT_PSA)) {
                        return new Result(name, null, replyPreview.mentions, replyPreview.mediaType, null, replyPreview.postType);
                    }
                }
            } else if (message.replyMessageId != null) {
                if (message.replyMessageSenderId.isMe()) {
                    name = view.getContext().getString(R.string.me);
                } else {
                    name = contactsDb.getContact(message.replyMessageSenderId).getDisplayName();
                }
            }
            if (replyPreview == null) {
                return new Result(name, null, null, Media.MEDIA_TYPE_UNKNOWN, null, null);
            }
            Bitmap thumb = null;
            if (replyPreview.file != null) {
                thumb = MediaUtils.decodeImage(replyPreview.file, dimensionLimit);
            }
            return new Result(name, replyPreview.text, replyPreview.mentions, replyPreview.mediaType, thumb, replyPreview.postType);
        };
        load(view, loader, displayer, message.rowId, cache);
    }

    static class Result implements TextContent {
        final String name;
        final String text;
        final Bitmap thumb;
        final @Media.MediaType int mediaType;
        final @Post.Type Integer postType;
        final @Nullable List<Mention> mentions;

        Result(String name, String text, @Nullable List<Mention> mentions, @Media.MediaType int mediaType, Bitmap thumb, @Post.Type Integer postType) {
            this.name = name;
            this.thumb = thumb;
            this.mediaType = mediaType;
            this.mentions = mentions;
            this.text = text;
            this.postType = postType;
        }

        @Override @Nullable
        public List<Mention> getMentions() {
            return mentions;
        }

        @Override
        public String getText() {
            return text;
        }
    }
}
