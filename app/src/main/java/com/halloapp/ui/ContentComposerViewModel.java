package com.halloapp.ui;

import android.app.Application;
import android.content.ContentResolver;
import android.graphics.Outline;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Size;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
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
    final ComputableLiveData<Post> replyPost;
    final ComputableLiveData<List<Contact>> mentionableContacts;

    private final String replyPostId;
    private final int replyPostMediaIndex;

    private final ContentDb contentDb;
    private final ContactsDb contactsDb;
    final Map<File, Parcelable> cropStates = new HashMap<>();


    static File getCropFile(@NonNull File file) {
        return new File(file.getAbsolutePath() + "-crop");
    }

    private ContactsDb.Observer contactsObserver = new ContactsDb.Observer() {
        @Override
        public void onContactsChanged() {
            if (mentionableContacts != null) {
                mentionableContacts.invalidate();
            }
        }

        @Override
        public void onContactsReset() {

        }
    };

    ContentComposerViewModel(@NonNull Application application, @Nullable String chatId, @Nullable Collection<Uri> uris, @Nullable String replyPostId, int replyPostMediaIndex) {
        super(application);
        contentDb = ContentDb.getInstance(application);
        contactsDb = ContactsDb.getInstance(application);
        this.replyPostId = replyPostId;
        this.replyPostMediaIndex = replyPostMediaIndex;
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
        if (replyPostId != null) {
            replyPost = new ComputableLiveData<Post>() {
                @Override
                protected Post compute() {
                    return contentDb.getPost(new UserId(chatId), replyPostId);
                }
            };
        } else {
            replyPost = null;
        }
        mentionableContacts = new ComputableLiveData<List<Contact>>() {
            @Override
            protected List<Contact> compute() {
                return contactsDb.getFriends();
            }
        };
        contactsDb.addObserver(contactsObserver);
    }


    @Override
    protected void onCleared() {
        final List<Media> mediaList = media.getValue();
        if (mediaList != null) {
            new ContentComposerActivity.CleanupTmpFilesTask(mediaList).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        contactsDb.removeObserver(contactsObserver);
    }

    @Nullable List<Media> getMedia() {
        return media.getValue();
    }

    private void loadUris(@NonNull Collection<Uri> uris) {
        new LoadContentUrisTask(getApplication(), uris, media).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void prepareContent(@Nullable String chatId, @Nullable String text, @Nullable List<Mention> mentions) {
        new PrepareContentTask(getApplication(), chatId, text, getSendMediaList(), mentions, contentItem, replyPostId, replyPostMediaIndex).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private @Nullable List<Media> getSendMediaList() {
        final List<Media> mediaList = getMedia();
        if (mediaList == null) {
            return null;
        }
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
        return sendMediaList;
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final String chatId;
        private final Collection<Uri> uris;
        private final String replyId;
        private final int replyPostMediaIndex;

        Factory(@NonNull Application application, @Nullable String chatId, @Nullable Collection<Uri> uris, @Nullable String replyId, int replyPostMediaIndex) {
            this.application = application;
            this.chatId = chatId;
            this.uris = uris;
            this.replyId = replyId;
            this.replyPostMediaIndex = replyPostMediaIndex;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ContentComposerViewModel.class)) {
                //noinspection unchecked
                return (T) new ContentComposerViewModel(application, chatId, uris, replyId, replyPostMediaIndex);
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
        private final List<Mention> mentions;
        private final Application application;
        private final MutableLiveData<ContentItem> contentItem;
        private final String replyPostId;
        private final int replyPostMediaIndex;

        PrepareContentTask(@NonNull Application application, @Nullable String chatId, @Nullable String text, @Nullable List<Media> media, @Nullable List<Mention> mentions, @NonNull MutableLiveData<ContentItem> contentItem, @Nullable String replyPostId, int replyPostMediaIndex) {
            this.chatId = chatId;
            this.application = application;
            this.text = text;
            this.media = media;
            this.mentions = mentions;
            this.contentItem = contentItem;
            this.replyPostId = replyPostId;
            this.replyPostMediaIndex = replyPostMediaIndex;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            final ContentItem contentItem = chatId == null ?
                    new Post(0, UserId.ME, RandomId.create(), System.currentTimeMillis(),Post.TRANSFERRED_NO, Post.SEEN_YES, text) :
                    new Message(0, chatId, UserId.ME, RandomId.create(), System.currentTimeMillis(), Message.STATE_INITIAL, text, replyPostId, replyPostMediaIndex);
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
            if (mentions != null) {
                contentItem.mentions.addAll(mentions);
            }
            this.contentItem.postValue(contentItem);
            return null;
        }
    }
}
