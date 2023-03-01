package com.halloapp.katchup.vm;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.arch.core.util.Function;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.daasuu.mp4compose.composer.ImagePostShareGenerator;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.KatchupStickerComment;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.katchup.Analytics;
import com.halloapp.katchup.KatchupCommentDataSource;
import com.halloapp.katchup.PublicContentCache;
import com.halloapp.katchup.media.MediaTranscoderTask;
import com.halloapp.katchup.media.PrepareVideoReactionTask;
import com.halloapp.katchup.media.TranscodeExternalShareVideoTask;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;

public class CommentsViewModel extends ViewModel {

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

    public CommentsViewModel(String postId, boolean isPublic) {
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
        contentDb.setCommentsSeen(postId);
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
                    Analytics.getInstance().commented("reaction");
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
        Analytics.getInstance().commented("text");
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
        Analytics.getInstance().commented("sticker");
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

    @WorkerThread
    private void prepareExternalShareVideo(Post post, @NonNull Function<File, Void> callback) throws IOException {
        Media selfie = post.getMedia().get(0);
        Media content = post.getMedia().get(1);
        File postFile = FileStore.getInstance().getShareFile(postId + ".mp4");
        if (postFile.exists()) {
            callback.apply(postFile);
            return;
        }
        if (content.type == Media.MEDIA_TYPE_VIDEO) {
            TranscodeExternalShareVideoTask transcodeExternalShareVideoTask = new TranscodeExternalShareVideoTask(content.file, selfie.file, postFile);
            MediaTranscoderTask transcoderTask = new MediaTranscoderTask(transcodeExternalShareVideoTask);
            transcoderTask.setListener(new MediaTranscoderTask.Listener() {
                @Override
                public void onSuccess() {
                    callback.apply(postFile);
                }

                @Override
                public void onError(Exception e) {
                    postFile.delete();
                    callback.apply(null);
                }

                @Override
                public void onProgress(double progress) {

                }

                @Override
                public void onCanceled() {
                    postFile.delete();
                    callback.apply(null);
                }
            });
            transcoderTask.start();
        } else if (content.type == Media.MEDIA_TYPE_IMAGE) {
            ImagePostShareGenerator.generateExternalShareVideo(content.file, selfie.file, postFile);
            callback.apply(postFile);
        } else {
            Log.e("Unexpected content type " + content.type);
            callback.apply(null);
        }
    }

    private Intent generateShareIntent(@NonNull Context context, File postFile) {
        Uri videoUri = FileProvider.getUriForFile(context, "com.halloapp.katchup.fileprovider", postFile);
        return (new ShareCompat.IntentBuilder(context))
                .setStream(videoUri)
                .setType("video/mp4")
                .setChooserTitle(context.getString(R.string.share_moment_label)).createChooserIntent();
    }

    public LiveData<Intent> shareExternallyWithPreview(@NonNull Context context) {
        MutableLiveData<Intent> result = new MutableLiveData<>();
        bgWorkers.execute(() -> {
            Post post = contentDb.getPost(postId);
            try {
                prepareExternalShareVideo(post, input -> {
                    if (input == null) {
                        Log.e("CommentsViewModel/shareExternallyWithPreview failed to get transcoded file");
                        result.postValue(null);
                    } else {
                        result.postValue(generateShareIntent(context, input));
                    }
                    return null;
                });
            } catch (IOException e) {
                Log.e("CommentsViewModel/shareExternallyWithPreview failed", e);
            }
        });

        return result;
    }

    public LiveData<Boolean> saveToGallery(@NonNull Context context) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        bgWorkers.execute(() -> {
            Post post = contentDb.getPost(postId);
            try {
                prepareExternalShareVideo(post, input -> {
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

        private final String postId;
        private final boolean isPublic;

        public CommentsViewModelFactory(String postId, boolean isPublic) {
            this.postId = postId;
            this.isPublic = isPublic;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CommentsViewModel.class)) {
                //noinspection unchecked
                return (T) new CommentsViewModel(postId, isPublic);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
