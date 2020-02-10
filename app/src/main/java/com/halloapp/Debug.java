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
import com.halloapp.media.MediaStore;
import com.halloapp.posts.PostsDb;
import com.halloapp.ui.MainActivity;
import com.halloapp.util.FileUtils;

public class Debug {

    private static final String DEBUG_MENU_RESET_REGISTRATION = "Reset registration";
    private static final String DEBUG_MENU_LOGOUT = "Logout";
    private static final String DEBUG_MENU_DELETE_POSTS_DB = "Delete posts DB";
    private static final String DEBUG_MENU_DELETE_CONTACTS_DB = "Delete contacts DB";
    private static final String DEBUG_MENU_SYNC_CONTACTS = "Sync contacts";
    private static final String DEBUG_MENU_SET_COMMENTS_SEEN = "Set comments seen";
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
                    ContactsSync.getInstance(activity).startContactSync();
                    break;
                }
                case DEBUG_MENU_SET_COMMENTS_SEEN: {
                    PostsDb.getInstance(activity).setCommentsSeen(true);
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
}
