package com.halloapp.media;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.halloapp.FileStore;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.proto.log_events.MediaDownload;
import com.halloapp.util.FileUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.RandomId;
import com.halloapp.util.stats.Events;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class DownloadMediaTask extends AsyncTask<Void, Void, Boolean> {

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final ContentItem contentItem;

    private final FileStore fileStore;
    private final ContentDb contentDb;

    public DownloadMediaTask(@NonNull ContentItem contentItem, @NonNull FileStore fileStore, @NonNull ContentDb contentDb) {
        this.contentItem = contentItem;
        this.fileStore = fileStore;
        this.contentDb = contentDb;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Log.i("DownloadMediaTask " + contentItem);
        long startTime = System.currentTimeMillis();
        MediaDownload.Builder mediaDownloadEvent = MediaDownload.newBuilder();
        mediaDownloadEvent.setId(contentItem.id);
        if (contentItem instanceof Post) {
            mediaDownloadEvent.setType(MediaDownload.Type.POST);
        } else if (contentItem instanceof Message) {
            mediaDownloadEvent.setType(MediaDownload.Type.MESSAGE);
        } else if (contentItem instanceof Comment) {
            mediaDownloadEvent.setType(MediaDownload.Type.COMMENT);
        }
        long totalSize = 0;
        int numPhotos = 0;
        int numVideos = 0;
        int numAudio = 0;
        int totalRetries = 0;
        boolean hasFailure = false;
        int index = 0;
        for (Media media : contentItem.media) {
            String mediaLogId = contentItem.id + "." + index++;
            switch (media.type) {
                case Media.MEDIA_TYPE_IMAGE:
                    numPhotos++;
                    break;
                case Media.MEDIA_TYPE_VIDEO:
                    numVideos++;
                    break;
                case Media.MEDIA_TYPE_AUDIO:
                    numAudio++;
                    break;
                case Media.MEDIA_TYPE_UNKNOWN:
                    break;
            }
            if (media.transferred == Media.TRANSFERRED_YES || media.transferred == Media.TRANSFERRED_FAILURE) {
                continue;
            }
            final Downloader.DownloadListener downloadListener = percent -> true;
            int attempts = 0;
            boolean retry;
            boolean success;
            do {
                attempts++;
                retry = false;
                success = false;
                try {
                    final File encFile = media.encFile != null ? media.encFile : fileStore.getTmpFile(RandomId.create() + ".enc");
                    final File file = fileStore.getMediaFile(RandomId.create() + "." + Media.getFileExt(media.type));
                    media.encFile = encFile;
                    contentItem.setMediaTransferred(media, contentDb);
                    Downloader.run(media.url, media.encKey, media.encSha256hash, media.type, encFile, file, downloadListener, mediaLogId);
                    if (!file.setLastModified(contentItem.timestamp)) {
                        Log.w("DownloadMediaTask: failed to set last modified to " + file.getAbsolutePath() + " for " + mediaLogId);
                    }
                    if (!encFile.delete()) {
                        Log.w("DownloadMediaTask: failed to delete temp enc file for " + mediaLogId);
                    }
                    media.file = file;
                    media.decSha256hash = FileUtils.getFileSha256(media.file);
                    media.transferred = Media.TRANSFERRED_YES;
                    contentItem.setMediaTransferred(media, contentDb);
                    Log.i("DownloadMediaTask: transfer status for " + mediaLogId + " set to " + Media.getMediaTransferStateString(media.transferred));
                    totalSize += file.length();
                    success = true;
                } catch (Downloader.DownloadException e) {
                    Log.e("DownloadMediaTask: download exception for " + media.url + " for " + mediaLogId, e);
                    if (media.encFile != null && e.code == 416) {
                        if (!media.encFile.delete()) {
                            Log.e("DownloadMediaTask: failed to delete temp enc file for " + mediaLogId);
                        } else {
                            retry = true;
                        }
                    } else if (e.code / 100 == 4) {
                        media.transferred = Media.TRANSFERRED_FAILURE;
                        contentItem.setMediaTransferred(media, contentDb);
                    }
                } catch (IOException e) {
                    Log.e("DownloadMediaTask: IOE downloading " + media.url + " for " + mediaLogId, e);
                    retry = true;
                } catch (GeneralSecurityException e) {
                    Log.e("DownloadMediaTask: GSE downloading " + media.url + " for " + mediaLogId, e);
                    if (media.encFile != null) {
                        if (!media.encFile.delete()) {
                            Log.e("DownloadMediaTask: failed to delete temp enc file for " + mediaLogId);
                        } else {
                            retry = true;
                        }
                    }
                }
            } while (retry && attempts < MAX_RETRY_ATTEMPTS);
            if (attempts > 1) {
                totalRetries += attempts - 1;
            }
            if (!success) {
                hasFailure = true;
            }
        }
        long endTime = System.currentTimeMillis();
        int duration = (int)(endTime - startTime);
        mediaDownloadEvent.setStatus(hasFailure ? MediaDownload.Status.FAIL : MediaDownload.Status.OK);
        mediaDownloadEvent.setRetryCount(totalRetries);
        mediaDownloadEvent.setDurationMs(duration);
        mediaDownloadEvent.setNumPhotos(numPhotos);
        mediaDownloadEvent.setNumVideos(numVideos);
        mediaDownloadEvent.setTotalSize((int) totalSize);
        Events.getInstance().sendEvent(mediaDownloadEvent.build());
        Log.i("DownloadMediaTask: Downloaded " + totalSize + " bytes of " + contentItem.id + " with " + totalRetries + " retries in " + duration + "ms");
        return null;
    }

    public static void download(@NonNull FileStore fileStore, @NonNull ContentDb contentDb, @NonNull Post post) {
        new DownloadMediaTask(post, fileStore, contentDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
    }
}
