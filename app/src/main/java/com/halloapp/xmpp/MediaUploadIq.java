package com.halloapp.xmpp;

import com.halloapp.proto.server.Iq;
import com.halloapp.proto.server.MediaUrl;
import com.halloapp.proto.server.UploadMedia;

public class MediaUploadIq extends HalloIq {

    final Urls urls = new Urls();
    long fileSize = 0;

    MediaUploadIq(long fileSize) {
        this.fileSize = fileSize;
    }

    private MediaUploadIq(String patch, String put, String get) {
        urls.patchUrl = patch;
        urls.putUrl = put;
        urls.getUrl = get;
    }

    @Override
    public Iq toProtoIq() {
        return Iq.newBuilder()
                .setType(Iq.Type.GET)
                .setUploadMedia(UploadMedia.newBuilder().setSize(fileSize))
                .build();
    }

    public static MediaUploadIq fromProto(UploadMedia uploadMedia) {
        MediaUrl mediaUrl = uploadMedia.getUrl();
        return new MediaUploadIq(mediaUrl.getPatch(), mediaUrl.getPut(), mediaUrl.getGet());
    }

    public static class Urls {
        public String putUrl;
        public String getUrl;
        public String patchUrl;
    }
}
