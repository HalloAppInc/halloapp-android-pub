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
import com.halloapp.proto.log_events.MediaObjectDownload;
import com.halloapp.util.FileUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.RandomId;
import com.halloapp.util.stats.Events;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

public class DownloadMediaTask extends AsyncTask<Void, Void, Boolean> {

    private static final Set<String> activeDownloads = new HashSet<>();

    public static void download(@NonNull ContentItem contentItem, @NonNull FileStore fileStore, @NonNull ContentDb contentDb) {
        new DownloadMediaTask(contentItem, fileStore, contentDb).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
    }

    private static final String CLOUD_FRONT_CDN = "u-cdn.halloapp.net";
    private static final String CDN_HIT = "Hit";
    private static final String CDN_MISS = "Miss";
    private static final String CDN_REFRESH = "Refresh";

    private static final int MAX_RETRY_ATTEMPTS = 3;

    private final ContentItem contentItem;

    private final FileStore fileStore;
    private final ContentDb contentDb;

    private DownloadMediaTask(@NonNull ContentItem contentItem, @NonNull FileStore fileStore, @NonNull ContentDb contentDb) {
        this.contentItem = contentItem;
        this.fileStore = fileStore;
        this.contentDb = contentDb;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Log.i("DownloadMediaTask " + contentItem);
        String id = contentItem.id;
        synchronized (DownloadMediaTask.class) {
            if (activeDownloads.contains(id)) {
                Log.i("DownloadMediaTask.download: active download already in progress for " + id);
                return null;
            } else {
                activeDownloads.add(id);
            }
        }
        try {
            long startTime = System.currentTimeMillis();
            MediaDownload.Builder mediaDownloadEvent = MediaDownload.newBuilder();
            mediaDownloadEvent.setId(contentItem.id);
            MediaObjectDownload.Type downloadType = MediaObjectDownload.Type.POST;
            if (contentItem instanceof Post) {
                mediaDownloadEvent.setType(MediaDownload.Type.POST);
                downloadType = MediaObjectDownload.Type.POST;
            } else if (contentItem instanceof Message) {
                mediaDownloadEvent.setType(MediaDownload.Type.MESSAGE);
                downloadType = MediaObjectDownload.Type.MESSAGE;
            } else if (contentItem instanceof Comment) {
                mediaDownloadEvent.setType(MediaDownload.Type.COMMENT);
                downloadType = MediaObjectDownload.Type.COMMENT;
            }
            long totalSize = 0;
            int numPhotos = 0;
            int numVideos = 0;
            int numAudio = 0;
            int totalRetries = 0;
            boolean hasFailure = false;
            int index = 0;
            for (Media media : contentItem.media) {
                MediaObjectDownload.Cdn cdn = MediaObjectDownload.Cdn.UNKNOWN_CDN;
                if (media.url != null && media.url.contains(CLOUD_FRONT_CDN)) {
                    cdn = MediaObjectDownload.Cdn.CLOUDFRONT;
                }
                MediaObjectDownload.MediaType mediaType = MediaObjectDownload.MediaType.UNRECOGNIZED;
                String mediaLogId = contentItem.id + "." + index++;
                switch (media.type) {
                    case Media.MEDIA_TYPE_IMAGE:
                        numPhotos++;
                        mediaType = MediaObjectDownload.MediaType.PHOTO;
                        break;
                    case Media.MEDIA_TYPE_VIDEO:
                        numVideos++;
                        mediaType = MediaObjectDownload.MediaType.VIDEO;
                        break;
                    case Media.MEDIA_TYPE_AUDIO:
                        mediaType = MediaObjectDownload.MediaType.AUDIO;
                        numAudio++;
                        break;
                    case Media.MEDIA_TYPE_UNKNOWN:
                        break;
                }
                if (media.transferred == Media.TRANSFERRED_YES || media.transferred == Media.TRANSFERRED_FAILURE) {
                    continue;
                }
                int attempts = 0;
                boolean retry;
                boolean success;
                do {
                    MediaObjectDownload.Builder downloadStatBuilder = MediaObjectDownload.newBuilder();
                    downloadStatBuilder.setId(contentItem.id);
                    downloadStatBuilder.setIndex(index);
                    downloadStatBuilder.setType(downloadType);
                    downloadStatBuilder.setCdn(cdn);
                    downloadStatBuilder.setMediaType(mediaType);
                    final Downloader.DownloadListener downloadListener = new Downloader.DownloadListener() {
                        @Override
                        public boolean onProgress(long bytesWritten) {
                            downloadStatBuilder.setProgressBytes(bytesWritten);
                            return true;
                        }

                        @Override
                        public void onLogInfo(int contentLength, String cdnPop, String cdnId, String cdnCache) {
                            downloadStatBuilder.setCdnPop(cdnPop);
                            downloadStatBuilder.setCdnId(cdnId);
                            downloadStatBuilder.setSize(contentLength);
                            if (cdnCache != null) {
                                boolean refresh = false;
                                Boolean miss = null;
                                if (cdnCache.contains(CDN_REFRESH)) {
                                    refresh = true;
                                }
                                if (cdnCache.contains(CDN_MISS)) {
                                    miss = true;
                                }
                                if (cdnCache.contains(CDN_HIT)) {
                                    miss = false;
                                }
                                if (miss == null) {
                                    Log.e("DownloadMediaTask/onCdnInfo invalid cdncache: " + cdnCache);
                                    return;
                                }
                                if (refresh) {
                                    downloadStatBuilder.setCdnCache(miss ? MediaObjectDownload.CdnCache.REFRESH_MISS : MediaObjectDownload.CdnCache.REFRESH_HIT);
                                } else {
                                    downloadStatBuilder.setCdnCache(miss ? MediaObjectDownload.CdnCache.MISS : MediaObjectDownload.CdnCache.HIT);
                                }
                            }
                        }
                    };
                    attempts++;
                    downloadStatBuilder.setRetryCount(attempts);
                    retry = false;
                    success = false;
                    long attemptStartTime = System.currentTimeMillis();
                    try {
                        final File encFile = media.encFile != null ? media.encFile : fileStore.getTmpFile(RandomId.create() + ".enc");
                        final File file = fileStore.getMediaFile(RandomId.create() + "." + Media.getFileExt(media.type));
                        media.encFile = encFile;
                        contentItem.setMediaTransferred(media, contentDb);
                        Downloader.run(media.url, media.encKey, media.encSha256hash, media.type, media.blobVersion, media.chunkSize, media.blobSize, encFile, file, downloadListener, mediaLogId);
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
                    } catch (ChunkedMediaParametersException e) {
                        Log.e("DownloadMediaTask: CMPE downloading " + media.url + " for " + mediaLogId, e);
                        media.transferred = Media.TRANSFERRED_FAILURE;
                        contentItem.setMediaTransferred(media, contentDb);
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
                    downloadStatBuilder.setDurationMs(System.currentTimeMillis() - attemptStartTime);
                    if (success) {
                        downloadStatBuilder.setStatus(MediaObjectDownload.Status.OK);
                    } else {
                        downloadStatBuilder.setStatus(MediaObjectDownload.Status.FAIL);
                    }
                    Events.getInstance().sendEvent(downloadStatBuilder.build());
                } while (retry && attempts < MAX_RETRY_ATTEMPTS);
                if (attempts > 1) {
                    totalRetries += attempts - 1;
                }
                if (!success) {
                    hasFailure = true;
                }
            }
            long endTime = System.currentTimeMillis();
            int duration = (int) (endTime - startTime);
            mediaDownloadEvent.setStatus(hasFailure ? MediaDownload.Status.FAIL : MediaDownload.Status.OK);
            mediaDownloadEvent.setRetryCount(totalRetries);
            mediaDownloadEvent.setDurationMs(duration);
            mediaDownloadEvent.setNumPhotos(numPhotos);
            mediaDownloadEvent.setNumVideos(numVideos);
            mediaDownloadEvent.setTotalSize((int) totalSize);
            Events.getInstance().sendEvent(mediaDownloadEvent.build());
            Log.i("DownloadMediaTask: Downloaded " + totalSize + " bytes of " + contentItem.id + " with " + totalRetries + " retries in " + duration + "ms");
            return null;
        } finally {
            activeDownloads.remove(contentItem.id);
        }
    }
}
