package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.FriendListResponse;
import com.halloapp.proto.server.FriendProfile;
import com.halloapp.proto.server.HalloappUserProfile;
import com.halloapp.proto.server.Iq;

import java.util.ArrayList;
import java.util.List;

public class FriendListResponseIq extends HalloIq {

    public boolean success;
    public String cursor;
    public List<Suggestion> suggestions = new ArrayList<>();
    public List<FriendshipInfo> friendshipList;


    public FriendListResponseIq(@NonNull FriendListResponse friendListResponse) {
        friendshipList = new ArrayList<>();
        this.success = friendListResponse.getResult().equals(FriendListResponse.Result.OK);
        this.cursor = friendListResponse.getCursor();
        
        for (FriendProfile friendProfile : friendListResponse.getFriendProfilesList()) {
            HalloappUserProfile userProfile = friendProfile.getUserProfile();
            @FriendshipInfo.Type int status = FriendshipInfo.fromProtoType(userProfile.getStatus());
            FriendshipInfo info = new FriendshipInfo(
                    new UserId(Long.toString(userProfile.getUid())),
                    userProfile.getUsername(),
                    userProfile.getName(),
                    userProfile.getAvatarId(),
                    status,
                    System.currentTimeMillis()
            );
            friendshipList.add(info);
            suggestions.add(new Suggestion(Suggestion.fromProto(friendProfile.getReason()), friendProfile.getRank(), info));
        }
    }

    @Override
    public Iq.Builder toProtoIq() {
        return null;
    }

    public static FriendListResponseIq fromProto(@NonNull FriendListResponse friendListResponse) {
        return new FriendListResponseIq(friendListResponse);
    }

    public static class Suggestion {
        
        public enum Type {
            CONTACT,
            FOF,
            PENDING,
            UNKNOWN,
            NONE
        }

        public static Suggestion.Type fromProto(FriendProfile.Reason reason) {
            switch (reason) {
                case DIRECT_CONTACT: return Type.CONTACT;
                case FRIENDS_OF_FRIENDS: return Type.FOF;
                case INCOMING_PENDING: return Type.PENDING;
                case UNKNOWN_REASON: return Type.UNKNOWN;
                default: return Type.NONE;
            }
        }

        public Type type;
        public int rank;
        public FriendshipInfo info;

        public Suggestion(Suggestion.Type type, int rank, FriendshipInfo info) {
            this.type = type;
            this.rank = rank;
            this.info = info;
        }
    }
}
