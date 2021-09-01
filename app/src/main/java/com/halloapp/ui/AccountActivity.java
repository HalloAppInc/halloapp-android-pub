package com.halloapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.util.Preconditions;

public class AccountActivity extends HalloActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        View requestDataView = findViewById(R.id.request_data);
        requestDataView.setOnClickListener(v -> {
            startActivity(new Intent(this, ExportDataActivity.class));
        });

        View deleteAccountView = findViewById(R.id.delete_account);
        deleteAccountView.setOnClickListener(v -> {
            startActivity(new Intent(this, DeleteAccountActivity.class));
        });
    }
}
