package com.halloapp.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionManager;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Post;
import com.halloapp.emoji.EmojiKeyboardLayout;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.posts.SeenByLoader;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AvatarsLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MomentViewerActivity extends HalloActivity {

    private static final String EXTRA_MOMENT_POST_ID = "moment_post_id";

    public static Intent viewMoment(@NonNull Context context, @NonNull String postId) {
        Intent i = new Intent(context, MomentViewerActivity.class);
        i.putExtra(EXTRA_MOMENT_POST_ID, postId);

        return i;
    }

    private MomentViewerViewModel viewModel;

    private SeenByLoader seenByLoader;
    private AvatarLoader avatarLoader;
    private ContactLoader contactLoader;
    private MediaThumbnailLoader fullThumbnailLoader;

    private View cover;
    private ViewGroup content;
    private LinearLayout uploadingContainer;

    private EmojiKeyboardLayout emojiKeyboardLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_moment_viewer);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setTitle("");

        String postId = getIntent().getStringExtra(EXTRA_MOMENT_POST_ID);

        if (postId == null) {
            Log.e("MomentViewerActivity/onCreate null post id");
            finish();
            return;
        }

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        fullThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, point.x));
        contactLoader = new ContactLoader();
        seenByLoader = new SeenByLoader();
        avatarLoader = AvatarLoader.getInstance();

        viewModel = new ViewModelProvider(this, new MomentViewerViewModel.Factory(postId)).get(MomentViewerViewModel.class);

        SimpleDateFormat dayFormatter = new SimpleDateFormat("EEEE", Locale.getDefault());
        ImageView imageView = findViewById(R.id.image);
        final ImageView avatar = findViewById(R.id.avatar);
        final TextView name = findViewById(R.id.name);
        final TextView time = findViewById(R.id.time);
        TextView lineOne = findViewById(R.id.line_one);
        TextView lineTwo = findViewById(R.id.line_two);
        content = findViewById(R.id.content);
        AvatarsLayout avatarsLayout = findViewById(R.id.seen_indicator);
        avatarsLayout.setAvatarLoader(avatarLoader);
        avatarsLayout.setAvatarCount(3);
        avatarsLayout.setOnClickListener(v -> {
            final Intent intent = new Intent(MomentViewerActivity.this, PostSeenByActivity.class);
            Post post = viewModel.post.getLiveData().getValue();
            if (post != null) {
                intent.putExtra(PostSeenByActivity.EXTRA_POST_ID, post.id);
                startActivity(intent);
            } else {
                Log.i("MomentViewerActivity/seenOnClick null post");
            }
        });
        EditText textEntry = findViewById(R.id.entry);
        View textEntryContainer = findViewById(R.id.text_entry);
        View sendBtn = findViewById(R.id.bottom_composer_send);

        ImageView emojiBtn = findViewById(R.id.kb_toggle);
        emojiKeyboardLayout = findViewById(R.id.emoji_keyboard);
        emojiKeyboardLayout.bind(emojiBtn, textEntry);
        sendBtn.setEnabled(false);
        uploadingContainer = findViewById(R.id.uploading_container);
        textEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    sendBtn.setEnabled(false);
                } else {
                    sendBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ImageView uploadingMomentImageView = findViewById(R.id.uploading_moment_image);
        sendBtn.setOnClickListener(v -> {
            String text = textEntry.getText().toString();
            if (TextUtils.isEmpty(text)) {
                Log.i("MomentViewerActivity/sendMessage no text");
                return;
            }
            viewModel.sendMessage(textEntry.getText().toString());
            textEntry.setText("");
            Toast.makeText(sendBtn.getContext(), R.string.private_reply_sent, Toast.LENGTH_SHORT).show();
            KeyboardUtils.hideSoftKeyboard(textEntry);
        });
        cover = findViewById(R.id.moment_cover);
        viewModel.unlockingMoment.getLiveData().observe(this, unlockingMoment -> {
            updateViewUnlockState();
            if (unlockingMoment == null) {
                fullThumbnailLoader.cancel(imageView);
                return;
            }
            fullThumbnailLoader.load(uploadingMomentImageView, unlockingMoment.getMedia().get(0));
        });
        viewModel.post.getLiveData().observe(this, post -> {
            updateViewUnlockState();
            if (post != null) {
                fullThumbnailLoader.load(imageView, post.getMedia().get(0));
                avatarLoader.load(avatar, post.senderUserId, false);
                if (post.isIncoming()) {
                    contactLoader.load(name, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                        @Override
                        public void showResult(@NonNull TextView view, @Nullable Contact result) {
                            if (result != null) {
                                name.setText(result.getDisplayName());
                                textEntry.setHint(getString(R.string.reply_to_contact, result.getDisplayName()));
                            }
                        }

                        @Override
                        public void showLoading(@NonNull TextView view) {
                            name.setText("");
                        }
                    });
                } else {
                    name.setText(R.string.me);
                }
                time.setText(TimeFormatter.formatMessageTime(time.getContext(), post.timestamp));
                lineTwo.setText(TimeFormatter.formatMessageTime(lineOne.getContext(), post.timestamp));
                lineOne.setText(dayFormatter.format(new Date(post.timestamp)));
                viewModel.setLoaded();
                if (post.isIncoming()) {
                    textEntryContainer.setVisibility(View.VISIBLE);
                    avatarsLayout.setVisibility(View.INVISIBLE);
                } else {
                    textEntryContainer.setVisibility(View.INVISIBLE);
                    avatarsLayout.setVisibility(View.VISIBLE);
                    avatarsLayout.setAvatarCount(Math.min(post.seenByCount, 3));
                    seenByLoader.load(avatarsLayout, postId);
                }
            } else {
                fullThumbnailLoader.cancel(imageView);
                avatarLoader.cancel(avatar);
                contactLoader.cancel(name);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (emojiKeyboardLayout.isEmojiKeyboardOpen()) {
            emojiKeyboardLayout.hideEmojiKeyboard();
            return;
        }
        super.onBackPressed();
    }

    private void updateViewUnlockState() {
        Post post = viewModel.post.getLiveData().getValue();
        Post unlockingPost = viewModel.unlockingMoment.getLiveData().getValue();

        boolean unlocked = false;
        if (post != null && post.isOutgoing()) {
            unlocked = true;
        } else if (unlockingPost != null && unlockingPost.transferred == Post.TRANSFERRED_YES) {
            unlocked = true;
        }
        if (unlocked) {
            if (cover.isLaidOut()) {
                TransitionManager.beginDelayedTransition(content);
            }
            cover.setVisibility(View.GONE);
            viewModel.setUncovered();
            uploadingContainer.setVisibility(View.GONE);
        } else {
            if (uploadingContainer.isLaidOut()) {
                TransitionManager.beginDelayedTransition(uploadingContainer);
            }
            uploadingContainer.setVisibility(View.VISIBLE);
            cover.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contactLoader.destroy();
        seenByLoader.destroy();
    }
}
