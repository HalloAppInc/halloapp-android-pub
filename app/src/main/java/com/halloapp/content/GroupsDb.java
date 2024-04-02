package com.halloapp.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.halloapp.AppContext;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.content.tables.ChatsTable;
import com.halloapp.content.tables.DeletedGroupNameTable;
import com.halloapp.content.tables.GroupMembersTable;
import com.halloapp.content.tables.GroupsTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.GroupsSync;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.Background;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.groups.MemberElement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GroupsDb {

    private final FileStore fileStore;
    private final ContentDbHelper databaseHelper;

    public GroupsDb(@NonNull FileStore fileStore, @NonNull ContentDbHelper databaseHelper) {
        this.fileStore = fileStore;
        this.databaseHelper = databaseHelper;
    }

    @WorkerThread
    @Nullable Group getGroup(@NonNull GroupId groupId) {
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(GroupsTable.TABLE_NAME,
                new String [] {
                        GroupsTable._ID,
                        GroupsTable.COLUMN_GROUP_ID,
                        GroupsTable.COLUMN_TIMESTAMP,
                        GroupsTable.COLUMN_GROUP_NAME,
                        GroupsTable.COLUMN_GROUP_DESCRIPTION,
                        GroupsTable.COLUMN_GROUP_AVATAR_ID,
                        GroupsTable.COLUMN_IS_ACTIVE,
                        GroupsTable.COLUMN_THEME,
                        GroupsTable.COLUMN_INVITE_LINK,
                        GroupsTable.COLUMN_EXPIRATION_TYPE,
                        GroupsTable.COLUMN_EXPIRATION_TIME},
                GroupsTable.COLUMN_GROUP_ID + "=?",
                new String [] {groupId.rawId()},
                null, null, null)) {
            if (cursor.moveToNext()) {
                int expirationType = cursor.getInt(9);
                long expirationTime = cursor.getLong(10);
                Group group = new Group(
                        cursor.getLong(0),
                        GroupId.fromNullable(cursor.getString(1)),
                        cursor.getLong(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getInt(6) == 1,
                        cursor.getInt(7),
                        parseExpiryInfo(expirationType, expirationTime));
                group.inviteToken = cursor.getString(8);
                return group;
            }
        }
        return null;
    }

    private ExpiryInfo parseExpiryInfo(int type, long time) {
        ExpiryInfo.Builder expiryBuilder = ExpiryInfo.newBuilder().setExpiryTypeValue(type);
        if (type == ExpiryInfo.ExpiryType.CUSTOM_DATE_VALUE) {
            expiryBuilder.setExpiryTimestamp(time);
        } else if (type == ExpiryInfo.ExpiryType.EXPIRES_IN_SECONDS_VALUE) {
            expiryBuilder.setExpiresInSeconds(time);
        }
        return expiryBuilder.build();
    }

    @WorkerThread
    boolean addGroup(@NonNull GroupInfo groupInfo) {
        return addGroup(groupInfo, 0L);
    }

    @WorkerThread
    boolean addGroup(@NonNull GroupInfo groupInfo, long addedTimestamp) {
        Log.i("GroupDb.addGroupChat " + groupInfo.groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(GroupsTable.COLUMN_GROUP_ID, groupInfo.groupId.rawId());
            chatValues.put(GroupsTable.COLUMN_GROUP_NAME, groupInfo.name);
            chatValues.put(GroupsTable.COLUMN_GROUP_DESCRIPTION, groupInfo.description);
            chatValues.put(GroupsTable.COLUMN_GROUP_AVATAR_ID, groupInfo.avatar);
            chatValues.put(GroupsTable.COLUMN_ADDED_TIMESTAMP, addedTimestamp);
            if (groupInfo.expiryInfo != null) {
                chatValues.put(GroupsTable.COLUMN_EXPIRATION_TYPE, groupInfo.expiryInfo.getExpiryTypeValue());
                if (groupInfo.expiryInfo.getExpiryType().equals(ExpiryInfo.ExpiryType.EXPIRES_IN_SECONDS)) {
                    chatValues.put(GroupsTable.COLUMN_EXPIRATION_TIME, groupInfo.expiryInfo.getExpiresInSeconds());
                } else if (groupInfo.expiryInfo.getExpiryType().equals(ExpiryInfo.ExpiryType.CUSTOM_DATE)) {
                    chatValues.put(GroupsTable.COLUMN_EXPIRATION_TIME, groupInfo.expiryInfo.getExpiryTimestamp());
                } else {
                    chatValues.put(GroupsTable.COLUMN_EXPIRATION_TIME, 0);
                }
            }

            boolean exists;
            try (Cursor cursor = db.rawQuery("SELECT * FROM " + GroupsTable.TABLE_NAME + " WHERE " + GroupsTable.COLUMN_GROUP_ID + "=?", new String[]{groupInfo.groupId.rawId()})) {
                exists = cursor.getCount() > 0;
            }

            if (exists) {
                db.update(GroupsTable.TABLE_NAME, chatValues, GroupsTable.COLUMN_GROUP_ID + "=?", new String[]{groupInfo.groupId.rawId()});
            } else {
                db.insertWithOnConflict(GroupsTable.TABLE_NAME, null, chatValues, SQLiteDatabase.CONFLICT_REPLACE);
            }

            if (groupInfo.members != null) {
                for (MemberInfo member : groupInfo.members) {
                    final ContentValues memberValues = new ContentValues();
                    memberValues.put(GroupMembersTable.COLUMN_GROUP_ID, groupInfo.groupId.rawId());
                    memberValues.put(GroupMembersTable.COLUMN_USER_ID, member.userId.rawId());
                    memberValues.put(GroupMembersTable.COLUMN_IS_ADMIN, MemberElement.Type.ADMIN.equals(member.type) ? 1 : 0);
                    db.insertWithOnConflict(GroupMembersTable.TABLE_NAME, null, memberValues, SQLiteDatabase.CONFLICT_ABORT);
                }
            }
            db.delete(DeletedGroupNameTable.TABLE_NAME, DeletedGroupNameTable.COLUMN_CHAT_ID + "=?", new String[]{groupInfo.groupId.rawId()});
            db.setTransactionSuccessful();
            Log.i("ContentDb.addGroupChat: added " + groupInfo.groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.addGroupChat: duplicate " + ex.getMessage() + " " + groupInfo.groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean updateGroupFeed(@NonNull GroupInfo groupInfo) {
        Log.i("GroupDb.updateGroupChat " + groupInfo.groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(GroupsTable.COLUMN_GROUP_NAME, groupInfo.name);
            chatValues.put(GroupsTable.COLUMN_GROUP_DESCRIPTION, groupInfo.description);
            chatValues.put(GroupsTable.COLUMN_GROUP_AVATAR_ID, groupInfo.avatar);
            if (groupInfo.background != null) {
                chatValues.put(GroupsTable.COLUMN_THEME, groupInfo.background.getTheme());
            }
            if (groupInfo.expiryInfo != null) {
                chatValues.put(GroupsTable.COLUMN_EXPIRATION_TYPE, groupInfo.expiryInfo.getExpiryTypeValue());
                if (groupInfo.expiryInfo.getExpiryType().equals(ExpiryInfo.ExpiryType.EXPIRES_IN_SECONDS)) {
                    chatValues.put(GroupsTable.COLUMN_EXPIRATION_TIME, groupInfo.expiryInfo.getExpiresInSeconds());
                } else if (groupInfo.expiryInfo.getExpiryType().equals(ExpiryInfo.ExpiryType.CUSTOM_DATE)) {
                    chatValues.put(GroupsTable.COLUMN_EXPIRATION_TIME, groupInfo.expiryInfo.getExpiryTimestamp());
                } else {
                    chatValues.put(GroupsTable.COLUMN_EXPIRATION_TIME, 0);
                }
            }
            db.update(GroupsTable.TABLE_NAME, chatValues, GroupsTable.COLUMN_GROUP_ID + "=?", new String[]{groupInfo.groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.updateGroupChat: updated " + groupInfo.groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.updateGroupChat: " + ex.getMessage() + " " + groupInfo.groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    void updateGroupTimestamp(@NonNull GroupId groupId, long timestamp) {
        Log.i("GroupsDb.updateGroupTimestamp " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        try (SQLiteStatement statement = db.compileStatement("UPDATE " + GroupsTable.TABLE_NAME + " SET " +
                GroupsTable.COLUMN_TIMESTAMP + "=" + timestamp + " WHERE " + GroupsTable.COLUMN_GROUP_ID + "='" + groupId.rawId() + "'")) {
            statement.executeUpdateDelete();
        }
    }

    @WorkerThread
    boolean setGroupTheme(@NonNull GroupId groupId, int theme) {
        Log.i("GroupsDb.setGroupBackground " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(GroupsTable.COLUMN_THEME, theme);
            db.update(GroupsTable.TABLE_NAME, chatValues, GroupsTable.COLUMN_GROUP_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.setGroupBackground: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.setGroupBackground: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setGroupActive(@NonNull GroupId groupId) {
        Log.i("GroupDb.setGroupActive " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(GroupsTable.COLUMN_IS_ACTIVE, 1);
            db.update(GroupsTable.TABLE_NAME, chatValues, GroupsTable.COLUMN_GROUP_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.setGroupActive: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.setGroupActive: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setGroupAvatar(@NonNull GroupId groupId, @NonNull String avatarId) {
        Log.i("GroupDb.setGroupAvatar " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(GroupsTable.COLUMN_GROUP_AVATAR_ID, avatarId);
            db.update(GroupsTable.TABLE_NAME, chatValues, GroupsTable.COLUMN_GROUP_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.setGroupAvatar: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.setGroupAvatar: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean addRemoveGroupMembers(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberInfo> added, @NonNull List<MemberInfo> removed) {
        Log.i("GroupDb.addRemoveGroupMembers " + groupId);
        boolean meAdded = false;
        for (MemberInfo memberInfo : added) {
            if (memberInfo.userId.isMe()) {
                meAdded = true;
                break;
            }
        }
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            boolean groupExists;
            try (Cursor cursor = db.rawQuery("SELECT * FROM " + GroupsTable.TABLE_NAME + " WHERE " + GroupsTable.COLUMN_GROUP_ID + "=?", new String[]{groupId.rawId()})) {
                groupExists = cursor.getCount() > 0;
            }
            if (!groupExists) {
                try (Cursor cursor = db.rawQuery("SELECT * FROM " + ChatsTable.TABLE_NAME + " WHERE " + ChatsTable.COLUMN_CHAT_ID + "=?", new String[]{groupId.rawId()})) {
                    groupExists = cursor.getCount() > 0;
                }
            }
            if (!groupExists) {
                long addedTimestamp = meAdded ? System.currentTimeMillis() : 0L;
                // TODO:  decide how we want to handle if the group doesnt exist
                //addGroup(new GroupInfo(groupId, groupName, null, avatarId, Background.getDefaultInstance(), new ArrayList<>(), null), addedTimestamp);
                GroupsSync.getInstance(AppContext.getInstance().get()).forceGroupSync();
            }

            for (MemberInfo member : added) {
                final ContentValues memberValues = new ContentValues();
                memberValues.put(GroupMembersTable.COLUMN_GROUP_ID, groupId.rawId());
                memberValues.put(GroupMembersTable.COLUMN_USER_ID, member.userId.rawId());
                memberValues.put(GroupMembersTable.COLUMN_IS_ADMIN, MemberElement.Type.ADMIN.equals(member.type) ? 1 : 0);
                db.insertWithOnConflict(GroupMembersTable.TABLE_NAME, null, memberValues, SQLiteDatabase.CONFLICT_IGNORE);
            }

            for (MemberInfo member : removed) {
                db.delete(
                        GroupMembersTable.TABLE_NAME,
                        GroupMembersTable.COLUMN_GROUP_ID + "=? AND " + GroupMembersTable.COLUMN_USER_ID + "=?",
                        new String[]{groupId.rawId(), member.userId.rawId()}
                );
            }

            db.setTransactionSuccessful();
            Log.i("ContentDb.addGroupMembers: added " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.addGroupMembers: duplicate " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean promoteDemoteGroupAdmins(@NonNull GroupId groupId, @NonNull List<MemberInfo> promoted, @NonNull List<MemberInfo> demoted) {
        Log.i("GroupDb.promoteDemoteGroupAdmins " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (MemberInfo member : promoted) {
                final ContentValues memberValues = new ContentValues();
                memberValues.put(GroupMembersTable.COLUMN_IS_ADMIN, 1);

                db.update(GroupMembersTable.TABLE_NAME,
                        memberValues,
                        GroupMembersTable.COLUMN_GROUP_ID + "=? AND " + GroupMembersTable.COLUMN_USER_ID + "=?",
                        new String[]{groupId.rawId(), member.userId.rawId()});
            }

            for (MemberInfo member : demoted) {
                final ContentValues memberValues = new ContentValues();
                memberValues.put(GroupMembersTable.COLUMN_IS_ADMIN, 0);

                db.update(GroupMembersTable.TABLE_NAME,
                        memberValues,
                        GroupMembersTable.COLUMN_GROUP_ID + "=? AND " + GroupMembersTable.COLUMN_USER_ID + "=?",
                        new String[]{groupId.rawId(), member.userId.rawId()});
            }

            db.setTransactionSuccessful();
            Log.i("ContentDb.promoteDemoteGroupAdmins: done " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.promoteDemoteGroupAdmins: error " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setGroupInviteLinkToken(@NonNull GroupId groupId, @Nullable String inviteToken) {
        Log.i("GroupsDb.setGroupInviteLinkToken " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(GroupsTable.COLUMN_INVITE_LINK, inviteToken);
            db.update(GroupsTable.TABLE_NAME, chatValues, GroupsTable.COLUMN_GROUP_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.setGroupInviteLinkToken: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.setGroupInviteLinkToken: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setGroupExpiry(@NonNull GroupId groupId, @NonNull ExpiryInfo expiryInfo) {
        Log.i("GroupsDb.setGroupExpiry " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(GroupsTable.COLUMN_EXPIRATION_TYPE, expiryInfo.getExpiryTypeValue());
            if (expiryInfo.getExpiryType().equals(ExpiryInfo.ExpiryType.EXPIRES_IN_SECONDS)) {
                chatValues.put(GroupsTable.COLUMN_EXPIRATION_TIME, expiryInfo.getExpiresInSeconds());
            } else if (expiryInfo.getExpiryType().equals(ExpiryInfo.ExpiryType.CUSTOM_DATE)) {
                chatValues.put(GroupsTable.COLUMN_EXPIRATION_TIME, expiryInfo.getExpiryTimestamp());
            } else {
                chatValues.put(GroupsTable.COLUMN_EXPIRATION_TIME, 0);
            }
            db.update(GroupsTable.TABLE_NAME, chatValues, GroupsTable.COLUMN_GROUP_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.setGroupExpiry: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.setGroupExpiry: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setGroupName(@NonNull GroupId groupId, @NonNull String name) {
        Log.i("GroupsDb.setGroupName " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(GroupsTable.COLUMN_GROUP_NAME, name);
            db.update(GroupsTable.TABLE_NAME, chatValues, GroupsTable.COLUMN_GROUP_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.setGroupName: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.setGroupName: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setGroupDescription(@NonNull GroupId groupId, @Nullable String description) {
        Log.i("GroupsDb.setGroupDescription " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(GroupsTable.COLUMN_GROUP_DESCRIPTION, description);
            db.update(GroupsTable.TABLE_NAME, chatValues, GroupsTable.COLUMN_GROUP_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.setGroupDescription: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.setGroupDescription: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    boolean setGroupInactive(@NonNull GroupId groupId) {
        Log.i("GroupsDb.setGroupInactive " + groupId);
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final ContentValues chatValues = new ContentValues();
            chatValues.put(GroupsTable.COLUMN_IS_ACTIVE, 0);
            db.update(GroupsTable.TABLE_NAME, chatValues, GroupsTable.COLUMN_GROUP_ID + "=?", new String[]{groupId.rawId()});

            db.setTransactionSuccessful();
            Log.i("ContentDb.setGroupInactive: success " + groupId);
        } catch (SQLiteConstraintException ex) {
            Log.w("ContentDb.setGroupInactive: " + ex.getMessage() + " " + groupId);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @WorkerThread
    @NonNull List<GroupId> getGroupsInCommon(@NonNull UserId userId) {
        final List<GroupId> groups = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(GroupMembersTable.TABLE_NAME,
                new String [] {
                        GroupMembersTable.COLUMN_GROUP_ID},
                GroupMembersTable.COLUMN_USER_ID + "=? OR " + GroupMembersTable.COLUMN_USER_ID + "=?",
                new String[]{"", userId.rawId()}, GroupMembersTable.COLUMN_GROUP_ID, "COUNT(DISTINCT " + GroupMembersTable.COLUMN_USER_ID + ") > 1", null)) {
            while (cursor.moveToNext()) {
                groups.add(GroupId.fromNullable(cursor.getString(0)));
            }
        }
        return groups;
    }

    @WorkerThread
    @NonNull
    List<MemberInfo> getGroupMembers(GroupId groupId) {
        final List<MemberInfo> members = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(GroupMembersTable.TABLE_NAME,
                new String [] {
                        GroupMembersTable._ID,
                        GroupMembersTable.COLUMN_USER_ID,
                        GroupMembersTable.COLUMN_IS_ADMIN},
                GroupMembersTable.COLUMN_GROUP_ID + "=?",
                new String[]{groupId.rawId()}, null, null, null)) {
            while (cursor.moveToNext()) {
                final MemberInfo member = new MemberInfo(
                        cursor.getLong(0),
                        new UserId(cursor.getString(1)),
                        cursor.getInt(2) == 1 ? MemberElement.Type.ADMIN : MemberElement.Type.MEMBER,
                        null);
                members.add(member);
            }
        }
        return members;
    }

    @WorkerThread
    @NonNull List<Group> getActiveGroups() {
        final List<Group> groups = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(GroupsTable.TABLE_NAME,
                new String [] {
                        GroupsTable._ID,
                        GroupsTable.COLUMN_GROUP_ID,
                        GroupsTable.COLUMN_TIMESTAMP,
                        GroupsTable.COLUMN_GROUP_NAME,
                        GroupsTable.COLUMN_GROUP_DESCRIPTION,
                        GroupsTable.COLUMN_GROUP_AVATAR_ID,
                        GroupsTable.COLUMN_IS_ACTIVE,
                        GroupsTable.COLUMN_THEME,
                        GroupsTable.COLUMN_INVITE_LINK,
                        GroupsTable.COLUMN_EXPIRATION_TYPE,
                        GroupsTable.COLUMN_EXPIRATION_TIME},
                GroupsTable.COLUMN_IS_ACTIVE + "=?",
                new String[]{"1"}, null, null, GroupsTable.COLUMN_TIMESTAMP + " DESC")) {
            while (cursor.moveToNext()) {
                int expirationType = cursor.getInt(9);
                long expirationTime = cursor.getLong(10);
                final Group group = new Group(
                        cursor.getLong(0),
                        GroupId.fromNullable(cursor.getString(1)),
                        cursor.getLong(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getInt(6) == 1,
                        cursor.getInt(7),
                        parseExpiryInfo(expirationType, expirationTime));
                group.inviteToken = cursor.getString(8);
                groups.add(group);
            }
        }
        return groups;
    }

    @WorkerThread
    @NonNull List<Group> getGroups() {
        final List<Group> groups = new ArrayList<>();
        final SQLiteDatabase db = databaseHelper.getReadableDatabase();
        try (final Cursor cursor = db.query(GroupsTable.TABLE_NAME,
                new String [] {
                        GroupsTable._ID,
                        GroupsTable.COLUMN_GROUP_ID,
                        GroupsTable.COLUMN_TIMESTAMP,
                        GroupsTable.COLUMN_GROUP_NAME,
                        GroupsTable.COLUMN_GROUP_DESCRIPTION,
                        GroupsTable.COLUMN_GROUP_AVATAR_ID,
                        GroupsTable.COLUMN_IS_ACTIVE,
                        GroupsTable.COLUMN_THEME,
                        GroupsTable.COLUMN_INVITE_LINK,
                        GroupsTable.COLUMN_EXPIRATION_TYPE,
                        GroupsTable.COLUMN_EXPIRATION_TIME},
                null,
                null, null, null, GroupsTable.COLUMN_TIMESTAMP + " DESC")) {
            while (cursor.moveToNext()) {
                int expirationType = cursor.getInt(9);
                long expirationTime = cursor.getLong(10);
                final Group group = new Group(
                        cursor.getLong(0),
                        GroupId.fromNullable(cursor.getString(1)),
                        cursor.getLong(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getInt(6) == 1,
                        cursor.getInt(7),
                        parseExpiryInfo(expirationType, expirationTime));
                group.inviteToken = cursor.getString(8);
                groups.add(group);
            }
        }
        return groups;
    }

    @WorkerThread
    void deleteGroup(@NonNull GroupId groupId) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.beginTransaction();
        final String sql =
                "SELECT " +
                        "m." + MediaTable.COLUMN_FILE + "," +
                        "m." + MediaTable.COLUMN_ENC_FILE + " " +
                        "FROM " + PostsTable.TABLE_NAME + " " +
                        "INNER JOIN (" +
                        "SELECT " +
                        MediaTable.COLUMN_PARENT_TABLE + "," +
                        MediaTable.COLUMN_PARENT_ROW_ID + "," +
                        MediaTable.COLUMN_FILE + "," +
                        MediaTable.COLUMN_ENC_FILE + "," +
                        MediaTable.COLUMN_TRANSFERRED + " FROM " + MediaTable.TABLE_NAME + ")" +
                        "AS m ON " + PostsTable.TABLE_NAME + "." + PostsTable._ID + "=m." + MediaTable.COLUMN_PARENT_ROW_ID + " AND '" + PostsTable.TABLE_NAME + "'=m." + MediaTable.COLUMN_PARENT_TABLE + " " +
                        "WHERE " + PostsTable.COLUMN_GROUP_ID + "=?";
        int deletedFiles = 0;
        try (final Cursor cursor = db.rawQuery(sql, new String[]{ groupId.rawId() })) {
            while (cursor.moveToNext()) {
                final File mediaFile = fileStore.getMediaFile(cursor.getString(0));
                if (mediaFile != null) {
                    if (mediaFile.delete()) {
                        deletedFiles++;
                    } else {
                        Log.i("PostsDb/deleteGroup: failed to delete " + mediaFile.getAbsolutePath());
                    }
                }
                final File encFile = fileStore.getTmpFile(cursor.getString(1));
                if (encFile != null) {
                    if (encFile.delete()) {
                        deletedFiles++;
                    } else {
                        Log.i("PostsDb/deleteGroup: failed to delete " + encFile.getAbsolutePath());
                    }
                }
            }
        }
        Log.i("PostsDb/deleteGroup: " + deletedFiles + " media files deleted");

        final int deletedPosts = db.delete(PostsTable.TABLE_NAME, PostsTable.COLUMN_GROUP_ID + "=?", new String[] {groupId.rawId()});
        Log.i("PostsDb/deleteGroup: " + deletedPosts + " posts deleted for group id " + groupId.rawId());

        db.delete(GroupsTable.TABLE_NAME, GroupsTable.COLUMN_GROUP_ID + "=?", new String[] {groupId.rawId()});
        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
