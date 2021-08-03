package com.halloapp.ui;

import android.app.Application;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.FlatCommentsDataSource;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Post;
import com.halloapp.content.VoiceNoteComment;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.media.VoiceNoteRecorder;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.FileUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class FlatCommentsViewModel extends AndroidViewModel {

    final LiveData<PagedList<Comment>> commentList;
    final ComputableLiveData<Long> lastSeenCommentRowId;
    final ComputableLiveData<List<Contact>> mentionableContacts;
    final ComputableLiveData<Boolean> isMember;
    final MutableLiveData<Post> post = new MutableLiveData<>();
    final MutableLiveData<Contact> replyContact = new MutableLiveData<>();
    final MutableLiveData<Boolean> postDeleted = new MutableLiveData<>();
    final MutableLiveData<Media> commentMedia = new MutableLiveData<>();

    private final Me me = Me.getInstance();
    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();

    private final String postId;
    private final FlatCommentsDataSource.Factory dataSourceFactory;

    private LoadUserTask loadUserTask;
    private LoadMediaUriTask loadMediaUriTask;

    private Observer<Post> postObserver;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private VoiceNoteRecorder voiceNoteRecorder;
    private VoiceNotePlayer voiceNotePlayer;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostRetracted(@NonNull Post post) {
            if (FlatCommentsViewModel.this.postId.equals(post.id)) {
                postDeleted.postValue(true);
            }
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            if (FlatCommentsViewModel.this.postId.equals(postId)) {
                loadPost(postId);
            }
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            if (FlatCommentsViewModel.this.postId.equals(comment.postId)) {
                Post parentPost = comment.getParentPost();
                if (parentPost != null) {
                    contentDb.setCommentsSeen(comment.postId);
                }
                invalidateDataSource();
            }
        }

        @Override
        public void onCommentUpdated(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
            if (FlatCommentsViewModel.this.postId.equals(postId)) {
                invalidateDataSource();
            }
        }

        @Override
        public void onCommentRetracted(@NonNull Comment comment) {
            if (FlatCommentsViewModel.this.postId.equals(comment.postId)) {
                invalidateDataSource();
            }
        }

        @Override
        public void onFeedCleanup() {
            invalidateDataSource();
        }

        private void invalidateDataSource() {
            mainHandler.post(dataSourceFactory::invalidateLatestDataSource);
        }

        @Override
        public void onGroupMembersChanged(@NonNull GroupId groupId) {
            Post post = FlatCommentsViewModel.this.post.getValue();
            if (post == null || groupId.equals(post.getParentGroup())) {
                isMember.invalidate();
            }
        }
    };

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            mentionableContacts.invalidate();
        }
    };

    public VoiceNotePlayer getVoiceNotePlayer() {
        return voiceNotePlayer;
    }

    private FlatCommentsViewModel(@NonNull Application application, @NonNull String postId) {
        super(application);
        this.postId = postId;

        voiceNoteRecorder = new VoiceNoteRecorder();
        voiceNotePlayer = new VoiceNotePlayer(application);

        contentDb.addObserver(contentObserver);

        lastSeenCommentRowId = new ComputableLiveData<Long>() {
            @Override
            protected Long compute() {
                long rowId = contentDb.getLastSeenCommentRowId(postId);
                contentDb.setCommentsSeen(postId);
                return rowId;
            }
        };

        isMember = new ComputableLiveData<Boolean>() {
            @Override
            protected Boolean compute() {
                GroupId groupId = contentDb.getPost(postId).getParentGroup();
                if (groupId != null) {
                    List<MemberInfo> members = contentDb.getGroupMembers(groupId);
                    for (MemberInfo memberInfo : members) {
                        if (memberInfo.userId.rawId().equals(me.getUser()) || memberInfo.userId.isMe()) {
                            return true;
                        }
                    }
                    return false;
                }
                return true;
            }
        };

        dataSourceFactory = new FlatCommentsDataSource.Factory(contentDb, contactsDb, postId);
        commentList = new LivePagedListBuilder<>(dataSourceFactory, new PagedList.Config.Builder().setPageSize(50).setEnablePlaceholders(false).build()).build();
        mentionableContacts = new ComputableLiveData<List<Contact>>() {
            @Override
            protected List<Contact> compute() {
                HashSet<UserId> contactSet = new HashSet<>();
                HashMap<UserId, String> mentionSet = new HashMap<>();
                Post parentPost = post.getValue();
                if (parentPost != null) {
                    // Allow mentioning anybody in the group if it is a group post
                    if (parentPost.parentGroup != null) {
                        List<MemberInfo> members = contentDb.getGroupMembers(parentPost.parentGroup);
                        List<Contact> contacts = new ArrayList<>();
                        for (MemberInfo memberInfo : members) {
                            if (memberInfo.userId.rawId().equals(me.getUser()) || memberInfo.userId.isMe()) {
                                continue;
                            }
                            contacts.add(contactsDb.getContact(memberInfo.userId));
                        }
                        return Contact.sort(contacts);
                    }

                    // Allow mentioning every mention from the post
                    final List<Mention> mentionsCopy = new ArrayList<>(parentPost.mentions);
                    for (Mention mention : mentionsCopy) {
                        if (!mention.userId.isMe()) {
                            mentionSet.put(mention.userId, mention.fallbackName);
                            contactSet.add(mention.userId);
                        }
                    }
                    if (!parentPost.senderUserId.isMe()) {
                        // Allow mentioning poster
                        contactSet.add(parentPost.senderUserId);
                    } else {
                        if (parentPost.getAudienceType() != null) {
                            switch (parentPost.getAudienceType()) {
                                case PrivacyList.Type.ALL: {
                                    List<Contact> contacts = contactsDb.getUsers();
                                    for (Contact contact : contacts) {
                                        contactSet.add(contact.userId);
                                    }
                                    break;
                                }
                                case PrivacyList.Type.EXCEPT: {
                                    List<Contact> contacts = contactsDb.getUsers();
                                    for (Contact contact : contacts) {
                                        contactSet.add(contact.userId);
                                    }
                                    if (parentPost.getExcludeList() != null) {
                                        for (UserId id : parentPost.getExcludeList()) {
                                            contactSet.remove(id);
                                        }
                                    }
                                    break;
                                }
                                case PrivacyList.Type.ONLY: {
                                    if (parentPost.getAudienceList() != null) {
                                        contactSet.addAll(parentPost.getAudienceList());
                                    }
                                    break;
                                }
                            }
                        } else {
                            List<Contact> contacts = contactsDb.getUsers();
                            for (Contact contact : contacts) {
                                contactSet.add(contact.userId);
                            }
                        }
                    }
                }
                // Allow mentioning everyone who has commented on the post
                PagedList<Comment> comments = commentList.getValue();
                if (comments != null) {
                    for (Comment comment : comments) {
                        if (!comment.senderUserId.isMe()) {
                            contactSet.add(comment.senderUserId);
                        }
                    }
                }
                List<Contact> contactList = new ArrayList<>();
                for (UserId userId : contactSet) {
                    Contact contact = contactsDb.getContact(userId);
                    String mentionName = mentionSet.get(userId);
                    if (!TextUtils.isEmpty(mentionName)) {
                        contact.fallbackName = mentionName;
                    }
                    contactList.add(contact);
                }
                return contactList;
            }
        };
        postObserver = post -> mentionableContacts.invalidate();
        post.observeForever(postObserver);
        contactsDb.addObserver(contactsObserver);
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
        contactsDb.removeObserver(contactsObserver);
        post.removeObserver(postObserver);
        voiceNotePlayer.onCleared();
        voiceNoteRecorder.onCleared();
    }

    public LiveData<Boolean> isRecording() {
        return voiceNoteRecorder.isRecording();
    }

    void loadReplyUser(@NonNull UserId userId) {
        if (loadUserTask != null) {
            loadUserTask.cancel(true);
        }

        loadUserTask = new LoadUserTask(userId, replyContact);
        loadUserTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void resetReplyUser() {
        if (loadUserTask != null) {
            loadUserTask.cancel(true);
        }
        replyContact.setValue(null);
    }

    void loadPost(@NonNull String postId) {
        new LoadPostTask(postId, post).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void loadCommentMediaUri(@NonNull Uri uri) {
        if (loadMediaUriTask != null) {
            loadMediaUriTask.cancel(true);
        }

        loadMediaUriTask = new LoadMediaUriTask(getApplication(), uri, commentMedia);
        loadMediaUriTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void resetCommentMediaUri() {
        if (loadMediaUriTask != null) {
            loadMediaUriTask.cancel(true);
        }
        commentMedia.setValue(null);
    }

    public void startRecording() {
        voiceNoteRecorder.record();
    }

    public void cancelRecording() {
        finishRecording(null, true);
    }

    public LiveData<Long> getRecordingTime() {
        return voiceNoteRecorder.getRecordingTime();
    }

    public void finishRecording(@Nullable String replyCommentId, boolean canceled) {
        final File recording = voiceNoteRecorder.finishRecording();
        if (canceled || recording == null) {
            return;
        }
        bgWorkers.execute(() -> {
            final File targetFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_AUDIO));
            if (!recording.renameTo(targetFile)) {
                Log.e("failed to rename " + recording.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
                return;
            }
            final Comment comment = new VoiceNoteComment(
                    0,
                    postId,
                    UserId.ME,
                    RandomId.create(),
                    replyCommentId,
                    System.currentTimeMillis(),
                    false,
                    true,
                    null);
            comment.setParentPost(post.getValue());

            final Media sendMedia = Media.createFromFile(Media.MEDIA_TYPE_AUDIO, targetFile);
            comment.media.add(sendMedia);
            contentDb.addComment(comment);
        });
    }

    void sendComment(@Nullable String postText, List<Mention> mentions, @Nullable String replyCommentId, boolean supportsWideColor) {
        final Comment comment = new Comment(
                0,
                postId,
                UserId.ME,
                RandomId.create(),
                replyCommentId,
                System.currentTimeMillis(),
                false,
                true,
                postText);
        comment.setParentPost(post.getValue());
        for (Mention mention : mentions) {
            if (mention.index < 0 || mention.index >= postText.length()) {
                continue;
            }
            comment.mentions.add(mention);
        }

        Media mediaItem = commentMedia.getValue();
        if (mediaItem == null) {
            contentDb.addComment(comment);
        } else {
            bgWorkers.execute(() -> {
                final File postFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(mediaItem.type));
                switch (mediaItem.type) {
                    case Media.MEDIA_TYPE_IMAGE: {
                        try {
                            RectF cropRect = null;
                            if (mediaItem.height > Constants.MAX_IMAGE_ASPECT_RATIO * mediaItem.width) {
                                final float padding = (mediaItem.height - Constants.MAX_IMAGE_ASPECT_RATIO * mediaItem.width) / 2;
                                cropRect = new RectF(0, padding / mediaItem.height, 1, 1 - padding / mediaItem.height);
                            }
                            MediaUtils.transcodeImage(mediaItem.file, postFile, cropRect, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY, !supportsWideColor);
                        } catch (IOException e) {
                            Log.e("failed to transcode image", e);
                            return; // TODO(jack): Error messages for user for these 3 cases
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_VIDEO: {
                        if (!mediaItem.file.renameTo(postFile)) {
                            Log.e("failed to rename " + mediaItem.file.getAbsolutePath() + " to " + postFile.getAbsolutePath());
                            return;
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_UNKNOWN:
                    default: {
                        Log.e("unknown media type " + mediaItem.file.getAbsolutePath());
                        return;
                    }
                }
                final Media sendMedia = Media.createFromFile(mediaItem.type, postFile);
                comment.media.add(sendMedia);
                contentDb.addComment(comment);
            });
        }
    }

    static class LoadUserTask extends AsyncTask<Void, Void, Contact> {

        private final UserId userId;
        private final MutableLiveData<Contact> contact;

        LoadUserTask(@NonNull UserId userId, @NonNull MutableLiveData<Contact> contact) {
            this.userId = userId;
            this.contact = contact;
        }

        @Override
        protected Contact doInBackground(Void... voids) {
            return ContactsDb.getInstance().getContact(userId);
        }

        @Override
        protected void onPostExecute(final Contact contact) {
            this.contact.postValue(contact);
        }
    }

    static class LoadPostTask extends AsyncTask<Void, Void, Post> {

        private final String postId;
        private final MutableLiveData<Post> post;

        LoadPostTask(@NonNull String postId, @NonNull MutableLiveData<Post> post) {
            this.postId = postId;
            this.post = post;
        }

        @Override
        protected Post doInBackground(Void... voids) {
            return ContentDb.getInstance().getPost(postId);
        }

        @Override
        protected void onPostExecute(final Post post) {
            this.post.postValue(post);
        }
    }

    private static class LoadMediaUriTask extends AsyncTask<Void, Void, Media> {

        private final Uri uri;
        private final Application application;
        private final MutableLiveData<Media> media;

        LoadMediaUriTask(@NonNull Application application, @NonNull Uri uri, @NonNull MutableLiveData<Media> media) {
            this.application = application;
            this.uri = uri;
            this.media = media;
        }

        @Override
        protected Media doInBackground(Void... voids) {
            final Map<Uri, Integer> types = MediaUtils.getMediaTypes(application, Collections.singletonList(uri));
            @Media.MediaType int mediaType = types.get(uri);
            final File file = FileStore.getInstance().getTmpFile(RandomId.create());
            FileUtils.uriToFile(application, uri, file);
            final Size size = MediaUtils.getDimensions(file, mediaType);
            if (size != null) {
                final Media mediaItem = Media.createFromFile(mediaType, file);
                mediaItem.width = size.getWidth();
                mediaItem.height = size.getHeight();
                return mediaItem;
            } else {
                Log.e("CommentsViewModel: failed to load " + uri);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Media media) {
            this.media.postValue(media);
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final String postId;

        Factory(@NonNull Application application, @NonNull String postId) {
            this.application = application;
            this.postId = postId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(FlatCommentsViewModel.class)) {
                //noinspection unchecked
                return (T) new FlatCommentsViewModel(application, postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

