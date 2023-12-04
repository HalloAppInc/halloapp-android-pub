package com.halloapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;
import androidx.lifecycle.LifecycleOwner;

import com.google.crypto.tink.subtle.Random;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Post;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.group.GroupFeedKeyManager;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.PrivateEdECKey;
import com.halloapp.crypto.keys.PrivateXECKey;
import com.halloapp.crypto.keys.PublicEdECKey;
import com.halloapp.crypto.keys.PublicXECKey;
import com.halloapp.crypto.signal.SignalKeyManager;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.nux.ZeroZoneManager;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.clients.CommentIdContext;
import com.halloapp.proto.clients.ContentDetails;
import com.halloapp.proto.clients.GroupHistoryPayload;
import com.halloapp.proto.clients.MemberDetails;
import com.halloapp.proto.clients.PostIdContext;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogProvider;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.groups.GroupsApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("VisibleForTests")
public class Debug {

    private static final String DEBUG_MENU_RESET_REGISTRATION = "Reset registration";
    private static final String DEBUG_MENU_TEST_ONBOARDING = "Test onboarding";
    private static final String DEBUG_MENU_LOGOUT = "Logout";
    private static final String DEBUG_MENU_DELETE_CONTENT_DB = "Delete posts DB";
    private static final String DEBUG_MENU_DELETE_CONTACTS_DB = "Delete contacts DB";
    private static final String DEBUG_MENU_DELETE_GALLERY_DB = "Delete gallery DB";
    private static final String DEBUG_MENU_SYNC_CONTACTS = "Sync contacts";
    private static final String DEBUG_MENU_SET_COMMENTS_SEEN = "Set comments seen";
    private static final String DEBUG_MENU_SET_INCOMING_POSTS_UNSEEN = "Set incoming posts unseen";
    private static final String DEBUG_MENU_CLEANUP_POSTS = "Cleanup posts";
    private static final String DEBUG_MENU_VISIT_EXPIRATION_ACTIVITY = "Visit expiration activity";
    private static final String DEBUG_MENU_CLEAR_AVATAR_CACHE = "Clear avatar disk cache";
    private static final String DEBUG_MENU_REMOVE_AVATAR = "Remove avatar";
    private static final String DEBUG_MENU_CLEAR_KEY_STORE = "Clear key store";
    private static final String DEBUG_MENU_SET_COMMENTS_UNSEEN = "Set comments unseen";
    private static final String DEBUG_MENU_SKIP_OUTBOUND_MESSAGE_KEY = "Skip outbound message key";
    private static final String DEBUG_MENU_FETCH_SERVER_PROPS = "Fetch server props";
    private static final String DEBUG_MENU_CRASH_PRECONDITION = "Preconditions crash null";
    private static final String DEBUG_MENU_TRY_DUP_COMMENT = "Try insert duplicate comment";
    private static final String DEBUG_MENU_CLEAR_LOGS = "Clear logs";
    private static final String DEBUG_MENU_RUN_DAILY_WORKER = "Run daily worker";
    private static final String DEBUG_MENU_CORRUPT_KEY_STORE = "Corrupt key store";
    private static final String DEBUG_MENU_NORMAL_USER_MODE = "Normal user mode";
    private static final String DEBUG_MENU_ADD_TO_ARCHIVE = "Add to archive";
    private static final String DEBUG_MENU_REMOVE_ARCHIVE = "Remove archive";
    private static final String DEBUG_MENU_FORCE_ZERO_ZONE = "Force Zero Zone";
    private static final String DEBUG_MENU_FORCE_LEAVE_ZERO_ZONE = "Leave Zero Zone";
    private static final String DEBUG_MENU_SKIP_OUTBOUND_GROUP_FEED_KEY = "Skip outbound group key";
    private static final String DEBUG_MENU_SKIP_INBOUND_GROUP_FEED_KEY = "Skip inbound group key";
    private static final String DEBUG_MENU_CORRUPT_GROUP_KEY_STORE = "Corrupt group key store";
    private static final String DEBUG_MENU_CORRUPT_HOME_KEY_STORE = "Corrupt home key store";
    private static final String DEBUG_MENU_CLEAR_DOWNLOADED_EMOJIS = "Clear downloaded emojis";
    private static final String DEBUG_MENU_ADD_HISTORY_RESEND_TOMBSTONES = "Add history resend tombstones";

    private static final BgWorkers bgWorkers = BgWorkers.getInstance();

    public static void showMainDebugMenu(@NonNull Activity activity, View anchor) {
        PopupMenu menu = new PopupMenu(activity, anchor);
        menu.getMenu().add(DEBUG_MENU_RESET_REGISTRATION);
        menu.getMenu().add(DEBUG_MENU_TEST_ONBOARDING);
        menu.getMenu().add(DEBUG_MENU_LOGOUT);
        menu.getMenu().add(DEBUG_MENU_DELETE_CONTENT_DB);
        menu.getMenu().add(DEBUG_MENU_DELETE_CONTACTS_DB);
        menu.getMenu().add(DEBUG_MENU_DELETE_GALLERY_DB);
        menu.getMenu().add(DEBUG_MENU_SYNC_CONTACTS);
        menu.getMenu().add(DEBUG_MENU_SET_COMMENTS_SEEN);
        menu.getMenu().add(DEBUG_MENU_SET_INCOMING_POSTS_UNSEEN);
        menu.getMenu().add(DEBUG_MENU_CLEANUP_POSTS);
        menu.getMenu().add(DEBUG_MENU_VISIT_EXPIRATION_ACTIVITY);
        menu.getMenu().add(DEBUG_MENU_CLEAR_AVATAR_CACHE);
        menu.getMenu().add(DEBUG_MENU_REMOVE_AVATAR);
        menu.getMenu().add(DEBUG_MENU_CLEAR_KEY_STORE);
        menu.getMenu().add(DEBUG_MENU_FETCH_SERVER_PROPS);
        menu.getMenu().add(DEBUG_MENU_CRASH_PRECONDITION);
        menu.getMenu().add(DEBUG_MENU_TRY_DUP_COMMENT);
        menu.getMenu().add(DEBUG_MENU_CLEAR_LOGS);
        menu.getMenu().add(DEBUG_MENU_RUN_DAILY_WORKER);
        menu.getMenu().add(DEBUG_MENU_CORRUPT_KEY_STORE);
        menu.getMenu().add(DEBUG_MENU_CORRUPT_GROUP_KEY_STORE);
        menu.getMenu().add(DEBUG_MENU_CORRUPT_HOME_KEY_STORE);
        menu.getMenu().add(DEBUG_MENU_NORMAL_USER_MODE);
        menu.getMenu().add(DEBUG_MENU_ADD_TO_ARCHIVE);
        menu.getMenu().add(DEBUG_MENU_REMOVE_ARCHIVE);
        menu.getMenu().add(DEBUG_MENU_FORCE_ZERO_ZONE);
        menu.getMenu().add(DEBUG_MENU_FORCE_LEAVE_ZERO_ZONE);
        menu.getMenu().add(DEBUG_MENU_CLEAR_DOWNLOADED_EMOJIS);
        menu.setOnMenuItemClickListener(item -> {
            SnackbarHelper.showInfo(activity, item.getTitle());
            switch (item.getTitle().toString()) {
                case DEBUG_MENU_RESET_REGISTRATION: {
                    new ResetRegistrationTask(activity.getApplication()).execute();
                    break;
                }
                case DEBUG_MENU_TEST_ONBOARDING: {
                    new TestOnboardingTask(activity.getApplication()).execute();
                    break;
                }
                case DEBUG_MENU_LOGOUT: {
                    Connection.getInstance().disconnect();
                    break;
                }
                case DEBUG_MENU_DELETE_CONTENT_DB: {
                    new DeleteContentDbTask(activity.getApplication()).execute();
                    break;
                }
                case DEBUG_MENU_DELETE_GALLERY_DB: {
                    new DeleteGalleryDbTask(activity.getApplication()).execute();
                    break;
                }
                case DEBUG_MENU_CLEAR_DOWNLOADED_EMOJIS: {
                    bgWorkers.execute(() -> {
                        Preferences.getInstance().setLocalEmojiVersion(0);
                    });
                    break;
                }
                case DEBUG_MENU_DELETE_CONTACTS_DB: {
                    new DeleteContactsDbTask(activity.getApplication()).execute();
                    break;
                }
                case DEBUG_MENU_SYNC_CONTACTS: {
                    ContactsSync.getInstance().forceFullContactsSync();
                    break;
                }
                case DEBUG_MENU_SET_COMMENTS_SEEN: {
                    ContentDb.getInstance().setCommentsSeen(true);
                }
                case DEBUG_MENU_SET_INCOMING_POSTS_UNSEEN: {
                    ContentDb.getInstance().setIncomingPostsSeen(Post.SEEN_NO);
                }
                case DEBUG_MENU_CLEANUP_POSTS: {
                    new CleanupPostsTask(activity.getApplication()).execute();
                    break;
                }
                case DEBUG_MENU_VISIT_EXPIRATION_ACTIVITY: {
                    activity.startActivity(new Intent(activity.getApplicationContext(), AppExpirationActivity.class));
                    break;
                }
                case DEBUG_MENU_CLEAR_AVATAR_CACHE: {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            File dir = new File(activity.getFilesDir(), "avatars");
                            String[] files = dir.list();
                            for (String file : files) {
                                File toDelete = new File(dir, file);
                                Log.d("DEBUG Deleting " + toDelete.getAbsolutePath());
                                toDelete.delete();
                            }
                            return null;
                        }
                    }.execute();

                    break;
                }
                case DEBUG_MENU_REMOVE_AVATAR: {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            AvatarLoader avatarLoader = AvatarLoader.getInstance();
                            avatarLoader.removeMyAvatar();
                            return null;
                        }
                    }.execute();

                    break;
                }
                case DEBUG_MENU_CLEAR_KEY_STORE: {
                    bgWorkers.execute(() -> {
                        try {
                            EncryptedKeyStore.getInstance().clearAll();
                        } catch (Exception e) {
                            Log.w("DEBUG failed to clear key store");
                        }
                    });
                    break;
                }
                case DEBUG_MENU_FETCH_SERVER_PROPS: {
                    Connection.getInstance().requestServerProps();
                    break;
                }
                case DEBUG_MENU_CRASH_PRECONDITION: {
                    Preconditions.checkNotNull(null);
                    break;
                }
                case DEBUG_MENU_TRY_DUP_COMMENT: {
                    UserId senderUserId = new UserId(Me.getInstance().getUser());
                    String postId = RandomId.create();
                    long timestamp = System.currentTimeMillis() * 1000L;
                    Post post = new Post(
                            -1,
                            senderUserId,
                            postId,
                            timestamp,
                            Post.TRANSFERRED_NO,
                            Post.SEEN_NO,
                            "Main post text"
                    );
                    Comment comment = new Comment(
                            -1,
                            postId,
                            senderUserId,
                            RandomId.create(),
                            null,
                            timestamp,
                            Comment.TRANSFERRED_NO,
                            false,
                            "TESTING COMMENT DUP"
                    );
                    comment.setParentPost(post);

                    ContentDb contentDb = ContentDb.getInstance();
                    contentDb.addPost(post);
                    contentDb.addComment(comment);
                    contentDb.addComment(comment);
                    break;
                }
                case DEBUG_MENU_CLEAR_LOGS: {
                    bgWorkers.execute(() -> FileStore.getInstance().purgeAllLogFiles());
                    break;
                }
                case DEBUG_MENU_RUN_DAILY_WORKER: {
                    DailyWorker.scheduleDebug(AppContext.getInstance().get());
                    break;
                }
                case DEBUG_MENU_CORRUPT_KEY_STORE: {
                    bgWorkers.execute(() -> {
                        List<Contact> addressBookUsers = ContactsDb.getInstance().getUsers(); // TODO: Could show ones from open chats, too
                        Contact.sort(addressBookUsers);
                        List<CharSequence> names = new ArrayList<>();
                        for (Contact contact : addressBookUsers) {
                            names.add(contact.getDisplayName());
                        }
                        CharSequence[] arr = new CharSequence[0];
                        activity.runOnUiThread(() -> {
                            AlertDialog.Builder selectUserBuilder = new AlertDialog.Builder(activity);
                            selectUserBuilder.setTitle("Pick user (from addressbook)")
                                    .setItems(names.toArray(arr), (dialog, whichUser) -> {
                                        Contact contact = addressBookUsers.get(whichUser);
                                        UserId peerUserId = contact.userId;
                                        Log.d("Debug selected: " + whichUser + " -> " + contact);
                                        showCorruptKeyStoreDialog(activity, peerUserId);
                                    });
                            selectUserBuilder.create().show();
                        });
                    });
                    break;
                }
                case DEBUG_MENU_CORRUPT_GROUP_KEY_STORE: {
                    bgWorkers.execute(() -> {
                        List<Group> groups = ContentDb.getInstance().getActiveGroups();
                        List<GroupId> groupIds = new ArrayList<>();
                        List<String> names = new ArrayList<>();
                        for (Group group : groups) {
                            groupIds.add(group.groupId);
                            names.add(group.name + " - " + group.groupId.rawId());
                        }

                        CharSequence[] arr = new CharSequence[0];
                        activity.runOnUiThread(() -> {
                            AlertDialog.Builder selectGroupBuilder = new AlertDialog.Builder(activity);
                            selectGroupBuilder.setTitle("Pick group (from active)")
                                    .setItems(names.toArray(arr), (dialog, which) -> {
                                        GroupId groupId = groupIds.get(which);
                                        Log.d("Debug selected: " + which + " -> " + groupId);
                                        showCorruptGroupKeyStoreDialog(activity, groupId);
                                    });
                            selectGroupBuilder.create().show();
                        });
                    });
                    break;
                }
                case DEBUG_MENU_CORRUPT_HOME_KEY_STORE: {
                    showCorruptHomeKeyStoreDialog(activity);
                    break;
                }
                case DEBUG_MENU_NORMAL_USER_MODE: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("Enter normal user mode?")
                            .setMessage("Force-quit app to be treated as an internal user again")
                            .setNegativeButton("No", (dialog, which) -> {
                                // Nothing
                            })
                            .setPositiveButton("Yes", (dialog, which) -> ServerProps.getInstance().forceExternalUser());
                    builder.show();
                    break;
                }
                case DEBUG_MENU_ADD_TO_ARCHIVE: {
                    bgWorkers.execute(() -> {
                        ContentDb.getInstance().archivePosts();
                    });
                    break;
                }
                case DEBUG_MENU_REMOVE_ARCHIVE: {
                    bgWorkers.execute(() -> {
                        ContentDb.getInstance().deleteArchive();
                    });
                    break;
                }
                case DEBUG_MENU_FORCE_LEAVE_ZERO_ZONE: {
                    bgWorkers.execute(() -> {
                        Preferences.getInstance().setForcedZeroZone(false);
                        Preferences.getInstance().setZeroZoneState(ZeroZoneManager.ZeroZoneState.NOT_IN_ZERO_ZONE);
                    });
                    break;
                }
                case DEBUG_MENU_FORCE_ZERO_ZONE: {
                    bgWorkers.execute(() -> {
                        Preferences.getInstance().setForcedZeroZone(true);
                        Preferences.getInstance().setZeroZoneGroupId(null);
                        Preferences.getInstance().setZeroZoneState(ZeroZoneManager.ZeroZoneState.WAITING_FOR_SYNC);
                    });
                    break;
                }
            }
            return false;
        });
        menu.show();
    }

    public static void showCommentsDebugMenu(@NonNull Activity activity, View anchor, @Nullable GroupId groupId, String postId) {
        PopupMenu menu = new PopupMenu(activity, anchor);

        menu.getMenu().add(DEBUG_MENU_SET_COMMENTS_UNSEEN);
        if (groupId != null) {
            menu.getMenu().add(DEBUG_MENU_SKIP_OUTBOUND_GROUP_FEED_KEY);
            menu.getMenu().add(DEBUG_MENU_SKIP_INBOUND_GROUP_FEED_KEY);
            menu.getMenu().add(DEBUG_MENU_CORRUPT_GROUP_KEY_STORE);
        } else {
            menu.getMenu().add(DEBUG_MENU_CORRUPT_HOME_KEY_STORE);
        }
        menu.setOnMenuItemClickListener(item -> {
            SnackbarHelper.showInfo(activity, item.getTitle());
            switch (item.getTitle().toString()) {
                case DEBUG_MENU_SET_COMMENTS_UNSEEN: {
                    ContentDb.getInstance().setCommentsSeen(postId, false);
                    break;
                }
                case DEBUG_MENU_SKIP_OUTBOUND_GROUP_FEED_KEY: {
                    skipOutboundGroupFeedKey(groupId);
                    break;
                }
                case DEBUG_MENU_SKIP_INBOUND_GROUP_FEED_KEY: {
                    skipInboundGroupFeedKey(activity, groupId);
                    break;
                }
                case DEBUG_MENU_CORRUPT_GROUP_KEY_STORE: {
                    showCorruptGroupKeyStoreDialog(activity, groupId);
                    break;
                }
                case DEBUG_MENU_CORRUPT_HOME_KEY_STORE: {
                    showCorruptHomeKeyStoreDialog(activity);
                    break;
                }
            }
            return false;
        });
        menu.show();

    }

    public static void showChatDebugMenu(@NonNull Activity activity, View anchor, ChatId chatId) {
        PopupMenu menu = new PopupMenu(activity, anchor);

        if (chatId instanceof  UserId) {
            menu.getMenu().add(DEBUG_MENU_SKIP_OUTBOUND_MESSAGE_KEY);
            menu.getMenu().add(DEBUG_MENU_CORRUPT_KEY_STORE);
            menu.setOnMenuItemClickListener(item -> {
                SnackbarHelper.showInfo(activity, item.getTitle());
                switch (item.getTitle().toString()) {
                    case DEBUG_MENU_SKIP_OUTBOUND_MESSAGE_KEY: {
                        bgWorkers.execute(() -> {
                            try {
                                SignalKeyManager.getInstance().getNextOutboundMessageKey((UserId)chatId);
                            } catch (Exception e) {
                                Log.w("DEBUG error skipping outbound message key", e);
                            }
                        });
                        break;
                    }
                    case DEBUG_MENU_CORRUPT_KEY_STORE: {
                        showCorruptKeyStoreDialog(activity, (UserId) chatId);
                        break;
                    }
                }
                return false;
            });
        }
        menu.show();
    }

    public static void showGroupDebugMenu(@NonNull Activity activity, View anchor, GroupId groupId) {
        PopupMenu menu = new PopupMenu(activity, anchor);

        menu.getMenu().add(DEBUG_MENU_SKIP_OUTBOUND_GROUP_FEED_KEY);
        menu.getMenu().add(DEBUG_MENU_SKIP_INBOUND_GROUP_FEED_KEY);
        menu.getMenu().add(DEBUG_MENU_CORRUPT_GROUP_KEY_STORE);
        menu.getMenu().add(DEBUG_MENU_ADD_HISTORY_RESEND_TOMBSTONES);
        menu.setOnMenuItemClickListener(item -> {
            SnackbarHelper.showInfo(activity, item.getTitle());
            switch (item.getTitle().toString()) {
                case DEBUG_MENU_SKIP_OUTBOUND_GROUP_FEED_KEY: {
                    skipOutboundGroupFeedKey(groupId);
                    break;
                }
                case DEBUG_MENU_SKIP_INBOUND_GROUP_FEED_KEY: {
                    skipInboundGroupFeedKey(activity, groupId);
                    break;
                }
                case DEBUG_MENU_CORRUPT_GROUP_KEY_STORE: {
                    showCorruptGroupKeyStoreDialog(activity, groupId);
                    break;
                }
                case DEBUG_MENU_ADD_HISTORY_RESEND_TOMBSTONES: {
                    bgWorkers.execute(() -> {
                        List<MemberInfo> members = ContentDb.getInstance().getGroupMembers(groupId);
                        List<MemberInfo> otherMembers = new ArrayList<>();
                        List<CharSequence> names = new ArrayList<>();
                        for (MemberInfo member : members) {
                            if (member.userId.isMe()) {
                                continue;
                            }
                            otherMembers.add(member);
                            names.add(member.userId.rawId());
                        }
                        CharSequence[] arr = new CharSequence[0];
                        activity.runOnUiThread(() -> {
                            AlertDialog.Builder selectUserBuilder = new AlertDialog.Builder(activity);
                            selectUserBuilder.setTitle("Pick sender")
                                    .setItems(names.toArray(arr), (dialog, whichUser) -> {
                                        MemberInfo member = otherMembers.get(whichUser);
                                        UserId peerUserId = member.userId;
                                        Log.d("Debug selected: " + whichUser + " -> " + member);
                                        bgWorkers.execute(() -> {
                                            String postId = RandomId.create();
                                            String parentCommentId = RandomId.create();
                                            String childCommentId = RandomId.create();
                                            long now = System.currentTimeMillis();
                                            long senderUid = Long.parseLong(peerUserId.rawId());
                                            ContentDetails postDetails = ContentDetails.newBuilder().setPostIdContext(PostIdContext.newBuilder().setFeedPostId(postId).setTimestamp(now).setSenderUid(senderUid).build()).build();
                                            ContentDetails parentCommentDetails = ContentDetails.newBuilder().setCommentIdContext(CommentIdContext.newBuilder().setCommentId(parentCommentId).setFeedPostId(postId).setTimestamp(now).setSenderUid(senderUid).build()).build();
                                            ContentDetails childCommentDetails = ContentDetails.newBuilder().setCommentIdContext(CommentIdContext.newBuilder().setCommentId(childCommentId).setParentCommentId(parentCommentId).setFeedPostId(postId).setTimestamp(now).setSenderUid(senderUid).build()).build();
                                            GroupHistoryPayload groupHistoryPayload = GroupHistoryPayload.newBuilder()
                                                    .addContentDetails(postDetails)
                                                    .addContentDetails(parentCommentDetails)
                                                    .addContentDetails(childCommentDetails)
                                                    .addMemberDetails(MemberDetails.newBuilder().setUid(Long.parseLong(Me.getInstance().getUser())).build())
                                                    .build();
                                            GroupsApi.getInstance().handleGroupHistoryPayload(groupHistoryPayload, groupId);
                                        });
                                    });
                            selectUserBuilder.create().show();
                        });
                    });
                    break;
                }
            }
            return false;
        });
        menu.show();
    }

    private static void skipOutboundGroupFeedKey(GroupId groupId) {
        bgWorkers.execute(() -> {
            try {
                GroupFeedKeyManager.getInstance().getNextOutboundMessageKey(groupId);
            } catch (Exception e) {
                Log.w("DEBUG error skipping outbound group key", e);
            }
        });
    }

    private static void skipInboundGroupFeedKey(Activity activity, GroupId groupId) {
        bgWorkers.execute(() -> {
            List<MemberInfo> members = ContentDb.getInstance().getGroupMembers(groupId);
            List<MemberInfo> otherMembers = new ArrayList<>();
            List<CharSequence> names = new ArrayList<>();
            for (MemberInfo member : members) {
                if (member.userId.isMe()) {
                    continue;
                }
                otherMembers.add(member);
                names.add(member.userId.rawId());
            }
            CharSequence[] arr = new CharSequence[0];
            activity.runOnUiThread(() -> {
                AlertDialog.Builder selectUserBuilder = new AlertDialog.Builder(activity);
                selectUserBuilder.setTitle("Pick user")
                        .setItems(names.toArray(arr), (dialog, whichUser) -> {
                            MemberInfo member = otherMembers.get(whichUser);
                            UserId peerUserId = member.userId;
                            Log.d("Debug selected: " + whichUser + " -> " + member);
                            bgWorkers.execute(() -> {
                                try {
                                    GroupFeedKeyManager.getInstance().getInboundMessageKey(groupId, peerUserId);
                                } catch (CryptoException e) {
                                    Log.e("Failed to get inbound message key", e);
                                }
                            });
                        });
                selectUserBuilder.create().show();
            });
        });
    }

    private static void showCorruptKeyStoreDialog(@NonNull Activity activity, @NonNull UserId peerUserId) {
        EncryptedKeyStore encryptedKeyStore = EncryptedKeyStore.getInstance();
        CharSequence[] corruptionOptions = {
                "Mark session not set up",
                "Mark peer not responded",
                "Remove skipped message keys",
                "Remove peer identity key",
                "Mutate peer identity key",
                "Remove peer signed pre key",
                "Remove peer one-time pre key",
                "Remove peer one-time pre key ID",
                "Mutate root key",
                "Mutate outbound chain key",
                "Mutate inbound chain key",
                "Mutate outbound ephemeral key",
                "Mutate inbound ephemeral key",
                "Mutate outbound ephemeral key ID",
                "Mutate inbound ephemeral key ID",
                "Mutate outbound previous chain length",
                "Mutate inbound previous chain length",
                "Mutate outbound current chain index",
                "Mutate inbound current chain index",
        };

        final int KEY_SIZE = 32;
        final int MAX_NUM = 500;
        Runnable[] corruptionActions = {
                () -> encryptedKeyStore.edit().setSessionAlreadySetUp(peerUserId, false).apply(),
                () -> encryptedKeyStore.edit().setPeerResponded(peerUserId, false).apply(),
                () -> encryptedKeyStore.edit().clearSkippedMessageKeys(peerUserId).apply(),
                () -> encryptedKeyStore.edit().clearPeerPublicIdentityKey(peerUserId).apply(),
                () -> encryptedKeyStore.edit().setPeerPublicIdentityKey(peerUserId, new PublicEdECKey(Random.randBytes(KEY_SIZE))).apply(),
                () -> encryptedKeyStore.edit().clearPeerSignedPreKey(peerUserId).apply(),
                () -> encryptedKeyStore.edit().clearPeerOneTimePreKey(peerUserId).apply(),
                () -> encryptedKeyStore.edit().clearPeerOneTimePreKeyId(peerUserId).apply(),
                () -> encryptedKeyStore.edit().setRootKey(peerUserId, Random.randBytes(KEY_SIZE)).apply(),
                () -> encryptedKeyStore.edit().setOutboundChainKey(peerUserId, Random.randBytes(KEY_SIZE)).apply(),
                () -> encryptedKeyStore.edit().setInboundChainKey(peerUserId, Random.randBytes(KEY_SIZE)).apply(),
                () -> {
                    try {
                        encryptedKeyStore.edit().setOutboundEphemeralKey(peerUserId, new PrivateXECKey(Random.randBytes(KEY_SIZE))).apply();
                    } catch (CryptoException e) {
                        Log.e("Failed to set outbound ephemeral key", e);
                    }
                },
                () -> {
                    try {
                        encryptedKeyStore.edit().setInboundEphemeralKey(peerUserId, new PublicXECKey(Random.randBytes(KEY_SIZE))).apply();
                    } catch (CryptoException e) {
                        Log.e("Failed to set inbound ephemeral key", e);
                    }
                },
                () -> encryptedKeyStore.edit().setOutboundEphemeralKeyId(peerUserId, Random.randInt(MAX_NUM)).apply(),
                () -> encryptedKeyStore.edit().setInboundEphemeralKeyId(peerUserId, Random.randInt(MAX_NUM)).apply(),
                () -> encryptedKeyStore.edit().setOutboundPreviousChainLength(peerUserId, Random.randInt(MAX_NUM)).apply(),
                () -> encryptedKeyStore.edit().setInboundPreviousChainLength(peerUserId, Random.randInt(MAX_NUM)).apply(),
                () -> encryptedKeyStore.edit().setOutboundCurrentChainIndex(peerUserId, Random.randInt(MAX_NUM)).apply(),
                () -> encryptedKeyStore.edit().setInboundCurrentChainIndex(peerUserId, Random.randInt(MAX_NUM)).apply(),
        };

        AlertDialog.Builder corruptionPickerBuilder = new AlertDialog.Builder(activity);
        corruptionPickerBuilder.setTitle("Pick corruption")
                .setItems(corruptionOptions, (ignored, which) -> {
                    Log.d("Debug selected corruption of " + which + " -> " + corruptionOptions[which]);
                    bgWorkers.execute(corruptionActions[which]);
                });
        corruptionPickerBuilder.create().show();
    }

    private static void showCorruptGroupKeyStoreDialog(@NonNull Activity activity, @NonNull GroupId groupId) {
        EncryptedKeyStore encryptedKeyStore = EncryptedKeyStore.getInstance();
        CharSequence[] corruptionOptions = {
                "Mark session not set up",
                "Remove skipped message keys",
                "Remove my group signing key",
                "Mutate my group signing key",
                "Remove peer group signing key",
                "Mutate peer group signing key",
                "Mutate outbound chain key",
                "Mutate inbound chain key",
                "Mutate outbound current chain index",
                "Mutate inbound current chain index",
        };

        final int PRIVATE_SIGNING_KEY_SIZE = 64;
        final int KEY_SIZE = 32;
        final int MAX_NUM = 500;
        Runnable[] corruptionActions = {
                () -> encryptedKeyStore.edit().clearGroupSendAlreadySetUp(groupId).apply(),
                () -> selectUserFromGroup(activity, groupId, userId -> encryptedKeyStore.edit().clearSkippedGroupFeedKeys(groupId, userId).apply()),
                () -> encryptedKeyStore.edit().clearMyGroupSigningKey(groupId).apply(),
                () -> encryptedKeyStore.edit().setMyGroupSigningKey(groupId, new PrivateEdECKey(Random.randBytes(PRIVATE_SIGNING_KEY_SIZE))).apply(),
                () -> selectUserFromGroup(activity, groupId, userId -> encryptedKeyStore.edit().clearPeerGroupSigningKey(groupId, userId).apply()),
                () -> selectUserFromGroup(activity, groupId, userId -> encryptedKeyStore.edit().setPeerGroupSigningKey(groupId, userId, new PublicEdECKey(Random.randBytes(KEY_SIZE))).apply()),
                () -> encryptedKeyStore.edit().setMyGroupChainKey(groupId, Random.randBytes(KEY_SIZE)).apply(),
                () -> selectUserFromGroup(activity, groupId, userId -> encryptedKeyStore.edit().setPeerGroupChainKey(groupId, userId, Random.randBytes(KEY_SIZE)).apply()),
                () -> encryptedKeyStore.edit().setMyGroupCurrentChainIndex(groupId, Random.randInt(MAX_NUM)).apply(),
                () -> selectUserFromGroup(activity, groupId, userId -> encryptedKeyStore.edit().setPeerGroupCurrentChainIndex(groupId, userId, Random.randInt(MAX_NUM)).apply()),
        };

        AlertDialog.Builder corruptionPickerBuilder = new AlertDialog.Builder(activity);
        corruptionPickerBuilder.setTitle("Pick corruption")
                .setItems(corruptionOptions, (ignored, which) -> {
                    Log.d("Debug selected corruption of " + which + " -> " + corruptionOptions[which]);
                    bgWorkers.execute(corruptionActions[which]);
                });
        corruptionPickerBuilder.create().show();
    }

    private static void showCorruptHomeKeyStoreDialog(@NonNull Activity activity) {
        activity.runOnUiThread(() -> {
            AlertDialog.Builder selectHomeBuilder = new AlertDialog.Builder(activity);
            selectHomeBuilder.setTitle("Pick home category")
                    .setItems(new String[]{"All", "Favorites"}, (dialog, which) -> {
                        Log.d("Debug selected: " + which);
                        showCorruptHomeKeyStoreDialog(activity, which == 1);
                    });
            selectHomeBuilder.create().show();
        });
    }

    private static void showCorruptHomeKeyStoreDialog(@NonNull Activity activity, boolean favorites) {
        EncryptedKeyStore encryptedKeyStore = EncryptedKeyStore.getInstance();
        CharSequence[] corruptionOptions = {
                "Mark session not set up",
                "Remove skipped message keys",
                "Remove my home signing key",
                "Mutate my home signing key",
                "Remove peer home signing key",
                "Mutate peer home signing key",
                "Mutate outbound chain key",
                "Mutate inbound chain key",
                "Mutate outbound current chain index",
                "Mutate inbound current chain index",
        };

        final int PRIVATE_SIGNING_KEY_SIZE = 64;
        final int KEY_SIZE = 32;
        final int MAX_NUM = 500;
        Runnable[] corruptionActions = {
                () -> encryptedKeyStore.edit().clearHomeSendAlreadySetUp(favorites).apply(),
                () -> selectUserFromHome(activity, userId -> encryptedKeyStore.edit().clearSkippedHomeFeedKeys(favorites, userId).apply()),
                () -> encryptedKeyStore.edit().clearMyHomeSigningKey(favorites).apply(),
                () -> encryptedKeyStore.edit().setMyHomeSigningKey(favorites, new PrivateEdECKey(Random.randBytes(PRIVATE_SIGNING_KEY_SIZE))).apply(),
                () -> selectUserFromHome(activity, userId -> encryptedKeyStore.edit().clearPeerHomeSigningKey(favorites, userId).apply()),
                () -> selectUserFromHome(activity, userId -> encryptedKeyStore.edit().setPeerHomeSigningKey(favorites, userId, new PublicEdECKey(Random.randBytes(KEY_SIZE))).apply()),
                () -> encryptedKeyStore.edit().setMyHomeChainKey(favorites, Random.randBytes(KEY_SIZE)).apply(),
                () -> selectUserFromHome(activity, userId -> encryptedKeyStore.edit().setPeerHomeChainKey(favorites, userId, Random.randBytes(KEY_SIZE)).apply()),
                () -> encryptedKeyStore.edit().setMyHomeCurrentChainIndex(favorites, Random.randInt(MAX_NUM)).apply(),
                () -> selectUserFromHome(activity, userId -> encryptedKeyStore.edit().setPeerHomeCurrentChainIndex(favorites, userId, Random.randInt(MAX_NUM)).apply()),
        };

        AlertDialog.Builder corruptionPickerBuilder = new AlertDialog.Builder(activity);
        corruptionPickerBuilder.setTitle("Pick corruption")
                .setItems(corruptionOptions, (ignored, which) -> {
                    Log.d("Debug selected corruption of " + which + " -> " + corruptionOptions[which]);
                    bgWorkers.execute(corruptionActions[which]);
                });
        corruptionPickerBuilder.create().show();
    }

    private static void selectUserFromGroup(@NonNull Activity activity, @NonNull GroupId groupId, @NonNull Consumer<UserId> handler) {
        ContentDb contentDb = ContentDb.getInstance();
        List<MemberInfo> members = contentDb.getGroupMembers(groupId);
        List<UserId> otherMemberIds = new ArrayList<>();
        List<String> namesList = new ArrayList<>();
        for (MemberInfo memberInfo : members) {
            if (!Me.getInstance().getUser().equals(memberInfo.userId.rawId()) && !UserId.ME.equals(memberInfo.userId)) {
                otherMemberIds.add(memberInfo.userId);
                Contact contact = ContactsDb.getInstance().getContact(memberInfo.userId);
                namesList.add(contact.getDisplayName() + " - " + memberInfo.userId.rawId());
            }
        }

        CharSequence[] names = new CharSequence[0];
        activity.runOnUiThread(() -> {
            AlertDialog.Builder memberPicker = new AlertDialog.Builder(activity);
            memberPicker.setTitle("Pick member")
                    .setItems(namesList.toArray(names), (ignored, which) -> {
                        Log.d("Debug selected corruption of " + which + " -> " + otherMemberIds.get(which));
                        bgWorkers.execute(() -> handler.accept(otherMemberIds.get(which)));
                    });
            memberPicker.create().show();
        });
    }

    private static void selectUserFromHome(@NonNull Activity activity, @NonNull Consumer<UserId> handler) {
        List<UserId> userIds = new ArrayList<>();
        List<String> namesList = new ArrayList<>();
        for (Contact contact : ContactsDb.getInstance().getUsers()) {
            userIds.add(contact.userId);
            namesList.add(contact.getDisplayName() + " - " + contact.userId.rawId());
        }

        CharSequence[] names = new CharSequence[0];
        activity.runOnUiThread(() -> {
            AlertDialog.Builder userPicker = new AlertDialog.Builder(activity);
            userPicker.setTitle("Pick user")
                    .setItems(namesList.toArray(names), (ignored, which) -> {
                        Log.d("Debug selected corruption of " + which + " -> " + userIds.get(which));
                        bgWorkers.execute(() -> handler.accept(userIds.get(which)));
                    });
            userPicker.create().show();
        });
    }

    public static void askSendLogsWithId(@NonNull LifecycleOwner lifecycleOwner, @NonNull Context context, @NonNull String contentId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Send logs about ID '" + contentId + "'?");
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
            ProgressDialog progressDialog = ProgressDialog.show(context, null, context.getString(R.string.preparing_logs));
            LogProvider.openLogIntent(context, contentId).observe(lifecycleOwner, intent -> {
                context.startActivity(intent);
                progressDialog.dismiss();
            });
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    private static void restart(Context context) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        Runtime.getRuntime().exit(0);
    }

    static class DeleteContactsDbTask extends AsyncTask<Void, Void, Void> {

        private final Application application;

        DeleteContactsDbTask(@NonNull Application application) {
            this.application = application;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ContactsDb.getInstance().deleteDb();
            restart(application);
            return null;
        }
    }

    static class DeleteContentDbTask extends AsyncTask<Void, Void, Void> {

        private final Application application;

        DeleteContentDbTask(@NonNull Application application) {
            this.application = application;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ContentDb.getInstance().deleteDb();
            FileUtils.deleteRecursive(FileStore.getInstance().getMediaDir());
            FileUtils.deleteRecursive(FileStore.getInstance().getTmpDir());
            restart(application);
            return null;
        }
    }

    static class DeleteGalleryDbTask extends AsyncTask<Void, Void, Void> {

        private final Application application;

        DeleteGalleryDbTask(@NonNull Application application) {
            this.application = application;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ContentDb.getInstance().deleteAllGalleryItems();
            ContentDb.getInstance().deleteAllSuggestions();
            restart(application);
            return null;
        }
    }

    static class ResetRegistrationTask extends AsyncTask<Void, Void, Void> {

        private final Application application;

        ResetRegistrationTask(@NonNull Application application) {
            this.application = application;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Me.getInstance().resetRegistration();
            EncryptedKeyStore.getInstance().clearAll();
            restart(application);
            return null;
        }
    }

    static class TestOnboardingTask extends AsyncTask<Void, Void, Void> {

        private final Application application;

        TestOnboardingTask(@NonNull Application application) {
            this.application = application;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Preferences.getInstance().setForcedZeroZone(true);
            Preferences.getInstance().setCompletedFirstPostOnboarding(false);
            Preferences.getInstance().setLastFullContactSyncTime(0);
            Me.getInstance().resetRegistration();
            EncryptedKeyStore.getInstance().clearAll();
            ContentDb.getInstance().removeHomeFeedZeroZonePost(() -> {
                restart(application);
            });
            return null;
        }
    }

    static class CleanupPostsTask extends AsyncTask<Void, Void, Void> {

        private final Application application;

        CleanupPostsTask(@NonNull Application application) {
            this.application = application;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ContentDb.getInstance().cleanup();
            FileStore.getInstance().cleanup();
            return null;
        }
    }
}
