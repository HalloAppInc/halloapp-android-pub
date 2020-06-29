package com.halloapp.ui.privacy;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.UserId;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BlockListViewModel extends AndroidViewModel {

    final ComputableLiveData<List<Contact>> blockList;

    private final MutableLiveData<Boolean> inProgress;

    final ContactsDb contactsDb;

    private Executor executor;

    public BlockListViewModel(@NonNull Application application) {
        super(application);
        executor = Executors.newSingleThreadExecutor();
        contactsDb = ContactsDb.getInstance(application);
        blockList = new ComputableLiveData<List<Contact>>() {
            @Override
            protected List<Contact> compute() {
                inProgress.postValue(true);
                List<UserId> ids = null;
                List<Contact> blockList = new ArrayList<>();
                try {
                    ids = Connection.getInstance().getBlockList().get();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
                if (ids != null) {
                    for (UserId userId : ids) {
                        blockList.add(contactsDb.getContact(userId));
                    }
                }
                inProgress.postValue(false);
                return blockList;
            }
        };
        inProgress = new MutableLiveData<>();
    }

    public LiveData<Boolean> getProgressLiveData() {
        return inProgress;
    }

    @MainThread
    public void unblockContact(@NonNull UserId userId) {
        inProgress.setValue(true);
        executor.execute(() -> {
            try {
                Connection.getInstance().unblockUsers(Collections.singleton(userId)).get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            blockList.invalidate();
        });
    }

    public void blockContact(@NonNull UserId userId) {
        inProgress.setValue(true);
        executor.execute(() -> {
            try {
                Connection.getInstance().blockUsers(Collections.singleton(userId)).get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            blockList.invalidate();
        });

    }

}
