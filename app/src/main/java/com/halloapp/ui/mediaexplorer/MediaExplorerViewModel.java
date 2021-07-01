package com.halloapp.ui.mediaexplorer;

import android.app.Application;
import android.content.Context;
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
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.io.File;
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
    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final ChatId chatId;
    private int position;
    private boolean initializationInProgress = true;

    private MediaExplorerViewModel(@NonNull Application application, @Nullable ChatId chatId, @NonNull List<MediaModel> preloaded, int position) {
        super(application);

        this.chatId = chatId;
        this.position = position;

        if (chatId == null) {
            final MutableLiveData<PagedList<MediaModel>> liveData = new MutableLiveData<>();
            final BgWorkers bgWorkers = BgWorkers.getInstance();
            final Executor executor = new HandlerExecutor(application.getMainLooper());
            media = liveData;
            bgWorkers.execute(() -> {
                PagedList.Builder<Integer, MediaModel> builder = new PagedList.Builder<>(new PositionalDataSource<MediaModel>() {
                    @Override
                    public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<MediaModel> callback) {
                        callback.onResult(preloaded, 0, preloaded.size());
                    }

                    @Override
                    public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<MediaModel> callback) {
                        callback.onResult(new ArrayList<>());
                    }
                }, preloaded.size());
                PagedList<MediaModel> list = builder.setNotifyExecutor(executor).setFetchExecutor(executor).build();
                liveData.postValue(list);
            });
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

    public LiveData<Boolean> savePostToGallery(MediaExplorerViewModel.MediaModel model) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();

        if (model.uri == null || model.uri.getPath() == null) {
            success.setValue(false);
            return success;
        }
        bgWorkers.execute(() -> {
            if (!MediaUtils.saveMediaToGallery(getApplication(), new File(model.uri.getPath()), model.type)) {
                success.postValue(false);
                Log.e("MediaExplorerViewModel/savePostToGallery failed to save media to gallery: " + media);
                return;
            }
            success.postValue(true);
        });
        return success;
    }

    public static class MediaModel implements Parcelable {
        public Uri uri;
        public int type;
        public long rowId;

        public static ArrayList<MediaModel> fromMedia(@NonNull List<Media> media) {
            ArrayList<MediaModel> models = new ArrayList<>(media.size());

            for (Media item : media) {
                Uri uri;

                if (item.file != null) {
                    uri = Uri.fromFile(item.file);
                } else if (item.url != null) {
                    uri = Uri.parse(item.url);
                } else {
                    Log.w("MediaExplorerViewModel.MediaModel: missing file and url for media");
                    continue;
                }

                models.add(new MediaExplorerViewModel.MediaModel(uri, item.type, item.rowId));
            }

            return models;
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
