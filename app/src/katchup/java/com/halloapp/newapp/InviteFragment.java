package com.halloapp.newapp;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ComputableLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.MainActivity;
import com.halloapp.R;
import com.halloapp.contacts.InviteContactsAdapter;
import com.halloapp.ui.HalloFragment;

import java.util.ArrayList;
import java.util.List;

public class InviteFragment extends HalloFragment {
    private static final int TYPE_INVITE_LINK_HEADER = 1;
    private static final int TYPE_SECTION_HEADER = 2;
    private static final int TYPE_PERSON = 3;

    private InviteViewModel viewModel;

    private InviteAdapter adapter = new InviteAdapter();

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

        return root;
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
        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            usernameView = itemView.findViewById(R.id.username);
            addView = itemView.findViewById(R.id.add);
            closeView = itemView.findViewById(R.id.close);
        }
    }

    public static class InviteItem {

        private final int type;
        private final String sectionTitle;
        private final String name;
        private final String username;

        public static InviteItem linkHeader() {
            return new InviteItem(TYPE_INVITE_LINK_HEADER, null, null, null);
        }

        public static InviteItem sectionHeader(String sectionTitle) {
            return new InviteItem(TYPE_SECTION_HEADER, sectionTitle, null, null);
        }

        public static InviteItem person(String name, String username) {
            return new InviteItem(TYPE_PERSON, null, name, username);
        }

        private InviteItem(int type, @Nullable String sectionTitle, @Nullable String name, @Nullable String username) {
            this.type = type;
            this.sectionTitle = sectionTitle;
            this.name = name;
            this.username = username;
        }
    }

    public static class InviteViewModel extends AndroidViewModel {

        public final ComputableLiveData<List<InviteItem>> items;

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
            list.add(InviteItem.sectionHeader("Requests")); // TODO: Make a string for section titles
            list.add(InviteItem.person("Duygu Daniels", "du77u"));
            list.add(InviteItem.person("Test User", "testing"));
            list.add(InviteItem.sectionHeader("Phone contacts"));
            list.add(InviteItem.sectionHeader("Friends of friends"));
            list.add(InviteItem.sectionHeader("Friends (0)")); // TODO: The number
            return list;
        }
    }
}
