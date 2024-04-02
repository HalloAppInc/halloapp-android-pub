package com.halloapp.ui.chats;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
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

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.AppContext;
import com.halloapp.Constants;
import com.halloapp.ContentDraftManager;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.CallMessage;
import com.halloapp.content.Chat;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.permissions.PermissionWatcher;
import com.halloapp.proto.clients.ContactCard;
import com.halloapp.ui.AdapterWithLifecycle;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.HalloFragment;
import com.halloapp.MainActivity;
import com.halloapp.ui.MainNavFragment;
import com.halloapp.ui.SystemMessageTextResolver;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.DeviceAvatarLoader;
import com.halloapp.ui.chat.chat.ChatActivity;
import com.halloapp.ui.chat.chat.MessageViewHolder;
import com.halloapp.ui.contacts.ConnectedContactsAdapter;
import com.halloapp.ui.contacts.NewInviteContactsAdapter;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.ContactCardUtils;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.AllowContactsView;
import com.halloapp.widget.FabExpandOnScrollListener;
import com.halloapp.xmpp.PresenceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class ChatsFragment extends HalloFragment implements MainNavFragment {

    private static final int REQUEST_CODE_OPEN_CHAT = 1;

    private final ChatsAdapter adapter = new ChatsAdapter();
    private NewInviteContactsAdapter inviteAdapter;
    private ConcatAdapter concatAdapter;
    private ConcatAdapter connectionAdapter;
    private ConnectedContactsAdapter connectedContactsAdapter;

    private final PresenceManager presenceManager = PresenceManager.getInstance();
    private final PermissionWatcher permissionWatcher = PermissionWatcher.getInstance();
    private final ContentDraftManager contentDraftManager = ContentDraftManager.getInstance();

    private ContactLoader contactLoader;
    private TextContentLoader textContentLoader;
    private AvatarLoader avatarLoader;
    private SystemMessageTextResolver systemMessageTextResolver;
    private DeviceAvatarLoader deviceAvatarLoader;

    private ChatsViewModel viewModel;

    private LinearLayoutManager layoutManager;

    private View emptyView;
    private View normalEmptyView;
    private AllowContactsView permsEmptyView;
    private TextView emptyViewMessage;
    private RecyclerView chatsView;

    private MenuItem searchMenuItem;

    private ActionMode actionMode;

    private final HashSet<ChatId> selectedChats = new HashSet<>();

    private ContentDraftManager.ContentDraftObserver chatDraftChangesObserver;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_OPEN_CHAT: {
                if (resultCode == Activity.RESULT_OK) {
                    if (searchMenuItem != null) {
                        searchMenuItem.collapseActionView();
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        avatarLoader = AvatarLoader.getInstance();
        deviceAvatarLoader = new DeviceAvatarLoader(AppContext.getInstance().get());
        contactLoader = new ContactLoader(userId -> {
            startActivity(ViewProfileActivity.viewProfile(requireContext(), userId));
            return null;
        });
        systemMessageTextResolver = new SystemMessageTextResolver(contactLoader);
        textContentLoader = new TextContentLoader();
        chatDraftChangesObserver = new ContentDraftManager.ContentDraftObserver() {
            @Override
            public void onDraftUpdated() {
                viewModel.chatsList.invalidate();
            }
        };
        contentDraftManager.addObserver(chatDraftChangesObserver);
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
        
        searchMenuItem.setVisible(EasyPermissions.hasPermissions(requireContext(), Manifest.permission.READ_CONTACTS));

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
                inviteAdapter.getFilter().filter(text);
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

    private List<Contact> searchInviteContacts;
    private List<Chat> searchChats;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        Log.i("ChatsFragment.onCreateView");
        final View root = inflater.inflate(R.layout.fragment_chats, container, false);
        chatsView = root.findViewById(R.id.chats);
        emptyView = root.findViewById(android.R.id.empty);
        emptyViewMessage = root.findViewById(R.id.empty_text);

        normalEmptyView = root.findViewById(R.id.normal_empty);
        permsEmptyView = root.findViewById(R.id.perms_empty);
        permsEmptyView.setOnAllowClick(v -> {
            PermissionUtils.hasOrRequestContactPermissions(requireActivity(), MainActivity.REQUEST_CODE_ASK_CONTACTS_PERMISSION);
        });

        Preconditions.checkNotNull((SimpleItemAnimator)chatsView.getItemAnimator()).setSupportsChangeAnimations(false);

        layoutManager = new LinearLayoutManager(getContext());
        chatsView.setLayoutManager(layoutManager);
        viewModel = new ViewModelProvider(requireActivity()).get(ChatsViewModel.class);
        if (viewModel.getSavedScrollState() != null) {
            layoutManager.onRestoreInstanceState(viewModel.getSavedScrollState());
        }
        viewModel.chatsList.getLiveData().observe(getViewLifecycleOwner(), chats -> {
            adapter.setChats(chats);
            emptyView.setVisibility((chats.size() == 0 && inviteAdapter == null) ? View.VISIBLE : View.GONE);
        });
        connectedContactsAdapter = new ConnectedContactsAdapter(() -> getViewLifecycleOwner());
        inviteAdapter = new NewInviteContactsAdapter();
        inviteAdapter.setSendingEnabled(true);
        inviteAdapter.setParent(new NewInviteContactsAdapter.InviteContactsAdapterParent() {
            @Override
            public void onInvite(@NonNull Contact contact) {
                viewModel.markContactInvited(contact);
                Intent chooser = IntentUtils.createSmsChooserIntent(requireContext(), getString(R.string.invite_friend_chooser_title, contact.getShortName()), Preconditions.checkNotNull(contact.normalizedPhone), getInviteText(contact));
                startActivity(chooser);
            }

            @Override
            public void onFiltered(@NonNull CharSequence constraint, @NonNull List<Contact> contacts) {
                searchInviteContacts = contacts;
                if (searchInviteContacts.isEmpty() &&
                        (searchChats == null || searchChats.isEmpty())) {
                    emptyView.setVisibility(View.VISIBLE);
                    if (TextUtils.isEmpty(constraint)) {
                        emptyViewMessage.setText(R.string.chats_page_empty);
                    } else {
                        emptyViewMessage.setText(getString(R.string.chats_search_empty, constraint));
                    }
                } else {
                    emptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public DeviceAvatarLoader getDeviceAvatarLoader() {
                return deviceAvatarLoader;
            }
        });
        concatAdapter = new ConcatAdapter(adapter, inviteAdapter);
        connectionAdapter = new ConcatAdapter(connectedContactsAdapter, inviteAdapter);
        chatsView.setAdapter(concatAdapter);
        viewModel.contactsList.getLiveData().observe(getViewLifecycleOwner(), inviteAdapter::setContacts);
        viewModel.showInviteList.observe(getViewLifecycleOwner(), showInvites -> {
            if (showInvites == null || !showInvites) {
                chatsView.setAdapter(concatAdapter);
                return;
            }
            chatsView.setAdapter(connectionAdapter);
            emptyView.setVisibility(View.GONE);
        });
        permissionWatcher.getPermissionLiveData(Manifest.permission.READ_CONTACTS).observe(getViewLifecycleOwner(), enabled -> {
            if (enabled == null || !enabled) {
                normalEmptyView.setVisibility(View.GONE);
                permsEmptyView.setVisibility(View.VISIBLE);
            } else {
                normalEmptyView.setVisibility(View.VISIBLE);
                permsEmptyView.setVisibility(View.GONE);
            }
        });
        viewModel.messageUpdated.observe(getViewLifecycleOwner(), updated -> adapter.notifyDataSetChanged());
        return root;
    }

    private String getInviteText(@NonNull Contact contact) {
        return getString(R.string.invite_text_with_name_and_number, contact.getShortName(), contact.getDisplayPhone(), Constants.DOWNLOAD_LINK_URL);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        chatsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()));
        chatsView.addOnScrollListener(new FabExpandOnScrollListener((AppCompatActivity) requireActivity()));
    }

    @Override
    public void onDestroy() {
        contentDraftManager.removeObserver(chatDraftChangesObserver);
        if (deviceAvatarLoader != null) {
            deviceAvatarLoader.destroy();
            deviceAvatarLoader = null;
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (viewModel != null && layoutManager != null) {
            viewModel.saveScrollState(layoutManager.onSaveInstanceState());
        }
        endActionMode();
        super.onDestroyView();
    }

    private class ChatsFilter extends FilterUtils.ItemFilter<Chat> {

        ChatsFilter(@NonNull List<Chat> chats) {
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
            adapter.setFilteredChats(filteredContacts, constraint);
            searchChats = filteredContacts;
            if (searchChats.isEmpty() &&
                    (searchInviteContacts == null || searchInviteContacts.isEmpty())) {
                emptyView.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(constraint)) {
                    emptyViewMessage.setText(R.string.chats_page_empty);
                } else {
                    emptyViewMessage.setText(getString(R.string.chats_search_empty, constraint));
                }
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    private void endActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void updateChatSelection(ChatId chatId) {
        if (selectedChats.contains(chatId)) {
            selectedChats.remove(chatId);
        } else {
            selectedChats.add(chatId);
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
                    mode.getMenuInflater().inflate(R.menu.chats_menu, menu);
                    statusBarColor = getActivity().getWindow().getStatusBarColor();

                    getActivity().getWindow().setStatusBarColor(getContext().getResources().getColor(R.color.color_secondary));
                    previousVisibility = getActivity().getWindow().getDecorView().getSystemUiVisibility();
                    if (Build.VERSION.SDK_INT >= 23) {
                        getActivity().getWindow().getDecorView().setSystemUiVisibility(previousVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    }
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.delete) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(getContext().getResources().getQuantityString(R.plurals.delete_chats_confirmation, selectedChats.size(), selectedChats.size()));
                        builder.setCancelable(true);
                        builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                            viewModel.deleteChats(selectedChats);
                            endActionMode();
                        });
                        builder.setNegativeButton(R.string.no, null);
                        builder.show();
                        return true;
                    } else if (item.getItemId() == R.id.view_profile) {
                        for (ChatId chat : selectedChats) {
                            if (chat instanceof UserId) {
                                getContext().startActivity(ViewProfileActivity.viewProfile(getContext(), (UserId) chat));
                                break;
                            }
                        }
                        endActionMode();
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
        actionMode.getMenu().findItem(R.id.view_profile).setVisible(selectedChats.size() == 1);
        actionMode.setTitle(Integer.toString(selectedChats.size()));
    }

    private class ChatsAdapter extends AdapterWithLifecycle<ViewHolderWithLifecycle> implements Filterable {

        private List<Chat> chats;
        private List<Chat> filteredChats;
        private CharSequence filterText;
        private List<String> filterTokens;

        void setChats(@NonNull List<Chat> chats) {
            this.chats = chats;
            this.filteredChats = new ArrayList<>(chats);
            getFilter().filter(filterText);
            notifyDataSetChanged();
        }

        @Override
        public @NonNull ViewHolderWithLifecycle onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.i("ChatsAdapter.onCreateViewHolder " + viewType);
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            if (holder instanceof ViewHolder) {
                ((ViewHolder) holder).bindTo(filteredChats.get(position), filterTokens);
            }
        }

        @Override
        public int getItemCount() {
            return getFilteredContactsCount();
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolderWithLifecycle holder) {
            if (holder instanceof ViewHolder) {
                ((ViewHolder) holder).detatchObservers();
            }
            super.onViewRecycled(holder);
        }

        void setFilteredChats(@NonNull List<Chat> contacts, CharSequence filterText) {
            this.filteredChats = contacts;
            ListIterator<Chat> iterator = filteredChats.listIterator();
            while (iterator.hasNext()) {
                if (iterator.next().name == null) {
                    iterator.remove();
                }
            }
            this.filterText = filterText;
            this.filterTokens = FilterUtils.getFilterTokens(filterText);
            notifyDataSetChanged();
        }

        private int getFilteredContactsCount() {
            return filteredChats == null ? 0 : filteredChats.size();
        }

        @Override
        public Filter getFilter() {
            return new ChatsFilter(chats);
        }

        class ViewHolder extends ViewHolderWithLifecycle {

            final ImageView avatarView;
            final TextView nameView;
            final TextView infoView;
            final TextView newMessagesView;
            final TextView timeView;
            final ImageView statusView;
            final ImageView mediaIcon;
            final TextView typingView;
            final View infoContainer;
            final View selectionView;
            final View selectionCheck;

            private Chat chat;

            private LiveData<PresenceManager.PresenceState> presenceLiveData;
            private LiveData<PresenceManager.GroupChatState> groupChatStateLiveData;

            private final Observer<PresenceManager.PresenceState> presenceObserver;
            private final Observer<PresenceManager.GroupChatState> groupChatStateObserver;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                infoView = itemView.findViewById(R.id.info);
                timeView = itemView.findViewById(R.id.time);
                newMessagesView = itemView.findViewById(R.id.new_messages);
                statusView = itemView.findViewById(R.id.status);
                mediaIcon = itemView.findViewById(R.id.media_icon);
                typingView = itemView.findViewById(R.id.typing_indicator);
                infoContainer = itemView.findViewById(R.id.info_container);
                selectionView = itemView.findViewById(R.id.selection_background);
                selectionCheck = itemView.findViewById(R.id.selection_check);
                itemView.setOnLongClickListener(v -> {
                    updateChatSelection(chat.chatId);
                    return true;
                });
                itemView.setOnClickListener(v -> {
                    if (actionMode == null) {
                        startActivityForResult(ChatActivity.open(requireContext(), chat.chatId), REQUEST_CODE_OPEN_CHAT);
                    } else {
                        updateChatSelection(chat.chatId);
                    }
                });

                presenceObserver = presenceState -> {
                    if (presenceState.state == PresenceManager.PresenceState.PRESENCE_STATE_TYPING) {
                        typingView.setVisibility(View.VISIBLE);
                        infoContainer.setVisibility(View.INVISIBLE);
                        typingView.setText(R.string.user_typing);
                    } else {
                        infoContainer.setVisibility(View.VISIBLE);
                        typingView.setVisibility(View.GONE);
                    }
                };

                groupChatStateObserver = groupChatState -> {
                    int typingUsers = groupChatState.typingUsers.size();
                    if (typingUsers > 0) {
                        typingView.setVisibility(View.VISIBLE);
                        infoContainer.setVisibility(View.INVISIBLE);
                        if (groupChatState.typingUsers.size() == 1) {
                            UserId typingUser = Preconditions.checkNotNull(groupChatState.typingUsers.get(0));
                            Contact contact = Preconditions.checkNotNull(groupChatState.contactMap.get(typingUser));
                            typingView.setText(getString(R.string.group_user_typing, contact.getDisplayName()));
                        } else if (groupChatState.typingUsers.size() > 0) {
                            typingView.setText(getString(R.string.group_many_users_typing));
                        }
                    } else {
                        infoContainer.setVisibility(View.VISIBLE);
                        typingView.setVisibility(View.GONE);
                    }
                };
            }

            void detatchObservers() {
                if (groupChatStateLiveData != null) {
                    groupChatStateLiveData.removeObserver(groupChatStateObserver);
                    groupChatStateLiveData = null;
                }

                if (presenceLiveData != null) {
                    presenceLiveData.removeObserver(presenceObserver);
                    presenceLiveData = null;
                }

                // Reset visibility for these views
                infoContainer.setVisibility(View.VISIBLE);
                typingView.setVisibility(View.GONE);
            }

            void bindTo(@NonNull Chat chat, @Nullable List<String> filterTokens) {
                this.chat = chat;
                timeView.setText(TimeFormatter.formatRelativeTime(timeView.getContext(), chat.timestamp));
                avatarLoader.load(avatarView, chat.chatId, false);
                if (selectedChats.contains(chat.chatId)) {
                    selectionView.setVisibility(View.VISIBLE);
                    selectionCheck.setVisibility(View.VISIBLE);
                } else {
                    selectionView.setVisibility(View.GONE);
                    selectionCheck.setVisibility(View.GONE);
                }
                CharSequence name = chat.name;
                if (filterTokens != null && !filterTokens.isEmpty()) {
                    CharSequence formattedName = FilterUtils.formatMatchingText(itemView.getContext(), chat.name, filterTokens);
                    if (formattedName != null) {
                        name = formattedName;
                    }
                }
                nameView.setText(name);
                detatchObservers();
                if (chat.chatId instanceof GroupId) {
                    groupChatStateLiveData = presenceManager.getChatStateLiveData((GroupId) chat.chatId);
                    groupChatStateLiveData.observe(getViewLifecycleOwner(), groupChatStateObserver);
                } else {
                    presenceLiveData = presenceManager.getLastSeenLiveData((UserId) chat.chatId, false);
                    presenceLiveData.observe(getViewLifecycleOwner(), presenceObserver);
                }

                final String chatDraft = contentDraftManager.getTextDraft(chat.chatId);
                contactLoader.cancel(infoView);
                if (!TextUtils.isEmpty(chatDraft)) {
                    viewModel.messageLoader.cancel(infoView);
                    infoView.setText(getContext().getString(R.string.draft_label, chatDraft));
                    infoView.setTypeface(infoView.getTypeface(), Typeface.ITALIC);
                } else {
                    infoView.setTypeface(Typeface.create(infoView.getTypeface(), Typeface.NORMAL));
                    // TODO: maybe consolidate loading into a single pass
                    if (chat.lastMessageRowId >= 0) {
                        viewModel.messageLoader.load(itemView, chat.lastMessageRowId, new ViewDataLoader.Displayer<View, Message>() {
                            @Override
                            public void showResult(@NonNull View view, @Nullable Message message) {
                                if (message != null) {
                                    bindMessagePreview(message);
                                } else {
                                    infoView.setText("");
                                    statusView.setVisibility(View.GONE);
                                    mediaIcon.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void showLoading(@NonNull View view) {
                                infoView.setText("");
                                statusView.setVisibility(View.GONE);
                                mediaIcon.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        viewModel.messageLoader.cancel(itemView);
                        if (!chat.isGroup) {
                            if (chat.timestamp == 0) {
                                infoView.setText(getString(R.string.empty_chat_with_name_placeholder, chat.name));
                                infoView.setTextColor(getResources().getColor(R.color.empty_chat_placeholder));
                            } else {
                                infoView.setText(getString(R.string.new_chat_name_placeholder, chat.name));
                                infoView.setTextColor(getResources().getColor(R.color.chat_message_preview));
                            }
                        } else {
                            infoView.setText("");
                        }
                        if (chat.timestamp == 0) {
                            timeView.setText("");
                        }
                        statusView.setVisibility(View.GONE);
                        mediaIcon.setVisibility(View.GONE);
                    }
                    if (chat.newMessageCount > 0) {
                        newMessagesView.setVisibility(View.VISIBLE);
                        newMessagesView.setText(String.format(Locale.getDefault(), "%d", chat.newMessageCount));
                        timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), R.color.unread_indicator));
                    } else if (chat.newMessageCount == Chat.MARKED_UNSEEN) {
                        newMessagesView.setVisibility(View.VISIBLE);
                        newMessagesView.setText(" ");
                        timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), R.color.unread_indicator));
                    } else {
                        timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), R.color.secondary_text));
                        newMessagesView.setVisibility(View.GONE);
                    }
                }
            }

            private void bindMessagePreview(@NonNull Message message) {
                if (message.isIncoming() || message.isRetracted() || message.type == Message.TYPE_CALL || message.type == Message.TYPE_SYSTEM) {
                    statusView.setVisibility(View.GONE);
                } else {
                    statusView.setVisibility(View.VISIBLE);
                    statusView.setImageResource(MessageViewHolder.getStatusImageResource(message.state));
                }
                if (message.media.size() == 0) {
                    if (message instanceof CallMessage) {
                        mediaIcon.setVisibility(View.VISIBLE);
                        @ColorRes int tintColor = R.color.primary_text;
                        switch (((CallMessage) message).callUsage) {
                            case CallMessage.Usage.MISSED_VOICE_CALL: {
                                mediaIcon.setImageResource(R.drawable.ic_missed_call);
                                tintColor = R.color.color_primary;
                                break;
                            }
                            case CallMessage.Usage.MISSED_VIDEO_CALL: {
                                mediaIcon.setImageResource(R.drawable.ic_missed_video_call);
                                tintColor = R.color.color_primary;
                                break;
                            }
                            case CallMessage.Usage.LOGGED_VIDEO_CALL:
                                mediaIcon.setImageResource(message.isOutgoing() ? R.drawable.ic_video_call_outgoing : R.drawable.ic_video_call_incoming);
                                break;
                            case CallMessage.Usage.LOGGED_VOICE_CALL:
                            default: {
                                mediaIcon.setImageResource(message.isOutgoing() ? R.drawable.ic_call_outgoing : R.drawable.ic_call_incoming);
                                break;
                            }
                        }
                        mediaIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mediaIcon.getContext(), tintColor)));
                    } else if (message.type == Message.TYPE_CONTACT) {
                        mediaIcon.setVisibility(View.VISIBLE);
                        mediaIcon.setImageResource(R.drawable.ic_privacy_my_contacts);
                        @ColorRes int tintColor = R.color.primary_text;
                        mediaIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mediaIcon.getContext(), tintColor)));
                    } else {
                        mediaIcon.setVisibility(View.GONE);
                    }
                } else if (message.media.size() == 1) {
                    mediaIcon.setVisibility(View.VISIBLE);
                    @ColorRes int tintColor = R.color.primary_text;
                    final Media media = message.media.get(0);
                    switch (media.type) {
                        case Media.MEDIA_TYPE_IMAGE: {
                            mediaIcon.setImageResource(R.drawable.ic_camera);
                            break;
                        }
                        case Media.MEDIA_TYPE_VIDEO: {
                            mediaIcon.setImageResource(R.drawable.ic_video);
                            break;
                        }
                        case Media.MEDIA_TYPE_AUDIO: {
                            mediaIcon.setImageResource(R.drawable.ic_keyboard_voice);
                            if (message.state == Message.STATE_OUTGOING_PLAYED) {
                                tintColor = R.color.message_state_read;
                            }
                            break;
                        }
                        case Media.MEDIA_TYPE_DOCUMENT: {
                            mediaIcon.setImageResource(R.drawable.ic_document);
                            break;
                        }
                        case Media.MEDIA_TYPE_UNKNOWN:
                        default: {
                            mediaIcon.setImageResource(R.drawable.ic_media_collection);
                            break;
                        }
                    }
                    mediaIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mediaIcon.getContext(), tintColor)));
                } else {
                    mediaIcon.setVisibility(View.VISIBLE);
                    mediaIcon.setImageResource(R.drawable.ic_media_collection);
                    mediaIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mediaIcon.getContext(), R.color.primary_text)));
                }

                if (message.chatId instanceof GroupId) {
                    if (message.type == Message.TYPE_SYSTEM) {
                        contactLoader.cancel(infoView);
                        infoView.setTextColor(infoView.getResources().getColor(R.color.chat_message_preview));
                        systemMessageTextResolver.bindSystemMessagePostPreview(infoView, message);
                    } else {
                        contactLoader.load(infoView, message.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                            @Override
                            public void showResult(@NonNull TextView view, @Nullable Contact result) {
                                bindMessageText(result == null ? null : result.getDisplayName(), message);
                            }

                            @Override
                            public void showLoading(@NonNull TextView view) {
                                infoView.setText("");
                            }
                        });
                    }
                } else {
                    contactLoader.cancel(infoView);
                    bindMessageText(null, message);
                }
            }

            private void bindMessageText(@Nullable String sender, @NonNull Message message) {
                infoView.setTextColor(infoView.getResources().getColor(R.color.chat_message_preview));
                final String text;
                if (message.isRetracted()) {
                    SpannableString ss = new SpannableString(getString(R.string.message_retracted_placeholder));
                    ss.setSpan(new StyleSpan(Typeface.ITALIC), 0, ss.length(), 0);
                    infoView.setText(ss);
                } else if (message.type == Message.TYPE_SYSTEM) {
                    infoView.setText("");
                } else if (message.type == Message.TYPE_CALL) {
                    if (message instanceof CallMessage) {
                        int callUsage = ((CallMessage) message).callUsage;
                        long callDuration;
                        switch (callUsage) {
                            case CallMessage.Usage.MISSED_VOICE_CALL:
                                text = itemView.getContext().getString(R.string.log_missed_voice_call);
                                break;
                            case CallMessage.Usage.LOGGED_VOICE_CALL:
                                callDuration = ((CallMessage) message).callDuration;
                                if (callDuration > 0) {
                                    text = itemView.getContext().getString(R.string.log_voice_call_with_duration, TimeFormatter.formatCallDuration(callDuration));
                                } else {
                                    text = itemView.getContext().getString(R.string.log_voice_call);
                                }
                                break;
                            case CallMessage.Usage.MISSED_VIDEO_CALL:
                                text = itemView.getContext().getString(R.string.log_missed_video_call);
                                break;
                            case CallMessage.Usage.LOGGED_VIDEO_CALL:
                                callDuration = ((CallMessage) message).callDuration;
                                if (callDuration > 0) {
                                    text = itemView.getContext().getString(R.string.log_video_call_with_duration, TimeFormatter.formatCallDuration(callDuration));
                                } else {
                                    text = itemView.getContext().getString(R.string.log_video_call);
                                }
                                break;
                            case CallMessage.Usage.UNANSWERED_VIDEO_CALL:
                                text = itemView.getContext().getString(R.string.log_unanswered_video_call);
                                break;
                            case CallMessage.Usage.UNANSWERED_VOICE_CALL:
                                text = itemView.getContext().getString(R.string.log_unanswered_voice_call);
                                break;
                            default:
                                text = "";
                        }
                    } else {
                        text = "";
                    }
                    infoView.setText(sender == null ? text : getString(R.string.chat_message_attribution, sender, text));
                } else if (message.type == Message.TYPE_CONTACT) {
                    ContactCard contactCard = ContactCardUtils.deserializeContactCard(message.text);
                    String name = getString(R.string.attachment_contact);;
                    if (contactCard != null) {
                        for (com.halloapp.proto.clients.Contact contact : contactCard.getContactsList()) {
                            name = contact.getName();
                            break;
                        }
                    }
                    infoView.setText(sender == null ? name : getString(R.string.chat_message_attribution, sender, name));
                } else if (TextUtils.isEmpty(message.text)) {
                    if (message.media.size() == 1) {
                        final Media media = message.media.get(0);
                        switch (media.type) {
                            case Media.MEDIA_TYPE_IMAGE: {
                                text = itemView.getContext().getString(R.string.photo);
                                break;
                            }
                            case Media.MEDIA_TYPE_VIDEO: {
                                text = itemView.getContext().getString(R.string.video);
                                break;
                            }
                            case Media.MEDIA_TYPE_AUDIO: {
                                text = itemView.getContext().getString(R.string.voice_note);
                                break;
                            }
                            case Media.MEDIA_TYPE_UNKNOWN:
                            default: {
                                text = "";
                                break;
                            }
                        }
                    } else {
                        text = itemView.getContext().getString(R.string.album);
                    }
                    infoView.setText(sender == null ? text : getString(R.string.chat_message_attribution, sender, text));
                } else {
                    textContentLoader.load(infoView, message, new TextContentLoader.TextDisplayer() {
                        @Override
                        public void showResult(TextView tv, CharSequence text) {
                            tv.setText(sender == null ? text : getString(R.string.chat_message_attribution, sender, text));
                        }

                        @Override
                        public void showPreview(TextView tv, CharSequence text) {
                            tv.setText(sender == null ? text : getString(R.string.chat_message_attribution, sender, text));
                        }
                    });
                }
            }
        }
    }
}
