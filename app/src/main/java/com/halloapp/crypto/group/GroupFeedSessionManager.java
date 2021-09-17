package com.halloapp.crypto.group;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.crypto.AutoCloseLock;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GroupFeedSessionManager {

    private final Connection connection;
    private final GroupFeedCipher groupFeedCipher;
    private final GroupFeedKeyManager groupFeedKeyManager;
    private final ConcurrentMap<Pair<GroupId, UserId>, AutoCloseLock> lockMap = new ConcurrentHashMap<>();

    private static GroupFeedSessionManager instance = null;

    public static GroupFeedSessionManager getInstance() {
        if (instance == null) {
            synchronized (SignalSessionManager.class) {
                if (instance == null) {
                    instance = new GroupFeedSessionManager(Connection.getInstance(), GroupFeedCipher.getInstance(), GroupFeedKeyManager.getInstance());
                }
            }
        }
        return instance;
    }

    private GroupFeedSessionManager(Connection connection, GroupFeedCipher groupFeedCipher, GroupFeedKeyManager groupFeedKeyManager) {
        this.connection = connection;
        this.groupFeedCipher = groupFeedCipher;
        this.groupFeedKeyManager = groupFeedKeyManager;
    }

    // Should be used in a try-with-resources block for auto-release
    private AutoCloseLock acquireLock(@NonNull GroupId groupId, @Nullable UserId userId) throws InterruptedException {
        Pair<GroupId, UserId> key = new Pair<>(groupId, userId);
        lockMap.putIfAbsent(key, new AutoCloseLock());
        return Preconditions.checkNotNull(lockMap.get(key)).lock();
    }

    public byte[] encryptMessage(@NonNull byte[] message, @NonNull GroupId groupId) throws CryptoException {
        try (AutoCloseLock autoCloseLock = acquireLock(groupId, null)) {
            return groupFeedCipher.convertForWire(message, groupId);
        } catch (InterruptedException e) {
            throw new CryptoException("group_enc_interrupted", e);
        }
    }

    public byte[] decryptMessage(@NonNull byte[] message, @NonNull GroupId groupId, @NonNull UserId peerUserId) throws CryptoException {
        try (AutoCloseLock autoCloseLock = acquireLock(groupId, peerUserId)) {
            return groupFeedCipher.convertFromWire(message, groupId, peerUserId);
        } catch (InterruptedException e) {
            throw new CryptoException("group_dec_interrupted", e);
        }
    }

    public GroupSetupInfo ensureGroupSetUp(GroupId groupId) throws CryptoException, NoSuchAlgorithmException {
        try (AutoCloseLock autoCloseLock = acquireLock(groupId, null)) {
            return groupFeedKeyManager.ensureGroupSetUp(groupId);
        } catch (InterruptedException e) {
            throw new CryptoException("group_setup_interrupted", e);
        }
    }

    public void tearDownOutboundSession(GroupId groupId) {
        try (AutoCloseLock autoCloseLock = acquireLock(groupId, null)) {
            groupFeedKeyManager.tearDownOutboundSession(groupId);
        } catch (InterruptedException e) {
            Log.e("Group session teardown interrupted", e);
            Log.sendErrorReport("Group teardown interrupted");
        }
    }

    public void sendPostRerequest(@NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String postId, boolean senderStateIssue) {
        connection.sendGroupPostRerequest(senderUserId, groupId, postId, senderStateIssue);
    }

    public void sendCommentRerequest(@NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String commentId, boolean senderStateIssue) {
        connection.sendGroupPostRerequest(senderUserId, groupId, commentId, senderStateIssue);
    }
}
