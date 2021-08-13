package com.halloapp.content;

import android.util.Size;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.halloapp.media.MediaUtils;
import com.halloapp.proto.clients.AlbumMedia;
import com.halloapp.proto.clients.EncryptedResource;
import com.halloapp.proto.clients.Image;
import com.halloapp.proto.clients.StreamingInfo;
import com.halloapp.proto.clients.Video;
import com.halloapp.proto.clients.VoiceNote;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.MessageElementHelper;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;

public class Media {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MEDIA_TYPE_UNKNOWN, MEDIA_TYPE_IMAGE, MEDIA_TYPE_VIDEO, MEDIA_TYPE_AUDIO})
    public @interface MediaType {}
    public static final int MEDIA_TYPE_UNKNOWN = 0;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MEDIA_TYPE_AUDIO = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TRANSFERRED_UNKNOWN, TRANSFERRED_NO, TRANSFERRED_YES, TRANSFERRED_FAILURE, TRANSFERRED_RESUME})
    public @interface TransferredState {}
    public static final int TRANSFERRED_UNKNOWN = -1;
    public static final int TRANSFERRED_NO = 0;
    public static final int TRANSFERRED_YES = 1;
    public static final int TRANSFERRED_FAILURE = 2;
    public static final int TRANSFERRED_RESUME = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({BLOB_VERSION_UNKNOWN, BLOB_VERSION_DEFAULT, BLOB_VERSION_CHUNKED})
    public @interface BlobVersion {}
    public static final int BLOB_VERSION_UNKNOWN = -1;
    public static final int BLOB_VERSION_DEFAULT = 0;
    public static final int BLOB_VERSION_CHUNKED = 1;

    public long rowId;
    public final @MediaType int type;
    public String url;
    public File file;
    public File encFile;
    public int width;
    public int height;
    public byte [] encKey;
    public byte [] encSha256hash;
    public byte [] decSha256hash;
    public @BlobVersion int blobVersion;
    public int chunkSize;
    public long blobSize;
    private boolean initialState = false;

    public @TransferredState int transferred;

    public static Media createFromFile(@MediaType int type, File file) {
        int width = 0;
        int height = 0;

        if (file.exists()) {
            Size size = MediaUtils.getDimensions(file, type);
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
            }
        }

        return new Media(0, type, null, file, generateEncKey(), null, null, width, height, TRANSFERRED_NO, BLOB_VERSION_DEFAULT, 0, 0);
    }

    public static Media createFromUrl(@MediaType int type, String url, byte [] encKey, byte [] encSha256hash, int width, int height, @BlobVersion int blobVersion, int chunkSize, long blobSize) {
        return new Media(0, type, url, null, encKey, encSha256hash, null, width, height, TRANSFERRED_NO, blobVersion, chunkSize, blobSize);
    }

    public static Media parseFromProto(Image image) {
        EncryptedResource resource = image.getImg();
        return createFromUrl(MEDIA_TYPE_IMAGE, resource.getDownloadUrl(),
                resource.getEncryptionKey().toByteArray(), resource.getCiphertextHash().toByteArray(),
                image.getWidth(), image.getHeight(), BLOB_VERSION_DEFAULT, 0, 0);
    }

    public static Media parseFromProto(Video video) {
        EncryptedResource resource = video.getVideo();
        StreamingInfo streamingInfo = video.getStreamingInfo();
        @BlobVersion int blobVersion = MessageElementHelper.fromProtoBlobVersion(streamingInfo.getBlobVersion());
        return createFromUrl(MEDIA_TYPE_VIDEO, resource.getDownloadUrl(),
                resource.getEncryptionKey().toByteArray(), resource.getCiphertextHash().toByteArray(),
                video.getWidth(), video.getHeight(), blobVersion, streamingInfo.getChunkSize(), streamingInfo.getBlobSize());
    }

    public static Media parseFromProto(VoiceNote voiceNote) {
        EncryptedResource resource = voiceNote.getAudio();
        return createFromUrl(MEDIA_TYPE_AUDIO, resource.getDownloadUrl(),
                resource.getEncryptionKey().toByteArray(), resource.getCiphertextHash().toByteArray(),
                0, 0, BLOB_VERSION_DEFAULT, 0, 0);
    }

    public static Media parseFromProto(AlbumMedia albumMedia) {
        switch (albumMedia.getMediaCase()) {
            case IMAGE: return parseFromProto(albumMedia.getImage());
            case VIDEO: return parseFromProto(albumMedia.getVideo());
        }
        return null;
    }

    public static float getMaxAspectRatio(List<Media> media) {
        float maxAspectRatio = 0;
        for (Media mediaItem : media) {
            if (mediaItem.width != 0) {
                float ratio = 1f * mediaItem.height / mediaItem.width;
                if (ratio > maxAspectRatio) {
                    maxAspectRatio = ratio;
                }
            }
        }
        return maxAspectRatio;
    }

    public static @MediaType int getMediaType(String mime) {
        if (mime == null) {
            return MEDIA_TYPE_UNKNOWN;
        } if (mime.startsWith("image/")) {
            return MEDIA_TYPE_IMAGE;
        } else if (mime.startsWith("video/")) {
            return MEDIA_TYPE_VIDEO;
        } else if (mime.startsWith("audio/")) {
            return MEDIA_TYPE_AUDIO;
        } else {
            return MEDIA_TYPE_UNKNOWN;
        }
    }

    public static String getFileExt(@MediaType int type) {
        switch (type) {
            case MEDIA_TYPE_IMAGE: {
                return "jpg";
            }
            case MEDIA_TYPE_VIDEO: {
                return "mp4";
            }
            case MEDIA_TYPE_AUDIO: {
                return "aac";
            }
            case MEDIA_TYPE_UNKNOWN:
            default: {
                return "";
            }
        }
    }

    public static String getMediaTransferStateString(@TransferredState int state) {
        switch (state) {
            case TRANSFERRED_NO: {
                return "no";
            }
            case TRANSFERRED_YES: {
                return "yes";
            }
            case TRANSFERRED_FAILURE: {
                return "failure";
            }
            case TRANSFERRED_RESUME: {
                return "resume";
            }
            case TRANSFERRED_UNKNOWN:
            default: {
                return "unknown";
            }
        }
    }

    public Media(long rowId, @MediaType int type, String url, File file, byte[] encKey, byte [] encSha256hash, byte [] decSha256hash, int width, int height, @TransferredState int transferred, @BlobVersion int blobVersion, int chunkSize, long blobSize) {
        this.rowId = rowId;
        this.type = type;
        this.url = url;
        this.file = file;
        this.encKey = encKey;
        this.encSha256hash = encSha256hash;
        this.decSha256hash = decSha256hash;
        this.width = width;
        this.height = height;
        this.transferred = transferred;
        this.blobVersion = blobVersion;
        this.chunkSize = chunkSize;
        this.blobSize = blobSize;
    }

    private static byte [] generateEncKey() {
        final SecureRandom random = new SecureRandom();
        final byte [] encKey = new byte[32];
        random.nextBytes(encKey);
        return encKey;
    }

    public boolean isInInitialState() {
        return initialState;
    }

    public void initializeRetry(){
        initialState = true;
        transferred = TRANSFERRED_RESUME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        final Media media = (Media) o;
        return rowId == media.rowId &&
                type == media.type &&
                Objects.equals(file, media.file) &&
                transferred == media.transferred;
    }

    @NonNull
    @Override
    public String toString() {
        return "Media {rowId:" + rowId + ", type:" + type + ", initialState:" + initialState + ", transferred:" + transferred + '}';
    }
}
