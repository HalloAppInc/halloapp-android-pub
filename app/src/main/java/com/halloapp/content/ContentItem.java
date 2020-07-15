package com.halloapp.content;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.halloapp.contacts.UserId;
import com.halloapp.ui.mentions.TextContent;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.List;

public abstract class ContentItem implements TextContent {

    public long rowId;
    public final UserId senderUserId;
    public final String id;
    public final long timestamp;

    public final String text;
    public final List<Media> media = new ArrayList<>();
    public final List<Mention> mentions = new ArrayList<>();

    public ContentItem(
            long rowId,
            UserId senderUserId,
            String id,
            long timestamp,
            String text) {
        this.rowId = rowId;
        this.senderUserId = senderUserId;
        this.id = id;
        this.timestamp = timestamp;
        this.text = TextUtils.isEmpty(text) ? null : text;
    }

    public abstract void addToStorage(@NonNull ContentDb contentDb);
    public abstract void send(@NonNull Connection connection);
    public abstract void setMediaTransferred(@NonNull Media media, @NonNull ContentDb contentDb);
    public abstract void setPatchUrl(long rowId, @NonNull String url, @NonNull ContentDb contentDb);
    public abstract String getPatchUrl(long rowId, @NonNull ContentDb contentDb);
    public abstract @Media.TransferredState int getMediaTransferred(long rowId, @NonNull ContentDb contentDb);

    public boolean isOutgoing() {
        return senderUserId.isMe();
    }

    public boolean isIncoming() {
        return !isOutgoing();
    }

    public boolean isRetracted() {
        return TextUtils.isEmpty(text) && media.isEmpty();
    }

    public boolean isAllMediaTransferred() {
        for (Media mediaItem : media) {
            if (mediaItem.transferred != Media.TRANSFERRED_YES) {
                return false;
            }
        }
        return true;
    }

    public boolean doesMention(@NonNull UserId userId) {
        for (Mention mention : mentions) {
            if (mention.userId.equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTransferFailed() {
        for (Media mediaItem : media) {
            if (mediaItem.transferred == Media.TRANSFERRED_FAILURE) {
                return true;
            }
        }
        return false;
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
