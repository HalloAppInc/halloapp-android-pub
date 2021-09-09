package com.halloapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.halloapp.id.ChatId;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.contacts.ContactsActivity;
import com.halloapp.ui.mediaedit.MediaEditActivity;

import java.util.ArrayList;
import java.util.Collections;

public class ShareActivity extends HalloActivity {

    private static final int REQUEST_SELECT_CHAT = 1;
    private static final int REQUEST_CONTENT_COMPOSER = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
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
                    ChatId chatId = ChatId.fromNullable(selectedId);
                    contentComposer.putExtra(MediaEditActivity.EXTRA_MEDIA, uris);
                    contentComposer.putExtra(ContentComposerActivity.EXTRA_CHAT_ID, chatId);
                    contentComposer.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra(Intent.EXTRA_TEXT));
                    startActivityForResult(contentComposer, REQUEST_CONTENT_COMPOSER);
                } else {
                    finish();
                }
                break;
            default: {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent contactPicker = ContactsActivity.createSharePicker(this);
        startActivityForResult(contactPicker, REQUEST_SELECT_CHAT);
    }
}
