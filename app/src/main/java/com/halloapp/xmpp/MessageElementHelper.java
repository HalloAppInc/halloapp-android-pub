package com.halloapp.xmpp;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.proto.ChatMessage;
import com.halloapp.proto.Container;
import com.halloapp.proto.MediaType;
import com.halloapp.util.Log;

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

    public static ChatMessage readEncodedEntryString(String entry) {
        return readEncodedEntry(Base64.decode(entry, Base64.NO_WRAP));
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

    public static ChatMessage messageToChatMessage(@NonNull Message message) {
        ChatMessage.Builder chatMessageBuilder = ChatMessage.newBuilder();
        for (Media media : message.media) {
            com.halloapp.proto.Media.Builder mediaBuilder = com.halloapp.proto.Media.newBuilder();
            mediaBuilder.setType(getProtoMediaType(media.type));
            mediaBuilder.setWidth(media.width);
            mediaBuilder.setHeight(media.height);
            mediaBuilder.setEncryptionKey(ByteString.copyFrom(media.encKey));
            mediaBuilder.setPlaintextHash(ByteString.copyFrom(media.sha256hash));
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
        return chatMessageBuilder.build();
    }
}
