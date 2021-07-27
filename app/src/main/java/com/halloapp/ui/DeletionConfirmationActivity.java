package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.halloapp.R;

public class DeletionConfirmationActivity extends HalloActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deletion_confirmation);

        Button returnRegistration = findViewById(R.id.return_home_button);
        returnRegistration.setOnClickListener(v -> {
            final Intent intent = new Intent(getApplication(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            getApplication().startActivity(intent);
        });
    }
}
