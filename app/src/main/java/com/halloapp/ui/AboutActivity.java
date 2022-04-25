package com.halloapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.ui.archive.ArchiveFragment;
import com.halloapp.ui.contacts.ContactHashInfoBottomSheetDialogFragment;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.StringUtils;

public class AboutActivity extends HalloActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_about);


        TextView supportView = findViewById(R.id.support_link);
        supportView.setMovementMethod(LinkMovementMethod.getInstance());
        supportView.setText(StringUtils.replaceLink(supportView.getContext(), Html.fromHtml(getString(R.string.halloapp_about_support)), "support-email", () -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + Constants.SUPPORT_EMAIL)); // only email apps should handle this
            startActivity(intent);
        }));
    }
}
