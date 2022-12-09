package com.halloapp.katchup;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.halloapp.MainActivity;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.xmpp.Connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FollowingFragment extends HalloFragment {
    private static final int TYPE_INVITE_LINK_HEADER = 1;
    private static final int TYPE_SECTION_HEADER = 2;
    private static final int TYPE_PERSON = 3;

    private static final int TAB_ADD = 1;
    private static final int TAB_FOLLOWING = 2;
    private static final int TAB_FOLLOWERS = 3;

    private InviteViewModel viewModel;

    private InviteAdapter adapter = new InviteAdapter();

    private View addButton;
    private View followingButton;
    private View followersButton;

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

        viewModel = new ViewModelProvider(requireActivity()).get(InviteViewModel.class);

        viewModel.items.getLiveData().observe(getViewLifecycleOwner(), items -> adapter.setItems(items));

        viewModel.selectedTab.observe(getViewLifecycleOwner(), this::setSelectedTab);

        addButton = root.findViewById(R.id.user_list_type_add);
        addButton.setOnClickListener(v -> viewModel.setSelectedTab(TAB_ADD));
        followingButton = root.findViewById(R.id.user_list_type_following);
        followingButton.setOnClickListener(v -> viewModel.setSelectedTab(TAB_FOLLOWING));
        followersButton = root.findViewById(R.id.user_list_type_followers);
        followersButton.setOnClickListener(v -> viewModel.setSelectedTab(TAB_FOLLOWERS));

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
                    return new PersonViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_item_person, parent, false));
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

    public static class LinkHeaderViewHolder extends ViewHolder<Void> {
        public LinkHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
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

    public static class PersonViewHolder extends ViewHolder<PersonItem> {
        private final ImageView avatarView;
        private final TextView nameView;
        private final TextView usernameView;
        private final View addView;
        private final View closeView;
        private final View followsYouView;

        private UserId userId;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);
            addView = itemView.findViewById(R.id.add);
            closeView = itemView.findViewById(R.id.close);
            followsYouView = itemView.findViewById(R.id.follows_you);

            addView.setOnClickListener(v -> {
                BgWorkers.getInstance().execute(() -> {
                    Connection.getInstance().requestFollowUser(userId);
                });
            });
        }

        @Override
        public void bindTo(PersonItem item) {
            this.userId = item.userId;
            this.nameView.setText(item.name);
            this.usernameView.setText("@" + item.username);
            this.followsYouView.setVisibility(item.followsYou ? View.VISIBLE : View.GONE);
            if (!item.following) {
                addView.setVisibility(View.VISIBLE);
                itemView.setOnClickListener(null);
            } else {
                addView.setVisibility(View.GONE);
                itemView.setOnClickListener(v -> {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setMessage(itemView.getContext().getString(R.string.confirm_unfollow_user, item.name))
                            .setPositiveButton(R.string.yes, (dialog1, which) -> Connection.getInstance().requestUnfollowUser(userId))
                            .setNegativeButton(R.string.no, null)
                            .create();
                    dialog.show();
                });
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
        private final boolean following;
        private final boolean followsYou;

        public PersonItem(@NonNull UserId userId, String name, String username, boolean following, boolean followsYou) {
            super(TYPE_PERSON);
            this.userId = userId;
            this.name = name;
            this.username = username;
            this.following = following;
            this.followsYou = followsYou;
        }
    }

    public static class InviteViewModel extends AndroidViewModel {

        public final ComputableLiveData<List<Item>> items;
        public final MutableLiveData<Integer> selectedTab = new MutableLiveData<>(TAB_ADD);

        public InviteViewModel(@NonNull Application application) {
            super(application);

            items = new ComputableLiveData<List<Item>>() {
                @Override
                protected List<Item> compute() {
                    return computeInviteItems();
                }
            };
        }

        private List<Item> computeInviteItems() {
            List<Item> list = new ArrayList<>();
            list.add(new LinkHeaderItem());

            int tab = Preconditions.checkNotNull(selectedTab.getValue());
            if (tab == TAB_ADD) {
                List<Contact> users = ContactsDb.getInstance().getUsers();
                list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_phone_contacts)));
                for (Contact contact : users) {
                    // TODO(jack): Switch to username once server supports it
                    list.add(new PersonItem(contact.userId, contact.getDisplayName().toLowerCase(Locale.getDefault()), contact.halloName, false, false));
                }
                list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_friends_of_friends)));
                list.add(new SectionHeaderItem(getApplication().getString(R.string.invite_section_campus)));
            } else if (tab == TAB_FOLLOWING) {
                List<RelationshipInfo> following = ContactsDb.getInstance().getRelationships(RelationshipInfo.Type.FOLLOWING);
                for (RelationshipInfo info : following) {
                    list.add(new PersonItem(info.userId, info.name, info.username, true, false));
                }
            } else if (tab == TAB_FOLLOWERS) {

            }

            return list;
        }

        @MainThread
        public void setSelectedTab(int selectedTab) {
            this.selectedTab.setValue(selectedTab);
            items.invalidate();
        }
    }
}
