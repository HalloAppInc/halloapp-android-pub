package com.halloapp.ui;

import android.app.Application;
import android.content.ContentResolver;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentComposerViewModel extends AndroidViewModel {

    final MutableLiveData<List<Media>> media = new MutableLiveData<>();
    final MutableLiveData<ContentItem> contentItem = new MutableLiveData<>();
    final ComputableLiveData<String> chatName;

    final Map<File, Parcelable> cropStates = new HashMap<>();


    ContentComposerViewModel(@NonNull Application application, @Nullable String chatId, @Nullable Collection<Uri> uris) {
        super(application);
        if (uris != null) {
            loadUris(uris);
        }
        if (chatId != null) {
            chatName = new ComputableLiveData<String>() {
                @Override
                protected String compute() {
                    return ContactsDb.getInstance(application).getContact(new UserId(chatId)).getDisplayName();
                }
            };
        } else {
            chatName = null;
        }
    }

    protected void onCleared() {
        final List<Media> mediaList = media.getValue();
        if (mediaList != null) {
            new ContentComposerActivity.CleanupTmpFilesTask(mediaList).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    List<Media> getMedia() {
        return media.getValue();
    }

    public static File getCropFile(@NonNull File file) {
        return new File(file.getAbsolutePath() + "-crop");
    }

    private void loadUris(@NonNull Collection<Uri> uris) {
        new LoadContentUrisTask(getApplication(), uris, media).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void prepareContent(@Nullable String chatId, @Nullable String text) {

        final List<Media> mediaList = getMedia();
        final List<Media> sendMediaList = new ArrayList<>();
        for (Media media : mediaList) {
            final Parcelable cropState = cropStates.get(media.file);
            final Media sendMedia;
            if (cropState != null) {
                sendMedia = new Media(0, media.type, null, getCropFile(media.file), null, null, 0, 0, Media.TRANSFERRED_NO);
            } else {
                sendMedia = media;
            }
            sendMediaList.add(sendMedia);
        }
        new PrepareContentTask(getApplication(), chatId, text, sendMediaList, contentItem).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final String chatId;
        private final Collection<Uri> uris;

        Factory(@NonNull Application application, @Nullable String chatId, @Nullable Collection<Uri> uris) {
            this.application = application;
            this.chatId = chatId;
            this.uris = uris;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ContentComposerViewModel.class)) {
                //noinspection unchecked
                return (T) new ContentComposerViewModel(application, chatId, uris);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    static class LoadContentUrisTask extends AsyncTask<Void, Void, List<Media>> {

        private final Collection<Uri> uris;
        private final Application application;
        private final MutableLiveData<List<Media>> media;

        LoadContentUrisTask(@NonNull Application application, @NonNull Collection<Uri> uris, @NonNull MutableLiveData<List<Media>> media) {
            this.application = application;
            this.uris = uris;
            this.media = media;
        }

        @Override
        protected List<Media> doInBackground(Void... voids) {
            final List<Media> media = new ArrayList<>();
            final ContentResolver contentResolver = application.getContentResolver();
            for (Uri uri : uris) {
                @Media.MediaType int mediaType = Media.getMediaType(contentResolver.getType(uri));
                final File file = FileStore.getInstance(application).getTmpFile(RandomId.create());
                FileUtils.uriToFile(application, uri, file);
                final Size size = MediaUtils.getDimensions(file, mediaType);
                if (size != null) {
                    final Media mediaItem = Media.createFromFile(mediaType, file);
                    mediaItem.width = size.getWidth();
                    mediaItem.height = size.getHeight();
                    media.add(mediaItem);
                } else {
                    Log.e("PostComposerActivity: failed to load " + uri);
                }
            }
            return media;
        }

        @Override
        protected void onPostExecute(List<Media> media) {
            this.media.postValue(media);
        }
    }

    static class PrepareContentTask extends AsyncTask<Void, Void, Void> {

        private final String chatId;
        private final String text;
        private final List<Media> media;
        private final Application application;
        private final MutableLiveData<ContentItem> contentItem;

        PrepareContentTask(@NonNull Application application, @Nullable String chatId, @Nullable String text, @Nullable List<Media> media, @NonNull MutableLiveData<ContentItem> contentItem) {
            this.chatId = chatId;
            this.application = application;
            this.text = text;
            this.media = media;
            this.contentItem = contentItem;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            final ContentItem contentItem = chatId == null ?
                    new Post(0, UserId.ME, RandomId.create(), System.currentTimeMillis(),Post.TRANSFERRED_NO, Post.SEEN_YES, text) :
                    new Message(0, chatId, UserId.ME, RandomId.create(), System.currentTimeMillis(), Message.STATE_INITIAL, text, null, -1);
            if (media != null) {
                for (Media media : media) {
                    final File postFile = FileStore.getInstance(application).getMediaFile(RandomId.create() + "." + Media.getFileExt(media.type));
                    switch (media.type) {
                        case Media.MEDIA_TYPE_IMAGE: {
                            try {
                                RectF cropRect = null;
                                if (media.height > Constants.MAX_IMAGE_ASPECT_RATIO * media.width) {
                                    final float padding = (media.height - Constants.MAX_IMAGE_ASPECT_RATIO * media.width) / 2;
                                    cropRect = new RectF(0, padding / media.height, 1, 1 - padding / media.height);
                                }
                                MediaUtils.transcodeImage(media.file, postFile, cropRect, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY);
                            } catch (IOException e) {
                                Log.e("failed to transcode image", e);
                                return null;
                            }
                            break;
                        }
                        case Media.MEDIA_TYPE_VIDEO: {
                            if (!media.file.renameTo(postFile)) {
                                Log.e("failed to rename " + media.file.getAbsolutePath() + " to " + postFile.getAbsolutePath());
                                return null;
                            }
                            break;
                        }
                        case Media.MEDIA_TYPE_UNKNOWN:
                        default: {
                            Log.e("unknown media type " + media.file.getAbsolutePath());
                            return null;
                        }
                    }
                    final Media sendMedia = Media.createFromFile(media.type, postFile);
                    contentItem.media.add(sendMedia);
                }
            }
            this.contentItem.postValue(contentItem);
            return null;
        }
    }
}
