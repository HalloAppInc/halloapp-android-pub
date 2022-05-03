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
import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.content.VoiceNotePost;
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
import com.halloapp.proto.clients.Moment;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MomentComposerViewModel extends AndroidViewModel {
    final MutableLiveData<List<EditMediaPair>> editMedia = new MutableLiveData<>();
    final MutableLiveData<EditMediaPair> loadingItem = new MutableLiveData<>();

    final MutableLiveData<List<ContentItem>> contentItems = new MutableLiveData<>();

    private boolean shouldDeleteTempFiles = true;


    MomentComposerViewModel(@NonNull Application application, @NonNull Uri uri) {
        super(application);

        loadUris(uri);
    }

    @Override
    protected void onCleared() {
        if (shouldDeleteTempFiles) {
            cleanTmpFiles();
        }
    }

    @Nullable List<EditMediaPair> getEditMedia() {
        return editMedia.getValue();
    }

    void loadUris(@NonNull Uri uri) {
        new LoadContentUrisTask(getApplication(), uri, editMedia, loadingItem).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void prepareContent(boolean supportsWideColor) {
        ArrayList<ShareDestination> destinations = null;
        new PrepareContentTask(getSendMediaList(), contentItems,!supportsWideColor).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void cleanTmpFiles() {
        Log.d("ContentComposerViewModel: cleanTmpFiles");
        final List<EditMediaPair> mediaPairList = editMedia.getValue();
        if (mediaPairList != null) {
            new CleanupTmpFilesTask(mediaPairList).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        private final Uri uri;

        Factory(@NonNull Application application, @NonNull Uri uri) {
            this.application = application;
            this.uri = uri;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MomentComposerViewModel.class)) {
                //noinspection unchecked
                return (T) new MomentComposerViewModel(application, uri);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    static class LoadContentUrisTask extends AsyncTask<Void, EditMediaPair, List<EditMediaPair>> {

        private final Uri uri;
        private final Application application;
        private final MutableLiveData<List<EditMediaPair>> media;
        private final MutableLiveData<EditMediaPair> loadingItem;

        final private long createTime;

        LoadContentUrisTask(@NonNull Application application,
                            @NonNull Uri uri,
                            @NonNull MutableLiveData<List<EditMediaPair>> media,
                            @NonNull MutableLiveData<EditMediaPair> loadingItem) {
            this.application = application;
            this.uri = uri;
            this.media = media;
            this.loadingItem = loadingItem;
            this.createTime = System.currentTimeMillis();
        }

        @Override
        protected List<EditMediaPair> doInBackground(Void... voids) {
            final List<EditMediaPair> mediaPairList = new ArrayList<>();

            final FileStore fileStore = FileStore.getInstance();
            int uriIndex = 0;
            Collection<Uri> uris = Collections.singleton(uri);
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

                    final Parcelable state = null;
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

        private final List<Media> media;
        private final MutableLiveData<List<ContentItem>> contentItems;
        private final boolean forcesRGB;

        PrepareContentTask(
             @Nullable List<Media> media,
                @NonNull MutableLiveData<List<ContentItem>> contentItems,
                boolean forcesRGB) {
            this.media = media;
            this.contentItems = contentItems;
            this.forcesRGB = forcesRGB;
        }

        private ContentItem createContentItem() {
            return new MomentPost(0, UserId.ME, RandomId.create(), System.currentTimeMillis(), Post.TRANSFERRED_NO, Post.SEEN_YES, null);
        }

        private List<ContentItem> createContentItems() {
            ArrayList<ContentItem> items = new ArrayList<>();
            items.add(createContentItem());


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

            List<Contact> contacts = contactsDb.getUsers();
            audienceList = new ArrayList<>(contacts.size());
            for (Contact contact : contacts) {
                audienceList.add(contact.userId);
            }
            audienceType = PrivacyList.Type.ALL;

            for (ContentItem item : items) {
                if (item instanceof Post) {
                    Post post = (Post) item;

                    if (post.getParentGroup() == null) {
                        post.setAudience(audienceType, audienceList);
                    }
                }
            }
        }

        private void setChunkedUploadsOnTestGroups(List<ContentItem> items) {
            if (!ServerProps.getInstance().getStreamingSendingEnabled()) {
                return;
            }

            List<GroupId> testChunkGroups = Arrays.asList(new GroupId("gmYchx3MBOXerd7QTmWqsO"), new GroupId("gGSFDZYubalo4izDKhE-Vv"));
            for (ContentItem item : items) {
                if (item instanceof Post) {
                    Post post = (Post) item;
                    GroupId groupId = post.getParentGroup();

                    if (groupId != null && testChunkGroups.contains(groupId)) {
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
