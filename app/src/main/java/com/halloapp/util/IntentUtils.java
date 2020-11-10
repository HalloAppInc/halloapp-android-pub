package com.halloapp.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class IntentUtils {

    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";

    public static Intent createSmsIntent(@NonNull String phoneNumber, @Nullable String text) {
        Uri smsUri = Uri.parse("smsto:" + phoneNumber);
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
        smsIntent.putExtra(Intent.EXTRA_TEXT, text);
        smsIntent.putExtra("sms_body", text);
        smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return smsIntent;
    }

    public static Intent createWhatsAppIntent(@NonNull String phoneNumber, @Nullable String text, boolean forBusiness) {
        Uri.Builder builder = Uri.parse("https://wa.me/" + phoneNumber).buildUpon();
        if (text != null) {
            builder.appendQueryParameter("text", text);
        }
        Intent whatsAppIntent = new Intent(Intent.ACTION_VIEW, builder.build());
        whatsAppIntent.setPackage(forBusiness ? WHATSAPP_BUSINESS_PACKAGE : WHATSAPP_PACKAGE);
        whatsAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return whatsAppIntent;
    }

    // Create a chooser intent for app's that rely on phone numbers to identify people
    public static Intent createSmsChooserIntent(@NonNull Context context, @NonNull String title, @NonNull String phoneNumber, @Nullable String text) {
        Intent smsIntent = createSmsIntent(phoneNumber, text);
        Intent waIntent = createWhatsAppIntent(phoneNumber, text, false);
        Intent w4bIntent = createWhatsAppIntent(phoneNumber, text, true);
        Intent chooser = null;
        ArrayList<Intent> extraIntents = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 24) {
            chooser = Intent.createChooser(smsIntent, title);
            chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, new ComponentName[] {
                    new ComponentName(WHATSAPP_PACKAGE, "com.whatsapp.Conversation"),
                    new ComponentName(WHATSAPP_BUSINESS_PACKAGE, "com.whatsapp.Conversation")});
            extraIntents.add(waIntent);
            extraIntents.add(w4bIntent);
        } else {
            List<ResolveInfo> resInfos = context.getPackageManager().queryIntentActivities(smsIntent, 0);
            for (ResolveInfo resolveInfo : resInfos) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (!packageName.toLowerCase().contains("whatsapp")) {
                    Intent intent = new Intent(smsIntent);
                    intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
                    intent.setPackage(packageName);
                    extraIntents.add(intent);
                }
            }
            Intent initialIntent;
            if (extraIntents.isEmpty()) {
                initialIntent = waIntent;
            } else {
                initialIntent = extraIntents.remove(0);
                extraIntents.add(waIntent);
                extraIntents.add(w4bIntent);
            }
            chooser = Intent.createChooser(initialIntent, title);
        }
        if (!extraIntents.isEmpty()) {
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents.toArray(new Intent[0]));
        }
        return chooser;
    }
}
