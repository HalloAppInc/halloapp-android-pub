package com.halloapp.xmpp.privacy;

import androidx.annotation.NonNull;

import com.halloapp.contacts.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.MutableObservable;
import com.halloapp.xmpp.util.Observable;

import java.util.Collection;
import java.util.List;

public class PrivacyListApi {

    private Connection connection;

    public PrivacyListApi(Connection connection) {
        this.connection = connection;
    }

    public Observable<Boolean> blockUsers(@NonNull Collection<UserId> users) {
        final SetPrivacyListIq requestIq = new SetPrivacyListIq(PrivacyList.Type.BLOCK, users, null);
        final MutableObservable<Boolean> requestResult = new MutableObservable<>();
        connection.sendRequestIq(requestIq).onResponse(iq -> {
            requestResult.setResponse(Boolean.TRUE);
        }).onError(e -> {
            requestResult.setResponse(Boolean.FALSE);
        });
        return requestResult;
    }

    public Observable<Boolean> unblockUsers(@NonNull Collection<UserId> users) {
        final SetPrivacyListIq requestIq = new SetPrivacyListIq(PrivacyList.Type.BLOCK, null, users);
        final MutableObservable<Boolean> requestResult = new MutableObservable<>();
        connection.sendRequestIq(requestIq).onResponse(iq -> {
            requestResult.setResponse(Boolean.TRUE);
        }).onError(e -> {
            requestResult.setResponse(Boolean.FALSE);
        });
        return requestResult;
    }

    public Observable<List<UserId>> getBlockList() {
        final PrivacyListsRequestIq requestIq = new PrivacyListsRequestIq(PrivacyList.Type.BLOCK);
        Observable<PrivacyListsResponseIq> iqResponse = connection.sendRequestIq(requestIq);
        return Observable.map(iqResponse, response -> {
            final PrivacyList list = response.getPrivacyList(PrivacyList.Type.BLOCK);
            return list == null ? null : list.getUserIds();
        });
    }

    public Observable<FeedPrivacy> getFeedPrivacy() {
        final PrivacyListsRequestIq requestIq = new PrivacyListsRequestIq(PrivacyList.Type.ONLY, PrivacyList.Type.EXCEPT);
        Observable<PrivacyListsResponseIq> iqResponse = connection.sendRequestIq(requestIq);
        return Observable.map(iqResponse, response -> {
            final PrivacyList exceptList = response.getPrivacyList(PrivacyList.Type.EXCEPT);
            final PrivacyList onlyList = response.getPrivacyList(PrivacyList.Type.ONLY);
            return new FeedPrivacy(response.activeType, exceptList == null ? null : exceptList.userIds, onlyList == null ? null : onlyList.userIds);
        });
    }

    public Observable<Boolean> setFeedPrivacy(@PrivacyList.Type String activeList, List<UserId> addedUsers, List<UserId> deletedUsers) {
        final SetPrivacyListIq requestIq = new SetPrivacyListIq(activeList, addedUsers, deletedUsers);
        return Observable.map(connection.sendRequestIq(requestIq), input -> true);
    }
}
