package com.halloapp.ui.groups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.group.GroupFeedSessionManager;
import com.halloapp.crypto.group.GroupSetupInfo;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.crypto.signal.SignalSessionSetupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.nux.ZeroZoneManager;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.EncryptedPayload;
import com.halloapp.proto.clients.GroupHistoryPayload;
import com.halloapp.proto.clients.MemberDetails;
import com.halloapp.proto.clients.SenderKey;
import com.halloapp.proto.clients.SenderState;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.proto.server.HistoryResend;
import com.halloapp.proto.server.IdentityKey;
import com.halloapp.proto.server.SenderStateBundle;
import com.halloapp.proto.server.SenderStateWithKeyInfo;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DelayedProgressLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.GroupHistoryDecryptStats;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.groups.GroupsApi;
import com.halloapp.xmpp.groups.MemberElement;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedGroupInfoViewModel extends BaseGroupInfoViewModel {

    public FeedGroupInfoViewModel(@NonNull Application application, @NonNull GroupId groupId) {
        super(application, groupId);
    }

    public LiveData<Boolean> changeExpiry(ExpiryInfo expiryInfo) {
        MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
        groupsApi.setGroupExpiry(groupId, expiryInfo)
                .onResponse(result::postValue)
                .onError(error -> {
                    Log.e("Leave change expiry failed", error);
                    result.postValue(false);
                });
        return result;
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final GroupId groupId;

        Factory(@NonNull Application application, @NonNull GroupId groupId) {
            this.application = application;
            this.groupId = groupId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(FeedGroupInfoViewModel.class)) {
                //noinspection unchecked
                return (T) new FeedGroupInfoViewModel(application, groupId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
