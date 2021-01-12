package com.halloapp.ui.mediaexplorer;

import android.app.Application;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.paging.PositionalDataSource;

import com.google.android.gms.common.util.concurrent.HandlerExecutor;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.id.ChatId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class MediaExplorerViewModel extends AndroidViewModel {

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final ChatId chatId;
        private final List<MediaModel> preloaded;
        private final int position;

        public Factory(@NonNull Application application, @Nullable ChatId chatId, @NonNull List<MediaModel> preloaded, int position) {
            this.application = application;
            this.chatId = chatId;
            this.preloaded = preloaded;
            this.position = position;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new MediaExplorerViewModel(application, chatId, preloaded, position);
        }
    }

    private final LiveData<PagedList<MediaModel>> media;
    private final ContentDb contentDb = ContentDb.getInstance();
    private final ChatId chatId;
    private int position;
    private boolean initializationInProgress = true;

    private MediaExplorerViewModel(@NonNull Application application, @Nullable ChatId chatId, @NonNull List<MediaModel> preloaded, int position) {
        super(application);

        this.chatId = chatId;
        this.position = position;

        if (chatId == null) {
            Executor executor = new HandlerExecutor(getApplication().getMainLooper());
            PagedList<MediaModel> list = new PagedList.Builder<>(new PositionalDataSource<MediaModel>() {
                @Override
                public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<MediaModel> callback) {
                    callback.onResult(preloaded, 0, preloaded.size());
                }

                @Override
                public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<MediaModel> callback) {
                    callback.onResult(new ArrayList<>());
                }
            }, preloaded.size())
                    .setNotifyExecutor(executor)
                    .setFetchExecutor(executor)
                    .build();

            media = new MutableLiveData<>(list);
        } else {
            MediaExplorerDataSource.Factory factory = new MediaExplorerDataSource.Factory(contentDb, chatId, preloaded);
            media = new LivePagedListBuilder<>(factory, 16).build();
        }
    }

    public boolean isChat() {
        return chatId != null;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setInitializationInProgress(boolean initializationInProgress) {
        this.initializationInProgress = initializationInProgress;
    }

    public boolean isInitializationInProgress() {
        return initializationInProgress;
    }

    public LiveData<PagedList<MediaModel>> getMedia() {
        return media;
    }

    @WorkerThread
    public String getContentIdInChat(long rowId) {
        Message msg = contentDb.getMessageForMedia(rowId);
        return msg != null ? msg.id : null;
    }

    @WorkerThread
    public int getPositionInChat(long rowId) {
        return (int) contentDb.getChatMediaPosition(chatId, rowId);
    }

    @WorkerThread
    public int getPositionInMessage(long rowId) {
        Message msg = contentDb.getMessageForMedia(rowId);

        for (int i = 0; msg != null && i < msg.media.size(); ++i) {
            if (msg.media.get(i).rowId == rowId) {
                return i;
            }
        }

        return -1;
    }

    public static class MediaModel implements Parcelable {
        public Uri uri;
        public int type;
        public long rowId;

        public MediaModel(@NonNull Uri uri, int type) {
            this.uri = uri;
            this.type = type;
        }

        public MediaModel(@NonNull Uri uri, int type, long rowId) {
            this.uri = uri;
            this.type = type;
            this.rowId = rowId;
        }

        private MediaModel(Parcel in) {
            uri = in.readParcelable(Uri.class.getClassLoader());
            type = in.readInt();
            rowId = in.readLong();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeParcelable(uri, flags);
            parcel.writeInt(type);
            parcel.writeLong(rowId);
        }

        public static final Parcelable.Creator<MediaModel> CREATOR = new Parcelable.Creator<MediaModel>() {
            public MediaModel createFromParcel(Parcel in) {
                return new MediaModel(in);
            }

            public MediaModel[] newArray(int size) {
                return new MediaModel[size];
            }
        };
    }
}
