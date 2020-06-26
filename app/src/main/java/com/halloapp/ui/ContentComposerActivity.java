package com.halloapp.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Post;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.mentions.MentionPickerView;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.Rtl;
import com.halloapp.util.StringUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.ClippedBitmapDrawable;
import com.halloapp.widget.ContentPhotoView;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.LinearSpacingItemDecoration;
import com.halloapp.widget.MediaViewPager;
import com.halloapp.widget.MentionableEntry;
import com.halloapp.widget.PlaceholderDrawable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContentComposerActivity extends HalloActivity {

    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_REPLY_POST_ID = "reply_post_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";

    private ContentComposerViewModel viewModel;
    private MediaThumbnailLoader fullThumbnailLoader;
    private MediaThumbnailLoader smallThumbnailLoader;
    private TextContentLoader textContentLoader;
    private MentionableEntry editText;
    private MentionPickerView mentionPickerView;
    private MediaViewPager mediaPager;
    private MediaPagerAdapter mediaPagerAdapter;
    private MediaListAdapter mediaListAdapter;
    private RecyclerView mediaList;
    private DrawDelegateView drawDelegateView;
    private View replyContainer;

    private String replyPostId;
    private int replyPostMediaIndex;

    private static final int REQUEST_CODE_CROP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ContentComposerActivity: onCreate");

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_content_composer);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        fullThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        smallThumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.details_media_list_height));
        textContentLoader = new TextContentLoader(this);

        mentionPickerView = findViewById(R.id.mention_picker_view);
        editText = findViewById(R.id.entry);
        editText.setMentionPickerView(mentionPickerView);

        final View sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(v -> {
            final Pair<String, List<Mention>> textAndMentions = editText.getTextWithMentions();
            final String postText = StringUtils.preparePostText(textAndMentions.first);
            if (TextUtils.isEmpty(postText) && viewModel.getMedia() == null) {
                Log.w("ContentComposerActivity: cannot send empty content");
                return;
            }
            viewModel.prepareContent(getIntent().getStringExtra(EXTRA_CHAT_ID), postText.trim(), textAndMentions.second);
        });

        final View progressView = findViewById(R.id.progress);

        final ArrayList<Uri> uris;
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            final Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            if (uri != null) {
                uris = new ArrayList<>(Collections.singleton(uri));
            } else {
                uris = null;
            }
        } else {
            uris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        }

        if (uris != null) {
            progressView.setVisibility(View.VISIBLE);
            if (uris.size() > Constants.MAX_POST_MEDIA_ITEMS) {
                CenterToast.show(this, getResources().getQuantityString(R.plurals.max_post_media_items, Constants.MAX_POST_MEDIA_ITEMS, Constants.MAX_POST_MEDIA_ITEMS));
                uris.subList(Constants.MAX_POST_MEDIA_ITEMS, uris.size()).clear();
            }
            sendButton.setEnabled(false);
            editText.setHint(R.string.type_a_caption_hint);
        } else {
            progressView.setVisibility(View.GONE);
            editText.requestFocus();
            editText.setHint(R.string.type_a_post_hint);
            editText.setPreImeListener((keyCode, event) -> {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    finish();
                    return true;
                }
                return false;
            });
        }

        if (savedInstanceState == null) {
            replyPostId = getIntent().getStringExtra(EXTRA_REPLY_POST_ID);
            replyPostMediaIndex = getIntent().getIntExtra(EXTRA_REPLY_POST_MEDIA_INDEX, -1);
        } else {
            replyPostId = savedInstanceState.getString(EXTRA_REPLY_POST_ID);
            replyPostMediaIndex = savedInstanceState.getInt(EXTRA_REPLY_POST_MEDIA_INDEX, -1);
        }
        if (replyPostId != null) {
            editText.requestFocus();
        }

        mediaList = findViewById(R.id.media_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mediaList.getContext(), RecyclerView.HORIZONTAL, false);
        mediaList.setLayoutManager(layoutManager);
        mediaList.addItemDecoration(new LinearSpacingItemDecoration(layoutManager, getResources().getDimensionPixelSize(R.dimen.details_media_list_spacing)));
        mediaListAdapter = new MediaListAdapter();
        mediaList.setAdapter(mediaListAdapter);

        final ItemTouchHelper.Callback ithCallback = new ItemTouchHelper.Callback() {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

                final List<Media> mediaList = viewModel.getMedia();
                if (mediaList == null) {
                    return false;
                }
                Media media = mediaList.remove(viewHolder.getAdapterPosition());
                mediaList.add(target.getAdapterPosition(), media);

                mediaPagerAdapter.setMedia(mediaList);
                setCurrentItem(target.getAdapterPosition(), false);

                media = mediaListAdapter.media.remove(viewHolder.getAdapterPosition());
                mediaListAdapter.media.add(target.getAdapterPosition(), media);

                mediaListAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.START | ItemTouchHelper.END);
            }
        };

        final ItemTouchHelper ith = new ItemTouchHelper(ithCallback);
        ith.attachToRecyclerView(mediaList);

        mediaPager = findViewById(R.id.media_pager);
        mediaPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.media_pager_margin));
        mediaPager.setVisibility(View.GONE);
        mediaPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mediaPagerAdapter = new MediaPagerAdapter();
        mediaPager.setAdapter(mediaPagerAdapter);

        drawDelegateView = findViewById(R.id.draw_delegate);

        viewModel = new ViewModelProvider(this,
                new ContentComposerViewModel.Factory(getApplication(), getIntent().getStringExtra(EXTRA_CHAT_ID), uris, replyPostId, replyPostMediaIndex)).get(ContentComposerViewModel.class);
        viewModel.media.observe(this, media -> {
            progressView.setVisibility(View.GONE);
            if (!media.isEmpty()) {
                mediaPager.setVisibility(View.VISIBLE);
                mediaPager.setOffscreenPageLimit(media.size());
            }
            mediaPagerAdapter.setMedia(media);
            if (media.size() <= 1) {
                mediaList.setVisibility(View.GONE);
            } else {
                mediaList.setVisibility(View.VISIBLE);
                mediaListAdapter.setMedia(media);
                setCurrentItem(0, false);
            }
            if (uris != null && media.size() != uris.size()) {
                CenterToast.show(getBaseContext(), R.string.failed_to_load_media);
            }
            sendButton.setEnabled(true);
            invalidateOptionsMenu();
        });
        viewModel.mentionableContacts.getLiveData().observe(this, new Observer<List<Contact>>() {
            @Override
            public void onChanged(List<Contact> contacts) {
                mentionPickerView.setMentionableContacts(contacts);
            }
        });
        viewModel.contentItem.observe(this, contentItem -> {
            if (contentItem != null) {
                contentItem.addToStorage(ContentDb.getInstance(getBaseContext()));
                setResult(RESULT_OK);
                finish();

                if (Intent.ACTION_SEND.equals(getIntent().getAction()) ||
                        Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
                    final Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_NAV_TARGET, MainActivity.NAV_TARGET_FEED);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        if (viewModel.chatName != null) {
            setTitle("");
            viewModel.chatName.getLiveData().observe(this, name -> {
                this.setTitle(name);
                if (replyPostId != null) {
                    final TextView replyNameView = findViewById(R.id.reply_name);
                    replyNameView.setText(name);
                }
            });
        } else {
            setTitle(R.string.new_post);
        }

        replyContainer = findViewById(R.id.reply_container);
        if (viewModel.replyPost != null) {
            viewModel.replyPost.getLiveData().observe(this, this::updatePostReply);
        } else {
            replyContainer.setVisibility(View.GONE);
        }
    }

    private void updatePostReply(@Nullable Post post) {
        if (post == null) {
            replyContainer.setVisibility(View.GONE);
        } else {
            replyContainer.setVisibility(View.VISIBLE);
            final TextView replyTextView = findViewById(R.id.reply_text);
            textContentLoader.load(replyTextView, post);
            final ImageView replyMediaIconView = findViewById(R.id.reply_media_icon);
            final ImageView replyMediaThumbView = findViewById(R.id.reply_media_thumb);
            if (replyPostMediaIndex >= 0 && replyPostMediaIndex < post.media.size()) {
                replyMediaThumbView.setVisibility(View.VISIBLE);
                replyMediaThumbView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.comment_media_list_corner_radius));
                    }
                });
                replyMediaThumbView.setClipToOutline(true);
                final Media media = post.media.get(replyPostMediaIndex);
                fullThumbnailLoader.load(replyMediaThumbView, media);
                replyMediaIconView.setVisibility(View.VISIBLE);
                switch (media.type) {
                    case Media.MEDIA_TYPE_IMAGE: {
                        replyMediaIconView.setImageResource(R.drawable.ic_camera);
                        if (TextUtils.isEmpty(post.text)) {
                            replyTextView.setText(R.string.photo);
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_VIDEO: {
                        replyMediaIconView.setImageResource(R.drawable.ic_video);
                        if (TextUtils.isEmpty(post.text)) {
                            replyTextView.setText(R.string.video);
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_UNKNOWN:
                    default: {
                        replyMediaIconView.setImageResource(R.drawable.ic_media_collection);
                        break;
                    }
                }
            } else {
                replyMediaThumbView.setVisibility(View.GONE);
                replyMediaIconView.setVisibility(View.GONE);
            }
            findViewById(R.id.reply_close).setOnClickListener(v -> {
                replyPostId = null;
                replyPostMediaIndex = -1;
                replyContainer.setVisibility(View.GONE);
            });
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (replyPostId != null) {
            outState.putString(EXTRA_REPLY_POST_ID, replyPostId);
            outState.putInt(EXTRA_REPLY_POST_MEDIA_INDEX, replyPostMediaIndex);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ContentComposerActivity: onDestroy");
        fullThumbnailLoader.destroy();
        smallThumbnailLoader.destroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.content_composer_menu, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem deleteMenuItem = menu.findItem(R.id.delete);
        final MenuItem cropMenuItem = menu.findItem(R.id.crop);
        final List<Media> media = viewModel.media.getValue();
        if (media == null) {
            deleteMenuItem.setVisible(false);
            cropMenuItem.setVisible(false);
        } else {
            deleteMenuItem.setVisible(media.size() > 0);
            cropMenuItem.setVisible(media.size() > getCurrentItem() && media.get(getCurrentItem()).type == Media.MEDIA_TYPE_IMAGE);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.crop: {
                final int currentItem = getCurrentItem();
                final List<Media> media = viewModel.media.getValue();
                if (media != null && media.size() > currentItem) {
                    final Media mediaItem = media.get(currentItem);
                    final Intent intent = new Intent(this, CropImageActivity.class);
                    intent.setData(Uri.fromFile(mediaItem.file));
                    intent.putExtra(CropImageActivity.EXTRA_STATE, viewModel.cropStates.get(mediaItem.file));
                    intent.putExtra(CropImageActivity.EXTRA_OUTPUT, Uri.fromFile(ContentComposerViewModel.getCropFile(mediaItem.file)));
                    startActivityForResult(intent, REQUEST_CODE_CROP);
                }
                return true;
            }
            case R.id.delete: {
                deleteCurrentItem();
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
        //noinspection SwitchStatementWithTooFewBranches
        switch (request) {
            case REQUEST_CODE_CROP: {
                if (result == RESULT_OK) {
                    onCropped(data);
                }
                break;
            }
        }
    }

    private void onCropped(@NonNull final Intent data) {
        if (data.getData() == null || data.getData().getPath() == null) {
            return;
        }
        final File origFile = new File(data.getData().getPath());
        final File cropFile = ContentComposerViewModel.getCropFile(origFile);
        smallThumbnailLoader.remove(origFile);
        smallThumbnailLoader.remove(cropFile);
        fullThumbnailLoader.remove(origFile);
        fullThumbnailLoader.remove(cropFile);
        viewModel.cropStates.put(origFile, data.getParcelableExtra(CropImageActivity.EXTRA_STATE));
        mediaListAdapter.notifyDataSetChanged();
        mediaPagerAdapter.notifyDataSetChanged();
        View view = mediaPager.findViewWithTag(origFile);
        if (view == null) {
            view = mediaPager.findViewWithTag(cropFile);
        }
        if (view instanceof ImageView) {
            final ImageView imageView = (ImageView)view;
            final Media displayMedia = new Media(0, Media.MEDIA_TYPE_IMAGE, null, cropFile, null, null, 0, 0, Media.TRANSFERRED_NO);
            imageView.setImageDrawable(null);
            fullThumbnailLoader.load(imageView, displayMedia);
        }
    }

    private void deleteCurrentItem() {
        final int currentItem = getCurrentItem();
        final List<Media> media = viewModel.media.getValue();
        if (media == null) {
            return;
        }
        media.remove(currentItem);
        mediaPagerAdapter.setMedia(media);
        mediaListAdapter.setMedia(media);
        if (!media.isEmpty()) {
            setCurrentItem(currentItem > media.size() ? media.size() - 1 : currentItem, true);
        } else {
            editText.setHint(R.string.type_a_post_hint);
            editText.requestFocus();
            final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
            imm.showSoftInput(editText,0);
        }
        if (media.size() <= 1) {
            mediaList.setVisibility(View.GONE);
        }
        invalidateOptionsMenu();
    }

    private class MediaPagerAdapter extends PagerAdapter {

        final List<Media> media = new ArrayList<>();

        MediaPagerAdapter() {
        }

        void setMedia(@NonNull List<Media> media) {
            this.media.clear();
            this.media.addAll(media);
            notifyDataSetChanged();
        }

        public int getItemPosition(@NonNull Object object) {
            int index = 0;
            final Object tag = ((View) object).getTag();
            for (Media mediaItem : media) {
                if (mediaItem.equals(tag)) {
                    return Rtl.isRtl(mediaPager.getContext()) ? media.size() - 1 - index : index;
                }
                index++;
            }
            return POSITION_NONE;
        }

        @Override
        public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position) {
            final View view = getLayoutInflater().inflate(R.layout.content_composer_media_pager_item, container, false);
            final ContentPhotoView imageView = view.findViewById(R.id.image);
            final View playButton = view.findViewById(R.id.play);
            final Media mediaItem = media.get(Rtl.isRtl(container.getContext()) ? media.size() - 1 - position : position);

            view.setTag(mediaItem);

            if (mediaItem.type == Media.MEDIA_TYPE_IMAGE) {
                if (mediaItem.height > Constants.MAX_IMAGE_ASPECT_RATIO * mediaItem.width) {
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            } else {
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            fullThumbnailLoader.load(imageView, mediaItem);
            if (mediaItem.type == Media.MEDIA_TYPE_VIDEO) {
                playButton.setVisibility(View.VISIBLE);
                playButton.setOnClickListener(v -> {
                    final Intent intent = new Intent(getBaseContext(), VideoPlaybackActivity.class);
                    intent.setData(Uri.fromFile(mediaItem.file));
                    startActivity(intent);
                });
            } else {
                playButton.setVisibility(View.GONE);
            }
            imageView.setDrawDelegate(drawDelegateView);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object view) {
            container.removeView((View) view);
        }

        @Override
        public int getCount() {
            return media.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    void setCurrentItem(int position, boolean smoothScroll) {
        mediaPager.setCurrentItem(Rtl.isRtl(mediaPager.getContext()) ? mediaPagerAdapter.getCount() - 1 - position : position, smoothScroll);
    }

    int getCurrentItem() {
        if (mediaPagerAdapter.getCount() == 0) {
            return 0;
        }
        return Rtl.isRtl(mediaPager.getContext()) ? mediaPagerAdapter.getCount() - 1 - mediaPager.getCurrentItem() : mediaPager.getCurrentItem();
    }

    private class MediaListAdapter extends RecyclerView.Adapter<MediaListAdapter.ViewHolder> {

        final List<Media> media = new ArrayList<>();

        MediaListAdapter() {
        }

        void setMedia(@NonNull List<Media> media) {
            this.media.clear();
            this.media.addAll(media);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.media_preview_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final Media mediaItem = media.get(position);
            holder.bind(mediaItem);
            holder.thumbnailView.setOnClickListener(v -> {
                final int mediaPos = media.indexOf(mediaItem);
                if (mediaPos >= 0) {
                    setCurrentItem(mediaPos, true);
                }
            });
        }

        @Override
        public int getItemCount() {
            return media.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            final ImageView thumbnailView;
            final ImageView typeIndicator;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                thumbnailView = itemView.findViewById(R.id.thumbnail);
                thumbnailView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.details_media_list_corner_radius));
                    }
                });
                thumbnailView.setClipToOutline(true);
                typeIndicator = itemView.findViewById(R.id.type_indicator);
            }

            void bind(Media media) {
                final Media displayMedia;
                if (viewModel.cropStates.containsKey(media.file)) {
                    displayMedia = new Media(0, media.type, null, ContentComposerViewModel.getCropFile(media.file), null, null, 0, 0, Media.TRANSFERRED_NO);
                } else {
                    displayMedia = media;
                }

                smallThumbnailLoader.load(thumbnailView, displayMedia, new ViewDataLoader.Displayer<ImageView, Bitmap>() {
                    @Override
                    public void showResult(@NonNull ImageView view, @Nullable Bitmap bitmap) {
                        if (bitmap != null) {
                            if (bitmap.getHeight() > Constants.MAX_IMAGE_ASPECT_RATIO * bitmap.getWidth()) {
                                final int padding = (int) ((bitmap.getHeight() - Constants.MAX_IMAGE_ASPECT_RATIO * bitmap.getWidth()) / 2);
                                final Rect bitmapRect = new Rect(0, padding, bitmap.getWidth(), bitmap.getHeight() - padding);
                                view.setImageDrawable(new ClippedBitmapDrawable(bitmap, bitmapRect));
                            } else {
                                view.setImageBitmap(bitmap);
                            }
                        } else {
                            showLoading(view);
                        }
                    }

                    @Override
                    public void showLoading(@NonNull ImageView view) {
                        if (media.width != 0 && media.height != 0) {
                            view.setImageDrawable(new PlaceholderDrawable(displayMedia.width, displayMedia.height, ContextCompat.getColor(getBaseContext(), R.color.media_placeholder)));
                        } else {
                            view.setImageDrawable(null);
                        }
                    }
                });
                if (media.type == Media.MEDIA_TYPE_VIDEO) {
                    typeIndicator.setImageResource(R.drawable.ic_video);
                    typeIndicator.setVisibility(View.VISIBLE);
                    thumbnailView.setContentDescription(getString(R.string.video));
                } else {
                    typeIndicator.setVisibility(View.GONE);
                    thumbnailView.setContentDescription(getString(R.string.photo));
                }
            }
        }
    }

    static class CleanupTmpFilesTask extends AsyncTask<Void, Void, Void> {

        private final List<Media> media;

        CleanupTmpFilesTask(@NonNull List<Media> media) {
            this.media = media;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (Media mediaItem : media) {
                if (!mediaItem.file.delete()) {
                    Log.e("failed to delete temporary file " + mediaItem.file.getAbsolutePath());
                }
                final File croppedFile = ContentComposerViewModel.getCropFile(mediaItem.file);
                if (croppedFile.exists()) {
                    if (!croppedFile.delete()) {
                        Log.e("failed to delete temporary file " + croppedFile.getAbsolutePath());
                    }
                }
            }
            return null;
        }
    }
}
