package com.halloapp.xmpp;

import androidx.annotation.Nullable;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.MediaUrl;
import com.halloapp.proto.server.UploadMedia;

public class MediaUploadIq extends HalloIq {

    final Urls urls = new Urls();
    long fileSize = 0;
    String downloadUrl;
    UploadMedia.Type type;

    MediaUploadIq(long fileSize, @Nullable String downloadUrl, @Nullable UploadMedia.Type type) {
        this.fileSize = fileSize;
        this.downloadUrl = downloadUrl;
        this.type = type;
    }

    private MediaUploadIq(String patch, String put, String get, String downloadUrl) {
        urls.patchUrl = patch;
        urls.putUrl = put;
        urls.getUrl = get;
        urls.downloadUrl = downloadUrl;
    }

    @Override
    public Iq toProtoIq() {
        final UploadMedia.Builder builder = UploadMedia.newBuilder();
        builder.setSize(fileSize);
        if (downloadUrl != null) {
            builder.setDownloadUrl(downloadUrl);
        }
        if (type != null) {
            builder.setType(type);
        }
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setId(getStanzaId())
                .setUploadMedia(builder)
                .build();
    }

    public static MediaUploadIq fromProto(UploadMedia uploadMedia) {
        MediaUrl mediaUrl = uploadMedia.getUrl();
        return new MediaUploadIq(mediaUrl.getPatch(), mediaUrl.getPut(), mediaUrl.getGet(), uploadMedia.getDownloadUrl());
    }

    public static class Urls {
        public String putUrl;
        public String getUrl;
        public String patchUrl;
        public String downloadUrl;
    }
}
