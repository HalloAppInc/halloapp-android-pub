package com.halloapp.ui;

import android.annotation.SuppressLint;
import android.app.Application;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
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
import com.halloapp.content.VoiceNotePost;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.media.VoiceNoteRecorder;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.log_events.MediaComposeLoad;
import com.halloapp.ui.mediaedit.EditImageView;
import com.halloapp.ui.share.ShareDestination;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ContentComposerViewModel extends AndroidViewModel {
    private final FeedPrivacyManager feedPrivacyManager = FeedPrivacyManager.getInstance();

    final MutableLiveData<Boolean> isLoadingMedia = new MutableLiveData<>(false);
    final MutableLiveData<Boolean> isLoadingVoiceDraft = new MutableLiveData<>(false);
    final MutableLiveData<Boolean> hasMediaLoadFailure = new MutableLiveData<>(false);

    final MutableLiveData<List<EditMediaPair>> editMedia = new MutableLiveData<>();
    final MutableLiveData<EditMediaPair> loadingItem = new MutableLiveData<>();

    final MutableLiveData<FeedPrivacy> feedPrivacyLiveData = new MutableLiveData<>();
    final MutableLiveData<List<ShareDestination>> destinationList =  new MutableLiveData<>(new ArrayList<>());
    final MutableLiveData<List<ContentItem>> contentItems = new MutableLiveData<>();
    final ComputableLiveData<String> shareTargetName;
    final ComputableLiveData<Post> replyPost;
    final ComputableLiveData<List<Contact>> mentionableContacts;

    private final String replyPostId;
    private final int replyPostMediaIndex;

    private final Me me;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;
    private final BgWorkers bgWorkers;

    private boolean shouldDeleteVoiceDraft = true;
    private boolean shouldDeleteTempMedia = true;

    private final VoiceNotePlayer voiceNotePlayer;
    private final VoiceNoteRecorder voiceNoteRecorder;

    private File voiceDraft;

    private @PrivacyList.Type String feedTarget = PrivacyList.Type.ALL;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            if (mentionableContacts != null) {
                mentionableContacts.invalidate();
            }
        }
    };

    private final FeedPrivacyManager.Observer feedPrivacyObserver = this::refreshFeedPrivacy;

    private GroupId targetGroupId;
    private final ChatId targetChatId;

    ContentComposerViewModel(@NonNull Application application, @Nullable ChatId chatId, @Nullable GroupId groupFeedId, @Nullable Collection<Uri> uris, @Nullable Bundle editStates, @Nullable Uri voiceDraftUri, @Nullable List<ShareDestination> destinations, @Nullable String replyPostId, int replyPostMediaIndex) {
        super(application);
        me = Me.getInstance();
        bgWorkers = BgWorkers.getInstance();
        contentDb = ContentDb.getInstance();
        contactsDb = ContactsDb.getInstance();
        this.replyPostId = replyPostId;
        this.replyPostMediaIndex = replyPostMediaIndex;

        this.targetChatId = chatId;
        this.targetGroupId = groupFeedId;

        this.voiceNotePlayer = new VoiceNotePlayer(application);
        this.voiceNoteRecorder = new VoiceNoteRecorder();
        this.destinationList.setValue(destinations);

        if (destinations != null) {
            for (ShareDestination destination : destinations) {
                if (destination.type == ShareDestination.TYPE_FAVORITES) {
                    feedTarget = PrivacyList.Type.ONLY;
                    break;
                }
            }
        }

        if (uris != null) {
            loadUris(uris, editStates);
        }
        if (voiceDraftUri != null) {
            copyVoiceDraftUri(voiceDraftUri);
        }
        shareTargetName = new ComputableLiveData<String>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected String compute() {
                if (targetChatId != null) {
                    if (chatId instanceof UserId) {
                        return contactsDb.getContact((UserId) chatId).getDisplayName();
                    } else if (chatId instanceof GroupId) {
                        return Preconditions.checkNotNull(contentDb.getGroupFeedOrChat((GroupId) chatId)).name;
                    }
                } else if (targetGroupId != null) {
                    return Preconditions.checkNotNull(contentDb.getGroup(targetGroupId)).name;
                }
                return null;
            }
        };
        if (replyPostId != null) {
            replyPost = new ComputableLiveData<Post>() {
                @SuppressLint("RestrictedApi")
                @Override
                protected Post compute() {
                    return contentDb.getPost(replyPostId);
                }
            };
        } else {
            replyPost = null;
        }
        mentionableContacts = new ComputableLiveData<List<Contact>>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected List<Contact> compute() {
                GroupId groupId = groupFeedId;
                if (groupId == null) {
                    if (!(chatId instanceof GroupId)) {
                        List<Contact> users = contactsDb.getFriends();
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
        refreshFeedPrivacy();
        feedPrivacyManager.addObserver(feedPrivacyObserver);
    }

    public void setDestinationFeed(@Nullable GroupId groupId) {
        this.targetGroupId = groupId;
        shareTargetName.invalidate();
    }

    public @PrivacyList.Type String getPrivacyList() {
        return feedTarget;
    }

    public void setPrivacyList(@NonNull @PrivacyList.Type String privacyList) {
        this.feedTarget = privacyList;
        FeedPrivacy privacy = feedPrivacyLiveData.getValue();
        if (privacy != null) {
            privacy.activeList = privacyList;
        } else {
            refreshFeedPrivacy();
        }
    }

    public List<ShareDestination> getDestinationsList() {
        return destinationList.getValue();
    }

    public void setDestinationsList(List<ShareDestination> destinations) {
        destinationList.postValue(destinations);
    }

    private void refreshFeedPrivacy() {
        bgWorkers.execute(this::loadFeedPrivacy);
    }

    @WorkerThread
    private void loadFeedPrivacy() {
        FeedPrivacy feedPrivacy = feedPrivacyManager.getFeedPrivacy();
        if (feedPrivacy == null) {
            feedPrivacy = new FeedPrivacy(feedTarget, null, null);
        } else {
            feedPrivacy = new FeedPrivacy(feedTarget, feedPrivacy.exceptList, feedPrivacy.onlyList);
        }
        feedPrivacyLiveData.postValue(feedPrivacy);
    }

    @Override
    protected void onCleared() {
        if (shouldDeleteTempMedia) {
            cleanTmpFiles();
        }
        if (shouldDeleteVoiceDraft) {
            deleteDraft();
        }
        contactsDb.removeObserver(contactsObserver);
        feedPrivacyManager.removeObserver(feedPrivacyObserver);
        voiceNoteRecorder.onCleared();
        voiceNotePlayer.onCleared();
    }

    @Nullable List<EditMediaPair> getEditMedia() {
        return editMedia.getValue();
    }

    public LiveData<FeedPrivacy> getFeedPrivacy() {
        return feedPrivacyLiveData;
    }

    void loadUris(@NonNull Collection<Uri> uris, @Nullable Bundle editStates) {
        isLoadingMedia.setValue(true);
        new LoadContentUrisTask(getApplication(), uris, editStates, editMedia, loadingItem, isLoadingMedia, hasMediaLoadFailure).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void prepareContent(@Nullable ChatId chatId, @Nullable GroupId groupFeedGroupId, @Nullable String text, @Nullable List<Mention> mentions, boolean supportsWideColor) {
        ArrayList<ShareDestination> destinations = null;
        if (destinationList.getValue() != null) {
            destinations = new ArrayList<>(destinationList.getValue());
        }

        boolean isFavorites = false;
        if (destinations != null) {
            for (ShareDestination destination : destinations) {
                if (destination.type == ShareDestination.TYPE_FAVORITES) {
                    isFavorites = true;
                    break;
                }
            }
        }

        FeedPrivacy feedPrivacy = feedPrivacyLiveData.getValue();
        if (feedPrivacy != null) {
            feedPrivacy = new FeedPrivacy(isFavorites ? PrivacyList.Type.ONLY : PrivacyList.Type.ALL, feedPrivacy.exceptList, feedPrivacy.onlyList);
        }

        new PrepareContentTask(chatId, groupFeedGroupId, feedPrivacy, destinations, text, getSendMediaList(), mentions, contentItems, replyPostId, replyPostMediaIndex, !supportsWideColor).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    public void onDestinationRemoved(ShareDestination removedDest) {
        List<ShareDestination> destinations = destinationList.getValue();
        if (destinations == null) {
            return;
        }

        ArrayList<ShareDestination> updated = new ArrayList<>(destinations);
        updated.remove(removedDest);

        destinationList.postValue(updated);
    }

    public void setDeleteTempFilesOnCleanup(boolean shouldDeleteTempMedia, boolean shouldDeleteVoiceDraft) {
        Log.d("ContentComposerViewModel: shouldDeleteTempMedia=" + shouldDeleteTempMedia + " shouldDeleteVoiceDraft=" + shouldDeleteVoiceDraft);
        this.shouldDeleteTempMedia = shouldDeleteTempMedia;
        this.shouldDeleteVoiceDraft = shouldDeleteVoiceDraft;
    }

    private @Nullable List<Media> getSendMediaList() {
        final List<EditMediaPair> mediaPairList = getEditMedia();
        if (voiceDraft == null && mediaPairList == null) {
            return null;
        }
        final List<Media> sendmediaPairList = new ArrayList<>();
        if (voiceDraft != null) {
            sendmediaPairList.add(Media.createFromFile(Media.MEDIA_TYPE_AUDIO, voiceDraft));
        }
        if (mediaPairList != null) {
            for (EditMediaPair mediaPair : mediaPairList) {
                sendmediaPairList.add(mediaPair.getRelevantMedia());
            }
        }
        return sendmediaPairList;
    }

    public VoiceNoteRecorder getVoiceNoteRecorder() {
        return voiceNoteRecorder;
    }

    public VoiceNotePlayer getVoiceNotePlayer() {
        return voiceNotePlayer;
    }

    public void finishRecording() {
        voiceDraft = voiceNoteRecorder.finishRecording();
    }

    public File getVoiceDraft() {
        return voiceDraft;
    }

    public void deleteDraft() {
        Log.d("ContentComposerViewModel: deleteDraft");
        if (voiceDraft != null) {
            final File draft = voiceDraft;
            voiceDraft = null;
            BgWorkers.getInstance().execute(() -> {
                draft.delete();
            });
        }
    }

    private void copyVoiceDraftUri(@NonNull Uri voiceDraftUri) {
        isLoadingVoiceDraft.setValue(true);
        bgWorkers.execute(() -> loadVoiceDraftUri(voiceDraftUri));
    }

    @WorkerThread
    private void loadVoiceDraftUri(@NonNull Uri uri) {
        final File fileDestination = FileStore.getInstance().getTmpFileForUri(uri, null);
        try {
            if (!fileDestination.exists()) {
                final boolean isLocalFile = Objects.equals(uri.getScheme(), "file");
                if (isLocalFile) {
                    final File sourceFile = new File(uri.getPath());
                    FileUtils.copyFile(sourceFile, fileDestination);
                } else {
                    FileUtils.uriToFile(getApplication(), uri, fileDestination);
                }
            }
            voiceDraft = fileDestination;
        } catch (IOException e) {
            Log.e("ContentComposerViewModel.loadVoiceDraftUri: " + uri, e);
            fileDestination.delete();
        } finally {
            isLoadingVoiceDraft.postValue(false);
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final ChatId chatId;
        private final GroupId groupFeedId;
        private final Collection<Uri> uris;
        private final Bundle editStates;
        private final Uri voiceDraftUri;
        private final List<ShareDestination> destinations;
        private final String replyId;
        private final int replyPostMediaIndex;

        Factory(@NonNull Application application, @Nullable ChatId chatId, @Nullable GroupId groupFeedId, @Nullable Collection<Uri> uris, @Nullable Bundle editStates, @Nullable Uri voiceDraftUri, @Nullable List<ShareDestination> destinations, @Nullable String replyId, int replyPostMediaIndex) {
            this.application = application;
            this.chatId = chatId;
            this.groupFeedId = groupFeedId;
            this.uris = uris;
            this.editStates = editStates;
            this.voiceDraftUri = voiceDraftUri;
            this.destinations = destinations;
            this.replyId = replyId;
            this.replyPostMediaIndex = replyPostMediaIndex;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ContentComposerViewModel.class)) {
                //noinspection unchecked
                return (T) new ContentComposerViewModel(application, chatId, groupFeedId, uris, editStates, voiceDraftUri, destinations, replyId, replyPostMediaIndex);
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
        private final MutableLiveData<Boolean> isLoadingMedia;
        private final MutableLiveData<Boolean> hasMediaLoadFailure;
        private final int expectedMediaCount;

        final private long createTime;

        LoadContentUrisTask(@NonNull Application application,
                            @NonNull Collection<Uri> uris,
                            @Nullable Bundle editStates,
                            @NonNull MutableLiveData<List<EditMediaPair>> media,
                            @NonNull MutableLiveData<EditMediaPair> loadingItem,
                            @NonNull MutableLiveData<Boolean> isLoadingMedia,
                            @NonNull MutableLiveData<Boolean> hasMediaLoadFailure) {
            this.application = application;
            this.uris = uris;
            this.editStates = editStates;
            this.media = media;
            this.loadingItem = loadingItem;
            this.createTime = System.currentTimeMillis();
            this.isLoadingMedia = isLoadingMedia;
            this.hasMediaLoadFailure = hasMediaLoadFailure;
            this.expectedMediaCount = uris.size();
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
                    final boolean isLocalFile = Objects.equals(uri.getScheme(), "file");
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
                final Size editSize = editFile.exists() && editFile.length() > 0 ? MediaUtils.getDimensions(editFile, mediaType) : null;

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
            this.isLoadingMedia.postValue(false);
            final int loadedMediaCount = mediaPairList != null ? mediaPairList.size() : 0;
            hasMediaLoadFailure.postValue(loadedMediaCount != expectedMediaCount);
        }
    }

    static class PrepareContentTask extends AsyncTask<Void, Void, Void> {

        private final ContentDb contentDb = ContentDb.getInstance();
        private final ContactsDb contactsDb = ContactsDb.getInstance();
        private final FeedPrivacyManager feedPrivacyManager = FeedPrivacyManager.getInstance();

        private final ChatId chatId;
        private final GroupId groupId;
        private final List<ShareDestination> destinations;
        private final String text;
        private final List<Media> media;
        private final List<Mention> mentions;
        private final MutableLiveData<List<ContentItem>> contentItems;
        private final String replyPostId;
        private final int replyPostMediaIndex;
        private final boolean forcesRGB;
        private final FeedPrivacy feedPrivacy;

        PrepareContentTask(
                @Nullable ChatId chatId,
                @Nullable GroupId groupId,
                @Nullable FeedPrivacy feedPrivacy,
                @Nullable List<ShareDestination> destinations,
                @Nullable String text, @Nullable List<Media> media,
                @Nullable List<Mention> mentions,
                @NonNull MutableLiveData<List<ContentItem>> contentItems,
                @Nullable String replyPostId,
                int replyPostMediaIndex,
                boolean forcesRGB) {
            this.chatId = chatId;
            this.groupId = groupId;
            this.feedPrivacy = feedPrivacy;
            this.destinations = destinations;
            this.text = text;
            this.media = media;
            this.mentions = mentions;
            this.contentItems = contentItems;
            this.replyPostId = replyPostId;
            this.replyPostMediaIndex = replyPostMediaIndex;
            this.forcesRGB = forcesRGB;
        }

        private ContentItem createContentItem(@Nullable ChatId chatId, @Nullable GroupId groupId, @Nullable Post replyPost) {
            if (chatId == null && media != null && media.size() >= 1 && media.get(0).type == Media.MEDIA_TYPE_AUDIO) {
                Post post = new VoiceNotePost(0, UserId.ME, RandomId.create(), System.currentTimeMillis(), Post.TRANSFERRED_NO, Post.SEEN_YES);

                if (groupId != null) {
                    post.setParentGroup(groupId);
                } else {
                    post.setCommentKey(generateCommentKey());
                }

                return post;
            } else if (chatId != null) {
                return new Message(0, chatId, UserId.ME, RandomId.create(), System.currentTimeMillis(), Message.TYPE_CHAT, Message.USAGE_CHAT, Message.STATE_INITIAL, text, replyPostId, replyPostMediaIndex, null, -1, replyPost == null ? null : replyPost.senderUserId, 0);
            } else {
                Post post = new Post(0, UserId.ME, RandomId.create(), System.currentTimeMillis(), Post.TRANSFERRED_NO, Post.SEEN_YES, text);

                if (groupId != null) {
                    post.setParentGroup(groupId);
                } else {
                    post.setCommentKey(generateCommentKey());
                }

                return post;
            }
        }

        private byte[] generateCommentKey() {
            boolean favorites = feedPrivacy != null && feedPrivacy.activeList.equals(PrivacyList.Type.ONLY);
            byte[] chainKey = EncryptedKeyStore.getInstance().getMyHomeChainKey(favorites);
            try {
                return CryptoUtils.hkdf(chainKey, null, new byte[] {0x07}, 64);
            } catch (GeneralSecurityException e) {
                Log.e("Failed to compute comment key", e);
                throw new IllegalStateException(e);
            }
        }

        private List<ContentItem> createContentItems() {
            ArrayList<ContentItem> items = new ArrayList<>();
            Post replyPost = replyPostId == null ? null : contentDb.getPost(replyPostId);

            if (destinations != null && destinations.size() > 0) {
                for (ShareDestination dest: destinations) {
                    if (dest.type == ShareDestination.TYPE_CONTACT) {
                        items.add(createContentItem(dest.id, null, replyPost));
                    } else if (dest.type == ShareDestination.TYPE_GROUP) {
                        items.add(createContentItem(null, (GroupId) dest.id, null));
                    } else {
                        items.add(createContentItem(null, null, null));
                    }
                }
            } else {
                items.add(createContentItem(chatId, groupId, replyPost));
            }

            return items;
        }

        private boolean prepareMedia(List<ContentItem> items) {
            if (media == null) {
                return true;
            }

            for (Media mediaItem : media) {
                final File postFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(mediaItem.type));
                switch (mediaItem.type) {
                    case Media.MEDIA_TYPE_IMAGE: {
                        try {
                            MediaUtils.transcodeImage(mediaItem.file, postFile, null, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY, forcesRGB);
                        } catch (IOException e) {
                            Log.e("failed to transcode image", e);
                            return false;
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_AUDIO:
                    case Media.MEDIA_TYPE_VIDEO: {
                        if (!mediaItem.file.renameTo(postFile)) {
                            Log.e("failed to rename " + mediaItem.file.getAbsolutePath() + " to " + postFile.getAbsolutePath());
                            return false;
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_UNKNOWN:
                    default: {
                        Log.e("unknown media type " + mediaItem.file.getAbsolutePath());
                        return false;
                    }
                }

                Media sendMedia = Media.createFromFile(mediaItem.type, postFile);
                for (ContentItem item : items) {
                    item.media.add(new Media(sendMedia));
                }
            }

            return true;
        }

        private void setPrivacy(List<ContentItem> items) {
            @PrivacyList.Type String audienceType;
            List<UserId> audienceList;
            List<UserId> excludeList = null;

            if (feedPrivacy == null || PrivacyList.Type.ALL.equals(feedPrivacy.activeList)) {
                List<Contact> contacts = contactsDb.getFriends();
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
                List<Contact> contacts = contactsDb.getFriends();
                audienceList = new ArrayList<>(contacts.size());
                for (Contact contact : contacts) {
                    if (!excludedSet.contains(contact.userId)) {
                        audienceList.add(contact.userId);
                    }
                }

                excludeList = feedPrivacy.exceptList;
            }

            for (ContentItem item : items) {
                if (item instanceof Post) {
                    Post post = (Post) item;

                    if (post.getParentGroup() == null) {
                        post.setExcludeList(excludeList);
                        post.setAudience(audienceType, audienceList);
                    }
                }
            }
        }

        private void setChunkedUploadsOnTestGroups(List<ContentItem> items) {
            boolean streamingSendingEnabled = ServerProps.getInstance().getStreamingSendingEnabled();

            List<GroupId> testChunkGroups = Arrays.asList(new GroupId("gmYchx3MBOXerd7QTmWqsO"), new GroupId("gGSFDZYubalo4izDKhE-Vv"));
            for (ContentItem item : items) {
                if (item instanceof Post) {
                    Post post = (Post) item;
                    GroupId groupId = post.getParentGroup();

                    if (streamingSendingEnabled || (groupId != null && testChunkGroups.contains(groupId))) {
                        for (Media mediaItem : post.media) {
                            if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
                                mediaItem.blobVersion = Media.BLOB_VERSION_CHUNKED;
                            }
                        }
                    }
                }
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<ContentItem> items = createContentItems();

            if (!prepareMedia(items)) {
                return null;
            }

            if (mentions != null) {
                for (ContentItem item : items) {
                    item.mentions.addAll(mentions);
                }
            }

            setPrivacy(items);
            setChunkedUploadsOnTestGroups(items);

            contentItems.postValue(items);
            return null;
        }
    }

    static class EditMediaPair {
        final Uri uri;
        final Media original;
        final Media edit;
        final Parcelable state;

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
