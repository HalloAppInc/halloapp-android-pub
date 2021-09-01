package com.halloapp.xmpp.feed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Me;
import com.halloapp.content.Comment;
import com.halloapp.content.FutureProofComment;
import com.halloapp.content.FutureProofPost;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Post;
import com.halloapp.content.VoiceNoteComment;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.Album;
import com.halloapp.proto.clients.AlbumMedia;
import com.halloapp.proto.clients.CommentContainer;
import com.halloapp.proto.clients.CommentContext;
import com.halloapp.proto.clients.Image;
import com.halloapp.proto.clients.PostContainer;
import com.halloapp.proto.clients.Text;
import com.halloapp.proto.clients.Video;
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

    public Comment parseComment(@NonNull String id, @Nullable String parentCommentId, @NonNull UserId publisherId, long timestamp, @NonNull CommentContainer commentContainer) {
        CommentContext context = commentContainer.getContext();

        switch (commentContainer.getCommentCase()) {
            case ALBUM: {
                Album album = commentContainer.getAlbum();
                Text caption = album.getText();
                final Comment comment = new Comment(0,
                        context.getFeedPostId(),
                        publisherId,
                        id,
                        parentCommentId,
                        timestamp,
                        true,
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

                return comment;
            }
            case TEXT: {
                Text text = commentContainer.getText();
                final Comment comment = new Comment(0,
                        context.getFeedPostId(),
                        publisherId,
                        id,
                        parentCommentId,
                        timestamp,
                        true,
                        false,
                        text.getText()
                );
                for (com.halloapp.proto.clients.Mention mentionProto : text.getMentionsList()) {
                    Mention mention = Mention.parseFromProto(mentionProto);
                    processMention(mention);
                    comment.mentions.add(mention);
                }
                return comment;
            }
            case VOICE_NOTE: {
                VoiceNote voiceNote = commentContainer.getVoiceNote();
                final Comment comment = new VoiceNoteComment(0,
                        context.getFeedPostId(),
                        publisherId,
                        id,
                        parentCommentId,
                        timestamp,
                        true,
                        false,
                        null
                );
                comment.media.add(Media.parseFromProto(voiceNote));
                return comment;
            }
            default:
            case COMMENT_NOT_SET: {
                // Future proof
                final FutureProofComment comment = new FutureProofComment(0,
                        context.getFeedPostId(),
                        publisherId,
                        id,
                        parentCommentId,
                        timestamp,
                        true,
                        false,
                        null
                );
                comment.setProtoBytes(commentContainer.toByteArray());
                return comment;
            }
        }
    }

    public Post parsePost(@NonNull String id, @NonNull UserId posterUserId, long timestamp, @NonNull PostContainer postContainer) {
        switch (postContainer.getPostCase()) {
            default:
            case POST_NOT_SET: {
                @Post.TransferredState int transferState = Post.TRANSFERRED_YES;
                FutureProofPost futureproofPost = new FutureProofPost(-1, posterUserId, id, timestamp, transferState, Post.SEEN_NO, null);
                futureproofPost.setProtoBytes(postContainer.toByteArray());

                return futureproofPost;
            }
            case TEXT: {
                Text text = postContainer.getText();
                Post np = new Post(-1, posterUserId, id, timestamp, Post.TRANSFERRED_YES, Post.SEEN_NO, text.getText());
                for (com.halloapp.proto.clients.Mention mentionProto : text.getMentionsList()) {
                    Mention mention = Mention.parseFromProto(mentionProto);
                    processMention(mention);
                    np.mentions.add(mention);
                }
                return np;
            }
            case ALBUM: {
                Album album = postContainer.getAlbum();
                Text caption = album.getText();
                Post np = new Post(-1, posterUserId, id, timestamp, posterUserId.isMe() ? Post.TRANSFERRED_YES : Post.TRANSFERRED_NO, Post.SEEN_NO, caption.getText());
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
                for (com.halloapp.proto.clients.Mention mentionProto : caption.getMentionsList()) {
                    Mention mention = Mention.parseFromProto(mentionProto);
                    processMention(mention);
                    np.mentions.add(mention);
                }
                return np;
            }
        }
    }
}
