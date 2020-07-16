package com.halloapp.ui.mentions;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.halloapp.Me;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Mention;

import java.util.ArrayList;
import java.util.List;

public class MentionsLoader {

    public static List<Mention> loadMentionNames(@NonNull Me me, @NonNull ContactsDb contactsDb, @NonNull List<Mention> mentions) {
        List<Mention> ret = new ArrayList<>();
        for (Mention mention : mentions) {
            String mentionText;
            boolean inAddressBook = false;
            if (mention.userId.isMe()) {
                mentionText = me.getName();
            } else {
                Contact contact = contactsDb.getContact(mention.userId);
                if (!TextUtils.isEmpty(mention.fallbackName)) {
                    contact.fallbackName = mention.fallbackName;
                }
                inAddressBook = contact.addressBookName != null;
                mentionText = contact.getDisplayName();
            }
            Mention updatedMention = new Mention(mention.index, mention.userId, mentionText);
            updatedMention.isInAddressBook = inAddressBook;
            ret.add(updatedMention);
        }
        return ret;
    }
}
