package com.halloapp.ui.messages;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.core.util.Preconditions;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MessagesViewModel extends AndroidViewModel {

    final MutableLiveData<List<Contact>> contactsList;
    private final ContactsDb contactsDb;

    private final ContactsDb.Observer contactsObserver = this::loadData;


    public MessagesViewModel(@NonNull Application application) {
        super(application);

        contactsDb = ContactsDb.getInstance(application);
        contactsDb.addObserver(contactsObserver);

        contactsList = new MutableLiveData<>();
        loadData();
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
    }

    private void loadData() {
        new LoadContactsTask(getApplication(), contactsList).execute();
    }


    static class LoadContactsTask extends AsyncTask<Void, Void, List<Contact>> {

        private final Application application;
        private final MutableLiveData<List<Contact>> data;

        LoadContactsTask(@NonNull Application application, @NonNull MutableLiveData<List<Contact>> data) {
            this.application = application;
            this.data = data;
        }

        @Override
        protected List<Contact> doInBackground(Void... voids) {
            List<Contact> contacts = ContactsDb.getInstance(application).getMemberContacts();
            final Collator collator = java.text.Collator.getInstance(Locale.getDefault());
            Collections.sort(contacts, (obj1, obj2) -> collator.compare(obj1.getDisplayName(), obj2.getDisplayName()));
            return contacts;
        }

        @Override
        protected void onPostExecute(final List<Contact> contacts) {
            data.postValue(contacts);
        }
    }

}