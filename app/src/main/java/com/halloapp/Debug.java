package com.halloapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.KeyManager;
import com.halloapp.id.ChatId;
import com.halloapp.id.UserId;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.MainActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogProvider;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.WhisperKeysResponseIq;

import java.io.File;
import java.util.Collections;

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
                    ContactsSync.getInstance(activity).startContactsSync(true);
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
                            false,
                            false,
                            "TESTING COMMENT DUP"
                    );
                    comment.setParentPost(post);

                    ContentDb contentDb = ContentDb.getInstance();
                    contentDb.addPost(post);
                    contentDb.addComment(comment);
                    contentDb.addComment(comment);
                }
                case DEBUG_MENU_CLEAR_LOGS: {
                    bgWorkers.execute(() -> FileStore.getInstance().purgeAllLogFiles());
                    break;
                }
                case DEBUG_MENU_RUN_DAILY_WORKER: {
                    DailyWorker.scheduleDebug(AppContext.getInstance().get());
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
            menu.setOnMenuItemClickListener(item -> {
                SnackbarHelper.showInfo(activity, item.getTitle());
                //noinspection SwitchStatementWithTooFewBranches
                switch (item.getTitle().toString()) {
                    case DEBUG_MENU_SKIP_OUTBOUND_MESSAGE_KEY: {
                        try {
                            KeyManager.getInstance().getNextOutboundMessageKey((UserId)chatId);
                        } catch (Exception e) {
                            Log.w("DEBUG error skipping outbound message key", e);
                        }
                        break;
                    }
                }
                return false;
            });
        }
        menu.show();
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
