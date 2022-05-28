package com.halloapp.crypto.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.crypto.AutoCloseLock;
import com.halloapp.crypto.CryptoException;
import com.halloapp.crypto.signal.SignalSessionManager;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.SenderState;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HomeFeedSessionManager {

    private final Connection connection;
    private final HomeFeedPostCipher homeFeedPostCipher;
    private final HomeFeedPostKeyManager homeFeedPostKeyManager;
    private final ConcurrentMap<UserId, AutoCloseLock> lockMap = new ConcurrentHashMap<>();

    private static HomeFeedSessionManager instance = null;

    public static HomeFeedSessionManager getInstance() {
        if (instance == null) {
            synchronized (SignalSessionManager.class) {
                if (instance == null) {
                    instance = new HomeFeedSessionManager(Connection.getInstance(), HomeFeedPostCipher.getInstance(), HomeFeedPostKeyManager.getInstance());
                }
            }
        }
        return instance;
    }

    private HomeFeedSessionManager(Connection connection, HomeFeedPostCipher homeFeedPostCipher, HomeFeedPostKeyManager homeFeedPostKeyManager) {
        this.connection = connection;
        this.homeFeedPostCipher = homeFeedPostCipher;
        this.homeFeedPostKeyManager = homeFeedPostKeyManager;
    }

    // Should be used in a try-with-resources block for auto-release
    private AutoCloseLock acquireLock(@Nullable UserId userId) throws InterruptedException {
        UserId key = userId == null ? UserId.ME : userId;
        lockMap.putIfAbsent(key, new AutoCloseLock());
        return Preconditions.checkNotNull(lockMap.get(key)).lock();
    }

    // TODO(jack): Make encrypt and decrypt functions for comments
    public byte[] encryptPost(@NonNull byte[] postBytes, boolean favorites) throws CryptoException {
        try (AutoCloseLock autoCloseLock = acquireLock(null)) {
            return homeFeedPostCipher.convertForWire(postBytes, favorites);
        } catch (InterruptedException e) {
            throw new CryptoException("home_enc_interrupted", e);
        }
    }

    public byte[] decryptPost(@NonNull byte[] postBytes, boolean favorites, @NonNull UserId peerUserId) throws CryptoException {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            return homeFeedPostCipher.convertFromWire(postBytes, favorites, peerUserId);
        } catch (InterruptedException e) {
            throw new CryptoException("home_dec_interrupted", e);
        }
    }

    public HomePostSetupInfo ensureSetUp(boolean favorites) throws CryptoException {
        try (AutoCloseLock autoCloseLock = acquireLock(null)) {
            return homeFeedPostKeyManager.ensureSetUp(favorites);
        } catch (InterruptedException e) {
            throw new CryptoException("home_setup_interrupted", e);
        }
    }

    public void tearDownOutboundSession(boolean favorites) {
        try (AutoCloseLock autoCloseLock = acquireLock(null)) {
            homeFeedPostKeyManager.tearDownOutboundSession(favorites);
        } catch (InterruptedException e) {
            Log.e("Home session teardown interrupted", e);
            Log.sendErrorReport("Home teardown interrupted");
        }
    }

    public void tearDownInboundSession(boolean favorites, @NonNull UserId peerUserId) {
        try (AutoCloseLock autoCloseLock = acquireLock(peerUserId)) {
            homeFeedPostKeyManager.tearDownInboundSession(favorites, peerUserId);
        } catch (InterruptedException e) {
            Log.e("Home session teardown interrupted", e);
            Log.sendErrorReport("Home teardown interrupted");
        }
    }

    public void sendPostRerequest(@NonNull UserId senderUserId, boolean favorites, @NonNull String postId, boolean senderStateIssue) {
        connection.sendHomePostRerequest(senderUserId, favorites, postId, senderStateIssue);
    }

    // TODO(jack): Implement comment rerequests
//    public void sendCommentRerequest(@NonNull UserId senderUserId, @NonNull GroupId groupId, @NonNull String commentId, boolean senderStateIssue) {
//        connection.sendGroupCommentRerequest(senderUserId, groupId, commentId, senderStateIssue);
//    }

    public SenderState getSenderState(boolean favorites) throws CryptoException {
        try (AutoCloseLock autoCloseLock = acquireLock(null)) {
            return homeFeedPostKeyManager.getSenderState(favorites);
        } catch (InterruptedException e) {
            throw new CryptoException("home_enc_interrupted", e);
        }
    }
}
