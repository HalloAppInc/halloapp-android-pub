package com.halloapp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Reaction;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.List;

public class ReactionListBottomSheetDialogFragment extends HalloBottomSheetDialogFragment {

    private static final String ARG_CONTENT_ID = "content_id";

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private final ContactLoader contactLoader = new ContactLoader(userId -> {
        startActivity(ViewProfileActivity.viewProfile(requireContext(), userId));
        return null;
    });

    public static ReactionListBottomSheetDialogFragment newInstance(@NonNull String contentId) {
        Bundle args = new Bundle();
        args.putString(ARG_CONTENT_ID, contentId);
        ReactionListBottomSheetDialogFragment dialogFragment = new ReactionListBottomSheetDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = requireArguments();
        String contentId = args.getString(ARG_CONTENT_ID);

        final View view = inflater.inflate(R.layout.reactions_list_bottom_sheet, container, false);

        final RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final ReactionsAdapter adapter = new ReactionsAdapter();
        recyclerView.setAdapter(adapter);

        BgWorkers.getInstance().execute(() -> {
            List<Reaction> reactions = ContentDb.getInstance().getReactions(contentId);
            Log.i("Received reactions list " + reactions + " for content item " + contentId);
            adapter.submitList(reactions);
        });
        return view;
    }

    class ReactionViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarView;
        TextView nameView;
        TextView timestampView;
        TextView emojiView;

        public ReactionViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            timestampView = itemView.findViewById(R.id.timestamp);
            emojiView = itemView.findViewById(R.id.reaction);
        }

        public void bindTo(@NonNull Reaction reaction) {
            avatarLoader.load(avatarView, reaction.senderUserId);
            contactLoader.load(nameView, reaction.senderUserId);
            timestampView.setText(TimeFormatter.formatRelativePostTime(timestampView.getContext(), reaction.timestamp));
            emojiView.setText(reaction.reactionType);
        }
    }

    class ReactionsAdapter extends RecyclerView.Adapter<ReactionViewHolder> {
        private List<Reaction> reactions = new ArrayList<>();

        public void submitList(List<Reaction> reactions) {
            this.reactions = reactions;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ReactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ReactionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reactions_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ReactionViewHolder holder, int position) {
            holder.bindTo(reactions.get(position));
        }

        @Override
        public int getItemCount() {
            return reactions.size();
        }
    }
}
