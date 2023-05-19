package com.halloapp.contacts;

import androidx.annotation.IntDef;

import com.halloapp.id.UserId;
import com.halloapp.proto.server.RelationshipList;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class RelationshipInfo {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Type.FOLLOWER, Type.FOLLOWING, Type.INCOMING, Type.OUTGOING, Type.BLOCKED})
    public @interface Type {
        public static final int FOLLOWER = 1;
        public static final int FOLLOWING = 2;
        public static final int INCOMING = 3;
        public static final int OUTGOING = 4;
        public static final int BLOCKED = 5;
    }

    public static @Type int fromProtoType(RelationshipList.Type type) {
        switch (type) {
            case FOLLOWER:
                return Type.FOLLOWER;
            case FOLLOWING:
                return Type.FOLLOWING;
            case INCOMING:
                return Type.INCOMING;
            case OUTGOING:
                return Type.OUTGOING;
            case BLOCKED:
                return Type.BLOCKED;
        }
        throw new IllegalArgumentException("Unexpected relationship type " + type);
    }

    public static RelationshipList.Type toProtoType(@Type int type) {
        switch (type) {
            case Type.FOLLOWER:
                return RelationshipList.Type.FOLLOWER;
            case Type.FOLLOWING:
                return RelationshipList.Type.FOLLOWING;
            case Type.INCOMING:
                return RelationshipList.Type.INCOMING;
            case Type.OUTGOING:
                return RelationshipList.Type.OUTGOING;
            case Type.BLOCKED:
                return RelationshipList.Type.BLOCKED;
        }
        throw new IllegalArgumentException("Unexpected relationship type " + type);
    }

    public final UserId userId;
    public final String username;
    public final String name;
    public final String avatarId;
    public final int relationshipType;
    public boolean seen;
    public final long timestamp;

    public RelationshipInfo(UserId userId, String username, String name, String avatarId, @Type int relationshipType, long timestamp) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.avatarId = avatarId;
        this.relationshipType = relationshipType;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipInfo that = (RelationshipInfo) o;
        return relationshipType == that.relationshipType
                && Objects.equals(userId, that.userId)
                && Objects.equals(username, that.username)
                && Objects.equals(name, that.name)
                && Objects.equals(avatarId, that.avatarId)
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, name, avatarId, relationshipType);
    }
}
