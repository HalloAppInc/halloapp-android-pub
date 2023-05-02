package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Group;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.groups.GroupPostLoader;
import com.halloapp.ui.groups.UnseenGroupPostsLoader;
import com.halloapp.ui.groups.ViewGroupFeedActivity;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class GroupsInCommonActivity extends HalloActivity {

    private static final String EXTRA_USER_ID = "user_id";

    public static Intent viewGroupsInCommon(@NonNull Context context, @NonNull UserId userId) {
        Preconditions.checkNotNull(userId);
        Intent intent = new Intent(context, GroupsInCommonActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }

    private ContactLoader contactLoader;
    private TextContentLoader textContentLoader;
    private UnseenGroupPostsLoader unseenGroupPostsLoader;
    private SystemMessageTextResolver systemMessageTextResolver;
    private AvatarLoader avatarLoader;
    private GroupPostLoader groupPostLoader;

    private GroupsAdapter adapter;

    private TextView emptyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_groups_in_common);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        UserId userId = getIntent().getParcelableExtra(EXTRA_USER_ID);

        if (userId == null) {
            Log.e("GroupsInCommonActivity/onCreate missing userId");
            return;
        }

        GroupsInCommonViewModel viewModel = new ViewModelProvider(this, new GroupsInCommonViewModel.Factory(userId)).get(GroupsInCommonViewModel.class);

        adapter = new GroupsAdapter();
        groupPostLoader = new GroupPostLoader();
        contactLoader = new ContactLoader(uid -> {
            startActivity(ViewProfileActivity.viewProfile(this, uid));
            return null;
        });
        textContentLoader = new TextContentLoader();
        unseenGroupPostsLoader = new UnseenGroupPostsLoader();
        systemMessageTextResolver = new SystemMessageTextResolver(contactLoader);
        avatarLoader = AvatarLoader.getInstance();

        EditText searchBox = findViewById(R.id.search_text);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s.toString());
            }
        });

        emptyView = findViewById(android.R.id.empty);

        final RecyclerView listView = findViewById(android.R.id.list);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);

        viewModel.groupsList.getLiveData().observe(this, v -> {
            adapter.setGroups(v);
        });
    }

    private class GroupsFilter extends FilterUtils.ItemFilter<Group> {

        GroupsFilter(@NonNull List<Group> groups) {
            super(groups);
        }

        @Override
        protected String itemToString(Group group) {
            return group.name;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            final List<Group> filteredGroups = (List<Group>) results.values;
            adapter.setFilteredGroups(filteredGroups, constraint);
            if (filteredGroups == null || filteredGroups.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(constraint)) {
                    emptyView.setText(R.string.groups_in_common_empty);
                } else {
                    emptyView.setText(getString(R.string.groups_search_empty, constraint));
                }
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    private class GroupsAdapter extends AdapterWithLifecycle<ViewHolderWithLifecycle> implements Filterable {

        private List<Group> groups;
        private List<Group> filteredGroups;
        private CharSequence filterText;
        private List<String> filterTokens;

        void setGroups(@NonNull List<Group> groups) {
            this.groups = groups;
            this.filteredGroups = new ArrayList<>(groups);
            getFilter().filter(filterText);
            notifyDataSetChanged();
        }

        @Override
        public @NonNull ViewHolderWithLifecycle onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.i("GroupsAdapter.onCreateViewHolder " + viewType);
            return new GroupsAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item, parent, false));

        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            if (position < getFilteredContactsCount()) {
                if (holder instanceof GroupsAdapter.ViewHolder) {
                    ((GroupsAdapter.ViewHolder) holder).bindTo(filteredGroups.get(position), filterTokens);
                }
            }
        }

        @Override
        public int getItemCount() {
            return getFilteredContactsCount();
        }

        void setFilteredGroups(@NonNull List<Group> groups, CharSequence filterText) {
            this.filteredGroups = groups;
            this.filterText = filterText;
            this.filterTokens = FilterUtils.getFilterTokens(filterText);
            notifyDataSetChanged();
        }

        private int getFilteredContactsCount() {
            return filteredGroups == null ? 0 : filteredGroups.size();
        }

        @Override
        public Filter getFilter() {
            return new GroupsFilter(groups);
        }

        class ViewHolder extends ViewHolderWithLifecycle {

            final ImageView avatarView;
            final TextView nameView;
            final TextView infoView;
            final TextView newMessagesView;
            final TextView timeView;
            final View infoContainer;
            final View selectionView;
            final View selectionCheck;

            private Group group;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                infoView = itemView.findViewById(R.id.info);
                timeView = itemView.findViewById(R.id.time);
                newMessagesView = itemView.findViewById(R.id.new_posts);
                infoContainer = itemView.findViewById(R.id.info_container);
                selectionView = itemView.findViewById(R.id.selection_background);
                selectionCheck = itemView.findViewById(R.id.selection_check);
                itemView.setOnClickListener(v -> {
                    startActivity(ViewGroupFeedActivity.viewFeed(GroupsInCommonActivity.this, group.groupId));
                });
            }

            void bindTo(@NonNull Group group, @Nullable List<String> filterTokens) {
                boolean differentGroup = this.group == null || !Objects.equals(group.groupId, this.group.groupId);
                this.group = group;
                selectionView.setVisibility(View.GONE);
                selectionCheck.setVisibility(View.GONE);

                avatarLoader.load(avatarView, group.groupId);
                CharSequence name = group.name;
                if (filterTokens != null && !filterTokens.isEmpty()) {
                    CharSequence formattedName = FilterUtils.formatMatchingText(itemView.getContext(), group.name, filterTokens);
                    if (formattedName != null) {
                        name = formattedName;
                    }
                }
                nameView.setText(name);

                groupPostLoader.load(infoView, group.groupId, new ViewDataLoader.Displayer<View, Post>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable Post result) {
                        if (result != null) {
                            infoView.setVisibility(View.VISIBLE);
                            if (result.type == Post.TYPE_SYSTEM) {
                                bindGroupSystemPostPreview(result);
                            } else {
                                bindGroupPostPreview(result);
                            }
                        } else {
                            infoView.setText("");
                            infoView.setVisibility(View.GONE);
                            timeView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void showLoading(@NonNull View view) {
                        if (differentGroup) {
                            infoView.setVisibility(View.VISIBLE);
                            infoView.setText("");
                        }
                    }
                });
                unseenGroupPostsLoader.load(newMessagesView, new ViewDataLoader.Displayer<View, List<Post>>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable List<Post> result) {
                        if (result == null || result.size() == 0) {
                            newMessagesView.setVisibility(View.GONE);
                            timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), R.color.secondary_text));
                        } else {
                            newMessagesView.setVisibility(View.VISIBLE);
                            newMessagesView.setText(String.format(Locale.getDefault(), "%d", result.size()));
                            timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), R.color.unread_indicator));
                        }
                    }

                    @Override
                    public void showLoading(@NonNull View view) {
                        if (differentGroup) {
                            newMessagesView.setVisibility(View.GONE);
                        }
                    }
                }, group.groupId);
            }

            private void bindGroupSystemPostPreview(@NonNull Post post) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setText(TimeFormatter.formatRelativeTime(timeView.getContext(), post.timestamp));
                contactLoader.cancel(infoView);
                systemMessageTextResolver.bindGroupSystemPostPreview(infoView, post);
            }

            private void bindGroupPostPreview(@NonNull Post post) {
                timeView.setVisibility(View.VISIBLE);
                timeView.setText(TimeFormatter.formatRelativeTime(timeView.getContext(), post.timestamp));
                if (post.isIncoming()) {
                    contactLoader.load(infoView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                        @Override
                        public void showResult(@NonNull TextView view, @Nullable Contact result) {
                            if (result == null) {
                                return;
                            }
                            bindPostCaption(result.getDisplayName(), post);
                        }

                        @Override
                        public void showLoading(@NonNull TextView view) {
                            infoView.setText("");
                        }
                    });
                } else {
                    contactLoader.cancel(infoView);
                    bindOwnPostCaption(post);
                }
            }

            private void bindPostCaption(@NonNull String sender, @NonNull Post post) {
                textContentLoader.load(infoView, post, new TextContentLoader.TextDisplayer() {
                    @Override
                    public void showResult(TextView tv, CharSequence text) {
                        if (post.isRetracted()) {
                            infoView.setText(getString(R.string.post_preview_retracted, sender));
                        } else if (TextUtils.isEmpty(post.text) || post.type == Post.TYPE_FUTURE_PROOF) {
                            infoView.setText(getString(R.string.post_preview_no_caption, sender));
                        } else {
                            infoView.setText(getString(R.string.post_preview_with_caption, sender, text));
                        }
                    }

                    @Override
                    public void showPreview(TextView tv, CharSequence text) {
                        infoView.setText("");
                    }
                });
            }

            private void bindOwnPostCaption(@NonNull Post post) {
                textContentLoader.load(infoView, post, new TextContentLoader.TextDisplayer() {
                    @Override
                    public void showResult(TextView tv, CharSequence text) {
                        if (post.isRetracted()) {
                            infoView.setText(getString(R.string.post_preview_retracted_by_you));
                        } else if (TextUtils.isEmpty(post.text) || post.type == Post.TYPE_FUTURE_PROOF) {
                            infoView.setText(getString(R.string.post_preview_no_caption_by_you));
                        } else {
                            infoView.setText(getString(R.string.post_preview_with_caption_by_you, text));
                        }
                    }

                    @Override
                    public void showPreview(TextView tv, CharSequence text) {
                        infoView.setText("");
                    }
                });
            }
        }
    }
}
