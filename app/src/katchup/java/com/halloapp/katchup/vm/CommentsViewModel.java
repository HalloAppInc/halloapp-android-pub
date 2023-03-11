package com.halloapp.katchup.vm;


import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.FileStore;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.KatchupStickerComment;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.katchup.Analytics;
import com.halloapp.katchup.KatchupCommentDataSource;
import com.halloapp.katchup.Notifications;
import com.halloapp.katchup.PublicContentCache;
import com.halloapp.katchup.ShareIntentHelper;
import com.halloapp.katchup.media.MediaTranscoderTask;
import com.halloapp.katchup.media.PrepareVideoReactionTask;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CommentsViewModel extends AndroidViewModel {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();
    private final PublicContentCache publicContentCache = PublicContentCache.getInstance();

    private ComputableLiveData<Post> postLiveData;

    private final boolean isPublic;
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

    public CommentsViewModel(@NonNull Application application, String postId, boolean isPublic) {
        super(application);
        this.postId = postId;
        this.isPublic = isPublic;
        postLiveData = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                Post post = isPublic ? PublicContentCache.getInstance().getPost(postId) : contentDb.getPost(postId);
                if (post != null && !post.senderUserId.isMe()) {
                    contentDb.setIncomingPostSeen(post.senderUserId, post.id, null);
                }
                return post;
            }
        };
        contentDb.addObserver(contentObserver);
        bgWorkers.execute(() -> {
            final List<String> unseenCommentIds = contentDb.getUnseenCommentIds(this.postId);
            contentDb.setCommentsSeen(postId);
            Notifications.getInstance(getApplication()).clearReactionNotifications(unseenCommentIds);
        });
    }

    protected LiveData<PagedList<Comment>> createCommentsList() {
        dataSourceFactory = new KatchupCommentDataSource.Factory(isPublic, contentDb, contactsDb, publicContentCache, postId);

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

    public LiveData<Boolean> onVideoReaction(File file, boolean canceled) {
        MutableLiveData<Boolean> sendSuccess = new MutableLiveData<>();
        if (canceled) {
            bgWorkers.execute(() -> file.delete());
            sendSuccess.postValue(false);
        } else {
            final File targetFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_VIDEO));
            MediaTranscoderTask transcoderTask = new MediaTranscoderTask(new PrepareVideoReactionTask(file.getAbsolutePath(), targetFile.getAbsolutePath()));
            transcoderTask.setListener(new MediaTranscoderTask.DefaultListener() {

                @Override
                public void onSuccess() {
                    bgWorkers.execute(() -> {
                        Analytics.getInstance().commented(getPost().getValue(), "reaction");
                    });
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
                    if (isPublic) {
                        publicContentCache.addComment(postId, comment);
                        invalidateLatestDataSource();
                    }
                    file.delete();
                    sendSuccess.postValue(true);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("CommentsViewModel/onVideoReaction failed to transcode", e);
                    file.delete();
                    targetFile.delete();
                    sendSuccess.postValue(false);
                }

                @Override
                public void onCanceled() {
                    Log.i("CommentsViewModel/onVideoReaction transcode canceled");
                    file.delete();
                    targetFile.delete();
                    sendSuccess.postValue(false);
                }
            });
           transcoderTask.start();
        }
        return sendSuccess;
    }

    public void sendComment(String text) {
        bgWorkers.execute(() -> {
            Analytics.getInstance().commented(getPost().getValue(), "text");
        });
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
        if (isPublic) {
            publicContentCache.addComment(postId, comment);
            invalidateLatestDataSource();
        }
    }

    public void sendTextSticker(String text, @ColorInt int color) {
        bgWorkers.execute(() -> {
            Analytics.getInstance().commented(getPost().getValue(), "sticker");
        });
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
        if (isPublic) {
            publicContentCache.addComment(postId, comment);
            invalidateLatestDataSource();
        }
    }

    public void retractComment(@NonNull Comment comment, @NonNull ProgressDialog progressDialog) {
        bgWorkers.execute(() -> {
            contentDb.retractComment(comment);
            publicContentCache.removeComment(comment.postId, comment);
            invalidateLatestDataSource();
            progressDialog.cancel();
        });
    }

    public LiveData<Boolean> saveToGallery(@NonNull Context context) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        bgWorkers.execute(() -> {
            Post post = contentDb.getPost(postId);
            if (post == null) {
                Log.e("CommentsViewModel/saveToGallery missing post " + postId);
                result.postValue(false);
                return;
            }

            try {
                ShareIntentHelper.prepareExternalShareVideo(post, true, input -> {
                    if (input == null) {
                        Log.e("CommentsViewModel/saveToGallery failed to get transcoded file");
                        result.postValue(false);
                    } else {
                        MediaUtils.saveMediaToGallery(context, input, Media.MEDIA_TYPE_VIDEO);
                        result.postValue(true);
                    }
                    return null;
                });
            } catch (IOException e) {
                Log.e("CommentsViewModel/saveToGallery failed", e);
                result.postValue(false);
            }
        });

        return result;
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

        private Application application;
        private final String postId;
        private final boolean isPublic;

        public CommentsViewModelFactory(@NonNull Application application, String postId, boolean isPublic) {
            this.application = application;
            this.postId = postId;
            this.isPublic = isPublic;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CommentsViewModel.class)) {
                //noinspection unchecked
                return (T) new CommentsViewModel(application, postId, isPublic);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
