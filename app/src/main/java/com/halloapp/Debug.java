package com.halloapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;

import com.google.crypto.tink.subtle.Random;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
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
import com.halloapp.props.ServerProps;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.MainActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogProvider;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("VisibleForTests")
public class Debug {

    private static final String DEBUG_MENU_RESET_REGISTRATION = "Reset registration";
    private static final String DEBUG_MENU_LOGOUT = "Logout";
    private static final String DEBUG_MENU_DELETE_CONTENT_DB = "Delete posts DB";
    private static final String DEBUG_MENU_DELETE_CONTACTS_DB = "Delete contacts DB";
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
    private static final String DEBUG_MENU_SKIP_OUTBOUND_GROUP_FEED_KEY = "Skip outbound key";
    private static final String DEBUG_MENU_SKIP_INBOUND_GROUP_FEED_KEY = "Skip inbound key";
    private static final String DEBUG_MENU_CORRUPT_GROUP_KEY_STORE = "Corrupt group key store";

    private static final BgWorkers bgWorkers = BgWorkers.getInstance();

    public static void showDebugMenu(@NonNull Activity activity, View anchor) {
        PopupMenu menu = new PopupMenu(activity, anchor);
        menu.getMenu().add(DEBUG_MENU_RESET_REGISTRATION);
        menu.getMenu().add(DEBUG_MENU_LOGOUT);
        menu.getMenu().add(DEBUG_MENU_DELETE_CONTENT_DB);
        menu.getMenu().add(DEBUG_MENU_DELETE_CONTACTS_DB);
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
        menu.getMenu().add(DEBUG_MENU_NORMAL_USER_MODE);
        menu.getMenu().add(DEBUG_MENU_ADD_TO_ARCHIVE);
        menu.getMenu().add(DEBUG_MENU_REMOVE_ARCHIVE);
        menu.setOnMenuItemClickListener(item -> {
            SnackbarHelper.showInfo(activity, item.getTitle());
            switch (item.getTitle().toString()) {
                case DEBUG_MENU_RESET_REGISTRATION: {
                    new ResetRegistrationTask(activity.getApplication()).execute();
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
            }
            return false;
        });
        menu.show();
    }

    public static void showDebugMenu(@NonNull Activity activity, View anchor, UserId userId, String postId) {
        PopupMenu menu = new PopupMenu(activity, anchor);
        menu.getMenu().add(DEBUG_MENU_SET_COMMENTS_UNSEEN);
        menu.setOnMenuItemClickListener(item -> {
            SnackbarHelper.showInfo(activity, item.getTitle());
            //noinspection SwitchStatementWithTooFewBranches
            switch (item.getTitle().toString()) {
                case DEBUG_MENU_SET_COMMENTS_UNSEEN: {
                    ContentDb.getInstance().setCommentsSeen(postId, false);
                    break;
                }
            }
            return false;
        });
        menu.show();

    }

    public static void showDebugMenu(@NonNull Activity activity, View anchor, ChatId chatId) {
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
        menu.setOnMenuItemClickListener(item -> {
            SnackbarHelper.showInfo(activity, item.getTitle());
            switch (item.getTitle().toString()) {
                case DEBUG_MENU_SKIP_OUTBOUND_GROUP_FEED_KEY: {
                    bgWorkers.execute(() -> {
                        try {
                            GroupFeedKeyManager.getInstance().getNextOutboundMessageKey(groupId);
                        } catch (Exception e) {
                            Log.w("DEBUG error skipping outbound group key", e);
                        }
                    });
                    break;
                }
                case DEBUG_MENU_SKIP_INBOUND_GROUP_FEED_KEY: {
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
                                                e.printStackTrace();
                                            }
                                        });
                                    });
                            selectUserBuilder.create().show();
                        });
                    });
                    break;
                }
                case DEBUG_MENU_CORRUPT_GROUP_KEY_STORE: {
                    showCorruptGroupKeyStoreDialog(activity, groupId);
                    break;
                }
            }
            return false;
        });
        menu.show();
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
                () -> encryptedKeyStore.setSessionAlreadySetUp(peerUserId, false),
                () -> encryptedKeyStore.setPeerResponded(peerUserId, false),
                () -> encryptedKeyStore.clearSkippedMessageKeys(peerUserId),
                () -> encryptedKeyStore.clearPeerPublicIdentityKey(peerUserId),
                () -> encryptedKeyStore.setPeerPublicIdentityKey(peerUserId, new PublicEdECKey(Random.randBytes(KEY_SIZE))),
                () -> encryptedKeyStore.clearPeerSignedPreKey(peerUserId),
                () -> encryptedKeyStore.clearPeerOneTimePreKey(peerUserId),
                () -> encryptedKeyStore.clearPeerOneTimePreKeyId(peerUserId),
                () -> encryptedKeyStore.setRootKey(peerUserId, Random.randBytes(KEY_SIZE)),
                () -> encryptedKeyStore.setOutboundChainKey(peerUserId, Random.randBytes(KEY_SIZE)),
                () -> encryptedKeyStore.setInboundChainKey(peerUserId, Random.randBytes(KEY_SIZE)),
                () -> {
                    try {
                        encryptedKeyStore.setOutboundEphemeralKey(peerUserId, new PrivateXECKey(Random.randBytes(KEY_SIZE)));
                    } catch (CryptoException e) {
                        e.printStackTrace();
                    }
                },
                () -> {
                    try {
                        encryptedKeyStore.setInboundEphemeralKey(peerUserId, new PublicXECKey(Random.randBytes(KEY_SIZE)));
                    } catch (CryptoException e) {
                        e.printStackTrace();
                    }
                },
                () -> encryptedKeyStore.setOutboundEphemeralKeyId(peerUserId, Random.randInt(MAX_NUM)),
                () -> encryptedKeyStore.setInboundEphemeralKeyId(peerUserId, Random.randInt(MAX_NUM)),
                () -> encryptedKeyStore.setOutboundPreviousChainLength(peerUserId, Random.randInt(MAX_NUM)),
                () -> encryptedKeyStore.setInboundPreviousChainLength(peerUserId, Random.randInt(MAX_NUM)),
                () -> encryptedKeyStore.setOutboundCurrentChainIndex(peerUserId, Random.randInt(MAX_NUM)),
                () -> encryptedKeyStore.setInboundCurrentChainIndex(peerUserId, Random.randInt(MAX_NUM)),
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

        final int KEY_SIZE = 32;
        final int MAX_NUM = 500;
        Runnable[] corruptionActions = {
                () -> encryptedKeyStore.clearGroupSendAlreadySetUp(groupId),
                () -> selectUserFromGroup(activity, groupId, userId -> encryptedKeyStore.clearSkippedGroupFeedKeys(groupId, userId)),
                () -> encryptedKeyStore.clearMyGroupSigningKey(groupId),
                () -> encryptedKeyStore.setMyGroupSigningKey(groupId, new PrivateEdECKey(Random.randBytes(KEY_SIZE))),
                () -> selectUserFromGroup(activity, groupId, userId -> encryptedKeyStore.clearPeerGroupSigningKey(groupId, userId)),
                () -> selectUserFromGroup(activity, groupId, userId -> encryptedKeyStore.setPeerGroupSigningKey(groupId, userId, new PublicEdECKey(Random.randBytes(KEY_SIZE)))),
                () -> encryptedKeyStore.setMyGroupChainKey(groupId, Random.randBytes(KEY_SIZE)),
                () -> selectUserFromGroup(activity, groupId, userId -> encryptedKeyStore.setPeerGroupChainKey(groupId, userId, Random.randBytes(KEY_SIZE))),
                () -> encryptedKeyStore.setMyGroupCurrentChainIndex(groupId, Random.randInt(MAX_NUM)),
                () -> selectUserFromGroup(activity, groupId, userId -> encryptedKeyStore.setPeerGroupCurrentChainIndex(groupId, userId, Random.randInt(MAX_NUM))),
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
                namesList.add(memberInfo.userId.rawId());
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

    public static void askSendLogsWithId(@NonNull Context context, @NonNull String contentId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Send logs about ID '" + contentId + "'?");
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> LogProvider.openDebugLogcatIntent(context, contentId));
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
