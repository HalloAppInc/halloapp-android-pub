package com.halloapp.ui;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.proto.log_events.MediaComposeLoad;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MomentComposerViewModel extends AndroidViewModel {

    private final BgWorkers bgWorkers;
    private final Preferences preferences;
    private final ContactsDb contactsDb;

    final MutableLiveData<List<Media>> media = new MutableLiveData<>();
    final MutableLiveData<String> location = new MutableLiveData<>();

    final MutableLiveData<List<ContentItem>> contentItems = new MutableLiveData<>();
    final MutableLiveData<Boolean> warnedAboutReplacingMoment = new MutableLiveData<>(false);
    final ComputableLiveData<Integer> contactsCount;

    private final UserId unlockUserId;
    int selfieMediaIndex;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            contactsCount.invalidate();
        }

        @Override
        public void onFriendshipsChanged(@NonNull FriendshipInfo friendshipInfo) {
            contactsCount.invalidate();
        }
    };

    MomentComposerViewModel(@NonNull Application application, @NonNull List<Uri> uris, @Nullable UserId unlockUserId, int selfieMediaIndex) {
        super(application);
        bgWorkers = BgWorkers.getInstance();
        preferences = Preferences.getInstance();
        this.unlockUserId = unlockUserId;
        this.selfieMediaIndex = selfieMediaIndex;
        load(uris);

        contactsCount = new ComputableLiveData<Integer>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected Integer compute() {
                return contactsDb.getFriendsCount();
            }
        };

        contactsDb = ContactsDb.getInstance();
        contactsDb.addObserver(contactsObserver);

        bgWorkers.execute(() -> {
            warnedAboutReplacingMoment.postValue(preferences.getWarnedMomentsReplace());
        });
    }

    @Override
    protected void onCleared() {
        cleanTmpFiles();
        contactsDb.removeObserver(contactsObserver);
    }

    private void load(List<Uri> uris) {
        Log.d("MomentComposerViewModel: load");

        bgWorkers.execute(() -> {
            long createTime = System.currentTimeMillis();
            int numPhotos = 0;
            long totalSize = 0;
            List<Media> result = new ArrayList<>();

            for (Uri uri : uris) {
                File file = new File(uri.getPath());
                Media item = Media.createFromFile(Media.MEDIA_TYPE_IMAGE, file);

                if (item.width != 0 && item.height != 0) {
                    result.add(item);

                    numPhotos++;
                    totalSize += file.length();
                } else {
                    Log.e("MomentComposerViewModel: failed to load " + uri);
                }
            }

            Events.getInstance().sendEvent(MediaComposeLoad.newBuilder()
                    .setDurationMs((int)(System.currentTimeMillis() - createTime))
                    .setNumPhotos(numPhotos)
                    .setTotalSize((int) totalSize).build());

            media.postValue(result);
        });
    }

    public void addMedia(File file) {
        List<Media> list = media.getValue() != null ? media.getValue() : new ArrayList<>();

        bgWorkers.execute(() -> {
            Media item = Media.createFromFile(Media.MEDIA_TYPE_IMAGE, file);

            if (item.width != 0 && item.height != 0) {
                list.add(item);
                media.postValue(list);
            }
        });
    }

    void prepareContent(boolean supportsWideColor, @Nullable String psaTag) {
        new PrepareContentTask(unlockUserId, media.getValue(), contentItems, psaTag, !supportsWideColor, selfieMediaIndex, location.getValue()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    void cleanTmpFiles() {
        Log.d("MomentContentComposerViewModel: cleanTmpFiles");

        List<Media> media = this.media.getValue();
        if (media == null || media.size() == 0) {
            return;
        }

        bgWorkers.execute(() -> {
            for (Media item: media) {
                if (!item.file.delete()) {
                    Log.e("failed to delete temporary file " + item.file.getAbsolutePath());
                }
            }
        });
    }

    public void removeAdditionalMedia() {
        List<Media> list = media.getValue();
        if (list != null && list.size() > 1) {
            if (selfieMediaIndex > 0) {
                selfieMediaIndex = -1;
            }

            media.postValue(list.subList(0, 1));
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final List<Uri> uris;
        private final UserId unlockUserId;
        private final int selfieMediaIndex;

        Factory(@NonNull Application application, @NonNull List<Uri> uris, @Nullable UserId unlockUserId, int selfieMediaIndex) {
            this.application = application;
            this.uris = uris;
            this.unlockUserId = unlockUserId;
            this.selfieMediaIndex = selfieMediaIndex;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MomentComposerViewModel.class)) {
                //noinspection unchecked
                return (T) new MomentComposerViewModel(application, uris, unlockUserId, selfieMediaIndex);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    static class PrepareContentTask extends AsyncTask<Void, Void, Void> {

        private final ContactsDb contactsDb = ContactsDb.getInstance();

        private final UserId unlockedUserId;
        private final List<Media> media;
        private final MutableLiveData<List<ContentItem>> contentItems;
        private final boolean forcesRGB;
        private final String psaTag;
        private final int selfieMediaIndex;
        private final String location;

        PrepareContentTask(
                @Nullable UserId unlockedUserId,
                @Nullable List<Media> media,
                @NonNull MutableLiveData<List<ContentItem>> contentItems,
                @Nullable String psaTag,
                boolean forcesRGB,
                int selfieMediaIndex,
                String location) {
            this.media = media;
            this.contentItems = contentItems;
            this.forcesRGB = forcesRGB;
            this.psaTag = psaTag;
            this.unlockedUserId = unlockedUserId;
            this.selfieMediaIndex = selfieMediaIndex;
            this.location = location;
        }

        private ContentItem createContentItem() {
            MomentPost post = new MomentPost(0, UserId.ME, RandomId.create(), System.currentTimeMillis(), Post.TRANSFERRED_NO, Post.SEEN_YES, null);
            post.unlockedUserId = unlockedUserId;
            post.selfieMediaIndex = selfieMediaIndex;
            post.location = location;
            if (!TextUtils.isEmpty(psaTag)) {
                post.type = Post.TYPE_MOMENT_PSA;
            }
            return post;
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
                            RectF cropRect = null;
                            if (media.size() > 1) {
                                cropRect = new RectF(0.25f, 0, 0.75f, 1);
                            }

                            MediaUtils.transcodeImage(mediaItem.file, postFile, cropRect, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY, forcesRGB);
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

            List<Contact> friends = contactsDb.getFriends();
            audienceList = new ArrayList<>(friends.size());
            for (Contact friend : friends) {
                audienceList.add(friend.userId);
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

        @Override
        protected Void doInBackground(Void... voids) {
            List<ContentItem> items = createContentItems();

            if (!prepareMedia(items)) {
                return null;
            }

            if (TextUtils.isEmpty(psaTag)) {
                ContentDb.getInstance().retractCurrentMoment();
            }

            for (ContentItem item : items) {
                if (item instanceof Post) {
                    ((Post) item).psaTag = psaTag;
                }
            }

            setPrivacy(items);

            contentItems.postValue(items);
            return null;
        }
    }
}
