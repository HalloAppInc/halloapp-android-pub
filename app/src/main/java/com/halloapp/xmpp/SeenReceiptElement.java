package com.halloapp.xmpp;

import com.halloapp.proto.server.SeenReceipt;

public class SeenReceiptElement {

    private final String threadId;
    private final String id;
    private final long timestamp;

    SeenReceiptElement(String threadId, String id) {
        this(threadId, id, 0);
    }

    private SeenReceiptElement(String threadId, String id, long timestamp) {
        this.threadId = threadId;
        this.id = id;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public SeenReceipt toProto() {
        SeenReceipt.Builder builder = SeenReceipt.newBuilder();
         builder.setId(id);
         if (threadId != null) {
             builder.setThreadId(threadId);
         }
         return builder.build();
    }
}
