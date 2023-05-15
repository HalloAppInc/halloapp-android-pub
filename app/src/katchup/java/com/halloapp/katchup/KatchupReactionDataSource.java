package com.halloapp.katchup;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Reaction;
import com.halloapp.id.UserId;
import com.halloapp.katchup.ui.Colors;
import com.halloapp.ui.groups.GroupParticipants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public abstract class KatchupReactionDataSource extends PositionalDataSource<Reaction> {
    private final ContactsDb contactsDb;
    protected final ContentDb contentDb;
    protected final String postId;

    private final HashMap<UserId, Contact> contactMap;

    private final List<Integer> unusedColors;

    public static class Factory extends DataSource.Factory<Integer, Reaction> {

        private final ContactsDb contactsDb;
        private final ContentDb contentDb;
        private final PublicContentCache publicContentCache;
        private final Function<Void, String> getId;
        private final boolean isPublic;

        private KatchupReactionDataSource latestSource;

        private final HashMap<UserId, Contact> contactMap = new HashMap<>();

        private final List<Integer> unusedColors = new LinkedList<>();

        public Factory(boolean isPublic, @NonNull ContentDb contentDb, @NonNull ContactsDb contactsDb, @NonNull PublicContentCache publicContentCache, @NonNull Function<Void, String> getId) {
            this.contactsDb = contactsDb;
            this.contentDb = contentDb;
            this.publicContentCache = publicContentCache;
            this.getId = getId;
            this.isPublic = isPublic;

            for (int i = 0; i < Colors.COMMENT_COLORS.length; i++) {
                unusedColors.add(i);
            }

            createInternal();
        }

        @Override
        public @NonNull DataSource<Integer, Reaction> create() {
            if (latestSource.isInvalid()) {
                createInternal();
            }
            return latestSource;
        }

        private void createInternal() {
            String postId = getId.apply(null);
            latestSource = isPublic
                    ? new PublicKatchupReactionDataSource(contentDb, contactsDb, publicContentCache, postId, contactMap, unusedColors)
                    : new LocalKatchupReactionDataSource(contentDb, contactsDb, postId, contactMap, unusedColors);
        }

        public void invalidateLatestDataSource() {
            latestSource.invalidate();
        }
    }

    private KatchupReactionDataSource(
            @NonNull ContentDb contentDb,
            @NonNull ContactsDb contactsDb,
            @NonNull String postId,
            @NonNull HashMap<UserId, Contact> contactMap,
            @NonNull List<Integer> unusedColors) {
        this.contentDb = contentDb;
        this.postId = postId;
        this.contactsDb = contactsDb;

        this.contactMap = contactMap;
        this.unusedColors = unusedColors;
    }

    protected void loadExtraFields(@NonNull List<Reaction> reactions) {
        for (Reaction reaction : reactions) {
            if (contactMap.containsKey(reaction.senderUserId)) {
                reaction.senderContact = contactMap.get(reaction.senderUserId);
            } else {
                reaction.senderContact = contactsDb.getContact(reaction.senderUserId);
                reaction.senderContact.setColorIndex(getColorIndex(reaction.senderUserId));
                contactMap.put(reaction.senderUserId, reaction.senderContact);
            }
        }
    }

    private int getColorIndex(UserId userId) {
        int defColor = GroupParticipants.getColorIndex(userId);
        if (unusedColors.size() == 0) {
            return defColor;
        }
        int index = unusedColors.indexOf(defColor);
        if (index != -1) {
            return unusedColors.remove(index);
        }
        index = (int)(Math.random() * unusedColors.size());
        return unusedColors.remove(index);
    }

    private static class LocalKatchupReactionDataSource extends KatchupReactionDataSource {
        private LocalKatchupReactionDataSource(
                @NonNull ContentDb contentDb,
                @NonNull ContactsDb contactsDb,
                @NonNull String postId,
                @NonNull HashMap<UserId, Contact> contactMap,
                @NonNull List<Integer> unusedColors
        ) {
            super(contentDb, contactsDb, postId, contactMap, unusedColors);
        }

        @Override
        public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<Reaction> callback) {
            final List<Reaction> reactions = contentDb.getKatchupPostReactions(postId, params.requestedStartPosition, params.requestedLoadSize);
            loadExtraFields(reactions);
            int count = contentDb.getKatchupPostReactionsCount(postId);
            callback.onResult(reactions, params.requestedStartPosition, count);
        }

        @Override
        public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<Reaction> callback) {
            List<Reaction> reactions = contentDb.getKatchupPostReactions(postId, params.startPosition, params.loadSize);
            loadExtraFields(reactions);
            callback.onResult(reactions);
        }
    }

    private static class PublicKatchupReactionDataSource extends KatchupReactionDataSource {
        private final PublicContentCache publicContentCache;

        private PublicKatchupReactionDataSource(
                @NonNull ContentDb contentDb,
                @NonNull ContactsDb contactsDb,
                @NonNull PublicContentCache publicContentCache,
                @NonNull String postId,
                @NonNull HashMap<UserId, Contact> contactMap,
                @NonNull List<Integer> unusedColors
        ) {
            super(contentDb, contactsDb, postId, contactMap, unusedColors);
            this.publicContentCache = publicContentCache;
        }

        @Override
        public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<Reaction> callback) {
            List<Reaction> reactions = publicContentCache.getReactions(postId);
            if (reactions != null) {
                int fullSize = reactions.size();
                reactions = params.requestedStartPosition >= reactions.size() ? new ArrayList<>() : reactions.subList(params.requestedStartPosition, Math.min(reactions.size(), params.requestedStartPosition + params.requestedLoadSize));
                loadExtraFields(reactions);
                callback.onResult(reactions, params.requestedStartPosition, fullSize);
            } else {
                callback.onResult(new ArrayList<>(), params.requestedStartPosition, 0);
            }
        }

        @Override
        public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<Reaction> callback) {
            List<Reaction> reactions = publicContentCache.getReactions(postId);
            reactions = params.startPosition >= reactions.size() ? new ArrayList<>() : reactions.subList(params.startPosition, Math.min(reactions.size(), params.startPosition + params.loadSize));
            loadExtraFields(reactions);
            callback.onResult(reactions);
        }
    }
}
