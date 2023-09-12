package com.halloapp.ui;

import android.annotation.SuppressLint;
import android.app.Application;

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
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.media.VoiceNoteRecorder;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MomentViewerViewModel extends AndroidViewModel {

    final ComputableLiveData<Post> unlockingMoment;
    private final MutableLiveData<Post> currentMoment = new MutableLiveData<>();
    private final MutableLiveData<Post> nextMoment = new MutableLiveData<>();

    private int position = 0;
    private String postId;
    private final List<Post> moments = new ArrayList<>();
    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;

    private final Set<Post> loaded = new HashSet<>();
    private final Set<Post> uncovered = new HashSet<>();

    private final VoiceNoteRecorder voiceNoteRecorder;
    private final VoiceNotePlayer voiceNotePlayer;

    private boolean initializing = true;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.type == Post.TYPE_MOMENT) {
                unlockingMoment.invalidate();
                fetchMoments();
            }
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            for (Post moment : moments) {
                if (postId.equals(moment.id)) {
                    fetchMoments();
                    break;
                }
            }

            if (unlockingMoment.getLiveData().getValue() == null || postId.equals(unlockingMoment.getLiveData().getValue().id)) {
                unlockingMoment.invalidate();
            }
        }
    };

    private MomentViewerViewModel(@NonNull Application application, @NonNull String postId) {
        super(application);
        this.postId = postId;

        bgWorkers = BgWorkers.getInstance();
        contentDb = ContentDb.getInstance();

        unlockingMoment = new ComputableLiveData<Post>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected Post compute() {
                String postId = contentDb.getUnlockingMomentId();
                if (postId != null) {
                    return contentDb.getPost(postId);
                }
                return null;
            }
        };

        contentDb.addObserver(contentObserver);
        voiceNoteRecorder = new VoiceNoteRecorder();
        voiceNotePlayer = new VoiceNotePlayer(application);

        bgWorkers.execute(this::fetchMoments);
    }

    public VoiceNotePlayer getVoiceNotePlayer() {
        return voiceNotePlayer;
    }

    public VoiceNoteRecorder getVoiceNoteRecorder() {
        return voiceNoteRecorder;
    }

    public LiveData<Post> getCurrent() {
        return currentMoment;
    }

    public LiveData<Post> getNext() {
        return nextMoment;
    }

    public void sendVoiceNote(@Nullable File recording) {
        if (recording == null) {
            return;
        }
        final Post replyPost = currentMoment.getValue();
        if (replyPost != null) {
            bgWorkers.execute(() -> {
                if (MediaUtils.getAudioDuration(recording) < Constants.MINIMUM_AUDIO_NOTE_DURATION_MS) {
                    Log.i("ChatViewModel/sendVoiceNote duration too short");
                    return;
                }
                final File targetFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_AUDIO));
                if (!recording.renameTo(targetFile)) {
                    Log.e("failed to rename " + recording.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
                    return;
                }
                final ContentItem contentItem = new Message(0,
                        replyPost.senderUserId,
                        UserId.ME,
                        RandomId.create(),
                        System.currentTimeMillis(),
                        Message.TYPE_VOICE_NOTE,
                        Message.USAGE_CHAT,
                        Message.STATE_INITIAL,
                        null,
                        replyPost.id,
                        0,
                       null,
                        -1,
                        null,
                        0);

                final Media sendMedia = Media.createFromFile(Media.MEDIA_TYPE_AUDIO, targetFile);
                contentItem.media.add(sendMedia);
                contentItem.addToStorage(contentDb);
            });
        }
    }

    @WorkerThread
    private void fetchMoments() {
        moments.clear();

        moments.addAll(contentDb.getMoments());

        position = 0;
        for (int i = 0; i < moments.size(); i++) {
            if (moments.get(i).id.equals(MomentViewerViewModel.this.postId)) {
                position = i;
                break;
            }
        }

        if (position < moments.size()) {
            MomentViewerViewModel.this.postId = moments.get(position).id;

            currentMoment.postValue(moments.get(position));

            if (moments.size() > 1) {
                nextMoment.postValue(moments.get((position + 1) % moments.size()));
            }
        }
    }

    public void onScreenshotted() {
        Post moment = currentMoment.getValue();
        if (moment == null || moment.senderUserId.isMe()) {
            Log.i("MomentViewerViewModel/onScreenshotted null moment or is my moment");
            return;
        }
        if (!loaded.contains(moment) || !uncovered.contains(moment)) {
            Log.i("MomentViewerViewModel/onScreenshotted moment not loaded yet, not sending screenshot notice");
            return;
        }
        if (moment instanceof MomentPost) {
            MomentPost momentPost = (MomentPost) moment;
            if (momentPost.screenshotted == MomentPost.SCREENSHOT_NO) {
                momentPost.screenshotted = MomentPost.SCREENSHOT_YES_PENDING;
                contentDb.setIncomingMomentScreenshotted(momentPost.senderUserId, momentPost.id);
            }
        }
    }

    public void finishRecording(boolean canceled) {
        File recording = voiceNoteRecorder.finishRecording();
        if (canceled || recording == null) {
            return;
        }
        sendVoiceNote(recording);
    }

    public int getMomentCount() {
        return moments.size();
    }

    public void moveToNext() {
        if (moments.size() > 1) {
            position = (position + 1) % moments.size();
            postId = moments.get(position).id;

            currentMoment.postValue(moments.get(position));
            nextMoment.postValue(moments.get((position + 1) % moments.size()));
        }
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
    }

    public void sendMessage(String text) {
        Post moment = currentMoment.getValue();
        if (moment == null) {
            Log.e("MomentViewerViewModel/sendMessage no such moment to reply to");
            return;
        }
        Message msg = new Message(0,
                moment.senderUserId,
                UserId.ME,
                RandomId.create(),
                System.currentTimeMillis(),
                Message.TYPE_CHAT,
                Message.USAGE_CHAT,
                Message.STATE_INITIAL,
                text,
                moment.id,
                0,
                null,
                -1 ,
                null,
                0);
        msg.addToStorage(contentDb);
    }

    public void setLoaded() {
        Post moment = currentMoment.getValue();
        loaded.add(moment);
        if (uncovered.contains(moment)) {
            sendSeenReceipt();
        }
    }

    private void sendSeenReceipt() {
        Post moment = currentMoment.getValue();

        if (moment == null) {
            Log.e("MomentViewerViewModel/sendSeenReceipt no post");
            return;
        }

        if (moment.isIncoming()) {
            contentDb.setIncomingPostSeen(moment.senderUserId, moment.id, null);
        }
    }

    public void setUncovered() {
        Post moment = currentMoment.getValue();
        if (loaded.contains(moment)) {
            uncovered.add(moment);
            sendSeenReceipt();
        }
    }

    public void setInitializing(boolean initializing) {
        this.initializing = initializing;
    }

    public boolean isInitializing() {
        return initializing;
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final String postId;
        private final Application application;

        Factory(@NonNull Application application, @NonNull String postId) {
            this.application = application;
            this.postId = postId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MomentViewerViewModel.class)) {
                //noinspection unchecked
                return (T) new MomentViewerViewModel(application, postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
