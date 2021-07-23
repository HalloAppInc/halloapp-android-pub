package com.halloapp.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PostOptionsViewModel extends ViewModel {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final AppContext appContext = AppContext.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();

    final ComputableLiveData<Post> post;
    final MutableLiveData<Boolean> postDeleted = new MutableLiveData<>();

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostRetracted(@NonNull Post retractedPost) {
            final Post post = PostOptionsViewModel.this.post.getLiveData().getValue();
            if (post != null && post.senderUserId.equals(retractedPost.senderUserId) && post.id.equals(retractedPost.id)) {
                postDeleted.postValue(true);
            }
        }

    };

    private PostOptionsViewModel(@NonNull String postId, boolean isArchived) {
        post = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                return isArchived ? contentDb.getArchivePost(postId) : contentDb.getPost(postId);
            }
        };

        contentDb.addObserver(contentObserver);
    }

    public LiveData<Boolean> savePostToGallery() {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        Post post = this.post.getLiveData().getValue();
        if (post == null) {
            success.setValue(false);
            return success;
        }
        Context context = appContext.get();
        bgWorkers.execute(() -> {
            for (Media media : post.media) {
                if (!MediaUtils.saveMediaToGallery(context, media)) {
                    success.postValue(false);
                    Log.e("PostOptionsViewModel/savePostToGallery failed to save media to gallery: " + media);
                    return;
                }
            }
            success.postValue(true);
        });
        return success;
    }

    public void resharePost(boolean supportsWideColor) {
        Post oldPost = this.post.getLiveData().getValue();
        Post newPost = new Post(0, UserId.ME, RandomId.create(), System.currentTimeMillis(), Post.TRANSFERRED_NO, Post.SEEN_YES, oldPost.text);
        for (Media mediaItem : oldPost.media) {
            final File postFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(mediaItem.type));
            switch (mediaItem.type) {
                case Media.MEDIA_TYPE_IMAGE: {
                    try {
                        MediaUtils.transcodeImage(mediaItem.file, postFile, null, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY, !supportsWideColor);
                    } catch (IOException e) {
                        Log.e("failed to transcode image", e);
                        return;
                    }
                    break;
                }
                case Media.MEDIA_TYPE_VIDEO: {
                    MediaUtils.copyFile(mediaItem.file, postFile);
                    break;
                }
                case Media.MEDIA_TYPE_UNKNOWN:
                default: {
                    Log.e("unknown media type " + mediaItem.file.getAbsolutePath());
                    return;
                }
            }
            final Media sendMedia = Media.createFromFile(mediaItem.type, postFile);
            newPost.media.add(sendMedia);
        }
        if (oldPost.getParentGroup() != null) {
            newPost.setParentGroup(oldPost.getParentGroup());
        } else {
            FeedPrivacy feedPrivacy = FeedPrivacyManager.getInstance().getFeedPrivacy();
            List<UserId> audienceList;
            @PrivacyList.Type String audienceType;
            if (feedPrivacy == null || PrivacyList.Type.ALL.equals(feedPrivacy.activeList)) {
                List<Contact> contacts = contactsDb.getUsers();
                audienceList = new ArrayList<>(contacts.size());
                for (Contact contact : contacts) {
                    audienceList.add(contact.userId);
                }
                audienceType = PrivacyList.Type.ALL;
            } else if (PrivacyList.Type.ONLY.equals(feedPrivacy.activeList)) {
                audienceList = feedPrivacy.onlyList;
                audienceType = PrivacyList.Type.ONLY;
            } else {
                HashSet<UserId> excludedSet = new HashSet<>(feedPrivacy.exceptList);
                audienceType = PrivacyList.Type.EXCEPT;
                List<Contact> contacts = contactsDb.getUsers();
                audienceList = new ArrayList<>(contacts.size());
                for (Contact contact : contacts) {
                    if (!excludedSet.contains(contact.userId)) {
                        audienceList.add(contact.userId);
                    }
                }
                newPost.setExcludeList(feedPrivacy.exceptList);
            }
            newPost.setAudience(audienceType, audienceList);
        }
        ContentDb.getInstance().addPost(newPost);
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final String postId;
        private final boolean isArchived;

        Factory(@NonNull String postId, boolean isArchived) {
            this.postId = postId;
            this.isArchived = isArchived;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(PostOptionsViewModel.class)) {
                //noinspection unchecked
                return (T) new PostOptionsViewModel(postId, isArchived);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
