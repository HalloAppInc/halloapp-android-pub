package com.halloapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.contacts.UserId;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.crypto.keys.EncryptedKeyStore;
import com.halloapp.crypto.keys.KeyManager;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.MainActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;
import com.halloapp.util.StringUtils;
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
    private static final String DEBUG_MENU_TEST_KEYS = "Test keys";
    private static final String DEBUG_MENU_CLEAR_KEY_STORE = "Clear key store";
    private static final String DEBUG_MENU_SET_COMMENTS_UNSEEN = "Set comments unseen";
    private static final String DEBUG_MENU_SKIP_OUTBOUND_MESSAGE_KEY = "Skip outbound message key";

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
        menu.getMenu().add(DEBUG_MENU_TEST_KEYS);
        menu.getMenu().add(DEBUG_MENU_CLEAR_KEY_STORE);
        menu.setOnMenuItemClickListener(item -> {
            Toast.makeText(activity, item.getTitle(), Toast.LENGTH_SHORT).show();
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
                    ContentDb.getInstance(activity).setCommentsSeen(true);
                }
                case DEBUG_MENU_SET_INCOMING_POSTS_UNSEEN: {
                    ContentDb.getInstance(activity).setIncomingPostsSeen(Post.SEEN_NO);
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
                            AvatarLoader avatarLoader = AvatarLoader.getInstance(Connection.getInstance(), activity);
                            avatarLoader.removeMyAvatar();
                            return null;
                        }
                    }.execute();

                    break;
                }
                case DEBUG_MENU_TEST_KEYS: {
                    try {
                        Connection.getInstance().uploadKeys(new byte[]{1, 2, 3, 4}, new byte[]{5, 6, 7, 8}, Collections.singletonList(new byte[]{10, 11, 12, 13}));
                        WhisperKeysResponseIq response = Connection.getInstance().downloadKeys(new UserId("14075553501@s.halloapp.net")).get();
                        Log.d("DEBUG response " + response + " identity key: " + StringUtils.bytesToHexString(response.identityKey));
                        Connection.getInstance().uploadMoreOneTimePreKeys(Collections.singletonList(new byte[]{3, 3, 3, 3}));
                        response = Connection.getInstance().downloadKeys(new UserId("14075553501@s.halloapp.net")).get();
                        Log.d("DEBUG response " + response + " identity key: " + StringUtils.bytesToHexString(response.identityKey));
                        Log.d("DEBUG COUNT: " + Connection.getInstance().getOneTimeKeyCount().get());
                    } catch (Exception e) {
                        Log.w("DEBUG key failure", e);
                    }
                    break;
                }
                case DEBUG_MENU_CLEAR_KEY_STORE: {
                    AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> {
                        try {
                            EncryptedKeyStore.getInstance().clearAll();
                        } catch (Exception e) {
                            Log.w("DEBUG failed to clear key store");
                        }
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
            Toast.makeText(activity, item.getTitle(), Toast.LENGTH_SHORT).show();
            switch (item.getTitle().toString()) {
                case DEBUG_MENU_SET_COMMENTS_UNSEEN: {
                    ContentDb.getInstance(activity).setCommentsSeen(userId, postId, false);
                    break;
                }
            }
            return false;
        });
        menu.show();

    }

    public static void showDebugMenu(@NonNull Activity activity, View anchor, UserId peerUserId) {
        PopupMenu menu = new PopupMenu(activity, anchor);
        menu.getMenu().add(DEBUG_MENU_SKIP_OUTBOUND_MESSAGE_KEY);
        menu.setOnMenuItemClickListener(item -> {
            Toast.makeText(activity, item.getTitle(), Toast.LENGTH_SHORT).show();
            switch (item.getTitle().toString()) {
                case DEBUG_MENU_SKIP_OUTBOUND_MESSAGE_KEY: {
                    try {
                        KeyManager.getInstance().getNextOutboundMessageKey(peerUserId);
                    } catch (Exception e) {
                        Log.w("DEBUG error skipping outbound message key", e);
                    }
                    break;
                }
            }
            return false;
        });
        menu.show();
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
            ContactsDb.getInstance(application).deleteDb();
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
            ContentDb.getInstance(application).deleteDb();
            FileUtils.deleteRecursive(FileStore.getInstance(application).getMediaDir());
            FileUtils.deleteRecursive(FileStore.getInstance(application).getTmpDir());
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
            Me.getInstance(application).resetRegistration();
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
            ContentDb.getInstance(application).cleanup();
            FileStore.getInstance(application).cleanup();
            return null;
        }
    }
}
