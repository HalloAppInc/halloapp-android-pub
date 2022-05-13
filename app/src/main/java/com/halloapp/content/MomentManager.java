package com.halloapp.content;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.id.UserId;
import com.halloapp.util.BgWorkers;

public class MomentManager {

    private static MomentManager instance;

    private BgWorkers bgWorkers;
    private ContentDb contentDb;

    public static MomentManager getInstance() {
        if (instance == null) {
            synchronized (MomentManager.class) {
                if (instance == null) {
                    instance = new MomentManager(BgWorkers.getInstance(), ContentDb.getInstance());
                }
            }
        }
        return instance;
    }

    private final MutableLiveData<Boolean> unlockLiveData = new MutableLiveData<>(false);

    private final ContentDb.Observer observer = new ContentDb.DefaultObserver() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.type == Post.TYPE_MOMENT && post.isOutgoing()) {
                invalidateUnlock();
            }
        }

        @Override
        public void onPostDeleted(@NonNull Post post) {
            if (post.type == Post.TYPE_MOMENT && post.isOutgoing()) {
                invalidateUnlock();
            }
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            if (senderUserId.isMe()) {
                invalidateUnlock();
            }
        }
    };

    private MomentManager(@NonNull BgWorkers bgWorkers, @NonNull ContentDb contentDb) {
        this.bgWorkers = bgWorkers;
        this.contentDb = contentDb;

        contentDb.addObserver(observer);

        invalidateUnlock();
    }

    public LiveData<Boolean> isUnlockedLiveData() {
        return unlockLiveData;
    }

    private void invalidateUnlock() {
        bgWorkers.execute(() -> {
            unlockLiveData.postValue(contentDb.getUnlockingMomentId() != null);
        });
    }
}
