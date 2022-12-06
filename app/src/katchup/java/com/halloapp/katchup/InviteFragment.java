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
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InviteFragment extends HalloFragment {
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
        View root = inflater.inflate(R.layout.fragment_invite, container, false);

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

        private List<InviteItem> items = new ArrayList<>();

        public void setItems(List<InviteItem> items) {
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
            if (holder instanceof SectionHeaderViewHolder) {
                ((SectionHeaderViewHolder) holder).textView.setText(items.get(position).sectionTitle);
            } else if (holder instanceof PersonViewHolder) {
                PersonViewHolder personViewHolder = (PersonViewHolder) holder;
                InviteItem inviteItem = items.get(position);
                personViewHolder.nameView.setText(inviteItem.name);
                personViewHolder.usernameView.setText("@" + inviteItem.username);
                personViewHolder.followsYouView.setVisibility(inviteItem.followsYou ? View.VISIBLE : View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    public static class LinkHeaderViewHolder extends RecyclerView.ViewHolder {
        public LinkHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class SectionHeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
        }
    }

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatarView;
        private final TextView nameView;
        private final TextView usernameView;
        private final View addView;
        private final View closeView;
        private final View followsYouView;
        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);
            addView = itemView.findViewById(R.id.add);
            closeView = itemView.findViewById(R.id.close);
            followsYouView = itemView.findViewById(R.id.follows_you);
        }
    }

    public static class InviteItem {

        private final int type;
        private final String sectionTitle;
        private final String name;
        private final String username;
        private final boolean followsYou;

        public static InviteItem linkHeader() {
            return new InviteItem(TYPE_INVITE_LINK_HEADER, null, null, null, false);
        }

        public static InviteItem sectionHeader(String sectionTitle) {
            return new InviteItem(TYPE_SECTION_HEADER, sectionTitle, null, null, false);
        }

        public static InviteItem person(String name, String username, boolean followsYou) {
            return new InviteItem(TYPE_PERSON, null, name, username, followsYou);
        }

        private InviteItem(int type, @Nullable String sectionTitle, @Nullable String name, @Nullable String username, boolean followsYou) {
            this.type = type;
            this.sectionTitle = sectionTitle;
            this.name = name;
            this.username = username;
            this.followsYou = followsYou;
        }
    }

    public static class InviteViewModel extends AndroidViewModel {

        public final ComputableLiveData<List<InviteItem>> items;
        public final MutableLiveData<Integer> selectedTab = new MutableLiveData<>(TAB_ADD);

        public InviteViewModel(@NonNull Application application) {
            super(application);

            items = new ComputableLiveData<List<InviteItem>>() {
                @Override
                protected List<InviteItem> compute() {
                    return computeInviteItems();
                }
            };
        }

        private List<InviteItem> computeInviteItems() {
            List<InviteItem> list = new ArrayList<>();
            list.add(InviteItem.linkHeader());

            int tab = Preconditions.checkNotNull(selectedTab.getValue());
            if (tab == TAB_ADD) {
                List<Contact> users = ContactsDb.getInstance().getUsers();
                list.add(InviteItem.sectionHeader(getApplication().getString(R.string.invite_section_phone_contacts)));
                for (Contact contact : users) {
                    // TODO(jack): Switch to username once server supports it
                    list.add(InviteItem.person(contact.getDisplayName().toLowerCase(Locale.getDefault()), contact.halloName, false));
                }
                list.add(InviteItem.sectionHeader(getApplication().getString(R.string.invite_section_friends_of_friends)));
                list.add(InviteItem.sectionHeader(getApplication().getString(R.string.invite_section_campus)));
            } else if (tab == TAB_FOLLOWING) {
                list.add(InviteItem.person("test name", "username", false));
                list.add(InviteItem.person("follows you", "followsyou", true));
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
