package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.halloapp.content.Comment;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.crypto.SessionSetupInfo;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.stats.Stats;
import com.halloapp.xmpp.util.Observable;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class NewConnection extends Connection {
    @Override
    public void connect() {

    }

    @Nullable
    @Override
    public String getConnectionPropHash() {
        return null;
    }

    @Override
    public void clientExpired() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void requestServerProps() {

    }

    @Override
    public Future<Integer> requestSecondsToExpiration() {
        return null;
    }

    @Override
    public Future<MediaUploadIq.Urls> requestMediaUpload(long fileSize) {
        return null;
    }

    @Override
    public Future<List<ContactInfo>> syncContacts(@Nullable Collection<String> addPhones, @Nullable Collection<String> deletePhones, boolean fullSync, @Nullable String syncId, int index, boolean lastBatch) {
        return null;
    }

    @Override
    public void sendPushToken(@NonNull String pushToken) {

    }

    @Override
    public Future<Boolean> sendName(@NonNull String name) {
        return null;
    }

    @Override
    public void subscribePresence(UserId userId) {

    }

    @Override
    public void updatePresence(boolean available) {

    }

    @Override
    public void updateChatState(@NonNull ChatId chat, int state) {

    }

    @Override
    public Future<Boolean> uploadKeys(@Nullable byte[] identityKey, @Nullable byte[] signedPreKey, @NonNull List<byte[]> oneTimePreKeys) {
        return null;
    }

    @Override
    public void uploadMoreOneTimePreKeys(@NonNull List<byte[]> oneTimePreKeys) {

    }

    @Override
    public Future<WhisperKeysResponseIq> downloadKeys(@NonNull UserId userId) {
        return null;
    }

    @Override
    public Future<Integer> getOneTimeKeyCount() {
        return null;
    }

    @Override
    public Future<Void> sendStats(List<Stats.Counter> counters) {
        return null;
    }

    @Override
    public Future<String> setAvatar(String base64, long numBytes, int width, int height) {
        return null;
    }

    @Override
    public Future<String> setGroupAvatar(GroupId groupId, String base64) {
        return null;
    }

    @Override
    public Future<String> getAvatarId(UserId userId) {
        return null;
    }

    @Override
    public Future<String> getMyAvatarId() {
        return null;
    }

    @Override
    public Future<Boolean> sharePosts(Map<UserId, Collection<Post>> shareMap) {
        return null;
    }

    @Override
    public void sendPost(@NonNull Post post) {

    }

    @Override
    public void retractPost(@NonNull String postId) {

    }

    @Override
    public void retractGroupPost(@NonNull GroupId groupId, @NonNull String postId) {

    }

    @Override
    public void sendComment(@NonNull Comment comment) {

    }

    @Override
    public void retractComment(@Nullable UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {

    }

    @Override
    public void retractGroupComment(@NonNull GroupId groupId, @NonNull UserId postSenderUserId, @NonNull String postId, @NonNull String commentId) {

    }

    @Override
    public void sendMessage(@NonNull Message message, @Nullable SessionSetupInfo sessionSetupInfo) {

    }

    @Override
    public void sendGroupMessage(@NonNull Message message, @Nullable SessionSetupInfo sessionSetupInfo) {

    }

    @Override
    public <T extends IQ> Observable<T> sendRequestIq(@NonNull IQ iq) {
        return null;
    }

    @Override
    public void sendRerequest(String encodedIdentityKey, @NonNull Jid originalSender, @NonNull String messageId) {

    }

    @Override
    public void sendAck(@NonNull String id) {

    }

    @Override
    public void sendPostSeenReceipt(@NonNull UserId senderUserId, @NonNull String postId) {

    }

    @Override
    public void sendMessageSeenReceipt(@NonNull ChatId chatId, @NonNull UserId senderUserId, @NonNull String messageId) {

    }
    
    @Override
    public UserId getUserId(@NonNull String user) {
        return null;
    }

    @Override
    public boolean getClientExpired() {
        return false;
    }
}
