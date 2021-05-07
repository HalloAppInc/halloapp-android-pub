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
import androidx.annotation.StringRes;
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
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.ui.AdapterWithLifecycle;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloFragment;
import com.halloapp.ui.MainNavFragment;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.GlobalUI;
import com.halloapp.util.ListFormatter;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class GroupsFragment extends HalloFragment implements MainNavFragment {

    private static final int REQUEST_CODE_OPEN_GROUP = 1;

    private final GroupsAdapter adapter = new GroupsAdapter();

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private GlobalUI globalUI;
    private ContactLoader contactLoader;
    private TextContentLoader textContentLoader;
    private UnseenGroupPostsLoader unseenGroupPostsLoader;

    private GroupListViewModel viewModel;

    private LinearLayoutManager layoutManager;

    private View emptyView;
    private TextView emptyViewMessage;

    private ActionMode actionMode;

    private MenuItem searchMenuItem;

    private HashMap<ChatId, Chat> selectedChats = new HashMap<>();

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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

        globalUI = GlobalUI.getInstance();
        contactLoader = new ContactLoader();
        textContentLoader = new TextContentLoader(requireContext());
        unseenGroupPostsLoader = new UnseenGroupPostsLoader();
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
                if (!TextUtils.isEmpty(text)) {
                    closeMenuItem.setVisible(true);
                } else {
                    closeMenuItem.setVisible(false);
                }
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
        final RecyclerView groupsView = root.findViewById(R.id.groups);
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

        groupsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()));

        return root;
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

    private void updateChatSelection(Chat chat) {
        ChatId chatId = chat.chatId;
        if (selectedChats.containsKey(chatId)) {
            selectedChats.remove(chatId);
        } else {
            selectedChats.put(chatId, chat);
        }
        adapter.notifyDataSetChanged();
        if (selectedChats.isEmpty()) {
            endActionMode();
            return;
        }
        if (actionMode == null) {
            actionMode = ((HalloActivity) getActivity()).startSupportActionMode(new ActionMode.Callback() {

                private int statusBarColor;
                private int previousVisibility;

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    mode.getMenuInflater().inflate(R.menu.groups_menu, menu);
                    statusBarColor = getActivity().getWindow().getStatusBarColor();

                    getActivity().getWindow().setStatusBarColor(getContext().getResources().getColor(R.color.color_secondary));
                    previousVisibility = getActivity().getWindow().getDecorView().getSystemUiVisibility();
                    getActivity().getWindow().getDecorView().setSystemUiVisibility(previousVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    MenuItem leaveItem = menu.findItem(R.id.leave_group);
                    leaveItem.setTitle(getResources().getQuantityString(R.plurals.leave_group_plural, selectedChats.size()));
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.delete) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(getContext().getResources().getQuantityString(R.plurals.delete_groups_confirmation, selectedChats.size(), selectedChats.size()));
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                            for (Chat chat : selectedChats.values()) {
                                ContentDb.getInstance().deleteChat(chat.chatId);
                            }
                            endActionMode();
                        });
                        builder.setNegativeButton(R.string.no, null);
                        builder.show();
                        return true;
                    } else if (item.getItemId() == R.id.view_group_info) {
                        for (ChatId chat : selectedChats.keySet()) {
                            if (chat instanceof GroupId) {
                                startActivity(GroupInfoActivity.viewGroup(getContext(), (GroupId) chat));
                                break;
                            }
                        }
                        endActionMode();
                    } else if (item.getItemId() == R.id.leave_group) {
                        List<GroupId> selectedGroups = new ArrayList<>();
                        for (ChatId chatId : selectedChats.keySet()) {
                            if (chatId instanceof GroupId) {
                                selectedGroups.add((GroupId) chatId);
                            }
                        }
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(getContext().getResources().getQuantityString(R.plurals.leave_multiple_groups_confirmation, selectedGroups.size(), selectedGroups.size()));
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
                    selectedChats.clear();
                    actionMode = null;
                    getActivity().getWindow().setStatusBarColor(statusBarColor);
                    getActivity().getWindow().getDecorView().setSystemUiVisibility(previousVisibility);
                }
            });
        }
        if (actionMode == null) {
            Log.e("ChatsFragment/updateChatSelection null actionmode");
            return;
        }
        boolean hasActiveGroup = false;
        for (Chat selectedChat : selectedChats.values()) {
            if (selectedChat.isActive) {
                hasActiveGroup = true;
                break;
            }
        }
        actionMode.getMenu().findItem(R.id.delete).setVisible(!hasActiveGroup);
        actionMode.getMenu().findItem(R.id.leave_group).setVisible(hasActiveGroup);

        if (selectedChats.size() == 1) {
            actionMode.getMenu().findItem(R.id.view_group_info).setVisible(true);
        } else {
            actionMode.getMenu().findItem(R.id.view_group_info).setVisible(false);
        }
        actionMode.setTitle(Integer.toString(selectedChats.size()));
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

    private class GroupsFilter extends FilterUtils.ItemFilter<Chat> {

        GroupsFilter(@NonNull List<Chat> chats) {
            super(chats);
        }

        @Override
        protected String itemToString(Chat chat) {
            return chat.name;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            final List<Chat> filteredContacts = (List<Chat>) results.values;
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

        private List<Chat> groups;
        private List<Chat> filteredGroups;
        private CharSequence filterText;
        private List<String> filterTokens;

        void setGroups(@NonNull List<Chat> chats) {
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

        void setFilteredGroups(@NonNull List<Chat> contacts, CharSequence filterText) {
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
            final TextView infoView;
            final TextView newMessagesView;
            final TextView timeView;
            final View infoContainer;
            final View selectionView;
            final View selectionCheck;

            private Chat chat;

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
                    updateChatSelection(chat);
                    return true;
                });
                itemView.setOnClickListener(v -> {
                    if (actionMode == null) {
                        startActivityForResult(ViewGroupFeedActivity.viewFeed(requireContext(), (GroupId) chat.chatId), REQUEST_CODE_OPEN_GROUP);
                    } else {
                        updateChatSelection(chat);
                    }
                });
            }

            void bindTo(@NonNull Chat chat, @Nullable List<String> filterTokens) {
                this.chat = chat;
                if (selectedChats.containsKey(chat.chatId)) {
                    selectionView.setVisibility(View.VISIBLE);
                    selectionCheck.setVisibility(View.VISIBLE);
                } else {
                    selectionView.setVisibility(View.GONE);
                    selectionCheck.setVisibility(View.GONE);
                }
                avatarLoader.load(avatarView, chat.chatId);
                CharSequence name = chat.name;
                if (filterTokens != null && !filterTokens.isEmpty()) {
                    CharSequence formattedName = FilterUtils.formatMatchingText(itemView.getContext(), chat.name, filterTokens);
                    if (formattedName != null) {
                        name = formattedName;
                    }
                }
                nameView.setText(name);

                viewModel.groupPostLoader.load(infoView, chat.chatId, new ViewDataLoader.Displayer<View, Post>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable Post result) {
                        if (result != null) {
                            infoView.setVisibility(View.VISIBLE);
                            if (result.type == Post.TYPE_SYSTEM) {
                                bindGroupSystemPostPreview(result);
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
                        infoView.setVisibility(View.VISIBLE);
                        infoView.setText("");
                    }
                });
                unseenGroupPostsLoader.load(newMessagesView, new ViewDataLoader.Displayer<View, List<Post>>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable List<Post> result) {
                        if (result == null || result.size() == 0) {
                            newMessagesView.setVisibility(View.GONE);
                        } else {
                            newMessagesView.setVisibility(View.VISIBLE);
                            newMessagesView.setText(String.format(Locale.getDefault(), "%d", result.size()));
                            timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), R.color.unread_indicator));
                        }
                    }

                    @Override
                    public void showLoading(@NonNull View view) {
                        newMessagesView.setVisibility(View.GONE);
                    }
                }, (GroupId) chat.chatId);
            }

            private void bindGroupSystemPostPreview(@NonNull Post post) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setText(TimeFormatter.formatRelativeTime(timeView.getContext(), post.timestamp));
                switch (post.usage) {
                    case Post.USAGE_CREATE_GROUP: {
                        systemMessageSingleUser(post, R.string.system_message_group_created_by_you, R.string.system_message_group_created);
                        break;
                    }
                    case Post.USAGE_ADD_MEMBERS: {
                        systemMessageAffectedList(post, R.string.system_message_members_added_by_you, R.string.system_message_members_added);
                        break;
                    }
                    case Post.USAGE_REMOVE_MEMBER: {
                        systemMessageAffectedList(post, R.string.system_message_members_removed_by_you, R.string.system_message_members_removed);
                        break;
                    }
                    case Post.USAGE_MEMBER_LEFT: {
                        systemMessageSingleUser(post, R.string.system_message_member_you_left, R.string.system_message_member_left);
                        break;
                    }
                    case Post.USAGE_PROMOTE: {
                        systemMessageAffectedList(post, R.string.system_message_members_promoted_by_you, R.string.system_message_members_promoted);
                        break;
                    }
                    case Post.USAGE_DEMOTE: {
                        systemMessageAffectedList(post, R.string.system_message_members_demoted_by_you, R.string.system_message_members_demoted);
                        break;
                    }
                    case Post.USAGE_AUTO_PROMOTE: {
                        systemMessageSingleUser(post, R.string.system_message_member_auto_promoted_you, R.string.system_message_member_auto_promoted);
                        break;
                    }
                    case Post.USAGE_NAME_CHANGE: {
                        if (post.senderUserId.isMe()) {
                            infoView.setText(itemView.getContext().getString(R.string.system_message_group_name_changed_by_you, post.text));
                        } else {
                            contactLoader.load(infoView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                                @Override
                                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                                    if (result != null) {
                                        infoView.setText(itemView.getContext().getString(R.string.system_message_group_name_changed, result.getDisplayName(), post.text));
                                    }
                                }

                                @Override
                                public void showLoading(@NonNull TextView view) {
                                    infoView.setText("");
                                }
                            });
                        }
                        break;
                    }
                    case Post.USAGE_AVATAR_CHANGE: {
                        systemMessageSingleUser(post, R.string.system_message_group_avatar_changed_by_you, R.string.system_message_group_avatar_changed);
                        break;
                    }
                    case Post.USAGE_GROUP_DELETED: {
                        systemMessageSingleUser(post, R.string.system_message_group_deleted_by_you, R.string.system_message_group_deleted);
                        break;
                    }
                    case Post.USAGE_MEMBER_JOINED: {
                        systemMessageSingleUser(post, R.string.system_message_you_joined, R.string.system_message_joined);
                        break;
                    }
                    case Post.USAGE_GROUP_THEME_CHANGED: {
                        systemMessageSingleUser(post, R.string.system_message_group_bg_changed_by_you, R.string.system_message_group_bg_changed);
                        break;
                    }
                    case Post.USAGE_POST:
                    default: {
                        Log.w("Unrecognized system message usage " + post.usage);
                    }
                }
            }

            private void systemMessageSingleUser(@NonNull Post message, @StringRes int meString, @StringRes int otherString) {
                if (message.senderUserId.isMe()) {
                    infoView.setText(itemView.getContext().getString(meString));
                } else {
                    contactLoader.load(infoView, message.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                        @Override
                        public void showResult(@NonNull TextView view, @Nullable Contact result) {
                            if (result != null) {
                                infoView.setText(itemView.getContext().getString(otherString, result.getDisplayName()));
                            }
                        }

                        @Override
                        public void showLoading(@NonNull TextView view) {
                            infoView.setText("");
                        }
                    });
                }
            }

            private void systemMessageAffectedList(@NonNull Post message, @StringRes int meString, @StringRes int otherString) {
                String commaSeparatedMembers = message.text;
                if (commaSeparatedMembers == null) {
                    Log.w("MessageViewHolder system message of type " + message.usage + " missing affected list " + message);
                    return;
                }
                String[] parts = commaSeparatedMembers.split(",");
                List<UserId> userIds = new ArrayList<>();
                userIds.add(message.senderUserId);
                for (String part : parts) {
                    userIds.add(new UserId(part));
                }
                contactLoader.loadMultiple(infoView, userIds, new ViewDataLoader.Displayer<TextView, List<Contact>>() {
                    @Override
                    public void showResult(@NonNull TextView view, @Nullable List<Contact> result) {
                        if (result != null) {
                            Contact sender = result.get(0);
                            boolean senderIsMe = sender.userId.isMe();
                            List<String> names = new ArrayList<>();
                            for (int i=1; i<result.size(); i++) {
                                Contact contact = result.get(i);
                                names.add(contact.userId.isMe() ? infoView.getResources().getString(R.string.you) : contact.getDisplayName());
                            }
                            String formatted = ListFormatter.format(itemView.getContext(), names);
                            if (senderIsMe) {
                                infoView.setText(itemView.getContext().getString(meString, formatted));
                            } else {
                                String senderName = sender.getDisplayName();
                                infoView.setText(itemView.getContext().getString(otherString, senderName, formatted));
                            }
                        }
                    }

                    @Override
                    public void showLoading(@NonNull TextView view) {
                        infoView.setText("");
                    }
                });
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
                    bindPostCaption(getString(R.string.you), post);
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
        }
    }
}
