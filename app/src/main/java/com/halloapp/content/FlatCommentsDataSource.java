package com.halloapp.content;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import com.halloapp.Me;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.ui.groups.GroupParticipants;
import com.halloapp.ui.mentions.MentionsLoader;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FlatCommentsDataSource extends PositionalDataSource<Comment> {

    private final ContactsDb contactsDb;
    private final ContentDb contentDb;
    private final String postId;

    public static class Factory extends DataSource.Factory<Integer, Comment> {

        private final ContactsDb contactsDb;
        private final ContentDb contentDb;
        private final String postId;

        private FlatCommentsDataSource latestSource;

        public Factory(@NonNull ContentDb contentDb, @NonNull ContactsDb contactsDb, @NonNull String postId) {
            this.contactsDb = contactsDb;
            this.contentDb = contentDb;
            this.postId = postId;

            latestSource = new FlatCommentsDataSource(contentDb, contactsDb, postId);
        }

        @Override
        public @NonNull DataSource<Integer, Comment> create() {
            if (latestSource.isInvalid()) {
                latestSource = new FlatCommentsDataSource(contentDb, contactsDb, postId);
            }
            return latestSource;
        }

        public void invalidateLatestDataSource() {
            latestSource.invalidate();
        }
    }

    private HashMap<UserId, Contact> contactMap = new HashMap<>();
    private HashMap<String, Comment> commentMap = new HashMap<>();

    private List<Integer> unusedColors = new LinkedList<>();

    private FlatCommentsDataSource(@NonNull ContentDb contentDb, @NonNull ContactsDb contactsDb, @NonNull String postId) {
        this.contentDb = contentDb;
        this.postId = postId;
        this.contactsDb = contactsDb;
        for (int i = 0; i < GroupParticipants.PARTICIPANT_COLORS.length; i++) {
            unusedColors.add(i);
        }
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<Comment> callback) {
        final List<Comment> comments = contentDb.getCommentsFlat(postId, params.requestedStartPosition, params.requestedLoadSize);
        loadExtraFields(comments);
        int count = contentDb.getCommentsFlatCount(postId);
        callback.onResult(comments, params.requestedStartPosition, count);
    }

    private void loadExtraFields(@NonNull List<Comment> comments) {
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

    @Override
    public void loadRange(@NonNull LoadRangeParams params, @NonNull LoadRangeCallback<Comment> callback) {
        List<Comment> comments = contentDb.getCommentsFlat(postId, params.startPosition, params.loadSize);
        loadExtraFields(comments);
        callback.onResult(comments);
    }
}
