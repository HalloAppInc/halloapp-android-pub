package com.halloapp.ui.share;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.halloapp.R;

import java.util.ArrayList;
import java.util.List;

public class ForwardActivity extends ShareActivity {

    public static final String RESULT_DESTINATIONS = "destinations";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TextView nextView = findViewById(R.id.next);
        nextView.setText(R.string.send);
    }

    protected @StringRes int getTitleText() {
        return R.string.forward_title;
    }

    @Override
    protected boolean showOnlyChats() {
        return true;
    }

    @Override
    protected void next() {
        List<ShareDestination> destinations = viewModel.selectionList.getValue();
        if (destinations == null || destinations.size() == 0) {
            return;
        }
        Intent data = new Intent();
        data.putExtra(RESULT_DESTINATIONS, new ArrayList<>(destinations));
        setResult(RESULT_OK, data);
        finish();
    }

}
