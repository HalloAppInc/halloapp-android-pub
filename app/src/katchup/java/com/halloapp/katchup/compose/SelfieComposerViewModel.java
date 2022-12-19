package com.halloapp.katchup.compose;

import android.net.Uri;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.crypto.CryptoUtils;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class SelfieComposerViewModel extends ViewModel {

    public SelfieComposerViewModel() {
        startTime = System.currentTimeMillis();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ComposeState.COMPOSING_CONTENT, ComposeState.COMPOSING_SELFIE, ComposeState.READY_TO_SEND})
    public @interface ComposeState {
        int COMPOSING_CONTENT = 0;
        int COMPOSING_SELFIE = 1;
        int READY_TO_SEND = 2;
    }

    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    private final MutableLiveData<Integer> currentState = new MutableLiveData<>(ComposeState.COMPOSING_CONTENT);

    private File selfieFile;

    public LiveData<Integer> getComposerState() {
        return currentState;
    }

    private float selfieX;
    private float selfieY;

    private int numTakes = 0;
    private int numSelfieTakes = 0;
    private long notificationTime;
    private long notificationId;
    private long startTime;

    public void setNotification(long notificationId, long notificationTime) {
        this.notificationId = notificationId;
        this.notificationTime = notificationTime;
    }

    public void onCapturedSelfie(@NonNull File selfieFile) {
        this.selfieFile = selfieFile;
        currentState.setValue(ComposeState.READY_TO_SEND);
        numSelfieTakes++;
    }

    public void onDiscardSelfie() {
        if (selfieFile != null) {
            final File fileToDelete = selfieFile;
            bgWorkers.execute(() -> {
                fileToDelete.delete();
            });
        }
        this.selfieFile = null;
        currentState.setValue(ComposeState.COMPOSING_SELFIE);
    }

    public void onComposedMedia(@NonNull Uri uri, @Media.MediaType int mediaType) {
        currentState.setValue(ComposeState.COMPOSING_SELFIE);
        numTakes++;
    }

    public void onComposedText(@NonNull String text, @ColorInt int color) {
        currentState.setValue(ComposeState.COMPOSING_SELFIE);
        numTakes++;
    }

    public boolean onBackPressed() {
        switch (currentState.getValue()) {
            case ComposeState.COMPOSING_CONTENT:
                return true;
            case ComposeState.COMPOSING_SELFIE:
                currentState.setValue(ComposeState.COMPOSING_CONTENT);
                return false;
            case ComposeState.READY_TO_SEND:
                currentState.setValue(ComposeState.COMPOSING_SELFIE);
                return false;
        }
        return true;
    }

    public LiveData<KatchupPost> sendPost(@NonNull Media content) {
        MutableLiveData<KatchupPost> sendResult = new MutableLiveData<>();
        bgWorkers.execute(() -> {
            KatchupPost post = new KatchupPost(0, UserId.ME, RandomId.create(), System.currentTimeMillis(), Post.TRANSFERRED_NO, Post.SEEN_YES, "");
            post.selfieX = this.selfieX;
            post.selfieY = this.selfieY;
            post.numTakes = numTakes;
            post.numSelfieTakes = numSelfieTakes;
            post.notificationTimestamp = notificationTime;
            post.notificationId = notificationId;
            post.timeTaken = Math.max(System.currentTimeMillis() - startTime, 0);
            addMedia(post, content);
            @PrivacyList.Type String audienceType;
            List<UserId> audienceList;

            List<Contact> contacts = ContactsDb.getInstance().getUsers();
            audienceList = new ArrayList<>(contacts.size());
            for (Contact contact : contacts) {
                audienceList.add(contact.userId);
            }
            audienceType = PrivacyList.Type.ALL;
            post.setAudience(audienceType, audienceList);
            post.commentKey = generateCommentKey();
            sendResult.postValue(post);
        });
        return sendResult;
    }

    private byte[] generateCommentKey() {
        byte[] chainKey = EncryptedKeyStore.getInstance().getMyHomeChainKey(false);
        try {
            return CryptoUtils.hkdf(chainKey, null, new byte[] {0x07}, 64);
        } catch (GeneralSecurityException e) {
            Log.e("Failed to compute comment key", e);
            throw new IllegalStateException(e);
        }
    }

    public void setSelfiePosition(float x, float y) {
        Log.i("SelfieComposerViewModel/setSelfiePosition: selfie positioned at x=" + x + ", y=" + y);
        this.selfieX = x;
        this.selfieY = y;
    }

    private boolean addMedia(KatchupPost post, Media content) {
        final File selfiePostFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(content.type));
        if (!selfieFile.renameTo(selfiePostFile)) {
            Log.e("failed to rename " + content.file.getAbsolutePath() + " to " + selfiePostFile.getAbsolutePath());
            return false;
        }
        post.media.add(Media.createFromFile(Media.MEDIA_TYPE_VIDEO, selfiePostFile));

        final File postFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(content.type));
        switch (content.type) {
            case Media.MEDIA_TYPE_IMAGE: {
                try {
                    MediaUtils.transcodeImage(content.file, postFile, null, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY, true);
                } catch (IOException e) {
                    Log.e("failed to transcode image", e);
                    return false;
                }
                break;
            }
            case Media.MEDIA_TYPE_VIDEO: {
                if (!content.file.renameTo(postFile)) {
                    Log.e("failed to rename " + content.file.getAbsolutePath() + " to " + postFile.getAbsolutePath());
                    return false;
                }
                break;
            }
            case Media.MEDIA_TYPE_AUDIO:
            case Media.MEDIA_TYPE_UNKNOWN:
            default: {
                Log.e("unknown/unsupported media type " + content.file.getAbsolutePath());
                return false;
            }
        }
        content.file = postFile;
        post.media.add(content);
        return true;
    }
}
