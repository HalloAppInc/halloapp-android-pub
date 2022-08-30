package com.halloapp.ui;

import android.app.Application;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.MomentManager;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.media.VoiceNoteRecorder;
import com.halloapp.ui.chat.ChatViewModel;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

import java.io.File;

public class MomentViewerViewModel extends AndroidViewModel {

    final ComputableLiveData<Post> post;
    final ComputableLiveData<Post> unlockingMoment;

    private final String postId;
    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final MomentManager momentManager;

    private LiveData<Boolean> unlockLiveData;

    private boolean loaded;
    private boolean uncovered;

    private final VoiceNoteRecorder voiceNoteRecorder;
    private final VoiceNotePlayer voiceNotePlayer;

    private MomentViewerViewModel(@NonNull Application application, @NonNull String postId) {
        super(application);
        this.postId = postId;

        bgWorkers = BgWorkers.getInstance();
        contentDb = ContentDb.getInstance();
        momentManager = MomentManager.getInstance();

        post = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                return contentDb.getPost(postId);
            }
        };

        unlockingMoment = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                String postId = contentDb.getUnlockingMomentId();
                if (postId != null) {
                    return contentDb.getPost(postId);
                }
                return null;
            }
        };
        contentDb.addObserver(new ContentDb.DefaultObserver() {

            @Override
            public void onPostAdded(@NonNull Post post) {
                if (post.type == Post.TYPE_MOMENT) {
                    unlockingMoment.invalidate();
                }
            }

            @Override
            public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
                if (unlockingMoment.getLiveData().getValue() == null || postId.equals(unlockingMoment.getLiveData().getValue().id)) {
                    unlockingMoment.invalidate();
                } else if (postId.equals(MomentViewerViewModel.this.postId)) {
                    post.invalidate();
                }
            }
        });
        voiceNoteRecorder = new VoiceNoteRecorder();
        voiceNotePlayer = new VoiceNotePlayer(application);
    }

    public VoiceNotePlayer getVoiceNotePlayer() {
        return voiceNotePlayer;
    }

    public VoiceNoteRecorder getVoiceNoteRecorder() {
        return voiceNoteRecorder;
    }

    public void sendVoiceNote(@Nullable File recording) {
        if (recording == null) {
            return;
        }
        final Post replyPost = post.getLiveData().getValue();
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

    public void onScreenshotted() {
        Post moment = post.getLiveData().getValue();
        if (moment == null || moment.senderUserId.isMe()) {
            Log.i("MomentViewerViewModel/onScreenshotted null moment or is my moment");
            return;
        }
        if (!loaded || !uncovered) {
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

    @Override
    protected void onCleared() {
        if (loaded && uncovered) {
            Post moment = post.getLiveData().getValue();
            if (moment != null && moment.isIncoming() && moment.isAllMediaTransferred()) {
                contentDb.hideMomentOnView(moment);
            }
        }
    }

    public void sendMessage(String text) {
        Post moment = post.getLiveData().getValue();
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
        loaded = true;
        if (uncovered) {
            sendSeenReceipt();
        }
    }

    private void sendSeenReceipt() {
        Post moment = post.getLiveData().getValue();
        if (moment == null || moment.isOutgoing()) {
            Log.e("MomentViewerViewModel/sendSeenReceipt no post");
            return;
        }
        contentDb.setIncomingPostSeen(moment.senderUserId, moment.id, null);
    }

    public void setUncovered() {
        uncovered = true;
        if (loaded) {
            sendSeenReceipt();
        }
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
