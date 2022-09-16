package com.halloapp.xmpp.feed;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Me;
import com.halloapp.UrlPreview;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.FutureProofComment;
import com.halloapp.content.FutureProofPost;
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
import com.halloapp.proto.clients.Moment;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.clients.Reaction;
import com.halloapp.proto.clients.Text;
import com.halloapp.proto.clients.Video;
import com.halloapp.proto.clients.VoiceNote;
import com.halloapp.util.logs.Log;

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
                    Log.w("Interpreted reaction as post reaction, which is not yet supported");
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
                np.media.add(Media.parseFromProto(moment.getImage()));
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
