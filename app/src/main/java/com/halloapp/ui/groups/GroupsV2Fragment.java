package com.halloapp.ui.groups;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.halloapp.Constants;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.content.VoiceNotePost;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.MediaThumbnailLoader;
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
import com.halloapp.util.FilterUtils;
import com.halloapp.util.GlobalUI;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.FabExpandOnScrollListener;
import com.halloapp.widget.HorizontalSpaceDecoration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GroupsV2Fragment extends HalloFragment implements MainNavFragment {

    private static final int REQUEST_CODE_OPEN_GROUP = 1;
    private static final int REQUEST_CODE_NEW_POST = 2;

    private final GroupsAdapter adapter = new GroupsAdapter();

    private GlobalUI globalUI;
    private ContactLoader contactLoader;
    private TextContentLoader textContentLoader;
    private UnseenGroupPostsLoader unseenGroupPostsLoader;
    private MediaThumbnailLoader mediaThumbnailLoader;
    private SystemMessageTextResolver systemMessageTextResolver;
    private AvatarLoader avatarLoader;
    private AudioDurationLoader audioDurationLoader;

    private GroupListV2ViewModel viewModel;

    private RecyclerView groupsView;
    private LinearLayoutManager layoutManager;

    private SwipeRefreshLayout swipeRefreshLayout;

    private View emptyView;
    private TextView emptyViewMessage;
    private TextView newPostsPill;

    private ActionMode actionMode;

    private MenuItem searchMenuItem;

    private final HashMap<GroupId, Group> selectedGroups = new HashMap<>();

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
        contactLoader = new ContactLoader();
        textContentLoader = new TextContentLoader();
        unseenGroupPostsLoader = new UnseenGroupPostsLoader();
        systemMessageTextResolver = new SystemMessageTextResolver(contactLoader);
        mediaThumbnailLoader = new MediaThumbnailLoader(requireContext(), 2 * getResources().getDimensionPixelSize(R.dimen.comment_media_list_height));
        audioDurationLoader = new AudioDurationLoader(requireContext());

        Notifications.getInstance(requireContext()).clearNewGroupNotification();
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
        newPostsPill = root.findViewById(R.id.new_posts_pill);
        emptyView = root.findViewById(android.R.id.empty);
        emptyViewMessage = root.findViewById(R.id.empty_text);
        swipeRefreshLayout = root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.groups_spinner_bar);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.groups_spinner_bg);
        swipeRefreshLayout.setOnRefreshListener(this::delayedRefresh);
        newPostsPill.setOnClickListener(v -> {
            swipeRefreshLayout.setRefreshing(true);
            delayedRefresh();
        });

        Preconditions.checkNotNull((SimpleItemAnimator)groupsView.getItemAnimator()).setSupportsChangeAnimations(false);

        layoutManager = new LinearLayoutManager(getContext());
        groupsView.setLayoutManager(layoutManager);
        groupsView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(GroupListV2ViewModel.class);
        if (viewModel.getSavedScrollState() != null) {
            layoutManager.onRestoreInstanceState(viewModel.getSavedScrollState());
        }
        viewModel.groupsList.getLiveData().observe(getViewLifecycleOwner(), chats -> {
            adapter.setGroups(chats);
            emptyView.setVisibility(chats.size() == 0 ? View.VISIBLE : View.GONE);
            onFinishRefresh();
        });
        viewModel.getNewPostsLiveData().observe(getViewLifecycleOwner(), newPosts -> {
            if (newPosts == null || newPosts == 0) {
                newPostsPill.setVisibility(View.GONE);
            } else {
                newPostsPill.setText(getResources().getQuantityString(R.plurals.new_posts_quantity, newPosts, newPosts));
                newPostsPill.setVisibility(View.VISIBLE);
            }
        });

        return root;
    }

    private void onFinishRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }

    private void delayedRefresh() {
        viewModel.refreshAll();
        groupsView.scrollToPosition(0);
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
                                ContentDb.getInstance().deleteChat(group.groupId);
                            }
                            endActionMode();
                        });
                        builder.setNegativeButton(R.string.no, null);
                        builder.show();
                        return true;
                    } else if (item.getItemId() == R.id.view_group_info) {
                        for (ChatId chat : selectedGroups.keySet()) {
                            if (chat instanceof GroupId) {
                                startActivity(GroupInfoActivity.viewGroup(requireContext(), (GroupId) chat));
                                break;
                            }
                        }
                        endActionMode();
                    } else if (item.getItemId() == R.id.leave_group) {
                        List<GroupId> selectedGroups = new ArrayList<>();
                        for (ChatId chatId : GroupsV2Fragment.this.selectedGroups.keySet()) {
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

        private final ImageView avatarView;
        private final TextView nameView;
        private final TextView previewTextView;
        private final ImageView previewImageView;
        private final View newBorder;
        private final ImageView mediaIconView;
        private final View commentsIndicator;
        private final View cardView;
        private final View voiceNoteContainer;
        private final TextView voiceNoteDuration;

        private final View imageProtectionTop;
        private final View imageProtectionBottom;

        private Post post;

        public PostPreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card);
            nameView = itemView.findViewById(R.id.name);
            avatarView = itemView.findViewById(R.id.avatar);
            previewTextView = itemView.findViewById(R.id.preview_text);
            previewImageView = itemView.findViewById(R.id.preview_image);
            newBorder = itemView.findViewById(R.id.new_border);
            mediaIconView = itemView.findViewById(R.id.media_icon);
            commentsIndicator = itemView.findViewById(R.id.comments_indicator);
            voiceNoteContainer = itemView.findViewById(R.id.voice_note_container);
            voiceNoteDuration = itemView.findViewById(R.id.seek_time);
            imageProtectionBottom = itemView.findViewById(R.id.image_protection_bottom);
            imageProtectionTop = itemView.findViewById(R.id.image_protection_top);

            cardView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    float radius = itemView.getResources().getDimension(R.dimen.group_post_preview_card_radius);
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
            cardView.setClipToOutline(true);
            itemView.setOnClickListener(v -> {
                final Intent intent = FlatCommentsActivity.viewComments(itemView.getContext(), post.id, post.senderUserId);
                intent.putExtra(FlatCommentsActivity.EXTRA_SHOW_KEYBOARD, post.commentCount == 0);
                startActivity(intent);
            });
        }

        public void bindTo(Post post) {
            this.post = post;
            contactLoader.load(nameView, post.senderUserId);
            avatarLoader.load(avatarView, post.senderUserId);
            audioDurationLoader.cancel(voiceNoteDuration);

            List<Media> media = post.getMedia();
            if (media.isEmpty()) {
                previewImageView.setTransitionName(null);
                previewImageView.setVisibility(View.GONE);
                imageProtectionTop.setVisibility(View.GONE);
                imageProtectionBottom.setVisibility(View.GONE);
                nameView.setShadowLayer(0, 0, 0, 0);
                nameView.setTextColor(ContextCompat.getColor(nameView.getContext(), R.color.primary_text));
                mediaIconView.setVisibility(View.GONE);
                if (post instanceof VoiceNotePost) {
                    previewTextView.setVisibility(View.GONE);
                    voiceNoteContainer.setVisibility(View.VISIBLE);
                    audioDurationLoader.load(voiceNoteDuration, post.media.get(0));
                } else {
                    previewTextView.setText(post.text);
                    previewTextView.setVisibility(View.VISIBLE);
                    voiceNoteContainer.setVisibility(View.GONE);
                }
            } else {
                voiceNoteContainer.setVisibility(View.GONE);
                previewImageView.setVisibility(View.VISIBLE);
                previewTextView.setVisibility(View.GONE);
                imageProtectionTop.setVisibility(View.VISIBLE);
                imageProtectionBottom.setVisibility(View.VISIBLE);
                nameView.setTextColor(Color.WHITE);
                nameView.setShadowLayer(3, 0, 0, Color.BLACK);
                if (!TextUtils.isEmpty(post.text)) {
                    mediaIconView.setImageResource(R.drawable.ic_group_text);
                    mediaIconView.setVisibility(View.VISIBLE);
                } else if (post instanceof VoiceNotePost) {
                    mediaIconView.setImageResource(R.drawable.ic_group_mic);
                    mediaIconView.setVisibility(View.VISIBLE);
                } else {
                    mediaIconView.setVisibility(View.GONE);
                }
                previewImageView.setTransitionName(MediaPagerAdapter.getTransitionName(post.id, 0));
                mediaThumbnailLoader.load(previewImageView, media.get(0));
            }

            if (post.unseenCommentCount > 0) {
                commentsIndicator.setVisibility(View.VISIBLE);
                commentsIndicator.setBackgroundResource(R.drawable.new_comments_indicator);
            } else if (post.commentCount > 0) {
                commentsIndicator.setVisibility(View.VISIBLE);
                commentsIndicator.setBackgroundResource(R.drawable.old_comments_indicator);
            } else {
                commentsIndicator.setVisibility(View.INVISIBLE);
            }

            newBorder.setVisibility(post.seen == Post.SEEN_NO ? View.VISIBLE : View.GONE);
        }
    }

    private class PostsPreviewAdapter extends HeaderFooterAdapter<Post> {

        private final AsyncPagedListDiffer<Post> differ;

        public PostsPreviewAdapter(@NonNull HeaderFooterAdapterParent parent) {
            super(parent);
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
            final TextView newMessagesView;
            final View infoContainer;
            final View selectionView;
            final View selectionCheck;
            final RecyclerView previewRv;

            private Group group;
            private PostsPreviewAdapter previewAdapter;

            private final Observer<PagedList<Post>> previewObserver;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                newMessagesView = itemView.findViewById(R.id.new_posts);
                infoContainer = itemView.findViewById(R.id.info_container);
                selectionView = itemView.findViewById(R.id.selection_background);
                selectionCheck = itemView.findViewById(R.id.selection_check);
                previewRv = itemView.findViewById(R.id.post_rv);
                previewRv.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                previewRv.addItemDecoration(new HorizontalSpaceDecoration(itemView.getContext().getResources().getDimensionPixelSize(R.dimen.group_post_preview_card_separation)));
                itemView.setOnLongClickListener(v -> {
                    updateGroupSelection(group);
                    return true;
                });
                itemView.setOnClickListener(v -> {
                    if (actionMode == null) {
                        startActivityForResult(ViewGroupFeedActivity.viewFeed(requireContext(), (GroupId) group.groupId), REQUEST_CODE_OPEN_GROUP);
                    } else {
                        updateGroupSelection(group);
                    }
                });
                previewObserver = list -> {
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
                    selectionCheck.setVisibility(View.VISIBLE);
                } else {
                    selectionView.setVisibility(View.GONE);
                    selectionCheck.setVisibility(View.GONE);
                }
                avatarLoader.load(avatarView, group.groupId);
                CharSequence name = group.name;
                if (filterTokens != null && !filterTokens.isEmpty()) {
                    CharSequence formattedName = FilterUtils.formatMatchingText(itemView.getContext(), group.name, filterTokens);
                    if (formattedName != null) {
                        name = formattedName;
                    }
                }
                nameView.setText(name);

                unseenGroupPostsLoader.load(newMessagesView, new ViewDataLoader.Displayer<View, List<Post>>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable List<Post> result) {
                        if (result == null || result.size() == 0) {
                            newMessagesView.setVisibility(View.GONE);
                        } else {
                            newMessagesView.setVisibility(View.VISIBLE);
                            newMessagesView.setText(String.format(Locale.getDefault(), "%d", result.size()));
                        }
                    }

                    @Override
                    public void showLoading(@NonNull View view) {
                        if (differentChat) {
                            newMessagesView.setVisibility(View.GONE);
                        }
                    }
                }, group.groupId);
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
                    });
                    View footer = adapter.addFooter(R.layout.view_group_post_preview_new_post);
                    footer.setOnClickListener(v -> {
                        Intent intent = new Intent(requireContext(), ContentComposerActivity.class);
                        intent.putExtra(ContentComposerActivity.EXTRA_GROUP_ID, group.groupId);
                        startActivityForResult(intent, REQUEST_CODE_NEW_POST);
                    });
                    footer.setOutlineProvider(new ViewOutlineProvider() {
                        @Override
                        public void getOutline(View view, Outline outline) {
                            float radius = itemView.getResources().getDimension(R.dimen.group_post_preview_card_radius);
                            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                        }
                    });
                    footer.setClipToOutline(true);
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
