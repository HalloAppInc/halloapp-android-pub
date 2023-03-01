package com.halloapp.katchup;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.MainActivity;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AspectRatioFrameLayout;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class ContactsAndLocationAccessActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks, OnboardingSplashFragment.OnSplashFadedHandler {
    private static final int REQUEST_CONTACTS_PERMISSION = 0;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private Button requestContactsAccessButton;
    private Button requestLocationAccessButton;

    private boolean splashFaded;
    private ContactsAndLocationAccessViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Analytics.getInstance().logOnboardingStart();
        Analytics.getInstance().openScreen("onboardingPermissions");

        final Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        WindowCompat.setDecorFitsSystemWindows(window, false);

        setContentView(R.layout.activity_contacts_and_location_access);

        formatPrivacyText();
        setOvalTextFrameRatio();
        requestContactsAccessButton = findViewById(R.id.request_contacts_access);
        requestContactsAccessButton.setOnClickListener(view -> requestContactsAccess());
        requestLocationAccessButton = findViewById(R.id.request_location_access);
        requestLocationAccessButton.setOnClickListener(view -> requestLocationAccess());

        viewModel = new ViewModelProvider(this).get(ContactsAndLocationAccessViewModel.class);
        viewModel.contactsAccessRequested.getLiveData().observe(this, accessRequested -> requestContactsAccessButton.setEnabled(!accessRequested));
        viewModel.locationAccessRequested.getLiveData().observe(this, accessRequested -> requestLocationAccessButton.setEnabled(!accessRequested));
        viewModel.allPermissionsRequested.observe(this, this::allPermissionsRequested);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshForGrantedPermissions();
    }

    private void setOvalTextFrameRatio() {
        final AspectRatioFrameLayout ovalTextFrame = findViewById(R.id.oval_text_frame);
        final int rotationAngle = getResources().getInteger(R.integer.jellybean_slant_right_up);
        ovalTextFrame.setAspectRatio((float) (Constants.PROFILE_PHOTO_OVAL_HEIGHT_RATIO / Math.cos(Math.toRadians(rotationAngle))));
    }

    private void formatPrivacyText() {
        final String iconSpacePrefix = "  ";
        final int textSize = getResources().getDimensionPixelSize(R.dimen.onboarding_info_text_size);
        final int textColor = getResources().getColor(R.color.onboarding_info_text);
        final Drawable padlockIcon = Preconditions.checkNotNull(ContextCompat.getDrawable(this, R.drawable.ic_settings_privacy));
        padlockIcon.setBounds(0, 0, textSize, textSize);
        padlockIcon.setTint(textColor);
        final ImageSpan privacyIcon = new ImageSpan(padlockIcon, ImageSpan.ALIGN_BASELINE);
        final SpannableString privacyString = new SpannableString(iconSpacePrefix + getResources().getString(R.string.contacts_and_location_privacy_statement));
        privacyString.setSpan(privacyIcon, 0, iconSpacePrefix.length() / 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        final TextView privacyStatement = findViewById(R.id.privacy_statement);
        privacyStatement.setText(privacyString);
    }

    private void refreshForGrantedPermissions() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_CONTACTS)) {
            viewModel.flagContactsAccessRequested();
        }
        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION) || EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            viewModel.flagLocationAccessRequested();
        }
    }

    private void requestContactsAccess() {
        if (splashFaded) {
            final String[] permissions = {Manifest.permission.READ_CONTACTS};
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, REQUEST_CONTACTS_PERMISSION, permissions)
                            .setRationale(R.string.contacts_access_request_rationale)
                            .setNegativeButtonText(R.string.permission_negative_button_text)
                            .build());
        }
    }

    private void requestLocationAccess() {
        if (splashFaded) {
            final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, REQUEST_LOCATION_PERMISSION, permissions)
                            .setRationale(R.string.location_access_request_rationale)
                            .setNegativeButtonText(R.string.permission_negative_button_text)
                            .build());
        }
    }

    private void allPermissionsRequested(@Nullable Boolean granted) {
        if (Boolean.TRUE.equals(granted)) {
            startActivity(new Intent(ContactsAndLocationAccessActivity.this, MainActivity.class));
            finish();
        }
    }

    private void onPermissionRequested(int requestCode, boolean granted) {
        if (requestCode == REQUEST_CONTACTS_PERMISSION) {
            Log.d("ContactsAndLocationAccessActivity.onPermissionRequested contacts");
            Analytics.getInstance().logOnboardingEnableContacts(granted);
            viewModel.flagContactsAccessRequested();
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            Log.d("ContactsAndLocationAccessActivity.onPermissionRequested location");
            Analytics.getInstance().logOnboardingEnableLocation(granted);
            viewModel.flagLocationAccessRequested();
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CONTACTS_PERMISSION) {
            Preferences.getInstance().clearContactSyncBackoffTime();
            ContactsSync.getInstance().forceFullContactsSync(true);
        }
        onPermissionRequested(requestCode, true);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        onPermissionRequested(requestCode, false);
    }

    @Override
    public void onSplashFaded() {
        splashFaded = true;
    }

    public static class ContactsAndLocationAccessViewModel extends AndroidViewModel {
        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final Preferences preferences = Preferences.getInstance();

        final ComputableLiveData<Boolean> contactsAccessRequested;
        final ComputableLiveData<Boolean> locationAccessRequested;
        final MediatorLiveData<Boolean> allPermissionsRequested;

        public ContactsAndLocationAccessViewModel(@NonNull Application application) {
            super(application);

            contactsAccessRequested = new ComputableLiveData<Boolean>() {
                @Override
                protected Boolean compute() {
                    return preferences.getContactsPermissionRequested();
                }
            };

            locationAccessRequested = new ComputableLiveData<Boolean>() {
                @Override
                protected Boolean compute() {
                    return preferences.getLocationPermissionRequested();
                }
            };

            allPermissionsRequested = new MediatorLiveData<>();
            allPermissionsRequested.addSource(contactsAccessRequested.getLiveData(), requested -> allPermissionsRequested.setValue(Boolean.TRUE.equals(requested) && Boolean.TRUE.equals(locationAccessRequested.getLiveData().getValue())));
            allPermissionsRequested.addSource(locationAccessRequested.getLiveData(), requested -> allPermissionsRequested.setValue(Boolean.TRUE.equals(requested) && Boolean.TRUE.equals(contactsAccessRequested.getLiveData().getValue())));
        }

        public void flagContactsAccessRequested() {
            bgWorkers.execute(() -> {
                preferences.setContactsPermissionRequested(true);
                contactsAccessRequested.invalidate();
            });
        }

        public void flagLocationAccessRequested() {
            bgWorkers.execute(() -> {
                preferences.setLocationPermissionRequested(true);
                locationAccessRequested.invalidate();
            });
        }
    }
}
