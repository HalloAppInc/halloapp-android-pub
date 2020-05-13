package com.halloapp.media;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.dstukalov.videoconverter.BadMediaException;
import com.dstukalov.videoconverter.MediaConversionException;
import com.dstukalov.videoconverter.MediaConverter;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.MediaUploadIq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class UploadMediaTask extends AsyncTask<Void, Void, Void> {

    private final ContentItem contentItem;

    private final FileStore fileStore;
    private final ContentDb contentDb;
    private final Connection connection;

    public UploadMediaTask(@NonNull ContentItem contentItem, @NonNull FileStore fileStore, @NonNull ContentDb contentDb, @NonNull Connection connection) {
        this.contentItem = contentItem;
        this.contentDb = contentDb;
        this.fileStore = fileStore;
        this.connection = connection;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.i("UploadMediaTask " + contentItem);
        for (Media media : contentItem.media) {
            if (media.transferred) {
                continue;
            }
            try {
                prepareMedia(media);
            } catch (IOException | MediaConversionException e) {
                Log.e("UploadMediaTask", e);
                return null;
            }
            final MediaUploadIq.Urls urls;
            try {
                urls = connection.requestMediaUpload().get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("UploadMediaTask", e);
                return null;
            }
            if (urls == null) {
                Log.e("UploadMediaTask: failed to get urls");
                return null;
            }

            final Uploader.UploadListener uploadListener = percent -> true;
            try {
                media.sha256hash = Uploader.run(media.file, media.encKey, media.type, urls.putUrl, uploadListener);
                media.url = urls.getUrl;
                media.transferred = true;
                contentItem.setMediaTransferred(media, contentDb);
            } catch (IOException e) {
                Log.e("UploadMediaTask: " + urls.putUrl, e);
                return null;
            }
        }
        contentItem.send(connection);
        return null;
    }

    private void prepareMedia(@NonNull Media media) throws IOException, MediaConversionException {
        if (media.type == Media.MEDIA_TYPE_VIDEO && MediaUtils.shouldConvertVideo(media.file)) {
            final File file = fileStore.getTmpFile(RandomId.create());
            final MediaConverter mediaConverter = new MediaConverter();
            mediaConverter.setInput(media.file);
            mediaConverter.setOutput(file);
            mediaConverter.setTimeRange(0, Constants.MAX_VIDEO_DURATION);
            try {
                mediaConverter.setVideoCodec(MediaConverter.VIDEO_CODEC_H265);
                mediaConverter.setVideoResolution(Constants.VIDEO_RESOLUTION_H265);
            } catch (FileNotFoundException e) {
                mediaConverter.setVideoCodec(MediaConverter.VIDEO_CODEC_H264);
                mediaConverter.setVideoResolution(Constants.VIDEO_RESOLUTION_H264);
            }
            mediaConverter.setVideoBitrate(Constants.VIDEO_BITRATE);
            mediaConverter.setAudioBitrate(Constants.AUDIO_BITRATE);
            mediaConverter.setListener(percent -> {
                Log.v("UploadPostTask.convert " + percent);
                return false;
            });

            try {
                mediaConverter.convert();
            } catch (BadMediaException e) {
                throw new IOException(e);
            }

            if (!media.file.delete()) {
                Log.e("UploadPostTask.convert: failed to delete " + media.file.getAbsolutePath());
            }
            if (!file.renameTo(media.file)) {
                Log.e("UploadPostTask.convert: failed to rename " + file.getAbsolutePath() + " to " + media.file.getAbsolutePath());
            }
        }
    }
}

