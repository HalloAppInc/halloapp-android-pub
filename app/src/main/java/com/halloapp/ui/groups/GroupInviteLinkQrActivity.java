package com.halloapp.ui.groups;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.id.GroupId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.util.QrUtils;

public class GroupInviteLinkQrActivity extends HalloActivity {

    public static Intent newIntent(@NonNull Context context, String url, GroupId groupId) {
        Intent intent = new Intent(context, GroupInviteLinkQrActivity.class);
        intent.putExtra(EXTRA_URL_FOR_QR, url);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }

    private static final String EXTRA_URL_FOR_QR = "qr_url";
    private static final String EXTRA_GROUP_ID = "group_id";

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_invite_qr);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        String url = getIntent().getStringExtra(EXTRA_URL_FOR_QR);
        GroupId groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);
        GroupInviteQRViewModel viewModel = new GroupInviteQRViewModel(this.getApplication(), url, groupId);

        ImageView qrImage = findViewById(R.id.qr_image);
        ImageView avatarView = findViewById(R.id.avatar);
        TextView nameView = findViewById(R.id.name);

        viewModel.qrBitMap.getLiveData().observe(this, bitmap -> {
            if (bitmap != null) {
                qrImage.setImageBitmap(bitmap);
                WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                layoutParams.screenBrightness = 1f;
                getWindow().setAttributes(layoutParams);
            }
        });
        avatarLoader.load(avatarView, groupId, false);
        viewModel.groupName.getLiveData().observe(this, nameView::setText);
    }

    private static class GroupInviteQRViewModel extends AndroidViewModel {

        private String url;
        private GroupId groupId;
        private ContentDb contentDb;

        private final ComputableLiveData<Bitmap> qrBitMap = new ComputableLiveData<Bitmap>() {
            @Override
            protected Bitmap compute() {
                return QrUtils.encode(url);
            }
        };

        public final ComputableLiveData<String> groupName = new ComputableLiveData<String>() {
            @Override
            protected String compute() {
                return contentDb.getChat(groupId).name;
            }
        };

        public GroupInviteQRViewModel(@NonNull Application application, String url, GroupId groupId) {
            super(application);
            this.url = url;
            this.groupId = groupId;
            contentDb = ContentDb.getInstance();
        }
    }
}
