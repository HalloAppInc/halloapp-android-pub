package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.ReportUserContent;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;

public class ReportActivity extends HalloActivity {

    public static Intent open(@NonNull Context context, @NonNull UserId userId, @Nullable String contentId) {
        Intent intent = new Intent(context, ReportActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        if (contentId != null) {
            intent.putExtra(EXTRA_CONTENT_ID, contentId);
        }
        return intent;
    }

    private static final String EXTRA_USER_ID = "user_id";
    private static final String EXTRA_CONTENT_ID = "content_id";

    private ReportUserContent.Reason reason;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report);

        UserId userId = getIntent().getParcelableExtra(EXTRA_USER_ID);
        String contentId = getIntent().getStringExtra(EXTRA_CONTENT_ID);
        Log.d("ReportActivity got userId " + userId + " and contentId " + contentId);

        findViewById(R.id.prev).setOnClickListener(v -> onBackPressed());

        View reportButton = findViewById(R.id.report_button);
        reportButton.setEnabled(false);
        View cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> finish());
        reportButton.setOnClickListener(v -> {
            Connection.getInstance().reportUserContent(userId, contentId, reason).onResponse(response -> {
                reportButton.post(() -> Toast.makeText(this, R.string.reported, Toast.LENGTH_LONG).show());
                if (contentId != null) {
                    Post post = ContentDb.getInstance().getPost(contentId);
                    PublicContentCache.getInstance().removeReportedPost(contentId);

                    if (post != null) {
                        ContentDb.getInstance().deletePost(post, null);
                    } else {
                        Log.w("Failed to find post " + contentId);
                    }
                    setResult(RESULT_OK);
                    finish();
                } else {
                    finish();
                }
            }).onError(err -> {
                Log.e("Failed to report content", err);
                SnackbarHelper.showWarning(reportButton, R.string.report_failed);
            });
        });

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            reportButton.setBackgroundResource(R.drawable.report_button_background_enabled);
            reportButton.setEnabled(true);
            if (checkedId == R.id.radio_dont_like) {
                reason = ReportUserContent.Reason.DONT_LIKE;
            } else if (checkedId == R.id.radio_spam) {
                reason = ReportUserContent.Reason.SPAM;
            } else if (checkedId == R.id.radio_violates_rules) {
                reason = ReportUserContent.Reason.VIOLATES_RULES;
            } else if (checkedId == R.id.radio_other) {
                reason = ReportUserContent.Reason.OTHER;
            }
        });
    }
}
