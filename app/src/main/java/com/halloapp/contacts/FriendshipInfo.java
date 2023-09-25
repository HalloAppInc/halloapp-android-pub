package com.halloapp.contacts;

import androidx.annotation.IntDef;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.FriendListRequest;
import com.halloapp.proto.server.FriendshipStatus;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class FriendshipInfo {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Type.NONE_STATUS, Type.INCOMING_PENDING, Type.OUTGOING_PENDING, Type.FRIENDS, Type.BLOCKED})
    public @interface Type {
        int NONE_STATUS = 1;
        int INCOMING_PENDING = 2;
        int OUTGOING_PENDING = 3;
        int FRIENDS = 4;
        int BLOCKED = 5;
    }

    public static @Type int fromProtoType(FriendshipStatus friendshipStatus, boolean isBlocked) {
        if (isBlocked) {
            return Type.BLOCKED;
        }
        switch (friendshipStatus) {
            case NONE_STATUS:
                return Type.NONE_STATUS;
            case INCOMING_PENDING:
                return Type.INCOMING_PENDING;
            case OUTGOING_PENDING:
                return Type.OUTGOING_PENDING;
            case FRIENDS:
                return Type.FRIENDS;
        }
        throw new IllegalArgumentException("Unexpected friendship status " + friendshipStatus);
    }

    public static @Type int fromFriendListAction(FriendListRequest.Action action) {
        switch (action) {
            case GET_FRIENDS:
                return Type.FRIENDS;
            case GET_INCOMING_PENDING:
                return Type.INCOMING_PENDING;
            case GET_OUTGOING_PENDING:
                return Type.OUTGOING_PENDING;
            case GET_SUGGESTIONS:
                return Type.NONE_STATUS;
        }
        throw new IllegalArgumentException("Unexpected friendship list request action " + action);
    }

    public final UserId userId;
    public final String username;
    public final String name;
    public final String avatarId;
    public final int friendshipStatus;
    public boolean seen;
    public final long timestamp;

    public FriendshipInfo(UserId userId, String username, String name, String avatarId, @Type int friendshipStatus, long timestamp) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.avatarId = avatarId;
        this.friendshipStatus = friendshipStatus;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendshipInfo that = (FriendshipInfo) o;
        return friendshipStatus == that.friendshipStatus
                && Objects.equals(userId, that.userId)
                && Objects.equals(username, that.username)
                && Objects.equals(name, that.name)
                && Objects.equals(avatarId, that.avatarId)
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, name, avatarId, friendshipStatus);
    }
}
