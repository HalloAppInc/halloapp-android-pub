package com.halloapp.ui.chat;

import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.LongSparseArray;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

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
import com.halloapp.util.TimeFormatter;
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
        Preconditions.checkNotNull((SimpleItemAnimator)chatView.getItemAnimator()).setSupportsChangeAnimations(false);

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
            final boolean newOutgoingMessage = viewModel.checkPendingOutgoing();
            final int newIncomingMessage = viewModel.checkPendingIncoming();
            if (newOutgoingMessage || scrollUpOnDataLoaded) {
                scrollUpOnDataLoaded = false;
                chatView.scrollToPosition(0);
                newMessagesView.setVisibility(View.GONE);
                if (newOutgoingMessage && adapter.newMessageCount > 0) {
                    adapter.newMessageCount = 0;
                    adapter.notifyDataSetChanged();
                }
            } else if (newIncomingMessage > 0) {
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
                if (adapter.chat != null && adapter.newMessageCount > 0) {
                    adapter.newMessageCount += newIncomingMessage;
                    adapter.notifyDataSetChanged();
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
                if (lastSeen == null) {
                    toolbar.setSubtitle(null);
                } else {
                    toolbar.setSubtitle(TimeFormatter.formatLastSeen(this, lastSeen * 1000L));
                }
            });
        });
        viewModel.deleted.observe(this, deleted -> {
            if (deleted != null && deleted) {
                finish();
            }
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
                Message.STATE_INITIAL,
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

        static final int VIEW_TYPE_OUTGOING_TEXT = 1;
        static final int VIEW_TYPE_INCOMING_TEXT = 2;
        static final int VIEW_TYPE_OUTGOING_MEDIA = 3;
        static final int VIEW_TYPE_OUTGOING_MEDIA_NO_CAPTION = 4;
        static final int VIEW_TYPE_INCOMING_MEDIA = 5;
        static final int VIEW_TYPE_OUTGOING_RETRACTED = 6;
        static final int VIEW_TYPE_INCOMING_RETRACTED = 7;

        @Nullable Chat chat;
        int newMessageCount;

        ChatAdapter() {
            super(DIFF_CALLBACK);
            setHasStableIds(true);
        }

        public void setChat(@Nullable Chat chat) {
            this.chat = chat;
            if (chat != null) {
                this.newMessageCount = chat.newMessageCount;
            } else {
                this.newMessageCount = 0;
            }
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return Preconditions.checkNotNull(getItem(position)).rowId;
        }

        @Override
        public int getItemViewType(int position) {
            final Message message = Preconditions.checkNotNull(getItem(position));
            if (message.isIncoming()) {
                if (message.isRetracted()) {
                    return VIEW_TYPE_INCOMING_RETRACTED;
                } else if (message.media.isEmpty()) {
                    return VIEW_TYPE_INCOMING_TEXT;
                } else {
                    return VIEW_TYPE_INCOMING_MEDIA;
                }
            } else {
                if (message.isRetracted()) {
                    return VIEW_TYPE_OUTGOING_RETRACTED;
                } else if (message.media.isEmpty()) {
                    return VIEW_TYPE_OUTGOING_TEXT;
                } else if (TextUtils.isEmpty(message.text)) {
                    return VIEW_TYPE_OUTGOING_MEDIA_NO_CAPTION;
                } else {
                    return VIEW_TYPE_OUTGOING_MEDIA;
                }
            }
        }

        @Override
        public @NonNull
        MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final @LayoutRes int layoutRes;
            switch (viewType) {
                case VIEW_TYPE_INCOMING_TEXT: {
                    layoutRes = R.layout.message_item_incoming_text;
                    break;
                }
                case VIEW_TYPE_OUTGOING_TEXT: {
                    layoutRes = R.layout.message_item_outgoing_text;
                    break;
                }
                case VIEW_TYPE_INCOMING_MEDIA: {
                    layoutRes = R.layout.message_item_incoming_media;
                    break;
                }
                case VIEW_TYPE_OUTGOING_MEDIA: {
                    layoutRes = R.layout.message_item_outgoing_media;
                    break;
                }
                case VIEW_TYPE_OUTGOING_MEDIA_NO_CAPTION: {
                    layoutRes = R.layout.message_item_outgoing_media_no_caption;
                    break;
                }
                case VIEW_TYPE_INCOMING_RETRACTED: {
                    layoutRes = R.layout.message_item_incoming_retracted;
                    break;
                }
                case VIEW_TYPE_OUTGOING_RETRACTED: {
                    layoutRes = R.layout.message_item_outgoing_retracted;
                    break;
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }
            final View layout = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
            return new MessageViewHolder(layout, messageViewHolderParent);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            final Message message = Preconditions.checkNotNull(getItem(position));
            holder.bindTo(message, (chat != null && chat.firstUnseenMessageRowId == message.rowId) ? newMessageCount : -1, position < getItemCount() - 1 ? getItem(position+1) : null);
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
