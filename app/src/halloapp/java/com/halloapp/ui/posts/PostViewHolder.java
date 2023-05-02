package com.halloapp.ui.posts;

import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.text.format.DateUtils;
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
import com.halloapp.UrlPreview;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.groups.MediaProgressLoader;
import com.halloapp.id.GroupId;
import com.halloapp.media.DownloadMediaTask;
import com.halloapp.media.UploadMediaTask;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.ui.ContentViewHolderParent;
import com.halloapp.ui.ExternalSharingBottomSheetDialogFragment;
import com.halloapp.ui.FavoritesInfoDialogFragment;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.PostOptionsBottomSheetDialogFragment;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.groups.GroupContentDecryptStatLoader;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.ui.home.HomeContentDecryptStatLoader;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.LimitingTextView;
import com.halloapp.widget.PostLinkPreviewView;
import com.halloapp.widget.SeenDetectorLayout;
import com.halloapp.widget.ShareExternallyView;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class PostViewHolder extends ViewHolderWithLifecycle {

    private final ImageView avatarView;
    private final TextView nameView;
    private final TextView groupView;
    private final TextView timeView;
    private final TextView decryptStatusView;
    private final ImageView statusView;
    private final View progressView;
    private final View moreOptionsView;
    private final ImageView privacyIndicator;
    protected final ViewPager2 mediaPagerView;
    protected final CircleIndicator3 mediaPagerIndicator;
    private final LimitingTextView textView;
    private final PostAttributionLayout postHeader;
    protected final MediaPagerAdapter mediaPagerAdapter;
    private final PostLinkPreviewView postLinkPreviewView;
    private final View footer;
    private final CardView cardView;
    private final ShareExternallyView shareExternalView;
    private final TextView shareExternalTitle;
    final View footerSpacing;

    final PostViewHolderParent parent;
    private final Connection connection;
    private final FileStore fileStore;
    private final ContentDb contentDb;
    private final GroupContentDecryptStatLoader groupContentDecryptStatLoader;
    private final HomeContentDecryptStatLoader homeContentDecryptStatLoader;
    Post post;

    protected PostFooterViewHolder postFooterViewHolder;

    private boolean canInteract = true;
    private String backupName = null;
    private boolean showGroupName;
    private @ColorRes int cardBgColor;

    public abstract static class PostViewHolderParent implements MediaPagerAdapter.MediaPagerAdapterParent, ContentViewHolderParent {
        public boolean shouldOpenProfileOnNamePress() {
            return true;
        }
        public abstract void showDialogFragment(@NonNull DialogFragment dialogFragment);
        public abstract VoiceNotePlayer getVoiceNotePlayer();
        public abstract MediaProgressLoader getMediaProgressLoader();
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

    public PostViewHolder(@NonNull View itemView, @NonNull PostViewHolderParent parent) {
        super(itemView);

        this.parent = parent;

        this.connection = Connection.getInstance();
        this.fileStore = FileStore.getInstance();
        this.contentDb = ContentDb.getInstance();
        this.groupContentDecryptStatLoader = new GroupContentDecryptStatLoader();
        this.homeContentDecryptStatLoader = new HomeContentDecryptStatLoader();

        cardView = itemView.findViewById(R.id.card_view);
        postHeader = itemView.findViewById(R.id.post_header);
        avatarView = itemView.findViewById(R.id.avatar);
        nameView = itemView.findViewById(R.id.name);
        groupView = itemView.findViewById(R.id.group_name);
        timeView = itemView.findViewById(R.id.time);
        privacyIndicator = itemView.findViewById(R.id.privacy_indicator);
        decryptStatusView = itemView.findViewById(R.id.decrypt_status);
        statusView = itemView.findViewById(R.id.status);
        progressView = itemView.findViewById(R.id.progress);
        moreOptionsView = itemView.findViewById(R.id.more_options);
        mediaPagerView = itemView.findViewById(R.id.media_pager);
        mediaPagerIndicator = itemView.findViewById(R.id.media_pager_indicator);
        textView = itemView.findViewById(R.id.text);
        footerSpacing = itemView.findViewById(R.id.footer_spacing);
        postLinkPreviewView = itemView.findViewById(R.id.link_preview);
        shareExternalView = itemView.findViewById(R.id.share_externally);
        shareExternalTitle = itemView.findViewById(R.id.share_externally_title);
        if (shareExternalView != null) {
            shareExternalView.setListener(new ShareExternallyView.ShareListener() {
                @Override
                public void onOpenShare() {
                    parent.showDialogFragment(ExternalSharingBottomSheetDialogFragment.shareDirectly(post.id, null));
                }

                @Override
                public void onShareTo(ShareExternallyView.ShareTarget target) {
                    int currentItem = 0;
                    if (mediaPagerView != null && mediaPagerView.getVisibility() == View.VISIBLE) {
                        currentItem = mediaPagerView.getCurrentItem();
                    }
                    parent.showDialogFragment(ExternalSharingBottomSheetDialogFragment.shareDirectly(post.id, target.getPackageName(), currentItem));
                }
            });
        }
        if (postLinkPreviewView != null) {
            postLinkPreviewView.setCloseButtonVisible(false);
            postLinkPreviewView.setMediaThumbnailLoader(parent.getMediaThumbnailLoader());
            postLinkPreviewView.setOnClickListener(v -> {
                UrlPreview preview = postLinkPreviewView.getUrlPreview();
                if (preview != null && preview.url != null) {
                    IntentUtils.openUrlInBrowser(postLinkPreviewView, preview.url);
                }
            });
        }
        footer = itemView.findViewById(R.id.post_footer);

        if (statusView != null) {
            statusView.setOnClickListener(v -> {
                if (post.senderUserId.isMe()) {
                    UploadMediaTask.restartUpload(post, fileStore, contentDb, connection);
                } else {
                    DownloadMediaTask.download(post, fileStore, contentDb);
                }
            });
        }

        if (privacyIndicator != null) {
            privacyIndicator.setOnClickListener(v -> {
                parent.showDialogFragment(FavoritesInfoDialogFragment.newInstance(post == null || post.senderUserId.isMe(), nameView.getText().toString()));
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
            if (post.shouldSendSeenReceipt() && canInteract) {
                post.seen = Post.SEEN_YES_PENDING;
                ContentDb.getInstance().setIncomingPostSeen(post.senderUserId, post.id, post.getParentGroup());
            }
            ContentDb.getInstance().markReactionsSeen(post.id);
        });

        if (textView != null) {
            textView.setOnReadMoreListener((view, limit) -> {
                parent.getTextLimits().put(post.rowId, limit);
                return false;
            });
        }
    }

    public void setFooter(@NonNull PostFooterViewHolder postFooterViewHolder) {
        this.postFooterViewHolder = postFooterViewHolder;
    }

    public void setCanInteract(boolean canInteract) {
        this.canInteract = canInteract;
        if (this.postFooterViewHolder != null) {
            this.postFooterViewHolder.setCanInteract(canInteract);
        }
    }

    public void setBackupName(@Nullable String backupName) {
        this.backupName = backupName;
    }

    @CallSuper
    public void bindTo(@NonNull Post post) {
        this.post = post;
        if (postFooterViewHolder != null) {
            postFooterViewHolder.bindTo(post);
        }
        if (shareExternalView != null) {
            if (post.isOutgoing() &&
                    (post.showShareFooter
                            || ((System.currentTimeMillis() - post.timestamp) < DateUtils.MINUTE_IN_MILLIS * 10))) {
                shareExternalView.setVisibility(View.VISIBLE);
                shareExternalTitle.setVisibility(View.VISIBLE);
            } else {
                shareExternalView.setVisibility(View.GONE);
                shareExternalTitle.setVisibility(View.GONE);
            }
        }
        parent.getAvatarLoader().load(avatarView, post.senderUserId, parent.shouldOpenProfileOnNamePress());
        if (post.isOutgoing()) {
            nameView.setText(nameView.getContext().getString(R.string.me));
        } else {
            parent.getContactLoader().load(nameView, post.senderUserId, parent.shouldOpenProfileOnNamePress(), backupName);
        }
        if (PrivacyList.Type.ONLY.equals(post.getAudienceType())) {
            privacyIndicator.setVisibility(View.VISIBLE);
        } else {
            privacyIndicator.setVisibility(View.GONE);
        }
        if (showGroupName) {
            if (post.getParentGroup() != null) {
                postHeader.setGroupAttributionVisible(true);
                parent.getGroupLoader().load(groupView, new ViewDataLoader.Displayer<View, Group>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable Group result) {
                        if (result != null) {
                            groupView.setText(result.name);
                            groupView.setTextColor(result.rowId != -1 ? itemView.getResources().getColor(R.color.primary_text) : itemView.getResources().getColor(R.color.post_group_left));
                            if (result.rowId != -1) {
                                groupView.setOnClickListener(v -> {
                                    GroupId chatId = post.getParentGroup();
                                    parent.startActivity(ViewGroupFeedActivity.viewFeed(groupView.getContext(), chatId));
                                });
                            }
                        }
                    }

                    @Override
                    public void showLoading(@NonNull View view) {
                        groupView.setText("");
                    }
                }, post.getParentGroup());
            } else {
                groupView.setClickable(false);
                parent.getGroupLoader().cancel(groupView);
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
        if (post.expirationMismatch) {
            String expireText;
            if (post.expirationTime == Post.POST_EXPIRATION_NEVER) {
                expireText = itemView.getContext().getString(R.string.never_expires);
            } else {
                String date = DateUtils.formatDateTime(itemView.getContext(), post.expirationTime, DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
                expireText = itemView.getContext().getString(R.string.expires_on, date);
            }
            timeView.append(" • " + expireText);
        }
        parent.getTimestampRefresher().scheduleTimestampRefresh(post.timestamp);
        if (BuildConfig.DEBUG) {
            timeView.setOnLongClickListener(v -> {
                Debug.askSendLogsWithId(this, timeView.getContext(), post.id);
                return false;
            });
        }

        if (decryptStatusView != null && !post.senderUserId.isMe()) {
            if (post.getParentGroup() != null) {
                groupContentDecryptStatLoader.loadPost(this, decryptStatusView, post.id);
            } else {
                homeContentDecryptStatLoader.loadPost(this, decryptStatusView, post.id);
            }
        }

        final boolean noCaption = TextUtils.isEmpty(post.text);

        final Integer selPos = parent.getMediaPagerPositionMap().get(post.rowId);
        List<Media> postMedia = post.getMedia();
        if (!postMedia.isEmpty() && mediaPagerView != null) {
            mediaPagerAdapter.setAllowSaving(shouldShowMoreOptions());
            mediaPagerAdapter.setMedia(postMedia);
            if (!post.id.equals(mediaPagerAdapter.getContentId())) {
                Log.i("PostViewHolder.bindTo: post " + post.id + " has " + postMedia.size() + " media: " + postMedia);
                mediaPagerAdapter.setContentId(post.id);
                final int defaultMediaInset = mediaPagerView.getResources().getDimensionPixelSize(R.dimen.media_pager_child_padding);
                if (postMedia.size() > 1) {
                    mediaPagerIndicator.setVisibility(View.VISIBLE);
                    mediaPagerIndicator.setViewPager(mediaPagerView);
                    mediaPagerAdapter.setMediaInset(defaultMediaInset, defaultMediaInset, defaultMediaInset, defaultMediaInset);
                } else {
                    mediaPagerAdapter.setMediaInset(defaultMediaInset, defaultMediaInset, defaultMediaInset, 0);
                    mediaPagerIndicator.setVisibility(View.GONE);
                }
                mediaPagerView.setCurrentItem(selPos == null ? 0 : selPos, false);
                mediaPagerView.setNestedScrollingEnabled(false);
                mediaPagerView.setTag(MediaPagerAdapter.getPagerTag(post.id));
            }
        }
        if (textView != null) {
            final Integer textLimit = parent.getTextLimits().get(post.rowId);
            textView.setLineLimit(textLimit != null ? textLimit :
                    (postMedia.isEmpty() ? Constants.TEXT_POST_LINE_LIMIT : Constants.MEDIA_POST_LINE_LIMIT));
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
                        (post.text.length() < 180 && postMedia.isEmpty()) ? R.dimen.post_text_size_large : R.dimen.post_text_size));
            }
        }

        if (postLinkPreviewView != null) {
            postLinkPreviewView.updateUrlPreview(post.urlPreview);
        }

        if (post.isRetracted()) {
            footer.setVisibility(View.GONE);
        } else {
            footer.setVisibility(View.VISIBLE);
        }
    }

    private boolean shouldShowMoreOptions() {
        return true;
    }

    public void selectMedia(int index) {
        if (mediaPagerView != null) {
            mediaPagerView.setCurrentItem(index, false);
        }
    }

    public void reloadReactions() {
        if (postFooterViewHolder != null) {
            postFooterViewHolder.reloadReactions();
        }
    }
}

