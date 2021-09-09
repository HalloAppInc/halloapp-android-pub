package com.halloapp.ui.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.content.Chat;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class PostSharePickerActivity extends HalloActivity {

    private GroupsViewModel viewModel;
    private TextView emptyView;

    private static final String EXTRA_TITLE_RES = "title_res";

    public static final String RESULT_SELECTED_ID = "selected_id";
    public static final String RESULT_SELECTED_TYPE = "selected_item_type";

    public static final String RESULT_SELECTED_HOME = "type_home";
    public static final String RESULT_SELECTED_GROUP = "type_group";


    private final PostSharePickerActivity.GroupsAdapter adapter = new PostSharePickerActivity.GroupsAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    public static Intent createSharePicker(@NonNull Context context) {
        return new Intent(context, PostSharePickerActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_share_post_picker);


        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        int titleRes = getIntent().getIntExtra(EXTRA_TITLE_RES, 0);
        if (titleRes != 0) {
            setTitle(titleRes);
        }

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
        searchBox.requestFocus();
        final RecyclerView listView = findViewById(android.R.id.list);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);
        emptyView = findViewById(android.R.id.empty);

        viewModel = new ViewModelProvider(this).get(GroupsViewModel.class);
        viewModel.groupsList.getLiveData().observe(this, adapter::setGroups);

        loadGroups();
    }

    private void loadGroups() {
        viewModel.groupsList.invalidate();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bindTo(@NonNull Chat group, List<String> filterTokens) {
        }

    }

    class GroupsViewHolder extends ViewHolder {

        final private ImageView avatarView;
        final private TextView nameView;

        private Chat group;

        GroupsViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra(RESULT_SELECTED_ID,  Preconditions.checkNotNull(group.chatId).rawId());
                result.putExtra(RESULT_SELECTED_TYPE,  RESULT_SELECTED_GROUP);
                setResult(RESULT_OK, result);
                finish();
            });
        }

        void bindTo(@NonNull Chat group, List<String> filterTokens) {
            this.group = group;
            avatarLoader.load(avatarView, Preconditions.checkNotNull(group.chatId), false);
            if (filterTokens != null && !filterTokens.isEmpty()) {
                String name = group.name;
                CharSequence formattedName = FilterUtils.formatMatchingText(PostSharePickerActivity.this, name, filterTokens);
                if (formattedName != null) {
                    nameView.setText(formattedName);
                } else {
                    nameView.setText(name);
                }
            } else {
                nameView.setText(group.name);
            }
        }
    }


    class HomeViewHolder extends ViewHolder {

        HomeViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra(RESULT_SELECTED_TYPE,  RESULT_SELECTED_HOME);
                setResult(RESULT_OK, result);
                finish();
            });
        }
    }

    private class GroupsAdapter extends RecyclerView.Adapter<PostSharePickerActivity.ViewHolder> implements Filterable {

        private List<Chat> groups = new ArrayList<>();
        private List<Chat> filteredGroups;
        private CharSequence filterText;
        private List<String> filterTokens;

        private static final int ITEM_HOME_FEED = 0;
        private static final int ITEM_GROUP = 1;

        @Override
        public Filter getFilter() {
            return new PostSharePickerActivity.GroupsFilter(groups);
        }

        void setGroups(@NonNull List<Chat> groups) {
            this.groups = groups;
            getFilter().filter(filterText);
        }

        void setFilteredGroups(@NonNull List<Chat> groups, CharSequence filterText) {
            this.filteredGroups = groups;
            this.filterText = filterText;
            this.filterTokens = FilterUtils.getFilterTokens(filterText);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == ITEM_HOME_FEED) {
                return new HomeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_item, parent, false));
            } else {
                return new GroupsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_list_item, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (holder instanceof HomeViewHolder) {
                return;
            }
            if(filteredGroups != null) {
                int adjustedPosition;
                if (isSearching()) {
                    adjustedPosition = position;
                } else {
                    adjustedPosition = position - 1;
                }
                if (adjustedPosition < getFilteredGroupsCount()) {
                    holder.bindTo(filteredGroups.get(adjustedPosition), filterTokens);
                }
            }
        }

        private int getFilteredGroupsCount() {
            return filteredGroups == null ? 0 : filteredGroups.size();
        }

        @Override
        public int getItemCount() {
            return getFilteredGroupsCount() + (isSearching() ? 0 : 1);
        }

        private boolean isSearching() {
            return filteredGroups != null && filteredGroups.size() != groups.size();
        }

        public int getItemViewType(int position){
            if (isSearching()) {
                return ITEM_GROUP;
            }
            if (position == 0) {
                return ITEM_HOME_FEED;
            } else {
                return ITEM_GROUP;
            }
        }

    }

    private class GroupsFilter extends FilterUtils.ItemFilter<Chat> {

        public GroupsFilter(@NonNull List<Chat> chats) {
            super(chats);
        }

        @Override
        protected String itemToString(Chat group) {
            return group.name;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            final List<Chat> filteredGroups = (List<Chat>) results.values;
            adapter.setFilteredGroups(filteredGroups, constraint);
            if (filteredGroups.isEmpty() && !TextUtils.isEmpty(constraint)) {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(getString(R.string.contact_search_empty, constraint));
            } else {
                emptyView.setVisibility(View.GONE);
            }
        }
    }
}

