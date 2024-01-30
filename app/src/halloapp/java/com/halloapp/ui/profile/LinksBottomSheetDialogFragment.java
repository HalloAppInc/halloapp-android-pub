package com.halloapp.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.SocialLink;
import com.halloapp.ui.HalloBottomSheetDialogFragment;

import java.util.ArrayList;

public class LinksBottomSheetDialogFragment extends HalloBottomSheetDialogFragment {

    private static final String ARG_LINKS = "links";

    public static LinksBottomSheetDialogFragment newInstance(@NonNull ArrayList<SocialLink> list) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_LINKS, list);
        LinksBottomSheetDialogFragment dialogFragment =  new LinksBottomSheetDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        ArrayList<SocialLink> links = args.getParcelableArrayList(ARG_LINKS);

        final View view = inflater.inflate(R.layout.links_bottom_sheet, container, false);
        final RecyclerView linksView = view.findViewById(R.id.links);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        linksView.setLayoutManager(layoutManager);

        LinksAdapter adapter = new LinksAdapter();
        linksView.setAdapter(adapter);
        adapter.setItems(links);

        return view;
    }

    @Override
    public int getTheme() {
        return R.style.RoundedBottomSheetDialog;
    }
}
