package com.halloapp.ui.contacts;

import com.halloapp.R;
import com.halloapp.ui.InfoBottomSheetDialogFragment;

public class ContactHashInfoBottomSheetDialogFragment extends InfoBottomSheetDialogFragment {

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.contact_hashing_faq_dialog_title);
    }

    @Override
    protected CharSequence getText() {
        return getString(R.string.contact_hashing_faq_dialog_info);
    }
}
