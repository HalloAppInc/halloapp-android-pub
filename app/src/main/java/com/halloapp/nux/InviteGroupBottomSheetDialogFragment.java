package com.halloapp.nux;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.ui.HalloBottomSheetDialogFragment;
import com.halloapp.util.ClipUtils;
import com.halloapp.util.IntentUtils;

public class InviteGroupBottomSheetDialogFragment extends HalloBottomSheetDialogFragment {

    public static InviteGroupBottomSheetDialogFragment newInstance(@NonNull String inviteToken) {
        InviteGroupBottomSheetDialogFragment instance = new InviteGroupBottomSheetDialogFragment();

        Bundle args = new Bundle();
        args.putString(ARG_INVITE_TOKEN, inviteToken);
        instance.setArguments(args);

        return instance;
    }

    private static final String ARG_INVITE_TOKEN = "invite_token";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.invite_group_bottom_sheet, container, false);

        Bundle args = requireArguments();
        String inviteLink = Constants.GROUP_INVITE_BASE_URL + args.getString(ARG_INVITE_TOKEN);

        TextView inviteLinkText = view.findViewById(R.id.invite_link_text);
        View shareButton = view.findViewById(R.id.share_link_button);
        View inviteLinkContainer = view.findViewById(R.id.invite_link_container);

        inviteLinkText.setText(inviteLink);
        shareButton.setOnClickListener(v -> {
            startActivity(IntentUtils.createShareUrlIntent(inviteLink));
            dismiss();
        });
        inviteLinkContainer.setOnClickListener(v -> {
            ClipUtils.copyToClipboard(inviteLink);
            Toast.makeText(inviteLinkContainer.getContext(), R.string.invite_link_copied, Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
