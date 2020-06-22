package com.halloapp.content;

import com.halloapp.contacts.UserId;

public class Mention {

    public static Mention parseFromProto(com.halloapp.proto.Mention protoMention) {
        return new Mention(protoMention.getIndex(), new UserId(protoMention.getUserId()), protoMention.getName());
    }

    public static com.halloapp.proto.Mention toProto(Mention mention) {
        com.halloapp.proto.Mention.Builder builder = com.halloapp.proto.Mention.newBuilder();
        builder.setIndex(mention.index);
        builder.setName(mention.fallbackName);
        builder.setUserId(mention.userId.rawId());
        return builder.build();
    }

    public long rowId;

    public int index;
    public UserId userId;
    public String fallbackName;

    public Mention(int index, UserId userId, String fallbackName) {
        this.index = index;
        this.userId = userId;
        this.fallbackName=  fallbackName;
    }

    public Mention(long rowId, int index, String rawUserId, String name) {
        this.rowId = rowId;
        this.index = index;
        this.userId = new UserId(rawUserId);
        this.fallbackName = name;
    }
}
