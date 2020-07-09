package com.halloapp.ui.contacts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.util.ComputableLiveData;

import java.util.List;

public class DeviceContactsViewModel extends AndroidViewModel {

    final ComputableLiveData<List<Contact>> contactList;

    public DeviceContactsViewModel(@NonNull Application application) {
        super(application);

        contactList = new ComputableLiveData<List<Contact>>() {

            @Override
            protected List<Contact> compute() {
                return Contact.sort(ContactsDb.getInstance(application).getNonUsers());
            }
        };
    }
}
