package com.halloapp.ui.contacts;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ComputableLiveData;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.privacy.FeedPrivacyManager;
import com.halloapp.proto.server.FriendListRequest;
import com.halloapp.proto.server.FriendshipStatus;
import com.halloapp.proto.server.HalloappUserProfile;
import com.halloapp.ui.DebouncedClickListener;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.DeviceAvatarLoader;
import com.halloapp.ui.contacts.ViewFriendsListActivity.ViewFriendsListViewModel.FriendState;
import com.halloapp.ui.contacts.ViewFriendsListActivity.ViewFriendsListViewModel.Tab;
import com.halloapp.ui.contacts.ViewFriendsListActivity.ViewFriendsListViewModel.Type;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.DelayedProgressLiveData;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.FriendListResponseIq;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

public class ViewFriendsListActivity extends HalloActivity {

    private View friendsContainer;
    private TextView requestsTab;
    private TextView friendsTab;
    private ImageView selectedRequestsTab;
    private ImageView selectedFriendsTab;
    private ImageView searchIconView;
    private EditText searchEditTextView;
    private TextView requestsTitleView;
    private RecyclerView listView;

    private ViewFriendsListViewModel viewModel;
    private final FriendsAdapter adapter = new FriendsAdapter();
    private DeviceAvatarLoader deviceAvatarLoader;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friends_list);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(ViewFriendsListViewModel.class);

        friendsContainer = findViewById(R.id.friends_type_container);
        requestsTab = findViewById(R.id.requests_tab);
        friendsTab = findViewById(R.id.friends_tab);
        selectedRequestsTab = findViewById(R.id.selected_requests_tab);
        selectedFriendsTab = findViewById(R.id.selected_friends_tab);
        searchEditTextView = findViewById(R.id.search_text);
        searchIconView = findViewById(R.id.search_icon);
        requestsTitleView = findViewById(R.id.requests_title);

        searchIconView.setOnClickListener(view -> {
            requestsTitleView.setVisibility(View.GONE);
            searchEditTextView.setVisibility(View.VISIBLE);
            searchEditTextView.setText("");
            KeyboardUtils.showSoftKeyboard(searchEditTextView);
        });

        searchEditTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateSearchText(s.toString());
            }
        });

        deviceAvatarLoader = new DeviceAvatarLoader(this);

        listView = findViewById(R.id.list);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        viewModel.getSelectedTab().observe(this, this::setSelectedTab);
        viewModel.items.getLiveData().observe(this, adapter::setItems);

        viewModel.searchState.observe(this, state -> {
            if (state == ViewFriendsListViewModel.SearchState.Closed) {
                searchIconView.setImageDrawable(getResources().getDrawable(R.drawable.ic_search));
                friendsContainer.setVisibility(viewModel.getSelectedTab().getValue() == Tab.ALL_REQUESTS ? View.GONE : View.VISIBLE);
                listView.setVisibility(View.VISIBLE);
            } else {
                searchIconView.setImageDrawable(getResources().getDrawable(R.drawable.ic_x));
                friendsContainer.setVisibility(View.GONE);
                listView.setVisibility(View.INVISIBLE);
            }
        });


        requestsTab.setOnClickListener(view -> viewModel.setSelectedTab(Tab.SUGGESTIONS));

        friendsTab.setOnClickListener(view -> viewModel.setSelectedTab(Tab.FRIENDS));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceAvatarLoader != null) {
            deviceAvatarLoader.destroy();
            deviceAvatarLoader = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (viewModel.getSelectedTab().getValue() == Tab.ALL_REQUESTS) {
            viewModel.setSelectedTab(Tab.SUGGESTIONS);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("RestrictedApi")
    private void setSelectedTab(@NonNull Tab tab) {
        int selectedTextColor = getResources().getColor(R.color.favorites_dialog_blue);
        int unselectedTextColor = getResources().getColor(R.color.black);
        Drawable selectedBackgroundDrawable = ContextCompat.getDrawable(this, R.drawable.rounded_entry_friendship_bg);
        Drawable unselectedBackgroundDrawable = null;

        friendsContainer.setVisibility(Tab.ALL_REQUESTS.equals(tab) ? View.GONE : View.VISIBLE);
        requestsTitleView.setVisibility(Tab.ALL_REQUESTS.equals(tab) ? View.VISIBLE : View.GONE);
        searchEditTextView.setVisibility(Tab.ALL_REQUESTS.equals(tab) ? View.INVISIBLE : View.VISIBLE);
        requestsTab.setTextColor(Tab.SUGGESTIONS.equals(tab) ? selectedTextColor : unselectedTextColor);
        selectedRequestsTab.setBackground(Tab.SUGGESTIONS.equals(tab) ? selectedBackgroundDrawable : unselectedBackgroundDrawable);
        friendsTab.setTextColor(Tab.FRIENDS.equals(tab) ? selectedTextColor : unselectedTextColor);
        selectedFriendsTab.setBackground(Tab.FRIENDS.equals(tab) ? selectedBackgroundDrawable : unselectedBackgroundDrawable);

        viewModel.items.invalidate();
    }

    public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Item> items = new ArrayList<>();

        public void setItems(@NonNull List<Item> items) {
            this.items = items;
            notifyDataSetChanged();
            listView.setVisibility(View.VISIBLE);
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).type;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case Type.SEE_REQUESTS:
                    return new SeeRequestsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item_see_requests, parent, false));
                case Type.SECTION_HEADER:
                    return new SectionHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item_section_header, parent, false));
                case Type.PERSON:
                    return new PersonViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item_person, parent, false));
                case Type.DEVICE_CONTACT:
                    return new DeviceContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item_device_contact, parent, false));
                case Type.NO_CONNECTIONS:
                    return new NoConnectionsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item_no_connections, parent, false));
            }
            throw new IllegalArgumentException("Invalid viewType " + viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof PersonViewHolder) {
                PersonViewHolder personViewHolder = (PersonViewHolder) holder;
                PersonItem item = (PersonItem) items.get(position);
                personViewHolder.bindTo(item);
            } else if (holder instanceof SectionHeaderViewHolder) {
                SectionHeaderViewHolder sectionHeaderViewHolder = (SectionHeaderViewHolder) holder;
                SectionHeaderItem item = (SectionHeaderItem) items.get(position);
                sectionHeaderViewHolder.bindTo(item);
            } else if (holder instanceof SeeRequestsViewHolder) {
                SeeRequestsViewHolder seeRequestsViewHolder = (SeeRequestsViewHolder) holder;
                SeeRequestsItem item = (SeeRequestsItem) items.get(position);
                seeRequestsViewHolder.bindTo(item);
            } else if (holder instanceof DeviceContactViewHolder) {
                DeviceContactViewHolder deviceContactViewHolder = (DeviceContactViewHolder) holder;
                DeviceContactItem item = (DeviceContactItem) items.get(position);
                deviceContactViewHolder.bindTo(item);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public static abstract class ViewHolder<T> extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public void bindTo(@NonNull T item) {}
    }

    public class SeeRequestsViewHolder extends ViewHolder<SeeRequestsItem> {

        private final TextView titleView;

        public SeeRequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.text);
        }

        @Override
        public void bindTo(@NonNull SeeRequestsItem item) {
            titleView.setText(item.title);
            itemView.setOnClickListener(view -> {
                viewModel.setSelectedTab(Tab.ALL_REQUESTS);
            });
        }
    }

    public static class SectionHeaderViewHolder extends ViewHolder<SectionHeaderItem> {
        private final TextView textView;
        private final View inviteHeaderView;

        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            inviteHeaderView = itemView.findViewById(R.id.invite_header);
        }

        @Override
        public void bindTo(@NonNull SectionHeaderItem item) {
            textView.setText(item.title);
            inviteHeaderView.setVisibility(item.isInviteHeader ? View.VISIBLE : View.GONE);
        }
    }

    public class PersonViewHolder extends ViewHolder<PersonItem> {

        private UserId userId;
        private FriendState friendState;
        private final ImageView avatarView;
        private final TextView nameView;
        private final TextView usernameView;
        private final Button addView;
        private final ImageButton addedView;
        private final View optionsView;
        private final View closeView;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);
            addView = itemView.findViewById(R.id.add_friend);
            addedView = itemView.findViewById(R.id.added_friend);
            optionsView = itemView.findViewById(R.id.options);
            closeView = itemView.findViewById(R.id.close);

            itemView.setOnClickListener(view -> {
                viewProfile();
            });

            addView.setOnClickListener(view -> {
                if (this.friendState == FriendState.DEFAULT) {
                    viewModel.sendFriendRequest(userId).observe(ViewFriendsListActivity.this, success -> {
                        if (Boolean.TRUE.equals(success)) {
                            addedView.setVisibility(View.VISIBLE);
                            addView.setVisibility(View.GONE);
                            return;
                        }
                        SnackbarHelper.showWarning(ViewFriendsListActivity.this, R.string.error_send_friend_request);
                    });
                } else if (this.friendState == FriendState.INCOMING_REQUEST) {
                    viewModel.acceptFriendRequest(userId).observe(ViewFriendsListActivity.this, success -> {
                        if (Boolean.TRUE.equals(success)) {
                            return;
                        }
                        SnackbarHelper.showWarning(ViewFriendsListActivity.this, R.string.error_accept_friend_request);
                    });;
                } else if (this.friendState == FriendState.OUTGOING_REQUEST) {
                    viewModel.withdrawFriendRequest(userId).observe(ViewFriendsListActivity.this, success -> {
                        if (Boolean.TRUE.equals(success)) {
                            return;
                        }
                        SnackbarHelper.showWarning(ViewFriendsListActivity.this, R.string.error_withdraw_friend_request);
                    });
                }
            });

            closeView.setOnClickListener(view -> {
                viewModel.rejectFriendRequest(userId).observe(ViewFriendsListActivity.this, success -> {
                    if (Boolean.TRUE.equals(success)) {
                        return;
                    }
                    SnackbarHelper.showWarning(ViewFriendsListActivity.this, R.string.error_reject_friend_request);
                });
            });

            optionsView.setOnClickListener(new DebouncedClickListener() {

                @Override
                public void onOneClick(@NonNull View v) {
                    PopupMenu menu = new PopupMenu(v.getContext(), v);
                    menu.inflate(R.menu.friend_menu);
                    menu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.view_profile) {
                            viewProfile();
                            return true;
                        } else if (item.getItemId() == R.id.add_to_favorites) {
                            viewModel.addToFavorites(userId);
                            return true;
                        } else if (item.getItemId() == R.id.remove_friend) {
                            viewModel.removeFriend(userId).observe(ViewFriendsListActivity.this, success -> {
                                if (Boolean.TRUE.equals(success)) {
                                    return;
                                }
                                SnackbarHelper.showWarning(ViewFriendsListActivity.this, R.string.remove_friend);
                            });
                            return true;
                        } else if (item.getItemId() == R.id.block_contact) {
                            viewModel.blockContact(userId).observe(ViewFriendsListActivity.this, success -> {
                                if (Boolean.TRUE.equals(success)) {
                                    return;
                                }
                                SnackbarHelper.showWarning(ViewFriendsListActivity.this, R.string.error_block_contact);
                            });
                            return true;
                        }
                        return false;
                    });
                    menu.setForceShowIcon(true);
                    menu.show();
                }
            });
        }

        private void viewProfile() {
            startActivity(ViewProfileActivity.viewProfile(getBaseContext(), userId));
        }

        @Override
        public void bindTo(@NonNull PersonItem item) {
            this.userId = item.userId;
            this.nameView.setText(item.name);
            this.friendState = item.friendState;
            if (!TextUtils.isEmpty(item.username)) {
                this.usernameView.setText("@" + item.username);
            } else {
                this.usernameView.setText(null);
            }
            AvatarLoader.getInstance().load(avatarView, item.userId, item.avatarId);
            if (item.friendState == FriendState.DEFAULT) {
                addView.setText(R.string.add_friend);
                addView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(addView.getContext(), R.color.favorites_dialog_blue)));
                addView.setTextColor(ContextCompat.getColor(addView.getContext(), R.color.white));
                addView.setVisibility(View.VISIBLE);
                addedView.setVisibility(View.GONE);
                optionsView.setVisibility(View.VISIBLE);
                closeView.setVisibility(View.GONE);
            } else if (item.friendState == FriendState.INCOMING_REQUEST) {
                addView.setText(R.string.confirm_friend);
                addView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(addView.getContext(), R.color.favorites_dialog_blue)));
                addView.setTextColor(ContextCompat.getColor(addView.getContext(), R.color.white));
                addView.setVisibility(View.VISIBLE);
                addedView.setVisibility(View.GONE);
                optionsView.setVisibility(View.GONE);
                closeView.setVisibility(View.VISIBLE);
            } else if (item.friendState == FriendState.FRIENDS) {
                addView.setVisibility(View.GONE);
                addedView.setVisibility(View.GONE);
                optionsView.setVisibility(View.VISIBLE);
                closeView.setVisibility(View.GONE);
            } else if (item.friendState == FriendState.OUTGOING_REQUEST) {
                addView.setText(R.string.cancel);
                addView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(addView.getContext(), R.color.black_10)));
                addView.setTextColor(ContextCompat.getColor(addView.getContext(), R.color.favorites_dialog_blue));
                addView.setVisibility(View.VISIBLE);
                addedView.setVisibility(View.GONE);
                optionsView.setVisibility(View.GONE);
                closeView.setVisibility(View.GONE);
            }
        }
    }

    public class DeviceContactViewHolder extends ViewHolder<DeviceContactItem> {

        private final ImageView avatarView;
        private final TextView nameView;
        private final TextView phoneView;
        private final TextView mutualContactsView;
        private final Button inviteView;
        private Contact contact;

        public DeviceContactViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            phoneView = itemView.findViewById(R.id.phone);
            mutualContactsView = itemView.findViewById(R.id.mutual_contacts);
            inviteView = itemView.findViewById(R.id.invite);

            inviteView.setOnClickListener(v -> {
                if (contact != null) {
                    Log.d("Inviting contact " + contact.getDisplayName() + " with number " + contact.normalizedPhone);
                    final Context context = itemView.getContext();
                    final Intent chooser = IntentUtils.createSmsChooserIntent(context, getString(R.string.invite_friend_chooser_title, contact.getShortName()), Preconditions.checkNotNull(contact.normalizedPhone), getInviteText(contact));
                    startActivity(chooser);
                }
            });

        }

        private String getInviteText(@NonNull Contact contact) {
            return getString(R.string.invite_text_with_name_and_number, contact.getShortName(), contact.getDisplayPhone(), Constants.DOWNLOAD_LINK_URL);
        }

        @Override
        public void bindTo(@NonNull DeviceContactItem item) {
            contact = item.contact;
            final Context context = getApplicationContext();
            inviteView.setText(context.getString(contact.invited ? R.string.invite_add_invite_sent : R.string.invite_action));
            nameView.setText(contact.getDisplayName());
            phoneView.setText(contact.getDisplayPhone());
            final int numberOfContacts = (int) contact.numPotentialFriends;
            mutualContactsView.setVisibility(numberOfContacts > 0 ? View.VISIBLE : View.GONE);
            if (numberOfContacts > 0) {
                mutualContactsView.setText(getResources().getQuantityString(R.plurals.friends_on_halloapp, numberOfContacts, numberOfContacts));
            }
            deviceAvatarLoader.load(avatarView, contact.addressBookPhone);
        }
    }

    public static class NoConnectionsViewHolder extends ViewHolder<Void> {

        public NoConnectionsViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


    public static abstract class Item {

        private final int type;

        public Item(@Type int type) {
            this.type = type;
        }
    }

    public static class SectionHeaderItem extends Item {

        private final String title;
        private final boolean isInviteHeader;

        public SectionHeaderItem(@NonNull String title, boolean isInviteHeader) {
            super(Type.SECTION_HEADER);
            this.title = title;
            this.isInviteHeader = isInviteHeader;
        }
    }

    public static class PersonItem extends Item {

        private final UserId userId;
        private final String name;
        private final String username;
        private final String avatarId;
        private final FriendState friendState;

        public PersonItem(@NonNull UserId userId, @NonNull String name, @Nullable String username, @Nullable String avatarId, @Nullable FriendState friendState) {
            super(Type.PERSON);
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.avatarId = avatarId;
            this.friendState = friendState == null ? FriendState.DEFAULT : friendState;
        }
    }

    public static class DeviceContactItem extends Item {

        private final Contact contact;

        public DeviceContactItem(@NonNull Contact contact) {
            super(Type.DEVICE_CONTACT);
            this.contact = contact;
        }
    }

    public static class SeeRequestsItem extends Item {

        private final String title;

        public SeeRequestsItem(@NonNull String title) {
            super(Type.SEE_REQUESTS);
            this.title = title;
        }
    }

    public static class NoConnectionsItem extends Item {

        public NoConnectionsItem() {
            super(Type.NO_CONNECTIONS);
        }
    }

    public static class ViewFriendsListViewModel extends AndroidViewModel {

        enum Tab {
            SUGGESTIONS,
            FRIENDS,
            ALL_REQUESTS,
        }

        enum FriendState {
            DEFAULT,
            OUTGOING_REQUEST,
            INCOMING_REQUEST,
            FRIENDS,
            BLOCKED,
        }

        enum SearchState {
            Closed,
            InProgress,
            Empty,
            Failed,
            Success,
        }

        @Retention(RetentionPolicy.SOURCE)
        @IntDef({Type.SEE_REQUESTS, Type.SECTION_HEADER, Type.PERSON, Type.DEVICE_CONTACT, Type.NO_CONNECTIONS})
        public @interface Type {
            int SEE_REQUESTS = 1;
            int SECTION_HEADER = 2;
            int PERSON = 3;
            int DEVICE_CONTACT = 4;
            int NO_CONNECTIONS = 5;
        }

        private final MutableLiveData<Tab> selectedTab = new MutableLiveData<>(Tab.SUGGESTIONS);
        private final List<FriendListResponseIq.Suggestion> contactSuggestionsList = new ArrayList<>();
        private final List<FriendListResponseIq.Suggestion> friendSuggestionsList = new ArrayList<>();
        private final List<HalloappUserProfile> searchUserResultList = new ArrayList<>();
        private final List<FriendshipInfo> searchFriendsResultList = new ArrayList<>();

        public final ComputableLiveData<List<Item>> items;
        public final MutableLiveData<SearchState> searchState = new MutableLiveData<>(SearchState.Closed);

        private String suggestionsCursor;

        private final Handler mainHandler = new Handler(Looper.getMainLooper());
        private Runnable searchUsersRunnable;
        private Runnable searchFriendsRunnable;

        public Map<UserId, FriendState> friendStateMap = new HashMap<>();

        private static final int SEARCH_DELAY_MS = 300;

        private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onFriendshipsChanged(FriendshipInfo friendshipInfo) {
                UserId userId = friendshipInfo.userId;
                FriendState friendState = fromFriendshipInfo(friendshipInfo.friendshipStatus);
                friendStateMap.put(userId, friendState);
                items.invalidate();
            }
        };

        private static FriendState fromFriendshipStatus(@NonNull FriendshipStatus status) {
            switch (status) {
                case NONE_STATUS: return FriendState.DEFAULT;
                case INCOMING_PENDING: return FriendState.INCOMING_REQUEST;
                case OUTGOING_PENDING: return FriendState.OUTGOING_REQUEST;
                case FRIENDS: return FriendState.FRIENDS;
                default: return FriendState.DEFAULT;
            }
        }

        private static FriendState fromFriendshipInfo(int status) {
            if (FriendshipInfo.Type.FRIENDS == status) {
                return FriendState.FRIENDS;
            } else if (FriendshipInfo.Type.OUTGOING_PENDING == status) {
                return FriendState.OUTGOING_REQUEST;
            } else if (FriendshipInfo.Type.INCOMING_PENDING == status) {
                return FriendState.INCOMING_REQUEST;
            } else if (FriendshipInfo.Type.BLOCKED == status) {
                return FriendState.BLOCKED;
            } else if (FriendshipInfo.Type.NONE_STATUS == status) {
                return FriendState.DEFAULT;
            }
            return FriendState.DEFAULT;
        }

        @SuppressLint("RestrictedApi")
        public ViewFriendsListViewModel(@NonNull Application application) {
            super(application);

            items = new ComputableLiveData<List<Item>>() {
                @Override
                protected List<Item> compute() {
                    return computeFriendItems();

                }
            };

            ContactsDb.getInstance().addObserver(contactsObserver);
            fetchSuggestions();
        }

        @Override
        protected void onCleared() {
            super.onCleared();
            ContactsDb.getInstance().removeObserver(contactsObserver);
        }

        @SuppressLint("RestrictedApi")
        private void fetchSuggestions() {
            Connection.getInstance().requestFriendList(suggestionsCursor, FriendListRequest.Action.GET_SUGGESTIONS).onResponse(response -> {
                if (!response.success) {
                    Log.e("Fetching friend suggestions was not successful");
                } else {
                    suggestionsCursor = response.cursor;

                    Map<UserId, String> names = new HashMap<>();
                    Map<UserId, String> usernames = new HashMap<>();
                    Map<UserId, String> avatars = new HashMap<>();
                    List<FriendListResponseIq.Suggestion> contactSuggestions = new ArrayList<>();
                    List<FriendListResponseIq.Suggestion> friendSuggestions = new ArrayList<>();

                    for (FriendListResponseIq.Suggestion suggestion : response.suggestions) {
                        UserId userId = suggestion.info.userId;
                        names.put(userId, suggestion.info.name);
                        usernames.put(userId, suggestion.info.username);
                        avatars.put(userId, suggestion.info.avatarId);
                        if (suggestion.info.friendshipStatus != FriendshipInfo.Type.NONE_STATUS) {
                            continue;
                        }
                        switch (suggestion.type) {
                            case CONTACT: {
                                contactSuggestions.add(suggestion);
                                break;
                            }
                            case FOF:
                            case PENDING: {
                                friendSuggestions.add(suggestion);
                                break;
                            }
                            default: {
                                Log.e("Unknown suggestion type: " + suggestion.type);
                                break;
                            }
                        }
                    }

                    Comparator<FriendListResponseIq.Suggestion> comparator = (o1, o2) -> o1.rank - o2.rank;
                    Collections.sort(contactSuggestions, comparator);
                    Collections.sort(friendSuggestions, comparator);

                    contactSuggestionsList.clear();
                    contactSuggestionsList.addAll(contactSuggestions);
                    friendSuggestionsList.clear();
                    friendSuggestionsList.addAll(friendSuggestions);

                    items.invalidate();

                    ContactsDb contactsDb = ContactsDb.getInstance();
                    contactsDb.updateUserNames(names);
                    contactsDb.updateUserUsernames(usernames);
                    contactsDb.updateUserAvatars(avatars);
                }
            }).onError(error -> {
                Log.e("Fetching friend suggestions has an error", error);
            });
        }

        @SuppressLint("RestrictedApi")
        public void setSelectedTab(@NonNull Tab tab) {
            selectedTab.setValue(tab);
            items.invalidate();
        }

        @SuppressLint("RestrictedApi")
        public void updateFriendState(@NonNull UserId userId, @NonNull FriendState friendState) {
            friendStateMap.put(userId, friendState);
            items.invalidate();
        }

        public MutableLiveData<Tab> getSelectedTab() {
            return selectedTab;
        }

        private void updateSearchText(@NonNull String searchText) {
            searchUsers(searchText);
            searchFriends(searchText);
        }

        @SuppressLint("RestrictedApi")
        private void searchUsers(@NonNull String s) {
            if (searchUsersRunnable != null) {
                mainHandler.removeCallbacks(searchUsersRunnable);
            }
            if (TextUtils.isEmpty(s)) {
                searchUserResultList.clear();
                searchState.setValue(SearchState.Closed);
            } else {
                searchState.postValue(SearchState.InProgress);
                searchUsersRunnable = () -> {
                    Connection.getInstance().searchForHalloappUser(s).onResponse(response -> {
                        if (!response.success) {
                            searchState.postValue(SearchState.Failed);
                            Log.e("Failed to get search results");
                        } else {
                            ContactsDb contactsDb = ContactsDb.getInstance();
                            Map<UserId, String> names = new HashMap<>();
                            Map<UserId, String> usernames = new HashMap<>();
                            Map<UserId, String> avatars = new HashMap<>();
                            searchUserResultList.clear();
                            searchUserResultList.addAll(response.profiles);
                            contactsDb.updateUserNames(names);
                            contactsDb.updateUserUsernames(usernames);
                            contactsDb.updateUserAvatars(avatars);
                            searchState.postValue(response.profiles.isEmpty() ? SearchState.Empty : SearchState.Success);
                            items.invalidate();
                        }
                    }).onError(err -> {
                        searchState.postValue(SearchState.Failed);
                        Log.e("User search got error", err);
                    });
                };
                mainHandler.postDelayed(searchUsersRunnable, SEARCH_DELAY_MS);
            }
            items.invalidate();
        }

        @SuppressLint("RestrictedApi")
        private void searchFriends(@NonNull String s) {
            if (searchFriendsRunnable != null) {
                mainHandler.removeCallbacks(searchFriendsRunnable);
            }

            if (s.length() < Constants.MIN_NAME_SEARCH_LENGTH) {
                searchFriendsResultList.clear();
            } else {
                searchFriendsRunnable = () -> {
                    BgWorkers.getInstance().execute(() -> {
                        final List<FriendshipInfo> friends = ContactsDb.getInstance().getFriendships(FriendshipInfo.Type.FRIENDS);
                        final Locale locale = Locale.getDefault();
                        final String lowerSearchText = s.toLowerCase(locale);
                        final ListIterator<FriendshipInfo> iterator = friends.listIterator();
                        while (iterator.hasNext()) {
                            final FriendshipInfo friend = iterator.next();
                            final String name = friend.name.toLowerCase(locale);
                            final String username = friend.username.toLowerCase(locale);
                            if (!name.startsWith(lowerSearchText) && !username.startsWith(lowerSearchText)) {
                                iterator.remove();
                            }
                        }
                        searchFriendsResultList.clear();
                        searchFriendsResultList.addAll(friends);
                        items.invalidate();
                    });
                };
                mainHandler.postDelayed(searchFriendsRunnable, SEARCH_DELAY_MS);
            }
            items.invalidate();
        }

        private List<Item> generateContactsInviteItemList() {
            final List<Contact> contacts = ContactsDb.getInstance().getSuggestedContactsForInvite(false);

            final List<Item> list = new ArrayList<>();
            if (!contacts.isEmpty()) {
                list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_friends_on_halloapp_title), true));
            }
            for (Contact contact : contacts) {
                list.add(new DeviceContactItem(contact));
            }
            return list;
        }

        private List<Item> computeFriendItems() {
            final ContactsDb contactsDb = ContactsDb.getInstance();
            List<Item> list = new ArrayList<>();

            if (searchState.getValue() != SearchState.Closed) {
                boolean isFirstOne = true;
                for (FriendshipInfo friend : searchFriendsResultList) {
                    FriendState friendState = friendStateMap.get(friend.userId) == null ? fromFriendshipInfo(friend.friendshipStatus) : friendStateMap.get(friend.userId);
                    if (friendState != FriendState.FRIENDS) {
                        continue;
                    }
                    if (isFirstOne) {
                        list.add(new SectionHeaderItem(getApplication().getString(R.string.halloapp_friends), false));
                        isFirstOne = false;
                    }
                    list.add(new PersonItem(
                            friend.userId,
                            friend.name,
                            friend.username,
                            friend.avatarId,
                            friendState));
                }
                isFirstOne = true;
                for (HalloappUserProfile userProfile : searchUserResultList) {
                    if (userProfile.getBlocked()) {
                        continue;
                    }
                    UserId userId = new UserId(Long.toString(userProfile.getUid()));
                    FriendState friendState = friendStateMap.get(userId) == null ? fromFriendshipStatus(userProfile.getStatus()) : friendStateMap.get(userId);
                    if (friendState == FriendState.FRIENDS || friendState == FriendState.BLOCKED) {
                        continue;
                    }
                    if (isFirstOne) {
                        list.add(new SectionHeaderItem(getApplication().getString(R.string.halloapp_users), false));
                        isFirstOne = false;
                    }
                    list.add(new PersonItem(
                            userId,
                            userProfile.getName(),
                            userProfile.getUsername(),
                            userProfile.getAvatarId(),
                            friendState));
                }
                return list;
            }

            List<FriendshipInfo> incomingRequestsList = contactsDb.getFriendships(FriendshipInfo.Type.INCOMING_PENDING);
            List<FriendshipInfo> outgoingRequestsList = contactsDb.getFriendships(FriendshipInfo.Type.OUTGOING_PENDING);
            Tab tab = selectedTab.getValue();
            if (tab == Tab.SUGGESTIONS) {
                if (!incomingRequestsList.isEmpty() || !outgoingRequestsList.isEmpty()) {
                    list.add(new SectionHeaderItem(getApplication().getString(R.string.friend_requests), false));
                    if (!incomingRequestsList.isEmpty()) {
                        FriendshipInfo friend = incomingRequestsList.get(0);
                        list.add(new PersonItem(
                                friend.userId,
                                friend.name,
                                friend.username,
                                friend.avatarId,
                                FriendState.INCOMING_REQUEST));
                    }
                    list.add(new SeeRequestsItem(getApplication().getString(R.string.see_all_requests)));
                }

                list.add(new SectionHeaderItem(getApplication().getString(R.string.from_contacts_list), false));
                if (contactSuggestionsList.isEmpty()) {
                    list.add(new NoConnectionsItem());
                } else {
                    for (FriendListResponseIq.Suggestion suggestion : contactSuggestionsList) {
                        list.add(new PersonItem(
                                suggestion.info.userId,
                                suggestion.info.name,
                                suggestion.info.username,
                                suggestion.info.avatarId,
                                friendStateMap.get(suggestion.info.userId)));
                    }
                }

                if (!friendSuggestionsList.isEmpty()) {
                    list.add(new SectionHeaderItem(getApplication().getString(R.string.friend_suggestions), false));
                    for (FriendListResponseIq.Suggestion suggestion : friendSuggestionsList) {
                        list.add(new PersonItem(
                                suggestion.info.userId,
                                suggestion.info.name,
                                suggestion.info.username,
                                suggestion.info.avatarId,
                                friendStateMap.get(suggestion.info.userId)));
                    }
                }
            } else if (tab == Tab.FRIENDS) {
                List<FriendshipInfo> friendsList = contactsDb.getFriendships(FriendshipInfo.Type.FRIENDS);
                list.add(new SectionHeaderItem(getApplication().getString(R.string.my_friends_on_halloapp), false));
                if (friendsList.isEmpty()) {
                    list.add(new NoConnectionsItem());
                } else {
                    for (FriendshipInfo friend : friendsList) {
                        list.add(new PersonItem(
                                friend.userId,
                                friend.name,
                                friend.username,
                                friend.avatarId,
                                FriendState.FRIENDS));
                    }
                }

                if (!incomingRequestsList.isEmpty() || !outgoingRequestsList.isEmpty()) {
                    list.add(new SeeRequestsItem(getApplication().getString(R.string.see_outgoing_requests)));
                }

                list.addAll(generateContactsInviteItemList());
            } else if (tab == Tab.ALL_REQUESTS) {
                if (!incomingRequestsList.isEmpty()) {
                    list.add(new SectionHeaderItem(getApplication().getString(R.string.requests_received), false));
                }
                for (FriendshipInfo friend : incomingRequestsList) {
                    list.add(new PersonItem(
                            friend.userId,
                            friend.name,
                            friend.username,
                            friend.avatarId,
                            FriendState.INCOMING_REQUEST));
                }

                if (!outgoingRequestsList.isEmpty()) {
                    list.add(new SectionHeaderItem(getApplication().getString(R.string.requests_sent), false));
                }
                for (FriendshipInfo friend : outgoingRequestsList) {
                    list.add(new PersonItem(
                            friend.userId,
                            friend.name,
                            friend.username,
                            friend.avatarId,
                            FriendState.OUTGOING_REQUEST));
                }
            }
            return list;
        }

        public void addToFavorites(@NonNull UserId userId) {
            BgWorkers.getInstance().execute(() -> {
                FeedPrivacyManager.getInstance().updateFeedPrivacy(PrivacyList.Type.ONLY, new ArrayList<>(Collections.singletonList(userId)));
            });
        }

        public LiveData<Boolean> removeFriend(@NonNull UserId userId) {
            MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
            Connection.getInstance().removeFriend(userId).onResponse(response -> {
                if (!response.success) {
                    Log.e("Unable to remove friend " + userId);
                    result.postValue(false);
                } else {
                    updateFriendState(userId, FriendState.DEFAULT);
                    ContactsDb.getInstance().removeFriendship(response.info);
                    result.postValue(true);
                }
            }).onError(e -> {
                Log.e("Unable to withdraw friend request", e);
                result.postValue(false);
            });
            return result;
        }

        public LiveData<Boolean> blockContact(@NonNull UserId userId) {
            MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
            Connection.getInstance().blockFriend(userId).onResponse(response -> {
                if (!response.success) {
                    Log.e("Unable to block user: " + userId);
                    result.postValue(false);
                } else {
                    updateFriendState(userId, FriendState.BLOCKED);
                    ContactsDb.getInstance().addFriendship(response.info);
                    result.postValue(true);
                }
            }).onError(e -> {
                Log.e("Unable to block user", e);
                result.postValue(false);
            });
            return result;
        }

        public LiveData<Boolean> rejectFriendRequest(@NonNull UserId userId) {
            MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
            Connection.getInstance().rejectFriendRequest(userId).onResponse(response -> {
                if (!response.success) {
                    Log.e("Unable to reject the friend request of " + userId);
                    result.postValue(false);
                } else {
                    updateFriendState(userId, FriendState.DEFAULT);
                    ContactsDb.getInstance().removeFriendship(response.info);
                    result.postValue(true);
                }
            }).onError(e -> {
                Log.e("Unable to reject friend request", e);
                result.postValue(false);
            });
            return result;
        }

        public LiveData<Boolean> sendFriendRequest(@NonNull UserId userId) {
            MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
            Connection.getInstance().sendFriendRequest(userId).onResponse(response -> {
                if (!response.success) {
                    Log.e("Unable to send a friend request to " + userId);
                    result.postValue(false);
                } else {
                    updateFriendState(userId, FriendState.OUTGOING_REQUEST);
                    ContactsDb.getInstance().addFriendship(response.info);
                    result.postValue(true);
                }
            }).onError(e -> {
                Log.e("Unable to send friend request", e);
                result.postValue(false);
            });
            return result;
        }

        public LiveData<Boolean> acceptFriendRequest(@NonNull UserId userId) {
            MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
            Connection.getInstance().acceptFriendRequest(userId).onResponse(response -> {
                if (!response.success) {
                    Log.e("Unable to confirm the friend request of " + userId);
                    result.postValue(false);
                } else {
                    updateFriendState(userId, FriendState.FRIENDS);
                    ContactsDb.getInstance().addFriendship(response.info);
                    result.postValue(true);
                }
            }).onError(e -> {
                Log.e("Unable to confirm friend request", e);
                result.postValue(false);
            });
            return result;
        }

        public LiveData<Boolean> withdrawFriendRequest(@NonNull UserId userId) {
            MutableLiveData<Boolean> result = new DelayedProgressLiveData<>();
            Connection.getInstance().withdrawFriendRequest(userId).onResponse(response -> {
                if (!response.success) {
                    Log.e("Unable to withdraw the friend request of " + userId);
                    result.postValue(false);
                } else {
                    updateFriendState(userId, FriendState.DEFAULT);
                    ContactsDb.getInstance().removeFriendship(response.info);
                    result.postValue(true);
                }
            }).onError(e -> {
                Log.e("Unable to withdraw friend request", e);
                result.postValue(false);
            });
            return result;
        }
    }
}
