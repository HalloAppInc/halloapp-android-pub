package com.halloapp.xmpp.feed;

import android.graphics.Color;
import android.text.TextUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Me;
import com.halloapp.UrlPreview;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.FutureProofComment;
import com.halloapp.content.FutureProofPost;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.KatchupStickerComment;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.content.ReactionComment;
import com.halloapp.content.VoiceNoteComment;
import com.halloapp.content.VoiceNotePost;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.Album;
import com.halloapp.proto.clients.AlbumMedia;
import com.halloapp.proto.clients.CommentContainer;
import com.halloapp.proto.clients.CommentContext;
import com.halloapp.proto.clients.Image;
import com.halloapp.proto.clients.KMomentContainer;
import com.halloapp.proto.clients.Moment;
import com.halloapp.proto.clients.PositionInfo;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.clients.Reaction;
import com.halloapp.proto.clients.Sticker;
import com.halloapp.proto.clients.Text;
import com.halloapp.proto.clients.Video;
import com.halloapp.proto.clients.VideoReaction;
import com.halloapp.proto.clients.VoiceNote;

public class FeedContentParser {

    private final Me me;

    public FeedContentParser(@NonNull Me me) {
        this.me = me;
    }

    private void processMention(@NonNull Mention mention) {
        if (mention.userId != null) {
            if (isMe(mention.userId.rawId())) {
                mention.userId = UserId.ME;
            }
        }
    }

    private boolean isMe(@NonNull String user) {
        return user.equals(me.getUser());
    }

    public Comment parseComment(@NonNull String id, @Nullable String parentCommentId, @NonNull UserId publisherId, long timestamp, @NonNull CommentContainer commentContainer, boolean decryptFailed) {
        CommentContext context = commentContainer.getContext();

        switch (commentContainer.getCommentCase()) {
            case VIDEO_REACTION: {
                VideoReaction videoReaction = commentContainer.getVideoReaction();
                final Comment comment = new Comment(0,
                        context.getFeedPostId(),
                        publisherId,
                        id,
                        context.getParentCommentId(),
                        timestamp,
                        decryptFailed ? Comment.TRANSFERRED_DECRYPT_FAILED : !videoReaction.hasVideo() || publisherId.isMe() ? Comment.TRANSFERRED_YES : Comment.TRANSFERRED_NO,
                        false,
                        null
                );
                comment.type = Comment.TYPE_VIDEO_REACTION;
                Video video = videoReaction.getVideo();
                comment.media.add(Media.parseFromProto(video));

                return comment;
            }
            case STICKER: {
                Sticker sticker = commentContainer.getSticker();
                @ColorInt int color;
                try {
                    color = Color.parseColor(sticker.getColor());
                } catch (IllegalArgumentException e) {
                    color = 0x9BDA91; // TODO(jack): Deal with this class in main depending on Colors.java from katchup (Colors.getDefaultStickerColor())
                }
                final Comment comment = new KatchupStickerComment(0,
                        context.getFeedPostId(),
                        publisherId,
                        id,
                        context.getParentCommentId(),
                        timestamp,
                        decryptFailed ? Comment.TRANSFERRED_DECRYPT_FAILED : Comment.TRANSFERRED_YES,
                        false,
                        sticker.getText(), color
                );
                return comment;
            }
            case ALBUM: {
                Album album = commentContainer.getAlbum();
                Text caption = album.getText();
                final Comment comment = new Comment(0,
                        context.getFeedPostId(),
                        publisherId,
                        id,
                        context.getParentCommentId(),
                        timestamp,
                        decryptFailed ? Comment.TRANSFERRED_DECRYPT_FAILED : album.getMediaList().isEmpty() || publisherId.isMe() ? Comment.TRANSFERRED_YES : Comment.TRANSFERRED_NO,
                        false,
                        caption.getText()
                );
                for (AlbumMedia albumMedia : album.getMediaList()) {
                    switch (albumMedia.getMediaCase()) {
                        case IMAGE: {
                            Image image = albumMedia.getImage();
                            comment.media.add(Media.parseFromProto(image));
                            break;
                        }
                        case VIDEO: {
                            Video video = albumMedia.getVideo();
                            comment.media.add(Media.parseFromProto(video));
                            break;
                        }
                    }
                }
                for (com.halloapp.proto.clients.Mention mentionProto : caption.getMentionsList()) {
                    Mention mention = Mention.parseFromProto(mentionProto);
                    processMention(mention);
                    comment.mentions.add(mention);
                }
                if (caption.hasLink()) {
                    comment.urlPreview = UrlPreview.fromProto(caption.getLink());
                }

                return comment;
            }
            case TEXT: {
                Text text = commentContainer.getText();
                final Comment comment = new Comment(0,
                        context.getFeedPostId(),
                        publisherId,
                        id,
                        context.getParentCommentId(),
                        timestamp,
                        decryptFailed ? Comment.TRANSFERRED_DECRYPT_FAILED : Comment.TRANSFERRED_YES,
                        false,
                        text.getText()
                );
                for (com.halloapp.proto.clients.Mention mentionProto : text.getMentionsList()) {
                    Mention mention = Mention.parseFromProto(mentionProto);
                    processMention(mention);
                    comment.mentions.add(mention);
                }
                if (text.hasLink()) {
                    comment.urlPreview = UrlPreview.fromProto(text.getLink());
                }
                return comment;
            }
            case VOICE_NOTE: {
                VoiceNote voiceNote = commentContainer.getVoiceNote();
                final Comment comment = new VoiceNoteComment(0,
                        context.getFeedPostId(),
                        publisherId,
                        id,
                        context.getParentCommentId(),
                        timestamp,
                        decryptFailed ? Comment.TRANSFERRED_DECRYPT_FAILED : publisherId.isMe() ? Comment.TRANSFERRED_YES : Comment.TRANSFERRED_NO,
                        false,
                        null
                );
                comment.media.add(Media.parseFromProto(voiceNote));
                return comment;
            }
            case REACTION: {
                Reaction reaction = commentContainer.getReaction();
                boolean isPostReaction = TextUtils.isEmpty(context.getParentCommentId());
                if (!isPostReaction) {
                    final ReactionComment reactionComment = new ReactionComment(
                            new com.halloapp.content.Reaction(id, context.getParentCommentId(), publisherId, reaction.getEmoji(), timestamp),
                            0,
                            context.getFeedPostId(),
                            publisherId,
                            id,
                            context.getParentCommentId(),
                            timestamp,
                            decryptFailed ? Comment.TRANSFERRED_DECRYPT_FAILED : publisherId.isMe() ? Comment.TRANSFERRED_YES : Comment.TRANSFERRED_NO,
                            false,
                            null
                    );
                    Post parentPost = ContentDb.getInstance().getPost(reactionComment.postId);
                    reactionComment.setParentPost(parentPost);
                    return reactionComment;
                } else {
                    final ReactionComment reactionComment = new ReactionComment(
                            new com.halloapp.content.Reaction(id, context.getFeedPostId(), publisherId, reaction.getEmoji(), timestamp),
                            0,
                            context.getFeedPostId(),
                            publisherId,
                            id,
                            null,
                            timestamp,
                            decryptFailed ? Comment.TRANSFERRED_DECRYPT_FAILED : publisherId.isMe() ? Comment.TRANSFERRED_YES : Comment.TRANSFERRED_NO,
                            false,
                            null
                    );
                    Post parentPost = ContentDb.getInstance().getPost(reactionComment.postId);
                    reactionComment.setParentPost(parentPost);
                    return reactionComment;
                }
            }
            default:
            case COMMENT_NOT_SET: {
                // Future proof
                final FutureProofComment comment = new FutureProofComment(0,
                        context.getFeedPostId(),
                        publisherId,
                        id,
                        context.getParentCommentId(),
                        timestamp,
                        decryptFailed ? Comment.TRANSFERRED_DECRYPT_FAILED : Comment.TRANSFERRED_YES,
                        false,
                        null
                );
                comment.setProtoBytes(commentContainer.toByteArray());
                return comment;
            }
        }
    }

    public KatchupPost parseKatchupPost(@NonNull String id, @NonNull UserId posterUserId, long timestamp, @NonNull KMomentContainer katchupContainer, boolean decryptFailure) {
        KatchupPost kp = new KatchupPost(-1, posterUserId, id, timestamp, decryptFailure ? Post.TRANSFERRED_DECRYPT_FAILED : posterUserId.isMe() ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO, Post.SEEN_NO, "");
        kp.media.add(Media.parseFromProto(katchupContainer.getLiveSelfie()));
        if (katchupContainer.hasImage()) {
            kp.media.add(Media.parseFromProto(katchupContainer.getImage()));
        } else {
            kp.media.add(Media.parseFromProto(katchupContainer.getVideo()));
        }
        if (katchupContainer.hasSelfiePositionInfo()) {
            PositionInfo positionInfo = katchupContainer.getSelfiePositionInfo();
            kp.selfieX = (float) positionInfo.getX();
            kp.selfieY = (float) positionInfo.getY();
        }
        if (!TextUtils.isEmpty(katchupContainer.getLocation())) {
            kp.location = katchupContainer.getLocation();
        }
        kp.seen = ContentDb.getInstance().isPostSeen(id) ? Post.SEEN_YES : Post.SEEN_NO;

        return kp;
    }

    public Post parsePost(@NonNull String id, @NonNull UserId posterUserId, long timestamp, @NonNull PostContainer postContainer, boolean decryptFailure) {
        switch (postContainer.getPostCase()) {
            default:
            case POST_NOT_SET: {
                @Post.TransferredState int transferState = decryptFailure ? Post.TRANSFERRED_DECRYPT_FAILED : Post.TRANSFERRED_YES;
                FutureProofPost futureproofPost = new FutureProofPost(-1, posterUserId, id, timestamp, transferState, Post.SEEN_NO, null);
                futureproofPost.setProtoBytes(postContainer.toByteArray());

                return futureproofPost;
            }
            case TEXT: {
                Text text = postContainer.getText();
                Post np = new Post(-1, posterUserId, id, timestamp, decryptFailure ? Post.TRANSFERRED_DECRYPT_FAILED : Post.TRANSFERRED_YES, Post.SEEN_NO, text.getText());
                for (com.halloapp.proto.clients.Mention mentionProto : text.getMentionsList()) {
                    Mention mention = Mention.parseFromProto(mentionProto);
                    processMention(mention);
                    np.mentions.add(mention);
                }
                if (text.hasLink()) {
                    np.urlPreview = UrlPreview.fromProto(text.getLink());
                }
                return np;
            }
            case VOICE_NOTE: {
                VoiceNotePost np = new VoiceNotePost(-1, posterUserId, id, timestamp, decryptFailure ? Post.TRANSFERRED_DECRYPT_FAILED : posterUserId.isMe() ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO, Post.SEEN_NO);
                VoiceNote voiceNote = postContainer.getVoiceNote();
                np.media.add(Media.parseFromProto(voiceNote));
                return np;
            }
            case MOMENT: {
                Moment moment = postContainer.getMoment();
                MomentPost np = new MomentPost(-1, posterUserId, id, timestamp, decryptFailure ? Post.TRANSFERRED_DECRYPT_FAILED : posterUserId.isMe() ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO, Post.SEEN_NO, "");

                if (!TextUtils.isEmpty(moment.getLocation())) {
                    np.location = moment.getLocation();
                }

                // Prior to version 1.25.306 (12 Oct 2022) of the iOS client, when there was no selfie it was still
                // sending an image object but with empty fields, and hasSelfieImage() always returned true
                if (!moment.hasSelfieImage() || TextUtils.isEmpty(moment.getSelfieImage().getImg().getDownloadUrl())) {
                    np.media.add(Media.parseFromProto(moment.getImage()));
                    np.selfieMediaIndex = -1;
                } else if (moment.getSelfieLeading()) {
                    np.media.add(Media.parseFromProto(moment.getSelfieImage()));
                    np.media.add(Media.parseFromProto(moment.getImage()));
                    np.selfieMediaIndex = 0;
                } else {
                    np.media.add(Media.parseFromProto(moment.getImage()));
                    np.media.add(Media.parseFromProto(moment.getSelfieImage()));
                    np.selfieMediaIndex = 1;
                }

                return np;
            }
            case ALBUM: {
                Album album = postContainer.getAlbum();
                Post np;
                if (album.hasVoiceNote()) {
                    np = new VoiceNotePost(-1, posterUserId, id, timestamp, decryptFailure ? Post.TRANSFERRED_DECRYPT_FAILED : posterUserId.isMe() ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO, Post.SEEN_NO);
                    VoiceNote voiceNote = album.getVoiceNote();
                    np.media.add(Media.parseFromProto(voiceNote));
                } else {
                    Text caption = album.getText();
                    np = new Post(-1, posterUserId, id, timestamp, decryptFailure ? Post.TRANSFERRED_DECRYPT_FAILED : posterUserId.isMe() ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO, Post.SEEN_NO, caption.getText());
                    if (caption.hasLink()) {
                        np.urlPreview = UrlPreview.fromProto(caption.getLink());
                    }
                    for (com.halloapp.proto.clients.Mention mentionProto : caption.getMentionsList()) {
                        Mention mention = Mention.parseFromProto(mentionProto);
                        processMention(mention);
                        np.mentions.add(mention);
                    }
                }
                for (AlbumMedia albumMedia : album.getMediaList()) {
                    switch (albumMedia.getMediaCase()) {
                        case IMAGE: {
                            Image image = albumMedia.getImage();
                            np.media.add(Media.parseFromProto(image));
                            break;
                        }
                        case VIDEO: {
                            Video video = albumMedia.getVideo();
                            np.media.add(Media.parseFromProto(video));
                            break;
                        }
                    }
                }
                return np;
            }
        }
    }
}
