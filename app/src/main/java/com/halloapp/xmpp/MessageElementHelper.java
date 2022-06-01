package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Me;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.proto.clients.Album;
import com.halloapp.proto.clients.AlbumMedia;
import com.halloapp.proto.clients.BlobVersion;
import com.halloapp.proto.clients.ChatContainer;
import com.halloapp.proto.clients.ChatContext;
import com.halloapp.proto.clients.ChatMessage;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.EncryptedResource;
import com.halloapp.proto.clients.File;
import com.halloapp.proto.clients.Files;
import com.halloapp.proto.clients.Image;
import com.halloapp.proto.clients.MediaType;
import com.halloapp.proto.clients.StreamingInfo;
import com.halloapp.proto.clients.Text;
import com.halloapp.proto.clients.Video;
import com.halloapp.proto.clients.VoiceNote;
import com.halloapp.util.logs.Log;

public class MessageElementHelper {

    public static @Media.BlobVersion int fromProtoBlobVersion(@NonNull BlobVersion blobVersion) {
        switch (blobVersion) {
            case BLOB_VERSION_DEFAULT:
                return Media.BLOB_VERSION_DEFAULT;

            case BLOB_VERSION_CHUNKED:
                return Media.BLOB_VERSION_CHUNKED;
        }
        Log.w("Unrecognized BlobVersion " + blobVersion);
        return Media.BLOB_VERSION_UNKNOWN;
    }

    public static BlobVersion getProtoBlobVersion(@Media.BlobVersion int blobVersion) {
        if (blobVersion == Media.BLOB_VERSION_CHUNKED) {
            return BlobVersion.BLOB_VERSION_CHUNKED;
        }
        return BlobVersion.BLOB_VERSION_DEFAULT;
    }

    public static ChatContainer messageToChatContainer(@NonNull Message message) {
        ChatContainer.Builder chatContainerBuilder = ChatContainer.newBuilder();
        Text.Builder textBuilder = Text.newBuilder();
        if (message.text != null) {
            textBuilder.setText(message.text);
            if (message.urlPreview != null) {
                textBuilder.setLink(message.urlPreview.toProto());
            }
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
        } else if (message.type == Message.TYPE_DOCUMENT) {
            File.Builder docBuilder = File.newBuilder();
            docBuilder.setFilename(message.text);
            if (!message.media.isEmpty()) {
                Media media = message.media.get(0);
                if (media.type == Media.MEDIA_TYPE_DOCUMENT) {
                    EncryptedResource resource = EncryptedResource.newBuilder()
                            .setDownloadUrl(media.url)
                            .setCiphertextHash(ByteString.copyFrom(media.encSha256hash))
                            .setEncryptionKey(ByteString.copyFrom(media.encKey)).build();
                    docBuilder.setData(resource);
                }
            }
            chatContainerBuilder.setFiles(Files.newBuilder().addFiles(docBuilder.build()));
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
                    StreamingInfo streamingInfo = StreamingInfo.newBuilder()
                            .setBlobVersion(MessageElementHelper.getProtoBlobVersion(media.blobVersion))
                            .setChunkSize(media.chunkSize)
                            .setBlobSize(media.blobSize)
                            .build();
                    mediaBuilder.setVideo(Video.newBuilder()
                            .setVideo(resource)
                            .setHeight(media.height)
                            .setWidth(media.width)
                            .setStreamingInfo(streamingInfo));
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
