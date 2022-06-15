package com.halloapp.content;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.tables.ArchiveTable;
import com.halloapp.content.tables.AudienceTable;
import com.halloapp.content.tables.CommentsTable;
import com.halloapp.content.tables.FutureProofTable;
import com.halloapp.content.tables.GroupMembersTable;
import com.halloapp.content.tables.HistoryResendPayloadTable;
import com.halloapp.content.tables.MediaTable;
import com.halloapp.content.tables.MentionsTable;
import com.halloapp.content.tables.MessagesTable;
import com.halloapp.content.tables.MomentsTable;
import com.halloapp.content.tables.OutgoingPlayedReceiptsTable;
import com.halloapp.content.tables.OutgoingSeenReceiptsTable;
import com.halloapp.content.tables.PostsTable;
import com.halloapp.content.tables.RepliesTable;
import com.halloapp.content.tables.RerequestsTable;
import com.halloapp.content.tables.SeenTable;
import com.halloapp.groups.GroupInfo;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.ContentDetails;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.DecryptStats;
import com.halloapp.util.stats.GroupDecryptStats;
import com.halloapp.xmpp.feed.FeedContentParser;

import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings("WeakerAccess")
public class ContentDb {

    private static ContentDb instance;

    private final Executor databaseWriteExecutor = Executors.newSingleThreadExecutor();

    private final ContentDbObservers observers = new ContentDbObservers();
    private final ContentDbHelper databaseHelper;

    private final Me me;
    private final CallsDb callsDb;
    private final MediaDb mediaDb;
    private final PostsDb postsDb;
    private final GroupsDb groupsDb;
    private final MomentsDb momentsDb;
    private final MentionsDb mentionsDb;
    private final MessagesDb messagesDb;
    private final Preferences preferences;
    private final FutureProofDb futureProofDb;
    private final UrlPreviewsDb urlPreviewsDb;

    public interface Observer {
        void onPostAdded(@NonNull Post post);
        void onPostRetracted(@NonNull Post post);
        void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId);
        void onPostDeleted(@NonNull Post post);
        void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId);
        void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId);
        void onCommentAdded(@NonNull Comment comment);
        void onCommentRetracted(@NonNull Comment comment);
        void onCommentUpdated(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId);
        void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId);
        void onMessageAdded(@NonNull Message message);
        void onMessageRetracted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId);
        void onMessageDeleted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId);
        void onMessageUpdated(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId);
        void onIncomingMessagePlayed(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId);
        void onGroupFeedAdded(@NonNull GroupId groupId);
        void onGroupMetadataChanged(@NonNull GroupId groupId);
        void onGroupMembersChanged(@NonNull GroupId groupId);
        void onGroupAdminsChanged(@NonNull GroupId groupId);
        void onGroupBackgroundChanged(@NonNull GroupId groupId);
        void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId);
        void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId seenByUserId, @NonNull String messageId);
        void onMediaPercentTransferred(@NonNull ContentItem contentItem, @NonNull Media media, int percent);
        void onChatSeen(@NonNull ChatId chatId, @NonNull Collection<SeenReceipt> seenReceipts);
        void onGroupSeen(@NonNull GroupId groupId);
        void onChatDeleted(@NonNull ChatId chatId);
        void onFeedCleanup();
        void onDbCreated();
        void onArchivedPostRemoved(@NonNull Post post);
        void onLocalPostSeen(@NonNull String postId);
    }

    public static class DefaultObserver implements Observer {
        public void onPostAdded(@NonNull Post post) {}
        public void onPostRetracted(@NonNull Post post) {}
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {}
        public void onPostDeleted(@NonNull Post post) {}
        public void onPostAudienceChanged(@NonNull Post post, @NonNull Collection<UserId> addedUsers) {}
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {}
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {}
        public void onLocalPostSeen(@NonNull String postId) {}
        public void onCommentAdded(@NonNull Comment comment) {}
        public void onCommentRetracted(@NonNull Comment comment) {}
        public void onCommentUpdated(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {}
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId) {}
        public void onMessageAdded(@NonNull Message message) {}
        public void onMessageRetracted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onMessageDeleted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onMessageUpdated(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onIncomingMessagePlayed(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {}
        public void onGroupFeedAdded(@NonNull GroupId groupId) {}
        public void onGroupMetadataChanged(@NonNull GroupId groupId) {}
        public void onGroupMembersChanged(@NonNull GroupId groupId) {}
        public void onGroupAdminsChanged(@NonNull GroupId groupId) {}
        public void onGroupBackgroundChanged(@NonNull GroupId groupId) {}
        public void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId) {}
        public void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {}
        public void onMediaPercentTransferred(@NonNull ContentItem contentItem, @NonNull Media media, int percent) {}
        public void onChatSeen(@NonNull ChatId chatId, @NonNull Collection<SeenReceipt> seenReceipts) {}
        public void onGroupSeen(@NonNull GroupId groupId) { }
        public void onChatDeleted(@NonNull ChatId chatId) {}
        public void onArchivedPostRemoved(@NonNull Post post) {}

        public void onFeedCleanup() {}
        public void onDbCreated() {}
    }

    public static ContentDb getInstance() {
        if (instance == null) {
            synchronized (ContentDb.class) {
                if (instance == null) {
                    instance = new ContentDb(Me.getInstance(), FileStore.getInstance(), Preferences.getInstance(), AppContext.getInstance(), ServerProps.getInstance());
                }
            }
        }
        return instance;
    }

    private ContentDb(
            @NonNull Me me,
            @NonNull final FileStore fileStore,
            @NonNull final Preferences preferences,
            final @NonNull AppContext appContext,
            @NonNull final ServerProps serverProps) {
        Context context = appContext.get();
        databaseHelper = new ContentDbHelper(context.getApplicationContext(), observers);
        this.me = me;
        this.preferences = preferences;

        groupsDb = new GroupsDb(fileStore, databaseHelper);
        callsDb = new CallsDb(databaseHelper);
        mentionsDb = new MentionsDb(databaseHelper);
        momentsDb = new MomentsDb(databaseHelper);
        mediaDb = new MediaDb(databaseHelper, fileStore);
        futureProofDb = new FutureProofDb(databaseHelper);
        urlPreviewsDb = new UrlPreviewsDb(mediaDb, databaseHelper);
        messagesDb = new MessagesDb(fileStore, callsDb, mentionsDb, futureProofDb, urlPreviewsDb, serverProps, databaseHelper);
        postsDb = new PostsDb(mediaDb, momentsDb, mentionsDb, futureProofDb, urlPreviewsDb, databaseHelper, fileStore, serverProps);
    }

    public void addObserver(@NonNull Observer observer) {
        observers.addObserver(observer);
    }

    public void removeObserver(@NonNull Observer observer) {
        observers.removeObserver(observer);
    }

    public void addPost(@NonNull Post post) {
        addPost(post, null);
    }

    public void addPost(@NonNull Post post, @Nullable Runnable completionRunnable) {
        addFeedItems(Collections.singletonList(post), new ArrayList<>(), completionRunnable);
    }

    @VisibleForTesting
    public List<Media> getAllMedia() {
        return mediaDb.getAllMedia();
    }

    public void addFeedItems(@NonNull List<Post> posts, @NonNull List<Comment> comments, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            addFeedItemsSync(posts, comments, completionRunnable);
        });
    }

    @WorkerThread
    private void addFeedItemsSync(@NonNull List<Post> posts, @NonNull List<Comment> comments, @Nullable Runnable completionRunnable) {
        for (Post post : posts) {
            boolean duplicate = false;
            final SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                if (post.isRetracted()) {
                    postsDb.retractPost(post);
                } else {
                    try {
                        if (post.type == Post.TYPE_MOMENT && post.isOutgoing()) {
                            postsDb.removeMomentEntryPost();
                        }
                        postsDb.addPost(post);
                        if (post.getParentGroup() != null) {
                            groupsDb.updateGroupTimestamp(post.getParentGroup(), post.timestamp);
                        }
                    } catch (SQLiteConstraintException ex) {
                        Log.w("ContentDb.addPost: duplicate " + ex.getMessage() + " " + post);
                        duplicate = true;
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            // important to notify outside of transaction
            if (!duplicate) {
                if (post.isRetracted()) {
                    observers.notifyPostRetracted(post);
                } else {
                    observers.notifyPostAdded(post);
                }
            }
        }

        final HashMap<String, Post> postCache = new HashMap<>();
        final HashMap<String, Comment> commentCache = new HashMap<>();
        final HashSet<String> checkedIds = new HashSet<>();
        final HashMap<UserId, Contact> contactMap = new HashMap<>();
        for (Comment comment : comments) {
            if (checkedIds.contains(comment.postId)) {
                comment.setParentPost(postCache.get(comment.postId));
            } else {
                Post parentPost = postsDb.getPost(comment.postId);
                if (parentPost != null) {
                    comment.setParentPost(parentPost);
                    postCache.put(comment.postId, parentPost);
                }
                checkedIds.add(comment.postId);
            }
            if (comment.isRetracted()) {
                postsDb.retractComment(comment);
                observers.notifyCommentRetracted(comment);
            } else {
                // If comment mentions you
                for (Mention mention : comment.mentions) {
                    if (mention.userId.isMe()) {
                        comment.shouldNotify = true;
                        break;
                    }
                }
                // If we're subscribed to the parent post
                Post parentPost = comment.getParentPost();
                if (!comment.shouldNotify && parentPost != null) {
                    if (parentPost.senderUserId.isMe() || parentPost.subscribed) {
                        comment.shouldNotify = true;
                    }
                }
                // If the comment is a reply to our comment
                if (!comment.shouldNotify && comment.parentCommentId != null) {
                    Comment replyComment = commentCache.get(comment.parentCommentId);
                    if (replyComment == null) {
                        replyComment = getComment(comment.parentCommentId);
                        commentCache.put(comment.parentCommentId, replyComment);
                    }
                    if (replyComment != null && replyComment.senderUserId.isMe()) {
                        comment.shouldNotify = true;
                    }
                }
                // If comment is made to a group by one of our contacts
                if (!comment.shouldNotify && parentPost != null && parentPost.getParentGroup() != null && ServerProps.getInstance().getGroupCommentsNotification()) {
                    Contact sender = contactMap.get(comment.senderUserId);
                    if (sender == null) {
                        sender = ContactsDb.getInstance().getContact(comment.senderUserId);
                        contactMap.put(comment.senderUserId, sender);
                    }
                    if (sender.inAddressBook()) {
                        comment.shouldNotify = true;
                    }
                }
                // Subscribe to parent post in groups if we comment
                if (parentPost != null && parentPost.getParentGroup() != null && !parentPost.subscribed && comment.senderUserId.isMe()) {
                    postsDb.subscribeToPost(parentPost);
                    parentPost.subscribed = true;
                }
                try {
                    postsDb.addComment(comment);
                    observers.notifyCommentAdded(comment);
                } catch (SQLiteConstraintException ex) {
                    Log.w("ContentDb.addComment: duplicate " + ex.getMessage() + " " + comment);
                }
            }
        }

        if (completionRunnable != null) {
            completionRunnable.run();
        }
    }

    public void hideMomentOnView(@NonNull Post post) {
        databaseWriteExecutor.execute(() -> {
            postsDb.hideMoment(post);
            observers.notifyPostUpdated(post.senderUserId, post.id);
        });
    }

    public void retractPost(@NonNull Post post) {
        databaseWriteExecutor.execute(() -> {
            postsDb.retractPost(post);
            observers.notifyPostRetracted(post);
        });
    }

    public void removePostFromArchive(@NonNull Post post) {
        databaseWriteExecutor.execute(() -> {
            postsDb.removePostFromArchive(post);
            observers.notifyArchivedPostRemoved(post);
        });
    }

    public void removeZeroZonePost(@NonNull Post post) {
        databaseWriteExecutor.execute(() -> {
            postsDb.deleteZeroZonePost(post);
            observers.notifyPostRetracted(post);
        });
    }

    public void setZeroZonePostSeen(@NonNull String postId) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setZeroZoneGroupPostSeen(postId);
            observers.notifyLocalPostSeen(postId);
        });
    }


    public void setIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setIncomingPostSeen(senderUserId, postId);
            observers.notifyIncomingPostSeen(senderUserId, postId);
        });
    }

    // for debug only
    public void setIncomingPostsSeen(@Post.SeenState int seen) {
        databaseWriteExecutor.execute(() -> postsDb.setIncomingPostsSeen(seen));
    }

    public void setPostSeenReceiptSent(@NonNull UserId senderUserId, @NonNull String postId) {
        databaseWriteExecutor.execute(() -> postsDb.setPostSeenReceiptSent(senderUserId, postId));
    }

    public void setOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId, long timestamp, @NonNull Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setOutgoingPostSeen(seenByUserId, postId, timestamp);
            observers.notifyOutgoingPostSeen(seenByUserId, postId);
            completionRunnable.run();
        });
    }

    public void setPostTransferred(@NonNull UserId senderUserId, @NonNull String postId) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setPostTransferred(senderUserId, postId);
            observers.notifyPostUpdated(senderUserId, postId);
        });
    }

    public void setPostProtoHash(@NonNull UserId senderUserId, @NonNull String postId, @Nullable byte[] protoHash) {
        databaseWriteExecutor.execute(() -> postsDb.setPostProtoHash(senderUserId, postId, protoHash));
    }

    public void setMediaTransferred(@NonNull Post post, @NonNull Media media) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setMediaTransferred(post, media);
            if (post.isIncoming() && post.isAllMediaTransferred()) {
                postsDb.setPostTransferred(post.senderUserId, post.id);
            }
            observers.notifyPostUpdated(post.senderUserId, post.id);
        });
    }

    public void setMediaTransferred(@NonNull Comment comment, @NonNull Media media) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setMediaTransferred(comment, media);
            if (comment.isIncoming() && comment.isAllMediaTransferred()) {
                postsDb.setCommentTransferred(comment.postId, comment.senderUserId, comment.id);
            }
            observers.notifyCommentUpdated(comment.postId, comment.senderUserId, comment.id);
        });
    }

    @WorkerThread
    public void setPatchUrl(@NonNull Post post, long rowId, @NonNull String url) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set patch url in Post: " + post);
            postsDb.setPatchUrl(rowId, url);
        });
    }

    @WorkerThread
    public void setPatchUrl(@NonNull Comment comment, long rowId, @NonNull String url) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set patch url in Comment: " + comment);
            postsDb.setPatchUrl(rowId, url);
        });
    }

    @WorkerThread
    public void setPatchUrl(@NonNull Message message, long rowId, @NonNull String url) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set patch url in Message: " + message);
            messagesDb.setPatchUrl(rowId, url);
        });
    }

    @WorkerThread
    public String getPatchUrl(@NonNull Post post, long rowId) {
        Log.i("Get patch url in Post: " + post);
        return postsDb.getPatchUrl(rowId);
    }

    @WorkerThread
    public String getPatchUrl(@NonNull Comment comment, long rowId) {
        Log.i("Get patch url in Comment: " + comment);
        return postsDb.getPatchUrl(rowId);
    }

    @WorkerThread
    public String getPatchUrl(@NonNull Message message, long rowId) {
        Log.i("Get patch url in Message: " + message);
        return messagesDb.getPatchUrl(rowId);
    }

    @WorkerThread
    public @Nullable Media getMediaByRowId(long rowId) {
        Log.i("Get media by row id " + rowId);
        return mediaDb.getMediaByRowId(rowId);
    }

    @WorkerThread
    public @Nullable Media getLatestMediaWithHash(@NonNull byte[] decSha256hash, @Media.BlobVersion int blobVersion) {
        Log.i("Get latest media with a given data hash");
        return mediaDb.getLatestMediaWithHash(decSha256hash, blobVersion);
    }

    @WorkerThread
    public int getMediaPercentTransferred(long rowId) {
        return mediaDb.getPercentTransferred(rowId);
    }

    @WorkerThread
    public void setMediaPercentTransferred(@NonNull ContentItem contentItem, @NonNull Media media, int percentTransferred) {
        databaseWriteExecutor.execute(() -> {
            mediaDb.setPercentTransferred(media.rowId, percentTransferred);
            observers.notifyMediaPercentTransferred(contentItem, media, percentTransferred);
        });
    }

    @WorkerThread
    public BitSet getMediaChunkSet(long rowId) {
        Log.i("Get media chunk set for media with rowId: " + rowId);
        return mediaDb.getChunkSet(rowId);
    }

    @WorkerThread
    public void updateMediaChunkSet(long rowId, @NonNull BitSet chunkSet) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Update media chunk set for media with rowId: " + rowId);
            mediaDb.updateChunkSet(rowId, chunkSet);
        });
    }

    @WorkerThread
    public void markChunkedMediaTransferComplete(long rowId) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Mark chunked media with rowId: " + rowId + " as complete");
            mediaDb.markChunkedTransferComplete(rowId);
        });
    }

    @WorkerThread
    public @Media.TransferredState int getMediaTransferred(@NonNull Post post, long rowId) {
        Log.i("Get transferred state from Post: "+ post);
        return postsDb.getMediaTransferred(rowId);
    }

    @WorkerThread
    public @Media.TransferredState int getMediaTransferred(@NonNull Comment comment, long rowId) {
        Log.i("Get transferred state from Comment: "+ comment);
        return postsDb.getMediaTransferred(rowId);
    }

    @WorkerThread
    public @Media.TransferredState int getMediaTransferred(@NonNull Message message, long rowId) {
        Log.i("Get transferred state from Message: "+ message);
        return messagesDb.getMediaTransferred(rowId);
    }

    @WorkerThread
    public byte[] getMediaEncKey(long rowId) {
        Log.i("Get media enc key for media with rowId: " + rowId);
        return mediaDb.getEncKey(rowId);
    }

    @WorkerThread
    public void setUploadProgress(@NonNull Post post, long rowId, long offset) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set upload progress in Post: " + post);
            postsDb.setUploadProgress(rowId, offset);
        });
    }

    @WorkerThread
    public void setUploadProgress(@NonNull Comment comment, long rowId, long offset) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set upload progress in Comment: " + comment);
            postsDb.setUploadProgress(rowId, offset);
        });
    }

    @WorkerThread
    public void setUploadProgress(@NonNull Message message, long rowId, long offset) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set upload progress in Message: " + message);
            messagesDb.setUploadProgress(rowId, offset);
        });
    }

    @WorkerThread
    public long getUploadProgress(@NonNull Post post, long rowId) {
        Log.i("Get upload progress in Post: " + post);
        return postsDb.getUploadProgress(rowId);
    }

    @WorkerThread
    public long getUploadProgress(@NonNull Comment comment, long rowId) {
        Log.i("Get upload progress in Comment: " + comment);
        return postsDb.getUploadProgress(rowId);
    }

    @WorkerThread
    public long getUploadProgress(@NonNull Message message, long rowId) {
        Log.i("Get upload progress in Message: " + message);
        return messagesDb.getUploadProgress(rowId);
    }

    @WorkerThread
    public void setRetryCount(@NonNull Post post, long rowId, int count) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set retry count in Post: " + post);
            postsDb.setRetryCount(rowId, count);
        });
    }

    @WorkerThread
    public void setRetryCount(@NonNull Comment comment, long rowId, int count) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set retry count in Comment: " + comment);
            postsDb.setRetryCount(rowId, count);
        });
    }

    @WorkerThread
    public void setRetryCount(@NonNull Message message, long rowId, int count) {
        databaseWriteExecutor.execute(() -> {
            Log.i("Set retry count in Message: " + message);
            messagesDb.setRetryCount(rowId, count);
        });
    }

    @WorkerThread
    public int getRetryCount(@NonNull Post post, long rowId) {
        Log.i("Get retry count in Post: " + post);
        return postsDb.getRetryCount(rowId);
    }

    @WorkerThread
    public int getRetryCount(@NonNull Comment comment, long rowId) {
        Log.i("Get retry count in Comment: " + comment);
        return postsDb.getRetryCount(rowId);
    }

    @WorkerThread
    public int getRetryCount(@NonNull Message message, long rowId) {
        Log.i("Get retry count in Message: " + message);
        return messagesDb.getRetryCount(rowId);
    }

    @WorkerThread
    public List<ContentDetails> getHistoryResendContent(@NonNull GroupId groupId, long myUid) {
        return postsDb.getHistoryResendContent(groupId, myUid);
    }

    @WorkerThread
    public @NonNull List<Post> getUnseenPosts(long timestamp, int count) {
        return getPosts(timestamp, count, false, null, true);
    }

    @WorkerThread
    public int getUnseenGroups() {
        return postsDb.getUnreadGroups();
    }

    @WorkerThread
    public @NonNull List<Post> getUnseenGroupPosts(@NonNull GroupId groupId) {
        return getPosts(null, 256, true, null, groupId, true);
    }

    @WorkerThread
    public @Nullable Post getLastUnseenGroupPost(@NonNull GroupId groupId) {
        List<Post> posts = getPosts(null, 10, true, null, groupId, true);
        if (posts.isEmpty()) {
            return null;
        }
        return posts.get(0);
    }

    @WorkerThread
    public @Nullable Post getLastGroupPost(@NonNull GroupId groupId) {
        List<Post> posts = getPosts(null, Constants.MAX_POST_MEDIA_ITEMS + 1, true, null, groupId, false);
        if (posts.isEmpty()) {
            return null;
        }
        return posts.get(0);
    }

    @WorkerThread
    @NonNull List<Post> getPosts(@Nullable Long timestamp, int count, boolean after, @Nullable UserId senderUserId, @Nullable GroupId groupId) {
        return getPosts(timestamp, count, after, senderUserId, groupId, false);
    }

    @WorkerThread
    private @NonNull List<Post> getPosts(@Nullable Long timestamp, int count, boolean after, @Nullable UserId senderUserId, boolean unseenOnly) {
        return getPosts(timestamp, count, after, senderUserId, null, unseenOnly);
    }

    @WorkerThread
    private @NonNull List<Post> getPosts(@Nullable Long timestamp, int count, boolean after, @Nullable UserId senderUserId, @Nullable GroupId groupId, boolean unseenOnly) {
        return postsDb.getPosts(timestamp, count, after, senderUserId, groupId, unseenOnly, false);
    }

    @WorkerThread
    @NonNull List<Post> getPostsOrderLastUpdate(@Nullable Long timestamp, int count, boolean after, @Nullable UserId senderUserId, @Nullable GroupId groupId) {
        return postsDb.getPosts(timestamp, count, after, senderUserId, groupId, false, true);
    }

    @WorkerThread
    public List<Post> getAllPosts() {
        List<Post> ret = postsDb.getAllPosts(null);
        List<Group> groups = getGroups();
        for (Group group : groups) {
            ret.addAll(postsDb.getAllPosts(group.groupId));
        }
        return ret;
    }

    @WorkerThread
    public @Nullable String getUnlockingMomentId() {
        return postsDb.getUnlockingMomentId();
    }

    @WorkerThread
    public void retractCurrentMoment() {
        String id = postsDb.getUnlockingMomentId();
        if (id != null) {
            postsDb.retractPost(postsDb.getPost(id));
        }
    }

    @WorkerThread
    public @Nullable Post getPost(@NonNull String postId) {
        return postsDb.getPost(postId);
    }

    @WorkerThread
    public @NonNull List<UserId> getPostSeenByUsers(@NonNull String postId) {
        return postsDb.getPostSeenByUsers(postId);
    }

    @WorkerThread
    public @NonNull List<UserId> getPostAudience(@NonNull String postId) {
        return postsDb.getPostSeenByUsers(postId);
    }

    @WorkerThread
    public @NonNull List<SeenByInfo> getPostSeenByInfos(@NonNull String postId) {
        return postsDb.getPostSeenByInfos(postId);
    }

    @WorkerThread
    public @Nullable Comment getComment(@NonNull String commentId) {
        return postsDb.getComment(commentId);
    }

    public void addComment(@NonNull Comment comment) {
        addFeedItems(new ArrayList<>(), Collections.singletonList(comment), null);
    }

    public void addUrlPreview(@NonNull ContentItem contentItem) {
        if (contentItem instanceof Comment) {
            urlPreviewsDb.addUrlPreview((Comment) contentItem);
        } else if (contentItem instanceof Post) {
            urlPreviewsDb.addUrlPreview((Post) contentItem);
        } else if (contentItem instanceof Message) {
            urlPreviewsDb.addUrlPreview((Message) contentItem);
        }
    }

    public void retractComment(@NonNull Comment comment) {
        databaseWriteExecutor.execute(() -> {
            Comment dbComment = comment;
            if (dbComment.rowId == 0) {
                Comment temp = postsDb.getComment(comment.id);
                if (temp != null) {
                    dbComment = temp;
                }
            }
            postsDb.retractComment(dbComment);
            observers.notifyCommentRetracted(dbComment);
        });
    }

    public boolean hasHomeZeroZonePost() {
        return postsDb.hasZeroZoneHomePost();
    }

    public void addHomeZeroZonePost() {
        databaseWriteExecutor.execute(() -> {
            if (postsDb.hasZeroZoneHomePost()) {
                return;
            }
            Post systemPost = new Post(0,
                    UserId.ME,
                    RandomId.create(),
                    System.currentTimeMillis(),
                    Post.TRANSFERRED_YES,
                    Post.SEEN_YES,
                    Post.TYPE_ZERO_ZONE,
                    null);
            addFeedItemsSync(Collections.singletonList(systemPost), new ArrayList<>(), null);
        });
    }

    public void addMomentEntryPost() {
        databaseWriteExecutor.execute(() -> {
            if (!postsDb.hasUnexpiredMomentEntryPost()) {
                postsDb.removeMomentEntryPost();
                Post systemPost = new Post(0,
                        UserId.ME,
                        RandomId.create(),
                        System.currentTimeMillis(),
                        Post.TRANSFERRED_YES,
                        Post.SEEN_YES,
                        Post.TYPE_MOMENT_ENTRY,
                        null);
                addFeedItemsSync(Collections.singletonList(systemPost), new ArrayList<>(), null);
            }
        });
    }

    public boolean hasGroupZeroZonePost(@NonNull GroupId groupId) {
        return postsDb.hasZeroZoneGroupPost(groupId);
    }

    public void setCommentTransferred(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setCommentTransferred(postId, commentSenderUserId, commentId);
            observers.notifyCommentUpdated(postId, commentSenderUserId, commentId);
        });
    }

    public void setCommentProtoHash(@NonNull String postId, @NonNull UserId commentSenderUserId, @NonNull String commentId, @Nullable byte[] protoHash) {
        databaseWriteExecutor.execute(() -> postsDb.setCommentProtoHash(postId, commentSenderUserId, commentId, protoHash));
    }

    // for debug only
    public void setCommentsSeen(boolean seen) {
        databaseWriteExecutor.execute(() -> {
            if (postsDb.setCommentsSeen(seen)) {
                observers.notifyCommentsSeen(UserId.ME, "");
            }
        });
    }

    public void setCommentsSeen(@NonNull String postId) {
        setCommentsSeen(postId, true);
    }

    public void setCommentsSeen(@NonNull String postId, boolean seen) {
        databaseWriteExecutor.execute(() -> {
            if (postsDb.setCommentsSeen(postId, seen)) {
                Post post = postsDb.getPost(postId);
                if (post != null) {
                    observers.notifyCommentsSeen(post.senderUserId, postId);
                }
            }
        });
    }

    public void setCommentSeen(@NonNull String postId, @NonNull String commentId, boolean seen) {
        databaseWriteExecutor.execute(() -> {
            if (postsDb.setCommentSeen(postId, commentId, seen)) {
                Post post = postsDb.getPost(postId);
                if (post != null) {
                    observers.notifyCommentsSeen(post.senderUserId, postId);
                }
            }
        });
    }

    public void setCommentPlayed(@NonNull String postId, @NonNull String commentId, boolean played) {
        databaseWriteExecutor.execute(() -> {
            if (postsDb.setCommentPlayed(postId, commentId, played)) {
                Post post = postsDb.getPost(postId);
                if (post != null) {
                    observers.notifyCommentsSeen(post.senderUserId, postId);
                }
            }
        });
    }

    @WorkerThread
    public long getLastSeenCommentRowId(@NonNull String postId) {
        return postsDb.getLastSeenCommentRowId(postId);
    }

    @WorkerThread
    public long getFirstUnseenCommentRowId(@NonNull String postId) {
        return postsDb.getFirstUnseenCommentRowId(postId);
    }

    @WorkerThread
    public int getUnseenCommentCount(@NonNull String postId) {
        return postsDb.getUnseenCommentCount(postId);
    }

    @WorkerThread
    @NonNull List<Comment> getComments(@NonNull String postId, @Nullable Integer start, @Nullable Integer count) {
        return postsDb.getComments(postId, start, count);
    }

    public List<Comment> getAllComments(@NonNull String postId) {
        return getComments(postId, null, null);
    }

    @WorkerThread
    @NonNull List<Comment> getCommentsFlat(@NonNull String postId, int start, int count) {
        return postsDb.getCommentsFlat(postId, start, count);
    }

    @WorkerThread
    int getCommentsFlatCount(@NonNull String postId) {
        return postsDb.getCommentsFlatCount(postId);
    }

    @WorkerThread
    public int getCommentFlatIndex(@NonNull String postId, @NonNull String commentId) {
        return postsDb.getCommentFlatIndex(postId, commentId);
    }

    @WorkerThread
    int getCommentCount(@NonNull String postId) {
        return postsDb.getCommentCount(postId);
    }

    @WorkerThread
    public byte[] getPostProtoHash(@NonNull String postId) {
        return postsDb.getPostProtoHash(postId);
    }

    @WorkerThread
    public byte[] getCommentProtoHash(@NonNull String commentId) {
        return postsDb.getCommentProtoHash(commentId);
    }

    /*
     * Returns posts that the userid is mentioned in
     * */
    @WorkerThread
    public @NonNull List<Post> getMentionedPosts(@NonNull UserId userId, int limit) {
        return postsDb.getMentionedPosts(userId, limit);
    }

    /*
     * Returns comments the userid is mentioned in
     */
    @WorkerThread
    public @NonNull List<Comment> getMentionedComments(@NonNull UserId userId, int limit) {
        return postsDb.getMentionedComments(userId, limit);
    }

    /*
     * returns "important" comments only
     * */
    @WorkerThread
    public @NonNull List<Comment> getIncomingCommentsHistory(int limit) {
        return postsDb.getIncomingCommentsHistory(limit);
    }

    @WorkerThread
    public @NonNull List<Comment> getNotificationComments(long timestamp, int count) {
        return postsDb.getNotificationComments(timestamp, count);
    }

    public void addMessage(@NonNull Message message) {
        addMessage(message, false, null);
    }

    public void addMessage(@NonNull Message message, boolean unseen, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            final Post replyPost = message.replyPostId == null ? null : getPost(message.replyPostId);
            final Message replyMessage = message.replyMessageId == null ? null : getMessage(message.chatId, message.replyMessageSenderId, message.replyMessageId);
            if (messagesDb.addMessage(message, unseen, replyPost, replyMessage)) {
                observers.notifyMessageAdded(message);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void updateMessageDecrypt(@NonNull Message message, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (messagesDb.updateMessageDecrypt(message)) {
                observers.notifyMessageUpdated(message.chatId, message.senderUserId, message.id);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setPostMissing(@NonNull String postId) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setPostMissing(postId);
        });
    }

    public void setCommentMissing(@NonNull String commentId) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setCommentMissing(commentId);
        });
    }

    public void addFeedGroup(@NonNull GroupInfo groupInfo, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (groupsDb.addGroup(groupInfo)) {
                observers.notifyGroupFeedAdded(groupInfo.groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void updateFeedGroup(@NonNull GroupInfo groupInfo, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (groupsDb.updateGroupChat(groupInfo)) {
                observers.notifyGroupMetadataChanged(groupInfo.groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setGroupName(@NonNull GroupId groupId, @NonNull String name, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (groupsDb.setGroupName(groupId, name)) {
                observers.notifyGroupMetadataChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setGroupDescription(@NonNull GroupId groupId, @NonNull String name, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (groupsDb.setGroupDescription(groupId, name)) {
                observers.notifyGroupMetadataChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setGroupTheme(@NonNull GroupId groupId, int theme, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (groupsDb.setGroupTheme(groupId, theme)) {
                observers.notifyGroupBackgroundChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setGroupLink(@NonNull GroupId groupId, @Nullable String inviteLink) {
        databaseWriteExecutor.execute(() -> {
            groupsDb.setGroupInviteLinkToken(groupId, inviteLink);
        });
    }

    public void setGroupAvatar(@NonNull GroupId groupId, @NonNull String avatarId, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (groupsDb.setGroupAvatar(groupId, avatarId)) {
                observers.notifyGroupMetadataChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setUnknownContactAllowed(@NonNull UserId userId, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setUnknownContactAllowed(userId);
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setGroupInactive(@NonNull GroupId groupId, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (groupsDb.setGroupInactive(groupId)) {
                observers.notifyGroupMetadataChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setGroupActive(@NonNull GroupId groupId, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (groupsDb.setGroupActive(groupId)) {
                observers.notifyGroupMetadataChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void addRemoveGroupMembers(@NonNull GroupId groupId, @Nullable String groupName, @Nullable String avatarId, @NonNull List<MemberInfo> added, @NonNull List<MemberInfo> removed, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (groupsDb.addRemoveGroupMembers(groupId, groupName, avatarId, added, removed)) {
                observers.notifyGroupMembersChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void promoteDemoteGroupAdmins(@NonNull GroupId groupId, @NonNull List<MemberInfo> promoted, @NonNull List<MemberInfo> demoted, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            if (groupsDb.promoteDemoteGroupAdmins(groupId, promoted, demoted)) {
                observers.notifyGroupAdminsChanged(groupId);
            }
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void deleteMoment(@NonNull Post moment) {
        databaseWriteExecutor.execute(() -> {
            postsDb.removeMoment(moment);
            observers.notifyPostDeleted(moment);
        });
    }

    public void deleteMessage(long rowId) {
        databaseWriteExecutor.execute(() -> {
            Message message = messagesDb.getMessage(rowId);
            if (message != null) {
                messagesDb.deleteMessage(rowId);
                observers.notifyMessageDeleted(message.chatId, message.senderUserId, message.id);
            }
        });
    }

    public void retractMessage(long rowId, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            Message msg = messagesDb.getMessage(rowId);
            if (msg != null) {
                retractMessage(msg, completionRunnable);
            } else {
                Log.e("ContentDb/retractMessage no message found for row id");
                if (completionRunnable != null) {
                    completionRunnable.run();
                }
            }
        });
    }

    public void retractMessage(@NonNull ChatId chatId, @NonNull UserId senderId, @NonNull String msgId, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            Message msg = messagesDb.getMessage(chatId, senderId, msgId);
            if (msg != null) {
                retractMessage(msg, completionRunnable);
            } else {
                Log.e("ContentDb/retractMessage no message found for row id");
                if (completionRunnable != null) {
                    completionRunnable.run();
                }
            }
        });
    }

    public void retractMessage(@NonNull Message message, @Nullable Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.retractMessage(message);
            observers.notifyMessageRetracted(message.chatId, message.senderUserId, message.id);
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        });
    }

    public void setMediaTransferred(@NonNull Message message, @NonNull Media media) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setMediaTransferred(message, media);
            if (message.isIncoming() && message.isAllMediaTransferred()) {
                messagesDb.setMessageTransferred(message.chatId, message.senderUserId, message.id);
            }
            observers.notifyMessageUpdated(message.chatId, message.senderUserId, message.id);
        });
    }

    public void setMessageRerequestCount(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId, int count) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setMessageRerequestCount(chatId, senderUserId, messageId, count);
        });
    }

    public void setPostRerequestCount(@Nullable GroupId groupId, @NonNull UserId senderUserId, @NonNull String postId, int count) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setPostRerequestCount(groupId, senderUserId, postId, count);
        });
    }

    public void setCommentRerequestCount(@NonNull GroupId groupId, @NonNull UserId senderUserId, @NonNull String commentId, int count) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setCommentRerequestCount(groupId, senderUserId, commentId, count);
        });
    }

    public void setHistoryResendRerequestCount(@NonNull GroupId groupId, @NonNull UserId senderUserId, @NonNull String historyId, int count) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setHistoryResendRerequestCount(groupId, senderUserId, historyId, count);
        });
    }

    public void setOutboundPostRerequestCount(@NonNull UserId rerequestorUserId, @NonNull String postId, int count) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setOutboundRerequestCount(rerequestorUserId, postId, PostsTable.TABLE_NAME, count);
        });
    }

    public int getOutboundPostRerequestCount(@NonNull UserId rerequestorUserId, @NonNull String postId) {
        return postsDb.getOutboundRerequestCount(rerequestorUserId, postId, PostsTable.TABLE_NAME);
    }

    public void setOutboundCommentRerequestCount(@NonNull UserId rerequestorUserId, @NonNull String postId, int count) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setOutboundRerequestCount(rerequestorUserId, postId, CommentsTable.TABLE_NAME, count);
        });
    }

    public int getOutboundCommentRerequestCount(@NonNull UserId rerequestorUserId, @NonNull String commentId) {
        return postsDb.getOutboundRerequestCount(rerequestorUserId, commentId, CommentsTable.TABLE_NAME);
    }

    public void setHistoryResendPayload(@NonNull GroupId groupId, @NonNull String historyResendId, @NonNull byte[] payload) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setHistoryResendPayload(groupId, historyResendId, payload);
        });
    }

    public byte[] getHistoryResendPayload(@NonNull GroupId groupId, @NonNull String historyResendId) {
        return postsDb.getHistoryResendPayload(groupId, historyResendId);
    }

    public void setMessageTransferred(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setMessageTransferred(chatId, senderUserId, messageId);
            observers.notifyMessageUpdated(chatId, senderUserId, messageId);
        });
    }

    public void setOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp, @NonNull Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setOutgoingMessageDelivered(chatId, recipientUserId, messageId, timestamp);
            observers.notifyOutgoingMessageDelivered(chatId, recipientUserId, messageId);
            completionRunnable.run();
        });
    }

    public void setOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp, @NonNull Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setOutgoingMessageSeen(chatId, recipientUserId, messageId, timestamp);
            observers.notifyOutgoingMessageSeen(chatId, recipientUserId, messageId);
            completionRunnable.run();
        });
    }

    public void setOutgoingMessagePlayed(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId, long timestamp, @NonNull Runnable completionRunnable) {
        databaseWriteExecutor.execute(() -> {
            messagesDb.setOutgoingMessagePlayed(chatId, recipientUserId, messageId, timestamp);
            observers.notifyOutgoingMessageSeen(chatId, recipientUserId, messageId);
            completionRunnable.run();
        });
    }

    public void processFutureProofContent() {
        databaseWriteExecutor.execute(() -> {
            messagesDb.processFutureProofMessages(observers::notifyMessageUpdated);
            FeedContentParser parser = new FeedContentParser(me);
            postsDb.processFutureProofContent(parser, observers);
        });
    }

    @WorkerThread
    public @NonNull List<Message> getUnseenMessages(int count) {
        return messagesDb.getUnseenMessages(count);
    }

    @WorkerThread
    public @NonNull List<CallMessage> getUnseenCallMessages(int count) {
        return messagesDb.getUnseenCallMessages(count);
    }

    @WorkerThread
    public boolean hasMessage(UserId senderUserId, String id) {
        return messagesDb.hasMessage(senderUserId, id);
    }

    @WorkerThread
    public @Nullable Message getMessage(long rowId) {
        return messagesDb.getMessage(rowId);
    }

    @WorkerThread
    public @Nullable Message getMessage(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        return messagesDb.getMessage(chatId, senderUserId, messageId);
    }

    @WorkerThread
    public int getMessageRerequestCount(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        return messagesDb.getMessageRerequestCount(chatId, senderUserId, messageId);
    }

    @WorkerThread
    public int getPostRerequestCount(@Nullable GroupId groupId, @NonNull UserId senderUserId, @NonNull String postId) {
        return postsDb.getPostRerequestCount(groupId, senderUserId, postId);
    }

    @WorkerThread
    public int getCommentRerequestCount(@NonNull GroupId groupId, @NonNull UserId senderUserId, @NonNull String commentId) {
        return postsDb.getCommentRerequestCount(groupId, senderUserId, commentId);
    }

    @WorkerThread
    public int getHistoryResendRerequestCount(@NonNull GroupId groupId, @NonNull UserId senderUserId, @NonNull String historyId) {
        return postsDb.getHistoryResendRerequestCount(groupId, senderUserId, historyId);
    }

    @WorkerThread
    public @Nullable Message getMessageForMedia(long mediaRowId) {
        return messagesDb.getMessageForMedia(mediaRowId);
    }

    @WorkerThread
    @NonNull List<Message> getMessages(@NonNull ChatId chatId, @Nullable Long startRowId, @Nullable Integer count, boolean after) {
        return messagesDb.getMessages(chatId, startRowId, count, after);
    }

    @WorkerThread
    public List<Message> getAllMessages() {
        List<Message> ret = new ArrayList<>();
        List<Chat> chats = getChats(false);
        for (Chat chat : chats) {
            ret.addAll(getMessages((UserId) chat.chatId, null, null, false));
        }
        return ret;
    }

    @WorkerThread
    public @NonNull List<Media> getChatMedia(@NonNull ChatId chatId, @Nullable Long startRowId, int count, boolean after) {
        return messagesDb.getChatMedia(chatId, startRowId, count, after);
    }

    @WorkerThread
    public long getChatMediaPosition(ChatId chatId, long rowId) {
        return messagesDb.getChatMediaPosition(chatId, rowId);
    }

    @WorkerThread
    public long getChatMediaCount(ChatId chatId) {
        return messagesDb.getChatMediaCount(chatId);
    }

    @WorkerThread
    public @Nullable ReplyPreview getReplyPreview(long messageRowId) {
        return messagesDb.getReplyPreview(messageRowId);
    }

    @WorkerThread
    public @NonNull List<Chat> getChats(boolean includeGroups) {
        return messagesDb.getChats(includeGroups);
    }

    @WorkerThread
    public @NonNull List<GroupId> getGroupsInCommon(UserId userId) {
        return groupsDb.getGroupsInCommon(userId);
    }

    @WorkerThread
    public @NonNull List<Group> getGroups() {
        return groupsDb.getGroups();
    }

    @WorkerThread
    public @NonNull List<Group> getActiveGroups() {
        return groupsDb.getActiveGroups();
    }

    @WorkerThread
    public @Nullable Chat getChat(@NonNull ChatId chatId) {
        return messagesDb.getChat(chatId);
    }

    @WorkerThread
    public @Nullable Group getGroup(@NonNull GroupId groupId) {
        return groupsDb.getGroup(groupId);
    }

    @WorkerThread
    public @Nullable String getDeletedChatName(@NonNull ChatId chatId) {
        return messagesDb.getDeletedChatName(chatId);
    }

    @WorkerThread
    public @NonNull List<MemberInfo> getGroupMembers(@NonNull GroupId groupId) {
        return groupsDb.getGroupMembers(groupId);
    }

    @WorkerThread
    public int getUnseenChatsCount() {
        return messagesDb.getUnseenChatsCount();
    }

    public void deleteGroup(@NonNull GroupId groupId) {
        databaseWriteExecutor.execute(() -> {
            groupsDb.deleteGroup(groupId);
        });
    }

    public void deleteChat(@NonNull ChatId chatId) {
        databaseWriteExecutor.execute(() -> {
            if (chatId instanceof GroupId) {
                groupsDb.deleteGroup((GroupId) chatId);
            }
            messagesDb.deleteChat(chatId);
            observers.notifyChatDeleted(chatId);
        });
    }

    public void setGroupSeen(@NonNull GroupId groupId) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setGroupSeen(groupId);
            observers.notifyGroupSeen(groupId);
        });
    }

    public void setChatSeen(@NonNull ChatId chatId) {
        databaseWriteExecutor.execute(() -> {
            final Collection<SeenReceipt> seenReceipts = messagesDb.setChatSeen(chatId);
            if (!seenReceipts.isEmpty()) {
                observers.notifyChatSeen(chatId, seenReceipts);
            }
        });
    }

    public void setMessagePlayed(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        databaseWriteExecutor.execute(() -> {
            if (messagesDb.setMessagePlayed(chatId, senderUserId, messageId)) {
                observers.notifyIncomingMessagePlayed(chatId, senderUserId, messageId);
            }
        });
    }

    public void setMessageSeenReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        databaseWriteExecutor.execute(() -> messagesDb.setMessageSeenReceiptSent(chatId, senderUserId, messageId));
    }

    public void setMessagePlayedReceiptSent(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
        databaseWriteExecutor.execute(() -> messagesDb.setMessagePlayedReceiptSent(chatId, senderUserId, messageId));
    }

    @WorkerThread
    @NonNull List<Message> getPendingMessages() {
        return messagesDb.getPendingMessages();
    }

    @WorkerThread
    @NonNull List<SeenReceipt> getPendingMessageSeenReceipts() {
        return messagesDb.getPendingMessageSeenReceipts();
    }

    @WorkerThread
    @NonNull List<PlayedReceipt> getPendingMessagePlayedReceipts() {
        return messagesDb.getPendingMessagePlayedReceipts();
    }

    @WorkerThread
    @NonNull List<Post> getPendingPosts() {
        return postsDb.getPendingPosts();
    }

    @WorkerThread
    @NonNull List<Comment> getPendingComments() {
        return postsDb.getPendingComments();
    }

    @WorkerThread
    @NonNull List<SeenReceipt> getPendingPostSeenReceipts() {
        return postsDb.getPendingPostSeenReceipts();
    }

    public void updatePostAudience(@NonNull Map<UserId, Collection<Post>> shareMap) {
        postsDb.updatePostAudience(shareMap);
    }

    @WorkerThread
    public List<DecryptStats> getMessageDecryptStats(long lastRowId) {
        return messagesDb.getMessageDecryptStats(lastRowId);
    }

    @WorkerThread
    public DecryptStats getMessageDecryptStats(String messageId) {
        return messagesDb.getMessageDecryptStats(messageId);
    }

    @WorkerThread
    public List<GroupDecryptStats> getGroupPostDecryptStats(long lastRowId) {
        return postsDb.getGroupPostDecryptStats(lastRowId);
    }

    @WorkerThread
    public GroupDecryptStats getGroupPostDecryptStats(String postId) {
        return postsDb.getGroupPostDecryptStats(postId);
    }

    @WorkerThread
    public List<GroupDecryptStats> getGroupCommentDecryptStats(long lastRowId) {
        return postsDb.getGroupCommentDecryptStats(lastRowId);
    }

    @WorkerThread
    public GroupDecryptStats getGroupCommentDecryptStats(String commentId) {
        return postsDb.getGroupCommentDecryptStats(commentId);
    }

    @WorkerThread
    public ExternalShareInfo getExternalShareInfo(@NonNull String postId) {
        return postsDb.getExternalShareInfo(postId);
    }

    @WorkerThread
    public void setExternalShareInfo(@NonNull String postId, @Nullable String shareId, @Nullable String shareKey) {
        databaseWriteExecutor.execute(() -> {
            postsDb.setExternalShareInfo(postId, shareId, shareKey);
        });
    }

    @WorkerThread
    public void cleanup() {
        Log.i("ContentDb.cleanup");
        if (postsDb.cleanup()) {
            databaseHelper.getWritableDatabase().execSQL("VACUUM");
            Log.i("ContentDb.cleanup: vacuum");
            observers.notifyFeedCleanup();
        }
        cleanupMedia();
    }

    private void cleanupMedia() {
        HashSet<String> mediaPaths = new HashSet<>();
        List<Media> allMedia = getAllMedia();
        for (Media media : allMedia) {
            if (media.file != null) {
                mediaPaths.add(media.file.getAbsolutePath());
            }
        }
        List<File> replyFiles = messagesDb.getReplyMediaFiles();
        for (File file : replyFiles) {
            mediaPaths.add(file.getAbsolutePath());
        }
        File mediaDir = FileStore.getInstance().getMediaDir();
        cleanupLeakedMediaFiles(mediaDir, mediaPaths);
    }

    private static void cleanupLeakedMediaFiles(File file, HashSet<String> mediaPaths) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                cleanupLeakedMediaFiles(files[i], mediaPaths);
            }
        } else if (!mediaPaths.contains(file.getAbsolutePath())) {
            long ageMs = System.currentTimeMillis() - file.lastModified();
            boolean oldEnough = ageMs > DateUtils.DAY_IN_MILLIS;
            if (oldEnough || true) {
                boolean deleted = file.delete();
                if (deleted) {
                    Log.w("MediaDb/cleanupLeakedMediaFiles deleted orphaned media file: " + file.getName());
                } else {
                    Log.w("MediaDb/cleanupLeakedMediaFiles failed to delete orphaned media file: " + file.getName());
                }
            }
        }
    }

    @NonNull
    public Collection<Post> getShareablePosts() {
        return postsDb.getShareablePosts();
    }

    @NonNull
    public List<Post> getArchivedPosts(Long timestamp, @Nullable Integer count, boolean after) {
        return postsDb.getArchivedPosts(timestamp, count, after);
    }

    @WorkerThread
    public @Nullable Post getArchivePost(@NonNull String postId) {
        return postsDb.getArchivePost(postId);
    }

    @WorkerThread
    public void archivePosts() {
        postsDb.archivePosts();
    }

    @WorkerThread
    public void deleteArchive() {
        postsDb.deleteArchive();
    }

    public void deleteDb() {
        databaseHelper.deleteDb();
    }

    public void checkIndexes() {
        String[] indexNames = new String[] {
                ArchiveTable.INDEX_POST_KEY,
                ArchiveTable.INDEX_TIMESTAMP,
                AudienceTable.INDEX_AUDIENCE_KEY,
                CommentsTable.INDEX_COMMENT_KEY,
                FutureProofTable.INDEX_FUTURE_PROOF_KEY,
                GroupMembersTable.INDEX_GROUP_USER,
                HistoryResendPayloadTable.INDEX_HISTORY_RESEND_ID,
                MediaTable.INDEX_DEC_HASH_KEY,
                MediaTable.INDEX_MEDIA_KEY,
                MentionsTable.INDEX_MENTION_KEY,
                MessagesTable.INDEX_MESSAGE_KEY,
                OutgoingSeenReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY,
                PostsTable.INDEX_POST_KEY,
                PostsTable.INDEX_TIMESTAMP,
                RepliesTable.INDEX_MESSAGE_KEY,
                RerequestsTable.INDEX_REREQUEST_KEY,
                SeenTable.INDEX_SEEN_KEY,
                OutgoingPlayedReceiptsTable.INDEX_OUTGOING_RECEIPT_KEY,
                MomentsTable.INDEX_POST_KEY
        };

        for (String name : indexNames) {
            Log.i("ContentDb.checkIndexes checking for index " + name);
            if (!hasIndex(name)) {
                Log.sendErrorReport("ContentDb.checkIndexes missing expected index " + name);
            }
        }
    }

    private boolean hasIndex(String name) {
        try (Cursor postIndexCountCursor = databaseHelper.getReadableDatabase().rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type=? AND name=?", new String[]{"index", name})) {
            if (postIndexCountCursor.moveToNext()) {
                if (postIndexCountCursor.getInt(0) <= 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
