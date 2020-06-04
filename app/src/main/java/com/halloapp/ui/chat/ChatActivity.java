package com.halloapp.ui.chat;

import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.LongSparseArray;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.Debug;
import com.halloapp.ForegroundChat;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.UserId;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.ContentComposerActivity;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_REPLY_POST_ID = "reply_post_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";

    private final ChatAdapter adapter = new ChatAdapter();
    private ChatViewModel viewModel;

    private PostEditText editText;
    private View replyContainer;

    private String chatId;

    private MediaThumbnailLoader mediaThumbnailLoader;
    private ContactLoader contactLoader;
    private AvatarLoader avatarLoader;
    private ReplyLoader replyLoader;
    private TimestampRefresher timestampRefresher;

    private String replyPostId;
    private int replyPostMediaIndex;

    private DrawDelegateView drawDelegateView;
    private final Stack<View> recycledMediaViews = new Stack<>();

    private boolean scrollUpOnDataLoaded;
    private boolean scrollToNewMessageOnDataLoaded = true;

    private final LongSparseArray<Integer> mediaPagerPositionMap = new LongSparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ChatActivity.onCreate");

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
        replyLoader = new ReplyLoader(this, getResources().getDimensionPixelSize(R.dimen.reply_thumb_size));
        avatarLoader = AvatarLoader.getInstance(Connection.getInstance(), this);
        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> adapter.notifyDataSetChanged());

        chatId = getIntent().getStringExtra(EXTRA_CHAT_ID);

        editText = findViewById(R.id.entry);
        editText.setMediaInputListener(uri -> startActivity(new Intent(getBaseContext(), ContentComposerActivity.class)
                .putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(Collections.singleton(uri)))
                .putExtra(ContentComposerActivity.EXTRA_CHAT_ID, chatId)));
        drawDelegateView = findViewById(R.id.draw_delegate);

        final RecyclerView chatView = findViewById(R.id.chat);
        Preconditions.checkNotNull((SimpleItemAnimator)chatView.getItemAnimator()).setSupportsChangeAnimations(false);

        findViewById(R.id.send).setOnClickListener(v -> sendMessage());
        findViewById(R.id.media).setOnClickListener(v -> pickMedia());

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(false);
        chatView.setLayoutManager(layoutManager);
        chatView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);

        chatView.setAdapter(adapter);

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

        viewModel = new ViewModelProvider(this,
                new ChatViewModel.Factory(getApplication(), chatId, replyPostId)).get(ChatViewModel.class);

        final View newMessagesView = findViewById(R.id.new_messages);
        newMessagesView.setOnClickListener(v -> {
            scrollUpOnDataLoaded = true;
            viewModel.reloadMessagesAt(Long.MAX_VALUE);
        });

        final Runnable newListCommitCallback = new Runnable() {

            int outgoingAdded;
            int incomingAdded;

            @Override
            public void run() {
                final int outgoingAdded = viewModel.getOutgoingAdded();
                final int incomingAdded = viewModel.getIncomingAdded();
                Log.i("ChatActivity: message list changed," +
                        " outgoingAdded=" + outgoingAdded +
                        " incomingAdded=" + incomingAdded +
                        " scrollToNewMessageOnDataLoaded=" + scrollToNewMessageOnDataLoaded +
                        " scrollUpOnDataLoaded=" + scrollUpOnDataLoaded);

                final boolean newOutgoingMessage = outgoingAdded > this.outgoingAdded;
                final boolean newIncomingMessage = incomingAdded > this.incomingAdded;

                this.outgoingAdded = outgoingAdded;
                this.incomingAdded = incomingAdded;

                adapter.setNewMessageCount(outgoingAdded == 0 ? viewModel.getInitialUnseen() + incomingAdded : 0);

                if (newOutgoingMessage || scrollUpOnDataLoaded) {
                    Log.i("ChatActivity: scroll up");
                    scrollUpOnDataLoaded = false;
                    chatView.scrollToPosition(0);
                    newMessagesView.setVisibility(View.GONE);
                } else if (newIncomingMessage) {
                    final View childView = layoutManager.getChildAt(0);
                    final boolean scrolled = childView == null || layoutManager.getPosition(childView) != 0;
                    if (scrolled) {
                        Log.i("ChatActivity: show 'new messages view'");
                        if (newMessagesView.getVisibility() != View.VISIBLE) {
                            newMessagesView.setVisibility(View.VISIBLE);
                            newMessagesView.setTranslationY(getResources().getDimension(R.dimen.details_media_list_height));
                            newMessagesView.animate().setDuration(200).translationY(0).start();
                        }
                    } else {
                        Log.i("ChatActivity: scroll up on new incoming message");
                        scrollUpOnDataLoaded = false;
                        chatView.scrollToPosition(0);
                        newMessagesView.setVisibility(View.GONE);
                    }
                    adapter.notifyDataSetChanged();
                } else if (scrollToNewMessageOnDataLoaded && adapter.newMessageCount > 0) {
                    Log.i("ChatActivity: scroll to new message, position=" + adapter.newMessageCount + " offset=" + (chatView.getHeight() * 9 / 10));
                    if (chatView.getHeight() > 0) {
                        layoutManager.scrollToPositionWithOffset(adapter.newMessageCount, chatView.getHeight() * 9 / 10);
                    } else {
                        chatView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                chatView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                Log.i("ChatActivity: scroll to new message after layout done, position=" + adapter.newMessageCount + " offset=" + (chatView.getHeight() * 9 / 10));
                                layoutManager.scrollToPositionWithOffset(adapter.newMessageCount, chatView.getHeight() * 9 / 10);
                            }
                        });
                    }
                    scrollToNewMessageOnDataLoaded = false;
                }
            }
        };

        viewModel.messageList.observe(this, messages -> adapter.submitList(messages, newListCommitCallback));
        viewModel.chat.getLiveData().observe(this, chat -> {
            Log.i("ChatActivity: chat changed, newMessageCount=" + (chat == null ? "null" : chat.newMessageCount));
            if (chat != null) {
                adapter.setFirstUnseenMessageRowId(chat.firstUnseenMessageRowId);
                adapter.setNewMessageCount(chat.newMessageCount);
            }
            ContentDb.getInstance(this).setChatSeen(chatId);
        });
        viewModel.contact.getLiveData().observe(this, contact -> {
            setTitle(contact.getDisplayName());
            if (replyPostId != null) {
                final TextView replyNameView = findViewById(R.id.reply_name);
                replyNameView.setText(contact.getDisplayName());
            }
            PresenceLoader presenceLoader = PresenceLoader.getInstance(Connection.getInstance());
            presenceLoader.getLastSeenLiveData(contact.userId).observe(this, presenceState -> {
                if (presenceState == null || presenceState.state == PresenceLoader.PresenceState.PRESENCE_STATE_UNKNOWN) {
                    toolbar.setSubtitle(null);
                } else if (presenceState.state == PresenceLoader.PresenceState.PRESENCE_STATE_ONLINE) {
                    toolbar.setSubtitle(getString(R.string.online));
                } else if (presenceState.state == PresenceLoader.PresenceState.PRESENCE_STATE_OFFLINE) {
                    toolbar.setSubtitle(TimeFormatter.formatLastSeen(this, presenceState.lastSeen));
                }
            });
        });
        viewModel.deleted.observe(this, deleted -> {
            if (deleted != null && deleted) {
                finish();
            }
        });
        replyContainer = findViewById(R.id.reply_container);
        if (viewModel.replyPost != null) {
            viewModel.replyPost.getLiveData().observe(this, this::updatePostReply);
        } else {
            replyContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("ChatActivity.onDestroy");
        mediaThumbnailLoader.destroy();
        contactLoader.destroy();
        replyLoader.destroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("ChatActivity.onStart " + chatId);
        ForegroundChat.getInstance().setForegroundChatId(chatId);
        Notifications.getInstance(this).clearMessageNotifications(chatId);
        if (adapter.firstUnseenMessageRowId >= 0) {
            Log.i("ChatActivity.onStart mark " + chatId + " as seen");
            ContentDb.getInstance(this).setChatSeen(chatId);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("ChatActivity.onStop");
        scrollToNewMessageOnDataLoaded = true;
        viewModel.chat.invalidate();
        ForegroundChat.getInstance().setForegroundChatId(null);
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (BuildConfig.DEBUG && keyCode == KeyEvent.KEYCODE_BACK) {
            final UserId userId = new UserId(chatId);
            Debug.showDebugMenu(this, editText, userId);
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
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
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getBaseContext().getString(R.string.delete_chat_confirmation));
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.yes, (dialog, which) -> ContentDb.getInstance(this).deleteChat(chatId));
                builder.setNegativeButton(R.string.no, null);
                builder.show();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void updatePostReply(@Nullable Post post) {
        if (post != null) {
            replyContainer.setVisibility(View.VISIBLE);
            final TextView replyTextView = findViewById(R.id.reply_text);
            replyTextView.setText(post.text);
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
                mediaThumbnailLoader.load(replyMediaThumbView, media);
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
        } else {
            replyContainer.setVisibility(View.GONE);
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
                messageText,
                replyPostId,
                replyPostMediaIndex);
        replyPostId = null;
        replyPostMediaIndex = -1;
        replyContainer.setVisibility(View.GONE);
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

        long firstUnseenMessageRowId = -1L;
        int newMessageCount;

        ChatAdapter() {
            super(DIFF_CALLBACK);
            setHasStableIds(true);
        }

        void setFirstUnseenMessageRowId(long firstUnseenMessageRowId) {
            if (this.firstUnseenMessageRowId != firstUnseenMessageRowId) {
                this.firstUnseenMessageRowId = firstUnseenMessageRowId;
                notifyDataSetChanged();
            }
        }

        void setNewMessageCount(int newMessageCount) {
            if (this.newMessageCount != newMessageCount) {
                this.newMessageCount = newMessageCount;
                notifyDataSetChanged();
            }
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
            holder.bindTo(message, firstUnseenMessageRowId == message.rowId ? newMessageCount : 0, position < getItemCount() - 1 ? getItem(position+1) : null);
        }
    }

    private final MessageViewHolder.MessageViewHolderParent messageViewHolderParent = new MessageViewHolder.MessageViewHolderParent() {
        private final LongSparseArray<Integer> textLimits = new LongSparseArray<>();

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
        public void startActivity(@NonNull Intent intent) {
            ChatActivity.this.startActivity(intent);
        }

        @Override
        public void startActivity(@NonNull Intent intent, @NonNull ActivityOptionsCompat options) {
            ChatActivity.this.startActivity(intent, options.toBundle());
        }

        @Override
        ReplyLoader getReplyLoader() {
            return replyLoader;
        }

        @Override
        public LongSparseArray<Integer> getTextLimits() {
            return textLimits;
        }
    };
}
