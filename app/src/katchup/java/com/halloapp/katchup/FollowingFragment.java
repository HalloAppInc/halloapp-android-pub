package com.halloapp.katchup;

import android.Manifest;
import android.app.Application;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ComputableLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.halloapp.ConnectionObservers;
import com.halloapp.MainActivity;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.proto.server.BasicUserProfile;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.FollowSuggestionsResponseIq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pub.devrel.easypermissions.EasyPermissions;

public class FollowingFragment extends HalloFragment {
    public static final String ARG_ONBOARDING_MODE = "onboarding_mode";

    private static final int TYPE_INVITE_LINK_HEADER = 1;
    private static final int TYPE_SECTION_HEADER = 2;
    private static final int TYPE_PERSON = 3;
    private static final int TYPE_MISSING_CONTACT_PERMISSIONS = 4;

    private static final int TAB_ADD = 1;
    private static final int TAB_FOLLOWING = 2;
    private static final int TAB_FOLLOWERS = 3;
    private static final int TAB_SEARCH = 4;

    private static final int SEARCH_DELAY_MS = 300;

    public interface NextScreenHandler {
        void nextScreen();
    }

    private InviteViewModel viewModel;

    private InviteAdapter adapter = new InviteAdapter();
    private KAvatarLoader kAvatarLoader = KAvatarLoader.getInstance();

    private View tabButtonContainer;
    private View addButton;
    private View followingButton;
    private View followersButton;
    private EditText searchEditText;
    private View clearSearch;
    private View noResults;
    private View failedToLoad;
    private View tryAgain;

    private boolean syncInFlight;
    private boolean onboardingMode;
    private int numFollowedDuringOnboarding = 0;

    public static FollowingFragment newInstance(boolean onboardingMode) {
        final FollowingFragment followingFragment = new FollowingFragment();

        final Bundle arguments = new Bundle();
        arguments.putBoolean(ARG_ONBOARDING_MODE, onboardingMode);
        followingFragment.setArguments(arguments);

        return followingFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_following, container, false);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            onboardingMode = getArguments().getBoolean(ARG_ONBOARDING_MODE, false);
        }

        View next = root.findViewById(R.id.next);
        next.setOnClickListener(v -> {
            NextScreenHandler activity = (NextScreenHandler) getActivity();
            activity.nextScreen();
        });

        RecyclerView listView = root.findViewById(R.id.recycler_view);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireContext());
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        tabButtonContainer = root.findViewById(R.id.user_list_type_container);
        addButton = root.findViewById(R.id.user_list_type_add);
        addButton.setOnClickListener(v -> viewModel.setSelectedTab(TAB_ADD));
        followingButton = root.findViewById(R.id.user_list_type_following);
        followingButton.setOnClickListener(v -> viewModel.setSelectedTab(TAB_FOLLOWING));
        followersButton = root.findViewById(R.id.user_list_type_followers);
        followersButton.setOnClickListener(v -> viewModel.setSelectedTab(TAB_FOLLOWERS));

        noResults = root.findViewById(R.id.no_results);
        failedToLoad = root.findViewById(R.id.failed_to_load);
        tryAgain = root.findViewById(R.id.try_search_again);

        viewModel = new ViewModelProvider(requireActivity()).get(InviteViewModel.class);

        viewModel.items.getLiveData().observe(getViewLifecycleOwner(), items -> adapter.setItems(items));

        viewModel.selectedTab.observe(getViewLifecycleOwner(), this::setSelectedTab);

        viewModel.searchState.observe(getViewLifecycleOwner(), state -> {
            clearSearch.setVisibility(InviteViewModel.SearchState.Closed.equals(state) ? View.GONE : View.VISIBLE);
            tabButtonContainer.setVisibility(!onboardingMode && InviteViewModel.SearchState.Closed.equals(state) ? View.VISIBLE : View.GONE);
            noResults.setVisibility(InviteViewModel.SearchState.Empty.equals(state) ? View.VISIBLE : View.GONE);
            failedToLoad.setVisibility(InviteViewModel.SearchState.Failed.equals(state) ? View.VISIBLE : View.GONE);
        });

        clearSearch = root.findViewById(R.id.search_clear);
        searchEditText = root.findViewById(R.id.search_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateSearchText(s.toString());
            }
        });
        clearSearch.setOnClickListener(v -> searchEditText.setText(""));

        tryAgain.setOnClickListener(v -> viewModel.updateSearchText(searchEditText.getText().toString()));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        Analytics.getInstance().openScreen("friendsPage");

        // keep up to date the user own avatar
        for (int i = 0; i < adapter.getItemCount(); i++) {
            if (adapter.getItemViewType(i) == TYPE_INVITE_LINK_HEADER) {
                adapter.notifyItemChanged(i);
            }
        }
    }

    private void setSelectedTab(int selectedTab) {
        Drawable selectedDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.selected_feed_type_background);
        Drawable unselectedDrawable = null;
        addButton.setBackground(selectedTab == TAB_ADD ? selectedDrawable : unselectedDrawable);
        followingButton.setBackground(selectedTab == TAB_FOLLOWING ? selectedDrawable : unselectedDrawable);
        followersButton.setBackground(selectedTab == TAB_FOLLOWERS ? selectedDrawable : unselectedDrawable);
    }

    private void requestContacts() {
        final ContactsSync contactsSync = ContactsSync.getInstance();
        contactsSync.cancelContactsSync();
        contactsSync.getWorkInfoLiveData()
                .observe(getViewLifecycleOwner(), workInfos -> {
                    if (workInfos != null) {
                        for (WorkInfo workInfo : workInfos) {
                            if (workInfo.getId().equals(contactsSync.getLastFullSyncRequestId())) {
                                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                    syncInFlight = false;
                                    viewModel.items.invalidate();
                                } else if (workInfo.getState().isFinished()) {
                                    syncInFlight = false;
                                }
                                break;
                            }
                        }
                    }
                });

        tryStartSync();
    }

    private void tryStartSync() {
        final String[] perms = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
        if (!EasyPermissions.hasPermissions(requireContext(), perms)) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
            builder.setTitle(R.string.katchup_contact_permission_request_title);
            builder.setMessage(R.string.contact_rationale_upload);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                ActivityCompat.requestPermissions(requireActivity(), perms, MainActivity.REQUEST_CODE_ASK_CONTACTS_PERMISSION);
            });
            builder.setNegativeButton(R.string.dont_allow, null);
            builder.show();
        } else if (!syncInFlight) {
            startSync();
        }
    }

    private void startSync() {
        syncInFlight = true;
        Preferences.getInstance().clearContactSyncBackoffTime();
        ContactsSync.getInstance().forceFullContactsSync(true);
    }

    public class InviteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Item> items = new ArrayList<>();

        public void setItems(List<Item> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).type;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_INVITE_LINK_HEADER:
                    return new LinkHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_item_link_header, parent, false));
                case TYPE_SECTION_HEADER:
                    return new SectionHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_item_section_header, parent, false));
                case TYPE_PERSON:
                    return new PersonViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_item_person, parent, false), () -> viewModel.fetchSuggestions());
                case TYPE_MISSING_CONTACT_PERMISSIONS:
                    return new MissingContactPermissionsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_item_contact_permissions, parent, false), () -> requestContacts());
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
            } else if (holder instanceof LinkHeaderViewHolder) {
                LinkHeaderViewHolder linkHeaderViewHolder = (LinkHeaderViewHolder) holder;
                LinkHeaderItem item = (LinkHeaderItem) items.get(position);
                linkHeaderViewHolder.bindTo(item);
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

        public void bindTo(T item) {};
    }

    public class LinkHeaderViewHolder extends ViewHolder<LinkHeaderItem> {
        public LinkHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bindTo(LinkHeaderItem item) {
            kAvatarLoader.load(itemView.findViewById(R.id.avatar), UserId.ME);
            TextView linkView = itemView.findViewById(R.id.bottom_text);
            linkView.setText(item.profileLink);
            itemView.setOnClickListener(v -> {
                startActivity(IntentUtils.createShareTextIntent(item.profileLink));
            });
        }
    }

    public static class SectionHeaderViewHolder extends ViewHolder<SectionHeaderItem> {
        private final TextView textView;
        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
        }

        @Override
        public void bindTo(SectionHeaderItem item) {
            textView.setText(item.title);
        }
    }

    public class PersonViewHolder extends ViewHolder<PersonItem> {
        private final ImageView avatarView;
        private final TextView nameView;
        private final TextView usernameView;
        private final View addView;
        private final View closeView;
        private final View followsYouView;
        private final TextView mutuals;

        private UserId userId;

        public PersonViewHolder(@NonNull View itemView, @NonNull Runnable reloadList) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);
            addView = itemView.findViewById(R.id.add);
            closeView = itemView.findViewById(R.id.close);
            followsYouView = itemView.findViewById(R.id.follows_you);
            mutuals = itemView.findViewById(R.id.mutuals);

            addView.setOnClickListener(v -> {
                BgWorkers.getInstance().execute(() -> {
                    RelationshipApi.getInstance().requestFollowUser(userId).onResponse(success -> {
                        if (Boolean.TRUE.equals(success)) {
                            if (onboardingMode) {
                                numFollowedDuringOnboarding++;
                            }
                            reloadList.run();
                        }
                    }).onError(error -> {
                        Log.e("Failed to request follow user", error);
                    });
                });
            });

            closeView.setOnClickListener(v -> {
                BgWorkers.getInstance().execute(() -> {
                    Connection.getInstance().rejectFollowSuggestion(userId).onResponse(res -> {
                        reloadList.run();
                    }).onError(error -> {
                        Log.e("Failed to reject follow suggestion", error);
                    });
                });
            });

            itemView.setOnClickListener(v -> {
                startActivity(ViewKatchupProfileActivity.viewProfile(requireContext(), userId));
            });
        }

        @Override
        public void bindTo(PersonItem item) {
            this.userId = item.userId;
            this.nameView.setText(item.name);
            this.usernameView.setText("@" + item.username);
            kAvatarLoader.load(avatarView, item.userId, item.avatarId);
            if (item.tab == TAB_ADD) {
                addView.setVisibility(View.VISIBLE);
                closeView.setVisibility(onboardingMode ? View.GONE : View.VISIBLE);
                followsYouView.setVisibility(View.GONE);
                mutuals.setVisibility(item.mutuals > 0 ? View.VISIBLE : View.GONE);
                mutuals.setText(getResources().getQuantityString(R.plurals.mutual_followers_count, item.mutuals, item.mutuals));
            } else if (item.tab == TAB_FOLLOWING) {
                addView.setVisibility(View.GONE);
                closeView.setVisibility(View.GONE);
                followsYouView.setVisibility(item.follower ? View.VISIBLE : View.GONE);
                mutuals.setVisibility(View.GONE);
            } else if (item.tab == TAB_FOLLOWERS) {
                addView.setVisibility(item.following ? View.GONE : View.VISIBLE);
                closeView.setVisibility(View.GONE);
                followsYouView.setVisibility(View.GONE);
                mutuals.setVisibility(View.GONE);
            } else if (item.tab == TAB_SEARCH) {
                addView.setVisibility(item.following ? View.GONE : View.VISIBLE);
                closeView.setVisibility(View.GONE);
                followsYouView.setVisibility(item.follower ? View.VISIBLE : View.GONE);
                mutuals.setVisibility(item.mutuals > 0 ? View.VISIBLE : View.GONE);
                mutuals.setText(getResources().getQuantityString(R.plurals.mutual_followers_count, item.mutuals, item.mutuals));
            }
        }
    }

    public static class MissingContactPermissionsViewHolder extends ViewHolder<Void> {
        public MissingContactPermissionsViewHolder(@NonNull View itemView, @NonNull Runnable onClick) {
            super(itemView);

            itemView.findViewById(R.id.see_contacts).setOnClickListener(v -> {
                onClick.run();
            });
        }
    }

    public static abstract class Item {
        private final int type;
        public Item(int type) {
            this.type = type;
        }
    }

    public static class LinkHeaderItem extends Item {
        private final String profileLink;
        public LinkHeaderItem(@NonNull String profileLink) {
            super(TYPE_INVITE_LINK_HEADER);
            this.profileLink = profileLink;
        }
    }

    public static class SectionHeaderItem extends Item {
        private final String title;
        public SectionHeaderItem(@NonNull String title) {
            super(TYPE_SECTION_HEADER);
            this.title = title;
        }
    }

    public static class PersonItem extends Item {
        private final UserId userId;
        private final String name;
        private final String username;
        private final String avatarId;
        private final boolean follower;
        private final boolean following;
        private final int mutuals;
        private final int tab;

        public PersonItem(@NonNull UserId userId, String name, String username, String avatarId, boolean follower, boolean following, int mutuals, int tab) {
            super(TYPE_PERSON);
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.avatarId = avatarId;
            this.follower = follower;
            this.following = following;
            this.mutuals = mutuals;
            this.tab = tab;
        }
    }

    public static class MissingContactPermissionsItem extends Item {
        public MissingContactPermissionsItem() {
            super(TYPE_MISSING_CONTACT_PERMISSIONS);
        }
    }

    public static class InviteViewModel extends AndroidViewModel {
        enum SearchState {
            Closed,
            InProgress,
            Empty,
            Failed,
            Success
        }

        public final List<FollowSuggestionsResponseIq.Suggestion> contactSuggestions = new ArrayList<>();
        public final List<FollowSuggestionsResponseIq.Suggestion> fofSuggestions = new ArrayList<>();
        public final List<BasicUserProfile> searchResults = new ArrayList<>();

        public final ComputableLiveData<List<Item>> items;
        public final MutableLiveData<Integer> selectedTab = new MutableLiveData<>(TAB_ADD);
        public final MutableLiveData<SearchState> searchState = new MutableLiveData<>(SearchState.Closed);

        private final Handler mainHandler = new Handler(Looper.getMainLooper());
        private Runnable searchRunnable;

        private final Connection.Observer connectionObserver = new Connection.Observer() {
            @Override
            public void onConnected() {
                fetchSuggestions();
            }
        };

        private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
            @Override
            public void onRelationshipsChanged() {
                items.invalidate();
            }
        };

        public InviteViewModel(@NonNull Application application) {
            super(application);

            fetchSuggestions();
            ConnectionObservers.getInstance().addObserver(connectionObserver);
            ContactsDb.getInstance().addObserver(contactsObserver);

            items = new ComputableLiveData<List<Item>>() {
                @Override
                protected List<Item> compute() {
                    return computeInviteItems();
                }
            };
        }

        private void updateSearchText(@NonNull String s) {
            if (searchRunnable != null) {
                mainHandler.removeCallbacks(searchRunnable);
            }
            if (TextUtils.isEmpty(s)) {
                searchResults.clear();
                searchState.setValue(SearchState.Closed);
            } else {
                Analytics.getInstance().openScreen("search");
                searchState.postValue(SearchState.InProgress);
                searchRunnable = () -> {
                    Connection.getInstance().searchForUser(s).onResponse(response -> {
                        if (!response.success) {
                            searchState.postValue(SearchState.Failed);
                            Log.e("Failed to get search results");
                        } else {
                            searchResults.clear();
                            searchResults.addAll(response.profiles);
                            searchState.postValue(response.profiles.isEmpty() ? SearchState.Empty : SearchState.Success);
                            items.invalidate();
                        }
                    }).onError(err -> {
                        searchState.postValue(SearchState.Failed);
                        Log.e("User search got error", err);
                    });
                };
                mainHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }
            items.invalidate();
        }

        private void fetchSuggestions() {
            Connection.getInstance().requestFollowSuggestions().onResponse(response -> {
                if (!response.success) {
                    Log.e("Suggestion fetch was not successful");
                } else {
                    Map<UserId, String> names = new HashMap<>();
                    List<FollowSuggestionsResponseIq.Suggestion> contacts = new ArrayList<>();
                    List<FollowSuggestionsResponseIq.Suggestion> fof = new ArrayList<>();

                    for (FollowSuggestionsResponseIq.Suggestion suggestion : response.suggestions) {
                        names.put(suggestion.info.userId, suggestion.info.name);
                        switch (suggestion.type) {
                            case Contact: {
                                contacts.add(suggestion);
                                break;
                            }
                            case Fof: {
                                fof.add(suggestion);
                                break;
                            }
                            case Campus: {
                                Log.w("Dropping campus suggestion " + suggestion.info.username);
                                break;
                            }
                        }
                    }

                    Comparator<FollowSuggestionsResponseIq.Suggestion> comparator = (o1, o2) -> o1.rank - o2.rank;
                    Collections.sort(contacts, comparator);
                    Collections.sort(fof, comparator);

                    contactSuggestions.clear();
                    contactSuggestions.addAll(contacts);
                    fofSuggestions.clear();
                    fofSuggestions.addAll(fof);

                    items.invalidate();

                    ContactsDb.getInstance().updateUserNames(names);
                }
            }).onError(error -> {
                Log.e("Suggestion fetch got error", error);
            });
        }

        private List<Item> computeInviteItems() {
            List<Item> list = new ArrayList<>();

            List<RelationshipInfo> followers = ContactsDb.getInstance().getRelationships(RelationshipInfo.Type.FOLLOWER);
            List<RelationshipInfo> following = ContactsDb.getInstance().getRelationships(RelationshipInfo.Type.FOLLOWING);
            Set<UserId> followerUserIds = new HashSet<>();
            Set<UserId> followingUserIds = new HashSet<>();
            for (RelationshipInfo follower : followers) {
                followerUserIds.add(follower.userId);
            }
            for (RelationshipInfo followed : following) {
                followingUserIds.add(followed.userId);
            }

            if (searchState.getValue() != SearchState.Closed) {
                for (BasicUserProfile userProfile : searchResults) {
                    UserId userId = new UserId(Long.toString(userProfile.getUid()));
                    list.add(new PersonItem(
                            userId,
                            userProfile.getName(),
                            userProfile.getUsername(),
                            userProfile.getAvatarId(),
                            followerUserIds.contains(userId),
                            followingUserIds.contains(userId),
                            userProfile.getNumMutualFollowing(),
                            TAB_SEARCH));
                }
                return list;
            }

            String profileLink = "katchup.com/" + Me.getInstance().getUsername();
            list.add(new LinkHeaderItem(profileLink));

            int tab = Preconditions.checkNotNull(selectedTab.getValue());
            if (tab == TAB_ADD) {
                if (!EasyPermissions.hasPermissions(getApplication(), android.Manifest.permission.READ_CONTACTS)) {
                    list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_phone_contacts)));
                    list.add(new MissingContactPermissionsItem());
                } else {
                    if (!contactSuggestions.isEmpty()) {
                        list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_phone_contacts)));
                    }
                    for (FollowSuggestionsResponseIq.Suggestion suggestion : contactSuggestions) {
                        list.add(new PersonItem(
                                suggestion.info.userId,
                                suggestion.info.name,
                                suggestion.info.username,
                                suggestion.info.avatarId,
                                followerUserIds.contains(suggestion.info.userId),
                                followingUserIds.contains(suggestion.info.userId),
                                suggestion.mutuals,
                                TAB_ADD));
                    }
                }
                list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_friends_of_friends)));
                for (FollowSuggestionsResponseIq.Suggestion suggestion : fofSuggestions) {
                    list.add(new PersonItem(
                            suggestion.info.userId,
                            suggestion.info.name,
                            suggestion.info.username,
                            suggestion.info.avatarId,
                            followerUserIds.contains(suggestion.info.userId),
                            followingUserIds.contains(suggestion.info.userId),
                            suggestion.mutuals,
                            TAB_ADD));
                }
            } else if (tab == TAB_FOLLOWING) {
                for (RelationshipInfo info : following) {
                    list.add(new PersonItem(
                            info.userId,
                            info.name,
                            info.username,
                            info.avatarId,
                            followerUserIds.contains(info.userId),
                            followingUserIds.contains(info.userId),
                            0,
                            TAB_FOLLOWING));
                }
            } else if (tab == TAB_FOLLOWERS) {
                for (RelationshipInfo info : followers) {
                    list.add(new PersonItem(
                            info.userId,
                            info.name,
                            info.username,
                            info.avatarId,
                            followerUserIds.contains(info.userId),
                            followingUserIds.contains(info.userId),
                            0,
                            TAB_FOLLOWERS));
                }
            }

            return list;
        }

        @MainThread
        public void setSelectedTab(int selectedTab) {
            this.selectedTab.setValue(selectedTab);
            items.invalidate();
        }

        @Override
        protected void onCleared() {
            ConnectionObservers.getInstance().removeObserver(connectionObserver);
            ContactsDb.getInstance().removeObserver(contactsObserver);
        }
    }

    public int getNumFollowedDuringOnboarding() {
        return numFollowedDuringOnboarding;
    }
}
