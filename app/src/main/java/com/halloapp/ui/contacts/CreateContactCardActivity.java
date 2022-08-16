package com.halloapp.ui.contacts;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.protobuf.ByteString;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.ContactAddress;
import com.halloapp.proto.clients.ContactCard;
import com.halloapp.proto.clients.ContactEmail;
import com.halloapp.proto.clients.ContactPhone;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.DeviceAvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ContactCardUtils;
import com.halloapp.util.logs.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CreateContactCardActivity extends HalloActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String EXTRA_CONTACT_CARD = "contact_card";

    private static final int DETAILS_QUERY_CONTACT_ID = 0;
    private static final int DETAILS_QUERY_CONTACT_EMAIL = 1;
    private static final int DETAILS_QUERY_CONTACT_ADDRESS = 2;

    private static final String ARG_LOADER_PHONE_ROW_ID = "phone_row_id";
    private static final String ARG_LOADER_CONTACT_ID = "contact_id";

    private static final String EXTRA_CONTACT_ID = "contact_id";
    private static final String EXTRA_USER_ID = "user_id";

    private static final String SELECTION = ContactsContract.Data.LOOKUP_KEY + " = ?";

    public static Intent shareContact(Context context, Contact contact) {
        Intent i = new Intent(context, CreateContactCardActivity.class);
        i.putExtra(EXTRA_CONTACT_ID, "" + contact.getAddressBookId());
        if (contact.userId != null) {
            i.putExtra(EXTRA_USER_ID, contact.userId);
        }
        return i;

    }

    private com.halloapp.proto.clients.Contact.Builder contactBuilder = com.halloapp.proto.clients.Contact.newBuilder();

    private static final String SORT_ORDER = ContactsContract.Data.MIMETYPE;

    private static final String[] ID_LOOKUP_PROJECTION = {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
    };

    private static final String[] EMAIL_LOOKUP_PROJECTION = {
            ContactsContract.CommonDataKinds.Email._ID,
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.TYPE,
            ContactsContract.CommonDataKinds.Email.LABEL,
    };

    private static final String[] ADDRESS_LOOKUP_PROJECTION = {
            ContactsContract.CommonDataKinds.StructuredPostal._ID,
            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
            ContactsContract.CommonDataKinds.StructuredPostal.LABEL,
    };

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle args) {
        CursorLoader loader;
        switch (loaderId) {
            case DETAILS_QUERY_CONTACT_ID: {
                loader =
                        new CursorLoader(
                                this,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                ID_LOOKUP_PROJECTION,
                                ContactsContract.Data._ID + " = ?",
                                new String[]{args.getString(ARG_LOADER_PHONE_ROW_ID)},
                                SORT_ORDER
                        );

                return loader;
            }
            case DETAILS_QUERY_CONTACT_EMAIL: {
                loader =
                        new CursorLoader(
                                this,
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                EMAIL_LOOKUP_PROJECTION,
                                SELECTION,
                                new String[]{args.getString(ARG_LOADER_CONTACT_ID)},
                                SORT_ORDER
                        );
                return loader;
            }
            case DETAILS_QUERY_CONTACT_ADDRESS: {
                loader =
                        new CursorLoader(
                                this,
                                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                                ADDRESS_LOOKUP_PROJECTION,
                                SELECTION,
                                new String[]{args.getString(ARG_LOADER_CONTACT_ID)},
                                SORT_ORDER
                        );
                return loader;
            }
        }
        throw new RuntimeException();
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case DETAILS_QUERY_CONTACT_ID: {
                Bundle args = new Bundle();
                data.moveToFirst();
                long contactId = data.getLong(4);
                args.putString(ARG_LOADER_CONTACT_ID, data.getString(1));
                String name = data.getString(2);
                String number = data.getString(3);
                int type = data.getInt(5);
                String label = data.getString(6);

                LoaderManager.getInstance(this).initLoader(DETAILS_QUERY_CONTACT_EMAIL, args, this);
                LoaderManager.getInstance(this).initLoader(DETAILS_QUERY_CONTACT_ADDRESS, args, this);
                contactBuilder.setName(name);
                contactBuilder.addNumbers(ContactPhone.newBuilder()
                        .setNumber(number)
                        .setLabel(ContactCardUtils.convertAndroidTelephoneTypeToString(type, label))
                        .build());
                updateAdapter();
                if (userId == null) {
                    bgWorkers.execute(() -> {
                        Bitmap bitmap = null;
                        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
                        try (InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), contactUri)) {
                            if (inputStream != null) {
                                bitmap = BitmapFactory.decodeStream(inputStream);
                            }
                        } catch (IOException e) {
                            Log.e("CreateContactCardActivity/getAddressBookPhoto failed to get photo", e);
                        }
                        addPhotoToContactCard(bitmap);
                    });
                }
                break;
            }
            case DETAILS_QUERY_CONTACT_ADDRESS: {
                if (data.getCount() > 0) {
                    data.moveToFirst();
                    int type = data.getInt(2);
                    String label = data.getString(3);
                    ContactAddress address = ContactAddress.newBuilder()
                            .setAddress(data.getString(1))
                            .setLabel(ContactCardUtils.convertAndroidAddressTypeToString(type, label))
                            .build();
                    contactBuilder.addAddresses(address);
                    updateAdapter();
                }
                break;
            }
            case DETAILS_QUERY_CONTACT_EMAIL: {
                if (data.getCount() > 0) {
                    data.moveToFirst();
                    int type = data.getInt(2);
                    String label = data.getString(3);
                    ContactEmail contactEmail = ContactEmail.newBuilder()
                            .setAddress(data.getString(1))
                            .setLabel(ContactCardUtils.convertAndroidEmailTypeToString(type, label)).build();
                    contactBuilder.addEmails(contactEmail);
                    updateAdapter();
                }
                break;
            }
        }
    }

    private void updateAdapter() {
        adapter.setContactCard(ContactCard.newBuilder().addContacts(contactBuilder).build());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    private MenuItem sendMenuItem;

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.share_contact_menu, menu);
        sendMenuItem = menu.findItem(R.id.send);
        updateAction(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.send) {
            Intent data = new Intent();
            data.putExtra(EXTRA_CONTACT_CARD, adapter.serializeContactCard().toByteArray());
            setResult(RESULT_OK, data);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateAction(boolean enabled) {
        if (sendMenuItem != null) {
            SpannableString ss = new SpannableString(getString(R.string.send));
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), enabled ? R.color.color_secondary : R.color.disabled_text)), 0, ss.length(), 0);
            sendMenuItem.setTitle(ss);
            sendMenuItem.setEnabled(enabled);
        }
    }

    private ContactCardAdapter adapter = new ContactCardAdapter(true);

    private AvatarLoader avatarLoader;
    private DeviceAvatarLoader deviceAvatarLoader;

    private UserId userId;

    private RecyclerView contactInfoRv;

    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = new Bundle();

        avatarLoader = AvatarLoader.getInstance();
        deviceAvatarLoader = new DeviceAvatarLoader(this);

        args.putString(ARG_LOADER_PHONE_ROW_ID, getIntent().getStringExtra(EXTRA_CONTACT_ID));
        LoaderManager.getInstance(this).initLoader(DETAILS_QUERY_CONTACT_ID, args, this);

        userId = getIntent().getParcelableExtra(EXTRA_USER_ID);

        setContentView(R.layout.activity_share_contact);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contactInfoRv = findViewById(R.id.contact_info_rv);
        ((SimpleItemAnimator) contactInfoRv.getItemAnimator()).setSupportsChangeAnimations(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        contactInfoRv.setLayoutManager(layoutManager);

        contactInfoRv.setAdapter(adapter);

        if (userId != null) {
            bgWorkers.execute(() -> {
                Bitmap bitmap = null;
                if (avatarLoader.hasAvatar(userId)) {
                    bitmap = avatarLoader.getAvatar(this, userId);
                    addPhotoToContactCard(bitmap);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceAvatarLoader != null) {
            deviceAvatarLoader.destroy();
            deviceAvatarLoader = null;
        }
    }

    @WorkerThread
    private void addPhotoToContactCard(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        contactInfoRv.post(() -> {
            contactBuilder.setPhoto(ByteString.copyFrom(baos.toByteArray()));
            updateAdapter();
        });
    }
}
