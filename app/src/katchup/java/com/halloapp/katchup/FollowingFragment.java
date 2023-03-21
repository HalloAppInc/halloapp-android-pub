package com.halloapp.katchup;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkInfo;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.halloapp.ConnectionObservers;
import com.halloapp.Constants;
import com.halloapp.MainActivity;
import com.halloapp.Me;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.katchup.avatar.KDeviceAvatarLoader;
import com.halloapp.proto.server.BasicUserProfile;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.FollowSuggestionsResponseIq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import pub.devrel.easypermissions.EasyPermissions;

public class FollowingFragment extends HalloFragment {
    public static final String ARG_ONBOARDING_MODE = "onboarding_mode";

    private static final int TYPE_SEE_MORE = 1;
    private static final int TYPE_SECTION_HEADER = 2;
    private static final int TYPE_PERSON = 3;
    private static final int TYPE_DEVICE_CONTACT = 4;
    private static final int TYPE_MISSING_CONTACT_PERMISSIONS = 5;

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
    private KDeviceAvatarLoader kDeviceAvatarLoader;

    private View tabButtonContainer;
    private View addButton;
    private View followingButton;
    private View followersButton;
    private EditText searchEditText;
    private View clearSearch;
    private View noResults;
    private View failedToLoad;
    private View tryAgain;
    private ImageView avatar;
    private TextView linkView;
    private View inviteFooter;
    private TextView newFollowerCount;

    private boolean syncInFlight;
    private boolean onboardingMode;
    private int numFollowedDuringOnboarding = 0;
    private String profileLink;

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

        kDeviceAvatarLoader = new KDeviceAvatarLoader(requireContext());

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
        avatar = root.findViewById(R.id.avatar);
        linkView = root.findViewById(R.id.bottom_text);
        inviteFooter = root.findViewById(R.id.invite_footer);
        newFollowerCount = root.findViewById(R.id.new_follower_count);

        viewModel = new ViewModelProvider(requireActivity(), new InviteViewModel.Factory(requireActivity().getApplication(), onboardingMode)).get(InviteViewModel.class);

        viewModel.items.getLiveData().observe(getViewLifecycleOwner(), items -> adapter.setItems(items));

        viewModel.selectedTab.observe(getViewLifecycleOwner(), this::setSelectedTab);

        viewModel.unseenFollowerCount.getLiveData().observe(getViewLifecycleOwner(), count -> {
            if (count > 0 && !onboardingMode && InviteViewModel.SearchState.Closed.equals(viewModel.searchState.getValue())) {
                newFollowerCount.setVisibility(View.VISIBLE);
                newFollowerCount.setText("+" + count);
            } else {
                newFollowerCount.setVisibility(View.GONE);
            }
        });

        viewModel.searchState.observe(getViewLifecycleOwner(), state -> {
            clearSearch.setVisibility(InviteViewModel.SearchState.Closed.equals(state) ? View.GONE : View.VISIBLE);
            tabButtonContainer.setVisibility(!onboardingMode && InviteViewModel.SearchState.Closed.equals(state) ? View.VISIBLE : View.GONE);
            noResults.setVisibility(InviteViewModel.SearchState.Empty.equals(state) ? View.VISIBLE : View.GONE);
            failedToLoad.setVisibility(InviteViewModel.SearchState.Failed.equals(state) ? View.VISIBLE : View.GONE);
            viewModel.unseenFollowerCount.invalidate();
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

        viewModel.checkTimestamp();
        viewModel.unseenFollowerCount.invalidate();

        kAvatarLoader.load(avatar, UserId.ME);
        BgWorkers.getInstance().execute(() -> {
            profileLink = "katchup.com/" + Me.getInstance().getUsername();
            linkView.post(() -> {
                linkView.setText(profileLink);
                inviteFooter.setOnClickListener(v -> {
                    startActivity(IntentUtils.createShareTextIntent(profileLink));
                });
            });
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.updateTimestamp();
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

    public int getNumFollowedDuringOnboarding() {
        return numFollowedDuringOnboarding;
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
                case TYPE_SEE_MORE:
                    return new SeeMoreViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_item_see_more, parent, false));
                case TYPE_SECTION_HEADER:
                    return new SectionHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_item_section_header, parent, false));
                case TYPE_PERSON:
                    return new PersonViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_item_person, parent, false), () -> viewModel.fetchSuggestions());
                case TYPE_MISSING_CONTACT_PERMISSIONS:
                    return new MissingContactPermissionsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_item_contact_permissions, parent, false), () -> requestContacts());
                case TYPE_DEVICE_CONTACT:
                    return new DeviceContactViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.follow_item_device_contact, parent, false));

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
            } else if (holder instanceof SeeMoreViewHolder) {
                SeeMoreViewHolder seeMoreViewHolder = (SeeMoreViewHolder) holder;
                SeeMoreItem item = (SeeMoreItem) items.get(position);
                seeMoreViewHolder.bindTo(item);
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

        public void bindTo(T item) {};
    }

    public static class SeeMoreViewHolder extends ViewHolder<SeeMoreItem> {
        private final TextView textView;
        private final ImageView imageView;

        public SeeMoreViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            imageView = itemView.findViewById(R.id.icon);
        }

        @Override
        public void bindTo(SeeMoreItem item) {
            textView.setText(item.more ? R.string.see_more : R.string.see_less);
            imageView.setRotation(item.more ? 0 : 180);
            itemView.setOnClickListener(v -> item.toggle.run());
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
        private final View newFollowerView;
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
            newFollowerView = itemView.findViewById(R.id.new_follower);
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
                newFollowerView.setVisibility(View.GONE);
            } else if (item.tab == TAB_FOLLOWING) {
                addView.setVisibility(View.GONE);
                closeView.setVisibility(View.GONE);
                followsYouView.setVisibility(item.follower ? View.VISIBLE : View.GONE);
                mutuals.setVisibility(View.GONE);
                newFollowerView.setVisibility(View.GONE);
            } else if (item.tab == TAB_FOLLOWERS) {
                addView.setVisibility(item.following ? View.GONE : View.VISIBLE);
                closeView.setVisibility(View.GONE);
                followsYouView.setVisibility(View.GONE);
                mutuals.setVisibility(View.GONE);
                newFollowerView.setVisibility(item.newFollower ? View.VISIBLE : View.GONE);
            } else if (item.tab == TAB_SEARCH) {
                addView.setVisibility(item.following ? View.GONE : View.VISIBLE);
                closeView.setVisibility(View.GONE);
                followsYouView.setVisibility(item.follower ? View.VISIBLE : View.GONE);
                mutuals.setVisibility(item.mutuals > 0 ? View.VISIBLE : View.GONE);
                mutuals.setText(getResources().getQuantityString(R.plurals.mutual_followers_count, item.mutuals, item.mutuals));
                newFollowerView.setVisibility(View.GONE);
            }
        }
    }

    public class DeviceContactViewHolder extends ViewHolder<DeviceContactItem> {
        private final ImageView avatarView;
        private final TextView nameView;
        private final TextView contactsView;
        private final TextView inviteView;
        private final View closeView;

        private Contact contact;

        public DeviceContactViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            contactsView = itemView.findViewById(R.id.contacts);
            inviteView = itemView.findViewById(R.id.invite);
            closeView = itemView.findViewById(R.id.close);

            inviteView.setOnClickListener(v -> {
                if (contact != null && !TextUtils.isEmpty(profileLink)) {
                    Log.d("InviteContactViewHolder.invite contact " + contact.getDisplayName() + " with number " + contact.normalizedPhone);
                    viewModel.markDeviceContactInvited(contact);
                    final Context context = requireContext();
                    final Intent chooser = IntentUtils.createSmsChooserIntent(context, context.getString(R.string.invite_friend_chooser_title, contact.getShortName()), Preconditions.checkNotNull(contact.normalizedPhone), profileLink);
                    startActivity(chooser);
                }
            });

            closeView.setOnClickListener(v -> {
                if (contact != null) {
                    Log.d("InviteContactViewHolder.dismiss contact " + contact.getDisplayName() + " with number " + contact.normalizedPhone);
                    viewModel.dismissSuggestedDeviceContact(contact);
                }
            });
        }

        @Override
        public void bindTo(DeviceContactItem item) {
            contact = item.contact;
            final Context context = requireContext();
            // Since we can't know whether the invite is actually sent, do not disable the button so that the user can resend the invite.
            inviteView.setText(context.getString(contact.invited ? R.string.invite_add_invite_sent : R.string.invite_add_invite));
            inviteView.setTextColor(ContextCompat.getColor(context, contact.invited ? R.color.white : R.color.black));
            inviteView.setBackground(AppCompatResources.getDrawable(context, contact.invited ? R.drawable.invite_add_button_background_disabled : R.drawable.invite_add_button_background));
            nameView.setText(contact.getDisplayName());
            final int numberOfContacts = (int) contact.numPotentialFriends;
            contactsView.setVisibility(numberOfContacts > 0 ? View.VISIBLE : View.GONE);
            if (numberOfContacts > 0) {
                contactsView.setText(getResources().getQuantityString(R.plurals.contacts_on_katchup, numberOfContacts, numberOfContacts));
            }
            closeView.setVisibility(onboardingMode || item.tab == TAB_SEARCH ? View.GONE : View.VISIBLE);
            kDeviceAvatarLoader.load(avatarView, contact);
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
        private final boolean newFollower;
        private final int tab;

        public PersonItem(@NonNull UserId userId, String name, String username, String avatarId, boolean follower, boolean following, int mutuals, boolean newFollower, int tab) {
            super(TYPE_PERSON);
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.avatarId = avatarId;
            this.follower = follower;
            this.following = following;
            this.mutuals = mutuals;
            this.newFollower = newFollower;
            this.tab = tab;
        }
    }

    public static class SeeMoreItem extends Item {
        public static final int SEE_MORE_CONTACTS = 1;
        public static final int SEE_MORE_FOF = 2;
        public static final int SEE_MORE_INVITES = 3;

        private final int type;
        private final boolean more;
        private final Runnable toggle;

        public SeeMoreItem(int type, boolean more, @NonNull Runnable toggle) {
            super(TYPE_SEE_MORE);
            this.type = type;
            this.more = more;
            this.toggle = toggle;
        }
    }

    public static class DeviceContactItem extends Item {
        final Contact contact;
        private final int tab;

        public DeviceContactItem(@NonNull Contact contact, int tab) {
            super(TYPE_DEVICE_CONTACT);
            this.contact = contact;
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

        private static final int SEE_MORE_LIMIT = 3;

        private static final long RESET_THRESHOLD_MS = 30 * DateUtils.MINUTE_IN_MILLIS;

        public final List<FollowSuggestionsResponseIq.Suggestion> contactSuggestions = new ArrayList<>();
        public final List<FollowSuggestionsResponseIq.Suggestion> fofSuggestions = new ArrayList<>();
        public final List<BasicUserProfile> searchUserResults = new ArrayList<>();
        public final List<Contact> searchContactsResults = new ArrayList<>();

        public final ComputableLiveData<List<Item>> items;
        public final MutableLiveData<Integer> selectedTab = new MutableLiveData<>(TAB_ADD);
        public final MutableLiveData<SearchState> searchState = new MutableLiveData<>(SearchState.Closed);
        public final ComputableLiveData<Integer> unseenFollowerCount;

        private final Handler mainHandler = new Handler(Looper.getMainLooper());
        private Runnable searchUsersRunnable;
        private Runnable searchContactsRunnable;
        private String searchText;

        private final boolean onboardingMode;

        private boolean contactsExpanded = false;
        private boolean fofExpanded = false;
        private boolean invitesExpanded = false;
        private long timestamp;

        private final Connection.Observer connectionObserver = new Connection.Observer() {
            @Override
            public void onConnected() {
                fetchSuggestions();
            }
        };

        private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
            @Override
            public void onNewContacts(@NonNull Collection<UserId> newContacts) {
                items.invalidate();
            }

            @Override
            public void onSuggestedContactDismissed(long addressBookId) {
                items.invalidate();
            }

            @Override
            public void onRelationshipsChanged() {
                items.invalidate();
            }
        };

        public InviteViewModel(@NonNull Application application, boolean onboardingMode) {
            super(application);
            this.onboardingMode = onboardingMode;

            fetchSuggestions();
            ConnectionObservers.getInstance().addObserver(connectionObserver);
            ContactsDb.getInstance().addObserver(contactsObserver);

            items = new ComputableLiveData<List<Item>>() {
                @Override
                protected List<Item> compute() {
                    return computeInviteItems();
                }
            };

            unseenFollowerCount = new ComputableLiveData<Integer>() {
                @Override
                protected Integer compute() {
                    return ContactsDb.getInstance().getUnseenFollowerCount();
                }
            };
        }

        private void updateSearchText(@NonNull String s) {
            searchText = s;
            searchUsers(s);
            searchContacts(s);
        }

        private void searchUsers(@NonNull String s) {
            if (searchUsersRunnable != null) {
                mainHandler.removeCallbacks(searchUsersRunnable);
            }
            if (TextUtils.isEmpty(s)) {
                searchUserResults.clear();
                searchState.setValue(SearchState.Closed);
            } else {
                Analytics.getInstance().openScreen("search");
                searchState.postValue(SearchState.InProgress);
                searchUsersRunnable = () -> {
                    Connection.getInstance().searchForUser(s).onResponse(response -> {
                        if (!response.success) {
                            searchState.postValue(SearchState.Failed);
                            Log.e("Failed to get search results");
                        } else {
                            searchUserResults.clear();
                            searchUserResults.addAll(response.profiles);
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

        private void searchContacts(@NonNull String s) {
            if (searchContactsRunnable != null) {
                mainHandler.removeCallbacks(searchContactsRunnable);
            }
            // Connection.searchForUser returns no results for short strings. Match the behaviour for contact search, so that we don't confuse the user.
            if (s.length() < Constants.MIN_NAME_SEARCH_LENGTH) {
                searchContactsResults.clear();
            } else {
                searchContactsRunnable = () -> {
                    BgWorkers.getInstance().execute(() -> {
                        final List<Contact> contacts = ContactsDb.getInstance().getSuggestedContactsForInvite(false);
                        final Locale locale = Locale.getDefault();
                        final String lowerSearchText = s.toLowerCase(locale);
                        final ListIterator<Contact> iterator = contacts.listIterator();
                        while (iterator.hasNext()) {
                            final Contact contact = iterator.next();
                            final String name = contact.getDisplayName().toLowerCase(locale);
                            if (!name.startsWith(lowerSearchText)) {
                                iterator.remove();
                            }
                        }
                        searchContactsResults.clear();
                        searchContactsResults.addAll(contacts);
                        items.invalidate();
                    });
                };
                mainHandler.postDelayed(searchContactsRunnable, SEARCH_DELAY_MS);
            }
            items.invalidate();
        }

        private void fetchSuggestions() {
            Connection.getInstance().requestFollowSuggestions().onResponse(response -> {
                if (!response.success) {
                    Log.e("Suggestion fetch was not successful");
                } else {
                    Map<UserId, String> names = new HashMap<>();
                    Map<UserId, String> usernames = new HashMap<>();
                    Map<UserId, String> avatars = new HashMap<>();
                    List<FollowSuggestionsResponseIq.Suggestion> contacts = new ArrayList<>();
                    List<FollowSuggestionsResponseIq.Suggestion> fof = new ArrayList<>();

                    for (FollowSuggestionsResponseIq.Suggestion suggestion : response.suggestions) {
                        names.put(suggestion.info.userId, suggestion.info.name);
                        usernames.put(suggestion.info.userId, suggestion.info.username);
                        avatars.put(suggestion.info.userId, suggestion.info.avatarId);
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

                    ContactsDb contactsDb = ContactsDb.getInstance();
                    contactsDb.updateUserNames(names);
                    contactsDb.updateUserUsernames(usernames);
                    contactsDb.updateUserAvatars(avatars);
                }
            }).onError(error -> {
                Log.e("Suggestion fetch got error", error);
            });
        }

        private List<Item> generateInviteItemList() {
            final List<Contact> contacts = ContactsDb.getInstance().getSuggestedContactsForInvite(false);
            final boolean canExpand = contacts.size() > SEE_MORE_LIMIT;
            if (canExpand && !invitesExpanded) {
                contacts.subList(SEE_MORE_LIMIT, contacts.size()).clear();
            }

            final List<Item> list = new ArrayList<>();
            if (!contacts.isEmpty()) {
                list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_device_contacts)));
            }
            for (Contact contact : contacts) {
                list.add(new DeviceContactItem(contact, TAB_ADD));
            }
            if (canExpand) {
                list.add(new SeeMoreItem(SeeMoreItem.SEE_MORE_INVITES, !invitesExpanded, this::toggleInvitesExpanded));
            }
            return list;
        }

        private List<Item> computeInviteItems() {
            final ContactsDb contactsDb = ContactsDb.getInstance();
            List<Item> list = new ArrayList<>();

            List<RelationshipInfo> followers = contactsDb.getRelationships(RelationshipInfo.Type.FOLLOWER);
            List<RelationshipInfo> following = contactsDb.getRelationships(RelationshipInfo.Type.FOLLOWING);
            Set<UserId> followerUserIds = new HashSet<>();
            Set<UserId> followingUserIds = new HashSet<>();
            for (RelationshipInfo follower : followers) {
                followerUserIds.add(follower.userId);
            }
            for (RelationshipInfo followed : following) {
                followingUserIds.add(followed.userId);
            }

            if (searchState.getValue() != SearchState.Closed) {
                for (BasicUserProfile userProfile : searchUserResults) {
                    UserId userId = new UserId(Long.toString(userProfile.getUid()));
                    list.add(new PersonItem(
                            userId,
                            userProfile.getName(),
                            userProfile.getUsername(),
                            userProfile.getAvatarId(),
                            followerUserIds.contains(userId),
                            followingUserIds.contains(userId),
                            userProfile.getNumMutualFollowing(),
                            false,
                            TAB_SEARCH));
                }
                for (Contact contact : searchContactsResults) {
                    list.add(new DeviceContactItem(contact, TAB_SEARCH));
                }
                return list;
            }

            int tab = Preconditions.checkNotNull(selectedTab.getValue());
            if (tab == TAB_ADD) {
                final boolean hasContactsPermission = EasyPermissions.hasPermissions(getApplication(), android.Manifest.permission.READ_CONTACTS);
                if (!hasContactsPermission) {
                    list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_katchup_contacts)));
                    list.add(new MissingContactPermissionsItem());
                } else {
                    if (!contactSuggestions.isEmpty()) {
                        list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_katchup_contacts)));
                    }
                    int suggestions = 0;
                    for (FollowSuggestionsResponseIq.Suggestion suggestion : contactSuggestions) {
                        if (!contactsExpanded && suggestions >= SEE_MORE_LIMIT) {
                            break;
                        }
                        list.add(new PersonItem(
                                suggestion.info.userId,
                                suggestion.info.name,
                                suggestion.info.username,
                                suggestion.info.avatarId,
                                followerUserIds.contains(suggestion.info.userId),
                                followingUserIds.contains(suggestion.info.userId),
                                suggestion.mutuals,
                                false,
                                TAB_ADD));
                        suggestions++;
                    }
                    if (contactSuggestions.size() > SEE_MORE_LIMIT) {
                        list.add(new SeeMoreItem(SeeMoreItem.SEE_MORE_CONTACTS, !contactsExpanded, this::toggleContactsExpanded));
                    }
                    if (onboardingMode && contactSuggestions.isEmpty()) {
                        list.addAll(generateInviteItemList());
                    }
                }
                list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_friends_of_friends)));
                int suggestions = 0;
                for (FollowSuggestionsResponseIq.Suggestion suggestion : fofSuggestions) {
                    if (!fofExpanded && suggestions >= SEE_MORE_LIMIT) {
                        break;
                    }
                    list.add(new PersonItem(
                            suggestion.info.userId,
                            suggestion.info.name,
                            suggestion.info.username,
                            suggestion.info.avatarId,
                            followerUserIds.contains(suggestion.info.userId),
                            followingUserIds.contains(suggestion.info.userId),
                            suggestion.mutuals,
                            false,
                            TAB_ADD));
                    suggestions++;
                }
                if (fofSuggestions.size() > SEE_MORE_LIMIT) {
                    list.add(new SeeMoreItem(SeeMoreItem.SEE_MORE_FOF, !fofExpanded, this::toggleFofExpanded));
                }

                if (hasContactsPermission && !onboardingMode) {
                    list.addAll(generateInviteItemList());
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
                            false,
                            TAB_FOLLOWING));
                }
            } else if (tab == TAB_FOLLOWERS) {
                BgWorkers.getInstance().execute(() -> {
                    ContactsDb.getInstance().markFollowersSeen();
                });
                Comparator<RelationshipInfo> comparator = (o1, o2) -> {
                    if (o1.seen && !o2.seen) {
                        return 1;
                    } else if (!o1.seen && o2.seen) {
                        return -1;
                    } else {
                        return 0;
                    }
                };
                Collections.sort(followers, comparator);
                for (RelationshipInfo info : followers) {
                    list.add(new PersonItem(
                            info.userId,
                            info.name,
                            info.username,
                            info.avatarId,
                            followerUserIds.contains(info.userId),
                            followingUserIds.contains(info.userId),
                            0,
                            !info.seen,
                            TAB_FOLLOWERS));
                }
            }

            return list;
        }

        @MainThread
        public void setSelectedTab(int selectedTab) {
            if (selectedTab != TAB_FOLLOWERS) {
                unseenFollowerCount.invalidate();
            }
            this.selectedTab.setValue(selectedTab);
            items.invalidate();
        }

        public void toggleContactsExpanded() {
            contactsExpanded = !contactsExpanded;
            items.invalidate();
        }

        public void toggleFofExpanded() {
            fofExpanded = !fofExpanded;
            items.invalidate();
        }

        public void toggleInvitesExpanded() {
            invitesExpanded = !invitesExpanded;
            items.invalidate();
        }

        public void updateTimestamp() {
            timestamp = System.currentTimeMillis();
        }

        public void checkTimestamp() {
            if (System.currentTimeMillis() - timestamp > RESET_THRESHOLD_MS) {
                reset();
            }
        }

        private void reset() {
            timestamp = System.currentTimeMillis();
            contactsExpanded = false;
            fofExpanded = false;
            invitesExpanded = false;
            updateSearchText("");
            selectedTab.setValue(TAB_ADD);
            items.invalidate();
        }

        @Override
        protected void onCleared() {
            ConnectionObservers.getInstance().removeObserver(connectionObserver);
            ContactsDb.getInstance().removeObserver(contactsObserver);
        }

        public void dismissSuggestedDeviceContact(@NonNull Contact contact) {
            BgWorkers.getInstance().execute(() -> {
                ContactsDb.getInstance().dismissSuggestedContact(contact);
            });
        }

        public void markDeviceContactInvited(@NonNull Contact contact) {
            final boolean inSearchMode = searchState.getValue() != SearchState.Closed;
            BgWorkers.getInstance().execute(() -> {
                ContactsDb.getInstance().markInvited(contact);
                if (inSearchMode) {
                    mainHandler.post(() -> {
                        searchContacts(searchText);
                    });
                } else {
                    items.invalidate();
                }
            });
        }

        public static class Factory implements ViewModelProvider.Factory {
            private final Application application;
            private final boolean onboardingMode;

            public Factory(Application application, boolean onboardingMode) {
                this.application = application;
                this.onboardingMode = onboardingMode;
            }

            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(InviteViewModel.class)) {
                    //noinspection unchecked
                    return (T) new InviteViewModel(application, onboardingMode);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }
}
