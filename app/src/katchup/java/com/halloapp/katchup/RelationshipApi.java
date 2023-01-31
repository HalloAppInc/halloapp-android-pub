package com.halloapp.katchup;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.Observable;

public class RelationshipApi {
    private static RelationshipApi instance;

    public static RelationshipApi getInstance() {
        if (instance == null) {
            synchronized (RelationshipApi.class) {
                if (instance == null) {
                    instance = new RelationshipApi(
                            Connection.getInstance(),
                            ContactsDb.getInstance()
                    );
                }
            }
        }
        return instance;
    }

    private Connection connection;
    private ContactsDb contactsDb;

    private RelationshipApi(
            @NonNull Connection connection,
            @NonNull ContactsDb contactsDb
    ) {
        this.connection = connection;
        this.contactsDb = contactsDb;
    }

    public Observable<Boolean> requestFollowUser(@NonNull UserId userId) {
        return connection.requestFollowUser(userId).map(res -> {
            if (res.success) {
                contactsDb.addRelationship(new RelationshipInfo(
                        res.userId,
                        res.username,
                        res.name,
                        res.avatarId,
                        RelationshipInfo.Type.FOLLOWING
                ));
            }
            return res.success;
        });
    }

    public Observable<Boolean> requestUnfollowUser(@NonNull UserId userId) {
        return connection.requestUnfollowUser(userId).map(res -> {
            if (res.success) {
                RelationshipInfo followingRelationship = contactsDb.getRelationship(res.userId, RelationshipInfo.Type.FOLLOWING);
                if (followingRelationship != null) {
                    contactsDb.removeRelationship(followingRelationship);
                }
            }
            return res.success;
        });
    }

    public Observable<Boolean> requestBlockUser(@NonNull UserId userId) {
        return connection.requestBlockUser(userId).map(res -> {
            if (res.success) {
                RelationshipInfo followingRelationship = contactsDb.getRelationship(res.userId, RelationshipInfo.Type.FOLLOWING);
                if (followingRelationship != null) {
                    contactsDb.removeRelationship(followingRelationship);
                }

                contactsDb.addRelationship(new RelationshipInfo(
                        res.userId,
                        res.username,
                        res.name,
                        res.avatarId,
                        RelationshipInfo.Type.BLOCKED
                ));
            }
            return res.success;
        });
    }

    public Observable<Boolean> requestUnblockUser(@NonNull UserId userId) {
        return connection.requestUnblockUser(userId).map(res -> {
            if (res.success) {
                RelationshipInfo blockedRelationship = contactsDb.getRelationship(res.userId, RelationshipInfo.Type.BLOCKED);
                if (blockedRelationship != null) {
                    contactsDb.removeRelationship(blockedRelationship);
                }
            }
            return res.success;
        });
    }
}
