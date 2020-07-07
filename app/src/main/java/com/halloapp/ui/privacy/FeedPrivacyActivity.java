package com.halloapp.ui.privacy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.privacy.FeedPrivacy;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.contacts.MultipleContactPickerActivity;
import com.halloapp.xmpp.PrivacyList;

import java.util.Collections;
import java.util.List;

public class FeedPrivacyActivity extends HalloActivity {

    private static final int REQUEST_CODE_SELECT_EXCEPT_LIST = 1;
    private static final int REQUEST_CODE_SELECT_ONLY_LIST = 2;

    private FeedPrivacyViewModel viewModel;

    private @Nullable List<UserId> onlyList;
    private @Nullable List<UserId> exceptList;

    private RadioGroup settingRadioGroup;

    private @PrivacyList.Type String selectedType;

    private @PrivacyList.Type String getSelectedType() {
        switch (settingRadioGroup.getCheckedRadioButtonId()) {
            case R.id.radio_all:
                return PrivacyList.Type.ALL;
            case R.id.radio_only:
                return PrivacyList.Type.ONLY;
            case R.id.radio_except:
                return PrivacyList.Type.EXCEPT;
        }
        return PrivacyList.Type.INVALID;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feed_privacy);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setElevation(getResources().getDimension(R.dimen.action_bar_elevation));
        }

        final RadioButton all = findViewById(R.id.radio_all);
        final RadioButton only = findViewById(R.id.radio_only);
        final RadioButton except = findViewById(R.id.radio_except);
        settingRadioGroup = findViewById(R.id.list_group);

        all.setOnClickListener(v -> {
            selectedType = PrivacyList.Type.ALL;
        });
        only.setOnClickListener(v -> {
            openMultipleContactPicker(REQUEST_CODE_SELECT_ONLY_LIST, onlyList, R.string.contact_picker_feed_only_title);
        });
        except.setOnClickListener(v -> {
            openMultipleContactPicker(REQUEST_CODE_SELECT_EXCEPT_LIST, exceptList, R.string.contact_picker_feed_except_title);
        });

        View content = findViewById(R.id.options_container);
        View progressSpinner = findViewById(R.id.progress_spinner);
        TextView errorView = findViewById(R.id.error_msg);

        viewModel = new ViewModelProvider(this).get(FeedPrivacyViewModel.class);
        viewModel.feedPrivacy.getLiveData().observe(this, feedPrivacy -> {
            progressSpinner.setVisibility(View.GONE);
            if (feedPrivacy == null) {
                errorView.setVisibility(View.VISIBLE);
                return;
            } else {
                content.setVisibility(View.VISIBLE);
            }
            onlyList = feedPrivacy.onlyList;
            exceptList = feedPrivacy.exceptList;
            selectedType = feedPrivacy.activeList;
            checkRadioButtonForType(selectedType);
        });

        View update = findViewById(R.id.save);
        update.setOnClickListener(v -> {
            selectedType = getSelectedType();
            FeedPrivacy feedPrivacy = viewModel.feedPrivacy.getLiveData().getValue();
            if (selectedType != null && feedPrivacy != null) {
                List<UserId> currentList = getCurrentList();
                if (viewModel.hasChanges(selectedType, currentList)) {
                    viewModel.savePrivacy(selectedType, currentList).observe(this, done -> {
                        if (done != null) {
                            Toast.makeText(this, done ? R.string.feed_privacy_update_success : R.string.feed_privacy_update_failure, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    return;
                }
            }
            finish();
        });
    }

    @NonNull
    private List<UserId> getCurrentList() {
        List<UserId> currentList;
        switch (selectedType) {
            case PrivacyList.Type.EXCEPT:
                currentList = exceptList;
                break;
            case PrivacyList.Type.ONLY:
                currentList = onlyList;
                break;
            default:
                currentList = Collections.emptyList();
        }
        if (currentList == null) {
            currentList = Collections.emptyList();
        }
        return currentList;
    }

    @Override
    public void onBackPressed() {
        selectedType = getSelectedType();
        if (viewModel.hasChanges(selectedType, getCurrentList())) {
            showDiscardChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showDiscardChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_discard_changes_message);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.action_discard, (dialog, which) -> {
            finish();
        });
        builder.create().show();
    }

    private void openMultipleContactPicker(int requestCode, List<UserId> currentList, @StringRes int title) {
        startActivityForResult(MultipleContactPickerActivity.newPickerIntent(this, currentList, title), requestCode);
    }

    private void revertToPreviousSelection() {
        checkRadioButtonForType(selectedType);
    }

    private void checkRadioButtonForType(@PrivacyList.Type String type) {
        if (type == null) {
            settingRadioGroup.clearCheck();
            return;
        }
        switch (type) {
            case PrivacyList.Type.ALL:
                settingRadioGroup.check(R.id.radio_all);
                break;
            case PrivacyList.Type.EXCEPT:
                settingRadioGroup.check(R.id.radio_except);
                break;
            case PrivacyList.Type.ONLY:
                settingRadioGroup.check(R.id.radio_only);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SELECT_EXCEPT_LIST:
                if (resultCode == RESULT_OK && data != null) {
                    settingRadioGroup.check(R.id.radio_except);
                    exceptList = data.getParcelableArrayListExtra(MultipleContactPickerActivity.EXTRA_RESULT_SELECTED_IDS);
                    selectedType = PrivacyList.Type.EXCEPT;
                } else {
                    revertToPreviousSelection();
                }
                break;
            case REQUEST_CODE_SELECT_ONLY_LIST:
                if (resultCode == RESULT_OK && data != null) {
                    settingRadioGroup.check(R.id.radio_only);
                    onlyList = data.getParcelableArrayListExtra(MultipleContactPickerActivity.EXTRA_RESULT_SELECTED_IDS);
                    selectedType = PrivacyList.Type.ONLY;
                } else {
                    revertToPreviousSelection();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
