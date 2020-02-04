package com.halloapp;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.PostsDb;
import com.halloapp.ui.MainActivity;

public class Debug {

    private static final String DEBUG_MENU_RESET_REGISTRATION = "Reset registration";
    private static final String DEBUG_MENU_LOGOUT = "Logout";
    private static final String DEBUG_MENU_DELETE_POSTS_DB = "Delete posts DB";
    private static final String DEBUG_MENU_DELETE_CONTACTS_DB = "Delete contacts DB";
    private static final String DEBUG_MENU_SYNC_CONTACTS = "Sync contacts";
    private static final String DEBUG_MENU_SET_COMMENTS_SEEN = "Set comments seen";
    private static final String DEBUG_MENU_SET_COMMENTS_UNSEEN = "Set comments unseen";

    public static void showDebugMenu(@NonNull Context context, View anchor) {
        PopupMenu menu = new PopupMenu(context, anchor);
        menu.getMenu().add(DEBUG_MENU_RESET_REGISTRATION);
        menu.getMenu().add(DEBUG_MENU_LOGOUT);
        menu.getMenu().add(DEBUG_MENU_DELETE_POSTS_DB);
        menu.getMenu().add(DEBUG_MENU_DELETE_CONTACTS_DB);
        menu.getMenu().add(DEBUG_MENU_SYNC_CONTACTS);
        menu.getMenu().add(DEBUG_MENU_SET_COMMENTS_SEEN);
        menu.setOnMenuItemClickListener(item -> {
            Toast.makeText(context, item.getTitle(), Toast.LENGTH_SHORT).show();
            switch (item.getTitle().toString()) {
                case DEBUG_MENU_RESET_REGISTRATION: {
                    Preferences.getInstance(context).resetRegistration();
                    restart(context);
                    break;
                }
                case DEBUG_MENU_LOGOUT: {
                    Connection.getInstance().disconnect();
                    break;
                }
                case DEBUG_MENU_DELETE_POSTS_DB: {
                    PostsDb.getInstance(context).deleteDb();
                    restart(context);
                    break;
                }
                case DEBUG_MENU_DELETE_CONTACTS_DB: {
                    ContactsDb.getInstance(context).deleteDb();
                    restart(context);
                    break;
                }
                case DEBUG_MENU_SYNC_CONTACTS: {
                    ContactsSync.getInstance(context).startContactSync();
                    break;
                }
                case DEBUG_MENU_SET_COMMENTS_SEEN: {
                    PostsDb.getInstance(context).setCommentsSeen(true);
                }
            }
            return false;
        });
        menu.show();
    }

    public static void showDebugMenu(@NonNull Context context, View anchor, UserId userId, String postId) {
        PopupMenu menu = new PopupMenu(context, anchor);
        menu.getMenu().add(DEBUG_MENU_SET_COMMENTS_UNSEEN);
        menu.setOnMenuItemClickListener(item -> {
            Toast.makeText(context, item.getTitle(), Toast.LENGTH_SHORT).show();
            switch (item.getTitle().toString()) {
                case DEBUG_MENU_SET_COMMENTS_UNSEEN: {
                    PostsDb.getInstance(context).setCommentsSeen(userId, postId, false);
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
}
