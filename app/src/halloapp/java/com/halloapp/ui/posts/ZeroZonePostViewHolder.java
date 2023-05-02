package com.halloapp.ui.posts;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Post;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.contacts.ViewMyNetworkActivity;
import com.halloapp.util.ClipUtils;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.SeenDetectorLayout;

public class ZeroZonePostViewHolder extends ViewHolderWithLifecycle {
    private final PostViewHolder.PostViewHolderParent parent;

    private final TextView timeView;
    private final TextView linkTextView;
    private final View shareLinkButton;
    private final View viewNetworkButton;
    private final View linkTextContainer;
    private final View removeNux;
    private final SeenDetectorLayout seenDetectorLayout;

    private String inviteLink;

    private Post post;

    public ZeroZonePostViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView);

        this.parent = parent;
        this.timeView = itemView.findViewById(R.id.time);
        this.shareLinkButton = itemView.findViewById(R.id.share_link_button);
        this.viewNetworkButton = itemView.findViewById(R.id.view_network_button);
        this.linkTextView = itemView.findViewById(R.id.invite_link_text);
        this.linkTextContainer = itemView.findViewById(R.id.invite_link_container);
        this.removeNux = itemView.findViewById(R.id.remove_zero_zone_nux);
        this.seenDetectorLayout = itemView.findViewById(R.id.seen_detector);
        if (seenDetectorLayout != null) {
            seenDetectorLayout.setOnSeenListener(() -> {
                if (post != null) {
                    ContentDb.getInstance().setZeroZonePostSeen(post.id);
                }
            });
        }
        if (shareLinkButton != null) {
            shareLinkButton.setOnClickListener(v -> {
                String inviteText = shareLinkButton.getContext().getString(R.string.group_invite_link_context, inviteLink);
                shareLinkButton.getContext().startActivity(IntentUtils.createShareTextIntent(inviteText));
            });
        }
        if (this.viewNetworkButton != null) {
            viewNetworkButton.setOnClickListener(v -> {
                Context context = viewNetworkButton.getContext();
                Intent i = new Intent(context, ViewMyNetworkActivity.class);
                context.startActivity(i);
            });
        }
        if (linkTextContainer != null) {
            linkTextContainer.setOnClickListener(v -> {
                ClipUtils.copyToClipboard(inviteLink);
                Toast.makeText(linkTextContainer.getContext(), R.string.invite_link_copied, Toast.LENGTH_SHORT).show();
            });
        }
        if (removeNux != null) {
            removeNux.setOnClickListener(v -> {
                if (post != null) {
                    ContentDb.getInstance().removeZeroZonePost(post);
                }
            });
        }
    }

    public void bindTo(Post post, boolean firstPost) {
        this.post = post;
        if (timeView != null) {
            TimeFormatter.setTimePostsFormat(timeView, post.timestamp);
        }
        parent.getTimestampRefresher().scheduleTimestampRefresh(post.timestamp);

        if (firstPost) {
            itemView.setPadding(itemView.getPaddingLeft(), itemView.getPaddingBottom(), itemView.getPaddingRight(), itemView.getPaddingBottom());
        } else {
            itemView.setPadding(itemView.getPaddingLeft(), 0, itemView.getPaddingRight(), itemView.getPaddingBottom());
        }
        if (removeNux != null) {
            if (firstPost) {
                removeNux.setVisibility(View.GONE);
            } else {
                removeNux.setVisibility(View.VISIBLE);
            }
        }
        if (linkTextView != null && post.getParentGroup() != null) {
            parent.getGroupLoader().load(linkTextView, new ViewDataLoader.Displayer<View, Group>() {
                @Override
                public void showResult(@NonNull View view, @Nullable Group result) {
                    String link;
                    if (result == null || result.inviteToken == null) {
                        link = Constants.GROUP_INVITE_BASE_URL;
                    } else {
                        link = Constants.GROUP_INVITE_BASE_URL + result.inviteToken;
                    }
                    inviteLink = link;
                    linkTextView.setText(link);
                }

                @Override
                public void showLoading(@NonNull View view) {
                    linkTextView.setText(Constants.GROUP_INVITE_BASE_URL);
                }
            }, post.getParentGroup());
        }
    }
}
