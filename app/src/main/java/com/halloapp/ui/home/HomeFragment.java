package com.halloapp.ui.home;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.R;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.PostComposerActivity;
import com.halloapp.ui.PostsFragment;
import com.halloapp.util.Log;
import com.halloapp.widget.BadgedDrawable;

import java.util.ArrayList;
import java.util.Collections;

public class HomeFragment extends PostsFragment {

    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 2;

    private BadgedDrawable notificationDrawable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HomeFragment: onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("HomeFragment: onDestroy");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        final RecyclerView postsView = root.findViewById(R.id.posts);
        final View emptyView = root.findViewById(android.R.id.empty);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);

        final HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.postList.observe(this, posts -> adapter.submitList(posts, () -> {
            final View childView = layoutManager.getChildAt(0);
            final boolean scrolled = childView == null || !(childView.getTop() == 0 && layoutManager.getPosition(childView) == 0);
            if (viewModel.checkPendingOutgoing() || !scrolled) {
                postsView.scrollToPosition(0);
            } else if (viewModel.checkPendingIncoming()) {
                postsView.smoothScrollBy(0, -getResources().getDimensionPixelSize(R.dimen.incoming_post_scroll_up));
            }
            emptyView.setVisibility(posts.size() == 0 ? View.VISIBLE : View.GONE);
        }));

        final float scrolledElevation = getResources().getDimension(R.dimen.action_bar_elevation);
        postsView.addOnScrollListener(new RecyclerView.OnScrollListener() {

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

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        postsView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        final MenuItem notificationsMenuItem = menu.findItem(R.id.notifications);
        notificationDrawable = new BadgedDrawable(
                Preconditions.checkNotNull(getContext()),
                notificationsMenuItem.getIcon(),
                getResources().getColor(R.color.badge_text),
                getResources().getColor(R.color.badge_background),
                getResources().getColor(R.color.window_background),
                getResources().getDimension(R.dimen.badge));
        notificationsMenuItem.setIcon(notificationDrawable);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notifications: {
                // testing-only
                String badge = notificationDrawable.getBadge();
                if (TextUtils.isEmpty(badge)) {
                    notificationDrawable.setBadge("1");
                } else {
                    int newBadge = (Integer.parseInt(badge) + 1) % 10;
                    notificationDrawable.setBadge(newBadge == 0 ? null : Integer.toString(newBadge));
                }
                // TODO (ds): open notifications
                return true;
            }
            case R.id.add_post_text: {
                startActivity(new Intent(getContext(), PostComposerActivity.class));
                return true;
            }
            case R.id.add_post_gallery: {
                getImageFromGallery();
                return true;
            }
            case R.id.add_post_camera: {
                getImageFromCamera();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        switch (request) {
            case REQUEST_CODE_PICK_IMAGE: {
                if (result == Activity.RESULT_OK) {
                    if (data == null) {
                        Log.e("HomeFragment.onActivityResult.REQUEST_CODE_PICK_IMAGE: no data");
                        Toast.makeText(getContext(), R.string.bad_image, Toast.LENGTH_SHORT).show();
                    } else {
                        final ArrayList<Uri> uris = new ArrayList<>();
                        final ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                uris.add(clipData.getItemAt(i).getUri());
                            }
                        } else {
                            final Uri uri = data.getData();
                            if (uri != null) {
                                uris.add(uri);
                            }
                        }
                        if (!uris.isEmpty()) {
                            final Intent intent = new Intent(getContext(), PostComposerActivity.class);
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            startActivity(intent);
                        } else {
                            Log.e("HomeFragment.onActivityResult.REQUEST_CODE_PICK_IMAGE: no uri");
                            Toast.makeText(getContext(), R.string.bad_image, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            }
            case REQUEST_CODE_CAPTURE_IMAGE: {
                if (result == Activity.RESULT_OK) {
                    final Intent intent = new Intent(getContext(), PostComposerActivity.class);
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                            new ArrayList<>(Collections.singleton(MediaUtils.getImageCaptureUri(Preconditions.checkNotNull(getContext())))));
                    startActivity(intent);
                }
                break;
            }
        }
    }

    private void getImageFromGallery() {
        final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        if (Build.VERSION.SDK_INT >= 26) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toURI()); // TODO (ds): doesn't seem to work properly, need to investigate
        }
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void getImageFromCamera() {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaUtils.getImageCaptureUri(Preconditions.checkNotNull(getContext())));
        startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE);
    }
}