package com.halloapp.ui.contacts;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.DebouncedClickListener;
import com.halloapp.ui.HalloActivity;

public class FirstPostOnboardActivity extends HalloActivity {

    private ContactsViewModel contactsViewModel;

    private ConnectedContactsAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_first_post_onboarding);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        View nextButton = findViewById(R.id.next);

        RecyclerView connectedContactsRv = findViewById(R.id.connection_rv);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        connectedContactsRv.setLayoutManager(lm);

        adapter = new ConnectedContactsAdapter(() -> FirstPostOnboardActivity.this);
        connectedContactsRv.setAdapter(adapter);

        contactsViewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        contactsViewModel.contactList.getLiveData().observe(this, contacts -> {
            if (contacts == null) {
                return;
            }
            if (contacts.size() > 5) {
                contacts = contacts.subList(0, 5);
            }
            adapter.setContacts(contacts);
        });

        nextButton.setOnClickListener(new DebouncedClickListener() {
            @Override
            public void onOneClick(@NonNull View view) {
                int sentRequestsSize = adapter.getSelectedFriends().size();
                if (sentRequestsSize > 0) {
                    contactsViewModel.sendFriendRequests(adapter.getSelectedFriends());
                    String infoStr = getResources().getQuantityString(R.plurals.sent_friend_requests, sentRequestsSize, sentRequestsSize);
                    Toast.makeText(FirstPostOnboardActivity.this, infoStr, Toast.LENGTH_SHORT).show();
                }
                startActivity(ContentComposerActivity.firstTimePostOnboarding(FirstPostOnboardActivity.this, sentRequestsSize));
            }
        });

    }
}
