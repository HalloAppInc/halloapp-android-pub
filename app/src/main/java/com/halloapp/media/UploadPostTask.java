package com.halloapp.media;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.dstukalov.videoconverter.BadMediaException;
import com.dstukalov.videoconverter.MediaConversionException;
import com.dstukalov.videoconverter.MediaConverter;
import com.halloapp.Connection;
import com.halloapp.Constants;
import com.halloapp.posts.Media;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.protocol.MediaUploadIq;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class UploadPostTask extends AsyncTask<Void, Void, Void> {

    private final Post post;

    private final MediaStore mediaStore;
    private final PostsDb postsDb;
    private final Connection connection;

    public UploadPostTask(@NonNull Post post, @NonNull MediaStore mediaStore, @NonNull PostsDb postsDb, @NonNull Connection connection) {
        this.post = post;
        this.mediaStore = mediaStore;
        this.postsDb = postsDb;
        this.connection = connection;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.i("UploadPostTask " + post);
        for (Media media : post.media) {
            if (media.transferred) {
                continue;
            }
            try {
                prepareMedia(media);
            } catch (IOException | MediaConversionException e) {
                Log.e("UploadPostTask", e);
                return null;
            }
            final MediaUploadIq.Urls urls;
            try {
                urls = connection.requestMediaUpload().get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("UploadPostTask", e);
                return null;
            }
            if (urls == null) {
                Log.e("UploadPostTask: failed to get urls");
                return null;
            }

            final Uploader.UploadListener uploadListener = percent -> true;
            try {
                media.sha256hash = Uploader.run(media.file, media.encKey, media.type, urls.putUrl, uploadListener);
                media.url = urls.getUrl;
                media.transferred = true;
                postsDb.setMediaTransferred(post, media);
            } catch (IOException e) {
                Log.e("UploadPostTask", e);
                return null;
            }
        }
        connection.sendPost(post);
        return null;
    }

    private void prepareMedia(@NonNull Media media) throws IOException, MediaConversionException {
        if (media.type == Media.MEDIA_TYPE_VIDEO && MediaUtils.shouldConvertVideo(media.file)) {
            final File file = mediaStore.getTmpFile(RandomId.create());
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

