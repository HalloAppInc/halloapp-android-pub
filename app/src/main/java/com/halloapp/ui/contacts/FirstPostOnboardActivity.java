package com.halloapp.ui.contacts;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.DebouncedClickListener;
import com.halloapp.ui.HalloActivity;

import java.util.List;

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

        TextView firstPostText = findViewById(R.id.create_first_post_text);

        contactsViewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        contactsViewModel.contactList.getLiveData().observe(this, contacts -> {
            if (contacts == null) {
                return;
            }
            if (contacts.size() > 5) {
                contacts = contacts.subList(0, 5);
            }
            if (contacts.size() == 0) {
                firstPostText.setText(R.string.create_first_post_no_contacts);
            } else {
                firstPostText.setText(R.string.create_first_post);
            }
            adapter.setContacts(contacts);
        });

        nextButton.setOnClickListener(new DebouncedClickListener() {
            @Override
            public void onOneClick(@NonNull View view) {
                List<Contact> contacts = contactsViewModel.contactList.getLiveData().getValue();
                startActivity(ContentComposerActivity.firstTimePostOnboarding(FirstPostOnboardActivity.this, contacts == null ? 0 : contacts.size()));
            }
        });

    }
}
