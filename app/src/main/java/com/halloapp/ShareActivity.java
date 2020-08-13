package com.halloapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.halloapp.id.ChatId;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.CropImageActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.contacts.ContactsActivity;

import java.util.ArrayList;
import java.util.Collections;

public class ShareActivity extends HalloActivity {

    private static final int REQUEST_SELECT_CHAT = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode)
        {
            case REQUEST_SELECT_CHAT:
                if (resultCode == RESULT_OK && data != null) {
                    Intent contentComposer = new Intent(this, ContentComposerActivity.class);
                    final ArrayList<Uri> uris;
                    if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
                        final Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                        if (uri != null) {
                            uris = new ArrayList<>(Collections.singleton(uri));
                        } else {
                            uris = null;
                        }
                    } else {
                        uris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    }
                    String selectedId = data.getStringExtra(ContactsActivity.RESULT_SELECTED_ID);
                    if (selectedId == null) {
                        finish();
                        return;
                    }
                    ChatId chatId = ChatId.fromString(selectedId);
                    contentComposer.putExtra(CropImageActivity.EXTRA_MEDIA, uris);
                    contentComposer.putExtra(ContentComposerActivity.EXTRA_CHAT_ID, chatId);
                    startActivity(contentComposer);
                }
                finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent contactPicker = new Intent(this, ContactsActivity.class);
        startActivityForResult(contactPicker, REQUEST_SELECT_CHAT);
    }
}
