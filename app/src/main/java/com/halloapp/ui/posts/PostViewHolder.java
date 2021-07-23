package com.halloapp.ui.posts;

import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.Debug;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.media.DownloadMediaTask;
import com.halloapp.media.UploadMediaTask;
import com.halloapp.ui.ContentViewHolderParent;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.PostOptionsBottomSheetDialogFragment;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.groups.GroupTheme;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.util.DialogFragmentUtils;
import com.halloapp.util.Rtl;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.SeenDetectorLayout;
import com.halloapp.xmpp.Connection;

import me.relex.circleindicator.CircleIndicator3;

public class PostViewHolder extends ViewHolderWithLifecycle {

    private final ImageView avatarView;
    private final TextView nameView;
    private final TextView groupView;
    private final TextView timeView;
    private final ImageView statusView;
    private final View progressView;
    private final View moreOptionsView;
    private final ViewPager2 mediaPagerView;
    private final CircleIndicator3 mediaPagerIndicator;
    private final LimitingTextView textView;
    private final PostAttributionLayout postHeader;
    protected final MediaPagerAdapter mediaPagerAdapter;
    private final View footer;
    private final CardView cardView;
    final View footerSpacing;

    final PostViewHolderParent parent;
    private final Connection connection;
    private final FileStore fileStore;
    private final ContentDb contentDb;
    Post post;

    private boolean showGroupName;
    private @ColorRes int cardBgColor;

    public abstract static class PostViewHolderParent implements MediaPagerAdapter.MediaPagerAdapterParent, ContentViewHolderParent {
        public boolean shouldOpenProfileOnNamePress() {
            return true;
        }
        public abstract void showDialogFragment(@NonNull DialogFragment dialogFragment);
    }

    public void setShowGroupName(boolean visible) {
        this.showGroupName = visible;
    }

    public void setCardBackgroundColor(@ColorRes int bgColor) {
        if (this.cardBgColor != bgColor) {
            this.cardBgColor = bgColor;
            if (cardBgColor == 0) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.getContext(), R.color.post_card_background));
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.getContext(), cardBgColor));
            }
        }
    }

    PostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView);

        this.parent = parent;

        this.connection = Connection.getInstance();
        this.fileStore = FileStore.getInstance();
        this.contentDb = ContentDb.getInstance();

        cardView = itemView.findViewById(R.id.card_view);
        postHeader = itemView.findViewById(R.id.post_header);
        avatarView = itemView.findViewById(R.id.avatar);
        nameView = itemView.findViewById(R.id.name);
        groupView = itemView.findViewById(R.id.group_name);
        timeView = itemView.findViewById(R.id.time);
        statusView = itemView.findViewById(R.id.status);
        progressView = itemView.findViewById(R.id.progress);
        moreOptionsView = itemView.findViewById(R.id.more_options);
        mediaPagerView = itemView.findViewById(R.id.media_pager);
        mediaPagerIndicator = itemView.findViewById(R.id.media_pager_indicator);
        textView = itemView.findViewById(R.id.text);
        footerSpacing = itemView.findViewById(R.id.footer_spacing);
        footer = itemView.findViewById(R.id.post_footer);

        if (statusView != null) {
            statusView.setOnClickListener(v -> {
                if (post.senderUserId.isMe()) {
                    UploadMediaTask.restartUpload(post, fileStore, contentDb, connection);
                } else {
                    DownloadMediaTask.download(fileStore, contentDb, post);
                }
            });
        }

        if (moreOptionsView != null) {
            moreOptionsView.setOnClickListener(v -> {
                parent.showDialogFragment(PostOptionsBottomSheetDialogFragment.newInstance(post.id, post.isArchived));
            });
        }

        if (mediaPagerView != null) {
            mediaPagerAdapter = new MediaPagerAdapter(parent, itemView.getContext().getResources().getDimension(R.dimen.post_media_radius), 0);
            mediaPagerAdapter.setOffscreenPlayerLimit(1);
            mediaPagerView.setAdapter(mediaPagerAdapter);
            mediaPagerView.setOffscreenPageLimit(1);
            mediaPagerView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    if (post == null) {
                        return;
                    }
                    if (position == 0) {
                        parent.getMediaPagerPositionMap().remove(post.rowId);
                    } else {
                        parent.getMediaPagerPositionMap().put(post.rowId, position);
                    }
                    mediaPagerAdapter.refreshPlayers(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        } else {
            mediaPagerAdapter = null;
        }

        final SeenDetectorLayout postContentLayout = itemView.findViewById(R.id.post_content);
        postContentLayout.setOnSeenListener(() -> {
            if (post.shouldSendSeenReceipt()) {
                post.seen = Post.SEEN_YES_PENDING;
                ContentDb.getInstance().setIncomingPostSeen(post.senderUserId, post.id);
            }
        });

        if (textView != null) {
            textView.setOnReadMoreListener((view, limit) -> {
                parent.getTextLimits().put(post.rowId, limit);
                return false;
            });
        }
    }

    @CallSuper
    public void bindTo(@NonNull Post post) {

        this.post = post;
        parent.getAvatarLoader().load(avatarView, post.senderUserId, parent.shouldOpenProfileOnNamePress());
        if (post.isOutgoing()) {
            nameView.setText(nameView.getContext().getString(R.string.me));
        } else {
            parent.getContactLoader().load(nameView, post.senderUserId, parent.shouldOpenProfileOnNamePress());
        }
        if (showGroupName) {
            if (post.getParentGroup() != null) {
                postHeader.setGroupAttributionVisible(true);
                parent.getChatLoader().load(groupView, new ViewDataLoader.Displayer<View, Chat>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable Chat result) {
                        if (result != null) {
                            GroupTheme theme = GroupTheme.getTheme(result.theme);
                            groupView.setText(result.name);
                            groupView.setOnClickListener(v -> {
                                ChatId chatId = result.chatId;
                                if (!(chatId instanceof GroupId)) {
                                    Log.w("Cannot open group feed for non-group " + chatId);
                                    return;
                                }
                                parent.startActivity(ViewGroupFeedActivity.viewFeed(groupView.getContext(), (GroupId)chatId));
                            });
                        } else {
                            Log.e("PostViewHolder/bind failed to load chat " + post.getParentGroup());
                        }
                    }

                    @Override
                    public void showLoading(@NonNull View view) {
                        groupView.setText("");
                    }
                }, post.getParentGroup());
            } else {
                groupView.setClickable(false);
                parent.getChatLoader().cancel(groupView);
                postHeader.setGroupAttributionVisible(false);
            }
        } else {
            postHeader.setGroupAttributionVisible(false);
        }
        if (post.transferred == Post.TRANSFERRED_NO) {
            if (post.isTransferFailed()) {
                progressView.setVisibility(View.GONE);
                statusView.setVisibility(View.VISIBLE);
                statusView.setImageResource(R.drawable.ic_error);
                statusView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(statusView.getContext(), R.color.design_default_color_error)));
            } else {
                progressView.setVisibility(View.VISIBLE);
                statusView.setVisibility(View.GONE);
            }
        } else {
            progressView.setVisibility(View.GONE);
            statusView.setVisibility(View.GONE);
        }
        if (moreOptionsView != null) {
            if (shouldShowMoreOptions()) {
                moreOptionsView.setVisibility(View.VISIBLE);
            } else {
                moreOptionsView.setVisibility(View.GONE);
            }
        }
        TimeFormatter.setTimePostsFormat(timeView, post.timestamp);
        parent.getTimestampRefresher().scheduleTimestampRefresh(post.timestamp);
        if (BuildConfig.DEBUG) {
            timeView.setOnLongClickListener(v -> {
                Debug.askSendLogsWithId(timeView.getContext(), post.id);
                return false;
            });
        }

        final boolean noCaption = TextUtils.isEmpty(post.text);

        final Integer selPos = parent.getMediaPagerPositionMap().get(post.rowId);
        if (!post.media.isEmpty()) {
            mediaPagerAdapter.setAllowSaving(shouldShowMoreOptions());
            mediaPagerAdapter.setMedia(post.media);
            if (!post.id.equals(mediaPagerAdapter.getContentId())) {
                Log.i("PostViewHolder.bindTo: post " + post.id + " has " + post.media.size() + " media: " + post.media);
                mediaPagerAdapter.setContentId(post.id);
                final int defaultMediaInset = mediaPagerView.getResources().getDimensionPixelSize(R.dimen.media_pager_child_padding);
                if (post.media.size() > 1) {
                    mediaPagerIndicator.setVisibility(View.VISIBLE);
                    mediaPagerIndicator.setViewPager(mediaPagerView);
                    mediaPagerAdapter.setMediaInset(defaultMediaInset, defaultMediaInset, defaultMediaInset, defaultMediaInset);
                } else {
                    mediaPagerAdapter.setMediaInset(defaultMediaInset, defaultMediaInset, defaultMediaInset, 0);
                    mediaPagerIndicator.setVisibility(View.GONE);
                }
                mediaPagerView.setCurrentItem(selPos == null ? (Rtl.isRtl(mediaPagerView.getContext()) ? post.media.size() - 1 : 0) : selPos, false);
                mediaPagerView.setNestedScrollingEnabled(false);
                mediaPagerView.setTag(MediaPagerAdapter.getPagerTag(post.id));
            }
        }
        if (textView != null) {
            final Integer textLimit = parent.getTextLimits().get(post.rowId);
            textView.setLineLimit(textLimit != null ? textLimit :
                    (post.media.isEmpty() ? Constants.TEXT_POST_LINE_LIMIT : Constants.MEDIA_POST_LINE_LIMIT));
            textView.setLineLimitTolerance(textLimit != null ? Constants.POST_LINE_LIMIT_TOLERANCE : 0);
            if (post.text != null) {
                parent.getTextContentLoader().load(textView, post);
            } else {
                textView.setText("");
            }

            if (noCaption) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textView.getContext().getResources().getDimension(
                        (post.text.length() < 180 && post.media.isEmpty()) ? R.dimen.post_text_size_large : R.dimen.post_text_size));
            }
        }

        if (post.isRetracted()) {
            footer.setVisibility(View.GONE);
        } else {
            footer.setVisibility(View.VISIBLE);
        }
    }

    private boolean shouldShowMoreOptions() {
        if (post.senderUserId.isMe()) {
            return true;
        }
        return post.getParentGroup() != null && !post.media.isEmpty();
    }

    public void selectMedia(int index) {
        if (mediaPagerView != null) {
            mediaPagerView.setCurrentItem(index, false);
        }
    }
}

