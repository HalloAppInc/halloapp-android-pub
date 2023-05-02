package com.halloapp.ui.groups;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.id.GroupId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.GridAutofitLayoutManager;

public class GroupBackgroundActivity extends HalloActivity {

    private static final String EXTRA_GROUP_ID = "group_id";

    public static Intent newIntent(@NonNull Context context, @NonNull GroupId groupId) {
        Preconditions.checkNotNull(groupId);
        Intent intent = new Intent(context, GroupBackgroundActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }

    private GroupId groupId;

    private GroupBackgroundViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupId = getIntent().getParcelableExtra(EXTRA_GROUP_ID);
        if (groupId == null) {
            throw new IllegalArgumentException("You must specify a group id for a group feed fragment");
        }

        viewModel = new ViewModelProvider(this, new GroupBackgroundViewModel.Factory(groupId)).get(GroupBackgroundViewModel.class);

        setContentView(R.layout.activity_group_background);

        View container = findViewById(R.id.container);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.group_bg_picker_background));

        RecyclerView rv = findViewById(R.id.bg_colors);
        GridAutofitLayoutManager layoutManager = new GridAutofitLayoutManager(this, getResources().getDimensionPixelSize(R.dimen.group_bg_item_width));

        rv.setLayoutManager(layoutManager);

        BgAdapter bgAdapter = new BgAdapter();
        rv.setAdapter(bgAdapter);

        viewModel.getBackground().observe(this, theme -> {
            if (theme == null) {
                return;
            }
            bgAdapter.setSelected(theme);
            GroupTheme groupTheme = GroupTheme.getTheme(theme);
            container.setBackgroundColor(ContextCompat.getColor(this, groupTheme.bgColor));
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_background_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.save);
        SpannableString ss = new SpannableString(getString(R.string.save));
        ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getBaseContext(), R.color.color_secondary)), 0, ss.length(), 0);
        menuItem.setTitle(ss);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            ProgressDialog progressDialog = ProgressDialog.show(this, null, getString(R.string.group_background_saving_dialog));
            viewModel.saveBackground().observe(this, result -> {
                if (result == null) {
                    return;
                }
                progressDialog.dismiss();
                Toast.makeText(this, result ? R.string.group_background_save_success : R.string.group_background_save_failed, Toast.LENGTH_LONG).show();
                finish();
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class ColorBgViewHolder extends RecyclerView.ViewHolder {

        private final FrameLayout bgColorView;
        private final View selectedRing;

        private int position;

        public ColorBgViewHolder(@NonNull View itemView) {
            super(itemView);

            bgColorView = itemView.findViewById(R.id.bg_color);
            selectedRing = itemView.findViewById(R.id.selected_ring);

            itemView.setOnClickListener(v -> viewModel.setBackground(position));
        }

        public void bind(int position, boolean selected) {
            GroupTheme theme = GroupTheme.getTheme(position);
            selectedRing.setVisibility(selected ? View.VISIBLE : View.GONE);
            bgColorView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(GroupBackgroundActivity.this, theme.bgColor)));
            this.position = position;
        }
    }

    private class BgAdapter extends RecyclerView.Adapter<ColorBgViewHolder> {

        private int selected;

        @NonNull
        @Override
        public ColorBgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ColorBgViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.group_theme_item, parent, false));
        }

        public void setSelected(int index) {
            this.selected = index;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(@NonNull ColorBgViewHolder holder, int position) {
            holder.bind(position, position == selected);
        }

        @Override
        public int getItemCount() {
            return 11;
        }
    }
}
