package com.halloapp.ui.chat;

import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.LongSparseArray;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.ForegroundChat;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Message;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.TimestampRefresher;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.posts.SeenByLoader;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.PostEditText;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.PresenceLoader;

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

    private boolean scrollUpOnDataLoaded;

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

        final View newMessagesView = findViewById(R.id.new_messages);
        newMessagesView.setOnClickListener(v -> {
            scrollUpOnDataLoaded = true;
            viewModel.reloadMessagesAt(Long.MAX_VALUE);
        });

        viewModel.messageList.observe(this, messages -> adapter.submitList(messages, () -> {
            if (viewModel.checkPendingOutgoing() || scrollUpOnDataLoaded) {
                scrollUpOnDataLoaded = false;
                chatView.scrollToPosition(0);
                newMessagesView.setVisibility(View.GONE);
            } else if (viewModel.checkPendingIncoming()) {
                final View childView = layoutManager.getChildAt(0);
                final boolean scrolled = childView == null || layoutManager.getPosition(childView) != 0;
                if (scrolled) {
                    if (newMessagesView.getVisibility() != View.VISIBLE) {
                        newMessagesView.setVisibility(View.VISIBLE);
                        newMessagesView.setTranslationY(getResources().getDimension(R.dimen.details_media_list_height));
                        newMessagesView.animate().setDuration(200).translationY(0).start();
                    }
                } else {
                    scrollUpOnDataLoaded = false;
                    chatView.scrollToPosition(0);
                    newMessagesView.setVisibility(View.GONE);
                }
            }
        }));
        viewModel.chat.getLiveData().observe(this, chat -> {
            adapter.setChat(chat);
            ContentDb.getInstance(this).setChatSeen(chatId);
        });

        viewModel.contact.getLiveData().observe(this, contact -> {
            setTitle(contact.getDisplayName());
            PresenceLoader presenceLoader = PresenceLoader.getInstance(Connection.getInstance());
            presenceLoader.getLastSeenLiveData(contact.userId).observe(this, lastSeen -> {
                // TODO(jack): set last seen in UI
            });
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ChatActivity.onDestroy");
        mediaThumbnailLoader.destroy();
        contactLoader.destroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("ChatActivity.onStart");
        ForegroundChat.getInstance().setForegroundChatId(chatId);
        Notifications.getInstance(this).clearMessageNotifications(chatId);
        if (adapter.chat != null) {
            ContentDb.getInstance(this).setChatSeen(chatId);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("ChatActivity.onStop");
        ForegroundChat.getInstance().setForegroundChatId(null);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.delete: {
                ContentDb.getInstance(this).deleteChat(chatId);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void sendMessage() {
        final String messageText = StringUtils.preparePostText(Preconditions.checkNotNull(editText.getText()).toString());
        if (TextUtils.isEmpty(messageText)) {
            Log.w("ChatActivity: cannot send empty message");
            return;
        }
        editText.setText(null);
        final Message message = new Message(0,
                chatId,
                UserId.ME,
                RandomId.create(),
                System.currentTimeMillis(),
                Message.TRANSFERRED_NO,
                Message.SEEN_NO,
                messageText);
        message.addToStorage(ContentDb.getInstance(this));
    }

    private void pickMedia() {
        // TODO (ds): uncomment when implemented
        final Intent intent = new Intent(this, MediaPickerActivity.class);
        intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_SEND);
        intent.putExtra(MediaPickerActivity.EXTRA_CHAT_ID, chatId);
        startActivity(intent);
    }

    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<Message>() {

        @Override
        public boolean areItemsTheSame(Message oldItem, Message newItem) {
            return oldItem.rowId == newItem.rowId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
            return oldItem.equals(newItem);
        }
    };

    private class ChatAdapter extends PagedListAdapter<Message, MessageViewHolder> {

        static final int MESSAGE_TYPE_TEXT = 0x00;
        static final int MESSAGE_TYPE_MEDIA = 0x01;
        static final int MESSAGE_TYPE_RETRACTED = 0x02;
        static final int MESSAGE_TYPE_MASK = 0xFF;

        static final int MESSAGE_DIRECTION_OUTGOING = 0x0000;
        static final int MESSAGE_DIRECTION_INCOMING = 0x0100;
        static final int MESSAGE_DIRECTION_MASK = 0xFF00;

        Chat chat;

        ChatAdapter() {
            super(DIFF_CALLBACK);
            setHasStableIds(true);
        }

        public void setChat(Chat chat) {
            this.chat = chat;
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return Preconditions.checkNotNull(getItem(position)).rowId;
        }

        @Override
        public int getItemViewType(int position) {
            final Message message = Preconditions.checkNotNull(getItem(position));
            return (message.isRetracted() ? MESSAGE_TYPE_RETRACTED : (message.media.isEmpty() ? MESSAGE_TYPE_TEXT : MESSAGE_TYPE_MEDIA)) |
                    (message.isOutgoing() ? MESSAGE_DIRECTION_OUTGOING : MESSAGE_DIRECTION_INCOMING);
        }

        @Override
        public @NonNull
        MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

            return new MessageViewHolder(layout, messageViewHolderParent);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            holder.bindTo(Preconditions.checkNotNull(getItem(position)), chat, position < getItemCount() - 1 ? getItem(position+1) : null);
        }
    }

    private final MessageViewHolder.MessageViewHolderParent messageViewHolderParent = new MessageViewHolder.MessageViewHolderParent() {

        @Override
        public AvatarLoader getAvatarLoader() {
            return avatarLoader;
        }

        @Override
        public ContactLoader getContactLoader() {
            return contactLoader;
        }

        @Override
        public SeenByLoader getSeenByLoader() {
            Preconditions.checkState(false, "messageViewHolderParent.getSeenByLoader call not expected");
            return null;
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
