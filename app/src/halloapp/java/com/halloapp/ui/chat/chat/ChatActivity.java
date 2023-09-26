package com.halloapp.ui.chat.chat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.LongSparseArray;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.SharedElementCallback;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.halloapp.ApkHasher;
import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.ContentDraftManager;
import com.halloapp.Debug;
import com.halloapp.DocumentPreviewLoader;
import com.halloapp.ForegroundChat;
import com.halloapp.Notifications;
import com.halloapp.R;
import com.halloapp.UrlPreview;
import com.halloapp.UrlPreviewLoader;
import com.halloapp.calling.calling.CallManager;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Mention;
import com.halloapp.content.Message;
import com.halloapp.content.Reaction;
import com.halloapp.emoji.ReactionPopupWindow;
import com.halloapp.groups.GroupLoader;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.AudioDurationLoader;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.media.MediaUtils;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.server.CallType;
import com.halloapp.ui.ContentComposerActivity;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.MediaPagerAdapter;
import com.halloapp.ui.ReactionLoader;
import com.halloapp.ui.SystemMessageTextResolver;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.TimestampRefresher;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.camera.CameraActivity;
import com.halloapp.ui.contacts.CreateContactCardActivity;
import com.halloapp.ui.contacts.SelectDeviceContactActivity;
import com.halloapp.ui.groups.ChatGroupInfoActivity;
import com.halloapp.ui.groups.FeedGroupInfoActivity;
import com.halloapp.ui.groups.GroupParticipants;
import com.halloapp.ui.mediaexplorer.MediaExplorerActivity;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.ui.mentions.MentionPickerView;
import com.halloapp.ui.mentions.TextContentLoader;
import com.halloapp.ui.posts.SeenByLoader;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.ui.share.ForwardActivity;
import com.halloapp.ui.share.ShareDestination;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ClipUtils;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.BaseInputView;
import com.halloapp.widget.DrawDelegateView;
import com.halloapp.widget.ItemSwipeHelper;
import com.halloapp.widget.LinkPreviewComposeView;
import com.halloapp.widget.MentionableEntry;
import com.halloapp.widget.NestedHorizontalScrollHelper;
import com.halloapp.widget.PressInterceptView;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.widget.SwipeListItemHelper;
import com.halloapp.xmpp.PresenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ChatActivity extends HalloActivity implements EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_REPLY_POST_SENDER_ID = "reply_post_sender_id";
    public static final String EXTRA_REPLY_POST_ID = "reply_post_id";
    public static final String EXTRA_REPLY_POST_MEDIA_INDEX = "reply_post_media_index";
    public static final String EXTRA_SELECTED_MESSAGE = "selected_message";
    public static final String EXTRA_OPEN_KEYBOARD = "open_keyboard";

    private static final String EXTRA_FROM_NOTIF = "from_notif";
    private static final String EXTRA_CHAT_ID = "chat_id";
    private static final String DUMMY_ACTION = "dummy_action";

    public static Intent open(@NonNull Context context, @NonNull ChatId chatId) {
        return open(context, chatId, false);
    }

    public static Intent open(@NonNull Context context, @NonNull ChatId chatId, boolean fromNotification) {
        //noinspection ConstantConditions
        if (chatId == null) {
            throw new IllegalArgumentException("Trying to open ChatActivity for null chatId");
        }
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EXTRA_CHAT_ID, chatId.rawId());
        intent.putExtra(EXTRA_FROM_NOTIF, fromNotification);
        intent.setAction(DUMMY_ACTION); // A StackOverflow user claimed this fixed extras getting dropped
        return intent;
    }

    private static final int REQUEST_CODE_COMPOSE = 1;
    private static final int REQUEST_CODE_VIEW_GROUP_INFO = 2;
    private static final int REQUEST_CODE_CHOOSE_DOCUMENT = 3;
    private static final int REQUEST_CODE_TAKE_PHOTO = 4;
    private static final int REQUEST_CODE_SELECT_CONTACT = 5;
    private static final int REQUEST_CODE_CREATE_CONTACT_CARD = 6;
    private static final int REQUEST_CODE_FORWARD_SELECTION = 7;

    private static final int REQUEST_PERMISSIONS_RECORD_VOICE_NOTE = 1;

    private static final int ADD_ANIMATION_DURATION = 60;
    private static final int MOVE_ANIMATION_DURATION = 125;
    private static final int LAST_SEEN_MARQUEE_DELAY = 2000;

    private static final int PERSIST_DELAY_MS = 500;

    private Runnable updateDraftRunnable;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContentDraftManager contentDraftManager = ContentDraftManager.getInstance();
    private final CallManager callManager = CallManager.getInstance();

    private FirebaseAnalytics firebaseAnalytics;

    private final ChatAdapter adapter = new ChatAdapter();
    private ChatViewModel viewModel;

    private MentionableEntry editText;
    private MentionPickerView mentionPickerView;
    private View replyContainer;
    private View replyPreviewContainer;
    private RecyclerView chatView;

    private TextView titleView;
    private TextView subtitleView;
    private ImageView avatarView;
    private View footer;

    private ChatId chatId;

    private AvatarLoader avatarLoader;
    private PresenceManager presenceManager;
    private BgWorkers bgWorkers;

    private MediaThumbnailLoader mediaThumbnailLoader;
    private DocumentPreviewLoader documentPreviewLoader;
    private GroupLoader groupLoader;
    private ReplyLoader replyLoader;
    private ContactLoader contactLoader;
    private TextContentLoader textContentLoader;
    private AudioDurationLoader audioDurationLoader;
    private TimestampRefresher timestampRefresher;
    private ActionMode actionMode;
    private SystemMessageTextResolver systemMessageTextResolver;
    private boolean focusSelectedMessageOnLoad = false;

    private UserId replySenderId;
    private int replyPostMediaIndex;
    private int replyMessageMediaIndex = 0;
    private long highlightedMessageRowId = -1;
    private boolean blocked;

    private ItemSwipeHelper itemSwipeHelper;
    private LinearLayoutManager layoutManager;
    private DrawDelegateView drawDelegateView;
    private final RecyclerView.RecycledViewPool recycledMediaViews = new RecyclerView.RecycledViewPool();

    private boolean scrollUpOnDataLoaded;
    private boolean scrollToNewMessageOnDataLoaded = true;
    private final LongSparseArray<Integer> mediaPagerPositionMap = new LongSparseArray<>();
    private Timer lastSeenMarqueeTimer;

    private MenuItem blockMenuItem;
    private MenuItem voiceCallMenuItem;
    private MenuItem videoCallMenuItem;
    private final Map<Long, Integer> replyMessageMediaIndexMap = new HashMap<>();

    private View unknownFriendsContainer;
    private View screenOverlay;

    private boolean showKeyboardOnResume;
    private boolean allowVoiceNoteSending;

    private boolean allowCalling;

    private boolean isCallOngoing = false;

    private BaseInputView chatInputView;

    private UrlPreviewLoader urlPreviewLoader;
    private LinkPreviewComposeView linkPreviewComposeView;
    private ReplyPreviewContainer replyPreviewContainerHolder;

    private ReactionPopupWindow reactionPopupWindow;
    private ReactionLoader reactionLoader;

    private MessageViewHolder selectedMessageViewholder;

    private final SharedElementCallback sharedElementCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            for (String name : names) {
                View view = MediaPagerAdapter.getTransitionView(findViewById(R.id.chat), name);
                if (view != null) {
                    sharedElements.put(name, view);
                }
            }
        }
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onReactionAdded(@NonNull Reaction reaction, @NonNull ContentItem contentItem) {
            if (contentItem instanceof Message) {
                RecyclerView.ViewHolder viewHolder = chatView.findViewHolderForItemId(contentItem.rowId);
                if (viewHolder instanceof MessageViewHolder) {
                    ((MessageViewHolder) viewHolder).reloadReactions();
                }
            }
        }
    };

    private void onMessageSent() {
        firebaseAnalytics.logEvent("msg_sent", null);
        viewModel.clearReply();
        chatInputView.clearTextDraft();
        replyPostMediaIndex = 0;
        replyMessageMediaIndex = 0;
        replyContainer.setVisibility(View.GONE);
        setResult(RESULT_OK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO(jack): Remove once we've fixed the null ChatId crash
        Log.d("ChatActivity digest: " + ApkHasher.getInstance().get());
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Log.d("ChatActivity got extra " + key + " -> " + extras.get(key));
            }
        }

        supportRequestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        supportRequestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setExitSharedElementCallback(sharedElementCallback);

        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(getResources().getBoolean(R.bool.light_system_bars)));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        setContentView(R.layout.activity_chat);

        screenOverlay = findViewById(R.id.darken_screen);
        screenOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (actionMode != null) {
                    actionMode.finish();
                }
                return true;
            }
        });

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        titleView = findViewById(R.id.title);
        subtitleView = findViewById(R.id.subtitle);
        avatarView = findViewById(R.id.avatar);
        footer = findViewById(R.id.footer);
        unknownFriendsContainer = findViewById(R.id.unknown_friend_container);

        chatInputView = findViewById(R.id.entry_view);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());

        final Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        mediaThumbnailLoader = new MediaThumbnailLoader(this, Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        documentPreviewLoader = new DocumentPreviewLoader(Math.min(Constants.MAX_IMAGE_DIMENSION, Math.max(point.x, point.y)));
        contactLoader = new ContactLoader(userId -> {
            startActivity(ViewProfileActivity.viewProfile(this, userId));
            return null;
        });
        reactionLoader = new ReactionLoader();
        groupLoader = new GroupLoader();
        replyLoader = new ReplyLoader(getResources().getDimensionPixelSize(R.dimen.reply_thumb_size));
        avatarLoader = AvatarLoader.getInstance();
        bgWorkers = BgWorkers.getInstance();
        presenceManager = PresenceManager.getInstance();
        textContentLoader = new TextContentLoader();
        audioDurationLoader = new AudioDurationLoader(this);
        timestampRefresher = new ViewModelProvider(this).get(TimestampRefresher.class);
        timestampRefresher.refresh.observe(this, value -> adapter.notifyDataSetChanged());
        urlPreviewLoader = new UrlPreviewLoader();

        systemMessageTextResolver = new SystemMessageTextResolver(contactLoader);

        allowVoiceNoteSending = true;

        String rawChatId = getIntent().getStringExtra(EXTRA_CHAT_ID);
        chatId = ChatId.fromNullable(rawChatId);
        Log.d("ChatActivity chatId " + chatId);
        allowCalling = chatId instanceof UserId;

        chatInputView.bindEmojiKeyboardLayout(findViewById(R.id.emoji_keyboard));
        chatInputView.setVoiceNoteControlView(findViewById(R.id.recording_ui));
        chatInputView.setInputParent(new BaseInputView.InputParent() {
            @Override
            public void onSendText() {
                sendMessage();
                onMessageSent();
            }

            @Override
            public void onSendVoiceNote() {
                viewModel.finishRecording(replyPostMediaIndex, replyMessageMediaIndex, false);
                onMessageSent();
            }

            @Override
            public void onSendVoiceDraft(File draft) {
                viewModel.sendVoiceNote(replyPostMediaIndex, replyMessageMediaIndex, draft);
                onMessageSent();
            }

            @Override
            public void onChooseGallery() {
                pickMedia();
            }

            @Override
            public void onChooseDocument() {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_DOCUMENT);
            }

            @Override
            public void onChooseCamera() {
                takePhoto();
            }

            @Override
            public void onChooseContact() {
                Intent i = SelectDeviceContactActivity.select(ChatActivity.this);
                startActivityForResult(i, REQUEST_CODE_SELECT_CONTACT);
            }

            @Override
            public void requestVoicePermissions() {
                EasyPermissions.requestPermissions(ChatActivity.this, getString(R.string.voice_note_record_audio_permission_rationale), REQUEST_PERMISSIONS_RECORD_VOICE_NOTE, Manifest.permission.RECORD_AUDIO);
            }

            @Override
            public void onUrl(String url) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                urlPreviewLoader.load(linkPreviewComposeView, url, new ViewDataLoader.Displayer<View, UrlPreview>() {
                    @Override
                    public void showResult(@NonNull View view, @Nullable UrlPreview result) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        linkPreviewComposeView.updateUrlPreview(result);
                    }

                    @Override
                    public void showLoading(@NonNull View view) {
                        linkPreviewComposeView.setLoadingUrl(url);
                        linkPreviewComposeView.setLoading(!TextUtils.isEmpty(url));
                    }
                });
            }
        });

        findViewById(R.id.unknown_block).setOnClickListener(v -> {
            final String chatName = viewModel.name.getLiveData().getValue();
            ProgressDialog blockDialog = ProgressDialog.show(this, null, getString(R.string.blocking_user_in_progress, chatName), true);
            blockDialog.show();

            viewModel.blockContact((UserId)chatId).observe(this, success -> {
                if (success == null) {
                    return;
                }
                blockDialog.cancel();
                if (success) {
                    SnackbarHelper.showInfo(chatView, getString(R.string.blocking_user_successful, chatName));
                    ContentDb.getInstance().setUnknownContactAllowed((UserId) chatId, () -> {
                        viewModel.chat.invalidate();
                    });
                    finish();
                } else {
                    SnackbarHelper.showWarning(chatView, getString(R.string.blocking_user_failed_check_internet, chatName));
                }
            });
        });

        findViewById(R.id.unknown_add_friend).setOnClickListener(v -> {
            if (chatId instanceof UserId) {
                ContentDb.getInstance().setUnknownContactAllowed((UserId) chatId, () -> {
                    viewModel.chat.invalidate();
                });
                viewModel.sendFriendRequest((UserId) chatId).observe(this, success -> {
                    if (Boolean.TRUE.equals(success)) {
                        SnackbarHelper.showInfo(this, R.string.send_friend_request_successful);
                        return;
                    }
                    SnackbarHelper.showWarning(this, R.string.error_send_friend_request);
                });
                ContentDb.getInstance().setChatSeen(chatId);
            }
        });

        mentionPickerView = findViewById(R.id.mention_picker_view);
        editText = findViewById(R.id.entry_card);
        editText.setMentionPickerView(mentionPickerView);
        editText.setMediaInputListener(uri -> startActivity(new Intent(getBaseContext(), ContentComposerActivity.class)
                .putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(Collections.singleton(uri)))
                .putExtra(ContentComposerActivity.EXTRA_CHAT_ID, chatId)
                .putExtra(ContentComposerActivity.EXTRA_ALLOW_ADD_MEDIA, true)));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSendButton(s);
            }
        });
        drawDelegateView = findViewById(R.id.draw_delegate);

        View toolbarTitleContainer = findViewById(R.id.toolbar_text_container);
        toolbarTitleContainer.setOnClickListener(v -> {
            if (chatId instanceof UserId) {
                Intent viewProfile = ViewProfileActivity.viewProfile(this, (UserId)chatId);
                startActivity(viewProfile);
            } else if (chatId instanceof GroupId) {
                startActivityForResult(ChatGroupInfoActivity.viewGroup(this, (GroupId)chatId), REQUEST_CODE_VIEW_GROUP_INFO);
            }
        });

        chatView = findViewById(R.id.chat);
        SimpleItemAnimator animator = Preconditions.checkNotNull((SimpleItemAnimator) chatView.getItemAnimator());
        animator.setSupportsChangeAnimations(false);
        animator.setAddDuration(ADD_ANIMATION_DURATION);
        animator.setMoveDuration(MOVE_ANIMATION_DURATION);

        ContentDb.getInstance().addObserver(contentObserver);

        View scrollToBottom = findViewById(R.id.scroll_to_bottom);
        chatView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1)) {
                    scrollToBottom.setVisibility(View.GONE);
                } else {
                    scrollToBottom.setVisibility(View.VISIBLE);
                }
                drawDelegateView.forceRecomputeOffsets();
            }
        });
        chatView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> drawDelegateView.forceRecomputeOffsets());
        scrollToBottom.setOnClickListener(v -> {
            scrollUpOnDataLoaded = true;
            viewModel.reloadMessagesAt(Long.MAX_VALUE);
            scrollToBottom.setVisibility(View.GONE);
        });

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(false);
        chatView.setLayoutManager(layoutManager);
        chatView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        NestedHorizontalScrollHelper.applyDefaultScrollRatio(chatView);

        chatView.setAdapter(adapter);

        String replyPostId;

        Long selMessage = null;

        if (savedInstanceState == null) {
            replySenderId = getIntent().getParcelableExtra(EXTRA_REPLY_POST_SENDER_ID);
            replyPostId = getIntent().getStringExtra(EXTRA_REPLY_POST_ID);
            replyPostMediaIndex = getIntent().getIntExtra(EXTRA_REPLY_POST_MEDIA_INDEX, 0);
        } else {
            replySenderId = savedInstanceState.getParcelable(EXTRA_REPLY_POST_SENDER_ID);
            replyPostId = savedInstanceState.getString(EXTRA_REPLY_POST_ID);
            replyPostMediaIndex = savedInstanceState.getInt(EXTRA_REPLY_POST_MEDIA_INDEX, 0);
            if (savedInstanceState.containsKey(EXTRA_SELECTED_MESSAGE)) {
                selMessage = savedInstanceState.getLong(EXTRA_SELECTED_MESSAGE);
            }
        }

        viewModel = new ViewModelProvider(this,
                new ChatViewModel.Factory(getApplication(), chatId, replySenderId, replyPostId)).get(ChatViewModel.class);

        editText.setText(contentDraftManager.getTextDraft(chatId));

        if (selMessage != null) {
            viewModel.loadSelectedMessage(selMessage);
        }
        final View newMessagesView = findViewById(R.id.new_messages);
        newMessagesView.setOnClickListener(v -> {
            scrollUpOnDataLoaded = true;
            viewModel.reloadMessagesAt(Long.MAX_VALUE);
        });

        if (allowVoiceNoteSending) {
            chatInputView.bindVoiceRecorder(this, viewModel.getVoiceNoteRecorder());
            chatInputView.bindVoicePlayer(this, viewModel.getVoiceNotePlayer());
        }

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
                if (focusSelectedMessageOnLoad) {
                    Long selectedRowId = viewModel.getSelectedMessageRowId();
                    if (selectedRowId != null) {
                        focusSelectedMessage(selectedRowId);
                    }
                }
            }
        };
        viewModel.getSelectedMessage().observe(this, message -> {
            if (message == null) {
                unfocusSelectedMessage();
                return;
            }
            focusSelectedMessage(message.rowId);
            updateActionMode(message);
        });
        viewModel.mentionableContacts.getLiveData().observe(this, contacts -> mentionPickerView.setMentionableContacts(contacts));
        viewModel.messageList.observe(this, messages -> adapter.submitList(messages, newListCommitCallback));
        viewModel.chat.getLiveData().observe(this, chat -> {
            Log.i("ChatActivity: chat changed, newMessageCount=" + (chat == null ? "null" : chat.newMessageCount));
            if (chat != null) {
                adapter.setFirstUnseenMessageRowId(chat.firstUnseenMessageRowId);
                adapter.setNewMessageCount(chat.newMessageCount);
                footer.setVisibility(chat.isActive ? View.VISIBLE : View.GONE);
                unknownFriendsContainer.setVisibility((!chat.isGroup && !chat.isActive) ? View.VISIBLE : View.GONE);
            }

            if (chatId instanceof UserId) {
                viewModel.name.getLiveData().observe(this, this::setTitle);
                presenceManager.getLastSeenLiveData((UserId)chatId, true).observe(this, presenceState -> {
                    if (presenceState == null || presenceState.state == PresenceManager.PresenceState.PRESENCE_STATE_UNKNOWN) {
                        setSubtitle(null);
                    } else if (presenceState.state == PresenceManager.PresenceState.PRESENCE_STATE_ONLINE) {
                        setSubtitle(getString(R.string.online));
                    } else if (presenceState.state == PresenceManager.PresenceState.PRESENCE_STATE_OFFLINE) {
                        setSubtitle(TimeFormatter.formatLastSeen(this, presenceState.lastSeen));
                    } else if (presenceState.state == PresenceManager.PresenceState.PRESENCE_STATE_TYPING) {
                        setSubtitle(getString(R.string.user_typing));
                    }
                });
            } else if (chatId instanceof GroupId) {
                if (chat != null) {
                    setTitle(chat.name);
                }
                presenceManager.getChatStateLiveData((GroupId)chatId).observe(this, groupChatState -> {
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
            if (chat == null || chat.isGroup || chat.isActive) {
                ContentDb.getInstance().setChatSeen(chatId);
            }
        });
        avatarLoader.load(avatarView, chatId);
        viewModel.deleted.observe(this, deleted -> {
            if (deleted != null && deleted) {
                finish();
            }
        });

        replyContainer = findViewById(R.id.reply_container);
        replyPreviewContainer = findViewById(R.id.reply_preview_container);
        replyPreviewContainerHolder = new ReplyPreviewContainer(replyContainer);
        replyPreviewContainerHolder.init(contactLoader, textContentLoader, audioDurationLoader, mediaThumbnailLoader);

        final TextView replyNameView = findViewById(R.id.reply_name);
        replyNameView.setTextColor(ContextCompat.getColor(this, R.color.secondary_text));

        replyPreviewContainerHolder.setOnDismissListener(() -> {
            viewModel.clearReply();
        });
        viewModel.reply.getLiveData().observe(this, reply -> {
            if (reply == null) {
                replyContainer.setVisibility(View.GONE);
                return;
            }
            if (reply.post != null) {
                replyPreviewContainerHolder.bindPost(reply.post, replyPostMediaIndex);
            } else if (reply.message != null) {
                long replyMessageRowId = reply.message.rowId;
                replyPostMediaIndex = 0;
                if (replyMessageMediaIndexMap.containsKey(replyMessageRowId)) {
                    replyMessageMediaIndex = replyMessageMediaIndexMap.get(replyMessageRowId);
                }
                replyPreviewContainerHolder.bindMessage(reply.message, replyMessageMediaIndex);
            }
        });

        linkPreviewComposeView = findViewById(R.id.link_preview_compose_view);
        linkPreviewComposeView.setMediaThumbnailLoader(mediaThumbnailLoader);
        linkPreviewComposeView.setOnRemovePreviewClickListener(v -> {
            urlPreviewLoader.cancel(linkPreviewComposeView);
            linkPreviewComposeView.setLoading(false);
            linkPreviewComposeView.updateUrlPreview(null);
        });

        SwipeListItemHelper swipeListItemHelper = new SwipeListItemHelper(
                Preconditions.checkNotNull(ContextCompat.getDrawable(this, R.drawable.ic_swipe_reply)),
                ContextCompat.getColor(this, R.color.swipe_reply_background),
                getResources().getDimensionPixelSize(R.dimen.swipe_reply_icon_margin)) {

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false; // initiate swipes manually by calling startSwipe()
            }

            @Override
            public boolean canSwipe(@NonNull RecyclerView.ViewHolder viewHolder) {
                switch (viewHolder.getItemViewType()) {
                    case ChatAdapter.VIEW_TYPE_SYSTEM:
                    case ChatAdapter.VIEW_TYPE_INCOMING_RETRACTED:
                    case ChatAdapter.VIEW_TYPE_OUTGOING_RETRACTED:
                    case ChatAdapter.VIEW_TYPE_INCOMING_FUTURE_PROOF:
                    case ChatAdapter.VIEW_TYPE_INCOMING_CALL_LOG:
                    case ChatAdapter.VIEW_TYPE_OUTGOING_CALL_LOG:
                        return false;
                }
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // workaround to reset swiped out view
                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                itemSwipeHelper.attachToRecyclerView(null);
                itemSwipeHelper.attachToRecyclerView(chatView);

                final Message message = ((MessageViewHolder)viewHolder).getMessage();
                if (message == null || message.isRetracted() || message.type == Message.TYPE_SYSTEM || message.type == Message.TYPE_CALL || message.type == Message.TYPE_FUTURE_PROOF) {
                    return;
                }
                viewModel.updateMessageRowId(message.rowId);

                showKeyboard();
            }
        };
        itemSwipeHelper = new ItemSwipeHelper(swipeListItemHelper);
        itemSwipeHelper.attachToRecyclerView(chatView);

        // Modified from ItemTouchHelper
        chatView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            private static final int ACTIVE_POINTER_ID_NONE = -1;

            private final int slop = ViewConfiguration.get(chatView.getContext()).getScaledTouchSlop();

            private int activePointerId;
            private float initialX;
            private float initialY;

            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
                final int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN) {
                    activePointerId = event.getPointerId(0);
                    initialX = event.getX();
                    initialY = event.getY();
                } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                    activePointerId = ACTIVE_POINTER_ID_NONE;
                } else if (activePointerId != ACTIVE_POINTER_ID_NONE) {
                    final int index = event.findPointerIndex(activePointerId);
                    if (index >= 0) {
                        RecyclerView.ViewHolder vh = findSwipedView(event);
                        if (vh instanceof MessageViewHolder) {
                            MessageViewHolder mvh = (MessageViewHolder) vh;
                            Message message = mvh.getMessage();
                            View view = mvh.itemView;

                            if (message.media != null && message.media.size() > 1) {
                                View mediaPager = view.findViewById(R.id.media_pager);
                                if (mediaPager != null) {
                                    int[] loc = new int[2];
                                    mediaPager.getLocationOnScreen(loc);
                                    int x = loc[0];
                                    int y = loc[1];

                                    if (initialX > x && initialX < x + mediaPager.getWidth() && initialY > y && initialY < y + mediaPager.getHeight()) {
                                        return false;
                                    }
                                }
                            }
                            swipeListItemHelper.setIconTint(GroupParticipants.getParticipantNameColor(ChatActivity.this, message.senderUserId));
                        }
                        if (vh != null) {
                            itemSwipeHelper.startSwipe(vh);
                        }
                    }
                }

                return false;
            }
            private RecyclerView.ViewHolder findSwipedView(MotionEvent motionEvent) {
                final RecyclerView.LayoutManager lm = Preconditions.checkNotNull(chatView.getLayoutManager());
                if (activePointerId == ACTIVE_POINTER_ID_NONE) {
                    return null;
                }
                final int pointerIndex = motionEvent.findPointerIndex(activePointerId);
                final float dx = motionEvent.getX(pointerIndex) - initialX;
                final float dy = motionEvent.getY(pointerIndex) - initialY;
                final float absDx = Math.abs(dx);
                final float absDy = Math.abs(dy);

                if (absDx < slop && absDy < slop) {
                    return null;
                }
                if (absDx > absDy && lm.canScrollHorizontally()) {
                    return null;
                } else if (absDy > absDx && lm.canScrollVertically()) {
                    return null;
                }
                if (chatView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
                    return null;
                }
                View child = chatView.findChildViewUnder(initialX, initialY);
                if (child == null) {
                    return null;
                }
                return chatView.getChildViewHolder(child);
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        showKeyboardOnResume = getIntent().getBooleanExtra(EXTRA_OPEN_KEYBOARD, showKeyboardOnResume);

        if (replyPostId != null) {
            showKeyboardOnResume = true;
        }

        chatInputView.setAllowVoiceNoteRecording(allowVoiceNoteSending);
        chatInputView.setAllowMedia(true);
    }

    private void updateSendButton(Editable s) {
        boolean currentlyRecording = viewModel.checkIsRecording();
        boolean emptyText = s == null || TextUtils.isEmpty(s.toString());
        chatInputView.setCanSend(!emptyText);
        if (!currentlyRecording) {
            if (updateDraftRunnable != null) {
                mainHandler.removeCallbacks(updateDraftRunnable);
            }
            updateDraftRunnable = () -> {
                if (emptyText) {
                    contentDraftManager.clearTextDraft(chatId);
                } else{
                    contentDraftManager.setTextDraft(chatId, s.toString());
                }
            };
            mainHandler.postDelayed(updateDraftRunnable, PERSIST_DELAY_MS);
        }
    }

    private void showKeyboard() {
        editText.requestFocus();
        final InputMethodManager imm = Preconditions.checkNotNull((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        avatarLoader.load(avatarView, chatId);

        if (showKeyboardOnResume) {
            editText.postDelayed(this::showKeyboard, Constants.KEYBOARD_SHOW_DELAY);
            showKeyboardOnResume = false;
        }

        File audioDraft = contentDraftManager.getAudioDraft(chatId);
        if (audioDraft != null) {
            chatInputView.bindAudioDraft(audioDurationLoader, audioDraft);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        File draft = chatInputView.getAudioDraft();
        contentDraftManager.setAudioDraft(chatId, draft);
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        titleView.setText(title);
    }

    private void setSubtitle(@Nullable CharSequence subtitle) {
        subtitleView.setVisibility(subtitle == null ? View.GONE : View.VISIBLE);
        subtitleView.setText(subtitle);

        if (lastSeenMarqueeTimer != null) {
            lastSeenMarqueeTimer.cancel();
        }
        lastSeenMarqueeTimer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                subtitleView.post(() -> subtitleView.setSelected(true));
            }
        };
        lastSeenMarqueeTimer.schedule(timerTask, LAST_SEEN_MARQUEE_DELAY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaThumbnailLoader.destroy();
        groupLoader.destroy();
        contactLoader.destroy();
        replyLoader.destroy();
        urlPreviewLoader.destroy();
        documentPreviewLoader.destroy();
        reactionLoader.destroy();
        ContentDb.getInstance().removeObserver(contentObserver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("ChatActivity.onStart " + chatId);
        ForegroundChat.getInstance().setForegroundChatId(chatId);
        Notifications.getInstance(this).clearMessageNotifications(chatId);
        Notifications.getInstance(this).updateMissedCallNotifications();
        Chat chat = viewModel.chat.getLiveData().getValue();
        if (chat == null || chat.isGroup || chat.isActive) {
            if (adapter.firstUnseenMessageRowId >= 0) {
                Log.i("ChatActivity.onStart mark " + chatId + " as seen");
                ContentDb.getInstance().setChatSeen(chatId);
            }
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
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.hasExtra(MediaExplorerActivity.EXTRA_CONTENT_ID) && data.hasExtra(MediaExplorerActivity.EXTRA_SELECTED)) {
            String contentId = data.getStringExtra(MediaExplorerActivity.EXTRA_CONTENT_ID);
            int position = data.getIntExtra(MediaExplorerActivity.EXTRA_SELECTED, 0);
            View root = findViewById(R.id.chat);

            if (root != null && contentId != null) {
                postponeEnterTransition();
                MediaPagerAdapter.preparePagerForTransition(root, contentId, position, this::startPostponedEnterTransition);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        String replyPostId = viewModel.getReplyPostId();
        if (replyPostId != null) {
            outState.putParcelable(EXTRA_REPLY_POST_SENDER_ID, replySenderId);
            outState.putString(EXTRA_REPLY_POST_ID, replyPostId);
            outState.putInt(EXTRA_REPLY_POST_MEDIA_INDEX, replyPostMediaIndex);
        }

        Long selectedMessageRowId = viewModel.getSelectedMessageRowId();
        if (selectedMessageRowId != null) {
            outState.putLong(EXTRA_SELECTED_MESSAGE, selectedMessageRowId);
        }
    }

    @Override
    public void onBackPressed() {
        if (!chatInputView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (BuildConfig.DEBUG && keyCode == KeyEvent.KEYCODE_BACK) {
            Debug.showChatDebugMenu(this, editText, chatId);
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        blockMenuItem = menu.findItem(R.id.block);
        voiceCallMenuItem = menu.findItem(R.id.call_voice);
        videoCallMenuItem = menu.findItem(R.id.call_video);

        blockMenuItem.setVisible(chatId instanceof UserId);
        menu.findItem(R.id.verify).setVisible(chatId instanceof UserId);
        viewModel.getBlockList().observe(this, userIds -> {
            blocked = updateBlockedContact(userIds);
            Log.i("ChatActivity: blocked = " + blocked);
            if (blocked) {
                blockMenuItem.setTitle(getString(R.string.unblock));
            } else {
                blockMenuItem.setTitle(getString(R.string.block));
            }
        });

        voiceCallMenuItem.setVisible(allowCalling);
        videoCallMenuItem.setVisible(allowCalling);
        callManager.getIsInCall().observe(this, (inCall) -> {
            this.isCallOngoing = inCall;
            updateCallButtons();
        });
        return true;
    }

    private void updateCallButtons() {
        if (voiceCallMenuItem != null) {
            voiceCallMenuItem.setEnabled(!this.isCallOngoing);
        }
        if (videoCallMenuItem != null) {
            videoCallMenuItem.setEnabled(!this.isCallOngoing);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear_chat) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getBaseContext().getString(R.string.delete_chat_confirmation));
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> ContentDb.getInstance().deleteChat(chatId));
            builder.setNegativeButton(R.string.no, null);
            builder.show();
            return true;
        } else if (item.getItemId() == R.id.block) {
            if (!blocked) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.block_user_confirmation, viewModel.contact.getLiveData().getValue().getDisplayName()));
                builder.setMessage(getString(R.string.block_user_confirmation_consequences));
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.yes, (dialog, which) -> blockContact(item));
                builder.setNegativeButton(R.string.no, null);
                builder.show();
            } else {
                unBlockContact(item);
            }
            return true;
        } else if (item.getItemId() == R.id.verify) {
            startActivity(KeyVerificationActivity.openKeyVerification(this, (UserId) chatId));
        } else if (item.getItemId() == R.id.call_voice) {
            Log.i("ChatActivity: starting a voice call with Uid: " + chatId.rawId());
            callManager.startCallActivity(this, (UserId) chatId, CallType.AUDIO);
            setResult(RESULT_OK);
        } else if (item.getItemId() == R.id.call_video) {
            Log.i("ChatActivity: starting a video call with Uid: " + chatId.rawId());
            callManager.startCallActivity(this, (UserId) chatId, CallType.VIDEO);
            setResult(RESULT_OK);
        }
        return super.onOptionsItemSelected(item);
    }

    private void blockContact(MenuItem item) {
        final String chatName = viewModel.name.getLiveData().getValue();
        ProgressDialog blockDialog = ProgressDialog.show(this, null, getString(R.string.blocking_user_in_progress, chatName), true);
        blockDialog.show();

        viewModel.blockContact((UserId)chatId).observe(this, success -> {
            if (success == null) {
                return;
            }
            blockDialog.cancel();
            if (success) {
                SnackbarHelper.showInfo(chatView, getString(R.string.blocking_user_successful, chatName));
                item.setTitle(getString(R.string.unblock));
                viewModel.sendSystemMessage(Message.USAGE_BLOCK, chatId);
            } else {
                SnackbarHelper.showWarning(chatView, getString(R.string.blocking_user_failed_check_internet, chatName));
            }
        });
    }

    private void unBlockContact(MenuItem item) {
        final String chatName = viewModel.name.getLiveData().getValue();
        ProgressDialog unblockDialog = ProgressDialog.show(this, null, getString(R.string.unblocking_user_in_progress, chatName), true);
        unblockDialog.show();
        viewModel.unblockContact(new UserId(chatId.rawId())).observe(this, success -> {
            if (success == null) {
                return;
            }
            unblockDialog.cancel();
            if (success) {
                SnackbarHelper.showInfo(chatView, getString(R.string.unblocking_user_successful, chatName));
                item.setTitle(getString(R.string.block));
                viewModel.sendSystemMessage(Message.USAGE_UNBLOCK, chatId);
            } else {
                SnackbarHelper.showWarning(chatView, getString(R.string.unblocking_user_failed_check_internet, chatName));
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

    private void sendMessage() {
        final Pair<String, List<Mention>> textAndMentions = editText.getTextWithMentions();
        final String messageText = textAndMentions.first;
        if (TextUtils.isEmpty(messageText)) {
            Log.w("ChatActivity: cannot send empty message");
            return;
        }
        editText.setText(null);
        final Message message = viewModel.buildMessage(messageText, replyPostMediaIndex, replyMessageMediaIndex);
        message.mentions.addAll(textAndMentions.second);
        linkPreviewComposeView.attachPreview(message);
        linkPreviewComposeView.updateUrlPreview(null);
        urlPreviewLoader.cancel(linkPreviewComposeView, true);
        if ((message.urlPreview == null || message.urlPreview.imageMedia == null) && message.loadingUrlPreview != null) {
            urlPreviewLoader.addWaitingContentItem(message);
        }
        viewModel.sendMessage(message);
    }

    private void takePhoto() {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra(CameraActivity.EXTRA_CHAT_ID, chatId);
        intent.putExtra(CameraActivity.EXTRA_REPLY_POST_ID, viewModel.getReplyPostId());
        intent.putExtra(CameraActivity.EXTRA_REPLY_POST_MEDIA_INDEX, replyPostMediaIndex);
        intent.putExtra(CameraActivity.EXTRA_PURPOSE, CameraActivity.PURPOSE_COMPOSE);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }

    private void pickMedia() {
        final Intent intent = MediaPickerActivity.pickForMessage(this, chatId, viewModel.getReplyPostId(), replyPostMediaIndex, chatInputView.getTextDraft());
        startActivityForResult(intent, REQUEST_CODE_COMPOSE);
    }

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_COMPOSE:
            case REQUEST_CODE_TAKE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    onMessageSent();
                }
                break;
            }
            case REQUEST_CODE_VIEW_GROUP_INFO: {
                if (resultCode == FeedGroupInfoActivity.RESULT_CODE_EXIT_CHAT) {
                    finish();
                }
                break;
            }
            case REQUEST_CODE_SELECT_CONTACT: {
                if (resultCode == RESULT_OK && data != null) {
                    Contact contact = data.getParcelableExtra(SelectDeviceContactActivity.EXTRA_SELECTED_CONTACT);

                    Intent i = CreateContactCardActivity.shareContact(this, contact);
                    startActivityForResult(i, REQUEST_CODE_CREATE_CONTACT_CARD);
                }
                break;
            }
            case REQUEST_CODE_FORWARD_SELECTION: {
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<ShareDestination> destinations = data.getParcelableArrayListExtra(ForwardActivity.RESULT_DESTINATIONS);
                    Long selectedMessageRowId = viewModel.getSelectedMessageRowId();
                    if (actionMode != null) {
                        actionMode.finish();
                    } else {
                        viewModel.selectMessage(null);
                    }
                    if (selectedMessageRowId == null) {
                        return;
                    }
                    ProgressDialog blockDialog = ProgressDialog.show(this, null, getString(R.string.forwarding_message_in_progress), true);
                    blockDialog.show();
                    viewModel.forwardMessage(destinations, selectedMessageRowId).observe(this, result -> {
                        if (result == null) {
                            return;
                        }
                        blockDialog.dismiss();
                        if (result) {
                            if (destinations.size() > 1) {
                                Toast.makeText(ChatActivity.this, R.string.forwarded_message, Toast.LENGTH_SHORT).show();
                            } else if (destinations.size() == 1) {
                                startActivity(ChatActivity.open(ChatActivity.this, destinations.get(0).id));
                                finish();
                            }
                        }
                    });
                }
                break;
            }
            case REQUEST_CODE_CREATE_CONTACT_CARD: {
                if (resultCode == RESULT_OK && data != null) {
                    byte[] contactCard = data.getByteArrayExtra(CreateContactCardActivity.EXTRA_CONTACT_CARD);
                    viewModel.sendContact(replyPostMediaIndex, replyMessageMediaIndex, contactCard);
                    onMessageSent();
                } else {
                    Intent i = SelectDeviceContactActivity.select(this);
                    startActivityForResult(i, REQUEST_CODE_SELECT_CONTACT);
                }
                break;
            }
            case REQUEST_CODE_CHOOSE_DOCUMENT: {
                if (resultCode == RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    String uriString = uri.toString();
                    File myFile = new File(uriString);
                    String path = myFile.getAbsolutePath();
                    String displayName = null;

                    if (uriString.startsWith("content://")) {
                        Cursor cursor = null;
                        try {
                            cursor = ChatActivity.this.getContentResolver().query(uri, null, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                                if (size > Constants.DOCUMENT_SIZE_LIMIT) {
                                    Toast.makeText(this, getString(R.string.file_too_large, displayName), Toast.LENGTH_SHORT).show();
                                    Log.e("ChatActivity/onDocumentSelected selected file is too large");
                                }
                            }
                        } finally {
                            cursor.close();
                        }
                        showDocumentSendConfirmationDialog(displayName, uri);
                    } else if (uriString.startsWith("file://")) {
                        displayName = myFile.getName();
                        final String fileName = displayName;
                        BgWorkers.getInstance().execute(() -> {
                            File file = new File(uri.getPath());
                            if (!file.exists()) {
                                Toast.makeText(this, getString(R.string.cannot_open_selected_file), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (file.length() > Constants.DOCUMENT_SIZE_LIMIT) {
                                Toast.makeText(this, getString(R.string.file_too_large, fileName), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mainHandler.post(() -> showDocumentSendConfirmationDialog(fileName, uri));
                        });
                    }
                }
                break;
            }
        }
    }

    private void showDocumentSendConfirmationDialog(String fileName, Uri uri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.send_document_confirmation, fileName, viewModel.name.getLiveData().getValue()));
        builder.setPositiveButton(R.string.send, (d, v) -> {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            viewModel.onSendDocument(fileName, uri);
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
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

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_PERMISSIONS_RECORD_VOICE_NOTE) {
            if (EasyPermissions.permissionPermanentlyDenied(ChatActivity.this, Manifest.permission.RECORD_AUDIO)) {
                new AppSettingsDialog.Builder(ChatActivity.this)
                        .setRationale(getString(R.string.voice_note_record_audio_permission_rationale_denied))
                        .build().show();
            }
        }
    }

    private class ChatAdapter extends PagedListAdapter<Message, MessageViewHolder> {

        static final int VIEW_TYPE_OUTGOING_TEXT = 1;
        static final int VIEW_TYPE_INCOMING_TEXT = 2;
        static final int VIEW_TYPE_OUTGOING_MEDIA = 3;
        static final int VIEW_TYPE_OUTGOING_MEDIA_NO_CAPTION = 4;
        static final int VIEW_TYPE_INCOMING_MEDIA = 5;
        static final int VIEW_TYPE_OUTGOING_RETRACTED = 6;
        static final int VIEW_TYPE_INCOMING_RETRACTED = 7;
        static final int VIEW_TYPE_SYSTEM = 8;
        static final int VIEW_TYPE_INCOMING_TOMBSTONE = 9;
        static final int VIEW_TYPE_INCOMING_FUTURE_PROOF = 10;
        static final int VIEW_TYPE_INCOMING_VOICE_NOTE = 11;
        static final int VIEW_TYPE_OUTGOING_VOICE_NOTE = 12;
        static final int VIEW_TYPE_INCOMING_CALL_LOG = 13;
        static final int VIEW_TYPE_OUTGOING_CALL_LOG = 14;
        static final int VIEW_TYPE_INCOMING_DOCUMENT = 15;
        static final int VIEW_TYPE_OUTGOING_DOCUMENT = 16;
        static final int VIEW_TYPE_INCOMING_CONTACT = 17;
        static final int VIEW_TYPE_OUTGOING_CONTACT = 18;

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
        public void onViewRecycled(@NonNull MessageViewHolder holder) {
            super.onViewRecycled(holder);
            holder.markRecycled();
        }

        @Override
        public void onViewAttachedToWindow(@NonNull MessageViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            holder.markAttach();
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull MessageViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            holder.markDetach();
        }

        @Override
        public int getItemViewType(int position) {
            final Message message = Preconditions.checkNotNull(getItem(position));
            if (message.type == Message.TYPE_SYSTEM) {
                return VIEW_TYPE_SYSTEM;
            } else if (message.type == Message.TYPE_FUTURE_PROOF) {
                return VIEW_TYPE_INCOMING_FUTURE_PROOF;
            } else if (message.type == Message.TYPE_CALL) {
                if (message.isIncoming()) {
                    return VIEW_TYPE_INCOMING_CALL_LOG;
                } else {
                    return VIEW_TYPE_OUTGOING_CALL_LOG;
                }
            } else if (message.isIncoming()) {
                if (message.isTombstone()) {
                    return VIEW_TYPE_INCOMING_TOMBSTONE;
                } if (message.isRetracted()) {
                    return VIEW_TYPE_INCOMING_RETRACTED;
                } else if (message.type == Message.TYPE_VOICE_NOTE) {
                    return VIEW_TYPE_INCOMING_VOICE_NOTE;
                } else if (message.type == Message.TYPE_DOCUMENT) {
                    return VIEW_TYPE_INCOMING_DOCUMENT;
                } else if (message.type == Message.TYPE_CONTACT) {
                    return VIEW_TYPE_INCOMING_CONTACT;
                } else if (message.media.isEmpty()) {
                    return VIEW_TYPE_INCOMING_TEXT;
                } else {
                    return VIEW_TYPE_INCOMING_MEDIA;
                }
            } else {
                if (message.isRetracted()) {
                    return VIEW_TYPE_OUTGOING_RETRACTED;
                } else if (message.type == Message.TYPE_VOICE_NOTE) {
                    return VIEW_TYPE_OUTGOING_VOICE_NOTE;
                } else if (message.type == Message.TYPE_DOCUMENT) {
                    return VIEW_TYPE_OUTGOING_DOCUMENT;
                } else if (message.type == Message.TYPE_CONTACT) {
                    return VIEW_TYPE_OUTGOING_CONTACT;
                } else if (message.media.isEmpty()) {
                    return VIEW_TYPE_OUTGOING_TEXT;
                } else {
                    return VIEW_TYPE_OUTGOING_MEDIA;
                }
            }
        }

        @Override
        public @NonNull
        MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            PressInterceptView root = new PressInterceptView(parent.getContext());
            root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            Log.i("ChatAdapter.onCreateViewHolder " + viewType);
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
                case VIEW_TYPE_INCOMING_TOMBSTONE: {
                    layoutRes = R.layout.message_item_tombstone;
                    break;
                }
                case VIEW_TYPE_INCOMING_FUTURE_PROOF: {
                    layoutRes = R.layout.message_item_future_proof;
                    break;
                }
                case VIEW_TYPE_INCOMING_VOICE_NOTE: {
                    layoutRes = R.layout.message_item_incoming_voice_note;
                    LayoutInflater.from(root.getContext()).inflate(layoutRes, root, true);
                    return new VoiceNoteMessageViewHolder(root, messageViewHolderParent);
                }
                case VIEW_TYPE_OUTGOING_VOICE_NOTE: {
                    layoutRes = R.layout.message_item_outgoing_voice_note;
                    LayoutInflater.from(root.getContext()).inflate(layoutRes, root, true);
                    return new VoiceNoteMessageViewHolder(root, messageViewHolderParent);
                }
                case VIEW_TYPE_OUTGOING_CALL_LOG:
                case VIEW_TYPE_INCOMING_CALL_LOG: {
                    layoutRes = R.layout.message_item_incoming_call;
                    LayoutInflater.from(root.getContext()).inflate(layoutRes, root, true);
                    return new CallMessageViewHolder(root, messageViewHolderParent);
                }
                case VIEW_TYPE_OUTGOING_DOCUMENT: {
                    layoutRes = R.layout.message_item_outgoing_document;
                    LayoutInflater.from(root.getContext()).inflate(layoutRes, root, true);
                    return new DocumentMessageViewHolder(root, messageViewHolderParent);
                }
                case VIEW_TYPE_INCOMING_DOCUMENT: {
                    layoutRes = R.layout.message_item_incoming_document;
                    LayoutInflater.from(root.getContext()).inflate(layoutRes, root, true);
                    return new DocumentMessageViewHolder(root, messageViewHolderParent);
                }
                case VIEW_TYPE_OUTGOING_CONTACT: {
                    layoutRes = R.layout.message_item_outgoing_contact;
                    LayoutInflater.from(root.getContext()).inflate(layoutRes, root, true);
                    return new ContactMessageViewHolder(root, messageViewHolderParent);
                }
                case VIEW_TYPE_INCOMING_CONTACT: {
                    layoutRes = R.layout.message_item_incoming_contact;
                    LayoutInflater.from(root.getContext()).inflate(layoutRes, root, true);
                    return new ContactMessageViewHolder(root, messageViewHolderParent);
                }
                default: {
                    throw new IllegalArgumentException();
                }
            }
            LayoutInflater.from(root.getContext()).inflate(layoutRes, root, true);
            if (root.getChildCount() > 0) {
                ((ViewGroup) root.getChildAt(0)).setClipChildren(false);
            }
            return new MessageViewHolder(root, messageViewHolderParent);
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
                            : null,
                    position == getItemCount() - 1);
            Message selectedMessage = viewModel.getSelectedMessage().getValue();
            if (selectedMessage != null && selectedMessage.rowId == message.rowId && selectedMessageViewholder == null) {
                selectedMessageViewholder = holder;
                holder.focusViewHolder();
            }
        }
    }

    private boolean updateActionMode(Message message) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(new ActionMode.Callback() {

                private int statusBarColor;
                private int previousVisibility;

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    getMenuInflater().inflate(R.menu.clipboard, menu);

                    statusBarColor = getWindow().getStatusBarColor();
                    getWindow().setStatusBarColor(getResources().getColor(R.color.chat_action_mode));
                    previousVisibility = getWindow().getDecorView().getSystemUiVisibility();
                    if (Build.VERSION.SDK_INT >= 23) {
                        getWindow().getDecorView().setSystemUiVisibility(previousVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    }

                    View contentContainerView = selectedMessageViewholder != null ? selectedMessageViewholder.getContentContainerView() : null;

                    if (contentContainerView != null && ServerProps.getInstance().getChatReactionsEnabled()) {
                        reactionPopupWindow = new ReactionPopupWindow(getBaseContext(), message, () -> {
                            reactionPopupWindow.dismiss();
                            if (actionMode != null) {
                                actionMode.finish();
                            }
                        });
                        reactionPopupWindow.show(contentContainerView);
                    }

                    screenOverlay.setVisibility(View.VISIBLE);

                    MenuItem copyItem = menu.findItem(R.id.copy);
                    String text = selectedMessageViewholder != null ? selectedMessageViewholder.copyText() : null;
                    copyItem.setVisible(!TextUtils.isEmpty(text));

                    MenuItem saveToGalleryItem = menu.findItem(R.id.save_to_gallery);
                    List<Media> mediaList = selectedMessageViewholder != null ? selectedMessageViewholder.getMessage().getMedia() : new ArrayList<>();
                    saveToGalleryItem.setVisible(Media.canBeSavedToGallery(mediaList));

                    MenuItem forwardItem = menu.findItem(R.id.forward);
                    forwardItem.setVisible(message.isForwardable());
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.copy) {
                        String text = selectedMessageViewholder != null ? selectedMessageViewholder.copyText() : null;
                        ClipUtils.copyToClipboard(text);
                        if (actionMode != null) {
                            actionMode.finish();
                        }
                        return true;
                    } else if (item.getItemId() == R.id.reply) {
                        viewModel.updateMessageRowId(message.rowId);
                        if (actionMode != null) {
                            actionMode.finish();
                        }
                        return true;
                    } else if (item.getItemId() == R.id.delete) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                        builder.setMessage(getResources().getQuantityString(R.plurals.delete_messages_confirmation, 1, 1));
                        builder.setNegativeButton(R.string.cancel, null);
                        DialogInterface.OnClickListener listener = (dialog, which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE: {
                                    handleMessageDeleteOrRetract(true);
                                    break;
                                }
                                case DialogInterface.BUTTON_NEUTRAL: {
                                    handleMessageDeleteOrRetract(false);
                                    break;
                                }
                            }
                        };
                        boolean retractable = isMessageRetractable(message);
                        if (retractable) {
                            builder.setNeutralButton(R.string.retract_message_option, listener);
                        }
                        builder.setPositiveButton(R.string.delete_message_option, listener);
                        builder.create().show();
                        return true;
                    } else if (item.getItemId() == R.id.forward) {
                        Intent forwardIntent = new Intent(ChatActivity.this, ForwardActivity.class);
                        startActivityForResult(forwardIntent, REQUEST_CODE_FORWARD_SELECTION);
                    } else if (item.getItemId() == R.id.save_to_gallery) {
                        List<Media> mediaList = selectedMessageViewholder.getMessage().getMedia();

                        bgWorkers.execute(() -> {
                            boolean success = true;
                            for (Media media : mediaList) {
                                if (!media.canBeSavedToGallery()) {
                                    continue;
                                }
                                if (!MediaUtils.saveMediaToGallery(getApplication(), media)) {
                                    success = false;
                                    Log.e("ChatActivity failed to save media to gallery: " + media);
                                }
                            }

                            if (success) {
                                SnackbarHelper.showInfo(chatView, R.string.media_saved_to_gallery);
                            } else {
                                SnackbarHelper.showWarning(chatView, R.string.media_save_to_gallery_failed);
                            }
                        });

                        if (actionMode != null) {
                            actionMode.finish();
                        }
                    } else if (item.getItemId() == R.id.info) {
                        Message selectedMessage = viewModel.getSelectedMessage().getValue();
                        if (selectedMessage != null) {
                            startActivity(MessageInfoActivity.viewMessageInfo(ChatActivity.this, selectedMessage.id));
                        }
                        if (actionMode != null) {
                            actionMode.finish();
                        }
                    }
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    if (reactionPopupWindow != null) {
                        reactionPopupWindow.dismiss();
                    }
                    screenOverlay.setVisibility(View.INVISIBLE);
                    unfocusSelectedMessage();
                    viewModel.selectMessage(null);
                    actionMode = null;

                    getWindow().setStatusBarColor(statusBarColor);
                    getWindow().getDecorView().setSystemUiVisibility(previousVisibility);
                }
            });
        }
        return true;
    }

    private void handleMessageDeleteOrRetract(boolean isDeletion) {
        ProgressDialog createDeleteMessageDialog = ProgressDialog.show(ChatActivity.this, null, getResources().getQuantityString(R.plurals.delete_messages_progress, 1, 1));
        Message selectedMessage = viewModel.getSelectedMessage().getValue();
        if (selectedMessage != null) {
            bgWorkers.execute(() -> {
                if (isDeletion) {
                    ContentDb.getInstance().deleteMessage(selectedMessage.rowId);
                } else {
                    ContentDb.getInstance().retractMessage(selectedMessage.rowId, null);
                }
                createDeleteMessageDialog.cancel();
            });
        }
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void focusSelectedMessage(long rowId) {
        for (int childCount = chatView.getChildCount(), i = 0; i < childCount; ++i) {
            final MessageViewHolder holder = (MessageViewHolder) chatView.getChildViewHolder(chatView.getChildAt(i));
            if (rowId == holder.message.rowId) {
                selectedMessageViewholder = holder;
                holder.focusViewHolder();
            }
        }
    }

    private void unfocusSelectedMessage() {
        if (selectedMessageViewholder != null) {
            selectedMessageViewholder.unfocusViewHolder();
            selectedMessageViewholder = null;
        }
    }

    private void selectMessage(Message selectedMessage) {
        viewModel.selectMessage(selectedMessage);
    }

    @WorkerThread
    public boolean isMessageRetractable(Message m) {
        if (m == null || m.senderUserId == null || !m.isMeMessageSender() || m.isRetracted()) {
            return false;
        }
        return true;
    }

    private final MessageViewHolder.MessageViewHolderParent messageViewHolderParent = new MessageViewHolder.MessageViewHolderParent() {
        private final LongSparseArray<Integer> textLimits = new LongSparseArray<>();

        @Override
        public void onItemLongClicked(@NonNull Message message) {
            if (actionMode == null) {
                selectMessage(message);
                chatView.requestDisallowInterceptTouchEvent(true);
            }
        }

        @Override
        public long getHighlightedMessageRowId() {
            return highlightedMessageRowId;
        }

        @Override
        public void clearHighlight() {
            highlightedMessageRowId = -1;
        }

        @Override
        DocumentPreviewLoader getDocumentPreviewLoader() {
            return documentPreviewLoader;
        }

        @Override
        VoiceNotePlayer getVoiceNotePlayer() {
            return viewModel.getVoiceNotePlayer();
        }

        @Override
        public void unblockContactFromTap() {
            if (blocked) {
                unBlockContact(blockMenuItem);
            }
        }

        @Override
        public void setReplyMessageMediaIndex(long rowId, int pos) {
            replyMessageMediaIndexMap.put(rowId, pos);
        }

        @Override
        void scrollToOriginal(Message replyingMessage) {
            viewModel.getRepliedMessageRowId(replyingMessage).observe(ChatActivity.this, rowId -> {
                viewModel.reloadMessagesAt(rowId);
                int c = adapter.getItemCount();
                for (int i=0; i<c; i++) {
                    if (adapter.getItemId(i) == rowId) {
                        highlightedMessageRowId = rowId;
                        layoutManager.scrollToPosition(i);
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            });
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
        public ReactionLoader getReactionLoader() {
            return reactionLoader;
        }

        @Override
        public SystemMessageTextResolver getSystemMessageTextResolver() {
            return systemMessageTextResolver;
        }

        @Override
        public GroupLoader getGroupLoader() {
            return groupLoader;
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
        public AudioDurationLoader getAudioDurationLoader() {
            return audioDurationLoader;
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
        public LifecycleOwner getLifecycleOwner() {
            return ChatActivity.this;
        }

        @Override
        ReplyLoader getReplyLoader() {
            return replyLoader;
        }

        @Override
        public LongSparseArray<Integer> getTextLimits() {
            return textLimits;
        }

        @Override
        void addToContacts() {
            ChatActivity.this.addToContacts();
        }

        @Override
        LiveData<Contact> getContactLiveData() {
            return viewModel.contact.getLiveData();
        }

        @Override
        LiveData<String> getPhoneLiveData() {
            return viewModel.phone;
        }

        @Override
        FragmentManager getSupportFragmentManager() {
            return ChatActivity.this.getSupportFragmentManager();
        }

        @Override
        void forwardMessage(@NonNull Message message) {
            viewModel.selectMessageRowId(message);
            Intent forwardIntent = new Intent(ChatActivity.this, ForwardActivity.class);
            startActivityForResult(forwardIntent, REQUEST_CODE_FORWARD_SELECTION);
        }
    };

    private void addToContacts() {
        startActivity(IntentUtils.createContactIntent(viewModel.contact.getLiveData().getValue(), viewModel.phone.getValue()));
    }
}
