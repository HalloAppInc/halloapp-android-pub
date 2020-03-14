package com.halloapp.ui.chat;

import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.LongSparseArray;
import androidx.core.util.Preconditions;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.UserId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.posts.ChatMessage;
import com.halloapp.posts.Post;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.TimestampRefresher;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.PostEditText;
import com.halloapp.xmpp.Connection;

import java.util.Stack;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CHAT_ID = "chat_id";

    private final ChatAdapter adapter = new ChatAdapter();

    private PostEditText editText;

    private String chatId;

    private MediaThumbnailLoader mediaThumbnailLoader;
    private ContactLoader contactLoader;
    private AvatarLoader avatarLoader;
    private TimestampRefresher timestampRefresher;

    private DrawDelegateView drawDelegateView;
    private final Stack<View> recycledMediaViews = new Stack<>();

    private final LongSparseArray<Integer> mediaPagerPositionMap = new LongSparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ChatActivity.onCreate");

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_chat);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        contactLoader = new ContactLoader(this);
        avatarLoader = AvatarLoader.getInstance(Connection.getInstance(), this);
        //ContactsDb.getInstance(this).addObserver(contactsObserver);
        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> adapter.notifyDataSetChanged());

        chatId = getIntent().getStringExtra(EXTRA_CHAT_ID);

        editText = findViewById(R.id.entry);
        drawDelegateView = findViewById(R.id.draw_delegate);

        final RecyclerView chatView = findViewById(R.id.chat);

        findViewById(R.id.send).setOnClickListener(v -> sendMessage());
        findViewById(R.id.media).setOnClickListener(v -> pickMedia());

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(false);
        chatView.setLayoutManager(layoutManager);

        chatView.setAdapter(adapter);

        final ChatViewModel viewModel = new ViewModelProvider(this, new ChatViewModel.Factory(getApplication(), chatId)).get(ChatViewModel.class);
        viewModel.messageList.observe(this, adapter::submitList);

        viewModel.contact.getLiveData().observe(this, contact -> setTitle(contact.getDisplayName()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ChatActivity.onDestroy");
        mediaThumbnailLoader.destroy();
        contactLoader.destroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void sendMessage() {
        final String postText = StringUtils.preparePostText(Preconditions.checkNotNull(editText.getText()).toString());
        ChatMessage message = new ChatMessage(0,
                chatId,
                UserId.ME,
                RandomId.create(),
                System.currentTimeMillis(),
                false,
                Post.POST_SEEN_YES,
                postText);
        Connection.getInstance().sendMessage(message);
    }

    private void pickMedia() {
        final Intent intent = new Intent(this, MediaPickerActivity.class);
        intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_POST);
        startActivity(intent);
    }

    private static final DiffUtil.ItemCallback<ChatMessage> DIFF_CALLBACK = new DiffUtil.ItemCallback<ChatMessage>() {

        @Override
        public boolean areItemsTheSame(ChatMessage oldItem, ChatMessage newItem) {
            return oldItem.rowId == newItem.rowId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChatMessage oldItem, @NonNull ChatMessage newItem) {
            return oldItem.equals(newItem);
        }
    };

    private class ChatAdapter extends PagedListAdapter<ChatMessage, ChatMessageViewHolder> {

        static final int MESSAGE_TYPE_TEXT = 0x00;
        static final int MESSAGE_TYPE_MEDIA = 0x01;
        static final int MESSAGE_TYPE_RETRACTED = 0x02;
        static final int MESSAGE_TYPE_MASK = 0xFF;

        static final int MESSAGE_DIRECTION_OUTGOING = 0x0000;
        static final int MESSAGE_DIRECTION_INCOMING = 0x0100;
        static final int MESSAGE_DIRECTION_MASK = 0xFF00;

        ChatAdapter() {
            super(DIFF_CALLBACK);
            setHasStableIds(true);
        }

        public long getItemId(int position) {
            return Preconditions.checkNotNull(getItem(position)).rowId;
        }

        @Override
        public int getItemViewType(int position) {
            final ChatMessage message = Preconditions.checkNotNull(getItem(position));
            return (message.isRetracted() ? MESSAGE_TYPE_RETRACTED : (message.media.isEmpty() ? MESSAGE_TYPE_TEXT : MESSAGE_TYPE_MEDIA)) |
                    (message.isOutgoing() ? MESSAGE_DIRECTION_OUTGOING : MESSAGE_DIRECTION_INCOMING);
        }

        @Override
        public @NonNull ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final @LayoutRes int layoutRes;
            switch (viewType & MESSAGE_DIRECTION_MASK) {
                case MESSAGE_DIRECTION_INCOMING: {
                    layoutRes = R.layout.message_item_incoming;
                    break;
                }
                case MESSAGE_DIRECTION_OUTGOING: {
                    layoutRes = R.layout.message_item_outgoing;
                    break;
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }
            final View layout = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
            final @LayoutRes int contentLayoutRes;
            switch (viewType & MESSAGE_TYPE_MASK) {
                case MESSAGE_TYPE_TEXT: {
                    contentLayoutRes = R.layout.message_content_text;
                    break;
                }
                case MESSAGE_TYPE_MEDIA: {
                    contentLayoutRes = R.layout.message_content_media;
                    break;
                }
                case MESSAGE_TYPE_RETRACTED: {
                    contentLayoutRes = R.layout.message_content_retracted;
                    break;
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }
            final ViewGroup contentHolder = layout.findViewById(R.id.content);
            LayoutInflater.from(parent.getContext()).inflate(contentLayoutRes, contentHolder, true);

            return new ChatMessageViewHolder(layout, chatMessageViewHolderParent);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
            holder.bindTo(Preconditions.checkNotNull(getItem(position)));
        }
    }

    private final ChatMessageViewHolder.ChatMessageViewHolderParent chatMessageViewHolderParent = new ChatMessageViewHolder.ChatMessageViewHolderParent() {

        @Override
        public AvatarLoader getAvatarLoader() {
            return avatarLoader;
        }

        @Override
        public ContactLoader getContactLoader() {
            return contactLoader;
        }

        @Override
        public DrawDelegateView getDrawDelegateView() {
            return drawDelegateView;
        }

        @Override
        public MediaThumbnailLoader getMediaThumbnailLoader() {
            return mediaThumbnailLoader;
        }

        @Override
        public LongSparseArray<Integer> getMediaPagerPositionMap() {
            return mediaPagerPositionMap;
        }

        @Override
        public Stack<View> getRecycledMediaViews() {
            return recycledMediaViews;
        }

        @Override
        public TimestampRefresher getTimestampRefresher() {
            return timestampRefresher;
        }

        @Override
        public void startActivity(Intent intent) {
            ChatActivity.this.startActivity(intent);
        }
    };

}
