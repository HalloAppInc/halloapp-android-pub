package com.halloapp;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.halloapp.contacts.Contact;
import com.halloapp.props.ServerProps;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.invites.InvitesResponseIq;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.Locale;

public class InviteHelper {
    public static void sendInviteExternal(@NonNull Context context, @NonNull Contact contact) {
        Intent chooser = IntentUtils.createSmsChooserIntent(context, context.getString(R.string.invite_friend_chooser_title, contact.getShortName()), Preconditions.checkNotNull(contact.normalizedPhone), getInviteText(context, contact));
        context.startActivity(chooser);
    }

    public static String getInviteText(Context context, @NonNull Contact contact) {
        String remoteInviteStrings = ServerProps.getInstance().getInviteStrings();
        if (!TextUtils.isEmpty(remoteInviteStrings)) {
            try {
                JSONObject jsonObject = new JSONObject(remoteInviteStrings);

                // See https://developer.android.com/reference/java/util/Locale#getLanguage() for why cannot directly look up string
                String language = Locale.getDefault().getLanguage();
                for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                    String key = it.next();
                    if (language.equals(new Locale(key).getLanguage())) {
                        String selected = jsonObject.getString(key);
                        try {
                            return String.format(selected.replace('@', 's'), contact.getShortName(), contact.getDisplayPhone());
                        } catch (IllegalFormatException e) {
                            Log.e("Failed to format invite string", e);
                            Log.sendErrorReport("Bad invite format string");
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("Failed to parse invite strings json", e);
                Log.sendErrorReport("Bad invite json");
            }
        }
        return context.getString(R.string.invite_text_with_name_and_number, contact.getShortName(), contact.getDisplayPhone(), Constants.DOWNLOAD_LINK_URL);
    }

    public static void showInviteIqErrorDialog(@NonNull Context context, @Nullable @InvitesResponseIq.Result Integer result) {
        @StringRes int errorMessageRes;
        if (result == null) {
            errorMessageRes = R.string.invite_failed_internet;
        } else {
            switch (result) {
                case InvitesResponseIq.Result.EXISTING_USER:
                    errorMessageRes = R.string.invite_failed_existing_user;
                    break;
                case InvitesResponseIq.Result.INVALID_NUMBER:
                    errorMessageRes = R.string.invite_failed_invalid_number;
                    break;
                case InvitesResponseIq.Result.NO_INVITES_LEFT:
                    errorMessageRes = R.string.invite_failed_no_invites;
                    break;
                case InvitesResponseIq.Result.NO_ACCOUNT:
                case InvitesResponseIq.Result.UNKNOWN:
                default:
                    errorMessageRes = R.string.invite_failed_unknown;
                    break;
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(context).setMessage(errorMessageRes).setPositiveButton(R.string.ok, null).create();
        dialog.show();
    }
}
