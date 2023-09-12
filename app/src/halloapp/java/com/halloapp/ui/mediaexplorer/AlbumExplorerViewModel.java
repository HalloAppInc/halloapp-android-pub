package com.halloapp.ui.mediaexplorer;

import android.annotation.SuppressLint;
import android.app.Application;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Media;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.List;

public class AlbumExplorerViewModel extends AndroidViewModel {

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
            return (T) new AlbumExplorerViewModel(application, chatId, preloaded, position);
        }
    }

    private final ChatId chatId;
    private int position;
    private boolean initializationInProgress = true;

    private ComputableLiveData<String> name;

    private AlbumExplorerViewModel(@NonNull Application application, @Nullable ChatId chatId, @NonNull List<MediaModel> preloaded, int position) {
        super(application);

        this.chatId = chatId;
        this.position = position;

        name = new ComputableLiveData<String>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected String compute() {
                if (chatId instanceof UserId) {
                    ContactsDb contactsDb = ContactsDb.getInstance();
                    Contact contact = contactsDb.getContact((UserId) chatId);
                    String phone = TextUtils.isEmpty(contact.addressBookName) ? contactsDb.readPhone((UserId) chatId) : null;
                    String normalizedPhone = phone == null ? null : PhoneNumberUtils.formatNumber("+" + phone, null);
                    if (!TextUtils.isEmpty(contact.addressBookName)) {
                        return contact.addressBookName;
                    } else if (!TextUtils.isEmpty(normalizedPhone)) {
                        return normalizedPhone;
                    }
                    return contact.getDisplayName();
                } else if (chatId instanceof GroupId) {
                    Group group = ContentDb.getInstance().getGroupFeedOrChat((GroupId) chatId);
                    if (group != null) {
                        return group.name;
                    }
                }
                return "";
            }
        };
    }

    public LiveData<String> getName() {
        return name.getLiveData();
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

    public static class MediaModel extends MediaExplorerViewModel.MediaModel {
        public final int width;
        public final int height;

        public static ArrayList<MediaExplorerViewModel.MediaModel> fromMedia(@NonNull List<Media> media) {
            ArrayList<MediaExplorerViewModel.MediaModel> models = new ArrayList<>(media.size());

            for (Media item : media) {
                Uri uri;
                if (item.file != null) {
                    uri = Uri.fromFile(item.file);
                } else {
                    Log.w("AlbumExplorerViewModel.MediaModel: missing file and url for media");
                    continue;
                }
                models.add(new MediaModel(uri, item.type, item.rowId, item.transferred, item.blobVersion, item.chunkSize, item.blobSize, item.width, item.height));
            }
            return models;
        }

        public MediaModel(
                @NonNull Uri uri, int type, long rowId, @Media.TransferredState int transferred, @Media.BlobVersion int blobVersion, int chunkSize, long blobSize, int width, int height) {
            super(uri, type, rowId, null, transferred, blobVersion, chunkSize, blobSize);

            this.width = width;
            this.height = height;
        }

        private MediaModel(Parcel in) {
            super(in);
            width = in.readInt();
            height = in.readInt();
        }

        public boolean isStreamingVideo() {
            return blobVersion == Media.BLOB_VERSION_CHUNKED && type == Media.MEDIA_TYPE_VIDEO && transferred == Media.TRANSFERRED_PARTIAL_CHUNKED;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            super.writeToParcel(parcel, flags);
            parcel.writeInt(width);
            parcel.writeInt(height);
        }

        public static final Parcelable.Creator<MediaModel> CREATOR = new Parcelable.Creator<MediaModel>() {
            public MediaModel createFromParcel(Parcel in) {
                return new MediaModel(in);
            }

            public MediaModel[] newArray(int size) {
                return new MediaModel[size];
            }
        };

        @NonNull
        @Override
        public String toString() {
            return "AlbumExplorerViewModel.MediaModel {uri:" + uri + " type:" + type + " rowId:" + rowId + " transferred:" + transferred + " blobVersion:" + blobVersion + " chunkSize:" + chunkSize + " blobSize:" + blobSize + "}";
        }
    }
}
