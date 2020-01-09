package com.halloapp.contacts;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;

import com.halloapp.util.Log;

public class AddressBookContacts {

    public static final void getAddressBookContacts(@NonNull Context context) {

        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        int idColumnIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);
        int displayNameColumnIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int phoneNumberColumnIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        int normPhoneNumberColumnIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
        int typeColumnIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
        int labelColumnIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
        while (phones.moveToNext()) {

            int id = phones.getInt(idColumnIndex);
            String name = phones.getString(displayNameColumnIndex);
            String phoneNumber = phones.getString(phoneNumberColumnIndex);
            String normPhoneNumber = phones.getString(normPhoneNumberColumnIndex);
            int type = phones.getInt(typeColumnIndex);
            String label = phones.getString(labelColumnIndex);

            Log.i(id + " contact:" + name + " phone:" + phoneNumber + " norm:" + normPhoneNumber + " type:" + ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, label));

        }
        Log.i(phones.getCount() + " contacts");
        phones.close();
    }
}
