package com.halloapp.xmpp.feed;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;
import com.halloapp.content.Comment;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Post;
import com.halloapp.content.ReactionComment;
import com.halloapp.proto.clients.Album;
import com.halloapp.proto.clients.AlbumMedia;
import com.halloapp.proto.clients.CommentContainer;
import com.halloapp.proto.clients.CommentContext;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.EncryptedResource;
import com.halloapp.proto.clients.Image;
import com.halloapp.proto.clients.Moment;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.clients.Reaction;
import com.halloapp.proto.clients.StreamingInfo;
import com.halloapp.proto.clients.Text;
import com.halloapp.proto.clients.Video;
import com.halloapp.proto.clients.VoiceNote;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.MessageElementHelper;

import java.util.ArrayList;
import java.util.List;

public class FeedContentEncoder {

    public static void encodeComment(Container.Builder containerBuilder, @NonNull Comment comment) {
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
            if (comment.urlPreview != null) {
                textBuilder.setLink(comment.urlPreview.toProto());
            }
            textContainer = textBuilder.build();
        }

        if (comment instanceof ReactionComment) {
            ReactionComment reactionComment = (ReactionComment) comment;
            builder.setReaction(Reaction.newBuilder().setEmoji(reactionComment.reaction.reactionType));
        } else if (comment.type == Comment.TYPE_VOICE_NOTE) {
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
    }

    public static byte[] encodeComment(@NonNull Comment comment) {
        Container.Builder containerBuilder = Container.newBuilder();
        encodeComment(containerBuilder, comment);
        return containerBuilder.build().toByteArray();
    }

    private static void encodeMoment(PostContainer.Builder containerBuilder, @NonNull Post post) {
        Moment.Builder builder = Moment.newBuilder();
        List<AlbumMedia> albumMedia = getAlbumMediaProtos(post.media);
        for (AlbumMedia m : albumMedia) {
            if (m.hasImage()) {
                builder.setImage(m.getImage());
                containerBuilder.setMoment(builder.build());
                return;
            }
        }
    }

    public static void encodePost(Container.Builder containerBuilder, @NonNull Post post) {
        PostContainer.Builder builder = PostContainer.newBuilder();
        if (post.commentKey != null) {
            builder.setCommentKey(ByteString.copyFrom(post.commentKey));
        }
        if (post.type == Post.TYPE_MOMENT || post.type == Post.TYPE_MOMENT_PSA) {
            encodeMoment(builder, post);
            containerBuilder.setPostContainer(builder);
            return;
        }
        Text textContainer = null;
        if (post.text != null) {
            Text.Builder textBuilder = Text.newBuilder();
            textBuilder.setText(post.text);
            if (!post.mentions.isEmpty()) {
                List<com.halloapp.proto.clients.Mention> mentionsList = new ArrayList<>();
                for (Mention mention : post.mentions) {
                    mentionsList.add(Mention.toProto(mention));
                }
                textBuilder.addAllMentions(mentionsList);
            }
            if (post.urlPreview != null) {
                textBuilder.setLink(post.urlPreview.toProto());
            }
            textContainer = textBuilder.build();
        }

        if (!post.media.isEmpty()) {
            if (post.type == Post.TYPE_VOICE_NOTE) {
                VoiceNote.Builder voiceNoteBuilder = VoiceNote.newBuilder();
                if (!post.media.isEmpty()) {
                    Media media = post.media.get(0);
                    if (media.type == Media.MEDIA_TYPE_AUDIO) {
                        EncryptedResource resource = EncryptedResource.newBuilder()
                                .setDownloadUrl(media.url)
                                .setCiphertextHash(ByteString.copyFrom(media.encSha256hash))
                                .setEncryptionKey(ByteString.copyFrom(media.encKey)).build();
                        voiceNoteBuilder.setAudio(resource);
                    }
                    if (post.media.size() > 1) {
                        Album.Builder albumBuilder = Album.newBuilder();
                        albumBuilder.addAllMedia(getAlbumMediaProtos(post.media.subList(1, post.media.size())));
                        albumBuilder.setVoiceNote(voiceNoteBuilder);
                        builder.setAlbum(albumBuilder);
                    } else {
                        builder.setVoiceNote(voiceNoteBuilder);
                    }
                }
            } else {
                Album.Builder albumBuilder = Album.newBuilder();
                albumBuilder.addAllMedia(getAlbumMediaProtos(post.media));
                if (textContainer != null) {
                    albumBuilder.setText(textContainer);
                }
                builder.setAlbum(albumBuilder);
            }
        } else if (textContainer != null) {
            builder.setText(textContainer);
        } else {
            Log.e("FeedContentEncoder/encodePost no content aborting");
            return;
        }
        containerBuilder.setPostContainer(builder);
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
                StreamingInfo streamingInfo = StreamingInfo.newBuilder()
                        .setBlobVersion(MessageElementHelper.getProtoBlobVersion(item.blobVersion))
                        .setChunkSize(item.chunkSize)
                        .setBlobSize(item.blobSize)
                        .build();
                albumMediaBuilder.setVideo(Video.newBuilder()
                        .setWidth(item.width)
                        .setHeight(item.height)
                        .setVideo(encryptedResource)
                        .setStreamingInfo(streamingInfo).build());
            } else {
                continue;
            }
            mediaList.add(albumMediaBuilder.build());
        }
        return mediaList;
    }

}
