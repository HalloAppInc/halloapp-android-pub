package com.halloapp.ui.mediaexplorer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.id.ChatId;
import com.halloapp.media.MediaUtils;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.TransitionUtils;
import com.halloapp.util.ViewUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.VerticalSpaceDecoration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class AlbumExplorerActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {
    public static final String EXTRA_MEDIA = "media";
    public static final String EXTRA_SELECTED = "selected";
    public static final String EXTRA_CONTENT_ID = "content-id";
    public static final String EXTRA_CHAT_ID = "chat-id";
    public static final String EXTRA_ALLOW_SAVING = "allow_saving";

    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSIONS = 1;

    private AlbumExplorerViewModel viewModel;

    private MediaExplorerAdapter adapter;

    private RecyclerView mediaRv;

    private boolean allowSaving;

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private String contentId;
    private ChatId chatId;

    private int screenWidth;

    public static Intent openAlbum(Context context, List<Media> media, int index, String contentId, ChatId chatId) {
        Intent intent = new Intent(context, AlbumExplorerActivity.class);
        intent.putExtra(AlbumExplorerActivity.EXTRA_MEDIA, AlbumExplorerViewModel.MediaModel.fromMedia(media));
        intent.putExtra(AlbumExplorerActivity.EXTRA_SELECTED, index);
        intent.putExtra(AlbumExplorerActivity.EXTRA_CONTENT_ID, contentId);
        intent.putExtra(AlbumExplorerActivity.EXTRA_CHAT_ID, chatId);
        intent.putExtra(AlbumExplorerActivity.EXTRA_ALLOW_SAVING, true);
        return intent;
    }

    private ArrayList<AlbumExplorerViewModel.MediaModel> media;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        supportRequestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        postponeEnterTransition();

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        contentId = getIntent().getStringExtra(EXTRA_CONTENT_ID);
        int selected = getIntent().getIntExtra(EXTRA_SELECTED, 0);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;

        setContentView(R.layout.activity_album_explorer);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        mediaRv = findViewById(R.id.media_rv);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mediaRv.setLayoutManager(lm);
        lm.scrollToPosition(selected);

        mediaRv.addItemDecoration(new VerticalSpaceDecoration(getResources().getDimensionPixelSize(R.dimen.album_list_view_vertical_margin)));

        media = getIntent().getParcelableArrayListExtra(EXTRA_MEDIA);
        if (media == null || media.size() == 0) {
            finish();
            return;
        }

        TextView titleView = findViewById(R.id.title);
        TextView subtitleView = findViewById(R.id.subtitle);
        ImageView avatarView = findViewById(R.id.avatar);

        chatId = getIntent().getParcelableExtra(EXTRA_CHAT_ID);

        avatarLoader.load(avatarView, chatId);

        adapter = new MediaExplorerAdapter(media);
        mediaRv.setAdapter(adapter);
        finishEnterTransitionWhenReady();

        allowSaving = getIntent().getBooleanExtra(EXTRA_ALLOW_SAVING, false);

        AlbumExplorerViewModel.Factory factory = new AlbumExplorerViewModel.Factory(getApplication(), chatId, media, getIntent().getIntExtra(EXTRA_SELECTED, 0));
        viewModel = new ViewModelProvider(this, factory).get(AlbumExplorerViewModel.class);

        viewModel.getName().observe(this, name -> {
            titleView.setText(name);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_to_gallery) {
            if (Build.VERSION.SDK_INT < 29) {
                if (!EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    EasyPermissions.requestPermissions(this, getString(R.string.save_to_gallery_storage_permission_rationale), REQUEST_EXTERNAL_STORAGE_PERMISSIONS, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    return true;
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSIONS) {
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSIONS) {
            if (EasyPermissions.permissionPermanentlyDenied(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                new AppSettingsDialog.Builder(this)
                        .setRationale(getString(R.string.save_to_gallery_storage_permission_rationale_denied))
                        .build().show();
            }
        }
    }

    @MainThread
    private void finishEnterTransitionWhenReady() {
        mediaRv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mediaRv.getChildCount() == 0) {
                    return;
                }
                int selected = getIntent().getIntExtra(EXTRA_SELECTED, 0);
                MediaViewHolder mediaViewHolder = (MediaViewHolder) mediaRv.findViewHolderForAdapterPosition(selected);
                if (mediaViewHolder == null) {
                    return;
                }
                TransitionUtils.finishTransitionSystemViews(AlbumExplorerActivity.this);
                mediaRv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mediaViewHolder.imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (mediaViewHolder.imageView.getDrawable() == null) {
                            return;
                        }

                        mediaViewHolder.imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        startPostponedEnterTransition();
                    }
                });
            }
        });
    }

    private class MediaViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final ImageView playBtn;

        private AlbumExplorerViewModel.MediaModel mediaModel;
        private int position;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            ViewUtils.clipRoundedRect(imageView, R.dimen.album_list_view_corner_radius);
            imageView.setOnClickListener(v -> {
                Intent intent = new Intent(imageView.getContext(), MediaExplorerActivity.class);
                intent.putExtra(MediaExplorerActivity.EXTRA_MEDIA, media);
                intent.putExtra(MediaExplorerActivity.EXTRA_SELECTED, position);
                intent.putExtra(MediaExplorerActivity.EXTRA_CONTENT_ID, contentId);
                intent.putExtra(MediaExplorerActivity.EXTRA_CHAT_ID, chatId);
                intent.putExtra(MediaExplorerActivity.EXTRA_ALLOW_SAVING, true);

                if (imageView.getContext() instanceof Activity) {
                    final ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(AlbumExplorerActivity.this, imageView, imageView.getTransitionName());
                    startActivity(intent, options.toBundle());
                } else {
                    startActivity(intent);
                }
            });
            playBtn = itemView.findViewById(R.id.play);
        }

        public void bindTo(@NonNull AlbumExplorerViewModel.MediaModel model, int position) {
            this.mediaModel = model;
            this.position = position;
            imageView.setTag(model);
            imageView.setImageBitmap(null);
            if (model.width != 0 && model.height != 0) {
                ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
                int viewWidth = screenWidth;
                layoutParams.height = model.height * viewWidth / model.width;
                itemView.setLayoutParams(layoutParams);
                imageView.setTransitionName(MediaPagerAdapter.getTransitionName(contentId, position));
            }
            if (model.type == Media.MEDIA_TYPE_VIDEO) {
                playBtn.setVisibility(View.VISIBLE);
            } else {
                playBtn.setVisibility(View.GONE);
            }
            bgWorkers.execute(() -> {
                Bitmap bitmap;
                try {
                    if (model.type == Media.MEDIA_TYPE_IMAGE) {
                        bitmap = MediaUtils.decodeImage(new File(model.uri.getPath()), Constants.MAX_IMAGE_DIMENSION);
                    } else if (model.type == Media.MEDIA_TYPE_VIDEO) {
                        bitmap = MediaUtils.decodeVideo(new File(model.uri.getPath()), Constants.MAX_IMAGE_DIMENSION);
                    } else {
                        bitmap = null;
                    }
                } catch (IOException e) {
                    Log.e("AlbumExplorerActivity: unable to bind image", e);
                    return;
                }

                imageView.post(() -> {
                    if (imageView.getTag() == model) {
                        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
                        if (bitmap != null) {
                            int width = itemView.getWidth();
                            if (bitmap.getWidth() != 0) {
                                int newHeight = bitmap.getHeight() * width / bitmap.getWidth();
                                if (layoutParams.height != newHeight) {
                                    layoutParams.height = newHeight;
                                    itemView.setLayoutParams(layoutParams);
                                }
                            }
                        } else {
                            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                            itemView.setLayoutParams(layoutParams);
                        }
                        imageView.setImageBitmap(bitmap);
                    }
                });
            });
        }
    }

    private class MediaExplorerAdapter extends RecyclerView.Adapter<MediaViewHolder> {

        private List<AlbumExplorerViewModel.MediaModel> list;

        MediaExplorerAdapter(List<AlbumExplorerViewModel.MediaModel> list) {
            this.list = list;
        }

        @Override
        public int getItemViewType(int position) {
            AlbumExplorerViewModel.MediaModel model = list.get(position);
            return model != null ? model.type : Media.MEDIA_TYPE_UNKNOWN;
        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }

        @NonNull
        @Override
        public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MediaViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.album_explorer_media_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
            AlbumExplorerViewModel.MediaModel model = list.get(position);

            if (model != null) {
                holder.bindTo(model, position);
            }
        }
    }
}
