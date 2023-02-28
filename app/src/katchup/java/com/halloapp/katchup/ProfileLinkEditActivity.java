package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.widget.SnackbarHelper;

public class ProfileLinkEditActivity extends HalloActivity {

    private static final long KEYBOARD_SHOW_DELAY = 100;

    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_LINK = "link";

    public static final int TYPE_LINK = 1;
    public static final int TYPE_TIKTOK = 2;
    public static final int TYPE_INSTAGRAM = 3;
    public static final int TYPE_SNAPCHAT = 4;

    private static final String NEGATIVE_SOCIAL_HANDLE_REGEX = "[\\s\\\\/]+";

    public static Intent open(@NonNull Context context, int type, @Nullable String link) {
        Intent intent = new Intent(context, ProfileLinkEditActivity.class);
        intent.putExtra(EXTRA_TYPE, type);
        intent.putExtra(EXTRA_LINK, link);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_link_edit);

        TextView labelView = findViewById(R.id.label);
        EditText linkView = findViewById(R.id.link);

        int type = getIntent().getIntExtra(EXTRA_TYPE, 0);
        if (type == TYPE_LINK) {
            labelView.setText(R.string.profile_link);
        } else if (type == TYPE_TIKTOK) {
            labelView.setText(R.string.tiktok);
        } else if (type == TYPE_INSTAGRAM) {
            labelView.setText(R.string.instagram);
        } else if (type == TYPE_SNAPCHAT) {
            labelView.setText(R.string.snapchat);
        }

        linkView.setText(getIntent().getStringExtra(EXTRA_LINK));

        View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> finish());

        View done = findViewById(R.id.done);
        done.setOnClickListener(v -> {
            KeyboardUtils.hideSoftKeyboard(linkView);

            String link = linkView.getText().toString().trim();

            if (!TextUtils.isEmpty(link) && link.startsWith("@")) {
                link = link.substring(1);
            }

            if (!TextUtils.isEmpty(link) && !isValid(link, type)) {
                if (type == TYPE_LINK) {
                    SnackbarHelper.showWarning(linkView, R.string.error_invalid_link);
                } else {
                    SnackbarHelper.showWarning(linkView, R.string.error_invalid_username);
                }

                return;
            }

            if (type == TYPE_LINK) {
                if (Uri.parse(link).getScheme() == null) {
                    link = "http://" + link;
                }
            }

            Intent intent = new Intent();
            intent.putExtra(EXTRA_TYPE, getIntent().getIntExtra(EXTRA_TYPE, 0));
            intent.putExtra(EXTRA_LINK, link);

            setResult(RESULT_OK, intent);

            finish();
        });

        // without the delay the keyboard is not shown
        linkView.postDelayed(() -> {
            linkView.setSelection(linkView.getText().length());
            KeyboardUtils.showSoftKeyboard(linkView);
        }, KEYBOARD_SHOW_DELAY);
    }

    public boolean isValid(String link, int type) {
        if (type == TYPE_LINK) {
            return Patterns.WEB_URL.matcher(link).matches();
        } else {
            return !link.matches(NEGATIVE_SOCIAL_HANDLE_REGEX);
        }
    }
}
