package com.halloapp.ui;

import android.graphics.Outline;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.util.Log;
import com.halloapp.util.TimeFormatter;
import com.halloapp.widget.CenterToast;
import com.halloapp.widget.LinearSpacingItemDecoration;

import java.util.List;

public class PostDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "post_id";

    private final SeenByAdapter adapter = new SeenByAdapter();
    private MediaThumbnailLoader mediaThumbnailLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PostDetailsActivity.onCreate");
        setContentView(R.layout.activity_post_details);

        /*
        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        */

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final RecyclerView seenByView = findViewById(R.id.seen_by_list);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        seenByView.setLayoutManager(layoutManager);
        seenByView.setAdapter(adapter);

        final String postId = Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_ID));

        final PostDetailsViewModel viewModel = new ViewModelProvider(this, new PostDetailsViewModel.Factory(getApplication(), postId)).get(PostDetailsViewModel.class);
        viewModel.contactsList.getLiveData().observe(this, adapter::setContacts);

        mediaThumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.details_media_list_height));

        viewModel.post.observe(this, this::showPost);
        viewModel.loadPost(postId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("PostDetailsActivity.onDestroy");
        mediaThumbnailLoader.destroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showPost(@NonNull Post post) {
        final RecyclerView mediaGallery = findViewById(R.id.media);
        if (post.media.isEmpty()) {
            mediaGallery.setVisibility(View.GONE);
        } else {
            final LinearLayoutManager layoutManager = new LinearLayoutManager(mediaGallery.getContext(), RecyclerView.HORIZONTAL, false);
            mediaGallery.setLayoutManager(layoutManager);
            mediaGallery.addItemDecoration(new LinearSpacingItemDecoration(layoutManager, getResources().getDimensionPixelSize(R.dimen.details_media_list_spacing)));
            mediaGallery.setAdapter(new MediaAdapter(post.media));
        }

        final TextView textView = findViewById(R.id.text);
        if (TextUtils.isEmpty(post.text)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(post.text);
        }

        final TextView timeView = findViewById(R.id.time);
        TimeFormatter.setTimeDiffText(timeView, System.currentTimeMillis() - post.timestamp);
    }

    private class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

        final List<Media> media;

        MediaAdapter(@NonNull List<Media> media) {
            this.media = media;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.details_media_list_corner_radius));
                }
            });
            imageView.setClipToOutline(true);
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final ImageView imageView = (ImageView)holder.itemView;
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            mediaThumbnailLoader.load(imageView, media.get(position));
        }

        @Override
        public int getItemCount() {
            return media.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

    private class SeenByAdapter extends RecyclerView.Adapter<SeenByAdapter.ViewHolder> {

        private List<Contact> contacts;

        public void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
            notifyDataSetChanged();
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.seen_by_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bindTo(contacts.get(position));
        }

        @Override
        public int getItemCount() {
            return contacts == null ? 0 : contacts.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            final ImageView avatarView;
            final TextView nameView;
            final View menuView;

            Contact contact;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                menuView = itemView.findViewById(R.id.menu);
                menuView.setOnClickListener(v -> {
                    final PopupMenu menu = new PopupMenu(menuView.getContext(), menuView);
                    getMenuInflater().inflate(R.menu.contact_menu, menu.getMenu());
                    menu.setOnMenuItemClickListener(item -> {
                        //noinspection SwitchStatementWithTooFewBranches
                        switch (item.getItemId()) {
                            case R.id.block: {
                                CenterToast.show(getBaseContext(), R.string.block); // TODO (ds): add contact blocking
                                return true;
                            }
                            default: {
                                return false;
                            }
                        }
                    });
                    menu.show();
                });
            }

            void bindTo(@NonNull Contact contact) {
                this.contact = contact;
                avatarView.setImageResource(R.drawable.avatar_person); // TODO (ds): load contact image
                nameView.setText(contact.getDisplayName());
            }
        }
    }
}
