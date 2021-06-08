package com.halloapp.xmpp.feed;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.halloapp.content.Comment;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.proto.clients.Album;
import com.halloapp.proto.clients.AlbumMedia;
import com.halloapp.proto.clients.CommentContainer;
import com.halloapp.proto.clients.CommentContext;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.EncryptedResource;
import com.halloapp.proto.clients.Image;
import com.halloapp.proto.clients.Text;
import com.halloapp.proto.clients.Video;
import com.halloapp.proto.clients.VoiceNote;
import com.halloapp.util.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class FeedContentEncoder {

    public static byte[] encodeComment(@NonNull Comment comment) {
        Container.Builder containerBuilder = Container.newBuilder();

        CommentContainer.Builder builder = CommentContainer.newBuilder();
        CommentContext.Builder context = CommentContext.newBuilder()
                .setFeedPostId(comment.postId);
        if (comment.parentCommentId != null) {
            context.setParentCommentId(comment.parentCommentId);
        }
        Text textContainer = null;
        if (comment.text != null) {
            Text.Builder textBuilder = Text.newBuilder();
            textBuilder.setText(comment.text);
            if (!comment.mentions.isEmpty()) {
                List<com.halloapp.proto.clients.Mention> mentionsList = new ArrayList<>();
                for (Mention mention : comment.mentions) {
                    mentionsList.add(Mention.toProto(mention));
                }
                textBuilder.addAllMentions(mentionsList);
            }
            textContainer = textBuilder.build();
        }

        if (comment.type == Comment.TYPE_VOICE_NOTE) {
            VoiceNote.Builder voiceNoteBuilder = VoiceNote.newBuilder();
            if (!comment.media.isEmpty()) {
                Media media = comment.media.get(0);
                if (media.type == Media.MEDIA_TYPE_AUDIO) {
                    EncryptedResource resource = EncryptedResource.newBuilder()
                            .setDownloadUrl(media.url)
                            .setCiphertextHash(ByteString.copyFrom(media.encSha256hash))
                            .setEncryptionKey(ByteString.copyFrom(media.encKey)).build();
                    voiceNoteBuilder.setAudio(resource);
                }
            }
            builder.setVoiceNote(voiceNoteBuilder);
        } else if (!comment.media.isEmpty()) {
            Album.Builder albumBuilder = Album.newBuilder();
            albumBuilder.addMedia(getAlbumMediaProtos(comment.media).get(0));
            if (textContainer != null) {
                albumBuilder.setText(textContainer);
            }
            builder.setAlbum(albumBuilder);
        } else if (textContainer != null) {
            builder.setText(textContainer);
        }
        builder.setContext(context.build());
        containerBuilder.setCommentContainer(builder.build());

        return containerBuilder.build().toByteArray();
    }

    private static List<AlbumMedia> getAlbumMediaProtos(List<Media> media) {
        Preconditions.checkState(!media.isEmpty(), "Trying to get empty media proto");

        List<AlbumMedia> mediaList = new ArrayList<>();
        for (Media item : media) {
            EncryptedResource encryptedResource = EncryptedResource.newBuilder()
                    .setEncryptionKey(ByteString.copyFrom(item.encKey))
                    .setCiphertextHash(ByteString.copyFrom(item.encSha256hash))
                    .setDownloadUrl(item.url).build();

            AlbumMedia.Builder albumMediaBuilder = AlbumMedia.newBuilder();
            if (item.type == Media.MEDIA_TYPE_IMAGE) {
                albumMediaBuilder.setImage(Image.newBuilder()
                        .setWidth(item.width)
                        .setHeight(item.height)
                        .setImg(encryptedResource).build());
            } else if (item.type == Media.MEDIA_TYPE_VIDEO) {
                albumMediaBuilder.setVideo(Video.newBuilder()
                        .setWidth(item.width)
                        .setHeight(item.height)
                        .setVideo(encryptedResource).build());
            } else {
                continue;
            }
            mediaList.add(albumMediaBuilder.build());
        }
        return mediaList;
    }
}
