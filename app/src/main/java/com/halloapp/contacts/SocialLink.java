package com.halloapp.contacts;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.proto.server.Link;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class SocialLink implements Parcelable {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Type.USER_DEFINED, Type.TIKTOK, Type.INSTAGRAM, Type.X, Type.YOUTUBE})
    public @interface Type {
        int USER_DEFINED = 1;
        int TIKTOK = 2;
        int INSTAGRAM = 3;
        int X = 4;
        int YOUTUBE = 5;
    }

    public static @Type int fromProtoType(@NonNull Link.Type type) {
        switch (type) {
            case USER_DEFINED:
                return Type.USER_DEFINED;
            case TIKTOK:
                return Type.TIKTOK;
            case INSTAGRAM:
                return Type.INSTAGRAM;
            case X:
                return Type.X;
            case YOUTUBE:
                return Type.YOUTUBE;
        }
        throw new IllegalArgumentException("Unexpected link type " + type);
    }

    public String getPrefix() {
        switch (type) {
            case Type.INSTAGRAM:
                return "instagram.com/";
            case Type.TIKTOK:
                return "tiktok.com/";
            case Type.X:
                return "x.com/";
            case Type.YOUTUBE:
                return "youtube.com/";
            default:
                return "https://";
        }
    }

    public String text;
    public @Type int type;

    public SocialLink(@NonNull String text, @Type int type) {
        this.text = text;
        this.type = type;
    }

    private SocialLink(Parcel in) {
        text = in.readString();
        type = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(text);
        parcel.writeInt(type);
    }

    public static final Creator<SocialLink> CREATOR = new Creator<SocialLink>() {
        public SocialLink createFromParcel(Parcel in) {
            return new SocialLink(in);
        }

        public SocialLink[] newArray(int size) {
            return new SocialLink[size];
        }
    };

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocialLink that = (SocialLink) o;
        return this.type == that.type && Objects.equals(this.text, that.text);
    }
}
