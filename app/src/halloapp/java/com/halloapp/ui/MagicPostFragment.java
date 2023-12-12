package com.halloapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.halloapp.GalleryWorker;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.Suggestion;
import com.halloapp.content.ContentDb;
import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.ui.mediapicker.GalleryThumbnailLoader;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

@RequiresApi(api = 24)
public class MagicPostFragment extends HalloFragment implements MainNavFragment, EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_POST = 2;

    private LinearLayoutManager layoutManager;
    private MagicPostViewModel viewModel;
    private SuggestionsAdapter suggestionsListAdapter;
    private RecyclerView suggestionsListView;
    private View emptyView;
    private BottomNavigationView navView;
    private GalleryThumbnailLoader thumbnailLoader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View root = inflater.inflate(R.layout.fragment_magic_post, container, false);

        layoutManager = new LinearLayoutManager(requireContext());
        navView = requireActivity().findViewById(R.id.nav_view);

        Point point = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getRealSize(point);
        viewModel = new ViewModelProvider(this, new MagicPostViewModel.Factory(requireActivity().getApplication(), ContentDb.getInstance(), BgWorkers.getInstance(), point)).get(MagicPostViewModel.class);

        suggestionsListView = root.findViewById(R.id.suggestions_list);
        emptyView = root.findViewById(R.id.empty);
        View nuxView = root.findViewById(R.id.magic_post_nux);
        View nuxViewDismiss = root.findViewById(R.id.magic_post_nux_dismiss);

        nuxViewDismiss.setOnClickListener(v -> {
            viewModel.setShowMagicPostNux(true);
        });

        final LinearLayoutManager suggestionsLayoutManager = new LinearLayoutManager(requireContext());
        suggestionsListView.setLayoutManager(suggestionsLayoutManager);

        suggestionsListAdapter = new SuggestionsAdapter(new HeaderFooterAdapter.HeaderFooterAdapterParent() {

            @NonNull
            @Override
            public Context getContext() {
                return requireContext();
            }

            @NonNull
            @Override
            public ViewGroup getParentViewGroup() {
                return suggestionsListView;
            }
        });

        suggestionsListView.setAdapter(suggestionsListAdapter);

        if (suggestionsListAdapter.getInternalItemCount() == 0) {
            loadSuggestionsPlaceholder();
        }

        viewModel.getSuggestionsList().observe(getViewLifecycleOwner(), suggestions -> {
            Notifications.getInstance(getContext()).clearMagicPostNotification();
            suggestionsListView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            if (suggestions == null || suggestions.size() == 0) {
                suggestionsListView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                return;
            }
            Collections.sort(suggestions, (o1, o2) -> Long.compare(o2.timestamp, o1.timestamp));

            suggestionsListAdapter.submitItems(suggestions);
        });

        viewModel.getShowedMagicPostNux().observe(getViewLifecycleOwner(), showed -> {
            nuxView.setVisibility(showed ? View.GONE : View.VISIBLE);
        });

        thumbnailLoader = new GalleryThumbnailLoader(getContext(), getResources().getDimensionPixelSize(R.dimen.media_gallery_grid_size));
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel.getSuggestionsList().getValue() != null) {
            viewModel.getSuggestions();
        }
    }

    public void resetScrollPosition() {
        layoutManager.scrollToPosition(0);
    }

    private void requestPermissions() {
        final String[] perms;
        if (Build.VERSION.SDK_INT >= 33) {
            perms = new String[]{Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.ACCESS_MEDIA_LOCATION};
        } else if (Build.VERSION.SDK_INT >= 29) {
            perms = new String[]{Manifest.permission.ACCESS_MEDIA_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE};
        } else {
            perms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }
        if (!EasyPermissions.hasPermissions(getActivity(), perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.storage_permission_rationale),
                REQUEST_CODE_ASK_STORAGE_PERMISSION, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> list) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_ASK_STORAGE_PERMISSION: {
                if (Build.VERSION.SDK_INT >= 24) {
                    GalleryWorker.schedule(getContext());
                }
                loadSuggestionsPlaceholder();
                viewModel.invalidate();
                break;
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (requestCode) {
                case REQUEST_CODE_ASK_STORAGE_PERMISSION: {
                    new AppSettingsDialog.Builder(this)
                            .setRationale(getString(R.string.storage_permission_rationale_denied))
                            .build().show();
                    break;
                }
            }
        }
    }

    private void loadSuggestionsPlaceholder() {
        emptyView.setVisibility(View.GONE);
        suggestionsListView.setVisibility(View.VISIBLE);
        ArrayList<Suggestion> placeholderSuggestions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            placeholderSuggestions.add(new Suggestion());
        }
        suggestionsListAdapter.submitItems(placeholderSuggestions);
    }

    private class SuggestionsAdapter extends HeaderFooterAdapter<Suggestion> {

        private static final int ITEM_TYPE_PLACEHOLDER = 0;
        private static final int ITEM_TYPE_SUGGESTION = 1;

        public SuggestionsAdapter(@NonNull HeaderFooterAdapterParent parent) {
            super(parent);
        }

        @Override
        public long getIdForItem(@NonNull Suggestion suggestion) {
            return 0;
        }

        @Override
        public int getViewTypeForItem(@NonNull Suggestion suggestion) {
            return suggestion.isPlaceholder ? ITEM_TYPE_PLACEHOLDER : ITEM_TYPE_SUGGESTION;
        }

        @NonNull
        @Override
        public ViewHolderWithLifecycle createViewHolderForViewType(@NonNull ViewGroup parent, int viewType) {
            if (viewType == ITEM_TYPE_PLACEHOLDER) {
                return new PlaceholderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.placeholder_suggestion_item, parent, false));
            } else if (viewType == ITEM_TYPE_SUGGESTION) {
                return new SuggestionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestion_item, parent, false));
            } else {
                throw new IllegalArgumentException("Unexpected viewType " + viewType);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            Suggestion suggestion = getItem(position);
            if (holder instanceof SuggestionViewHolder && suggestion != null) {
                ((SuggestionViewHolder) holder).bindTo(suggestion);
            }
        }
    }

    private static class PlaceholderViewHolder extends ViewHolderWithLifecycle {

        public PlaceholderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private class SuggestionViewHolder extends ViewHolderWithLifecycle {

        private final ImageView thumbnailTopLeft, thumbnailTopRight, thumbnailBottomLeft, thumbnailBottomRight;
        private final TextView suggestionText, suggestionTime, suggestionTitle;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailTopLeft = itemView.findViewById(R.id.thumbnail_top_left);
            thumbnailTopRight = itemView.findViewById(R.id.thumbnail_top_right);
            thumbnailBottomLeft = itemView.findViewById(R.id.thumbnail_bottom_left);
            thumbnailBottomRight = itemView.findViewById(R.id.thumbnail_bottom_right);
            suggestionText = itemView.findViewById(R.id.suggestion_text);
            suggestionTime = itemView.findViewById(R.id.time);
            suggestionTitle = itemView.findViewById(R.id.title);
        }

        public void bindTo(@NonNull Suggestion item) {
            setThumbnails(item.thumbnails);
            suggestionTitle.setVisibility(TextUtils.isEmpty(item.locationName) ? View.GONE : View.VISIBLE);
            suggestionText.setText(TextUtils.isEmpty(item.locationAddress)
                    ? getString(R.string.suggestion_text_template, item.size, formatRelativeTime(item.timestamp))
                    : getString(R.string.suggestion_text_with_location_template, item.size, item.locationAddress));
            suggestionTime.setText(formatRelativeTime(item.timestamp));
            suggestionTitle.setText(item.locationName);

            itemView.setOnClickListener(v -> {
                Intent intent = MediaPickerActivity.pickForMagicPost(getContext(), item.id, item.size);
                startActivityForResult(intent, REQUEST_CODE_POST);
            });
        }

        private void setThumbnails(@NonNull GalleryItem[] thumbnails) {
            if (thumbnails[0] != null) {
                thumbnailLoader.load(thumbnailTopLeft, thumbnails[0]);
            } else {
                thumbnailTopLeft.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_suggestion_image));
            }
            if (thumbnails[1] != null) {
                thumbnailLoader.load(thumbnailTopRight, thumbnails[1]);
            }
            if (thumbnails[2] != null) {
                thumbnailLoader.load(thumbnailBottomLeft, thumbnails[2]);
            }
            if (thumbnails[3] != null) {
                thumbnailLoader.load(thumbnailBottomRight, thumbnails[3]);
            }
            thumbnailTopRight.setVisibility(thumbnails[1] == null ? View.GONE : View.VISIBLE);
            thumbnailBottomLeft.setVisibility(thumbnails[2] == null ? View.GONE : View.VISIBLE);
            thumbnailBottomRight.setVisibility(thumbnails[3] == null ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_POST && resultCode == Activity.RESULT_OK) {
            navView.setSelectedItemId(R.id.navigation_home);
        }
    }

    private String formatRelativeTime(long time) {
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(time);
        calendar.setTime(date);
        if (TimeUtils.isSameDay(time, System.currentTimeMillis())) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            sdf.setTimeZone(TimeZone.getDefault());
            return sdf.format(date).toLowerCase(Locale.getDefault());
        } else {
            return TimeUtils.getDayMonth(time);
        }
    }
}
