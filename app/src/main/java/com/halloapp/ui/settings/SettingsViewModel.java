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
import com.halloapp.contacts.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.invites.InvitesApi;
import com.halloapp.xmpp.privacy.PrivacyList;
import com.halloapp.xmpp.privacy.PrivacyListApi;
import com.halloapp.xmpp.util.Observable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class SettingsViewModel extends AndroidViewModel {

    private Me me;
    private BgWorkers bgWorkers;
    private Connection connection;
    private ContactsDb contactsDb;
    private Preferences preferences;

    private final InvitesApi invitesApi;
    private final PrivacyListApi privacyListApi;

    private ComputableLiveData<String> phoneNumberLiveData;
    private final MutableLiveData<List<UserId>> blockList;
    private final MutableLiveData<FeedPrivacy> feedPrivacyLiveData;
    private final MutableLiveData<Integer> inviteCountLiveData;

    public SettingsViewModel(@NonNull Application application) {
        super(application);

        me = Me.getInstance(application);
        bgWorkers = BgWorkers.getInstance();
        connection = Connection.getInstance();
        contactsDb = ContactsDb.getInstance(application);
        preferences = Preferences.getInstance(application);

        invitesApi = new InvitesApi(connection);
        privacyListApi = new PrivacyListApi(connection);

        blockList = new MutableLiveData<>();
        feedPrivacyLiveData = new MutableLiveData<>();
        inviteCountLiveData = new MutableLiveData<>();
        phoneNumberLiveData = new ComputableLiveData<String>() {
            @Override
            protected String compute() {
                return me.getPhone();
            }
        };
        bgWorkers.execute(() -> me.getName());
        refresh();
    }

    @MainThread
    public void refresh() {
        phoneNumberLiveData.invalidate();
        fetchFeedPrivacy();
        fetchBlockList();
        fetchInvitesCount();
    }

    private void fetchFeedPrivacy() {
        bgWorkers.execute(() -> {
            String cachedActiveList = preferences.getFeedPrivacyActiveList();
            FeedPrivacy feedPrivacy;
            List<UserId> exceptList = null;
            List<UserId> onlyList = null;
            if (PrivacyList.Type.EXCEPT.equals(cachedActiveList)) {
                exceptList = contactsDb.getFeedExclusionList();
            } else if (PrivacyList.Type.ONLY.equals(cachedActiveList)) {
                onlyList = contactsDb.getFeedShareList();
            }
            feedPrivacy = new FeedPrivacy(cachedActiveList, exceptList, onlyList);
            feedPrivacyLiveData.postValue(feedPrivacy);
            privacyListApi.getFeedPrivacy().onResponse(feedPrivacyResponse -> {
                preferences.setFeedPrivacyActiveList(feedPrivacyResponse.activeList);
                contactsDb.setFeedExclusionList(feedPrivacyResponse.exceptList);
                contactsDb.setFeedShareList(feedPrivacyResponse.onlyList);
                feedPrivacyLiveData.postValue(feedPrivacyResponse);
            });
        });
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
        return feedPrivacyLiveData;
    }

    private void fetchBlockList() {
        bgWorkers.execute(() -> {
            List<UserId> blockedUserIds = contactsDb.getBlockList();
            blockList.postValue(blockedUserIds);
        });
    }

    public LiveData<String> getPhone() {
        return phoneNumberLiveData.getLiveData();
    }

    public LiveData<String> getName() {
        return me.name;
    }

    public LiveData<List<UserId>> getBlockList() {
        return blockList;
    }

    public LiveData<Integer> getInviteCount() {
        return inviteCountLiveData;
    }
}
