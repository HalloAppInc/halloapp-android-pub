package com.halloapp.ui.settings;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.invites.InvitesApi;
import com.halloapp.xmpp.privacy.PrivacyList;
import com.halloapp.xmpp.privacy.PrivacyListApi;

import java.util.List;

public class SettingsViewModel extends AndroidViewModel {

    private Me me;
    private BgWorkers bgWorkers;
    private Connection connection;
    private ContactsDb contactsDb;
    private Preferences preferences;
    private BlockListManager blockListManager;
    private FeedPrivacyManager feedPrivacyManager;

    private final InvitesApi invitesApi;

    private ComputableLiveData<String> phoneNumberLiveData;
    private ComputableLiveData<FeedPrivacy> feedPrivacyLiveData;
    private final MutableLiveData<Integer> inviteCountLiveData;
    private ComputableLiveData<List<UserId>> blockListLiveData;

    private final BlockListManager.Observer blockListObserver = () -> {
        blockListLiveData.invalidate();
    };

    private final FeedPrivacyManager.Observer feedPrivacyObserver = () -> {
        feedPrivacyLiveData.invalidate();
    };

    public SettingsViewModel(@NonNull Application application) {
        super(application);

        me = Me.getInstance();
        bgWorkers = BgWorkers.getInstance();
        connection = Connection.getInstance();
        contactsDb = ContactsDb.getInstance();
        preferences = Preferences.getInstance();
        blockListManager = BlockListManager.getInstance();
        feedPrivacyManager = FeedPrivacyManager.getInstance();

        invitesApi = new InvitesApi(connection);

        inviteCountLiveData = new MutableLiveData<>();
        phoneNumberLiveData = new ComputableLiveData<String>() {
            @Override
            protected String compute() {
                return me.getPhone();
            }
        };
        blockListLiveData = new ComputableLiveData<List<UserId>>() {
            @Override
            protected List<UserId> compute() {
                return blockListManager.getBlockList();
            }
        };
        blockListManager.addObserver(blockListObserver);
        feedPrivacyLiveData = new ComputableLiveData<FeedPrivacy>() {
            @Override
            protected FeedPrivacy compute() {
                return feedPrivacyManager.getFeedPrivacy();
            }
        };
        feedPrivacyManager.addObserver(feedPrivacyObserver);
        bgWorkers.execute(() -> me.getName());
        refresh();
    }

    @MainThread
    public void refresh() {
        phoneNumberLiveData.invalidate();
        fetchInvitesCount();
    }

    private void fetchInvitesCount() {
        bgWorkers.execute(() -> {
            int cachedInviteCount = preferences.getInvitesRemaining();
            if (cachedInviteCount != -1) {
                inviteCountLiveData.postValue(cachedInviteCount);
            }
            invitesApi.getAvailableInviteCount().onResponse(inviteCountLiveData::postValue);
        });
    }

    public LiveData<FeedPrivacy> getFeedPrivacy() {
        return feedPrivacyLiveData.getLiveData();
    }

    public LiveData<String> getPhone() {
        return phoneNumberLiveData.getLiveData();
    }

    public LiveData<String> getName() {
        return me.name;
    }

    public LiveData<List<UserId>> getBlockList() {
        return blockListLiveData.getLiveData();
    }

    public LiveData<Integer> getInviteCount() {
        return inviteCountLiveData;
    }

    @Override
    protected void onCleared() {
        blockListManager.removeObserver(blockListObserver);
        feedPrivacyManager.removeObserver(feedPrivacyObserver);
    }
}
