package com.halloapp.xmpp;

import androidx.annotation.Nullable;

import com.halloapp.content.Media;
import com.halloapp.proto.server.MediaCounters;

import java.util.List;

public class MediaCounts {
    int audioCount = 0;
    int imageCount = 0;
    int videoCount = 0;

    public MediaCounts(@Nullable List<Media> media) {
        if (media != null) {
            for (Media item : media) {
                if (item.type == Media.MEDIA_TYPE_AUDIO) {
                    audioCount++;
                } else if (item.type == Media.MEDIA_TYPE_IMAGE) {
                    imageCount++;
                } else if (item.type == Media.MEDIA_TYPE_VIDEO) {
                    videoCount++;
                }
            }
        }
    }

    public MediaCounters toProto() {
        return MediaCounters.newBuilder()
                .setNumAudio(audioCount)
                .setNumImages(imageCount)
                .setNumVideos(videoCount)
                .build();
    }
}
