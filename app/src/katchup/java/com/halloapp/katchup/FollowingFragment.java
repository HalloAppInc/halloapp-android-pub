package com.halloapp.katchup;

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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ComputableLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.ConnectionObservers;
import com.halloapp.MainActivity;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.proto.server.UserProfile;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.FollowSuggestionsResponseIq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FollowingFragment extends HalloFragment {
    private static final int TYPE_INVITE_LINK_HEADER = 1;
    private static final int TYPE_SECTION_HEADER = 2;
    private static final int TYPE_PERSON = 3;

    private static final int TAB_ADD = 1;
    private static final int TAB_FOLLOWING = 2;
    private static final int TAB_FOLLOWERS = 3;

    private static final int SEARCH_DELAY_MS = 300;

    private InviteViewModel viewModel;

    private InviteAdapter adapter = new InviteAdapter();
    private KAvatarLoader kAvatarLoader = KAvatarLoader.getInstance();

    private View tabButtonContainer;
    private View addButton;
    private View followingButton;
    private View followersButton;
    private EditText searchEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_following, container, false);

        View next = root.findViewById(R.id.next);
        next.setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
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

        viewModel = new ViewModelProvider(requireActivity()).get(InviteViewModel.class);

        viewModel.items.getLiveData().observe(getViewLifecycleOwner(), items -> adapter.setItems(items));

        viewModel.selectedTab.observe(getViewLifecycleOwner(), this::setSelectedTab);

        viewModel.searchInProgress.observe(getViewLifecycleOwner(), inProgress -> {
            tabButtonContainer.setVisibility(Boolean.TRUE.equals(inProgress) ? View.GONE : View.VISIBLE);
        });

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

        return root;
    }

    private void setSelectedTab(int selectedTab) {
        Drawable selectedDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.selected_feed_type_background);
        Drawable unselectedDrawable = null;
        addButton.setBackground(selectedTab == TAB_ADD ? selectedDrawable : unselectedDrawable);
        followingButton.setBackground(selectedTab == TAB_FOLLOWING ? selectedDrawable : unselectedDrawable);
        followersButton.setBackground(selectedTab == TAB_FOLLOWERS ? selectedDrawable : unselectedDrawable);
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
                    return new LinkHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_item_link_header, parent, false));
                case TYPE_SECTION_HEADER:
                    return new SectionHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_item_section_header, parent, false));
                case TYPE_PERSON:
                    return new PersonViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_item_person, parent, false), () -> viewModel.fetchSuggestions());
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

    public class LinkHeaderViewHolder extends ViewHolder<Void> {
        public LinkHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            kAvatarLoader.load(itemView.findViewById(R.id.avatar), UserId.ME);
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

        private UserId userId;

        public PersonViewHolder(@NonNull View itemView, @NonNull Runnable reloadList) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);
            addView = itemView.findViewById(R.id.add);
            closeView = itemView.findViewById(R.id.close);
            followsYouView = itemView.findViewById(R.id.follows_you);

            addView.setOnClickListener(v -> {
                BgWorkers.getInstance().execute(() -> {
                    Connection.getInstance().requestFollowUser(userId).onResponse(res -> {
                        if (!res.success) {
                            Log.e("Follow user failed");
                        } else {
                            reloadList.run();
                            ContactsDb.getInstance().addRelationship(new RelationshipInfo(
                                    res.userId,
                                    res.username,
                                    res.name,
                                    res.avatarId,
                                    RelationshipInfo.Type.FOLLOWING
                            ));
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
        }

        @Override
        public void bindTo(PersonItem item) {
            this.userId = item.userId;
            this.nameView.setText(item.name);
            this.usernameView.setText("@" + item.username);
            this.followsYouView.setVisibility(View.GONE);
            kAvatarLoader.load(avatarView, item.userId);
            if (item.tab == TAB_ADD) {
                addView.setVisibility(View.VISIBLE);
                closeView.setVisibility(View.VISIBLE);
                itemView.setOnClickListener(null);
            } else if (item.tab == TAB_FOLLOWING) {
                addView.setVisibility(View.GONE);
                closeView.setVisibility(View.GONE);
                // TODO(jack): once user profiles are implemented this should go to profile where user can unfollow
                itemView.setOnClickListener(v -> {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setMessage(itemView.getContext().getString(R.string.confirm_unfollow_user, item.name))
                            .setPositiveButton(R.string.yes, (dialog1, which) -> {
                                Connection.getInstance().requestUnfollowUser(userId).onResponse(res -> {
                                    ContactsDb.getInstance().removeRelationship(new RelationshipInfo(
                                            res.userId,
                                            res.username,
                                            res.name,
                                            res.avatarId,
                                            RelationshipInfo.Type.FOLLOWING
                                    ));
                                }).onError(error -> {
                                    Log.e("Failed to unfollow user", error);
                                });
                            })
                            .setNegativeButton(R.string.no, null)
                            .create();
                    dialog.show();
                });
            } else if (item.tab == TAB_FOLLOWERS) {
                addView.setVisibility(View.GONE);
                closeView.setVisibility(View.GONE);
            }
        }
    }

    public static abstract class Item {
        private final int type;
        public Item(int type) {
            this.type = type;
        }
    }

    public static class LinkHeaderItem extends Item {
        public LinkHeaderItem() {
            super(TYPE_INVITE_LINK_HEADER);
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
        private final int tab;

        public PersonItem(@NonNull UserId userId, String name, String username, int tab) {
            super(TYPE_PERSON);
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.tab = tab;
        }
    }

    public static class InviteViewModel extends AndroidViewModel {

        public final List<FollowSuggestionsResponseIq.Suggestion> contactSuggestions = new ArrayList<>();
        public final List<FollowSuggestionsResponseIq.Suggestion> fofSuggestions = new ArrayList<>();
        public final List<FollowSuggestionsResponseIq.Suggestion> campusSuggestions = new ArrayList<>();
        public final List<UserProfile> searchResults = new ArrayList<>();

        public final ComputableLiveData<List<Item>> items;
        public final MutableLiveData<Integer> selectedTab = new MutableLiveData<>(TAB_ADD);
        public final MutableLiveData<Boolean> searchInProgress = new MutableLiveData<>(false);

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
                searchInProgress.setValue(false);
            } else {
                searchInProgress.postValue(true);
                searchRunnable = () -> {
                    Connection.getInstance().searchForUser(s).onResponse(response -> {
                        if (!response.success) {
                            Log.e("Failed to get search results");
                        } else {
                            searchResults.clear();
                            searchResults.addAll(response.profiles);
                            items.invalidate();
                        }
                    }).onError(err -> {
                        // TODO(jack): Show network failure UI
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
                    List<FollowSuggestionsResponseIq.Suggestion> contacts = new ArrayList<>();
                    List<FollowSuggestionsResponseIq.Suggestion> fof = new ArrayList<>();
                    List<FollowSuggestionsResponseIq.Suggestion> campus = new ArrayList<>();

                    for (FollowSuggestionsResponseIq.Suggestion suggestion : response.suggestions) {
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
                                campus.add(suggestion);
                                break;
                            }
                        }
                    }

                    Comparator<FollowSuggestionsResponseIq.Suggestion> comparator = (o1, o2) -> o2.rank - o1.rank;
                    Collections.sort(contacts, comparator);
                    Collections.sort(fof, comparator);
                    Collections.sort(campus, comparator);

                    contactSuggestions.clear();
                    contactSuggestions.addAll(contacts);
                    fofSuggestions.clear();
                    fofSuggestions.addAll(fof);
                    campusSuggestions.clear();
                    campusSuggestions.addAll(campus);

                    items.invalidate();
                }
            }).onError(error -> {
                Log.e("Suggestion fetch got error", error);
            });
        }

        private List<Item> computeInviteItems() {
            List<Item> list = new ArrayList<>();

            if (Boolean.TRUE.equals(searchInProgress.getValue())) {
                for (UserProfile userProfile : searchResults) {
                    list.add(new PersonItem(new UserId(Long.toString(userProfile.getUid())), userProfile.getName(), userProfile.getUsername(), -1));
                }
                return list;
            }

            list.add(new LinkHeaderItem());

            int tab = Preconditions.checkNotNull(selectedTab.getValue());
            if (tab == TAB_ADD) {
                list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_phone_contacts)));
                for (FollowSuggestionsResponseIq.Suggestion suggestion : contactSuggestions) {
                    list.add(new PersonItem(suggestion.info.userId, suggestion.info.name, suggestion.info.username, TAB_ADD));
                }
                list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_friends_of_friends)));
                for (FollowSuggestionsResponseIq.Suggestion suggestion : fofSuggestions) {
                    list.add(new PersonItem(suggestion.info.userId, suggestion.info.name, suggestion.info.username, TAB_ADD));
                }
                list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_campus)));
                for (FollowSuggestionsResponseIq.Suggestion suggestion : campusSuggestions) {
                    list.add(new PersonItem(suggestion.info.userId, suggestion.info.name, suggestion.info.username, TAB_ADD));
                }
            } else if (tab == TAB_FOLLOWING) {
                List<RelationshipInfo> following = ContactsDb.getInstance().getRelationships(RelationshipInfo.Type.FOLLOWING);
                for (RelationshipInfo info : following) {
                    list.add(new PersonItem(info.userId, info.name, info.username, TAB_FOLLOWING));
                }
            } else if (tab == TAB_FOLLOWERS) {
                List<RelationshipInfo> followers = ContactsDb.getInstance().getRelationships(RelationshipInfo.Type.FOLLOWER);
                for (RelationshipInfo info : followers) {
                    list.add(new PersonItem(info.userId, info.name, info.username, TAB_FOLLOWERS));
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
}
