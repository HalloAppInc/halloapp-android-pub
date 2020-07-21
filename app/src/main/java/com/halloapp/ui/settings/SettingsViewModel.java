package com.halloapp.ui.settings;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.Me;
import com.halloapp.contacts.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.privacy.PrivacyListApi;
import com.halloapp.xmpp.util.Observable;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class SettingsViewModel extends AndroidViewModel {

    private Me me;
    private BgWorkers bgWorkers;
    private Connection connection;

    private final PrivacyListApi privacyListApi;

    private ComputableLiveData<Integer> inviteCountData;
    private ComputableLiveData<String> phoneNumberLiveData;
    private final MutableLiveData<List<UserId>> blockList;
    private final MutableLiveData<FeedPrivacy> feedPrivacyLiveData;

    public SettingsViewModel(@NonNull Application application) {
        super(application);

        me = Me.getInstance(application);
        bgWorkers = BgWorkers.getInstance();
        connection = Connection.getInstance();
        privacyListApi = new PrivacyListApi(connection);

        blockList = new MutableLiveData<>();
        feedPrivacyLiveData = new MutableLiveData<>();
        inviteCountData = new ComputableLiveData<Integer>() {
            @Override
            protected Integer compute() {
                try {
                    return connection.getAvailableInviteCount().get();
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("InviteFriendsViewModel/inviteCountData failed to get count");
                    return null;
                }
            }
        };
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
        inviteCountData.invalidate();
        privacyListApi.getFeedPrivacy().onResponse(feedPrivacyLiveData::postValue);
        fetchBlockList();
    }

    public LiveData<FeedPrivacy> getFeedPrivacy() {
        return feedPrivacyLiveData;
    }

    private void fetchBlockList() {
        privacyListApi.getBlockList().onResponse(blockList::postValue);
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
        return inviteCountData.getLiveData();
    }
}
