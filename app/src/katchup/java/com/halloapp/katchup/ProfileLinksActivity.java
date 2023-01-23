package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.ui.HalloActivity;

public class ProfileLinksActivity extends HalloActivity {

    public static final String EXTRA_LINK = "link";
    public static final String EXTRA_TIKTOK = "tiktok";
    public static final String EXTRA_INSTAGRAM = "instagram";
    public static final String EXTRA_SNAPCHAT = "snapchat";

    private static final int REQUEST_CODE_EDIT_LINK = 1;

    public static Intent open(@NonNull Context context, @Nullable String link, @Nullable String tiktok, @Nullable String instagram, @Nullable String snapchat) {
        Intent intent = new Intent(context, ProfileLinksActivity.class);
        intent.putExtra(EXTRA_LINK, link);
        intent.putExtra(EXTRA_TIKTOK, tiktok);
        intent.putExtra(EXTRA_INSTAGRAM, instagram);
        intent.putExtra(EXTRA_SNAPCHAT, snapchat);

        return intent;
    }

    private ProfileLinksViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_links);

        TextView linkView = findViewById(R.id.link);
        TextView tiktokView = findViewById(R.id.tiktok);
        TextView instagramView = findViewById(R.id.instagram);
        TextView snapchatView = findViewById(R.id.snapchat);

        String link = getIntent().getStringExtra(EXTRA_LINK);
        String tiktok = getIntent().getStringExtra(EXTRA_TIKTOK);
        String instagram = getIntent().getStringExtra(EXTRA_INSTAGRAM);
        String snapchat = getIntent().getStringExtra(EXTRA_SNAPCHAT);

        viewModel = new ViewModelProvider(this, new ProfileLinksViewModel.Factory(link, tiktok, instagram, snapchat)).get(ProfileLinksViewModel.class);

        viewModel.link.observe(this, value -> {
            if (!TextUtils.isEmpty(value)) {
                if (value.startsWith("https://")) {
                    value = value.substring(8);
                } else if (value.startsWith("http://")) {
                    value = value.substring(7);
                }
            }

            linkView.setText(value);
        });
        viewModel.tiktok.observe(this, value -> {
            if (!TextUtils.isEmpty(value) && !value.startsWith("@")) {
                value = "@" + value;
            }

            tiktokView.setText(value);
        });
        viewModel.instagram.observe(this, value -> {
            if (!TextUtils.isEmpty(value) && !value.startsWith("@")) {
                value = "@" + value;
            }

            instagramView.setText(value);
        });
        viewModel.snapchat.observe(this, value -> {
            if (!TextUtils.isEmpty(value) && !value.startsWith("@")) {
                value = "@" + value;
            }

            snapchatView.setText(value);
        });

        View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> finish());

        View done = findViewById(R.id.done);
        done.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_LINK, viewModel.link.getValue());
            intent.putExtra(EXTRA_TIKTOK, viewModel.tiktok.getValue());
            intent.putExtra(EXTRA_INSTAGRAM, viewModel.instagram.getValue());
            intent.putExtra(EXTRA_SNAPCHAT, viewModel.snapchat.getValue());

            setResult(RESULT_OK, intent);

            finish();
        });

        linkView.setOnClickListener(v -> {
            startActivityForResult(ProfileLinkEditActivity.open(this, ProfileLinkEditActivity.TYPE_LINK, viewModel.link.getValue()), REQUEST_CODE_EDIT_LINK);
        });

        tiktokView.setOnClickListener(v -> {
            startActivityForResult(ProfileLinkEditActivity.open(this, ProfileLinkEditActivity.TYPE_TIKTOK, viewModel.tiktok.getValue()), REQUEST_CODE_EDIT_LINK);
        });

        instagramView.setOnClickListener(v -> {
            startActivityForResult(ProfileLinkEditActivity.open(this, ProfileLinkEditActivity.TYPE_INSTAGRAM, viewModel.instagram.getValue()), REQUEST_CODE_EDIT_LINK);
        });

        snapchatView.setOnClickListener(v -> {
            startActivityForResult(ProfileLinkEditActivity.open(this, ProfileLinkEditActivity.TYPE_SNAPCHAT, viewModel.snapchat.getValue()), REQUEST_CODE_EDIT_LINK);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_EDIT_LINK && data != null) {
            int type = data.getIntExtra(ProfileLinkEditActivity.EXTRA_TYPE, 0);
            String link = data.getStringExtra(ProfileLinkEditActivity.EXTRA_LINK);

            if (type == ProfileLinkEditActivity.TYPE_LINK) {
                viewModel.link.postValue(link);
            } else if (type == ProfileLinkEditActivity.TYPE_TIKTOK) {
                viewModel.tiktok.postValue(link);
            } else if (type == ProfileLinkEditActivity.TYPE_INSTAGRAM) {
                viewModel.instagram.postValue(link);
            } else if (type == ProfileLinkEditActivity.TYPE_SNAPCHAT) {
                viewModel.snapchat.postValue(link);
            }
        }
    }

    public static class ProfileLinksViewModel extends ViewModel {
        public final MutableLiveData<String> link = new MutableLiveData<>();
        public final MutableLiveData<String> tiktok = new MutableLiveData<>();
        public final MutableLiveData<String> instagram = new MutableLiveData<>();
        public final MutableLiveData<String> snapchat = new MutableLiveData<>();

        public ProfileLinksViewModel(String link, String tiktok, String instagram, String snapchat) {
            this.link.postValue(link);
            this.tiktok.postValue(tiktok);
            this.instagram.postValue(instagram);
            this.snapchat.postValue(snapchat);
        }

        public static class Factory implements ViewModelProvider.Factory {
            private final String link;
            private final String tiktok;
            private final String instagram;
            private final String snapchat;

            public Factory(String link, String tiktok, String instagram, String snapchat) {
                this.link = link;
                this.tiktok = tiktok;
                this.instagram = instagram;
                this.snapchat = snapchat;
            }

            @Override
            public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ProfileLinksViewModel.class)) {
                    //noinspection unchecked
                    return (T) new ProfileLinksViewModel(link, tiktok, instagram, snapchat);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }
}
