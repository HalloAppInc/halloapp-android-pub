package com.halloapp.contacts;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.halloapp.util.logs.Log;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AddressBookContacts {

    static class AddressBookContact {

        final long id;
        final String name;
        final String phone;

        AddressBookContact(long id, @NonNull String name, @NonNull String phone) {
            this.id = id;
            this.name = name;
            this.phone = phone;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final AddressBookContact that = (AddressBookContact) o;
            return name.equals(that.name) && phone.equals(that.phone);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, phone);
        }
    }

    static Collection<AddressBookContact> getAddressBookContacts(@NonNull Context context) {

        final Set<AddressBookContact> contacts = new HashSet<>();
        try (Cursor contactsCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null)) {
            if (contactsCursor == null) {
                Log.e("AddressBookContacts: no contacts cursor");
                return null;
            }
            final int idColumnIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);
            final int nameColumnIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            final int phoneColumnIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (contactsCursor.moveToNext()) {
                final long id = contactsCursor.getLong(idColumnIndex);
                final String name = contactsCursor.getString(nameColumnIndex);
                final String phone = contactsCursor.getString(phoneColumnIndex);
                if (!TextUtils.isEmpty(phone)) {
                    contacts.add(new AddressBookContact(id, name == null ? "" : name, phone));
                }
            }
            Log.i("AddressBookContacts: " + contacts.size() + " contacts");
            return contacts;
        } catch (SecurityException ex) {
            Log.e("AddressBookContacts", ex);
            return null;
        }
    }

    public static Set<String> fetchWANumbers(@NonNull Context context) {
        String selection = "account_type IN (?)";
        String[] selectionArgs = new String[] { "com.whatsapp" };

        final Set<String> contacts = new HashSet<>();
        try (Cursor contactsCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,selection,selectionArgs, null)) {
            if (contactsCursor == null) {
                Log.e("AddressBookContacts/fetchWAnumbers: no contacts cursor");
                return null;
            }
            final int phoneColumnIndex = contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
            while (contactsCursor.moveToNext()) {
                final String phone = contactsCursor.getString(phoneColumnIndex);
                if (!TextUtils.isEmpty(phone)) {
                    contacts.add(phone.replaceAll( "[^\\d]", ""));
                }
            }
            Log.i("AddressBookContacts/fetchWANumbers: " + contacts.size() + " wa numbers");
            return contacts;
        } catch (SecurityException ex) {
            Log.e("AddressBookContacts/fetchWANumbers", ex);
            return null;
        }
    }
}
