package com.halloapp.ui.groups;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.Constants;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.content.VoiceNotePost;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.MediaPaletteThumbnailLoader;
import com.halloapp.ui.AdapterWithLifecycle;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.FlatCommentsActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloFragment;
import com.halloapp.ui.HeaderFooterAdapter;
import com.halloapp.ui.MainNavFragment;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.SystemMessageTextResolver;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.posts.PostDiffCallback;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.GlobalUI;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.FabExpandOnScrollListener;
import com.halloapp.widget.HorizontalSpaceDecoration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GroupsFragment extends HalloFragment implements MainNavFragment {

    private static final int REQUEST_CODE_OPEN_GROUP = 1;
    private static final int REQUEST_CODE_NEW_POST = 2;

    private static final float MINIMUM_CARD_VISIBILITY = 0.1f;

    private GroupsAdapter adapter;

    private GlobalUI globalUI;
    private ContactLoader contactLoader;
    private TextContentLoader textContentLoader;
    private UnseenGroupPostsLoader unseenGroupPostsLoader;
    private MediaPaletteThumbnailLoader mediaThumbnailLoader;
    private SystemMessageTextResolver systemMessageTextResolver;
    private AvatarLoader avatarLoader;
    private AudioDurationLoader audioDurationLoader;

    private GroupListViewModel viewModel;

    private RecyclerView groupsView;
    private LinearLayoutManager layoutManager;

    private View emptyView;
    private TextView emptyViewMessage;

    private ActionMode actionMode;

    private MenuItem searchMenuItem;

    private final HashMap<GroupId, Group> selectedGroups = new HashMap<>();
    private final HashMap<String, Integer> textPostColorMapping = new HashMap<>();

    private int textPostColorIndex = 0;
    private static @ColorRes int[] textPostColors = new int[] {
            R.color.group_text_post_1,
            R.color.group_text_post_2,
            R.color.group_text_post_3,
            R.color.group_text_post_4,
            R.color.group_text_post_5,
            R.color.group_text_post_6,
            R.color.group_text_post_7,
            R.color.group_text_post_8,
            R.color.group_text_post_9,
            R.color.group_text_post_10,
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_OPEN_GROUP: {
                if (resultCode == Activity.RESULT_OK) {
                    if (searchMenuItem != null) {
                        searchMenuItem.collapseActionView();
                    }
                }
                break;
            }
            case REQUEST_CODE_NEW_POST: {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.refreshAll();
                    groupsView.scrollToPosition(0);
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        avatarLoader = AvatarLoader.getInstance();
        globalUI = GlobalUI.getInstance();
        contactLoader = new ContactLoader(userId -> {
            startActivity(ViewProfileActivity.viewProfile(requireContext(), userId));
            return null;
        });
        textContentLoader = new TextContentLoader();
        unseenGroupPostsLoader = new UnseenGroupPostsLoader();
        systemMessageTextResolver = new SystemMessageTextResolver(contactLoader);
        mediaThumbnailLoader = new MediaPaletteThumbnailLoader(requireContext(), getResources().getDimensionPixelSize(R.dimen.groups_v2_card_height));
        audioDurationLoader = new AudioDurationLoader(requireContext());

        Notifications.getInstance(requireContext()).clearNewGroupNotification();
        Notifications.getInstance(requireContext()).clearRemovedFromGroupNotification();
    }

    @Override
    public void resetScrollPosition() {
        layoutManager.scrollToPosition(0);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.chat_list_menu, menu);
        searchMenuItem = menu.findItem(R.id.menu_search);
        final MenuItem closeMenuItem = menu.findItem(R.id.menu_clear);
        final SearchView searchView = (SearchView) searchMenuItem.getActionView();

        closeMenuItem.setVisible(false);
        ImageView closeBtn = searchView.findViewById(R.id.search_close_btn);
        closeBtn.setEnabled(false);
        closeBtn.setImageDrawable(null);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String text) {
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(final String text) {
                adapter.getFilter().filter(text);
                closeMenuItem.setVisible(!TextUtils.isEmpty(text));
                return false;
            }
        });
        closeMenuItem.setOnMenuItemClickListener(item -> {
            searchView.setQuery("", false);
            return true;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        Log.i("GroupsV2Fragment.onCreateView");
        final View root = inflater.inflate(R.layout.fragment_groups_v2, container, false);
        groupsView = root.findViewById(R.id.groups);
        emptyView = root.findViewById(android.R.id.empty);
        emptyViewMessage = root.findViewById(R.id.empty_text);

        Preconditions.checkNotNull((SimpleItemAnimator)groupsView.getItemAnimator()).setSupportsChangeAnimations(false);

        final Point point = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getSize(point);
        int screenWidth = point.x;

        int cardWidth = getResources().getDimensionPixelSize(R.dimen.groups_v2_card_width);
        int cardHeight = getResources().getDimensionPixelSize(R.dimen.groups_v2_card_height);
        int cardSpacing = getResources().getDimensionPixelSize(R.dimen.groups_v2_card_horizontal_space);

        int remainingWidth = (screenWidth - cardSpacing) % (cardWidth + cardSpacing);
        int numCards = (screenWidth - cardSpacing) / (cardWidth + cardSpacing);
        float ratio = 1f;
        if (remainingWidth < MINIMUM_CARD_VISIBILITY * cardWidth) {
            float sizeIncreaseAmount = (MINIMUM_CARD_VISIBILITY * cardWidth) - remainingWidth;
            ratio = ((numCards * cardWidth) - sizeIncreaseAmount) / (numCards * cardWidth);
        } else if (remainingWidth > cardWidth - cardSpacing) {
            float sizeIncreaseAmount = ((MINIMUM_CARD_VISIBILITY * cardWidth) + cardSpacing + cardWidth) - remainingWidth;
            float newCardWidth = cardWidth - (sizeIncreaseAmount / (numCards + 1));
            ratio = newCardWidth / cardWidth;
        }
        cardWidth *= ratio;
        cardHeight *= ratio;

        adapter = new GroupsAdapter(cardWidth, cardHeight);
        layoutManager = new LinearLayoutManager(getContext());
        groupsView.setLayoutManager(layoutManager);
        groupsView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(GroupListViewModel.class);
        if (viewModel.getSavedScrollState() != null) {
            layoutManager.onRestoreInstanceState(viewModel.getSavedScrollState());
        }
        viewModel.groupsList.getLiveData().observe(getViewLifecycleOwner(), chats -> {
            adapter.setGroups(chats);
            emptyView.setVisibility(chats.size() == 0 ? View.VISIBLE : View.GONE);
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        groupsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()));
        groupsView.addOnScrollListener(new FabExpandOnScrollListener((AppCompatActivity) requireActivity()));
    }

    @Override
    public void onDestroyView() {
        if (viewModel != null && layoutManager != null) {
            viewModel.saveScrollState(layoutManager.onSaveInstanceState());
        }
        endActionMode();
        super.onDestroyView();
    }

    private void endActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void updateGroupSelection(Group group) {
        GroupId groupId = group.groupId;
        if (selectedGroups.containsKey(groupId)) {
            selectedGroups.remove(groupId);
        } else {
            selectedGroups.put(groupId, group);
        }
        adapter.notifyDataSetChanged();
        if (selectedGroups.isEmpty()) {
            endActionMode();
            return;
        }
        if (actionMode == null) {
            actionMode = ((HalloActivity) requireActivity()).startSupportActionMode(new ActionMode.Callback() {

                private int statusBarColor;
                private int previousVisibility;

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.groups_menu, menu);
                    statusBarColor = requireActivity().getWindow().getStatusBarColor();

                    requireActivity().getWindow().setStatusBarColor(requireContext().getResources().getColor(R.color.color_secondary));
                    previousVisibility = requireActivity().getWindow().getDecorView().getSystemUiVisibility();
                    //noinspection InlinedApi
                    requireActivity().getWindow().getDecorView().setSystemUiVisibility(previousVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    MenuItem leaveItem = menu.findItem(R.id.leave_group);
                    leaveItem.setTitle(getResources().getQuantityString(R.plurals.leave_group_plural, selectedGroups.size()));
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.delete) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        builder.setMessage(requireContext().getResources().getQuantityString(R.plurals.delete_groups_confirmation, selectedGroups.size(), selectedGroups.size()));
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                            for (Group group : selectedGroups.values()) {
                                ContentDb.getInstance().deleteGroup(group.groupId);
                            }
                            endActionMode();
                        });
                        builder.setNegativeButton(R.string.no, null);
                        builder.show();
                        return true;
                    } else if (item.getItemId() == R.id.view_group_info) {
                        for (ChatId chat : selectedGroups.keySet()) {
                            if (chat instanceof GroupId) {
                                startActivity(FeedGroupInfoActivity.viewGroup(requireContext(), (GroupId) chat));
                                break;
                            }
                        }
                        endActionMode();
                    } else if (item.getItemId() == R.id.new_post) {
                        for (ChatId chat : selectedGroups.keySet()) {
                            if (chat instanceof GroupId) {
                                Intent intent = new Intent(requireContext(), ContentComposerActivity.class);
                                intent.putExtra(ContentComposerActivity.EXTRA_GROUP_ID, group.groupId);
                                startActivityForResult(intent, REQUEST_CODE_NEW_POST);
                                break;
                            }
                        }
                        endActionMode();
                    } else if (item.getItemId() == R.id.leave_group) {
                        List<GroupId> selectedGroups = new ArrayList<>();
                        for (ChatId chatId : GroupsFragment.this.selectedGroups.keySet()) {
                            if (chatId instanceof GroupId) {
                                selectedGroups.add((GroupId) chatId);
                            }
                        }
                        final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                        builder.setMessage(requireContext().getResources().getQuantityString(R.plurals.leave_multiple_groups_confirmation, selectedGroups.size(), selectedGroups.size()));
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                            endActionMode();
                            leaveGroups(selectedGroups);
                        });
                        builder.setNegativeButton(R.string.no, null);
                        builder.show();
                    }
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    adapter.notifyDataSetChanged();
                    selectedGroups.clear();
                    actionMode = null;
                    requireActivity().getWindow().setStatusBarColor(statusBarColor);
                    requireActivity().getWindow().getDecorView().setSystemUiVisibility(previousVisibility);
                }
            });
        }
        if (actionMode == null) {
            Log.e("ChatsFragment/updateChatSelection null actionmode");
            return;
        }
        boolean hasActiveGroup = false;
        for (Group selectedGroup : selectedGroups.values()) {
            if (selectedGroup.isActive) {
                hasActiveGroup = true;
                break;
            }
        }
        actionMode.getMenu().findItem(R.id.delete).setVisible(!hasActiveGroup);
        actionMode.getMenu().findItem(R.id.leave_group).setVisible(hasActiveGroup);
        actionMode.getMenu().findItem(R.id.new_post).setVisible(selectedGroups.size() == 1);
        actionMode.getMenu().findItem(R.id.view_group_info).setVisible(selectedGroups.size() == 1);
        actionMode.setTitle(Integer.toString(selectedGroups.size()));
    }

    private void leaveGroups(@NonNull Collection<GroupId> groupIds) {
        ProgressDialog dialog = ProgressDialog.show(getContext(), "", requireContext().getResources().getQuantityString(R.plurals.leave_groups_progress, groupIds.size()));
        long startTime = System.currentTimeMillis();
        viewModel.leaveGroup(groupIds).observe(getViewLifecycleOwner(),
                success -> {
                    if (success != null) {
                        long dT = System.currentTimeMillis() - startTime;
                        if (dT >= Constants.MINIMUM_PROGRESS_DIALOG_TIME_MILLIS) {
                            dialog.cancel();
                        } else {
                            globalUI.postDelayed(dialog::cancel, Constants.MINIMUM_PROGRESS_DIALOG_TIME_MILLIS - dT);
                        }
                    }
                });
    }

    private class GroupsFilter extends FilterUtils.ItemFilter<Group> {

        GroupsFilter(@NonNull List<Group> groups) {
            super(groups);
        }

        @Override
        protected String itemToString(Group chat) {
            return chat.name;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            final List<Group> filteredContacts = (List<Group>) results.values;
            adapter.setFilteredGroups(filteredContacts, constraint);
            if (filteredContacts.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(constraint)) {
                    emptyViewMessage.setText(R.string.groups_page_empty);
                } else {
                    emptyViewMessage.setText(getString(R.string.groups_search_empty, constraint));
                }
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    private class PostPreviewViewHolder extends ViewHolderWithLifecycle {

        private final TextView nameView;
        private final TextView previewTextView;
        private final ImageView previewImageView;
        private final ImageView mediaIconView;
        private final View videoIconView;
        private final View albumIconView;
        private final View commentsIndicator;
        private final View cardView;
        private final View voiceNoteContainer;
        private final TextView voiceNoteDuration;
        private final ImageView voiceAvatarView;
        private final View commentBar;

        private final View imageProtectionTop;
        private final View imageProtectionBottom;

        private final View newIndicator;

        private Post post;

        public PostPreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card);
            nameView = itemView.findViewById(R.id.name);
            previewTextView = itemView.findViewById(R.id.preview_text);
            previewImageView = itemView.findViewById(R.id.preview_image);
            mediaIconView = itemView.findViewById(R.id.media_icon);
            albumIconView = itemView.findViewById(R.id.album_icon);
            videoIconView = itemView.findViewById(R.id.video_icon);
            commentsIndicator = itemView.findViewById(R.id.comments_indicator);
            voiceNoteContainer = itemView.findViewById(R.id.voice_note_container);
            voiceNoteDuration = itemView.findViewById(R.id.seek_time);
            voiceAvatarView = itemView.findViewById(R.id.voice_note_avatar);
            imageProtectionBottom = itemView.findViewById(R.id.bottom_protection_bar);
            imageProtectionTop = itemView.findViewById(R.id.top_protection_bar);
            newIndicator = itemView.findViewById(R.id.new_indicator);
            commentBar = itemView.findViewById(R.id.comment_bar);

            cardView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    float radius = itemView.getResources().getDimension(R.dimen.group_post_preview_card_radius);
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
            cardView.setClipToOutline(true);
            commentBar.setOnClickListener(v -> {
                final Intent intent = FlatCommentsActivity.viewComments(itemView.getContext(), post.id, post.senderUserId);
                intent.putExtra(FlatCommentsActivity.EXTRA_SHOW_KEYBOARD, post.commentCount == 0);
                startActivity(intent);
            });
            itemView.setOnClickListener(v -> {
                if (post.seen == Post.SEEN_NO) {
                    ContentDb.getInstance().setIncomingPostSeen(post.senderUserId, post.id, post.getParentGroup());
                }
                final Intent intent = ViewGroupFeedActivity.viewFeed(itemView.getContext(), post.getParentGroup(), post.timestamp);
                startActivity(intent);
            });
        }

        public void bindTo(Post post) {
            this.post = post;
            contactLoader.load(nameView, post.senderUserId);
            audioDurationLoader.cancel(voiceNoteDuration);

            List<Media> media = post.getMedia();
            if (media.isEmpty()) {
                previewImageView.setTransitionName(null);
                previewImageView.setVisibility(View.GONE);
                mediaIconView.setVisibility(View.GONE);
                int bgColor;
                if (textPostColorMapping.containsKey(post.id)) {
                    bgColor = textPostColorMapping.get(post.id);
                } else {
                    bgColor = ContextCompat.getColor(cardView.getContext(), textPostColors[textPostColorIndex % textPostColors.length]);
                    textPostColorIndex++;
                    textPostColorMapping.put(post.id, bgColor);
                }
                setBarColor(bgColor);
                cardView.setBackgroundColor(bgColor);
                if (post instanceof VoiceNotePost) {
                    previewTextView.setVisibility(View.GONE);
                    voiceNoteContainer.setVisibility(View.VISIBLE);
                    audioDurationLoader.load(voiceNoteDuration, post.media.get(0));
                    avatarLoader.load(voiceAvatarView, post.senderUserId, false);
                    textContentLoader.cancel(previewTextView);
                } else {
                    avatarLoader.cancel(voiceAvatarView);
                    textContentLoader.load(previewTextView, post);
                    previewTextView.setText(post.text);
                    previewTextView.setVisibility(View.VISIBLE);
                    voiceNoteContainer.setVisibility(View.GONE);
                }
                albumIconView.setVisibility(View.GONE);
                videoIconView.setVisibility(View.GONE);
            } else {
                textContentLoader.cancel(previewTextView);
                avatarLoader.cancel(voiceAvatarView);
                clearBackgroundColor();
                voiceNoteContainer.setVisibility(View.GONE);
                previewImageView.setVisibility(View.VISIBLE);
                previewTextView.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(post.text)) {
                    mediaIconView.setVisibility(View.GONE);
                } else if (post instanceof VoiceNotePost) {
                    mediaIconView.setImageResource(R.drawable.ic_group_mic);
                    mediaIconView.setVisibility(View.VISIBLE);
                } else {
                    mediaIconView.setVisibility(View.GONE);
                }
                if (media.size() > 1) {
                    albumIconView.setVisibility(View.VISIBLE);
                } else {
                    albumIconView.setVisibility(View.GONE);
                }
                if (media.get(0).type == Media.MEDIA_TYPE_VIDEO) {
                    videoIconView.setVisibility(View.VISIBLE);
                } else {
                    videoIconView.setVisibility(View.GONE);
                }
                previewImageView.setTransitionName(MediaPagerAdapter.getTransitionName(post.id, 0));
                mediaThumbnailLoader.load(previewImageView, media.get(0), palette -> {
                    setBarColor(palette.getDominantColor(0xDC000000));
                });
            }

            if (post.unseenCommentCount > 0) {
                commentsIndicator.setVisibility(View.VISIBLE);
                commentsIndicator.setBackgroundResource(R.drawable.new_comments_indicator);
            } else if (post.commentCount > 0) {
                commentsIndicator.setVisibility(View.VISIBLE);
                commentsIndicator.setBackgroundResource(R.drawable.old_groups_comments_indicator);
            } else {
                commentsIndicator.setVisibility(View.INVISIBLE);
            }

            newIndicator.setVisibility(post.seen == Post.SEEN_NO ? View.VISIBLE : View.GONE);
        }

        private void clearBackgroundColor() {
            cardView.setBackgroundColor(ContextCompat.getColor(cardView.getContext(), R.color.card_background));
        }

        private void setBarColor(@ColorInt int color) {
            color = ColorUtils.setAlphaComponent(color, 220);
            imageProtectionBottom.setBackgroundColor(color);
            imageProtectionTop.setBackgroundColor(color);
        }
    }

    private class PostsPreviewAdapter extends HeaderFooterAdapter<Post> {

        private final AsyncPagedListDiffer<Post> differ;

        private int cardWidth;
        private int cardHeight;
        private int maxLines = 5;

        public PostsPreviewAdapter(@NonNull HeaderFooterAdapterParent parent, int cardWidth, int cardHeight) {
            super(parent);
            this.cardHeight = cardHeight;
            this.cardWidth = cardWidth;
            float textSize = getResources().getDimensionPixelSize(R.dimen.groups_v2_text_size) * 1.3f;
            maxLines = (int)((0.6f * cardHeight)/textSize);
            setHasStableIds(true);
            final AdapterListUpdateCallback adapterCallback = new AdapterListUpdateCallback(this);
            final ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {

                public void onInserted(int position, int count) {
                    adapterCallback.onInserted(position + getHeaderCount(), count);
                }

                public void onRemoved(int position, int count) {
                    adapterCallback.onRemoved(position + getHeaderCount(), count);
                }

                public void onMoved(int fromPosition, int toPosition) {
                    int headerCount = getHeaderCount();
                    adapterCallback.onMoved(fromPosition + headerCount, toPosition + headerCount);
                }

                public void onChanged(int position, int count, @Nullable Object payload) {
                    adapterCallback.onChanged(position + getHeaderCount(), count, payload);
                }
            };
            differ = new AsyncPagedListDiffer<>(listUpdateCallback, new AsyncDifferConfig.Builder<>(new PostDiffCallback()).build());
            setDiffer(differ);
        }

        @Override
        public long getIdForItem(Post post) {
            return post.rowId;
        }

        @Override
        public int getViewTypeForItem(Post post) {
            return 0;
        }

        @NonNull
        @Override
        public ViewHolderWithLifecycle createViewHolderForViewType(@NonNull ViewGroup parent, int viewType) {
            View container = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_group_post_preview_container, parent, false);
            ViewGroup.LayoutParams layoutParams = container.findViewById(R.id.card).getLayoutParams();
            layoutParams.width = cardWidth;
            layoutParams.height = cardHeight;
            container.setLayoutParams(layoutParams);
            TextView textPreviewView = container.findViewById(R.id.preview_text);
            textPreviewView.setMaxLines(maxLines);
            return new PostPreviewViewHolder(container);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            if (holder instanceof PostPreviewViewHolder) {
                ((PostPreviewViewHolder) holder).bindTo(differ.getItem(position));
            }
        }
    }

    private final HashMap<GroupId, PostsPreviewAdapter> adapterHashMap = new HashMap<>();

    private class GroupsAdapter extends AdapterWithLifecycle<ViewHolderWithLifecycle> implements Filterable {

        private List<Group> groups;
        private List<Group> filteredGroups;
        private CharSequence filterText;
        private List<String> filterTokens;

        private int cardWidth;
        private int cardHeight;

        GroupsAdapter(int cardWidth, int cardHeight) {
            this.cardWidth = cardWidth;
            this.cardHeight = cardHeight;
        }

        void setGroups(@NonNull List<Group> groups) {
            this.groups = groups;
            this.filteredGroups = new ArrayList<>(groups);
            getFilter().filter(filterText);
            notifyDataSetChanged();
        }

        @Override
        public @NonNull ViewHolderWithLifecycle onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.i("GroupsAdapter.onCreateViewHolder " + viewType);
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.group_v2_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            if (position < getFilteredContactsCount()) {
                if (holder instanceof ViewHolder) {
                    ((ViewHolder) holder).bindTo(filteredGroups.get(position), filterTokens);
                }
            }
        }

        @Override
        public int getItemCount() {
            return getFilteredContactsCount();
        }

        void setFilteredGroups(@NonNull List<Group> contacts, CharSequence filterText) {
            this.filteredGroups = contacts;
            this.filterText = filterText;
            this.filterTokens = FilterUtils.getFilterTokens(filterText);
            notifyDataSetChanged();
        }

        private int getFilteredContactsCount() {
            return filteredGroups == null ? 0 : filteredGroups.size();
        }

        @Override
        public Filter getFilter() {
            return new GroupsFilter(groups);
        }

        class ViewHolder extends ViewHolderWithLifecycle {

            final ImageView avatarView;
            final TextView nameView;
            final View addPost;
            final View infoContainer;
            final View selectionView;
            final RecyclerView previewRv;

            private Group group;
            private PostsPreviewAdapter previewAdapter;

            private final Observer<PagedList<Post>> previewObserver;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                addPost = itemView.findViewById(R.id.add_post);
                infoContainer = itemView.findViewById(R.id.info_container);
                selectionView = itemView.findViewById(R.id.selection_background);
                previewRv = itemView.findViewById(R.id.post_rv);
                addPost.setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), ContentComposerActivity.class);
                    intent.putExtra(ContentComposerActivity.EXTRA_GROUP_ID, group.groupId);
                    startActivityForResult(intent, REQUEST_CODE_NEW_POST); 
                });
                previewRv.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                previewRv.addItemDecoration(new HorizontalSpaceDecoration(itemView.getContext().getResources().getDimensionPixelSize(R.dimen.group_post_preview_card_separation)));
                itemView.setOnLongClickListener(v -> {
                    updateGroupSelection(group);
                    return true;
                });
                itemView.setOnClickListener(v -> {
                    if (actionMode == null) {
                        startActivityForResult(ViewGroupFeedActivity.viewFeed(requireContext(), group.groupId), REQUEST_CODE_OPEN_GROUP);
                    } else {
                        updateGroupSelection(group);
                    }
                });
                previewObserver = list -> {
                    if (list.size() == 0) {
                        previewRv.setPadding(previewRv.getPaddingLeft(), previewRv.getPaddingTop(), previewRv.getPaddingRight(), 0);
                    } else {
                        previewRv.setPadding(previewRv.getPaddingLeft(), previewRv.getPaddingTop(), previewRv.getPaddingRight(), previewRv.getResources().getDimensionPixelSize(R.dimen.groups_preview_rv_padding_bottom));
                    }
                    if (previewAdapter != null) {
                        previewAdapter.submitList(list, () -> {
                            previewRv.scrollToPosition(0);
                        });
                    }
                };
            }

            @Override
            public void markAttach() {
                super.markAttach();
                viewModel.getGroupPagedList(group.groupId).observe(getViewLifecycleOwner(), previewObserver);

            }

            @Override
            public void markDetach() {
                super.markDetach();
                viewModel.getGroupPagedList(group.groupId).removeObserver(previewObserver);
            }

            void bindTo(@NonNull Group group, @Nullable List<String> filterTokens) {
                boolean differentChat = this.group == null || !Objects.equals(group.groupId, this.group.groupId);
                this.group = group;
                if (selectedGroups.containsKey(group.groupId)) {
                    selectionView.setVisibility(View.VISIBLE);
                } else {
                    selectionView.setVisibility(View.GONE);
                }
                addPost.setVisibility(group.isActive ? View.VISIBLE : View.GONE);
                avatarLoader.load(avatarView, group.groupId);
                CharSequence name = group.name;
                if (filterTokens != null && !filterTokens.isEmpty()) {
                    CharSequence formattedName = FilterUtils.formatMatchingText(itemView.getContext(), group.name, filterTokens);
                    if (formattedName != null) {
                        name = formattedName;
                    }
                }
                nameView.setText(name);
                PostsPreviewAdapter adapter;
                if (!adapterHashMap.containsKey(group.groupId)) {
                    adapter = new PostsPreviewAdapter(new HeaderFooterAdapter.HeaderFooterAdapterParent() {
                        @NonNull
                        @Override
                        public Context getContext() {
                            return requireContext();
                        }

                        @NonNull
                        @Override
                        public ViewGroup getParentViewGroup() {
                            return previewRv;
                        }
                    }, cardWidth, cardHeight);
                    adapterHashMap.put(group.groupId, adapter);
                } else {
                    adapter = adapterHashMap.get(group.groupId);
                }
                previewAdapter = adapter;
                previewRv.setAdapter(adapter);
            }
        }
    }
}
