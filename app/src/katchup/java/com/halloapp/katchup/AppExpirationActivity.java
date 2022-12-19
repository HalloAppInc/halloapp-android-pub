package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.halloapp.R;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.IntentUtils;

public class AppExpirationActivity extends HalloActivity {

    public static final String EXTRA_DAYS_LEFT = "days_left";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_katchup_expiration);

        View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> finish());

        String text;
        int daysLeft = getIntent().getIntExtra(EXTRA_DAYS_LEFT, 10);
        if (daysLeft > 0) {
            text = getResources().getQuantityString(R.plurals.katchup_expiration_days_left, daysLeft, daysLeft);
        } else {
            text = getResources().getString(R.string.katchup_expired_explanation);
            prev.setVisibility(View.GONE);
        }

        TextView description = findViewById(R.id.description);
        description.setText(text);

        final Button downloadButton = findViewById(R.id.download_button);
        downloadButton.setOnClickListener(v -> IntentUtils.openPlayOrMarket(this));
    }

    public static Intent open(Context context, int daysLeft) {
        Intent intent = new Intent(context, AppExpirationActivity.class);
        intent.putExtra(AppExpirationActivity.EXTRA_DAYS_LEFT, daysLeft);

        if (daysLeft <= 0) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        return intent;
    }
}
