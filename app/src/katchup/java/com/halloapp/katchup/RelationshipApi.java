package com.halloapp.katchup;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.Observable;

import java.util.List;

public class RelationshipApi {
    private static RelationshipApi instance;

    public static RelationshipApi getInstance() {
        if (instance == null) {
            synchronized (RelationshipApi.class) {
                if (instance == null) {
                    instance = new RelationshipApi(
                            Connection.getInstance(),
                            ContactsDb.getInstance(),
                            ContentDb.getInstance(),
                            KAvatarLoader.getInstance()
                    );
                }
            }
        }
        return instance;
    }

    private final Connection connection;
    private final ContactsDb contactsDb;
    private final ContentDb contentDb;
    private final KAvatarLoader kAvatarLoader;

    private RelationshipApi(
            @NonNull Connection connection,
            @NonNull ContactsDb contactsDb,
            @NonNull ContentDb contentDb,
            @NonNull KAvatarLoader kAvatarLoader
    ) {
        this.connection = connection;
        this.contactsDb = contactsDb;
        this.contentDb = contentDb;
        this.kAvatarLoader = kAvatarLoader;
    }

    public Observable<Boolean> requestFollowUser(@NonNull UserId userId) {
        return connection.requestFollowUser(userId).map(res -> {
            if (res.success) {
                contactsDb.addRelationship(new RelationshipInfo(
                        res.userId,
                        res.username,
                        res.name,
                        res.avatarId,
                        RelationshipInfo.Type.FOLLOWING,
                        System.currentTimeMillis()
                ));
            }
            Analytics.getInstance().followed(res.success);
            return res.success;
        }).mapError(err -> {
            Analytics.getInstance().followed(false);
            return err;
        });
    }

    public Observable<Boolean> requestUnfollowUser(@NonNull UserId userId) {
        return connection.requestUnfollowUser(userId).map(res -> {
            if (res.success) {
                RelationshipInfo followingRelationship = contactsDb.getRelationship(res.userId, RelationshipInfo.Type.FOLLOWING);
                if (followingRelationship != null) {
                    contactsDb.removeRelationship(followingRelationship);
                }
                deletePostsByUser(userId);
            }
            Analytics.getInstance().unfollowed(res.success);
            return res.success;
        }).mapError(err -> {
            Analytics.getInstance().unfollowed(false);
            return err;
        });
    }

    public Observable<Boolean> requestRemoveFollower(@NonNull UserId userId) {
        return connection.requestRemoveFollower(userId).map(res -> {
            if (res.success) {
                RelationshipInfo followingRelationship = contactsDb.getRelationship(res.userId, RelationshipInfo.Type.FOLLOWER);
                if (followingRelationship != null) {
                    contactsDb.removeRelationship(followingRelationship);
                }
            }
            Analytics.getInstance().removedFollower(res.success);
            return res.success;
        }).mapError(err -> {
            Analytics.getInstance().removedFollower(false);
            return err;
        });
    }

    public Observable<Boolean> requestBlockUser(@NonNull UserId userId) {
        return connection.requestBlockUser(userId).map(res -> {
            if (res.success) {
                RelationshipInfo followingRelationship = contactsDb.getRelationship(res.userId, RelationshipInfo.Type.FOLLOWING);
                if (followingRelationship != null) {
                    contactsDb.removeRelationship(followingRelationship);
                }

                RelationshipInfo followerRelationship = contactsDb.getRelationship(res.userId, RelationshipInfo.Type.FOLLOWER);
                if (followerRelationship != null) {
                    contactsDb.removeRelationship(followerRelationship);
                }

                contactsDb.addRelationship(new RelationshipInfo(
                        res.userId,
                        res.username,
                        res.name,
                        res.avatarId,
                        RelationshipInfo.Type.BLOCKED,
                        System.currentTimeMillis()
                ));

                deletePostsByUser(userId);
                kAvatarLoader.removeAvatar(userId);
            }
            Analytics.getInstance().blocked(res.success);
            return res.success;
        }).mapError(err -> {
            Analytics.getInstance().blocked(false);
            return err;
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
            Analytics.getInstance().unblocked(res.success);
            return res.success;
        }).mapError(err -> {
            Analytics.getInstance().unblocked(false);
            return err;
        });
    }

    private void deletePostsByUser(@NonNull UserId userId) {
        List<Post> posts = contentDb.getPosts(null, 10, false, userId, null);
        for (Post post : posts) {
            contentDb.deletePost(post, null);
        }
    }
}
