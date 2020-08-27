package com.halloapp.xmpp;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.ForegroundChat;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.privacy.BlockListManager;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PresenceLoader {

    private static PresenceLoader instance;

    private final Connection connection;
    private final ContactsDb contactsDb;
    private final ForegroundChat foregroundChat;
    private final BlockListManager blockListManager;
    private final Map<UserId, MutableLiveData<PresenceState>> map = new HashMap<>();
    private final Map<GroupId, MutableLiveData<GroupChatState>> groupChatStateMap = new HashMap<>();

    private Map<UserId, ChatState> chatStateMap = new HashMap<>();

    public static PresenceLoader getInstance() {
        if (instance == null) {
            synchronized (PresenceLoader.class) {
                if (instance == null) {
                    instance = new PresenceLoader(
                            Connection.getInstance(),
                            ContactsDb.getInstance(),
                            ForegroundChat.getInstance(),
                            BlockListManager.getInstance());
                }
            }
        }
        return instance;
    }

    private PresenceLoader(
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
    }

    public LiveData<PresenceState> getLastSeenLiveData(UserId userId) {
        MutableLiveData<PresenceState> mld = map.get(userId);
        if (mld == null) {
            mld = new MutableLiveData<>();
            map.put(userId, mld);
            connection.subscribePresence(userId);
        }
        return mld;
    }

    public LiveData<GroupChatState> getChatStateLiveData(GroupId groupId) {
        MutableLiveData<GroupChatState> mld = groupChatStateMap.get(groupId);
        if (mld == null) {
            mld = new MutableLiveData<>();
            groupChatStateMap.put(groupId, mld);
        }
        return mld;
    }

    public void reportChatState(UserId userId, ChatState chatState) {
        chatStateMap.put(userId, chatState);
        if (chatState.chatId instanceof UserId){
            MutableLiveData<PresenceState> mld = map.get(userId);
            if (mld == null) {
                return;
            }
            if (chatState.type == ChatState.Type.AVAILABLE) {
                mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_ONLINE));
            } else {
                mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_TYPING));
            }
        } else if (chatState.chatId instanceof GroupId){
            GroupId groupId = (GroupId) chatState.chatId;
            MutableLiveData<GroupChatState> mld = groupChatStateMap.get(groupId);
            if (mld == null) {
                return;
            }
            GroupChatState groupChatState = mld.getValue();
            if (groupChatState == null) {
                groupChatState = new GroupChatState();
            }
            if (chatState.type == ChatState.Type.AVAILABLE) {
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
        if (lastSeen != null) {
            chatStateMap.remove(userId);
            mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_OFFLINE, lastSeen * 1000L));
        } else {
            ChatState chatState = chatStateMap.get(userId);
            if (chatState == null || chatState.type == ChatState.Type.AVAILABLE) {
                mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_ONLINE));
            } else {
                mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_TYPING));
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

    public void onDisconnect() {
        Log.d("PresenceLoader marking all as unknown");
        for (UserId userId : map.keySet()) {
            MutableLiveData<PresenceState> mld = Preconditions.checkNotNull(map.get(userId));
            mld.postValue(new PresenceState(PresenceState.PRESENCE_STATE_UNKNOWN));
        }

        groupChatStateMap.clear();
    }

    public void onReconnect() {
        Log.d("PresenceLoader resetting subscriptions");
        UserId keepUserId = null;
        MutableLiveData<PresenceState> keepMld = null;
        for (UserId userId : map.keySet()) {
            MutableLiveData<PresenceState> mld = Preconditions.checkNotNull(map.get(userId));
            if (foregroundChat.isForegroundChatId(userId)) {
                keepUserId = userId;
                keepMld = mld;
                break;
            }
        }
        map.clear();
        if (keepUserId != null) {
            Log.d("PresenceLoader maintaining subscription to " + keepUserId);
            map.put(keepUserId, keepMld);
            connection.subscribePresence(keepUserId);
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
