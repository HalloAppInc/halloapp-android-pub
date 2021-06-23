package com.halloapp.contacts;

import android.util.LongSparseArray;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class ContactsDiff {

    final Collection<AddressBookContacts.AddressBookContact> added = new ArrayList<>();
    final Collection<Contact> updated = new ArrayList<>();
    final Collection<Long> removedRowIds = new ArrayList<>();
    final Collection<String> removedNormalizedPhones = new ArrayList<>();

    void calculate(Collection<AddressBookContacts.AddressBookContact> addressBookContacts, Collection<Contact> halloContacts) {
        added.clear();
        updated.clear();
        removedRowIds.clear();
        removedNormalizedPhones.clear();

        final LongSparseArray<Contact> halloContactsMap = new LongSparseArray<>();
        final Set<Long> removedRowIds = new HashSet<>();
        final Set<String> removedNormalizedPhones = new HashSet<>();
        for (Contact contact : halloContacts) {
            halloContactsMap.append(contact.addressBookId, contact);
            removedRowIds.add(contact.rowId);
            if (contact.normalizedPhone != null) {
                removedNormalizedPhones.add(contact.normalizedPhone);
            }
        }
        for (AddressBookContacts.AddressBookContact addressBookContact : addressBookContacts) {
            final Contact contact = halloContactsMap.get(addressBookContact.id);
            if (contact == null) {
                added.add(addressBookContact);
            } else {
                if (!Objects.equals(contact.addressBookPhone, addressBookContact.phone)) {
                    contact.addressBookPhone = addressBookContact.phone;
                    contact.addressBookName = addressBookContact.name;
                    contact.halloName = null;
                    contact.normalizedPhone = null;
                    contact.userId = null;
                    updated.add(contact);
                } else if (!Objects.equals(contact.addressBookName, addressBookContact.name)) {
                    contact.addressBookName = addressBookContact.name;
                    updated.add(contact);
                }
                removedRowIds.remove(contact.rowId);
                if (contact.normalizedPhone != null) {
                    removedNormalizedPhones.remove(contact.normalizedPhone);
                }
            }
        }

        this.removedRowIds.addAll(removedRowIds);
        this.removedNormalizedPhones.addAll(removedNormalizedPhones);
    }

    @Override
    public @NonNull String toString() {
        return "ContactsDiff[" + added.size() + " added, " + updated.size() + " updated, " + removedRowIds.size() + " IDs removed]";
    }

    boolean isEmpty() {
        return added.isEmpty() && removedRowIds.isEmpty() && removedNormalizedPhones.isEmpty() && updated.isEmpty();
    }
}
