package com.halloapp.ui.chat;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.LongSparseArray;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
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
import com.halloapp.Me;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.TimestampRefresher;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.groups.GroupInfoActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.posts.SeenByLoader;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.RandomId;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.NestedHorizontalScrollHelper;
import com.halloapp.widget.PostEditText;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.PresenceLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends HalloActivity {

    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_REPLY_POST_SENDER_ID = "reply_post_sender_id";
    public static final String EXTRA_REPLY_POST_ID = "reply_post_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";
    public static final String EXTRA_SELECTED_MESSAGE_ROW_ID = "selected_message_row_id";
    public static final String EXTRA_COPY_TEXT = "copy_text";

    private static final int REQUEST_CODE_COMPOSE = 1;
    private static final int REQUEST_CODE_VIEW_GROUP_INFO = 2;

    public static Map<ChatId, String> messageDrafts = new HashMap<>();

    private final ChatAdapter adapter = new ChatAdapter();
    private ChatViewModel viewModel;

    private PostEditText editText;
    private View replyContainer;

    private TextView titleView;
    private TextView subtitleView;
    private TextView replyNameView;
    private ImageView avatarView;
    private View footer;

    private ChatId chatId;

    private Me me;
    private AvatarLoader avatarLoader;
    private PresenceLoader presenceLoader;

    private MediaThumbnailLoader mediaThumbnailLoader;
    private ContactLoader contactLoader;
    private ReplyLoader replyLoader;
    private TextContentLoader textContentLoader;
    private TimestampRefresher timestampRefresher;
    private ActionMode actionMode;

    private String replyPostId;
    private UserId replySenderId;
    private long replyMessageRowId = -1;
    private Message replyMessage;
    private int replyPostMediaIndex;
    private int replyMessageMediaIndex = 0;
    private long selectedMessageRowId = -1;
    private String copyText;
    private boolean blocked;
    private String chatName;

    private DrawDelegateView drawDelegateView;
    private final RecyclerView.RecycledViewPool recycledMediaViews = new RecyclerView.RecycledViewPool();

    private boolean scrollUpOnDataLoaded;
    private boolean scrollToNewMessageOnDataLoaded = true;
    private final LongSparseArray<Integer> mediaPagerPositionMap = new LongSparseArray<>();

    private MenuItem menuItem;
    private Map<Long, Integer> replyMessageMediaIndexMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_chat);

        titleView = findViewById(R.id.title);
        subtitleView = findViewById(R.id.subtitle);
        avatarView = findViewById(R.id.avatar);
        footer = findViewById(R.id.footer);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        contactLoader = new ContactLoader(this);
        replyLoader = new ReplyLoader(this, getResources().getDimensionPixelSize(R.dimen.reply_thumb_size));
        me = Me.getInstance();
        avatarLoader = AvatarLoader.getInstance();
        presenceLoader = PresenceLoader.getInstance();
        textContentLoader = new TextContentLoader(this);
        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> adapter.notifyDataSetChanged());

        chatId = getIntent().getParcelableExtra(EXTRA_CHAT_ID);
        Log.d("ChatActivity chatId " + chatId);

        final ImageView sendButton = findViewById(R.id.send);

        editText = findViewById(R.id.entry);
        editText.setText(ChatActivity.messageDrafts.get(chatId));
        editText.setMediaInputListener(uri -> startActivity(new Intent(getBaseContext(), ContentComposerActivity.class)
                .putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(Collections.singleton(uri)))
                .putExtra(ContentComposerActivity.EXTRA_CHAT_ID, chatId)));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    messageDrafts.remove(chatId);
                    sendButton.clearColorFilter();
                } else {
                    messageDrafts.put(chatId, s.toString());
                    sendButton.setColorFilter(ContextCompat.getColor(ChatActivity.this, R.color.color_secondary));
                }
            }
        });
        drawDelegateView = findViewById(R.id.draw_delegate);

        replyNameView = findViewById(R.id.reply_name);

        View toolbarTitleContainer = findViewById(R.id.toolbar_text_container);
        toolbarTitleContainer.setOnClickListener(v -> {
            if (chatId instanceof UserId) {
                Intent viewProfile = ViewProfileActivity.viewProfile(this, (UserId)chatId);
                startActivity(viewProfile);
            } else if (chatId instanceof GroupId) {
                startActivityForResult(GroupInfoActivity.viewGroup(this, (GroupId)chatId), REQUEST_CODE_VIEW_GROUP_INFO);
            }
        });

        final RecyclerView chatView = findViewById(R.id.chat);
        Preconditions.checkNotNull((SimpleItemAnimator) chatView.getItemAnimator()).setSupportsChangeAnimations(false);

        sendButton.setOnClickListener(v -> sendMessage());
        findViewById(R.id.media).setOnClickListener(v -> pickMedia());

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(false);
        chatView.setLayoutManager(layoutManager);
        chatView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        NestedHorizontalScrollHelper.applyDefaultScrollRatio(chatView);

        chatView.setAdapter(adapter);

        if (savedInstanceState == null) {
            replySenderId = getIntent().getParcelableExtra(EXTRA_REPLY_POST_SENDER_ID);
            replyPostId = getIntent().getStringExtra(EXTRA_REPLY_POST_ID);
            replyPostMediaIndex = getIntent().getIntExtra(EXTRA_REPLY_POST_MEDIA_INDEX, -1);
        } else {
            replySenderId = savedInstanceState.getParcelable(EXTRA_REPLY_POST_SENDER_ID);
            replyPostId = savedInstanceState.getString(EXTRA_REPLY_POST_ID);
            replyPostMediaIndex = savedInstanceState.getInt(EXTRA_REPLY_POST_MEDIA_INDEX, -1);
            selectedMessageRowId = savedInstanceState.getLong(EXTRA_SELECTED_MESSAGE_ROW_ID, selectedMessageRowId);
            copyText = savedInstanceState.getString(EXTRA_COPY_TEXT);
            if (selectedMessageRowId != -1) {
                updateActionMode(copyText);
            }
        }
        if (replyPostId != null) {
            editText.requestFocus();
        }

        viewModel = new ViewModelProvider(this,
                new ChatViewModel.Factory(getApplication(), chatId, replySenderId, replyPostId)).get(ChatViewModel.class);

        final View newMessagesView = findViewById(R.id.new_messages);
        newMessagesView.setOnClickListener(v -> {
            scrollUpOnDataLoaded = true;
            viewModel.reloadMessagesAt(Long.MAX_VALUE);
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.onComposingMessage();
            }
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
                    adapter.notifyDataSetChanged();
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
                footer.setVisibility(chat.isActive ? View.VISIBLE : View.GONE);
            }

            if (chatId instanceof UserId) {
                viewModel.name.getLiveData().observe(this, name -> {
                    chatName = name;
                    setTitle(name);
                });
                presenceLoader.getLastSeenLiveData((UserId)chatId).observe(this, presenceState -> {
                    if (presenceState == null || presenceState.state == PresenceLoader.PresenceState.PRESENCE_STATE_UNKNOWN) {
                        setSubtitle(null);
                    } else if (presenceState.state == PresenceLoader.PresenceState.PRESENCE_STATE_ONLINE) {
                        setSubtitle(getString(R.string.online));
                    } else if (presenceState.state == PresenceLoader.PresenceState.PRESENCE_STATE_OFFLINE) {
                        setSubtitle(TimeFormatter.formatLastSeen(this, presenceState.lastSeen));
                    } else if (presenceState.state == PresenceLoader.PresenceState.PRESENCE_STATE_TYPING) {
                        setSubtitle(getString(R.string.user_typing));
                    }
                });
            } else if (chatId instanceof GroupId) {
                if (chat != null) {
                    chatName = chat.name;
                    setTitle(chat.name);
                }
                presenceLoader.getChatStateLiveData((GroupId)chatId).observe(this, groupChatState -> {
                    if (groupChatState == null || groupChatState.typingUsers == null || groupChatState.typingUsers.isEmpty()) {
                        setSubtitle(null);
                    } else {
                        if (groupChatState.typingUsers.size() == 1) {
                            UserId typingUser = Preconditions.checkNotNull(groupChatState.typingUsers.get(0));
                            Contact contact = groupChatState.contactMap.get(typingUser);
                            if (contact == null) {
                                setSubtitle(null);
                                return;
                            }
                            setSubtitle(getString(R.string.group_user_typing, contact.getDisplayName()));
                        } else {
                            setSubtitle(getString(R.string.group_many_users_typing));
                        }
                    }
                });
            }
            ContentDb.getInstance(this).setChatSeen(chatId);
        });
        avatarLoader.load(avatarView, chatId);
        viewModel.deleted.observe(this, deleted -> {
            if (deleted != null && deleted) {
                finish();
            }
        });
        replyContainer = findViewById(R.id.reply_container);
        if (viewModel.replyPost != null) {
            viewModel.replyPost.getLiveData().observe(this, this::updatePostReply);
            viewModel.replyName.getLiveData().observe(this, replyNameView::setText);
        } else {
            replyContainer.setVisibility(View.GONE);
        }
        viewModel.replyMessage.getLiveData().observe(this, this::updateMessageReply);
    }

    @Override
    protected void onResume() {
        super.onResume();
        avatarLoader.load(avatarView, chatId);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        titleView.setText(title);
    }

    private void setSubtitle(@Nullable CharSequence subtitle) {
        subtitleView.setVisibility(subtitle == null ? View.GONE : View.VISIBLE);
        subtitleView.setText(subtitle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        scrollToNewMessageOnDataLoaded = true;
        viewModel.chat.invalidate();
        ForegroundChat.getInstance().setForegroundChatId(null);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (replyPostId != null) {
            outState.putParcelable(EXTRA_REPLY_POST_SENDER_ID, replySenderId);
            outState.putString(EXTRA_REPLY_POST_ID, replyPostId);
            outState.putInt(EXTRA_REPLY_POST_MEDIA_INDEX, replyPostMediaIndex);
        }

        outState.putLong(EXTRA_SELECTED_MESSAGE_ROW_ID, selectedMessageRowId);
        outState.putString(EXTRA_COPY_TEXT, copyText);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (BuildConfig.DEBUG && keyCode == KeyEvent.KEYCODE_BACK) {
            Debug.showDebugMenu(this, editText, chatId);
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        menuItem = menu.findItem(R.id.block);
        menuItem.setVisible(chatId instanceof UserId);
        viewModel.getBlockList().observe(this, userIds -> {
            blocked = updateBlockedContact(userIds);
            Log.i("ChatActivity: blocked = " + blocked);
            if (blocked) {
                menuItem.setTitle(getString(R.string.unblock));
            } else {
                menuItem.setTitle(getString(R.string.block));
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_chat: {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getBaseContext().getString(R.string.delete_chat_confirmation));
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.yes, (dialog, which) -> ContentDb.getInstance(this).deleteChat(chatId));
                builder.setNegativeButton(R.string.no, null);
                builder.show();
                return true;
            }
            case R.id.block: {
                if (!blocked) {
                    blockContact(item);
                } else {
                    unBlockContact(item);
                }
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void blockContact(MenuItem item) {
        ProgressDialog blockDialog = ProgressDialog.show(this, null, getString(R.string.blocking_user_in_progress, chatName), true);
        blockDialog.show();

        viewModel.blockContact((UserId)chatId).observe(this, success -> {
            if (success == null) {
                return;
            }
            blockDialog.cancel();
            if (success) {
                SnackbarHelper.showInfo(this, getString(R.string.blocking_user_successful, chatName));
                item.setTitle(getString(R.string.unblock));
                viewModel.sendSystemMessage(Message.USAGE_BLOCK, chatId);
            } else {
                SnackbarHelper.showWarning(this, getString(R.string.blocking_user_failed_check_internet, chatName));
            }
        });
    }

    private void unBlockContact(MenuItem item) {
        ProgressDialog unblockDialog = ProgressDialog.show(this, null, getString(R.string.unblocking_user_in_progress, chatName), true);
        unblockDialog.show();
        viewModel.unblockContact(new UserId(chatId.rawId())).observe(this, success -> {
            if (success == null) {
                return;
            }
            unblockDialog.cancel();
            if (success) {
                SnackbarHelper.showInfo(this, getString(R.string.unblocking_user_successful, chatName));
                item.setTitle(getString(R.string.block));
                viewModel.sendSystemMessage(Message.USAGE_UNBLOCK, chatId);
            } else {
                SnackbarHelper.showWarning(this, getString(R.string.unblocking_user_failed_check_internet, chatName));
            }
        });
    }

    private boolean updateBlockedContact(List<UserId> userIds) {
        if (userIds == null) {
            Log.i("ChatActivity: blocklist's userids is null");
            return false;
        }
        for (UserId userId : userIds) {
            if (userId.equals(chatId)) {
                return true;
            }
        }
        return false;
    }

    private void updatePostReply(@Nullable Post post) {
        if (post != null) {
            replyContainer.setVisibility(View.VISIBLE);
            final TextView replyTextView = replyContainer.findViewById(R.id.reply_text);
            textContentLoader.load(replyTextView, post);
            final ImageView replyMediaIconView = replyContainer.findViewById(R.id.reply_media_icon);
            final ImageView replyMediaThumbView = replyContainer.findViewById(R.id.reply_media_thumb);
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
            replyContainer.findViewById(R.id.reply_close).setOnClickListener(v -> {
                replyPostId = null;
                replyPostMediaIndex = -1;
                replyContainer.setVisibility(View.GONE);
            });
        } else {
            replyContainer.setVisibility(View.GONE);
        }
    }

    private void updateMessageReply(@Nullable Message message) {
        if (message != null) {
            replyMessage = message;
            replyContainer.setVisibility(View.VISIBLE);
            contactLoader.load(replyNameView, message.senderUserId);
            TextView replyTextView = replyContainer.findViewById(R.id.reply_text);
            textContentLoader.load(replyTextView, message);
            final ImageView replyMediaIconView = replyContainer.findViewById(R.id.reply_media_icon);
            final ImageView replyMediaThumbView = replyContainer.findViewById(R.id.reply_media_thumb);
            if (replyMessageMediaIndexMap.containsKey(replyMessageRowId)) {
                replyMessageMediaIndex = replyMessageMediaIndexMap.get(replyMessageRowId);
            }
            if (replyMessageMediaIndex >= 0 && replyMessageMediaIndex < message.media.size()) {
                replyMediaThumbView.setVisibility(View.VISIBLE);
                replyMediaThumbView.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), getResources().getDimension(R.dimen.comment_media_list_corner_radius));
                    }
                });
                replyMediaThumbView.setClipToOutline(true);
                final Media media = message.media.get(replyMessageMediaIndex);
                mediaThumbnailLoader.load(replyMediaThumbView, media);
                replyMediaIconView.setVisibility(View.VISIBLE);
                switch (media.type) {
                    case Media.MEDIA_TYPE_IMAGE: {
                        replyMediaIconView.setImageResource(R.drawable.ic_camera);
                        if (TextUtils.isEmpty(message.text)) {
                            replyTextView.setText(R.string.photo);
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_VIDEO: {
                        replyMediaIconView.setImageResource(R.drawable.ic_video);
                        if (TextUtils.isEmpty(message.text)) {
                            replyTextView.setText(R.string.video);
                        }
                        break;
                    }
                    case Media.MEDIA_TYPE_UNKNOWN:
                    default: {
                        replyMediaIconView.setImageResource(R.drawable.ic_media_collection);
                        replyTextView.setText(message.text);
                        break;
                    }
                }
            } else {
                replyMediaThumbView.setVisibility(View.GONE);
                replyMediaIconView.setVisibility(View.GONE);
            }
            replyContainer.findViewById(R.id.reply_close).setOnClickListener(v -> {
                replyMessageRowId = -1;
                replyMessageMediaIndex = -1;
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
                Message.TYPE_CHAT,
                Message.USAGE_CHAT,
                Message.STATE_INITIAL,
                messageText,
                replyPostId,
                replyPostMediaIndex,
                replyMessage != null ? replyMessage.id : null,
                replyMessageMediaIndex ,
                replyMessage != null ? (replyMessage.senderUserId.isMe() ? new UserId(me.getUser()) : replyMessage.senderUserId) : null,
                0);
        replyPostId = null;
        replyPostMediaIndex = -1;
        replyMessage = null;
        replyMessageMediaIndex = -1;
        replyContainer.setVisibility(View.GONE);
        message.addToStorage(ContentDb.getInstance(this));
    }

    private void pickMedia() {
        // TODO (ds): uncomment when implemented
        final Intent intent = new Intent(this, MediaPickerActivity.class);
        intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_SEND);
        intent.putExtra(MediaPickerActivity.EXTRA_CHAT_ID, chatId);
        intent.putExtra(MediaPickerActivity.EXTRA_REPLY_POST_ID, replyPostId);
        intent.putExtra(MediaPickerActivity.EXTRA_REPLY_POST_MEDIA_INDEX, replyPostMediaIndex);
        startActivityForResult(intent, REQUEST_CODE_COMPOSE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_COMPOSE: {
                if (resultCode == RESULT_OK) {
                    replyPostId = null;
                    replyPostMediaIndex = -1;
                    updatePostReply(null);
                }
                break;
            }
            case REQUEST_CODE_VIEW_GROUP_INFO: {
                if (resultCode == GroupInfoActivity.RESULT_CODE_EXIT_CHAT) {
                    finish();
                }
                break;
            }
        }
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
        static final int VIEW_TYPE_SYSTEM = 8;

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
            if (message.type == Message.TYPE_SYSTEM) {
                return VIEW_TYPE_SYSTEM;
            } else if (message.isIncoming()) {
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
                case VIEW_TYPE_SYSTEM: {
                    layoutRes = R.layout.message_item_system;
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
            holder.bindTo(
                    message,
                    firstUnseenMessageRowId == message.rowId
                            ? newMessageCount
                            : 0,
                    position < getItemCount() - 1
                            ? getItem(position + 1)
                            : null,
                    position >= 1
                            ? getItem(position - 1)
                            : null);
        }
    }

    private boolean updateActionMode(String text) {
        if (actionMode == null) {
            copyText = text;
            actionMode = startSupportActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    getMenuInflater().inflate(R.menu.clipboard, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.copy:
                            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText(getString(R.string.copy_text), text);
                            clipboardManager.setPrimaryClip(clipData);
                            if (actionMode != null) {
                                actionMode.finish();
                            }
                            return true;
                        case R.id.reply:
                            replyMessageRowId = selectedMessageRowId;
                            viewModel.updateMessageRowId(selectedMessageRowId);
                            if (actionMode != null) {
                                actionMode.finish();
                            }
                            return true;
                        default:
                            return false;
                    }
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    selectedMessageRowId = -1;
                    adapter.notifyDataSetChanged();
                    actionMode = null;
                }
            });
        }
        return true;
    }

    private final MessageViewHolder.MessageViewHolderParent messageViewHolderParent = new MessageViewHolder.MessageViewHolderParent() {
        private final LongSparseArray<Integer> textLimits = new LongSparseArray<>();

        @Override
        public void onItemLongClicked(String text, long messageRowId) {
            selectedMessageRowId = messageRowId;
            updateActionMode(text);
            adapter.notifyDataSetChanged();
        }

        @Override
        public long getSelectedMessageRowId() {
            return selectedMessageRowId;
        }

        @Override
        public void unblockContactFromTap() {
            if (blocked) {
                unBlockContact(menuItem);
            }
        }

        @Override
        public void setReplyMessageMediaIndex(long rowId, int pos) {
            replyMessageMediaIndexMap.put(rowId, pos);
        }

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
        public TextContentLoader getTextContentLoader() {
            return textContentLoader;
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
        public RecyclerView.RecycledViewPool getMediaViewPool() {
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
