package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.halloapp.content.tables.ArchiveTable;
import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.MentionsTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.content.tables.RepliesTable;

import java.util.ArrayList;
import java.util.List;

public class MentionsDb {
    private final ContentDbHelper databaseHelper;

    public MentionsDb(@NonNull ContentDbHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void addMentions(@NonNull Message message) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        for (Mention mention : message.mentions) {
            final ContentValues mentionItemValues = new ContentValues();
            mentionItemValues.put(MentionsTable.COLUMN_PARENT_TABLE, MessagesTable.TABLE_NAME);
            mentionItemValues.put(MentionsTable.COLUMN_PARENT_ROW_ID, message.rowId);

            mentionItemValues.put(MentionsTable.COLUMN_MENTION_INDEX, mention.index);
            mentionItemValues.put(MentionsTable.COLUMN_MENTION_NAME, mention.fallbackName);
            mentionItemValues.put(MentionsTable.COLUMN_MENTION_USER_ID, mention.userId.rawId());
            mention.rowId = db.insertWithOnConflict(MentionsTable.TABLE_NAME, null, mentionItemValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void addMentions(@NonNull Post post) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        for (Mention mention : post.mentions) {
            final ContentValues mentionItemValues = new ContentValues();

            mentionItemValues.put(MentionsTable.COLUMN_PARENT_TABLE, post.isArchived ? ArchiveTable.TABLE_NAME : PostsTable.TABLE_NAME);
            mentionItemValues.put(MentionsTable.COLUMN_PARENT_ROW_ID, post.rowId);
            mentionItemValues.put(MentionsTable.COLUMN_MENTION_INDEX, mention.index);
            mentionItemValues.put(MentionsTable.COLUMN_MENTION_NAME, mention.fallbackName);
            mentionItemValues.put(MentionsTable.COLUMN_MENTION_USER_ID, mention.userId.rawId());
            mention.rowId = db.insertWithOnConflict(MentionsTable.TABLE_NAME, null, mentionItemValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void addMentions(@NonNull Comment comment) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        for (Mention mention : comment.mentions) {
            final ContentValues mentionItemValues = new ContentValues();
            mentionItemValues.put(MentionsTable.COLUMN_PARENT_TABLE, CommentsTable.TABLE_NAME);
            mentionItemValues.put(MentionsTable.COLUMN_PARENT_ROW_ID, comment.rowId);

            mentionItemValues.put(MentionsTable.COLUMN_MENTION_INDEX, mention.index);
            mentionItemValues.put(MentionsTable.COLUMN_MENTION_NAME, mention.fallbackName);
            mentionItemValues.put(MentionsTable.COLUMN_MENTION_USER_ID, mention.userId.rawId());
            mention.rowId = db.insertWithOnConflict(MentionsTable.TABLE_NAME, null, mentionItemValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void addReplyPreviewMentions(long replyPreviewRowId, List<Mention> mentions) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        for (Mention mention : mentions) {
            final ContentValues mentionItemValues = new ContentValues();
            mentionItemValues.put(MentionsTable.COLUMN_PARENT_TABLE, RepliesTable.TABLE_NAME);
            mentionItemValues.put(MentionsTable.COLUMN_PARENT_ROW_ID, replyPreviewRowId);

            mentionItemValues.put(MentionsTable.COLUMN_MENTION_INDEX, mention.index);
            mentionItemValues.put(MentionsTable.COLUMN_MENTION_NAME, mention.fallbackName);
            mentionItemValues.put(MentionsTable.COLUMN_MENTION_USER_ID, mention.userId.rawId());
            mention.rowId = db.insertWithOnConflict(MentionsTable.TABLE_NAME, null, mentionItemValues, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public void fillMentions(@NonNull Comment comment) {
        comment.mentions.clear();
        comment.mentions.addAll(readMentions(CommentsTable.TABLE_NAME, comment.rowId));
    }

    public void fillMentions(@NonNull Post post) {
        post.mentions.clear();
        if (post.isArchived) {
            post.mentions.addAll(readMentions(ArchiveTable.TABLE_NAME, post.rowId));
        } else {
            post.mentions.addAll(readMentions(PostsTable.TABLE_NAME, post.rowId));
        }
    }

    public void fillMentions(@NonNull Message message) {
        message.mentions.clear();
        message.mentions.addAll(readMentions(MessagesTable.TABLE_NAME, message.rowId));
    }

    public void fillMentions(@NonNull ReplyPreview replyPreview) {
        replyPreview.mentions.clear();
        replyPreview.mentions.addAll(readMentions(RepliesTable.TABLE_NAME, replyPreview.rowId));
    }

    private List<Mention> readMentions(String parentTable, long parentRowId) {
        ArrayList<Mention> mentions = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(MentionsTable.TABLE_NAME,
                new String[]{
                        MentionsTable._ID,
                        MentionsTable.COLUMN_PARENT_TABLE,
                        MentionsTable.COLUMN_PARENT_ROW_ID,
                        MentionsTable.COLUMN_MENTION_INDEX,
                        MentionsTable.COLUMN_MENTION_USER_ID,
                        MentionsTable.COLUMN_MENTION_NAME},
                MentionsTable.COLUMN_PARENT_TABLE + "=? AND " + MentionsTable.COLUMN_PARENT_ROW_ID + "=?",
                new String[] {
                        parentTable,
                        Long.toString(parentRowId)
                },
                null,
                null,
                MentionsTable._ID + " ASC")) {
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                int index = cursor.getInt(3);
                String userId = cursor.getString(4);
                String name = cursor.getString(5);
                mentions.add(new Mention(rowId, index, userId, name));
            }
        }
        return mentions;
    }
}
