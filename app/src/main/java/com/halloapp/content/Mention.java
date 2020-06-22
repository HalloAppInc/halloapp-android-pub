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

    public Mention(int index, UserId userId, String fallbackName) {
        this.index = index;
        this.userId = userId;
        this.fallbackName=  fallbackName;
    }

    public int index;
    public UserId userId;
    public String fallbackName;
}
