package com.halloapp.xmpp;

import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.Album;
import com.halloapp.proto.clients.AlbumMedia;
import com.halloapp.proto.clients.Comment;
import com.halloapp.proto.clients.CommentContainer;
import com.halloapp.proto.clients.CommentContext;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.EncryptedResource;
import com.halloapp.proto.clients.Image;
import com.halloapp.proto.clients.MediaType;
import com.halloapp.proto.clients.Post;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.clients.Text;
import com.halloapp.proto.clients.Video;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PublishedEntry {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ENTRY_FEED, ENTRY_COMMENT})
    @interface EntryType {}
    static final int ENTRY_FEED = 0;
    static final int ENTRY_COMMENT = 1;

    final @EntryType int type;
    final String id;
    final long timestamp;
    final String user;
    final String text;
    final String feedItemId;
    final String parentCommentId;
    final List<Media> media = new ArrayList<>();
    final List<com.halloapp.proto.clients.Mention> mentions = new ArrayList<>();

    public static class Media {

        @Retention(RetentionPolicy.SOURCE)
        @StringDef({
                MEDIA_TYPE_IMAGE,
                MEDIA_TYPE_VIDEO
        })
        @interface MediaType {}
        static final String MEDIA_TYPE_IMAGE = "image";
        static final String MEDIA_TYPE_VIDEO = "video";

        final String type;
        final String url;
        final byte [] encKey;
        final byte [] encSha256hash;
        int width;
        int height;

        public Media(@MediaType String type, String url, byte [] encKey, byte [] encSha256hash, int width, int height) {
            this.type = type;
            this.url = url;
            this.encKey = encKey;
            this.encSha256hash = encSha256hash;
            this.width = width;
            this.height = height;
        }
    }

    static @com.halloapp.content.Media.MediaType int getMediaType(@PublishedEntry.Media.MediaType String protocolMediaType) {
        switch (protocolMediaType) {
            case PublishedEntry.Media.MEDIA_TYPE_IMAGE: {
                return com.halloapp.content.Media.MEDIA_TYPE_IMAGE;
            }
            case PublishedEntry.Media.MEDIA_TYPE_VIDEO: {
                return com.halloapp.content.Media.MEDIA_TYPE_VIDEO;
            }
            default: {
                return com.halloapp.content.Media.MEDIA_TYPE_UNKNOWN;
            }
        }
    }

    static @PublishedEntry.Media.MediaType String getMediaType(@com.halloapp.content.Media.MediaType int mediaType) {
        switch (mediaType) {
            case com.halloapp.content.Media.MEDIA_TYPE_IMAGE: {
                return PublishedEntry.Media.MEDIA_TYPE_IMAGE;
            }
            case com.halloapp.content.Media.MEDIA_TYPE_VIDEO: {
                return PublishedEntry.Media.MEDIA_TYPE_VIDEO;
            }
            case com.halloapp.content.Media.MEDIA_TYPE_UNKNOWN:
            default: {
                throw new IllegalArgumentException();
            }
        }
    }

    static PublishedEntry getFeedEntry(@NonNull String payload, @NonNull String id, long timestamp, String publisherId) {
        PublishedEntry.Builder entryBuilder = readEncodedEntryString(payload);
        entryBuilder.id(id);
        entryBuilder.timestamp(timestamp);
        if (publisherId != null) {
            entryBuilder.user(publisherId);
        }
        return entryBuilder.build();
    }

    PublishedEntry(@EntryType int type, String id, long timestamp, String user, String text, String feedItemId, String parentCommentId) {
        this.type = type;
        this.id = id;
        this.timestamp = timestamp;
        this.user = user;
        if (text != null && text.length() > Constants.MAX_TEXT_LENGTH) {
            this.text = text.substring(0, Constants.MAX_TEXT_LENGTH);
        } else {
            this.text = text;
        }
        this.feedItemId = feedItemId;
        this.parentCommentId = parentCommentId;
    }

    public String getEncodedEntryString() {
        return Base64.encodeToString(getEncodedEntry(), Base64.NO_WRAP);
    }

    private byte[] getEncodedEntry() {
        Container.Builder containerBuilder = Container.newBuilder();
        switch (type) {
            case ENTRY_FEED: {
                Post.Builder postBuilder = Post.newBuilder();
                if (!media.isEmpty()) {
                    postBuilder.addAllMedia(getMediaProtos());
                }
                if (text != null) {
                    postBuilder.setText(text);
                }
                if (!mentions.isEmpty()) {
                    postBuilder.addAllMentions(mentions);
                }
                containerBuilder.setPost(postBuilder.build());
                break;
            }
            case ENTRY_COMMENT: {
                Comment.Builder commentBuilder = Comment.newBuilder();
                commentBuilder.setFeedPostId(feedItemId);
                if (!media.isEmpty()) {
                    commentBuilder.setMedia(getMediaProtos().get(0));
                }
                if (parentCommentId != null) {
                    commentBuilder.setParentCommentId(parentCommentId);
                }
                if (text != null) {
                    commentBuilder.setText(text);
                }
                if (!mentions.isEmpty()) {
                    commentBuilder.addAllMentions(mentions);
                }
                containerBuilder.setComment(commentBuilder.build());
                break;
            }
            default: {
                throw new IllegalStateException("Unknown type " + type);
            }
        }
        if (ServerProps.getInstance().getNewClientContainerEnabled()) {
            switch (type) {
                case ENTRY_FEED: {
                    PostContainer.Builder builder = PostContainer.newBuilder();
                    Text textContainer = null;
                    if (text != null) {
                        Text.Builder textBuilder = Text.newBuilder();
                        textBuilder.setText(text);
                        if (!mentions.isEmpty()) {
                            textBuilder.addAllMentions(mentions);
                        }
                        textContainer = textBuilder.build();
                    }

                    if (!media.isEmpty()) {
                        Album.Builder albumBuilder = Album.newBuilder();
                        albumBuilder.addAllMedia(getAlbumMediaProtos());
                        if (textContainer != null) {
                            albumBuilder.setText(textContainer);
                        }
                        builder.setAlbum(albumBuilder);
                    } else {
                        builder.setText(textContainer);
                    }
                    containerBuilder.setPostContainer(builder);
                    break;
                }
                case ENTRY_COMMENT: {
                    CommentContainer.Builder builder = CommentContainer.newBuilder();
                    CommentContext.Builder context = CommentContext.newBuilder()
                            .setFeedPostId(feedItemId);
                    if (parentCommentId != null) {
                        context.setParentCommentId(parentCommentId);
                    }
                    Text textContainer = null;
                    if (text != null) {
                        Text.Builder textBuilder = Text.newBuilder();
                        textBuilder.setText(text);
                        if (!mentions.isEmpty()) {
                            textBuilder.addAllMentions(mentions);
                        }
                        textContainer = textBuilder.build();
                    }

                    if (!media.isEmpty()) {
                        Album.Builder albumBuilder = Album.newBuilder();
                        albumBuilder.addMedia(getAlbumMediaProtos().get(0));
                        if (textContainer != null) {
                            albumBuilder.setText(textContainer);
                        }
                        builder.setAlbum(albumBuilder);
                    } else if (textContainer != null) {
                        builder.setText(textContainer);
                    }
                    builder.setContext(context.build());
                    containerBuilder.setCommentContainer(builder.build());
                    break;
                }
                default: {
                    throw new IllegalStateException("Unknown type " + type);
                }
            }
        }
        return containerBuilder.build().toByteArray();
    }

    private List<AlbumMedia> getAlbumMediaProtos() {
        Preconditions.checkState(!media.isEmpty(), "Trying to get empty media proto");

        List<AlbumMedia> mediaList = new ArrayList<>();
        for (Media item : media) {
            EncryptedResource encryptedResource = EncryptedResource.newBuilder()
                    .setEncryptionKey(ByteString.copyFrom(item.encKey))
                    .setCiphertextHash(ByteString.copyFrom(item.encSha256hash))
                    .setDownloadUrl(item.url).build();

            AlbumMedia.Builder albumMediaBuilder = AlbumMedia.newBuilder();
            if ("image".equals(item.type)) {
                albumMediaBuilder.setImage(Image.newBuilder()
                        .setWidth(item.width)
                        .setHeight(item.height)
                        .setImg(encryptedResource).build());

            } else if ("video".equals(item.type)) {
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

    private List<com.halloapp.proto.clients.Media> getMediaProtos() {
        Preconditions.checkState(!media.isEmpty(), "Trying to get empty media proto");

        List<com.halloapp.proto.clients.Media> mediaList = new ArrayList<>();
        for (Media item : media) {
            com.halloapp.proto.clients.Media.Builder mediaBuilder = com.halloapp.proto.clients.Media.newBuilder();
            mediaBuilder.setType(getProtoMediaType(item.type));
            mediaBuilder.setWidth(item.width);
            mediaBuilder.setHeight(item.height);
            mediaBuilder.setEncryptionKey(ByteString.copyFrom(item.encKey));
            mediaBuilder.setCiphertextHash(ByteString.copyFrom(item.encSha256hash));
            mediaBuilder.setDownloadUrl(item.url);
            mediaList.add(mediaBuilder.build());
        }
        return mediaList;
    }

    private MediaType getProtoMediaType(@NonNull String s) {
        if ("image".equals(s)) {
            return MediaType.MEDIA_TYPE_IMAGE;
        } else if ("video".equals(s)) {
            return MediaType.MEDIA_TYPE_VIDEO;
        }
        Log.w("Unrecognized media type string " + s);
        return MediaType.MEDIA_TYPE_UNSPECIFIED;
    }

    private static String fromProtoMediaType(@NonNull MediaType type) {
        if (type == MediaType.MEDIA_TYPE_IMAGE) {
            return Media.MEDIA_TYPE_IMAGE;
        } else if (type == MediaType.MEDIA_TYPE_VIDEO) {
            return Media.MEDIA_TYPE_VIDEO;
        }
        Log.w("Unrecognized MediaType " + type);
        return null;
    }

    private static List<Media> fromMediaProtos(List<com.halloapp.proto.clients.Media> mediaList) {
        List<Media> ret = new ArrayList<>();
        for (com.halloapp.proto.clients.Media item : mediaList) {
            ret.add(new Media(fromProtoMediaType(
                    item.getType()),
                    item.getDownloadUrl(),
                    item.getEncryptionKey().toByteArray(),
                    item.getCiphertextHash().toByteArray(),
                    item.getWidth(),
                    item.getHeight()));
        }
        return ret;
    }

    private static Builder readEncodedEntryString(String entry) {
        return readEncodedEntry(Base64.decode(entry, Base64.NO_WRAP));
    }

    private static Builder readEncodedEntry(byte[] entry) {
        try {
            Container container = Container.parseFrom(entry);
            Builder builder = new Builder();
            if (container.hasPost()) {
                Post post = container.getPost();
                builder.type(ENTRY_FEED);
                builder.text(post.getText());
                builder.media(fromMediaProtos(post.getMediaList()));
                builder.mentions(post.getMentionsList());
            } else if (container.hasComment()) {
                Comment comment = container.getComment();
                builder.type(ENTRY_COMMENT);
                builder.feedItemId(comment.getFeedPostId());
                builder.parentCommentId(comment.getParentCommentId());
                builder.text(comment.getText());
                builder.mentions(comment.getMentionsList());

                com.halloapp.proto.clients.Media media = comment.getMedia();
                if (!"".equals(media.getDownloadUrl())) {
                    builder.media(fromMediaProtos(Collections.singletonList(media)));
                }
            } else {
                Log.i("Unknown encoded entry type");
            }

            return builder;
        } catch (InvalidProtocolBufferException e) {
            Log.w("Error reading encoded entry", e);
        }
        return null;
    }

    @Override
    public @NonNull String toString() {
        return "PublishedEntry[id=" + id + " type=" + type + " text=" + text + "]";
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        @EntryType int type;
        String id;
        long timestamp;
        String user;
        String text;
        String feedItemId;
        String parentCommentId;
        List<Media> media;
        List<com.halloapp.proto.clients.Mention> mentions;

        Builder type(@EntryType int type) {
            this.type = type;
            return this;
        }

        Builder id(String id) {
            this.id = id;
            return this;
        }

        Builder user(String user) {
            this.user = user;
            return this;
        }

        Builder text(String text) {
            this.text = text;
            return this;
        }

        Builder timestamp(long timestampSeconds) {
            timestamp = 1000L * timestampSeconds;
            return this;
        }

        Builder feedItemId(String feedItemId) {
            this.feedItemId = feedItemId;
            return this;
        }

        Builder parentCommentId(String parentCommentId) {
            this.parentCommentId = TextUtils.isEmpty(parentCommentId) ? null : parentCommentId;
            return this;
        }

        Builder mentions(List<com.halloapp.proto.clients.Mention> mentions) {
            this.mentions = mentions;
            return this;
        }

        Builder media(List<Media> media) {
            this.media = media;
            return this;
        }
        PublishedEntry build() {
            final PublishedEntry entry = new PublishedEntry(type, id, timestamp, user, text, feedItemId, parentCommentId);
            if (media != null) {
                entry.media.addAll(media);
            }
            if (mentions != null) {
                entry.mentions.addAll(mentions);
            }
            return entry;
        }
    }
}
