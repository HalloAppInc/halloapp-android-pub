package com.halloapp.katchup;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.SnackbarHelper;

public class SetupNameProfileActivity extends HalloActivity {

    private static final float ENTRY_USABLE_WIDTH_RATIO = 0.95f;

    private TextView nameEditHint;
    private EditText nameEditText;
    private View nextButton;

    private SetupNameProfileViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup_name);

        final int nameTextSize = getResources().getDimensionPixelSize(R.dimen.registration_name_text_size);
        final int nameTextMinSize = getResources().getDimensionPixelSize(R.dimen.registration_name_text_min_size);
        final int regEntryHorizontalPadding = getResources().getDimensionPixelSize(R.dimen.reg_name_field_horizontal_padding);

        nextButton = findViewById(R.id.next);
        nextButton.setEnabled(false);
        nameEditHint = findViewById(R.id.name_hint);
        nameEditText = findViewById(R.id.name);
        nameEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constants.MAX_NAME_LENGTH)});
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final int length = s.length();
                nameEditHint.setVisibility(length > 0 ? View.INVISIBLE : View.VISIBLE);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                final float textWidth = StaticLayout.getDesiredWidth(s, nameEditText.getPaint());
                final float availableWidth = displayMetrics.widthPixels - 2 * regEntryHorizontalPadding;
                final float scaleFactor = availableWidth / textWidth;
                final int candidateTextSize = (int) Math.floor(nameEditText.getTextSize() * scaleFactor * ENTRY_USABLE_WIDTH_RATIO);
                nameEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.max(nameTextMinSize, Math.min(nameTextSize, candidateTextSize)));

                updateNextButton();
            }
        });
        nameEditText.requestFocus();

        nextButton.setOnClickListener(v -> {
            final String name = StringUtils.preparePostText(Preconditions.checkNotNull(nameEditText.getText()).toString());
            if (TextUtils.isEmpty(name)) {
                SnackbarHelper.showWarning(this, R.string.name_must_be_specified);
                nameEditText.requestFocus();
            } else {
                Analytics.getInstance().logOnboardingEnteredName();
                startActivity(SetupUsernameProfileActivity.open(this, name));
            }
        });

        viewModel = new ViewModelProvider(this).get(SetupNameProfileActivity.SetupNameProfileViewModel.class);
        viewModel.name.getLiveData().observe(this, name -> nameEditText.setText(name));
    }

    private void updateNextButton() {
        boolean nameValid = !TextUtils.isEmpty(nameEditText.getText().toString());

        nextButton.setEnabled(nameValid);
    }

    public static class SetupNameProfileViewModel extends ViewModel {
        private final Me me;
        final ComputableLiveData<String> name;

        public SetupNameProfileViewModel() {
            me = Me.getInstance();
            name = new ComputableLiveData<String>() {
                @Override
                protected String compute() {
                    return me.getName();
                }
            };
        }
    }
}
