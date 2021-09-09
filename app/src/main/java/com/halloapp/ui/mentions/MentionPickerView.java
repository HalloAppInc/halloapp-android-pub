package com.halloapp.ui.mentions;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.ui.avatar.AvatarLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class MentionPickerView extends FrameLayout {

    protected final AvatarLoader avatarLoader;

    private OnMentionListener listener;

    private RecyclerView recyclerView;

    private boolean showing = false;

    public interface OnMentionListener {
        void onMention(@NonNull Contact contact);
    }

    public MentionPickerView(@NonNull Context context) {
        super(context);
        avatarLoader = AvatarLoader.getInstance();
        init();
    }

    public MentionPickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        avatarLoader = AvatarLoader.getInstance();
        init();
    }

    public MentionPickerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        avatarLoader = AvatarLoader.getInstance();
        init();
    }

    public void hide() {
        if (!showing) {
            return;
        }
        showing = false;
        clearAnimation();
        animate().translationY(getHeight()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!showing) {
                    setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void show() {
        if (showing) {
            return;
        }
        showing = true;
        clearAnimation();
        setTranslationY(getHeight());
        setVisibility(View.VISIBLE);
        animate().translationY(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (showing) {
                    setTranslationY(0);
                }
            }
        });
    }

    private CharSequence filter;

    public void setMentionFilter(CharSequence filter) {
        this.filter = filter;
        filterContacts();
    }

    private void init() {
        inflate(getContext(), R.layout.mention_picker, this);

        recyclerView = findViewById(R.id.recycler_view);
        adapter = new MentionsAdapter(avatarLoader);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        setElevation(getResources().getDimensionPixelSize(R.dimen.mention_picker_elevation));
    }

    public void setOnMentionListener (@Nullable OnMentionListener listener) {
        this.listener = listener;
    }

    private MentionsAdapter adapter;

    private @Nullable List<Contact> mentionableContacts;
    private @Nullable List<Contact> filteredContacts;

    public void setMentionableContacts(@Nullable Collection<Contact> contacts) {
        if (contacts == null) {
            mentionableContacts = null;
        } else {
            mentionableContacts = new ArrayList<>(contacts);
            Contact.sort(mentionableContacts);
        }
        filterContacts();
    }

    private void filterContacts() {
        Transition transition = new AutoTransition();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(this, transition);
        filteredContacts = new ArrayList<>();
        if (mentionableContacts == null) {
            return;
        }
        if (filter != null) {
            String searchString = filter.toString().toLowerCase(Locale.getDefault());
            for (Contact contact : mentionableContacts) {
                String contactDisplayName = contact.getDisplayName().toLowerCase(Locale.getDefault());
                if (TextUtils.isEmpty(contactDisplayName)) {
                    continue;
                }
                int resultIndex = contactDisplayName.indexOf(searchString);
                if (resultIndex == -1) {
                    continue;
                }
                if (resultIndex == 0) {
                    filteredContacts.add(contact);
                } else {
                    char prevChar = contactDisplayName.charAt(resultIndex - 1);
                    if (!Character.isLetterOrDigit(prevChar)) {
                        filteredContacts.add(contact);
                    }
                }
            }
        } else {
            filteredContacts.addAll(mentionableContacts);
        }
        adapter.notifyDataSetChanged();
    }

    private class MentionsAdapter extends RecyclerView.Adapter<MentionsAdapter.MentionItemViewHolder> {

        private final AvatarLoader avatarLoader;

        public MentionsAdapter(@NonNull AvatarLoader avatarLoader) {
            this.avatarLoader = avatarLoader;
        }

        @NonNull
        @Override
        public MentionItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MentionItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.mention_picker_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MentionItemViewHolder holder, int position) {
            holder.bindTo(filteredContacts.get(position));
        }

        @Override
        public int getItemCount() {
            return filteredContacts == null ? 0 : filteredContacts.size();
        }

        class MentionItemViewHolder extends RecyclerView.ViewHolder {

            final ImageView avatar;
            final TextView name;

            public MentionItemViewHolder(@NonNull View itemView) {
                super(itemView);
                avatar = itemView.findViewById(R.id.avatar);
                name = itemView.findViewById(R.id.name);
            }

            public void bindTo(@NonNull Contact contact) {
                if (contact.userId != null) {
                    avatarLoader.load(avatar, contact.userId);
                }
                name.setText(contact.getDisplayName());
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onMention(contact);
                        }
                    }
                });
            }
        }
    }
}
