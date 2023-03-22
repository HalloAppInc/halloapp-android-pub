package com.halloapp.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.proto.clients.ContactAddress;
import com.halloapp.proto.clients.ContactEmail;
import com.halloapp.proto.clients.ContactPhone;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class IntentUtils {

    private static final String FB_MESSENGER_PACKAGE = "com.facebook.orca";
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";

    public static Intent createShareDlIntent(@NonNull Context context) {
        return createShareTextIntent(context.getString(R.string.share_halloapp_text));
    }

    public static Intent createShareTextIntent(@NonNull String url) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");

        return Intent.createChooser(sendIntent, null);
    }

    public static Intent createSmsIntent(@NonNull String phoneNumber, @Nullable String text) {
        Preconditions.checkNotNull(phoneNumber);
        Uri smsUri = Uri.parse("smsto:" + phoneNumber);
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
        smsIntent.putExtra(Intent.EXTRA_TEXT, text);
        smsIntent.putExtra("sms_body", text);
        smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return smsIntent;
    }

    public static Intent createWhatsAppIntent(@NonNull String phoneNumber, @Nullable String text, boolean forBusiness) {
        Preconditions.checkNotNull(phoneNumber);
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

    public static Intent createContactIntent(@Nullable Contact contact, @Nullable String phone) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

        if (contact != null) {
            intent.putExtra(ContactsContract.Intents.Insert.NAME, TextUtils.isEmpty(contact.halloName) ? contact.fallbackName : contact.halloName);
        }
        if (phone != null) {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
        }

        return intent;
    }

    public static void openOurWebsiteInBrowser(@NonNull View view, @NonNull String suffix) {
        String url = Constants.WEBSITE_BASE_URL;
        String language = Locale.getDefault().getLanguage();
        // See https://developer.android.com/reference/java/util/Locale#getLanguage() for why cannot directly look up string
        for (String translatedLang : Constants.WEBSITE_TRANSLATIONS) {
            if (language.equals(new Locale(translatedLang).getLanguage())) {
                url += translatedLang + "/";
                break;
            }
        }
        url += suffix;
        openUrlInBrowser(view, url);
    }

    public static void openOurWebsiteInBrowser(@NonNull Activity activity, @NonNull String suffix) {
        String url = Constants.WEBSITE_BASE_URL;
        String language = Locale.getDefault().getLanguage();
        // See https://developer.android.com/reference/java/util/Locale#getLanguage() for why cannot directly look up string
        for (String translatedLang : Constants.WEBSITE_TRANSLATIONS) {
            if (language.equals(new Locale(translatedLang).getLanguage())) {
                url += translatedLang + "/";
                break;
            }
        }
        url += suffix;
        openUrlInBrowser(activity, url);
    }

    public static void openUrlInBrowser(@NonNull View view, @NonNull String url) {
        url = addMissingScheme(url);

        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            view.getContext().startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            SnackbarHelper.showWarning(view, R.string.failed_to_open_link);
            Log.e("IntentUtils/openUrlInBrowser failed to open url " + url);
        }
    }

    public static void openUrlInBrowser(@NonNull Activity activity, @NonNull String url) {
        url = addMissingScheme(url);

        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            SnackbarHelper.showWarning(activity, R.string.failed_to_open_link);
            Log.e("IntentUtils/openUrlInBrowser failed to open url " + url);
        }
    }

    private static String addMissingScheme(String url) {
        if (Uri.parse(url).getScheme() == null) {
            return "http://" + url;
        }

        return url;
    }

    public static void showAddContactDialog(@NonNull Context context, @NonNull com.halloapp.proto.clients.Contact contact) {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(context);
        alertDialogBuilder.setMessage(R.string.add_contact_dialog_message);
        alertDialogBuilder.setPositiveButton(R.string.new_contact, (dialog, which) -> {
            context.startActivity(addNewContact(contact));
        });
        alertDialogBuilder.setNegativeButton(R.string.existing_contact, (dialog, which) -> {
            context.startActivity(editContact(contact));
        });
        alertDialogBuilder.create().show();
    }

    public static Intent addNewContact(@NonNull com.halloapp.proto.clients.Contact contact) {
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        addContactToContactIntent(contact, intent);
        return intent;
    }

    public static Intent editContact(@NonNull com.halloapp.proto.clients.Contact contact) {
        Intent intentInsertEdit = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intentInsertEdit.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        addContactToContactIntent(contact, intentInsertEdit);

        return intentInsertEdit;
    }

    private static void addContactToContactIntent(com.halloapp.proto.clients.Contact contact, Intent intent) {
        intent.putExtra(ContactsContract.Intents.Insert.NAME, contact.getName());
        for (ContactPhone telephone : contact.getNumbersList()) {
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, telephone.getNumber());
            intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactCardUtils.convertTelephoneTypeToAndroid(telephone.getLabel()));
            break;
        }
        for (ContactEmail email : contact.getEmailsList()) {
            intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email.getAddress());
            break;
        }
        for (ContactAddress address : contact.getAddressesList()) {
            intent.putExtra(ContactsContract.Intents.Insert.POSTAL, address.getAddress());
            break;
        }
    }

    public static void openPlayOrMarket(@NonNull Activity activity) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(Constants.PLAY_STORE_URL));
            intent.setPackage("com.android.vending");
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.i("Play Store Not Installed", e);
            try {
                final Intent intent = Intent.parseUri("market://details?id=" + BuildConfig.APPLICATION_ID, 0);
                activity.startActivity(intent);
            } catch (URISyntaxException | ActivityNotFoundException e2) {
                Log.w("Failed to open any market", e2);
                SnackbarHelper.showWarning(activity, R.string.no_market_found);
            }
        }
    }

    public static void openPlayOrMarket(@NonNull View view) {
        try {
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(Constants.PLAY_STORE_URL));
            intent.setPackage("com.android.vending");
            view.getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.i("Play Store Not Installed", e);
            try {
                final Intent intent = Intent.parseUri("market://details?id=" + BuildConfig.APPLICATION_ID, 0);
                view.getContext().startActivity(intent);
            } catch (URISyntaxException | ActivityNotFoundException e2) {
                Log.w("Failed to open any market", e2);
                SnackbarHelper.showWarning(view, R.string.no_market_found);
            }
        }
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
        Preconditions.checkNotNull(title);
        String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context);
        boolean fbIsDefaultSms = FB_MESSENGER_PACKAGE.equals(defaultSmsPackage);

        Intent smsIntent = createSmsIntent(phoneNumber, text);
        Intent waIntent = createWhatsAppIntent(phoneNumber, text, false);
        Intent w4bIntent = createWhatsAppIntent(phoneNumber, text, true);

        Intent chooser;
        ArrayList<Intent> extraIntents = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 24) {
            chooser = Intent.createChooser(smsIntent, title);
            chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, new ComponentName[] {
                    new ComponentName(WHATSAPP_PACKAGE, "com.whatsapp.Conversation"),
                    new ComponentName(WHATSAPP_BUSINESS_PACKAGE, "com.whatsapp.Conversation"),
                    new ComponentName(fbIsDefaultSms ? "" : FB_MESSENGER_PACKAGE, "com.facebook.messaging.sms.defaultapp.ComposeSmsActivity")});
            extraIntents.add(waIntent);
            extraIntents.add(w4bIntent);
        } else {
            List<ResolveInfo> resInfos = context.getPackageManager().queryIntentActivities(smsIntent, 0);
            for (ResolveInfo resolveInfo : resInfos) {
                String packageName = resolveInfo.activityInfo.packageName;
                if (!packageName.toLowerCase(Locale.ROOT).contains("whatsapp") && (fbIsDefaultSms || !packageName.toLowerCase(Locale.ROOT).contains("facebook"))) {
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

    public static Intent createPhotoPickerIntent(boolean allowMultiple) {
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        final String[] mimeTypes = new String[]{"image/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        return intent;
    }
}
