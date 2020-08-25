package com.halloapp.xmpp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.util.Log;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jxmpp.jid.Jid;

public class ChatStateStanza extends Stanza {

    private static final String ELEMENT = "chat_state";

    public final String type;
    public final String threadId;
    public final String threadType;

    ChatStateStanza(@NonNull Jid to, @NonNull String type, @NonNull ChatId chatId) {
        setTo(to);
        this.type = type;
        threadId = chatId.rawId();
        if (chatId instanceof UserId) {
            threadType = "chat";
        } else if (chatId instanceof GroupId) {
            threadType = "group_chat";
        } else {
            threadType = "chat";
            Log.e("ChatStateStanza invalid type of chat id for chat state");
        }
    }

    ChatStateStanza(@NonNull Jid to, @NonNull String id, @NonNull String type, @NonNull String threadId, @NonNull String threadType) {
        setTo(to);
        setStanzaId(id);
        this.type = type;
        this.threadId = threadId;
        this.threadType = threadType;
    }

    @Override
    public XmlStringBuilder toXML(String enclosingNamespace) {
        final XmlStringBuilder buf = new XmlStringBuilder(enclosingNamespace);
        buf.halfOpenElement(ELEMENT);
        addCommonAttributes(buf, enclosingNamespace);
        buf.attribute("type", type);
        buf.attribute("thread_id", threadId);
        buf.attribute("thread_type", threadType);
        buf.closeEmptyElement();
        return buf;
    }

    @Override
    public @NonNull String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Presence Stanza [");
        logCommonAttributes(sb);
        sb.append("type=").append(type);
        sb.append(']');
        return sb.toString();
    }
}
