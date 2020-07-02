package com.halloapp.ui.invites;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.InvitesResponseIq;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InviteFriendsViewModel extends AndroidViewModel {

    private Connection connection;

    private Executor executor;

    ComputableLiveData<Integer> inviteCountData;

    public static final int RESPONSE_RETRYABLE = -1;

    public InviteFriendsViewModel(@NonNull Application application) {
        super(application);
        executor = Executors.newSingleThreadExecutor();
        connection = Connection.getInstance();

        inviteCountData = new ComputableLiveData<Integer>() {
            @Override
            protected Integer compute() {
                try {
                    Integer response = connection.getAvailableInviteCount().get();
                    if (response == null) {
                        return RESPONSE_RETRYABLE;
                    }
                    return response;
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("InviteFriendsViewModel/inviteCountData failed to get count");
                    return RESPONSE_RETRYABLE;
                }
            }
        };
    }

    public LiveData<Integer> sendInvite(@NonNull String phoneNumber) {
        MutableLiveData<Integer> inviteResult = new MutableLiveData<>();
        executor.execute(() -> {
            Future<Integer> resultFuture = connection.sendInvite(phoneNumber);
            try {
                inviteResult.postValue(resultFuture.get());
                inviteCountData.invalidate();
                return;
            } catch (ExecutionException | InterruptedException e) {
                Log.e("inviteFriendsViewModel/sendInvite failed to send invite", e);
            }
            inviteResult.postValue(InvitesResponseIq.Result.UNKNOWN);
        });
        return inviteResult;
    }
}
