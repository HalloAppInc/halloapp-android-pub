package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.AiImageRequest;
import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.Link;
import com.halloapp.proto.server.SetLinkRequest;

public class AiImageRequestIq extends HalloIq {

    private final String text;
    private final int count;

    public AiImageRequestIq(@NonNull String text, int count) {
        this.text = text;
        this.count = count;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setAiImageRequest(AiImageRequest.newBuilder()
                        .setText(text)
                        .setNumImages(count)
                        .setPromptMode(AiImageRequest.PromptMode.SERVER)
                        .build());
    }
}

