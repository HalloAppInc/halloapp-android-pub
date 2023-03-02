package com.halloapp.katchup;

import android.content.Intent;
import android.graphics.Outline;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.transition.TransitionManager;

import com.halloapp.BuildConfig;
import com.halloapp.R;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.logs.LogProvider;

public class SettingsFeedbackFragment extends HalloFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings_feedback, container, false);

        View feedbackForm = root.findViewById(R.id.feedback_form);
        EditText feedbackView = root.findViewById(R.id.feedback);
        View sendBtn = root.findViewById(R.id.send);
        View sendLogsBtn = root.findViewById(R.id.send_logs);
        View thankYouView = root.findViewById(R.id.thank_you);

        sendBtn.setEnabled(false);
        thankYouView.setVisibility(View.GONE);

        ViewOutlineProvider roundedOutlineProvider = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), view.getHeight());
            }
        };
        sendBtn.setClipToOutline(true);
        sendBtn.setOutlineProvider(roundedOutlineProvider);

        sendLogsBtn.setOnClickListener(v -> LogProvider.openLogIntent(requireContext()).observe(getViewLifecycleOwner(), this::startActivity));
        sendBtn.setOnClickListener(v -> {
            TransitionManager.beginDelayedTransition((ViewGroup) feedbackForm.getParent());
            feedbackForm.setVisibility(View.GONE);
            thankYouView.setVisibility(View.VISIBLE);

            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {LogProvider.getSupportEmail()});
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.katchup_email_logs_subject, BuildConfig.VERSION_NAME, LogProvider.getTimestamp()));
            intent.putExtra(android.content.Intent.EXTRA_TEXT, feedbackView.getText().toString().trim());

            startActivity(intent);
        });

        feedbackView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sendBtn.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
            }
        });

        return root;
    }
}
