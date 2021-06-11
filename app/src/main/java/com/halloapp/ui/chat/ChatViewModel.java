package com.halloapp.ui.chat;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Chat;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.MessagesDataSource;
import com.halloapp.content.Post;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.media.VoiceNoteRecorder;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.props.ServerProps;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DelayedProgressLiveData;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.ChatState;
import com.halloapp.xmpp.Connection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatViewModel extends AndroidViewModel {

    private final ChatId chatId;
    private UserId replySenderId;
    private Long replyMessageRowId;
    private String replyPostId;

    final LiveData<PagedList<Message>> messageList;
    final ComputableLiveData<Contact> contact;
    final ComputableLiveData<String> name;
    final ComputableLiveData<Chat> chat;
    final ComputableLiveData<Reply> reply;
    final ComputableLiveData<List<Contact>> mentionableContacts;
    private ComputableLiveData<List<UserId>> blockListLiveData;
    final MutableLiveData<Boolean> deleted = new MutableLiveData<>(false);

    private final Me me;
    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final Connection connection;
    private final ContactsDb contactsDb;
    private final ServerProps serverProps;
    private final AtomicInteger outgoingAddedCount = new AtomicInteger(0);
    private final AtomicInteger incomingAddedCount = new AtomicInteger(0);
    private final AtomicInteger initialUnseen = new AtomicInteger(0);
    private final MessagesDataSource.Factory dataSourceFactory;
    private final BlockListManager blockListManager;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private VoiceNoteRecorder voiceNoteRecorder;
    private VoiceNotePlayer voiceNotePlayer;

    private final BlockListManager.Observer blockListObserver = () -> {
        blockListLiveData.invalidate();
    };

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onMessageAdded(@NonNull Message message) {
            if (ChatViewModel.this.chatId.equals(message.chatId)) {
                if (message.isOutgoing()) {
                    outgoingAddedCount.incrementAndGet();
                    mainHandler.post(() -> reloadMessagesAt(Long.MAX_VALUE));
                } else {
                    incomingAddedCount.incrementAndGet();
                    invalidateMessages();
                }
            }
        }

        public void onMessageRetracted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
            if (ChatViewModel.this.chatId.equals(chatId)) {
                invalidateMessages();
            }
        }

        @Override
        public void onMessageDeleted(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
            if (ChatViewModel.this.chatId.equals(chatId)) {
                invalidateMessages();
            }
        }

        public void onMessageUpdated(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {
            if (ChatViewModel.this.chatId.equals(chatId)) {
                invalidateMessages();
            }
        }

        public void onOutgoingMessageDelivered(@NonNull ChatId chatId, @NonNull UserId recipientUserId, @NonNull String messageId) {
            if (ChatViewModel.this.chatId.equals(chatId)) {
                invalidateMessages();
            }
        }

        public void onOutgoingMessageSeen(@NonNull ChatId chatId, @NonNull UserId seenByUserId, @NonNull String messageId) {
            if (ChatViewModel.this.chatId.equals(chatId)) {
                invalidateMessages();
            }
        }

        public void onChatDeleted(@NonNull ChatId chatId) {
            if (ChatViewModel.this.chatId.equals(chatId)) {
                deleted.postValue(true);
                invalidateMessages();
            }
        }

        @Override
        public void onDbCreated() {
        }

        private void invalidateMessages() {
            mainHandler.post(dataSourceFactory::invalidateLatestDataSource);
        }
    };

    public ChatViewModel(@NonNull Application application, @NonNull ChatId chatId, @Nullable UserId initialReplySenderId, @Nullable String initialReplyPostId) {
        super(application);

        this.chatId = chatId;
        this.replySenderId = initialReplySenderId;
        this.replyPostId = initialReplyPostId;

        me = Me.getInstance();
        bgWorkers = BgWorkers.getInstance();
        contentDb = ContentDb.getInstance();
        contentDb.addObserver(contentObserver);
        connection = Connection.getInstance();
        contactsDb = ContactsDb.getInstance();
        serverProps = ServerProps.getInstance();

        blockListManager = BlockListManager.getInstance();

        dataSourceFactory = new MessagesDataSource.Factory(contentDb, chatId);
        messageList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();

        contact = new ComputableLiveData<Contact>() {
            @Override
            protected Contact compute() {
                return ContactsDb.getInstance().getContact((UserId)chatId);
            }
        };

        name = new ComputableLiveData<String>() {
            @Override
            protected String compute() {
                ContactsDb contactsDb = ContactsDb.getInstance();
                Contact contact = contactsDb.getContact((UserId)chatId);
                String phone = TextUtils.isEmpty(contact.addressBookName) ? contactsDb.readPhone((UserId)chatId) : null;
                String normalizedPhone = phone == null ? null : PhoneNumberUtils.formatNumber("+" + phone, null);
                return TextUtils.isEmpty(contact.addressBookName) ? normalizedPhone : contact.getDisplayName();
            }
        };

        chat = new ComputableLiveData<Chat>() {
            @Override
            protected Chat compute() {
                final Chat chat = contentDb.getChat(chatId);
                initialUnseen.set(chat != null ? chat.newMessageCount : 0);
                incomingAddedCount.set(0);
                if (chat == null && chatId instanceof UserId) {
                    contactsDb.markContactSeen((UserId) chatId);
                }
                return chat;
            }
        };

        mentionableContacts = new ComputableLiveData<List<Contact>>() {
            @Override
            protected List<Contact> compute() {
                if (!(chatId instanceof GroupId)) {
                    return null;
                }
                List<MemberInfo> members = contentDb.getGroupMembers((GroupId) chatId);
                List<Contact> contacts = new ArrayList<>();
                for (MemberInfo memberInfo : members) {
                    if (memberInfo.userId.rawId().equals(me.getUser())) {
                        continue;
                    }
                    contacts.add(contactsDb.getContact(memberInfo.userId));
                }
                return Contact.sort(contacts);
            }
        };

        reply = new ComputableLiveData<Reply>() {
            @Override
            protected Reply compute() {
                if (replyPostId != null) {
                    Post post = contentDb.getPost(replyPostId);
                    if (post != null) {
                        String name = getName(replySenderId);
                        if (name != null) {
                            return new Reply(post, name);
                        }
                    }
                }

                if (replyMessageRowId != null) {
                    Message message = contentDb.getMessage(replyMessageRowId);
                    if (message != null) {
                        String name = getName(message.senderUserId);
                        if (name != null) {
                            return new Reply(message, name);
                        }
                    }
                }

                return null;
            }

            private String getName(UserId sender) {
                if (sender == null) {
                    return null;
                }
                if (sender.isMe()) {
                    return me.getName();
                }
                return contactsDb.getContact(sender).getDisplayName();
            }
        };

        blockListLiveData = new ComputableLiveData<List<UserId>>() {
            @Override
            protected List<UserId> compute() {
                return blockListManager.getBlockList();
            }
        };
        blockListManager.addObserver(blockListObserver);

        resetComposingRunnable = () -> {
            connection.updateChatState(chatId, ChatState.Type.AVAILABLE);
            lastUpdateTime = 0;
        };

        voiceNoteRecorder = new VoiceNoteRecorder();
        voiceNotePlayer = new VoiceNotePlayer();
    }

    private long lastUpdateTime;

    private final Runnable resetComposingRunnable;

    public void onComposingMessage() {
        mainHandler.removeCallbacks(resetComposingRunnable);
        if (System.currentTimeMillis() > lastUpdateTime + 10_000) {
            connection.updateChatState(chatId, ChatState.Type.TYPING);
            lastUpdateTime = System.currentTimeMillis();
        }
        mainHandler.postDelayed(resetComposingRunnable, 3000);
    }

    public LiveData<Boolean> isRecording() {
        return voiceNoteRecorder.isRecording();
    }

    public LiveData<Long> getRecordingTime() {
        return voiceNoteRecorder.getRecordingTime();
    }

    public void startRecording() {
        voiceNoteRecorder.record();
    }

    public void finishRecording(int replyPostMediaIndex, boolean canceled) {
        File recording = voiceNoteRecorder.finishRecording();
        if (canceled) {
            return;
        }
        bgWorkers.execute(() -> {
            final File targetFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_AUDIO));
            if (!recording.renameTo(targetFile)) {
                Log.e("failed to rename " + recording.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
                return;
            }
            final ContentItem contentItem = new Message(0, chatId, UserId.ME, RandomId.create(), System.currentTimeMillis(), Message.TYPE_VOICE_NOTE, Message.USAGE_CHAT, Message.STATE_INITIAL, null, replyPostId, replyPostMediaIndex, null, -1, null, 0);

            final Media sendMedia = Media.createFromFile(Media.MEDIA_TYPE_AUDIO, targetFile);
            contentItem.media.add(sendMedia);
            contentItem.addToStorage(contentDb);
        });
    }

    public void updateMessageRowId(long rowId) {
        clearReplyInfo();
        replyMessageRowId = rowId;
        reply.invalidate();
    }

    private void clearReplyInfo() {
        replySenderId = null;
        replyMessageRowId = null;
        replyPostId = null;
    }

    @NonNull
    public LiveData<List<UserId>> getBlockList() {
        return blockListLiveData.getLiveData()  ;
    }

    @MainThread
    public LiveData<Boolean> unblockContact(@NonNull UserId userId) {
        MutableLiveData<Boolean> unblockResult = new DelayedProgressLiveData<>();
        blockListManager.unblockContact(userId).onResponse(result -> {
            if (result == null || !result) {
                unblockResult.postValue(false);
            } else {
                unblockResult.postValue(true);
                connection.subscribePresence(userId);
            }
        }).onError(e -> {
            unblockResult.postValue(false);
        });
        return unblockResult;
    }

    @MainThread
    public LiveData<Boolean> blockContact(@NonNull UserId userId) {
        MutableLiveData<Boolean> blockResult = new DelayedProgressLiveData<>();
        blockListManager.blockContact(userId).onResponse(result -> {
            if (result == null || !result) {
                blockResult.postValue(false);
            } else {
                blockResult.postValue(true);
            }
        }).onError(e -> {
            blockResult.postValue(false);
        });
        return blockResult;
    }

    public void sendSystemMessage(@Message.Usage int usage, ChatId chatId) {
        final Message message = new Message(0,
                chatId,
                UserId.ME,
                RandomId.create(),
                System.currentTimeMillis(),
                Message.TYPE_SYSTEM,
                usage,
                Message.STATE_OUTGOING_DELIVERED,
                null,
                null,
                -1,
                null,
                -1,
                null,
                0);
        message.addToStorage(contentDb);
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
        blockListManager.removeObserver(blockListObserver);
        mainHandler.removeCallbacks(resetComposingRunnable);
        resetComposingRunnable.run();
        voiceNotePlayer.onCleared();
    }

    int getOutgoingAdded() {
        return outgoingAddedCount.get();
    }

    int getIncomingAdded() {
        return incomingAddedCount.get();
    }

    int getInitialUnseen() {
        return initialUnseen.get();
    }

    public VoiceNotePlayer getVoiceNotePlayer() {
        return voiceNotePlayer;
    }

    void reloadMessagesAt(long rowId) {
        final PagedList pagedList = messageList.getValue();
        if (pagedList != null) {
            ((MessagesDataSource)pagedList.getDataSource()).reloadAt(rowId);
        }
    }

    public LiveData<Long> getRepliedMessageRowId(Message replyingMessage) {
        MutableLiveData<Long> rowIdLiveData = new MutableLiveData<>();
        bgWorkers.execute(() -> {
            Message originalMessage = ContentDb.getInstance().getMessage(replyingMessage.chatId, replyingMessage.replyMessageSenderId, replyingMessage.replyMessageId);
            if (originalMessage != null) {
                rowIdLiveData.postValue(originalMessage.rowId);
            }
        });
        return rowIdLiveData;
    }

    public static class Reply {
        final Post post;
        final Message message;
        final String name;

        public Reply(@NonNull Post post, @NonNull String name) {
            this.post = post;
            this.message = null;
            this.name = name;
        }

        public Reply(@NonNull Message message, @NonNull String name) {
            this.post = null;
            this.message = message;
            this.name = name;
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final ChatId chatId;
        private final String replyPostId;
        private final UserId replySenderId;

        Factory(@NonNull Application application, @NonNull ChatId chatId, @Nullable UserId replySenderId, @Nullable String replyPostId) {
            this.application = application;
            this.chatId = chatId;
            this.replyPostId = replyPostId;
            this.replySenderId = replySenderId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ChatViewModel.class)) {
                //noinspection unchecked
                return (T) new ChatViewModel(application, chatId, replySenderId, replyPostId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
