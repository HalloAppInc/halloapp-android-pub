package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.proto.server.ChatState;
import com.halloapp.util.logs.Log;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.util.XmlStringBuilder;

import java.util.Locale;

public class ChatStateStanza extends Stanza {

    private static final String ELEMENT = "chat_state";

    public final String type;
    public final String threadId;
    public final String threadType;

    ChatStateStanza(@NonNull String type, @NonNull ChatId chatId) {
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

    ChatStateStanza(@NonNull String id, @NonNull String type, @NonNull String threadId, @NonNull String threadType) {
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

    public ChatState toProto() {
        return ChatState.newBuilder()
                .setType(ChatState.Type.valueOf(type.toUpperCase(Locale.US)))
                .setThreadId(threadId)
                .setThreadType(ChatState.ThreadType.valueOf(threadType.toUpperCase(Locale.US)))
                .build();
    }
}
