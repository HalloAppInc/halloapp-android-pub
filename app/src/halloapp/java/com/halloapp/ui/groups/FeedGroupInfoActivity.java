package com.halloapp.ui.groups;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.id.GroupId;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.server.ExpiryInfo;
import com.halloapp.ui.markdown.MarkdownUtils;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

public class FeedGroupInfoActivity extends BaseGroupInfoActivity implements SelectGroupExpiryDialogFragment.Host {

    public static Intent viewGroup(@NonNull Context context, @NonNull GroupId groupId) {
        Preconditions.checkNotNull(groupId);
        Intent intent = new Intent(context, FeedGroupInfoActivity.class);
        intent.putExtra(GROUP_ID, groupId);
        return intent;
    }

    private int selectedExpiry = -1;

    private boolean showExpirySetting = false;

    private FeedGroupInfoViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View bgContainer = findViewById(R.id.group_background);
        showExpirySetting = ServerProps.getInstance().isGroupExpiryEnabled();
        TextView groupBgDesc = findViewById(R.id.group_background_description);
        ImageView bgColorPreview = findViewById(R.id.bg_color_preview);

        TextView groupExpiryDesc = findViewById(R.id.group_expiry_description);
        ImageView groupExpiryIcon = findViewById(R.id.group_expiry_icon);
        View expirationContainer = findViewById(R.id.expiration_container);
        expirationContainer.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.getUserIsAdmin().getValue())) {
                DialogFragmentUtils.showDialogFragmentOnce(SelectGroupExpiryDialogFragment.newInstance(selectedExpiry), getSupportFragmentManager());
            }
        });

        bgContainer.setVisibility(View.VISIBLE);
        bgContainer.setOnClickListener(v -> {
            if (getChatIsActive()) {
                Intent i = GroupBackgroundActivity.newIntent(this, groupId);
                startActivity(i);
            } else {
                SnackbarHelper.showWarning(bgContainer, R.string.failed_no_longer_member);
            }
        });

        getViewModel();

        viewModel.getGroup().observe(this, group -> {
            if (group == null) {
                Log.w("GroupInfoActivity got null chat for " + groupId);
                return;
            }
            groupBgDesc.setText(group.theme == 0 ? R.string.group_background_default : R.string.group_background_color);
            GroupTheme theme = GroupTheme.getTheme(group.theme);
            bgColorPreview.setImageDrawable(new ColorDrawable(ContextCompat.getColor(this, theme.bgColor)));
            if (group.expiryInfo != null) {
                switch (group.expiryInfo.getExpiryType()) {
                    case NEVER:
                        groupExpiryDesc.setText(R.string.expiration_never);
                        groupExpiryIcon.setImageResource(R.drawable.ic_content_no_expiry);
                        selectedExpiry = SelectGroupExpiryDialogFragment.OPTION_NEVER;
                        if (!showExpirySetting) {
                            expirationContainer.setVisibility(View.VISIBLE);
                        }
                        break;
                    case EXPIRES_IN_SECONDS:
                        long seconds = group.expiryInfo.getExpiresInSeconds();
                        groupExpiryDesc.setText(TimeFormatter.formatExpirationDuration(this, (int) seconds));
                        groupExpiryIcon.setImageResource(R.drawable.ic_content_expiry);
                        if (seconds == Constants.SECONDS_PER_DAY) {
                            selectedExpiry = SelectGroupExpiryDialogFragment.OPTION_24_HOURS;
                            if (!showExpirySetting) {
                                expirationContainer.setVisibility(View.VISIBLE);
                            }
                        } else if (seconds == Constants.SECONDS_PER_DAY * 30) {
                            selectedExpiry = SelectGroupExpiryDialogFragment.OPTION_30_DAYS;
                            if (!showExpirySetting) {
                                expirationContainer.setVisibility(View.GONE);
                            }
                        } else {
                            selectedExpiry = -1;
                            if (!showExpirySetting) {
                                expirationContainer.setVisibility(View.VISIBLE);
                            }
                        }
                        break;
                    default:
                        if (!showExpirySetting) {
                            expirationContainer.setVisibility(View.GONE);
                        }
                        selectedExpiry = -1;
                }
            }
        });
    }

    @Override
    public void onExpirySelected(int selectedOption) {
        ProgressDialog changeExpiryDialog = ProgressDialog.show(this, null, getString(R.string.change_group_expiry_in_progress), true);
        changeExpiryDialog.show();
        ExpiryInfo expiryInfo = null;
        switch (selectedOption) {
            case SelectGroupExpiryDialogFragment.OPTION_24_HOURS:
                expiryInfo = ExpiryInfo.newBuilder().setExpiresInSeconds(Constants.SECONDS_PER_DAY).setExpiryType(ExpiryInfo.ExpiryType.EXPIRES_IN_SECONDS).build();
                break;
            case SelectGroupExpiryDialogFragment.OPTION_30_DAYS:
                expiryInfo = ExpiryInfo.newBuilder().setExpiresInSeconds(Constants.SECONDS_PER_DAY * 30).setExpiryType(ExpiryInfo.ExpiryType.EXPIRES_IN_SECONDS).build();
                break;
            case SelectGroupExpiryDialogFragment.OPTION_NEVER:
                expiryInfo = ExpiryInfo.newBuilder().setExpiryType(ExpiryInfo.ExpiryType.NEVER).build();
                break;
        }
        if (expiryInfo != null) {
            viewModel.changeExpiry(expiryInfo).observe(this, success -> {
                changeExpiryDialog.cancel();
                if (success == null || !success) {
                    SnackbarHelper.showWarning(this, R.string.group_expiry_change_failure);
                } else {
                    SnackbarHelper.showInfo(this, R.string.group_expiry_changed);
                }
            });
        } else {
            changeExpiryDialog.cancel();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_group_info;
    }

    @Override
    protected BaseGroupInfoViewModel getViewModel() {
        if (viewModel != null) {
            return viewModel;
        }
        viewModel = new ViewModelProvider(this, new FeedGroupInfoViewModel.Factory(getApplication(), groupId)).get(FeedGroupInfoViewModel.class);
        return viewModel;
    }
}
