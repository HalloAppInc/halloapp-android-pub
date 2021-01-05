package com.halloapp.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.util.logs.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IntentUtils {

    private static final String FB_MESSENGER_PACKAGE = "com.facebook.orca";
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

    public static Intent createFbMessengerIntent(@Nullable String text) {
        Intent fbIntent = new Intent(Intent.ACTION_SEND);
        fbIntent.putExtra(Intent.EXTRA_TEXT, text);
        fbIntent.setType("text/plain");
        fbIntent.setClassName(FB_MESSENGER_PACKAGE, "com.facebook.messenger.intents.ShareIntentHandler");
        fbIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return fbIntent;
    }

    private static boolean isEnabled(@NonNull Context context, @Nullable String packageName) {
        if (packageName == null) {
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // Create a chooser intent for app's that rely on phone numbers to identify people
    public static Intent createSmsChooserIntent(@NonNull Context context, @NonNull String title, @NonNull String phoneNumber, @Nullable String text) {
        String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context);
        boolean fbIsDefaultSms = FB_MESSENGER_PACKAGE.equals(defaultSmsPackage);

        Intent smsIntent = createSmsIntent(phoneNumber, text);
        Intent fbIntent = createFbMessengerIntent(text);
        Intent waIntent = createWhatsAppIntent(phoneNumber, text, false);
        Intent w4bIntent = createWhatsAppIntent(phoneNumber, text, true);

        Intent chooser = null;
        ArrayList<Intent> extraIntents = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 24) {
            chooser = Intent.createChooser(smsIntent, title);
            chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, new ComponentName[] {
                    new ComponentName(WHATSAPP_PACKAGE, "com.whatsapp.Conversation"),
                    new ComponentName(WHATSAPP_BUSINESS_PACKAGE, "com.whatsapp.Conversation"),
                    new ComponentName(fbIsDefaultSms ? "" : FB_MESSENGER_PACKAGE, "com.facebook.messaging.sms.defaultapp.ComposeSmsActivity")});
            if (!fbIsDefaultSms) {
                extraIntents.add(fbIntent);
            }
            extraIntents.add(waIntent);
            extraIntents.add(w4bIntent);
        } else {
            List<ResolveInfo> resInfos = context.getPackageManager().queryIntentActivities(smsIntent, 0);
            for (ResolveInfo resolveInfo : resInfos) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (!packageName.toLowerCase().contains("whatsapp") && (fbIsDefaultSms || !packageName.toLowerCase().contains("facebook"))) {
                    Intent intent = new Intent(smsIntent);
                    intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
                    intent.setPackage(packageName);
                    extraIntents.add(intent);
                }
            }
            if (!fbIsDefaultSms) {
                extraIntents.add(fbIntent);
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
            if (extraIntents.size() > 2 && Build.VERSION.SDK_INT >= 28) {
                // Looks like EXTRA_INITIAL_INTENTS was limited to 3 in API 28 and 2 in 30. See link below for limit in master of AOSP as of Jan 2021
                // https://github.com/aosp-mirror/platform_frameworks_base/blob/b6ac2afd21164d38da40330d16c9909865ef70da/core/java/com/android/internal/app/ChooserActivity.java#L282
                Log.w("Got " + extraIntents.size() + " > 2 extra intents; some may be ommitted");
            }

            PackageManager packageManager = context.getPackageManager();
            Collator collator = Collator.getInstance();

            Collections.sort(extraIntents, new Comparator<Intent>() {
                @Override
                public int compare(Intent o1, Intent o2) {
                    if (isEnabled(context, o1.getPackage()) && !isEnabled(context, o2.getPackage())) {
                        return -1;
                    } else if (isEnabled(context, o2.getPackage()) && !isEnabled(context, o1.getPackage())) {
                        return 1;
                    }

                    try {
                        PackageInfo first = packageManager.getPackageInfo(o1.getPackage(), 0);
                        PackageInfo second = packageManager.getPackageInfo(o2.getPackage(), 0);
                        String fn = packageManager.getApplicationLabel(first.applicationInfo).toString();
                        String sn = packageManager.getApplicationLabel(second.applicationInfo).toString();
                        return collator.compare(fn, sn);
                    } catch (PackageManager.NameNotFoundException e) {
                        // ignore
                    }

                    return 0;
                }
            });
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents.toArray(new Intent[0]));
        }
        return chooser;
    }
}
