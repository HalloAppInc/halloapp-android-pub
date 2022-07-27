package com.halloapp.util;

import android.content.res.Resources;
import android.provider.ContactsContract;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.proto.clients.ContactCard;
import com.halloapp.util.logs.Log;

public class ContactCardUtils {

    private static final String LABEL_HOME = "home";
    private static final String LABEL_WORK = "work";
    private static final String LABEL_OTHER = "other";
    private static final String LABEL_MOBILE = "mobile";
    private static final String LABEL_MAIN = "main";
    private static final String LABEL_WORK_FAX = "work fax";
    private static final String LABEL_HOME_FAX = "home fax";
    private static final String LABEL_PAGER = "pager";

    public static int convertTelephoneTypeToAndroid(@Nullable String label) {
        if (label == null) {
            return ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
        }
        switch (label) {
            case LABEL_HOME: return ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
            case LABEL_MOBILE: return ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
            case LABEL_WORK: return ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
            case LABEL_MAIN: return ContactsContract.CommonDataKinds.Phone.TYPE_MAIN;
            case LABEL_WORK_FAX: return ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK;
            case LABEL_HOME_FAX: return ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME;
            case LABEL_PAGER: return ContactsContract.CommonDataKinds.Phone.TYPE_PAGER;
            case LABEL_OTHER: return ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
            default: return ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM;
        }
    }

    @NonNull
    public static String convertAndroidTelephoneTypeToString(int type, @Nullable String label) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME: return LABEL_HOME;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE: return LABEL_MOBILE;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK: return LABEL_WORK;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK: return LABEL_WORK_FAX;
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME: return LABEL_HOME_FAX;
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER: return LABEL_PAGER;
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER: return LABEL_OTHER;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN: return LABEL_MAIN;
            default: return label == null ? "" : label;
        }
    }

    public static CharSequence getTelephoneLabel(Resources resources, String telephoneType) {
        int type = convertTelephoneTypeToAndroid(telephoneType);
        return ContactsContract.CommonDataKinds.Phone.getTypeLabel(resources, type, telephoneType == null ? "" : telephoneType);
    }

    public static int convertAddressTypeToAndroid(@Nullable String addressType) {
        if (addressType == null) {
            return ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER;
        }
        switch (addressType) {
            case LABEL_HOME: return ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME;
            case LABEL_WORK: return ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK;
            case LABEL_OTHER: return ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER;
            default: return ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM;
        }
    }

    @NonNull
    public static String convertAndroidAddressTypeToString(int type, @Nullable String label) {
        switch (type) {
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME: return LABEL_HOME;
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK: return LABEL_WORK;
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER: return LABEL_OTHER;
            default: return label == null ? "" : label;
        }
    }

    @NonNull
    public static String convertAndroidEmailTypeToString(int type, @Nullable String label) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Email.TYPE_HOME: return LABEL_HOME;
            case ContactsContract.CommonDataKinds.Email.TYPE_WORK: return LABEL_WORK;
            case ContactsContract.CommonDataKinds.Email.TYPE_OTHER: return LABEL_OTHER;
            default: return label == null ? "" : label;
        }
    }

    public static CharSequence getAddressLabel(Resources resources, String addressType) {
        int type = convertAddressTypeToAndroid(addressType);
        return ContactsContract.CommonDataKinds.StructuredPostal.getTypeLabel(resources, type, addressType == null ? "" : addressType);
    }

    public static String serializeContactCard(ContactCard contactCard) {
        return Base64.encodeToString(contactCard.toByteArray(), Base64.NO_WRAP);
    }

    @Nullable
    public static ContactCard deserializeContactCard(String base64Contact) {
        try {
            return ContactCard.parseFrom(Base64.decode(base64Contact, Base64.NO_WRAP));
        } catch (InvalidProtocolBufferException e) {
            Log.e("ContactCardUtils/deserializeContactCard failed", e);
        }
        return null;
    }

}
