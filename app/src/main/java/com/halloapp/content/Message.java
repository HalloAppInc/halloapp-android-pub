package com.halloapp.content;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.BuildConfig;
import com.halloapp.Me;
import com.halloapp.UrlPreview;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.Album;
import com.halloapp.proto.clients.AlbumMedia;
import com.halloapp.proto.clients.ChatContainer;
import com.halloapp.proto.clients.ChatContext;
import com.halloapp.proto.clients.Text;
import com.halloapp.proto.clients.VoiceNote;
import com.halloapp.xmpp.Connection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class Message extends ContentItem {

    public final ChatId chatId;

    public final @Type int type;
    public final @Usage int usage;
    public final @State int state;

    public final int rerequestCount;

    public final String replyPostId;
    public final int replyPostMediaIndex;
    public final String replyMessageId;
    public final int replyMessageMediaIndex;
    public final UserId replyMessageSenderId;

    // stats not read from DB
    public String failureReason;
    public String clientVersion;
    public String senderVersion;
    public String senderPlatform;

    @SuppressLint("UniqueConstants")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_CHAT, TYPE_SYSTEM, TYPE_FUTURE_PROOF, TYPE_VOICE_NOTE, TYPE_RETRACTED})
    public @interface Type {}
    public static final int TYPE_CHAT = 0;
    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_FUTURE_PROOF = 2;
    public static final int TYPE_VOICE_NOTE = 3;
    public static final int TYPE_RETRACTED = 4;

    @SuppressLint("UniqueConstants")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({USAGE_CHAT, USAGE_BLOCK, USAGE_UNBLOCK, USAGE_CREATE_GROUP, USAGE_ADD_MEMBERS, USAGE_REMOVE_MEMBER, USAGE_MEMBER_LEFT, USAGE_PROMOTE, USAGE_DEMOTE, USAGE_AUTO_PROMOTE, USAGE_NAME_CHANGE, USAGE_AVATAR_CHANGE, USAGE_GROUP_DELETED, USAGE_KEYS_CHANGED, USAGE_MISSED_AUDIO_CALL, USAGE_MISSED_VIDEO_CALL})
    public @interface Usage {}
    public static final int USAGE_CHAT = 0;
    public static final int USAGE_BLOCK = 1;
    public static final int USAGE_UNBLOCK = 2;
    public static final int USAGE_CREATE_GROUP = 3;
    public static final int USAGE_ADD_MEMBERS = 4;
    public static final int USAGE_REMOVE_MEMBER = 5;
    public static final int USAGE_MEMBER_LEFT = 6;
    public static final int USAGE_PROMOTE = 7;
    public static final int USAGE_DEMOTE = 8;
    public static final int USAGE_AUTO_PROMOTE = 9;
    public static final int USAGE_NAME_CHANGE = 10;
    public static final int USAGE_AVATAR_CHANGE = 11;
    public static final int USAGE_GROUP_DELETED = 12;
    public static final int USAGE_KEYS_CHANGED = 13;
    public static final int USAGE_MISSED_AUDIO_CALL = 14;
    public static final int USAGE_MISSED_VIDEO_CALL = 15;

    @SuppressLint("UniqueConstants")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_INITIAL, STATE_INCOMING_RECEIVED, STATE_OUTGOING_SENT, STATE_OUTGOING_DELIVERED, STATE_OUTGOING_SEEN, STATE_INCOMING_DECRYPT_FAILED, STATE_OUTGOING_PLAYED, STATE_INCOMING_PLAYED})
    public @interface State {}
    public static final int STATE_INITIAL = 0;
    public static final int STATE_INCOMING_RECEIVED = 1;
    public static final int STATE_OUTGOING_SENT = 1;
    public static final int STATE_OUTGOING_DELIVERED = 2;
    public static final int STATE_OUTGOING_SEEN = 3;
    public static final int STATE_INCOMING_DECRYPT_FAILED = 4;
    public static final int STATE_OUTGOING_PLAYED = 5;
    public static final int STATE_INCOMING_PLAYED = 6;


    public Message(
            long rowId,
            ChatId chatId,
            UserId senderUserId,
            String messageId,
            long timestamp,
            @Type int messageType,
            @Usage int usage,
            @State int state,
            String text,
            String replyPostId,
            int replyPostMediaIndex,
            String replyMessageId,
            int replyMessageMediaIndex,
            UserId replyMessageSenderId,
            int rerequestCount) {
        super(rowId, senderUserId, messageId, timestamp, text);
        this.chatId = chatId;
        this.type = messageType;
        this.usage = usage;
        this.state = state;
        this.replyPostId = TextUtils.isEmpty(replyPostId) ? null : replyPostId;
        this.replyPostMediaIndex = replyPostMediaIndex;
        this.replyMessageId = replyMessageId;
        this.replyMessageMediaIndex = replyMessageMediaIndex;
        this.replyMessageSenderId = replyMessageSenderId;
        this.rerequestCount = rerequestCount;
    }

    public static Message parseFromProto(UserId fromUserId, String id, long timestamp, @NonNull ChatContainer chatContainer) {
        Message message;
        ChatContext context = chatContainer.getContext();
        String rawReplyMessageId = context.getChatReplyMessageId();
        String rawSenderId = context.getChatReplyMessageSenderId();
        switch (chatContainer.getMessageCase()) {
            case ALBUM:
                Album album = chatContainer.getAlbum();
                Text albumText = album.getText();
                message = new Message(0,
                        fromUserId,
                        fromUserId,
                        id,
                        timestamp,
                        Message.TYPE_CHAT,
                        Message.USAGE_CHAT,
                        album.getMediaCount() == 0 ? Message.STATE_INCOMING_RECEIVED : Message.STATE_INITIAL,
                        albumText.getText(),
                        context.getFeedPostId(),
                        context.getFeedPostMediaIndex(),
                        TextUtils.isEmpty(rawReplyMessageId) ? null : rawReplyMessageId,
                        context.getChatReplyMessageMediaIndex(),
                        rawSenderId.equals(Me.getInstance().getUser()) ? UserId.ME : new UserId(rawSenderId),
                        0);
                for (AlbumMedia item : album.getMediaList()) {
                    message.media.add(Media.parseFromProto(item));
                }
                for (com.halloapp.proto.clients.Mention item : albumText.getMentionsList()) {
                    message.mentions.add(Mention.parseFromProto(item));
                }
                if (albumText.hasLink()) {
                    message.urlPreview = UrlPreview.fromProto(albumText.getLink());
                }
                break;
            case TEXT:
                Text text = chatContainer.getText();
                message = new Message(0,
                        fromUserId,
                        fromUserId,
                        id,
                        timestamp,
                        Message.TYPE_CHAT,
                        Message.USAGE_CHAT,
                        Message.STATE_INCOMING_RECEIVED,
                        text.getText(),
                        context.getFeedPostId(),
                        context.getFeedPostMediaIndex(),
                        TextUtils.isEmpty(rawReplyMessageId) ? null : rawReplyMessageId,
                        context.getChatReplyMessageMediaIndex(),
                        rawSenderId.equals(Me.getInstance().getUser()) ? UserId.ME : new UserId(rawSenderId),
                        0);
                for (com.halloapp.proto.clients.Mention item : text.getMentionsList()) {
                    message.mentions.add(Mention.parseFromProto(item));
                }
                if (text.hasLink()) {
                    message.urlPreview = UrlPreview.fromProto(text.getLink());
                }
                break;
            case VOICE_NOTE:
                VoiceNote voiceNote = chatContainer.getVoiceNote();
                message = new VoiceNoteMessage(0,
                        fromUserId,
                        fromUserId,
                        id,
                        timestamp,
                        Message.USAGE_CHAT,
                        Message.STATE_INITIAL,
                        null,
                        context.getFeedPostId(),
                        context.getFeedPostMediaIndex(),
                        TextUtils.isEmpty(rawReplyMessageId) ? null : rawReplyMessageId,
                        context.getChatReplyMessageMediaIndex(),
                        rawSenderId.equals(Me.getInstance().getUser()) ? UserId.ME : new UserId(rawSenderId),
                        0);
                message.media.add(Media.parseFromProto(voiceNote));
                break;
            default:
            case MESSAGE_NOT_SET: {
                FutureProofMessage futureProofMessage = new FutureProofMessage(0,
                        fromUserId,
                        fromUserId,
                        id,
                        timestamp,
                        Message.USAGE_CHAT,
                        Message.STATE_INCOMING_RECEIVED,
                        null,
                        context.getFeedPostId(),
                        context.getFeedPostMediaIndex(),
                        TextUtils.isEmpty(rawReplyMessageId) ? null : rawReplyMessageId,
                        context.getChatReplyMessageMediaIndex(),
                        rawSenderId.equals(Me.getInstance().getUser()) ? UserId.ME : new UserId(rawSenderId),
                        0);
                futureProofMessage.setProtoBytes(chatContainer.toByteArray());
                message = futureProofMessage;
                break;
            }
        }
        return message;
    }

    public boolean isLocalMessage() {
        return type == Message.TYPE_SYSTEM;
    }

    public boolean isTombstone() {
        return state == Message.STATE_INCOMING_DECRYPT_FAILED;
    }

    public boolean isRetracted() {
        return (type == TYPE_CHAT && super.isRetracted()) || type == TYPE_RETRACTED;
    }

    public boolean isMeMessageSender() {
        return senderUserId.equals(UserId.ME);
    }

    @Override
    public void addToStorage(@NonNull ContentDb contentDb) {
        contentDb.addMessage(this);
    }

    @Override
    public void send(@NonNull Connection connection) {
        SignalSessionManager.getInstance().sendMessage(this);
    }

    @Override
    public void setMediaTransferred(@NonNull Media media, @NonNull ContentDb contentDb) {
        contentDb.setMediaTransferred(this, media);
    }

    @Override
    public void setPatchUrl(long rowId, @NonNull String url, @NonNull ContentDb contentDb) {
        contentDb.setPatchUrl(this, rowId, url);
    }

    @Override
    public String getPatchUrl(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getPatchUrl(this, rowId);
    }

    @Override
    public @Media.TransferredState int getMediaTransferred(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getMediaTransferred(this, rowId);
    }

    @Override
    public byte[] getMediaEncKey(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getMediaEncKey(rowId);
    }

    @Override
    public void setUploadProgress(long rowId, long offset, @NonNull ContentDb contentDb) {
        contentDb.setUploadProgress(this, rowId, offset);
    }

    @Override
    public long getUploadProgress(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getUploadProgress(this, rowId);
    }

    @Override
    public void setRetryCount(long rowId, int count, @NonNull ContentDb contentDb) {
        contentDb.setRetryCount(this, rowId, count);
    }

    @Override
    public int getRetryCount(long rowId, @NonNull ContentDb contentDb) {
        return contentDb.getRetryCount(this, rowId);
    }

    @Override
    public @NonNull String toString() {
        return "Message {timestamp:" + timestamp + " sender:" + senderUserId + ", state: " + state + ", id:" + id + (BuildConfig.DEBUG ? ", text:" + text : "") + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        final Message message = (Message) o;
        return rowId == message.rowId &&
                Objects.equals(senderUserId, message.senderUserId) &&
                Objects.equals(id, message.id) &&
                timestamp == message.timestamp &&
                Objects.equals(text, message.text) &&
                state == message.state &&
                media.equals(message.media) &&
                Objects.equals(urlPreview, message.urlPreview);
    }
}
