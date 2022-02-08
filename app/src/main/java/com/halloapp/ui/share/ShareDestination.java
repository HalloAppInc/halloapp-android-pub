package com.halloapp.ui.share;

import android.os.Parcel;
import android.os.Parcelable;

import com.halloapp.contacts.Contact;
import com.halloapp.content.Chat;
import com.halloapp.id.ChatId;

import java.util.Objects;

public class ShareDestination implements Parcelable {
    public final static int TYPE_FEED = 0;
    public final static int TYPE_GROUP = 1;
    public final static int TYPE_CONTACT = 2;

    public int type;
    public String name;
    public ChatId id;

    public static ShareDestination feed() {
        ShareDestination destination = new ShareDestination();
        destination.type = TYPE_FEED;
        destination.name = "";

        return destination;
    }

    public static ShareDestination fromGroup(Chat group) {
        ShareDestination destination = new ShareDestination();
        destination.type = TYPE_GROUP;
        destination.name = group.name;
        destination.id = group.chatId;

        return destination;
    }

    public static ShareDestination fromContact(Contact contact) {
        ShareDestination destination = new ShareDestination();
        destination.type = TYPE_CONTACT;
        destination.name = contact.getDisplayName();
        destination.id = contact.userId;

        return destination;
    }

    private ShareDestination() {
    }

    private ShareDestination(Parcel in) {
        type = in.readInt();
        name = in.readString();
        id = in.readParcelable(ChatId.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(type);
        parcel.writeString(name);
        parcel.writeParcelable(id, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShareDestination that = (ShareDestination) o;
        return type == that.type && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

    public static final Creator<ShareDestination> CREATOR = new Creator<ShareDestination>() {
        public ShareDestination createFromParcel(Parcel in) {
            return new ShareDestination(in);
        }

        public ShareDestination[] newArray(int size) {
            return new ShareDestination[size];
        }
    };
}