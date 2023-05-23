package com.halloapp.katchup.vm;


import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.paging.PositionalDataSource;

import com.google.android.gms.common.util.concurrent.HandlerExecutor;
import com.halloapp.FileStore;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.KatchupStickerComment;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.content.Reaction;
import com.halloapp.content.ReactionComment;
import com.halloapp.id.UserId;
import com.halloapp.katchup.Analytics;
import com.halloapp.katchup.KatchupCommentDataSource;
import com.halloapp.katchup.KatchupPostsDataSource;
import com.halloapp.katchup.KatchupReactionDataSource;
import com.halloapp.katchup.PublicContentCache;
import com.halloapp.katchup.RelationshipApi;
import com.halloapp.katchup.ShareIntentHelper;
import com.halloapp.katchup.media.MediaTranscoderTask;
import com.halloapp.katchup.media.PrepareVideoReactionTask;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

public class CommentsViewModel extends AndroidViewModel {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();
    private final PublicContentCache publicContentCache = PublicContentCache.getInstance();

    private final MutableLiveData<Post> postLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> followable = new MutableLiveData<>();

    private final Set<String> publicPostIdSet = new HashSet<>();
    private final boolean fromStack;
    private final boolean fromArchive;
    private final String originalPostId;

    private KatchupCommentDataSource.Factory commentDataSourceFactory;
    private KatchupReactionDataSource.Factory reactionDataSourceFactory;

    private final LiveData<PagedList<Post>> postList;
    private final LiveData<PagedList<Comment>> commentList;
    private final LiveData<PagedList<Reaction>> reactionList;
    private final ComputableLiveData<Boolean> postIsSelfReacted;

    private final MutableLiveData<Comment> selectedComment = new MutableLiveData<>();
    private final MutableLiveData<Media> playingVideoReaction = new MutableLiveData<>();

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

        @Override
        public void onReactionsAdded(@NonNull List<Reaction> reactions) {
            for (Reaction reaction : reactions) {
                if (getCurrentPostId().equals(reaction.contentId)) {
                    invalidateLatestDataSource();
                }
            }
        }

        @Override
        public void onReactionRetracted(@NonNull Reaction reaction) {
            if (getCurrentPostId().equals(reaction.contentId)) {
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

        @Override
        public void onReactionAdded(@NonNull Reaction reaction, @NonNull ContentItem contentItem) {
            if (getCurrentPostId().equals(reaction.contentId)) {
                invalidateLatestDataSource();
            }
        }
    };

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onRelationshipsChanged() {
            invalidateLatestDataSource();
        }

        @Override
        public void onRelationshipRemoved(@NonNull RelationshipInfo relationshipInfo) {
            if (relationshipInfo.relationshipType == RelationshipInfo.Type.FOLLOWING) {
                invalidateLatestDataSource();
            }
        }
    };

    public CommentsViewModel(@NonNull Application application, String postId, List<String> postIdList, boolean fromStack, boolean fromArchive) {
        super(application);
        this.fromStack = fromStack;
        this.fromArchive = fromArchive;
        this.originalPostId = postId;
        postList = createPostsList(postIdList);
        commentList = createCommentsList();
        reactionList = createReactionsList();
        postIsSelfReacted = new ComputableLiveData<Boolean>() {
            @Override
            protected Boolean compute() {
                return contentDb.getKatchupPostIsSelfReacted(getCurrentPostId());
            }
        };
        contentDb.addObserver(contentObserver);
        publicContentCache.addObserver(cacheObserver);
        contactsDb.addObserver(contactsObserver);
    }

    private String getCurrentPostId() {
        Post post = postLiveData.getValue();
        return post == null ? originalPostId : post.id;
    }

    public void setPost(@NonNull Post post) {
        postLiveData.setValue(post);

        bgWorkers.execute(() -> {
            followable.postValue(!post.senderUserId.isMe() && contactsDb.getRelationship(post.senderUserId, RelationshipInfo.Type.FOLLOWING) == null);
        });

        if (!post.senderUserId.isMe() && (post.seen == Post.SEEN_NO || post.seen == Post.SEEN_NO_HIDDEN)) {
            contentDb.setIncomingPostSeen(post.senderUserId, post.id, null);
        }

        invalidateLatestDataSource();

        final List<String> unseenCommentIds = contentDb.getUnseenCommentIds(getCurrentPostId());
        contentDb.setCommentsSeen(getCurrentPostId());
        Notifications.getInstance(getApplication()).clearReactionNotifications(unseenCommentIds);
    }

    protected LiveData<PagedList<Post>> createPostsList(@Nullable List<String> postIdList) {
        if (fromStack) {
            KatchupPostsDataSource.Factory postsDataSourceFactory = new KatchupPostsDataSource.Factory(contentDb, KatchupPostsDataSource.POST_TYPE_UNSEEN, originalPostId, false);
            return new LivePagedListBuilder<>(postsDataSourceFactory, new PagedList.Config.Builder().setPageSize(5).setEnablePlaceholders(false).build()).build();
        } else if (fromArchive) {
            Executor mainThreadExecutor = new HandlerExecutor(getApplication().getMainLooper());
            MediatorLiveData<PagedList<Post>> mediatorLiveData = new MediatorLiveData<>();

            bgWorkers.execute(() -> {
                Post post = getPost(originalPostId);

                if (post != null && post.senderUserId.isMe()) {
                    KatchupPostsDataSource.Factory postsDataSourceFactory = new KatchupPostsDataSource.Factory(contentDb, KatchupPostsDataSource.POST_TYPE_MY_ARCHIVE, originalPostId, true);
                    LiveData<PagedList<Post>> postListLiveData = new LivePagedListBuilder<>(postsDataSourceFactory, new PagedList.Config.Builder().setPageSize(5).setEnablePlaceholders(false).build()).build();

                    mainThreadExecutor.execute(() -> mediatorLiveData.addSource(postListLiveData, mediatorLiveData::postValue));
                } else if (post != null && postIdList != null) {
                    ArrayList<Post> posts = new ArrayList<>(postIdList.size());
                    for (String postId : postIdList) {
                        Post postItem = getPost(postId);

                        if (postItem != null) {
                            posts.add(postItem);
                        }
                    }

                    Collections.sort(posts, (post1, post2) -> {
                        if (post1.timestamp > post2.timestamp) {
                            return 1;
                        } else if (post1.timestamp < post2.timestamp) {
                            return -1;
                        }

                        return 0;
                    });

                    mediatorLiveData.postValue(pagedList(posts));
                }
            });

            return mediatorLiveData;
        } else {
            MutableLiveData<PagedList<Post>> postsLiveData = new MutableLiveData<>();

            bgWorkers.execute(() -> {
                Post post = getPost(originalPostId);

                if (post != null) {
                    postsLiveData.postValue(pagedList(Collections.singletonList(post)));
                }
            });

            return postsLiveData;
        }
    }

    public boolean isPublic() {
        return publicPostIdSet.contains(getCurrentPostId());
    }

    public boolean isPublic(String postId) {
        return publicPostIdSet.contains(postId);
    }

    @Nullable
    protected Post getPost(@NonNull String postId) {
        Post post = contentDb.getPost(postId);
        if (post != null) {
            return post;
        }

        post = PublicContentCache.getInstance().getPost(postId);
        if (post != null) {
            publicPostIdSet.add(postId);
            return post;
        }

        return null;
    }

    @NonNull
    protected PagedList<Post> pagedList(@NonNull List<Post> posts) {
        Executor mainThreadExecutor = new HandlerExecutor(getApplication().getMainLooper());

        return new PagedList.Builder<>(new PositionalDataSource<Post>() {
            @Override
            public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<Post> callback) {
                callback.onResult(posts, 0, posts.size());
            }

            @Override
            public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<Post> callback) {
                callback.onResult(new ArrayList<>());
            }
        }, posts.size()).setNotifyExecutor(mainThreadExecutor).setFetchExecutor(mainThreadExecutor).build();
    }

    protected LiveData<PagedList<Comment>> createCommentsList() {
        commentDataSourceFactory = new KatchupCommentDataSource.Factory(v -> isPublic(), contentDb, contactsDb, publicContentCache, v -> getCurrentPostId());

        return new LivePagedListBuilder<>(commentDataSourceFactory, new PagedList.Config.Builder().setPageSize(50).setEnablePlaceholders(false).build()).build();
    }

    protected LiveData<PagedList<Reaction>> createReactionsList() {
        reactionDataSourceFactory = new KatchupReactionDataSource.Factory(v -> isPublic(), contentDb, contactsDb, publicContentCache, v -> getCurrentPostId());

        return new LivePagedListBuilder<>(reactionDataSourceFactory, new PagedList.Config.Builder().setPageSize(50).setEnablePlaceholders(false).build()).build();
    }

    protected void invalidateLatestDataSource() {
        commentDataSourceFactory.invalidateLatestDataSource();
        reactionDataSourceFactory.invalidateLatestDataSource();
        postIsSelfReacted.invalidate();
    }

    public LiveData<PagedList<Post>> getPostList() {
        return postList;
    }

    public LiveData<PagedList<Comment>> getCommentList() {
        return commentList;
    }

    public LiveData<PagedList<Reaction>> getReactionList() {
        return reactionList;
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
            bgWorkers.execute(file::delete);
            sendSuccess.postValue(false);
        } else {
            String postId = getCurrentPostId();
            boolean isPublic = isPublic();
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
        if (isPublic()) {
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
        if (isPublic()) {
            publicContentCache.addComment(getCurrentPostId(), comment);
            invalidateLatestDataSource();
        }
    }

    public void retractComment(@NonNull Comment comment, @NonNull ProgressDialog progressDialog) {
        bgWorkers.execute(() -> {
            contentDb.retractComment(comment);
            if (comment instanceof ReactionComment) {
                publicContentCache.removeReaction(comment.postId, ((ReactionComment) comment).reaction);
            } else {
                publicContentCache.removeComment(comment.postId, comment);
            }
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

    public LiveData<Boolean> getPostIsSelfReacted() {
        return postIsSelfReacted.getLiveData();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        contentDb.removeObserver(contentObserver);
        publicContentCache.removeObserver(cacheObserver);
        contactsDb.removeObserver(contactsObserver);
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

    public void toggleLike() {
        final Post post = getPost().getValue();
        if (post == null) {
            Log.i("CommentsViewModel/toggleLike null moment");
            return;
        }

        bgWorkers.execute(() -> {
            final Reaction existingReaction = contentDb.getMyKatchupPostReaction(post.id);
            if (existingReaction != null) {
                contentDb.retractReaction(existingReaction, post);
                if (isPublic(post.id)) {
                    publicContentCache.removeReaction(existingReaction.contentId, existingReaction);
                    invalidateLatestDataSource();
                }
            } else {
                final Reaction newReaction = new Reaction(RandomId.create(), post.id, UserId.ME, Reaction.TYPE_KATCHUP_LIKE, System.currentTimeMillis());
                contentDb.addReaction(newReaction, post);
                if (isPublic(post.id)) {
                    publicContentCache.addReaction(newReaction.contentId, newReaction);
                    invalidateLatestDataSource();
                }
            }
        });
    }

    public LiveData<Boolean> followUser(@NonNull UserId userId) {
        final MutableLiveData<Boolean> result = new MutableLiveData<>();
        RelationshipApi.getInstance().requestFollowUser(userId).onResponse(res -> {
            if (Boolean.TRUE.equals(res)) {
                invalidateLatestDataSource();
                result.postValue(true);
            } else {
                Log.e("Failed to follow user");
                result.postValue(false);
            }
        }).onError(err -> {
            Log.e("Failed to follow user", err);
            result.postValue(false);
        });
        return result;
    }

    public static class CommentsViewModelFactory implements ViewModelProvider.Factory {

        private final Application application;
        private final String postId;
        private final List<String> postIdList;
        private final boolean fromStack;
        private final boolean fromArchive;

        public CommentsViewModelFactory(@NonNull Application application, String postId, List<String> postIdList, boolean fromStack, boolean fromArchive) {
            this.application = application;
            this.postId = postId;
            this.postIdList = postIdList;
            this.fromStack = fromStack;
            this.fromArchive = fromArchive;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CommentsViewModel.class)) {
                //noinspection unchecked
                return (T) new CommentsViewModel(application, postId, postIdList, fromStack, fromArchive);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
