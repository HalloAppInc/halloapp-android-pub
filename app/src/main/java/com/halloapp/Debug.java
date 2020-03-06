package com.halloapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.contacts.UserId;
import com.halloapp.media.MediaStore;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.MainActivity;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;
import com.halloapp.xmpp.Connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Debug {

    private static final String DEBUG_MENU_RESET_REGISTRATION = "Reset registration";
    private static final String DEBUG_MENU_LOGOUT = "Logout";
    private static final String DEBUG_MENU_DELETE_POSTS_DB = "Delete posts DB";
    private static final String DEBUG_MENU_DELETE_CONTACTS_DB = "Delete contacts DB";
    private static final String DEBUG_MENU_SYNC_CONTACTS = "Sync contacts";
    private static final String DEBUG_MENU_SET_COMMENTS_SEEN = "Set comments seen";
    private static final String DEBUG_MENU_SET_INCOMING_POSTS_UNSEEN = "Set incoming posts unseen";
    private static final String DEBUG_MENU_CLEANUP_POSTS = "Cleanup posts";
    private static final String DEBUG_MENU_VISIT_EXPIRATION_ACTIVITY = "Visit expiration activity";
    private static final String DEBUG_MENU_SET_AVATAR = "Set avatar";
    private static final String DEBUG_MENU_SET_COMMENTS_UNSEEN = "Set comments unseen";
    private static final String DEBUG_MENU_DELETE_POST = "Delete post";

    public static void showDebugMenu(@NonNull Activity activity, View anchor) {
        PopupMenu menu = new PopupMenu(activity, anchor);
        menu.getMenu().add(DEBUG_MENU_RESET_REGISTRATION);
        menu.getMenu().add(DEBUG_MENU_LOGOUT);
        menu.getMenu().add(DEBUG_MENU_DELETE_POSTS_DB);
        menu.getMenu().add(DEBUG_MENU_DELETE_CONTACTS_DB);
        menu.getMenu().add(DEBUG_MENU_SYNC_CONTACTS);
        menu.getMenu().add(DEBUG_MENU_SET_COMMENTS_SEEN);
        menu.getMenu().add(DEBUG_MENU_SET_INCOMING_POSTS_UNSEEN);
        menu.getMenu().add(DEBUG_MENU_CLEANUP_POSTS);
        menu.getMenu().add(DEBUG_MENU_VISIT_EXPIRATION_ACTIVITY);
        menu.getMenu().add(DEBUG_MENU_SET_AVATAR);
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
                case DEBUG_MENU_DELETE_POSTS_DB: {
                    new DeletePostsDbTask(activity.getApplication()).execute();
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
                    PostsDb.getInstance(activity).setCommentsSeen(true);
                }
                case DEBUG_MENU_SET_INCOMING_POSTS_UNSEEN: {
                    PostsDb.getInstance(activity).setIncomingPostsSeen(Post.POST_SEEN_NO);
                }
                case DEBUG_MENU_CLEANUP_POSTS: {
                    new CleanupPostsTask(activity.getApplication()).execute();
                    break;
                }
                case DEBUG_MENU_VISIT_EXPIRATION_ACTIVITY: {
                    activity.startActivity(new Intent(activity.getApplicationContext(), AppExpirationActivity.class));
                    break;
                }
                case DEBUG_MENU_SET_AVATAR: {
                    // TODO(jack): Convert testing code into feature code
                    try {
                        Resources res = activity.getResources();
                        InputStream is = res.openRawResource(R.raw.ic_launcher_round);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        MessageDigest md = MessageDigest.getInstance("SHA-1");

                        byte[] buf = new byte[1000];
                        int count;
                        while ((count = is.read(buf)) != -1) {
                            baos.write(buf, 0, count);
                            md.update(buf, 0, count);
                        }

                        byte[] sha1hash = md.digest();
                        StringBuilder hexString = new StringBuilder();
                        for (int i=0; i<sha1hash.length; i++) {
                            hexString.append(Integer.toHexString((sha1hash[i] & 0xFF) | 0x100).substring(1,3));
                        }
                        String hash = hexString.toString();

                        byte[] file = baos.toByteArray();
                        String base64 = Base64.encodeToString(file, Base64.DEFAULT);

                        Connection connection = Connection.getInstance();
                        connection.publishAvatarData(hash, base64);
                        connection.getMyMostRecentAvatarData();
                        connection.publishAvatarMetadata(hash);
                        connection.getMyMostRecentAvatarMetadata();
                    } catch (IOException | NoSuchAlgorithmException e) {
                        Log.e("JACK error", e);
                    }
                }
            }
            return false;
        });
        menu.show();
    }

    public static void showDebugMenu(@NonNull Activity activity, View anchor, UserId userId, String postId) {
        PopupMenu menu = new PopupMenu(activity, anchor);
        menu.getMenu().add(DEBUG_MENU_SET_COMMENTS_UNSEEN);
        menu.getMenu().add(DEBUG_MENU_DELETE_POST);
        menu.setOnMenuItemClickListener(item -> {
            Toast.makeText(activity, item.getTitle(), Toast.LENGTH_SHORT).show();
            switch (item.getTitle().toString()) {
                case DEBUG_MENU_SET_COMMENTS_UNSEEN: {
                    PostsDb.getInstance(activity).setCommentsSeen(userId, postId, false);
                    break;
                }
                case DEBUG_MENU_DELETE_POST: {
                    PostsDb.getInstance(activity).deletePost(userId, postId);
                    activity.finish();
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

    static class DeletePostsDbTask extends AsyncTask<Void, Void, Void> {

        private final Application application;

        DeletePostsDbTask(@NonNull Application application) {
            this.application = application;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            PostsDb.getInstance(application).deleteDb();
            FileUtils.deleteRecursive(MediaStore.getInstance(application).getMediaDir());
            FileUtils.deleteRecursive(MediaStore.getInstance(application).getTmpDir());
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
            PostsDb.getInstance(application).cleanup();
            MediaStore.getInstance(application).cleanup();
            return null;
        }
    }
}
