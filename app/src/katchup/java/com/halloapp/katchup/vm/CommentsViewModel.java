package com.halloapp.katchup.vm;


import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.CommentsDataSource;
import com.halloapp.content.ContentDb;
import com.halloapp.content.KatchupStickerComment;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.content.VoiceNoteComment;
import com.halloapp.id.UserId;
import com.halloapp.katchup.KatchupCommentDataSource;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;

import java.io.File;

public class CommentsViewModel extends ViewModel {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();

    private ComputableLiveData<Post> postLiveData;

    private final String postId;

    private KatchupCommentDataSource.Factory dataSourceFactory;

    private LiveData<PagedList<Comment>> commentList;

    private MutableLiveData<Comment> selectedComment = new MutableLiveData<>();
    private MutableLiveData<Media> playingVideoReaction = new MutableLiveData<>();

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            if (!CommentsViewModel.this.postId.equals(comment.postId)) {
                return;
            }
            invalidateLatestDataSource();
        }

        @Override
        public void onCommentRetracted(@NonNull Comment comment) {
            if (!CommentsViewModel.this.postId.equals(comment.postId)) {
                return;
            }
            invalidateLatestDataSource();
        }

        @Override
        public void onCommentUpdated(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
            if (!CommentsViewModel.this.postId.equals(postId)) {
                return;
            }
            invalidateLatestDataSource();
        }
    };

    public CommentsViewModel(String postId) {
        this.postId = postId;
        postLiveData = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                Post post = contentDb.getPost(postId);
                if (!post.senderUserId.isMe()) {
                    contentDb.setIncomingPostSeen(post.senderUserId, post.id, null);
                }
                return post;
            }
        };
        contentDb.addObserver(contentObserver);
    }

    protected LiveData<PagedList<Comment>> createCommentsList() {
        dataSourceFactory = new KatchupCommentDataSource.Factory(contentDb, contactsDb, postId);

        return new LivePagedListBuilder<>(dataSourceFactory, new PagedList.Config.Builder().setPageSize(50).setEnablePlaceholders(false).build()).build();
    }

    protected void invalidateLatestDataSource() {
        dataSourceFactory.invalidateLatestDataSource();
    }

    public LiveData<PagedList<Comment>> getCommentList() {
        if (commentList == null) {
            commentList = createCommentsList();
        }
        return commentList;
    }

    public void selectComment(@Nullable Comment comment) {
        selectedComment.setValue(comment);
    }

    public LiveData<Comment> getSelectedComment() {
        return selectedComment;
    }

    public LiveData<Media> getPlayingVideoReaction() {
        return playingVideoReaction;
    }

    public void setPlayingVideoReaction(@Nullable Media media) {
        playingVideoReaction.setValue(media);
    }

    public void onVideoReaction(File file, boolean canceled) {
        bgWorkers.execute(() -> {
            if (canceled) {
                file.delete();
                return;
            }
            final File targetFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_VIDEO));
            if (!file.renameTo(targetFile)) {
                Log.e("failed to rename " + file.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
                return;
            }
            final Comment comment = new Comment(
                    0,
                    postId,
                    UserId.ME,
                    RandomId.create(),
                    null,
                    System.currentTimeMillis(),
                    Comment.TRANSFERRED_NO,
                    true,
                    null);
            comment.type = Comment.TYPE_VIDEO_REACTION;
            final Media sendMedia = Media.createFromFile(Media.MEDIA_TYPE_VIDEO, targetFile);
            comment.media.add(sendMedia);
            contentDb.addComment(comment);
        });
    }

    public void sendComment(String text) {
        final Comment comment = new Comment(
                0,
                postId,
                UserId.ME,
                RandomId.create(),
                null,
                System.currentTimeMillis(),
                Comment.TRANSFERRED_NO,
                true,
                text);
        contentDb.addComment(comment);
    }

    public void sendTextSticker(String text, @ColorInt int color) {
        final Comment comment = new KatchupStickerComment(
                0,
                postId,
                UserId.ME,
                RandomId.create(),
                null,
                System.currentTimeMillis(),
                Comment.TRANSFERRED_NO,
                true,
                text,
                color);
        contentDb.addComment(comment);
    }

    public LiveData<Post> getPost() {
        return postLiveData.getLiveData();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        contentDb.removeObserver(contentObserver);
    }

    public static class CommentsViewModelFactory implements ViewModelProvider.Factory {

        private final String postId;

        public CommentsViewModelFactory(String postId) {
            this.postId = postId;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CommentsViewModel.class)) {
                //noinspection unchecked
                return (T) new CommentsViewModel(postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
