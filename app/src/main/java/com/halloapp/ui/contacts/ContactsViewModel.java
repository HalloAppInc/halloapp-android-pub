package com.halloapp.ui.contacts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.util.ComputableLiveData;

import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ContactsViewModel extends AndroidViewModel {

    final ComputableLiveData<List<Contact>> contactList;

    public ContactsViewModel(@NonNull Application application) {
        super(application);

        contactList = new ComputableLiveData<List<Contact>>() {

            @Override
            protected List<Contact> compute() {
                List<Contact> contacts = ContactsDb.getInstance(application).getUsers();
                Collator collator = Collator.getInstance(Locale.getDefault());
                Collections.sort(contacts, (o1, o2) -> {
                    boolean alpha1 = Character.isAlphabetic(o1.getDisplayName().codePointAt(0));
                    boolean alpha2 = Character.isAlphabetic(o2.getDisplayName().codePointAt(0));
                    if (alpha1 == alpha2) {
                        return collator.compare(o1.getDisplayName(), o2.getDisplayName());
                    } else {
                        return alpha1 ? -1 : 1;
                    }
                });
                return contacts;
            }
        };
    }
}
