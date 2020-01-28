package com.halloapp.ui.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;

import java.util.List;

public class MessagesFragment extends Fragment {

    private final ContactsAdapter adapter = new ContactsAdapter();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        final View root = inflater.inflate(R.layout.fragment_messages, container, false);
        final RecyclerView chatsView = root.findViewById(R.id.chats);
        final View emptyView = root.findViewById(android.R.id.empty);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatsView.setLayoutManager(layoutManager);
        chatsView.setAdapter(adapter);

        final MessagesViewModel viewModel = new ViewModelProvider(this).get(MessagesViewModel.class);
        viewModel.contactsList.observe(this, contacts -> {
            adapter.setContacts(contacts);
            emptyView.setVisibility(contacts.size() == 0 ? View.VISIBLE : View.GONE);
        });

        final float scrolledElevation = getResources().getDimension(R.dimen.scrolled_elevation);
        chatsView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                final View childView = layoutManager.getChildAt(0);
                final boolean scrolled = childView == null || !(childView.getTop() == 0 && layoutManager.getPosition(childView) == 0);
                final AppCompatActivity activity = Preconditions.checkNotNull((AppCompatActivity)getActivity());
                final ActionBar actionBar = Preconditions.checkNotNull(activity.getSupportActionBar());
                final float elevation = scrolled ? scrolledElevation : 0;
                if (actionBar.getElevation() != elevation) {
                    actionBar.setElevation(elevation);
                }
            }
        });

        return root;
    }

    class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

        private List<Contact> contacts;

        void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
            notifyDataSetChanged();
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bindTo(contacts.get(position));
        }

        @Override
        public int getItemCount() {
            return contacts == null ? 0 : contacts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final ImageView avatarView;
            final TextView nameView;
            final TextView infoView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                infoView = itemView.findViewById(R.id.info);
            }

            void bindTo(Contact contact) {
                avatarView.setImageResource(R.drawable.avatar_person); // TODO (ds): load contact image
                nameView.setText(contact.getDisplayName());
                infoView.setText(contact.getInternationalPhone());
            }
        }
    }
}