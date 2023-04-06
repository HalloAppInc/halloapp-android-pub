package com.halloapp.content;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.BuildConfig;
import com.halloapp.id.UserId;
import com.halloapp.util.BgWorkers;

public class MomentManager {

    private static MomentManager instance;

    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;

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

    private final MutableLiveData<MomentUnlockStatus> unlockLiveData = new MutableLiveData<>(new MomentUnlockStatus());

    private final ContentDb.Observer observer = new ContentDb.DefaultObserver() {

        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.type == (BuildConfig.IS_KATCHUP ? Post.TYPE_KATCHUP : Post.TYPE_MOMENT)) {
                invalidateUnlock();
            }
        }

        @Override
        public void onPostRetracted(@NonNull Post post) {
            if (post.type == (BuildConfig.IS_KATCHUP ? Post.TYPE_KATCHUP : Post.TYPE_MOMENT) && post.isOutgoing()) {
                invalidateUnlock();
            }
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            if (senderUserId.isMe()) {
                invalidateUnlock();
            }
        }

        @Override
        public void onPostsExpired() {
            invalidateUnlock();
        }
    };

    public void refresh() {
        invalidateUnlock();
    }

    private MomentManager(@NonNull BgWorkers bgWorkers, @NonNull ContentDb contentDb) {
        this.bgWorkers = bgWorkers;
        this.contentDb = contentDb;

        contentDb.addObserver(observer);

        invalidateUnlock();
    }

    public LiveData<MomentUnlockStatus> isUnlockedLiveData() {
        return unlockLiveData;
    }

    private void invalidateUnlock() {
        bgWorkers.execute(() -> {
            unlockLiveData.postValue(contentDb.getMomentUnlockStatus());
        });
    }
}
