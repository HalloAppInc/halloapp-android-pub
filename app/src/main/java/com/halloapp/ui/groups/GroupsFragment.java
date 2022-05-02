package com.halloapp.ui.groups;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.Constants;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.ui.AdapterWithLifecycle;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloFragment;
import com.halloapp.ui.MainNavFragment;
import com.halloapp.ui.SystemMessageTextResolver;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.GlobalUI;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.FabExpandOnScrollListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GroupsFragment extends HalloFragment implements MainNavFragment {

    private static final int REQUEST_CODE_OPEN_GROUP = 1;

    private final GroupsAdapter adapter = new GroupsAdapter();

    private GlobalUI globalUI;
    private ContactLoader contactLoader;
    private TextContentLoader textContentLoader;
    private UnseenGroupPostsLoader unseenGroupPostsLoader;
    private SystemMessageTextResolver systemMessageTextResolver;
    private AvatarLoader avatarLoader;

    private GroupListViewModel viewModel;

    private RecyclerView groupsView;
    private LinearLayoutManager layoutManager;

    private View emptyView;
    private TextView emptyViewMessage;

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

        Log.i("GroupsFragment.onCreateView");
        final View root = inflater.inflate(R.layout.fragment_groups, container, false);
        groupsView = root.findViewById(R.id.groups);
        emptyView = root.findViewById(android.R.id.empty);
        emptyViewMessage = root.findViewById(R.id.empty_text);

        Preconditions.checkNotNull((SimpleItemAnimator)groupsView.getItemAnimator()).setSupportsChangeAnimations(false);

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
        protected String itemToString(Group group) {
            return group.name;
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

    private class GroupsAdapter extends AdapterWithLifecycle<ViewHolderWithLifecycle> implements Filterable {

        private List<Group> groups;
        private List<Group> filteredGroups;
        private CharSequence filterText;
        private List<String> filterTokens;

        GroupsAdapter() {
            setHasStableIds(true);
        }

        void setGroups(@NonNull List<Group> chats) {
            this.groups = chats;
            this.filteredGroups = new ArrayList<>(chats);
            getFilter().filter(filterText);
            notifyDataSetChanged();
        }

        @Override
        public @NonNull ViewHolderWithLifecycle onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.i("GroupsAdapter.onCreateViewHolder " + viewType);
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false));
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

        @Override
        public long getItemId(int position) {
            return filteredGroups.get(position).rowId;
        }

        void setFilteredGroups(@NonNull List<Group> groups, CharSequence filterText) {
            this.filteredGroups = groups;
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
            final TextView infoView;
            final TextView newMessagesView;
            final TextView timeView;
            final View infoContainer;
            final View selectionView;
            final View selectionCheck;

            private Group group;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                infoView = itemView.findViewById(R.id.info);
                timeView = itemView.findViewById(R.id.time);
                newMessagesView = itemView.findViewById(R.id.new_posts);
                infoContainer = itemView.findViewById(R.id.info_container);
                selectionView = itemView.findViewById(R.id.selection_background);
                selectionCheck = itemView.findViewById(R.id.selection_check);
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

                viewModel.groupPostLoader.load(infoView, group.groupId, new ViewDataLoader.Displayer<View, Post>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable Post result) {
                        if (result != null) {
                            infoView.setVisibility(View.VISIBLE);
                            if (result.type == Post.TYPE_SYSTEM) {
                                bindGroupSystemPostPreview(result);
                            } else if (result.type == Post.TYPE_ZERO_ZONE) {
                                bindGroupZeroZonePostPreview(result);
                            } else {
                                bindGroupPostPreview(result);
                            }
                        } else {
                            infoView.setText("");
                            infoView.setVisibility(View.GONE);
                            timeView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void showLoading(@NonNull View view) {
                        if (differentChat) {
                            infoView.setVisibility(View.VISIBLE);
                            infoView.setText("");
                        }
                    }
                });
                unseenGroupPostsLoader.load(newMessagesView, new ViewDataLoader.Displayer<View, List<Post>>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable List<Post> result) {
                        if (result == null || result.size() == 0) {
                            newMessagesView.setVisibility(View.GONE);
                            timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), R.color.secondary_text));
                        } else {
                            newMessagesView.setVisibility(View.VISIBLE);
                            newMessagesView.setText(String.format(Locale.getDefault(), "%d", result.size()));
                            timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), R.color.unread_indicator));
                        }
                    }

                    @Override
                    public void showLoading(@NonNull View view) {
                        if (differentChat) {
                            newMessagesView.setVisibility(View.GONE);
                        }
                    }
                }, group.groupId);
            }

            private void bindGroupSystemPostPreview(@NonNull Post post) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setText(TimeFormatter.formatRelativeTime(timeView.getContext(), post.timestamp));
                contactLoader.cancel(infoView);
                systemMessageTextResolver.bindGroupSystemPostPreview(infoView, post);
            }

            private void bindGroupZeroZonePostPreview(@NonNull Post post) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setText(TimeFormatter.formatRelativeTime(timeView.getContext(), post.timestamp));
                contactLoader.cancel(infoView);
                infoView.setText(R.string.invite_friends_post_preview);
                systemMessageTextResolver.bindGroupSystemPostPreview(infoView, post);
            }

            private void bindGroupPostPreview(@NonNull Post post) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setText(TimeFormatter.formatRelativeTime(timeView.getContext(), post.timestamp));
                if (post.isIncoming()) {
                    contactLoader.load(infoView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                        @Override
                        public void showResult(@NonNull TextView view, @Nullable Contact result) {
                            if (result == null) {
                                return;
                            }
                            bindPostCaption(result.getDisplayName(), post);
                        }

                        @Override
                        public void showLoading(@NonNull TextView view) {
                            infoView.setText("");
                        }
                    });
                } else {
                    contactLoader.cancel(infoView);
                    bindOwnPostCaption(post);
                }
            }

            private void bindPostCaption(@NonNull String sender, @NonNull Post post) {
                textContentLoader.load(infoView, post, new TextContentLoader.TextDisplayer() {
                    @Override
                    public void showResult(TextView tv, CharSequence text) {
                        if (post.isRetracted()) {
                            infoView.setText(getString(R.string.post_preview_retracted, sender));
                        } else if (TextUtils.isEmpty(post.text) || post.type == Post.TYPE_FUTURE_PROOF) {
                            infoView.setText(getString(R.string.post_preview_no_caption, sender));
                        } else {
                            infoView.setText(getString(R.string.post_preview_with_caption, sender, text));
                        }
                    }

                    @Override
                    public void showPreview(TextView tv, CharSequence text) {
                        infoView.setText("");
                    }
                });
            }

            private void bindOwnPostCaption(@NonNull Post post) {
                textContentLoader.load(infoView, post, new TextContentLoader.TextDisplayer() {
                    @Override
                    public void showResult(TextView tv, CharSequence text) {
                        if (post.isRetracted()) {
                            infoView.setText(getString(R.string.post_preview_retracted_by_you));
                        } else if (TextUtils.isEmpty(post.text) || post.type == Post.TYPE_FUTURE_PROOF) {
                            infoView.setText(getString(R.string.post_preview_no_caption_by_you));
                        } else {
                            infoView.setText(getString(R.string.post_preview_with_caption_by_you, text));
                        }
                    }

                    @Override
                    public void showPreview(TextView tv, CharSequence text) {
                        infoView.setText("");
                    }
                });
            }
        }
    }
}
