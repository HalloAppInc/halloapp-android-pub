package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.proto.server.AiImageRequest;
import com.halloapp.proto.server.Iq;

public class AiImageRequestIq extends HalloIq {

    private final String text;
    private final int count;
    private final boolean custom;

    public AiImageRequestIq(@NonNull String text, int count, boolean custom) {
        this.text = text;
        this.count = count;
        this.custom = custom;
    }

    @Override
    public Iq.Builder toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.SET)
                .setAiImageRequest(AiImageRequest.newBuilder()
                        .setText(text)
                        .setNumImages(count)
                        .setPromptMode(custom ? AiImageRequest.PromptMode.USER : AiImageRequest.PromptMode.SERVER)
                        .build());
    }
}

