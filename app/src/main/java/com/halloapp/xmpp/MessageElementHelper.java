package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.proto.clients.Album;
import com.halloapp.proto.clients.AlbumMedia;
import com.halloapp.proto.clients.ChatContainer;
import com.halloapp.proto.clients.ChatContext;
import com.halloapp.proto.clients.ChatMessage;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.EncryptedResource;
import com.halloapp.proto.clients.Image;
import com.halloapp.proto.clients.MediaType;
import com.halloapp.proto.clients.Text;
import com.halloapp.proto.clients.Video;
import com.halloapp.proto.clients.VoiceNote;
import com.halloapp.util.logs.Log;

public class MessageElementHelper {

    public static @Media.MediaType int fromProtoMediaType(@NonNull MediaType type) {
        if (type == MediaType.MEDIA_TYPE_IMAGE) {
            return Media.MEDIA_TYPE_IMAGE;
        } else if (type == MediaType.MEDIA_TYPE_VIDEO) {
            return Media.MEDIA_TYPE_VIDEO;
        }
        Log.w("Unrecognized MediaType " + type);
        return Media.MEDIA_TYPE_UNKNOWN;
    }

    private static MediaType getProtoMediaType(@Media.MediaType int type) {
        if (type == Media.MEDIA_TYPE_IMAGE) {
            return MediaType.MEDIA_TYPE_IMAGE;
        } else if (type == Media.MEDIA_TYPE_VIDEO) {
            return MediaType.MEDIA_TYPE_VIDEO;
        }
        Log.w("Unrecognized media type " + type);
        return MediaType.MEDIA_TYPE_UNSPECIFIED;
    }

    public static ChatMessage readEncodedEntry(byte[] entry) {
        final Container container;
        try {
            container = Container.parseFrom(entry);
        } catch (InvalidProtocolBufferException e) {
            Log.w("Error reading encoded entry", e);
            return null;
        }
        if (container.hasChatMessage()) {
            return container.getChatMessage();
        } else {
            Log.i("Unknown encoded entry type");
        }
        return null;
    }

    public static ChatContainer readEncodedContainer(byte[] entry) {
        final Container container;
        try {
            container = Container.parseFrom(entry);
        } catch (InvalidProtocolBufferException e) {
            Log.w("Error reading encoded entry", e);
            return null;
        }
        if (container.hasChatContainer()) {
            return container.getChatContainer();
        } else {
            Log.i("Unknown encoded entry type");
        }
        return null;
    }

    // TODO: (clarkc) remove when non container clients expire
    public static ChatMessage messageToChatMessage(@NonNull Message message) {
        if (message.type != Message.TYPE_CHAT) {
            return null;
        }
        ChatMessage.Builder chatMessageBuilder = ChatMessage.newBuilder();
        for (Media media : message.media) {
            com.halloapp.proto.clients.Media.Builder mediaBuilder = com.halloapp.proto.clients.Media.newBuilder();
            mediaBuilder.setType(getProtoMediaType(media.type));
            mediaBuilder.setWidth(media.width);
            mediaBuilder.setHeight(media.height);
            mediaBuilder.setEncryptionKey(ByteString.copyFrom(media.encKey));
            mediaBuilder.setCiphertextHash(ByteString.copyFrom(media.encSha256hash));
            mediaBuilder.setDownloadUrl(media.url);
            chatMessageBuilder.addMedia(mediaBuilder.build());
        }
        for (Mention mention : message.mentions) {
            chatMessageBuilder.addMentions(Mention.toProto(mention));
        }
        if (message.text != null) {
            chatMessageBuilder.setText(message.text);
        }
        if (message.replyPostId != null) {
            chatMessageBuilder.setFeedPostId(message.replyPostId);
            chatMessageBuilder.setFeedPostMediaIndex(message.replyPostMediaIndex);
        }
        if (message.replyMessageId != null) {
            chatMessageBuilder.setChatReplyMessageId(message.replyMessageId);
            chatMessageBuilder.setChatReplyMessageMediaIndex(message.replyMessageMediaIndex);
            chatMessageBuilder.setChatReplyMessageSenderId(message.replyMessageSenderId.isMe() ? Me.getInstance().getUser() : message.replyMessageSenderId.rawId());
        }
        return chatMessageBuilder.build();
    }

    public static ChatContainer messageToChatContainer(@NonNull Message message) {
        ChatContainer.Builder chatContainerBuilder = ChatContainer.newBuilder();
        Text.Builder textBuilder = Text.newBuilder();
        if (message.text != null) {
            textBuilder.setText(message.text);
        }
        for (Mention mention : message.mentions) {
            textBuilder.addMentions(Mention.toProto(mention));
        }

        if (message.type == Message.TYPE_VOICE_NOTE) {
            VoiceNote.Builder voiceNoteBuilder = VoiceNote.newBuilder();
            if (!message.media.isEmpty()) {
                Media media = message.media.get(0);
                if (media.type == Media.MEDIA_TYPE_AUDIO) {
                    EncryptedResource resource = EncryptedResource.newBuilder()
                            .setDownloadUrl(media.url)
                            .setCiphertextHash(ByteString.copyFrom(media.encSha256hash))
                            .setEncryptionKey(ByteString.copyFrom(media.encKey)).build();
                    voiceNoteBuilder.setAudio(resource);
                }
            }
            chatContainerBuilder.setVoiceNote(voiceNoteBuilder);
        } else if (!message.media.isEmpty()) {
            Album.Builder albumBuilder = Album.newBuilder();
            for (Media media : message.media) {
                EncryptedResource resource = EncryptedResource.newBuilder()
                        .setDownloadUrl(media.url)
                        .setCiphertextHash(ByteString.copyFrom(media.encSha256hash))
                        .setEncryptionKey(ByteString.copyFrom(media.encKey)).build();
                AlbumMedia.Builder mediaBuilder = AlbumMedia.newBuilder();
                if (media.type == Media.MEDIA_TYPE_IMAGE) {
                    mediaBuilder.setImage(Image.newBuilder()
                            .setImg(resource)
                            .setWidth(media.width)
                            .setHeight(media.height));
                } else if (media.type == Media.MEDIA_TYPE_VIDEO) {
                    mediaBuilder.setVideo(Video.newBuilder()
                            .setVideo(resource)
                            .setHeight(media.height)
                            .setWidth(media.width));
                }
                albumBuilder.addMedia(mediaBuilder.build());
            }
            albumBuilder.setText(textBuilder);
            chatContainerBuilder.setAlbum(albumBuilder);
        } else {
            chatContainerBuilder.setText(textBuilder);
        }
        ChatContext.Builder context = ChatContext.newBuilder();
        if (message.replyPostId != null) {
            context.setFeedPostId(message.replyPostId);
            context.setFeedPostMediaIndex(message.replyPostMediaIndex);
        }
        if (message.replyMessageId != null) {
            context.setChatReplyMessageId(message.replyMessageId);
            context.setChatReplyMessageMediaIndex(message.replyMessageMediaIndex);
            context.setChatReplyMessageSenderId(message.replyMessageSenderId.isMe() ? Me.getInstance().getUser() : message.replyMessageSenderId.rawId());
        }
        chatContainerBuilder.setContext(context);
        return chatContainerBuilder.build();
    }
}
