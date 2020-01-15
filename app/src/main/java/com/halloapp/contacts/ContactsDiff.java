package com.halloapp.contacts;

import android.util.LongSparseArray;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ContactsDiff {

    final Collection<AddressBookContacts.AddressBookContact> added = new ArrayList<>();
    final Collection<Long> removed = new ArrayList<>();
    final Collection<Contact> updated = new ArrayList<>();

    void calculate(Collection<AddressBookContacts.AddressBookContact> addressBookContacts, Collection<Contact> halloContacts) {
        final LongSparseArray<Contact> halloContactsMap = new LongSparseArray<>();
        for (Contact contact : halloContacts) {
            halloContactsMap.append(contact.addressBookId, contact);
        }
        final Set<Long> halloContactsInAddressBook = new HashSet<>();
        for (AddressBookContacts.AddressBookContact addressBookContact : addressBookContacts) {
            final Contact contact = halloContactsMap.get(addressBookContact.id);
            if (contact == null) {
                added.add(addressBookContact);
            } else {
                if (!Objects.equals(contact.phone, addressBookContact.phone)) {
                    contact.phone = addressBookContact.phone;
                    contact.name = addressBookContact.name;
                    contact.jid = null;
                    contact.member = false;
                    updated.add(contact);
                } else if (!Objects.equals(contact.name, addressBookContact.name)) {
                    contact.name = addressBookContact.name;
                    updated.add(contact);
                }
                halloContactsInAddressBook.add(contact.id);
            }
        }
        for (Contact contact : halloContacts) {
            if (!halloContactsInAddressBook.contains(contact.id)) {
                removed.add(contact.id);
            }
        }
    }

    @Override
    public @NonNull String toString() {
        return "ContactsDiff[" + added.size() + " added, " + updated.size() + " updated, " + removed.size() + " removed]";
    }

    boolean isEmpty() {
        return added.isEmpty() && removed.isEmpty() && updated.isEmpty();
    }
}
