package com.halloapp.katchup;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import com.halloapp.Me;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Mention;
import com.halloapp.id.UserId;
import com.halloapp.katchup.ui.Colors;
import com.halloapp.ui.groups.GroupParticipants;
import com.halloapp.ui.mentions.MentionsLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public abstract class KatchupCommentDataSource extends PositionalDataSource<Comment> {
    private final ContactsDb contactsDb;
    protected final ContentDb contentDb;
    protected final String postId;

    private HashMap<UserId, Contact> contactMap;
    private HashMap<String, Comment> commentMap;

    private List<Integer> unusedColors;

    public static class Factory extends DataSource.Factory<Integer, Comment> {

        private final ContactsDb contactsDb;
        private final ContentDb contentDb;
        private final PublicContentCache publicContentCache;
        private final String postId;
        private final boolean isPublic;

        private KatchupCommentDataSource latestSource;

        private HashMap<UserId, Contact> contactMap = new HashMap<>();
        private HashMap<String, Comment> commentMap = new HashMap<>();

        private List<Integer> unusedColors = new LinkedList<>();

        public Factory(boolean isPublic, @NonNull ContentDb contentDb, @NonNull ContactsDb contactsDb, @NonNull PublicContentCache publicContentCache, @NonNull String postId) {
            this.contactsDb = contactsDb;
            this.contentDb = contentDb;
            this.publicContentCache = publicContentCache;
            this.postId = postId;
            this.isPublic = isPublic;

            for (int i = 0; i < Colors.COMMENT_COLORS.length; i++) {
                unusedColors.add(i);
            }

            createInternal();
        }

        @Override
        public @NonNull DataSource<Integer, Comment> create() {
            if (latestSource.isInvalid()) {
                createInternal();
            }
            return latestSource;
        }

        private void createInternal() {
            latestSource = isPublic
                    ? new PublicKatchupCommentsDataSource(contentDb, contactsDb, publicContentCache, postId, contactMap, commentMap, unusedColors)
                    : new LocalKatchupCommentsDataSource(contentDb, contactsDb, postId, contactMap, commentMap, unusedColors);
        }

        public void invalidateLatestDataSource() {
            latestSource.invalidate();
        }
    }

    private KatchupCommentDataSource(
            @NonNull ContentDb contentDb,
            @NonNull ContactsDb contactsDb,
            @NonNull String postId,
            @NonNull HashMap<UserId, Contact> contactMap,
            @NonNull HashMap<String, Comment> commentMap,
            @NonNull List<Integer> unusedColors) {
        this.contentDb = contentDb;
        this.postId = postId;
        this.contactsDb = contactsDb;

        this.contactMap = contactMap;
        this.commentMap = commentMap;
        this.unusedColors = unusedColors;
    }

    protected void loadExtraFields(@NonNull List<Comment> comments) {
        for (Comment comment : comments) {
            commentMap.put(comment.id, comment);
        }

        for (Comment comment : comments) {
            fillComment(comment, contactMap);

            if (comment.parentCommentId != null) {
                if (commentMap.containsKey(comment.parentCommentId)) {
                    comment.parentComment = commentMap.get(comment.parentCommentId);
                } else {
                    comment.parentComment = contentDb.getComment(comment.parentCommentId);
                    if (comment.parentComment != null) {
                        fillComment(comment.parentComment, contactMap);
                    }
                    commentMap.put(comment.parentCommentId, comment.parentComment);
                }
            }
        }
    }

    private void fillComment(@NonNull Comment comment, @NonNull HashMap<UserId, Contact> contactMap) {
        fillSenderContact(comment, contactMap);
        if (!comment.mentions.isEmpty()) {
            List<Mention> mentions = MentionsLoader.loadMentionNames(Me.getInstance(), contactsDb, comment.mentions);
            comment.mentions.clear();
            comment.mentions.addAll(mentions);
        }
    }

    private void fillSenderContact(@NonNull Comment comment, @NonNull HashMap<UserId, Contact> contactMap) {
        if (contactMap.containsKey(comment.senderUserId)) {
            comment.senderContact = contactMap.get(comment.senderUserId);
        } else {
            comment.senderContact = contactsDb.getContact(comment.senderUserId);
            comment.senderContact.setColorIndex(getColorIndex(comment.senderUserId));
            contactMap.put(comment.senderUserId, comment.senderContact);
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

    private static class LocalKatchupCommentsDataSource extends KatchupCommentDataSource {
        private LocalKatchupCommentsDataSource(
                @NonNull ContentDb contentDb,
                @NonNull ContactsDb contactsDb,
                @NonNull String postId,
                @NonNull HashMap<UserId, Contact> contactMap,
                @NonNull HashMap<String, Comment> commentMap,
                @NonNull List<Integer> unusedColors
        ) {
            super(contentDb, contactsDb, postId, contactMap, commentMap, unusedColors);
        }

        @Override
        public void loadInitial(@NonNull PositionalDataSource.LoadInitialParams params, @NonNull PositionalDataSource.LoadInitialCallback<Comment> callback) {
            final List<Comment> comments = contentDb.getCommentsKatchup(postId, params.requestedStartPosition, params.requestedLoadSize);
            loadExtraFields(comments);
            int count = contentDb.getCommentsKatchupCount(postId);
            callback.onResult(comments, params.requestedStartPosition, count);
        }

        @Override
        public void loadRange(@NonNull PositionalDataSource.LoadRangeParams params, @NonNull PositionalDataSource.LoadRangeCallback<Comment> callback) {
            List<Comment> comments = contentDb.getCommentsKatchup(postId, params.startPosition, params.loadSize);
            loadExtraFields(comments);
            callback.onResult(comments);
        }
    }

    private static class PublicKatchupCommentsDataSource extends KatchupCommentDataSource {
        private final PublicContentCache publicContentCache;

        private PublicKatchupCommentsDataSource(
                @NonNull ContentDb contentDb,
                @NonNull ContactsDb contactsDb,
                @NonNull PublicContentCache publicContentCache,
                @NonNull String postId,
                @NonNull HashMap<UserId, Contact> contactMap,
                @NonNull HashMap<String, Comment> commentMap,
                @NonNull List<Integer> unusedColors
        ) {
            super(contentDb, contactsDb, postId, contactMap, commentMap, unusedColors);
            this.publicContentCache = publicContentCache;
        }

        @Override
        public void loadInitial(@NonNull PositionalDataSource.LoadInitialParams params, @NonNull PositionalDataSource.LoadInitialCallback<Comment> callback) {
            List<Comment> comments = publicContentCache.getComments(postId);
            int fullSize = comments.size();
            comments = params.requestedStartPosition >= comments.size() ? new ArrayList<>() : comments.subList(params.requestedStartPosition, Math.min(comments.size(), params.requestedStartPosition + params.requestedLoadSize));
            loadExtraFields(comments);
            callback.onResult(comments, params.requestedStartPosition, fullSize);
        }

        @Override
        public void loadRange(@NonNull PositionalDataSource.LoadRangeParams params, @NonNull PositionalDataSource.LoadRangeCallback<Comment> callback) {
            List<Comment> comments = publicContentCache.getComments(postId);
            comments = params.startPosition >= comments.size() ? new ArrayList<>() : comments.subList(params.startPosition, Math.min(comments.size(), params.startPosition + params.loadSize));
            loadExtraFields(comments);
            callback.onResult(comments);
        }
    }
}
