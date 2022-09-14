package com.halloapp.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.halloapp.id.UserId;

public class Mention implements Parcelable {

    public static Mention parseFromProto(com.halloapp.proto.clients.Mention protoMention) {
        return new Mention(protoMention.getIndex(), new UserId(protoMention.getUserId()), protoMention.getName());
    }

    public static com.halloapp.proto.clients.Mention toProto(Mention mention) {
        com.halloapp.proto.clients.Mention.Builder builder = com.halloapp.proto.clients.Mention.newBuilder();
        builder.setIndex(mention.index);
        if (!TextUtils.isEmpty(mention.fallbackName)) {
            builder.setName(mention.fallbackName);
        }
        builder.setUserId(mention.userId.rawId());
        return builder.build();
    }

    public long rowId;

    public final int index;
    public UserId userId;
    public final String fallbackName;
    public boolean isInAddressBook;

    public Mention(int index, UserId userId, String fallbackName) {
        this.index = index;
        this.userId = userId;
        this.fallbackName = fallbackName;
    }

    public Mention(long rowId, int index, String rawUserId, String name) {
        this.rowId = rowId;
        this.index = index;
        this.userId = new UserId(rawUserId);
        this.fallbackName = name;
    }

    private Mention(Parcel parcel) {
        rowId = parcel.readLong();
        index = parcel.readInt();
        userId = parcel.readParcelable(UserId.class.getClassLoader());
        fallbackName = parcel.readString();
        isInAddressBook = parcel.readInt() != 0;
    }

    public static final Parcelable.Creator<Mention> CREATOR = new Parcelable.Creator<Mention>() {
        public Mention createFromParcel(Parcel parcel) {
            return new Mention(parcel);
        }

        public Mention[] newArray(int size) {
            return new Mention[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(rowId);
        parcel.writeInt(index);
        parcel.writeParcelable(userId, flags);
        parcel.writeString(fallbackName);
        parcel.writeInt(isInAddressBook ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
