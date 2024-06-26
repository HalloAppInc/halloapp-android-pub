package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.halloapp.R;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;

public class AppExpirationActivity extends HalloActivity {

    public static final String EXTRA_DAYS_LEFT = "days_left";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_expiration);

        String text;
        int daysLeft = getIntent().getIntExtra(EXTRA_DAYS_LEFT, 10);
        if (daysLeft > 0) {
            text = getResources().getQuantityString(R.plurals.app_expiration_days_left, daysLeft, daysLeft);
            Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        } else {
            text = getResources().getString(R.string.app_expired_explanation);
        }

        TextView description = findViewById(R.id.description);
        description.setText(text);

        final Button downloadButton = findViewById(R.id.download_button);
        downloadButton.setOnClickListener(v -> IntentUtils.openPlayOrMarket(this));
    }

    public static void open(Context context, int daysLeft) {
        Intent appExpirationIntent = new Intent(context, AppExpirationActivity.class);
        appExpirationIntent.putExtra(AppExpirationActivity.EXTRA_DAYS_LEFT, daysLeft);
        if (daysLeft <= 0) {
            appExpirationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            appExpirationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(appExpirationIntent);
    }
}
