package com.halloapp.xmpp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.ForegroundChat;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.util.logs.Log;
import com.halloapp.util.Preconditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PresenceManager {

    private static final int MSG_CLEAR_TYPING = 1;
    private static final int TYPING_CACHE_DELAY = 25_000;

    private static PresenceManager instance;

    private final Connection connection;
    private final ContactsDb contactsDb;
    private final ForegroundChat foregroundChat;
    private final BlockListManager blockListManager;
    private final Map<UserId, MutableLiveData<PresenceState>> map = new HashMap<>();
    private final Set<UserId> subscriptions = new HashSet<>();
    private final Map<GroupId, MutableLiveData<GroupChatState>> groupChatStateMap = new HashMap<>();

    private final Map<UserId, ChatState> chatStateMap = new HashMap<>();

    // Rely on map because Handlers depend on simple == to compare obj
    private final Map<UserId, UserId> userIdMap = new HashMap<>();

    private final Handler chatStateUpdateHandler;

    private boolean userAvailable;

    public static PresenceManager getInstance() {
        if (instance == null) {
            synchronized (PresenceManager.class) {
                if (instance == null) {
                    instance = new PresenceManager(
                            Connection.getInstance(),
                            ContactsDb.getInstance(),
                            ForegroundChat.getInstance(),
                            BlockListManager.getInstance());
                }
            }
        }
        return instance;
    }

    private PresenceManager(
            Connection connection,
            ContactsDb contactsDb,
            ForegroundChat foregroundChat,
            BlockListManager blockListManager) {
        this.connection = connection;
        this.contactsDb = contactsDb;
        this.foregroundChat = foregroundChat;
        this.blockListManager = blockListManager;
        this.blockListManager.addObserver(() -> {
            List<UserId> blockList = blockListManager.getBlockList();
            if (blockList != null) {
                for (UserId blockedId : blockList) {
                    reportBlocked(blockedId);
                }
            }
        });
        HandlerThread chatStateThread = new HandlerThread("ChatStateHandler");
        chatStateThread.start();

        chatStateUpdateHandler = new Handler(chatStateThread.getLooper()) {

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_CLEAR_TYPING) {
                    synchronized (chatStateMap) {
                        if (userIdMap.get((UserId) msg.obj) == msg.obj) {
                            ChatState state = chatStateMap.get((UserId) msg.obj);
                            if (state != null && state.type == ChatState.Type.TYPING) {
                                reportChatState((UserId) msg.obj, new ChatState(ChatState.Type.AVAILABLE, state.chatId));
                            }
                        }
                    }
                }
            }
        };
    }

    public LiveData<PresenceState> getLastSeenLiveData(UserId userId, boolean needPresence) {
        MutableLiveData<PresenceState> mld = map.get(userId);
        if (mld == null) {
            mld = new MutableLiveData<>();
            map.put(userId, mld);
        }
        if (needPresence) {
            subscribePresence(userId);
        }
        return mld;
    }

    public LiveData<GroupChatState> getChatStateLiveData(GroupId groupId) {
        synchronized (groupChatStateMap) {
            MutableLiveData<GroupChatState> mld = groupChatStateMap.get(groupId);
            if (mld == null) {
                mld = new MutableLiveData<>();
                groupChatStateMap.put(groupId, mld);
            }
            return mld;
        }
    }

    private void clearScheduledTypingState(@NonNull UserId userId) {
        if (userIdMap.containsKey(userId)) {
            chatStateUpdateHandler.removeMessages(MSG_CLEAR_TYPING, userIdMap.remove(userId));
        }
    }

    private void scheduleTypingStateClear(@NonNull UserId userId) {
        if (userIdMap.containsKey(userId)) {
            userId = userIdMap.get(userId);
        } else {
            userIdMap.put(userId, userId);
        }
        Message msg = new Message();
        msg.what = MSG_CLEAR_TYPING;
        msg.obj = userId;
        chatStateUpdateHandler.sendMessageDelayed(msg, TYPING_CACHE_DELAY);
    }

    public void reportChatState(UserId userId, ChatState chatState) {
        synchronized (chatStateMap) {
            clearScheduledTypingState(userId);
            ChatState oldState = chatStateMap.remove(userId);
            if (oldState != null && !chatState.chatId.equals(oldState.chatId)) {
                if (oldState.type == ChatState.Type.TYPING) {
                    // Cleanup old chat state
                    updateChatState(userId, new ChatState(ChatState.Type.AVAILABLE, oldState.chatId));
                }
            }
            chatStateMap.put(userId, chatState);
            updateChatState(userId, chatState);
        }
    }

    private void subscribePresence(UserId userId) {
        synchronized (subscriptions) {
            if (subscriptions.contains(userId)) {
                return;
            }
            connection.subscribePresence(userId);
            subscriptions.add(userId);
        }
    }

    private void updateChatState(@NonNull UserId userId, ChatState chatState) {
        if (chatState.chatId instanceof UserId) {
            MutableLiveData<PresenceState> mld = map.get(userId);
            if (mld == null) {
                return;
            }
            if (chatState.type == ChatState.Type.AVAILABLE) {
                mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_ONLINE));
            } else {
                mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_TYPING));
                subscribePresence(userId);
                scheduleTypingStateClear(userId);
            }
        } else if (chatState.chatId instanceof GroupId) {
            updateGroupChatState(userId,
                    (GroupId) chatState.chatId,
                    new PresenceState(chatState.type == ChatState.Type.TYPING
                            ? PresenceState.PRESENCE_STATE_TYPING
                            : PresenceState.PRESENCE_STATE_ONLINE));
        }
    }

    private void updateGroupChatState(@NonNull UserId userId, GroupId groupId, PresenceState presenceState) {
        synchronized (groupChatStateMap) {
            MutableLiveData<GroupChatState> mld = groupChatStateMap.get(groupId);
            if (mld == null) {
                return;
            }
            GroupChatState groupChatState = mld.getValue();
            if (groupChatState == null) {
                groupChatState = new GroupChatState();
            }
            if (presenceState.state != PresenceState.PRESENCE_STATE_TYPING) {
                groupChatState.typingUsers.remove(userId);
                groupChatState.contactMap.remove(userId);
            } else {
                if (!groupChatState.typingUsers.contains(userId)) {
                    groupChatState.typingUsers.add(userId);
                    groupChatState.contactMap.put(userId, contactsDb.getContact(userId));
                }
            }
            mld.postValue(groupChatState);
        }
    }

    public void reportPresence(UserId userId, Long lastSeen) {
        MutableLiveData<PresenceState> mld = map.get(userId);
        if (mld == null) {
            Log.w("ReportPresence received unexpected presence for user " + userId);
            return;
        }
        synchronized (chatStateMap) {
            if (lastSeen != null) {
                ChatState oldState = chatStateMap.remove(userId);
                if (oldState != null && oldState.chatId instanceof GroupId) {
                    updateGroupChatState(userId, (GroupId) oldState.chatId, new PresenceState(PresenceState.PRESENCE_STATE_OFFLINE));
                }
                mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_OFFLINE, lastSeen * 1000L));
            } else {
                ChatState chatState = chatStateMap.get(userId);
                if (chatState != null && userId.equals(chatState.chatId) && chatState.type == ChatState.Type.TYPING) {
                    mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_TYPING));
                } else {
                    mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_ONLINE));
                }
            }
        }
    }

    public void reportBlocked(UserId userId) {
        MutableLiveData<PresenceState> mld = map.get(userId);
        if (mld == null) {
            Log.w("ReportBlocked received unexpected presence for user " + userId);
            return;
        }
        mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_UNKNOWN));

    }

    public void onBackground() {
        this.userAvailable = false;
        sendServerPresence();
    }

    public void setAvailable(boolean available) {
        if (this.userAvailable != available) {
            this.userAvailable = available;
            sendServerPresence();
        }
    }

    public void onDisconnect() {
        Log.d("PresenceManager marking all as unknown");
        for (UserId userId : map.keySet()) {
            MutableLiveData<PresenceState> mld = Preconditions.checkNotNull(map.get(userId));
            mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_UNKNOWN));
        }
        synchronized (groupChatStateMap) {
            for (GroupId groupId : groupChatStateMap.keySet()) {
                MutableLiveData<GroupChatState> mld = groupChatStateMap.get(groupId);
                if (mld != null) {
                    mld.postValue(new GroupChatState());
                }
            }
        }
    }

    private void sendServerPresence() {
        connection.updatePresence(userAvailable);
    }

    public void onReconnect() {
        sendServerPresence();

        Log.d("PresenceManager resetting subscriptions");
        UserId keepUserId = null;
        for (UserId userId : subscriptions) {
            if (foregroundChat.isForegroundChatId(userId)) {
                keepUserId = userId;
                break;
            }
        }
        subscriptions.clear();
        if (keepUserId != null) {
            Log.d("PresenceManager maintaining subscription to " + keepUserId);
            subscribePresence(keepUserId);
        }
    }

    public static class GroupChatState {
        public final List<UserId> typingUsers;
        public final Map<UserId, Contact> contactMap;

        private GroupChatState() {
            typingUsers = new ArrayList<>();
            contactMap = new HashMap<>();
        }
    }

    public static class PresenceState {
        public static final int PRESENCE_STATE_UNKNOWN = 0;
        public static final int PRESENCE_STATE_ONLINE = 1;
        public static final int PRESENCE_STATE_OFFLINE = 2;
        public static final int PRESENCE_STATE_TYPING = 3;

        public final int state;
        public final long lastSeen;

        private PresenceState(int state) {
            this.state = state;
            this.lastSeen = 0;
        }

        private PresenceState(int state, long lastSeen) {
            this.state = state;
            this.lastSeen = lastSeen;
        }
    }
}
