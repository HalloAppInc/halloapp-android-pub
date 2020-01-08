package com.halloapp;

import androidx.annotation.NonNull;

import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;

import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;

import java.util.Arrays;
import java.util.List;

public class ConnectionObserver implements Connection.Observer {

    private final PostsDb postsDb;

    ConnectionObserver(PostsDb postsDb) {
        this.postsDb = postsDb;
    }

    @Override
    public void onConnected() {
        final List<Jid> contacts = Arrays.asList(
                //JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("13477521636"), Domainpart.fromOrNull(Connection.XMPP_DOMAIN)), // duygu
                //JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("14703381473"), Domainpart.fromOrNull(Connection.XMPP_DOMAIN)), // murali
                //JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("14154121848"), Domainpart.fromOrNull(Connection.XMPP_DOMAIN)), // michael
                //JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("14088922686"), Domainpart.fromOrNull(Connection.XMPP_DOMAIN)), // tony
                JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("16502752675"), Domainpart.fromOrNull(Connection.XMPP_DOMAIN)),
                JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("16502813677"), Domainpart.fromOrNull(Connection.XMPP_DOMAIN))
        );
        Connection.getInstance(this).syncPubSub(contacts);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLoginFailed() {
        HalloApp.instance.resetRegistration();
    }

    @Override
    public void onOutgoingPostAcked(@NonNull String chatJid, @NonNull String postId) {
        postsDb.setPostState(chatJid, "", postId, Post.POST_STATE_OUTGOING_SENT);
    }

    @Override
    public void onOutgoingPostDelivered(@NonNull String chatJid, @NonNull String postId) {
        postsDb.setPostState(chatJid, "", postId, Post.POST_STATE_OUTGOING_DELIVERED);
    }

    @Override
    public void onIncomingPostReceived(@NonNull Post post) {
        if (post.type == Post.POST_TYPE_TEXT) {
            post.state = Post.POST_STATE_INCOMING_RECEIVED;
        }
        postsDb.addPost(post);
    }
}
