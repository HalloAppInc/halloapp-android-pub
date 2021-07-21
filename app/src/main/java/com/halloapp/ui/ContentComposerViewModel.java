package com.halloapp.ui;

import android.app.Application;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.proto.log_events.MediaComposeLoad;
import com.halloapp.ui.mediaedit.EditImageView;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ContentComposerViewModel extends AndroidViewModel {
    private final FeedPrivacyManager feedPrivacyManager = FeedPrivacyManager.getInstance();
    private ComputableLiveData<FeedPrivacy> feedPrivacyLiveData;

    final MutableLiveData<List<EditMediaPair>> editMedia = new MutableLiveData<>();
    final MutableLiveData<EditMediaPair> loadingItem = new MutableLiveData<>();

    final MutableLiveData<ContentItem> contentItem = new MutableLiveData<>();
    final ComputableLiveData<String> shareTargetName;
    final ComputableLiveData<Post> replyPost;
    final ComputableLiveData<List<Contact>> mentionableContacts;

    private final String replyPostId;
    private final int replyPostMediaIndex;

    private final Me me;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;

    private boolean shouldDeleteTempFiles = true;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            if (mentionableContacts != null) {
                mentionableContacts.invalidate();
            }
        }
    };

    private final FeedPrivacyManager.Observer feedPrivacyObserver = () -> {
        feedPrivacyLiveData.invalidate();
    };

    ContentComposerViewModel(@NonNull Application application, @Nullable ChatId chatId, @Nullable GroupId groupFeedId, @Nullable Collection<Uri> uris, @Nullable Bundle editStates, @Nullable String replyPostId, int replyPostMediaIndex) {
        super(application);
        me = Me.getInstance();
        contentDb = ContentDb.getInstance();
        contactsDb = ContactsDb.getInstance();
        this.replyPostId = replyPostId;
        this.replyPostMediaIndex = replyPostMediaIndex;
        if (uris != null) {
            loadUris(uris, editStates);
        }
        if (chatId != null) {
            shareTargetName = new ComputableLiveData<String>() {
                @Override
                protected String compute() {
                    if (chatId instanceof UserId) {
                        return contactsDb.getContact((UserId)chatId).getDisplayName();
                    } else if (chatId instanceof GroupId) {
                        return Preconditions.checkNotNull(contentDb.getChat(chatId)).name;
                    }
                    return null;
                }
            };
        } else if (groupFeedId != null) {
            shareTargetName = new ComputableLiveData<String>() {
                @Override
                protected String compute() {
                    return Preconditions.checkNotNull(contentDb.getChat(groupFeedId)).name;

                }
            };
        } else {
            shareTargetName = null;
        }
        if (replyPostId != null) {
            replyPost = new ComputableLiveData<Post>() {
                @Override
                protected Post compute() {
                    return contentDb.getPost(replyPostId);
                }
            };
        } else {
            replyPost = null;
        }
        mentionableContacts = new ComputableLiveData<List<Contact>>() {
            @Override
            protected List<Contact> compute() {
                GroupId groupId = groupFeedId;
                if (groupId == null) {
                    if (!(chatId instanceof GroupId)) {
                        List<Contact> users = contactsDb.getUsers();
                        for (Contact contact : users) {
                            if (contact.userId != null) {
                                contact.halloName = contactsDb.readName(contact.userId);
                            }
                        }
                        return users;
                    }
                    groupId = (GroupId) chatId;
                }
                List<MemberInfo> members = contentDb.getGroupMembers(groupId);
                List<Contact> contacts = new ArrayList<>();
                for (MemberInfo memberInfo : members) {
                    if (memberInfo.userId.rawId().equals(me.getUser()) || memberInfo.userId.isMe()) {
                        continue;
                    }
                    contacts.add(contactsDb.getContact(memberInfo.userId));
                }
                return Contact.sort(contacts);
            }
        };
        contactsDb.addObserver(contactsObserver);
        feedPrivacyLiveData = new ComputableLiveData<FeedPrivacy>() {
            @Override
            protected FeedPrivacy compute() {
                return feedPrivacyManager.getFeedPrivacy();
            }
        };
        feedPrivacyManager.addObserver(feedPrivacyObserver);
    }


    @Override
    protected void onCleared() {
        if (shouldDeleteTempFiles) {
            cleanTmpFiles();
        }
        contactsDb.removeObserver(contactsObserver);
        feedPrivacyManager.removeObserver(feedPrivacyObserver);
    }

    @Nullable List<EditMediaPair> getEditMedia() {
        return editMedia.getValue();
    }

    public LiveData<FeedPrivacy> getFeedPrivacy() {
        return feedPrivacyLiveData.getLiveData();
    }

    void loadUris(@NonNull Collection<Uri> uris, @Nullable Bundle editStates) {
        new LoadContentUrisTask(getApplication(), uris, editStates, editMedia, loadingItem).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void prepareContent(@Nullable ChatId chatId, @Nullable GroupId groupFeedGroupId, @Nullable String text, @Nullable List<Mention> mentions, boolean supportsWideColor) {
        new PrepareContentTask(chatId, groupFeedGroupId, text, getSendMediaList(), mentions, contentItem, replyPostId, replyPostMediaIndex, !supportsWideColor).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void cleanTmpFiles() {
        Log.d("ContentComposerViewModel: cleanTmpFiles");
        final List<EditMediaPair> mediaPairList = editMedia.getValue();
        if (mediaPairList != null) {
            new CleanupTmpFilesTask(mediaPairList).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    void deleteMediaItem(final int index) {
        List<EditMediaPair> mediaPairList = editMedia.getValue();
        if (mediaPairList != null && 0 <= index && index < mediaPairList.size()) {
            final EditMediaPair mediaPair = mediaPairList.get(index);
            mediaPairList.remove(index);
            new CleanupTmpFilesTask(new ArrayList<>(Collections.singleton(mediaPair))).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void doNotDeleteTempFiles() {
        shouldDeleteTempFiles = false;
    }

    private @Nullable List<Media> getSendMediaList() {
        final List<EditMediaPair> mediaPairList = getEditMedia();
        if (mediaPairList == null) {
            return null;
        }
        final List<Media> sendmediaPairList = new ArrayList<>();
        for (EditMediaPair mediaPair : mediaPairList) {
            sendmediaPairList.add(mediaPair.getRelevantMedia());
        }
        return sendmediaPairList;
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final ChatId chatId;
        private final GroupId groupFeedId;
        private final Collection<Uri> uris;
        private final Bundle editStates;
        private final String replyId;
        private final int replyPostMediaIndex;

        Factory(@NonNull Application application, @Nullable ChatId chatId, @Nullable GroupId groupFeedId, @Nullable Collection<Uri> uris, @Nullable Bundle editStates, @Nullable String replyId, int replyPostMediaIndex) {
            this.application = application;
            this.chatId = chatId;
            this.groupFeedId = groupFeedId;
            this.uris = uris;
            this.editStates = editStates;
            this.replyId = replyId;
            this.replyPostMediaIndex = replyPostMediaIndex;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ContentComposerViewModel.class)) {
                //noinspection unchecked
                return (T) new ContentComposerViewModel(application, chatId, groupFeedId, uris, editStates, replyId, replyPostMediaIndex);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    static class LoadContentUrisTask extends AsyncTask<Void, EditMediaPair, List<EditMediaPair>> {

        private final Collection<Uri> uris;
        private final Bundle editStates;
        private final Application application;
        private final MutableLiveData<List<EditMediaPair>> media;
        private final MutableLiveData<EditMediaPair> loadingItem;

        final private long createTime;

        LoadContentUrisTask(@NonNull Application application,
                            @NonNull Collection<Uri> uris,
                            @Nullable Bundle editStates,
                            @NonNull MutableLiveData<List<EditMediaPair>> media,
                            @NonNull MutableLiveData<EditMediaPair> loadingItem) {
            this.application = application;
            this.uris = uris;
            this.editStates = editStates;
            this.media = media;
            this.loadingItem = loadingItem;
            this.createTime = System.currentTimeMillis();
        }

        @Override
        protected List<EditMediaPair> doInBackground(Void... voids) {
            final List<EditMediaPair> mediaPairList = new ArrayList<>();

            final FileStore fileStore = FileStore.getInstance();
            int uriIndex = 0;
            final Map<Uri, Integer> types = MediaUtils.getMediaTypes(application, uris);

            int numVideos = 0;
            int numPhotos = 0;
            long totalSize = 0;

            for (Uri uri : uris) {
                final boolean isLocalFile = Objects.equals(uri.getScheme(), "file");
                @Media.MediaType int mediaType = types.get(uri);

                switch (mediaType) {
                    case Media.MEDIA_TYPE_IMAGE:
                        numPhotos++;
                        break;
                    case Media.MEDIA_TYPE_VIDEO:
                        numVideos++;
                        break;
                }

                final File originalFile = fileStore.getTmpFileForUri(uri, null);
                boolean fileCreated = false;
                if (!originalFile.exists()) {
                    if (isLocalFile) {
                        try {
                            File src = new File(uri.getPath());
                            if (FileUtils.isInternalFile(src)) {
                                Log.w("Skipping uri for internal file " + uri);
                                continue;
                            }
                            FileUtils.copyFile(src, originalFile);
                        } catch (IOException e) {
                            // Swallow the exception, the logic below will handle the case of empty file.
                            Log.e("LoadContentUrisTask: doInBackground copyFile " + uri, e);
                        }
                    } else {
                        FileUtils.uriToFile(application, uri, originalFile);
                    }
                    fileCreated = true;
                }
                final Size originalSize = MediaUtils.getDimensions(originalFile, mediaType);

                final File editFile = fileStore.getTmpFileForUri(uri, "edit");
                final Size editSize = editFile.exists() ? MediaUtils.getDimensions(editFile, mediaType) : null;

                if (originalSize != null) {
                    final Media originalItem = Media.createFromFile(mediaType, originalFile);
                    originalItem.width = originalSize.getWidth();
                    originalItem.height = originalSize.getHeight();

                    totalSize += originalFile.length();

                    final Media editItem;
                    if (editSize != null) {
                        editItem = Media.createFromFile(mediaType, editFile);
                        editItem.width = editSize.getWidth();
                        editItem.height = editSize.getHeight();
                    } else {
                        editItem = null;
                    }

                    final Parcelable state;
                    if (editStates == null) {
                        state = null;
                    } else {
                        editStates.setClassLoader(EditImageView.State.class.getClassLoader());
                        state = editStates.getParcelable(uri.toString());
                    }
                    mediaPairList.add(new EditMediaPair(uri, originalItem, editItem, state));

                    if (mediaPairList.size() == 1 && uriIndex + 1 != uris.size()) {
                        publishProgress(mediaPairList.get(0));
                    }
                } else {
                    if (fileCreated && originalFile.exists()) {
                        if (!originalFile.delete()) {
                            Log.e("ContentComposerViewModel failed to delete " + originalFile.getAbsolutePath());
                        }
                    }
                    Log.e("PostComposerActivity: failed to load " + uri);
                }

                uriIndex++;
            }
            Events.getInstance().sendEvent(MediaComposeLoad.newBuilder()
                    .setDurationMs((int)(System.currentTimeMillis() - createTime))
                    .setNumPhotos(numPhotos)
                    .setNumVideos(numVideos)
                    .setTotalSize((int) totalSize).build());

            return mediaPairList;
        }

        @Override
        protected void onProgressUpdate(EditMediaPair... mediaPairs) {
            this.loadingItem.postValue(mediaPairs[0]);
        }

        @Override
        protected void onPostExecute(List<EditMediaPair> mediaPairList) {
            this.media.postValue(mediaPairList);
        }
    }

    static class PrepareContentTask extends AsyncTask<Void, Void, Void> {

        private final ContentDb contentDb = ContentDb.getInstance();
        private final ContactsDb contactsDb = ContactsDb.getInstance();
        private final FeedPrivacyManager feedPrivacyManager = FeedPrivacyManager.getInstance();

        private final ChatId chatId;
        private final GroupId groupId;
        private final String text;
        private final List<Media> media;
        private final List<Mention> mentions;
        private final MutableLiveData<ContentItem> contentItem;
        private final String replyPostId;
        private final int replyPostMediaIndex;
        private final boolean forcesRGB;

        PrepareContentTask(@Nullable ChatId chatId, @Nullable GroupId groupId, @Nullable String text, @Nullable List<Media> media, @Nullable List<Mention> mentions, @NonNull MutableLiveData<ContentItem> contentItem, @Nullable String replyPostId, int replyPostMediaIndex, boolean forcesRGB) {
            this.chatId = chatId;
            this.groupId = groupId;
            this.text = text;
            this.media = media;
            this.mentions = mentions;
            this.contentItem = contentItem;
            this.replyPostId = replyPostId;
            this.replyPostMediaIndex = replyPostMediaIndex;
            this.forcesRGB = forcesRGB;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Post replyPost = replyPostId == null ? null : contentDb.getPost(replyPostId);
            final ContentItem contentItem = chatId == null ?
                    new Post(0, UserId.ME, RandomId.create(), System.currentTimeMillis(),Post.TRANSFERRED_NO, Post.SEEN_YES, text) :
                    new Message(0, chatId, UserId.ME, RandomId.create(), System.currentTimeMillis(), Message.TYPE_CHAT, Message.USAGE_CHAT, Message.STATE_INITIAL, text, replyPostId, replyPostMediaIndex, null, -1, replyPost == null ? null : replyPost.senderUserId, 0);
            if (media != null) {
                for (Media mediaItem : media) {
                    final File postFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(mediaItem.type));
                    switch (mediaItem.type) {
                        case Media.MEDIA_TYPE_IMAGE: {
                            try {
                                MediaUtils.transcodeImage(mediaItem.file, postFile, null, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY, forcesRGB);
                            } catch (IOException e) {
                                Log.e("failed to transcode image", e);
                                return null;
                            }
                            break;
                        }
                        case Media.MEDIA_TYPE_VIDEO: {
                            if (!mediaItem.file.renameTo(postFile)) {
                                Log.e("failed to rename " + mediaItem.file.getAbsolutePath() + " to " + postFile.getAbsolutePath());
                                return null;
                            }
                            break;
                        }
                        case Media.MEDIA_TYPE_UNKNOWN:
                        default: {
                            Log.e("unknown media type " + mediaItem.file.getAbsolutePath());
                            return null;
                        }
                    }
                    final Media sendMedia = Media.createFromFile(mediaItem.type, postFile);
                    contentItem.media.add(sendMedia);
                }
            }
            if (mentions != null) {
                contentItem.mentions.addAll(mentions);
            }
            if (contentItem instanceof Post) {
                Post contentPost = (Post) contentItem;
                if (groupId != null) {
                    contentPost.setParentGroup(groupId);
                } else {
                    FeedPrivacy feedPrivacy = feedPrivacyManager.getFeedPrivacy();
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
                        contentPost.setExcludeList(feedPrivacy.exceptList);
                    }
                    contentPost.setAudience(audienceType, audienceList);
                }
            }
            this.contentItem.postValue(contentItem);
            return null;
        }
    }

    static class EditMediaPair {
        Uri uri;
        Media original;
        Media edit;
        Parcelable state;

        public static float getMaxAspectRatio(List<EditMediaPair> mediaPairList) {
            float maxAspectRatio = 0;
            for (EditMediaPair mediaPair : mediaPairList) {
                final Media mediaItem = mediaPair.getRelevantMedia();
                if (mediaItem.width != 0) {
                    float ratio = 1f * mediaItem.height / mediaItem.width;
                    if (ratio > maxAspectRatio) {
                        maxAspectRatio = ratio;
                    }
                }
            }
            return maxAspectRatio;
        }

        EditMediaPair(Uri uri, Media original, Media edit, Parcelable state) {
            this.uri = uri;
            this.original = original;
            this.edit = edit;
            this.state = state;
        }

        final Media getRelevantMedia() {
            return edit == null ? original : edit;
        }
    }

    static class CleanupTmpFilesTask extends AsyncTask<Void, Void, Void> {

        private final List<EditMediaPair> mediaPairList;

        CleanupTmpFilesTask(@NonNull List<EditMediaPair> media) {
            this.mediaPairList = media;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (EditMediaPair mediaPair : mediaPairList) {
                if (!mediaPair.original.file.delete()) {
                    Log.e("failed to delete temporary file " + mediaPair.original.file.getAbsolutePath());
                }
                if (mediaPair.edit != null && mediaPair.edit.file.exists()) {
                    if (!mediaPair.edit.file.delete()) {
                        Log.e("failed to delete temporary file " + mediaPair.edit.file.getAbsolutePath());
                    }
                }
            }
            return null;
        }
    }
}
