package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.KeyboardUtils;

public class ProfileBioEditActivity extends HalloActivity {
    private static final long KEYBOARD_SHOW_DELAY = 100;

    public static final String EXTRA_BIO = "bio";

    public static Intent open(@NonNull Context context, @Nullable String bio) {
        Intent intent = new Intent(context, ProfileBioEditActivity.class);
        intent.putExtra(EXTRA_BIO, bio);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_bio_edit);

        EditText bioView = findViewById(R.id.bio);

        String bio = getIntent().getStringExtra(EXTRA_BIO);
        if (!TextUtils.isEmpty(bio)) {
            bioView.setText(bio);
        }

        View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> finish());

        View done = findViewById(R.id.done);
        done.setOnClickListener(v -> {
            KeyboardUtils.hideSoftKeyboard(bioView);
            String newBio = bioView.getText().toString().replace("\n", " ").replace("\r", " ").trim();

            Intent intent = new Intent();
            intent.putExtra(EXTRA_BIO, newBio);

            setResult(RESULT_OK, intent);

            finish();
        });

        // without the delay the keyboard is not shown
        bioView.postDelayed(() -> {
            bioView.setSelection(bioView.getText().length());
            KeyboardUtils.showSoftKeyboard(bioView);
        }, KEYBOARD_SHOW_DELAY);
    }
}
