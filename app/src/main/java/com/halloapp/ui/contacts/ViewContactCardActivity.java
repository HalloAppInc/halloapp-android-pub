package com.halloapp.ui.contacts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.R;
import com.halloapp.proto.clients.ContactCard;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.logs.Log;

public class ViewContactCardActivity extends HalloActivity {

    public static final String EXTRA_CONTACT_CARD = "contact_card";

    public static Intent viewContactCard(Context context, ContactCard contactCard) {
        Intent i = new Intent(context, ViewContactCardActivity.class);
        i.putExtra(EXTRA_CONTACT_CARD, contactCard.toByteArray());
        return i;

    }

    private ContactCard contactCard;

    private ContactCardAdapter adapter = new ContactCardAdapter();

    private RecyclerView contactInfoRv;

    private MenuItem sendMenuItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        byte[] contactCardBytes = getIntent().getByteArrayExtra(EXTRA_CONTACT_CARD);
        try {
            contactCard = ContactCard.parseFrom(contactCardBytes);
        } catch (InvalidProtocolBufferException e) {
            Log.e("ViewContactCardActivity/onCreate failed to parse contact", e);
            finish();
            return;
        }

        setContentView(R.layout.activity_share_contact);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contactInfoRv = findViewById(R.id.contact_info_rv);
        ((SimpleItemAnimator) contactInfoRv.getItemAnimator()).setSupportsChangeAnimations(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        contactInfoRv.setLayoutManager(layoutManager);

        contactInfoRv.setAdapter(adapter);
        adapter.setContactCard(contactCard);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.share_contact_menu, menu);
        sendMenuItem = menu.findItem(R.id.send);
        updateAction(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.send) {
            if (contactCard.getContactsCount() > 0) {
                IntentUtils.showAddContactDialog(this, contactCard.getContacts(0));
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateAction(boolean enabled) {
        if (sendMenuItem != null) {
            SpannableString ss = new SpannableString(getString(R.string.action_add));
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), enabled ? R.color.color_secondary : R.color.disabled_text)), 0, ss.length(), 0);
            sendMenuItem.setTitle(ss);
            sendMenuItem.setEnabled(enabled);
        }
    }

}
