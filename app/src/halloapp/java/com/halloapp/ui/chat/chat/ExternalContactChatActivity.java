package com.halloapp.ui.chat.chat;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.ThreadUtils;

public class ExternalContactChatActivity extends HalloActivity {

    private final ContactsDb contactsDb = ContactsDb.getInstance();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getIntent().getData() != null && "content".equals(getIntent().getData().getScheme())) {
            // Doing db read on UI thread intentionally
            // We don't want this activity to really create, we just want it to identify the
            // userid then trampoline to the chat activity
            ThreadUtils.runWithoutStrictModeRestrictions(() -> {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(getIntent().getData(), null, null, null, null);
                    if (cursor != null && cursor.moveToNext()) {
                        String normNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.RawContacts.Data.DATA1));
                        @SuppressLint("WrongThread") UserId userId = contactsDb.getUserForPhoneNumber(normNumber);
                        if (userId != null) {
                            startActivity(ChatActivity.open(this, userId));
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            });
        }

        finish();
    }
}
