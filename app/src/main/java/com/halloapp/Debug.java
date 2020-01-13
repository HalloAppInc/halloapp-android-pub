package com.halloapp;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class Debug {

    private static final String DEBUG_MENU_RESET_REGISTRATION = "Reset registration";
    private static final String DEBUG_MENU_LOGOUT = "Logout";
    private static final String DEBUG_MENU_DELETE_POSTS_DB = "Delete posts DB";
    private static final String DEBUG_MENU_DELETE_CONTACTS_DB = "Delete contacts DB";

    static void showDebugMenu(@NonNull Context context, View anchor) {
        PopupMenu menu = new PopupMenu(context, anchor);
        menu.getMenu().add(DEBUG_MENU_RESET_REGISTRATION);
        menu.getMenu().add(DEBUG_MENU_LOGOUT);
        menu.getMenu().add(DEBUG_MENU_DELETE_POSTS_DB);
        menu.getMenu().add(DEBUG_MENU_DELETE_CONTACTS_DB);
        menu.setOnMenuItemClickListener(item -> {
            Toast.makeText(context, item.getTitle(), Toast.LENGTH_SHORT).show();
            switch (item.getTitle().toString()) {
                case DEBUG_MENU_RESET_REGISTRATION: {
                    HalloApp.instance.resetRegistration();
                    restart(context);
                    break;
                }
                case DEBUG_MENU_LOGOUT: {
                    HalloApp.instance.disconnect();
                    break;
                }
                case DEBUG_MENU_DELETE_POSTS_DB: {
                    break;
                }
                case DEBUG_MENU_DELETE_CONTACTS_DB: {
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
