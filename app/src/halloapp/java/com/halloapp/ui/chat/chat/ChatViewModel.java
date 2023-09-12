package com.halloapp.ui.chat.chat;

import android.annotation.SuppressLint;
import android.app.Application;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Base64;

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

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.Me;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Chat;
import com.halloapp.content.ContactMessage;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.DocumentMessage;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.MessagesDataSource;
import com.halloapp.content.Post;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaUtils;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.media.VoiceNoteRecorder;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.ui.share.ShareDestination;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.DelayedProgressLiveData;
import com.halloapp.util.FileUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.ChatState;
import com.halloapp.xmpp.Connection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatViewModel extends AndroidViewModel {

    private final ChatId chatId;
    private UserId replySenderId;
    private Long replyMessageRowId;
    private String replyPostId;

    final LiveData<PagedList<Message>> messageList;
    final ComputableLiveData<Contact> contact;
    final ComputableLiveData<String> name;
    final MutableLiveData<String> phone = new MutableLiveData<>();
    final ComputableLiveData<Chat> chat;
    final ComputableLiveData<Reply> reply;
    final ComputableLiveData<List<Contact>> mentionableContacts;
    private ComputableLiveData<List<UserId>> blockListLiveData;
    final MutableLiveData<Boolean> deleted = new MutableLiveData<>(false);

    private final MutableLiveData<Message> selectedMessage = new MutableLiveData<>();
    private Long selectedMessageRowId = null;

    private final Me me;
    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final Connection connection;
    private final ContactsDb contactsDb;
    private final AtomicInteger outgoingAddedCount = new AtomicInteger(0);
    private final AtomicInteger incomingAddedCount = new AtomicInteger(0);
    private final AtomicInteger initialUnseen = new AtomicInteger(0);
    private final MessagesDataSource.Factory dataSourceFactory;
    private final BlockListManager blockListManager;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final VoiceNoteRecorder voiceNoteRecorder;
    private final VoiceNotePlayer voiceNotePlayer;

    private final BlockListManager.Observer blockListObserver = () -> {
        blockListLiveData.invalidate();
    };

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onNewContacts(@NonNull Collection<UserId> newContacts) {
            contact.invalidate();
            name.invalidate();
        }

        @Override
        public void onContactsChanged() {
            contact.invalidate();
            name.invalidate();
        }
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

        @Override
        public void onGroupMetadataChanged(@NonNull GroupId groupId) {
            if (ChatViewModel.this.chatId.equals(groupId)) {
                chat.invalidate();
            }
        }

        @Override
        public void onGroupMembersChanged(@NonNull GroupId groupId) {
            if (ChatViewModel.this.chatId.equals(groupId)) {
                chat.invalidate();
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
        contactsDb.addObserver(contactsObserver);

        blockListManager = BlockListManager.getInstance();

        dataSourceFactory = new MessagesDataSource.Factory(contentDb, chatId);
        messageList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();

        contact = new ComputableLiveData<Contact>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected Contact compute() {
                if (chatId instanceof GroupId) {
                    return null;
                }
                return ContactsDb.getInstance().getContact((UserId)chatId);
            }
        };

        name = new ComputableLiveData<String>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected String compute() {
                ContactsDb contactsDb = ContactsDb.getInstance();
                Contact contact = contactsDb.getContact((UserId)chatId);
                String phone = TextUtils.isEmpty(contact.addressBookName) ? contactsDb.readPhone((UserId)chatId) : null;
                String normalizedPhone = phone == null ? null : PhoneNumberUtils.formatNumber("+" + phone, null);
                ChatViewModel.this.phone.postValue(normalizedPhone);
                if (!TextUtils.isEmpty(contact.addressBookName)) {
                    return contact.addressBookName;
                } else if (!TextUtils.isEmpty(normalizedPhone)) {
                    return normalizedPhone;
                }
                return contact.getDisplayName();
            }
        };

        chat = new ComputableLiveData<Chat>() {
            @SuppressLint("RestrictedApi")
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
            @SuppressLint("RestrictedApi")
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
            @SuppressLint("RestrictedApi")
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
            @SuppressLint("RestrictedApi")
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
        voiceNotePlayer = new VoiceNotePlayer(application);
    }

    private long lastUpdateTime;

    private final Runnable resetComposingRunnable;

    public void loadSelectedMessage(long rowId) {
        selectedMessageRowId = rowId;
        Log.i("ChatViewModel/loadSelectedMessage loading rowId " + rowId);
        bgWorkers.execute(() -> {
            Log.i("ChatViewModel/loadedSelectedMessage loaded rowId " + rowId);
            selectedMessage.postValue(contentDb.getMessage(rowId));
        });
    }

    public LiveData<Message> getSelectedMessage() {
        return selectedMessage;
    }

    public Long getSelectedMessageRowId() {
        return selectedMessageRowId;
    }

    public void selectMessage(@Nullable Message message) {
        selectedMessageRowId = message == null ? null : message.rowId;
        selectedMessage.setValue(message);
    }

    public void selectMessageRowId(@Nullable Message message) {
        selectedMessageRowId = message == null ? null : message.rowId;
    }

    public void onComposingMessage() {
        mainHandler.removeCallbacks(resetComposingRunnable);
        if (System.currentTimeMillis() > lastUpdateTime + 10_000) {
            connection.updateChatState(chatId, ChatState.Type.TYPING);
            lastUpdateTime = System.currentTimeMillis();
        }
        mainHandler.postDelayed(resetComposingRunnable, 3000);
    }

    public VoiceNoteRecorder getVoiceNoteRecorder() {
        return voiceNoteRecorder;
    }

    public boolean checkIsRecording() {
        Boolean isRecording = voiceNoteRecorder.isRecording().getValue();
        if (isRecording == null) {
            return false;
        }
        return isRecording;
    }

    public void sendVoiceNote(int replyPostMediaIndex, int replyMessageMediaIndex, @Nullable File recording) {
        if (recording == null) {
            return;
        }
        ChatViewModel.Reply replyData = reply.getLiveData().getValue();
        final Message replyMessage = replyData == null ? null : replyData.message;
        final Post replyPost = replyData == null ? null : replyData.post;
        final String replyPostId = replyPost == null ? null : replyPost.id;
        bgWorkers.execute(() -> {
            if (MediaUtils.getAudioDuration(recording) < Constants.MINIMUM_AUDIO_NOTE_DURATION_MS) {
                Log.i("ChatViewModel/sendVoiceNote duration too short");
                return;
            }
            final File targetFile = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_AUDIO));
            if (!recording.renameTo(targetFile)) {
                Log.e("failed to rename " + recording.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
                return;
            }
            final ContentItem contentItem = new Message(0,
                    chatId,
                    UserId.ME,
                    RandomId.create(),
                    System.currentTimeMillis(),
                    Message.TYPE_VOICE_NOTE,
                    Message.USAGE_CHAT,
                    Message.STATE_INITIAL,
                    null,
                    replyPostId,
                    replyPostMediaIndex,
                    replyMessage != null ? replyMessage.id : null,
                    replyMessageMediaIndex ,
                    replyMessage != null ? replyMessage.senderUserId : replySenderId,
                    0);


            final Media sendMedia = Media.createFromFile(Media.MEDIA_TYPE_AUDIO, targetFile);
            contentItem.media.add(sendMedia);
            contentItem.addToStorage(contentDb);
        });
    }

    public void finishRecording(int replyPostMediaIndex, int replyMessageMediaIndex, boolean canceled) {
        File recording = voiceNoteRecorder.finishRecording();
        if (canceled || recording == null) {
            return;
        }
        sendVoiceNote(replyPostMediaIndex,replyMessageMediaIndex, recording);
    }

    public void sendMessage(Message message) {
        if (message.urlPreview != null && message.urlPreview.imageMedia != null) {
            bgWorkers.execute(() -> {
                if (message.urlPreview.imageMedia != null) {
                    final File imagePreview = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));
                    try {
                        MediaUtils.transcodeImage(message.urlPreview.imageMedia.file, imagePreview, null, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY, false);
                        message.urlPreview.imageMedia.file = imagePreview;
                    } catch (IOException e) {
                        Log.e("failed to transcode url preview image", e);
                        message.urlPreview.imageMedia = null;
                    }
                }
                message.addToStorage(ContentDb.getInstance());
            });
        } else {
            message.addToStorage(ContentDb.getInstance());
        }
    }

    public void sendContact(int replyPostMediaIndex, int replyMessageMediaIndex, byte[] contactCardBytes) {
        ChatViewModel.Reply replyData = reply.getLiveData().getValue();
        final Message replyMessage = replyData == null ? null : replyData.message;
        final Post replyPost = replyData == null ? null : replyData.post;
        final String replyPostId = replyPost == null ? null : replyPost.id;

        final ContactMessage contactMessage = new ContactMessage(0,
                chatId,
                UserId.ME,
                RandomId.create(),
                System.currentTimeMillis(),
                Message.USAGE_CHAT,
                Message.STATE_INITIAL,
                Base64.encodeToString(contactCardBytes, Base64.NO_WRAP),
                replyPostId,
                replyPostMediaIndex,
                replyMessage != null ? replyMessage.id : null,
                replyMessageMediaIndex ,
                replyMessage != null ? replyMessage.senderUserId : replySenderId,
                0);
        contactMessage.addToStorage(contentDb);
    }

    public Message buildMessage(String messageText, int replyPostMediaIndex, int replyMessageMediaIndex) {
        ChatViewModel.Reply replyData = reply.getLiveData().getValue();
        final Message replyMessage = replyData == null ? null : replyData.message;
        final Post replyPost = replyData == null ? null : replyData.post;
        final String replyPostId = replyPost == null ? null : replyPost.id;

        return new Message(0,
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
                replyMessage != null ? replyMessage.senderUserId : replySenderId,
                0);
    }

    public void onSendDocument(String fileName, Uri uri) {
        bgWorkers.execute(() -> {

            final boolean isLocalFile = Objects.equals(uri.getScheme(), "file");
            @Media.MediaType int mediaType = Media.MEDIA_TYPE_DOCUMENT;

            final File file = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(mediaType));
            if (!file.exists()) {
                if (isLocalFile) {
                    try {
                        File src = new File(uri.getPath());
                        FileUtils.copyFile(src, file);
                    } catch (IOException e) {
                        // Swallow the exception, the logic below will handle the case of empty file.
                        Log.e("ChatViewModel/onSendDocument copyFile " + uri, e);
                        return;
                    }
                } else {
                    FileUtils.uriToFile(getApplication(), uri, file);
                }
            }

            final ContentItem contentItem = new DocumentMessage(0,
                    chatId,
                    UserId.ME,
                    RandomId.create(),
                    System.currentTimeMillis(),
                    Message.USAGE_CHAT,
                    Message.STATE_INITIAL,
                    fileName,
                    replyPostId,
                    0,
                    null,
                    0 ,
                    replySenderId,
                    0);


            final Media sendMedia = Media.createFromFile(Media.MEDIA_TYPE_DOCUMENT, file);
            contentItem.media.add(sendMedia);
            contentItem.addToStorage(contentDb);
        });
    }

    public void updateMessageRowId(long rowId) {
        clearReplyInfo();
        replyMessageRowId = rowId;
        reply.invalidate();
    }

    public void clearReply() {
        clearReplyInfo();
        reply.invalidate();
    }

    private void clearReplyInfo() {
        replySenderId = null;
        replyMessageRowId = null;
        replyPostId = null;
    }

    public String getReplyPostId() {
        return replyPostId;
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
        bgWorkers.execute(() -> {
            if (contentDb.getChat(chatId) == null) {
                Log.i("Skipping adding system message because chat " + chatId + " does not already exist");
            } else {
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
        });
    }

    @Override
    protected void onCleared() {
        contactsDb.removeObserver(contactsObserver);
        contentDb.removeObserver(contentObserver);
        blockListManager.removeObserver(blockListObserver);
        mainHandler.removeCallbacks(resetComposingRunnable);
        resetComposingRunnable.run();
        voiceNotePlayer.onCleared();
    }

    public LiveData<Boolean> forwardMessage(ArrayList<ShareDestination> destinations, long rowId) {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();
        bgWorkers.execute(() -> {
            Message message = contentDb.getMessage(rowId);
            if (message == null) {
                Log.e("ChatActivity/forward message message is null");
                return;
            }
            for (ShareDestination dest : destinations) {
                Message forward = new Message(0,
                        dest.id,
                        UserId.ME,
                        RandomId.create(),
                        System.currentTimeMillis(),
                        message.type,
                        message.usage,
                        Message.STATE_INITIAL,
                        message.text,
                        null,
                        0,
                        null,
                        -1,
                        null, 0);
                for (Media media : message.media) {
                    File src = media.file;
                    File dst = FileStore.getInstance().getMediaFile(RandomId.create() + "." + Media.getFileExt(media.type));
                    try {
                        FileUtils.copyFile(src, dst);
                    } catch (IOException e) {
                        Log.e("ChatViewModel/forwardMessage failed to copy file", e);
                    }
                    forward.media.add(Media.createFromFile(media.type, dst));
                }
                forward.addToStorage(contentDb);
            }
            resultLiveData.postValue(true);
        });
        return resultLiveData;
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
        final PagedList<Message> pagedList = messageList.getValue();
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
