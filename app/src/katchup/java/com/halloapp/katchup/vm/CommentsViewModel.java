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
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.KatchupStickerComment;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.katchup.Analytics;
import com.halloapp.katchup.KatchupCommentDataSource;
import com.halloapp.Notifications;
import com.halloapp.katchup.PublicContentCache;
import com.halloapp.katchup.ShareIntentHelper;
import com.halloapp.katchup.media.MediaTranscoderTask;
import com.halloapp.katchup.media.PrepareVideoReactionTask;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CommentsViewModel extends AndroidViewModel {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();
    private final PublicContentCache publicContentCache = PublicContentCache.getInstance();

    private MutableLiveData<Post> postLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> followable = new MutableLiveData<>();

    private final boolean isPublic;
    private final boolean fromStack;
    private final String originalPostId;

    private List<Post> posts;
    private int currentIndex;

    private KatchupCommentDataSource.Factory dataSourceFactory;

    private LiveData<PagedList<Comment>> commentList;

    private MutableLiveData<Comment> selectedComment = new MutableLiveData<>();
    private MutableLiveData<Media> playingVideoReaction = new MutableLiveData<>();

    private final PublicContentCache.Observer cacheObserver = new PublicContentCache.DefaultObserver() {

        @Override
        public void onCommentsAdded(@NonNull List<Comment> comments) {
            for (Comment comment : comments) {
                if (getCurrentPostId().equals(comment.postId)) {
                    invalidateLatestDataSource();
                }
            }
        }

        @Override
        public void onCommentRetracted(@NonNull Comment comment) {
            if (getCurrentPostId().equals(comment.postId)) {
                invalidateLatestDataSource();
            }
        }
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            if (getCurrentPostId().equals(comment.postId)) {
                invalidateLatestDataSource();
            }
        }

        @Override
        public void onCommentRetracted(@NonNull Comment comment) {
            if (getCurrentPostId().equals(comment.postId)) {
                invalidateLatestDataSource();
            }
        }

        @Override
        public void onCommentUpdated(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
            if (getCurrentPostId().equals(postId)) {
                invalidateLatestDataSource();
            }
        }
    };

    public CommentsViewModel(@NonNull Application application, String postId, boolean isPublic, boolean fromStack) {
        super(application);
        this.isPublic = isPublic;
        this.fromStack = fromStack;
        this.originalPostId = postId;
        commentList = createCommentsList();
        bgWorkers.execute(() -> {
            if (fromStack) {
                posts = contentDb.getAllUnseenPosts();
                Collections.sort(posts, (o1, o2) -> {
                    if (o1.id.equals(postId)) {
                        return -1;
                    } else if (o2.id.equals(postId)) {
                        return 1;
                    } else {
                        return 0;
                    }
                });
            } else {
                Post post = isPublic ? PublicContentCache.getInstance().getPost(postId) : contentDb.getPost(postId);
                if (post != null && !post.senderUserId.isMe()) {
                    contentDb.setIncomingPostSeen(post.senderUserId, post.id, null);
                }
                posts = Collections.singletonList(post);
            }
            updatePost();
            contentDb.addObserver(contentObserver);
            publicContentCache.addObserver(cacheObserver);
        });
    }

    private String getCurrentPostId() {
        return posts == null ? originalPostId : Preconditions.checkNotNull(posts.get(currentIndex)).id;
    }

    public void moveToNextPost() {
        if (posts != null && currentIndex + 1 < posts.size()) {
            currentIndex += 1;
            updatePost();
        }
    }

    public void moveToPreviousPost() {
        if (currentIndex > 0) {
            currentIndex -= 1;
            updatePost();
        }
    }

    private void updatePost() {
        Post post = posts.get(currentIndex);
        postLiveData.postValue(post);

        bgWorkers.execute(() -> {
            followable.postValue(!post.senderUserId.isMe() && contactsDb.getRelationship(post.senderUserId, RelationshipInfo.Type.FOLLOWING) == null);
        });

        if (post != null && !post.senderUserId.isMe()) {
            contentDb.setIncomingPostSeen(post.senderUserId, post.id, null);
        }

        invalidateLatestDataSource();

        final List<String> unseenCommentIds = contentDb.getUnseenCommentIds(getCurrentPostId());
        contentDb.setCommentsSeen(getCurrentPostId());
        Notifications.getInstance(getApplication()).clearReactionNotifications(unseenCommentIds);
    }

    protected LiveData<PagedList<Comment>> createCommentsList() {
        dataSourceFactory = new KatchupCommentDataSource.Factory(isPublic, contentDb, contactsDb, publicContentCache, v -> getCurrentPostId());

        return new LivePagedListBuilder<>(dataSourceFactory, new PagedList.Config.Builder().setPageSize(50).setEnablePlaceholders(false).build()).build();
    }

    protected void invalidateLatestDataSource() {
        dataSourceFactory.invalidateLatestDataSource();
    }

    public LiveData<PagedList<Comment>> getCommentList() {
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
                            getCurrentPostId(),
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
                        publicContentCache.addComment(getCurrentPostId(), comment);
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
                getCurrentPostId(),
                UserId.ME,
                RandomId.create(),
                null,
                System.currentTimeMillis(),
                Comment.TRANSFERRED_NO,
                true,
                text);
        contentDb.addComment(comment);
        if (isPublic) {
            publicContentCache.addComment(getCurrentPostId(), comment);
            invalidateLatestDataSource();
        }
    }

    public void sendTextSticker(String text, @ColorInt int color) {
        bgWorkers.execute(() -> {
            Analytics.getInstance().commented(getPost().getValue(), "sticker");
        });
        final Comment comment = new KatchupStickerComment(
                0,
                getCurrentPostId(),
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
            publicContentCache.addComment(getCurrentPostId(), comment);
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
            Post post = contentDb.getPost(getCurrentPostId());
            if (post == null) {
                Log.e("CommentsViewModel/saveToGallery missing post " + getCurrentPostId());
                result.postValue(false);
                return;
            }

            try {
                ShareIntentHelper.prepareExternalShareVideo(post, true, false, input -> {
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
        return postLiveData;
    }

    public LiveData<Boolean> getFollowable() {
        return followable;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        contentDb.removeObserver(contentObserver);
        publicContentCache.removeObserver(cacheObserver);
    }

    public void onScreenshotted() {
        final Post post = getPost().getValue();
        if (post == null || post.senderUserId.isMe()) {
            Log.i("CommentsViewModel/onScreenshotted null moment or is my moment");
            return;
        }
        if (post instanceof KatchupPost) {
            KatchupPost katchupPost = (KatchupPost) post;
            if (katchupPost.screenshotted == KatchupPost.SCREENSHOT_NO) {
                katchupPost.screenshotted = KatchupPost.SCREENSHOT_YES_PENDING;
                contentDb.setIncomingMomentScreenshotted(katchupPost.senderUserId, katchupPost.id);
            }
        }
    }

    public static class CommentsViewModelFactory implements ViewModelProvider.Factory {

        private Application application;
        private final String postId;
        private final boolean isPublic;
        private final boolean fromStack;

        public CommentsViewModelFactory(@NonNull Application application, String postId, boolean isPublic, boolean fromStack) {
            this.application = application;
            this.postId = postId;
            this.isPublic = isPublic;
            this.fromStack = fromStack;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CommentsViewModel.class)) {
                //noinspection unchecked
                return (T) new CommentsViewModel(application, postId, isPublic, fromStack);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
