package com.halloapp.media;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dstukalov.videoconverter.BadMediaException;
import com.dstukalov.videoconverter.MediaConversionException;
import com.dstukalov.videoconverter.MediaConverter;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.log_events.MediaUpload;
import com.halloapp.util.FileUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.util.stats.Events;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.MediaUploadIq;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

public class UploadMediaTask extends AsyncTask<Void, Void, Void> {

    private final ContentItem contentItem;

    private final FileStore fileStore;
    private final ContentDb contentDb;
    private final Connection connection;
    public static ConcurrentHashMap<String, Boolean> contentItemIds = new ConcurrentHashMap<>();

    private static final int RETRY_LIMIT = 3;

    public UploadMediaTask(@NonNull ContentItem contentItem, @NonNull FileStore fileStore, @NonNull ContentDb contentDb, @NonNull Connection connection) {
        this.contentItem = contentItem;
        this.contentDb = contentDb;
        this.fileStore = fileStore;
        this.connection = connection;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.i("Resumable Uploader " + contentItem);

        if (contentItem.isTransferFailed()) {
            Log.i("Resumable Uploader ContentItem isTransferFailed");
            return null;
        }

        if (UploadMediaTask.contentItemIds.containsKey(contentItem.id)) {
            Log.i("Resumable Uploader: duplicate contentItem is currently uploading");
            return null;
        }
        UploadMediaTask.contentItemIds.put(contentItem.id, true);

        MediaUpload.Builder uploadEvent = MediaUpload.newBuilder();
        if (contentItem instanceof Post) {
            uploadEvent.setType(MediaUpload.Type.POST);
        } else if (contentItem instanceof Message) {
            uploadEvent.setType(MediaUpload.Type.MESSAGE);
        } else if (contentItem instanceof Comment) {
            uploadEvent.setType(MediaUpload.Type.COMMENT);
        }
        uploadEvent.setId(contentItem.id);

        long maxVideoDurationSeconds = 0;
        if (contentItem instanceof Message) {
            maxVideoDurationSeconds = ServerProps.getInstance().getMaxChatVideoDuration();
        } else if (contentItem instanceof Post || contentItem instanceof Comment) {
            maxVideoDurationSeconds = ServerProps.getInstance().getMaxFeedVideoDuration();
        }

        long startTimeMs = System.currentTimeMillis();

        int numPhotos = 0;
        int numVideos = 0;
        long totalSize = 0;
        int totalRetries = 0;

        boolean success = true;

        for (Media media : contentItem.media) {
            Log.i("Resumable Uploader media transferred: " + media.transferred);
            if (media.transferred == Media.TRANSFERRED_YES || media.transferred == Media.TRANSFERRED_FAILURE || media.transferred == Media.TRANSFERRED_UNKNOWN) {
                continue;
            }
            success = false;
            switch (media.type) {
                case Media.MEDIA_TYPE_IMAGE:
                    numPhotos++;
                    break;
                case Media.MEDIA_TYPE_VIDEO:
                    numVideos++;
                    break;
                case Media.MEDIA_TYPE_UNKNOWN:
                    break;
            }
            try {
                prepareMedia(media, maxVideoDurationSeconds);
            } catch (IOException | MediaConversionException e) {
                Log.e("UploadMediaTask", e);
                break;
            }

            byte[] decSha256hash = null;
            Media existingHashedMedia = null;
            try {
                decSha256hash = FileUtils.getFileSha256(media.file);
                existingHashedMedia = contentDb.getLatestMediaWithHash(decSha256hash);
                Log.d("Resumable Uploader: existing upload = " + existingHashedMedia);
            } catch (IOException e) {
                Log.e("Resumable Uploader: could not compute hash for " + media.file.getAbsolutePath(), e);
            } catch (NoSuchAlgorithmException e) {
                Log.e("Resumable Uploader NoSuchAlgorithmException");
            }

            Log.d("Resumable Uploader: transferred = " + media.transferred);

            File encryptedFile;
            try {
                encryptedFile = encryptFile(media.file, media.encKey, media.type, contentItem.id);
            } catch (IOException e) {
                Log.e("resumable uploader Fail to encrypt file");
                break;
            }
            long fileSize = encryptedFile.length();
            totalSize += fileSize;
            MediaUploadIq.Urls urls = null;
            long offset = 0;
            if (media.transferred == Media.TRANSFERRED_NO) {
                try {
                    final String downloadUrl = existingHashedMedia != null ? existingHashedMedia.url : null;
                    urls = connection.requestMediaUpload(fileSize, downloadUrl).await();
                    if (urls == null) {
                        Log.e("Resumable Uploader: failed to get urls");
                        break;
                    }
                    Log.d("Resumable Uploader: obtained downloadUrl = " + urls.downloadUrl);

                    if (urls.patchUrl != null) {
                        contentItem.setPatchUrl(media.rowId, urls.patchUrl, contentDb);
                        media.transferred = Media.TRANSFERRED_RESUME;
                        contentItem.setMediaTransferred(media, contentDb);
                    }
                } catch (InterruptedException e) {
                    Log.e("Resumable Uploader ", e);
                    break;
                } catch (ObservableErrorException e) {
                    Log.e("Resumable Uploader: failed to get urls", e);
                    break;
                }
            } else if (media.transferred == Media.TRANSFERRED_RESUME) {
                urls = new MediaUploadIq.Urls();
                urls.patchUrl = contentItem.getPatchUrl(media.rowId, contentDb);
                try {
                    offset = ResumableUploader.sendHeadRequest(urls.patchUrl);
                    Log.i("Resumable Uploader offset: " + offset);
                    if (offset == -1) {
                        continue;
                    }
                    int retryCount;
                    if (media.isInInitialState()) {
                        Log.i("Resumable Upload media is re-initialized");
                        retryCount = 0;
                        media.encKey = contentItem.getMediaEncKey(media.rowId, contentDb);
                    } else {
                        if (offset > contentItem.getUploadProgress(media.rowId, contentDb)) {
                            retryCount = 0;
                            contentItem.setUploadProgress(media.rowId, offset, contentDb);
                        } else {
                            retryCount = contentItem.getRetryCount(media.rowId, contentDb);
                            if (retryCount >= RETRY_LIMIT) {
                                totalRetries += retryCount;
                                media.transferred = Media.TRANSFERRED_FAILURE;
                                Log.i("Resumable Upload media reaches its retry limit");
                                break;
                            }
                            retryCount++;
                        }
                    }
                    contentItem.setRetryCount(media.rowId, retryCount, contentDb);
                    totalRetries += retryCount;
                } catch (IOException e) {
                    Log.e("Resumable Uploader: failed to get offset from HEAD request" + e);
                }
            }

            final Uploader.UploadListener uploadListener = percent -> true;
            final ResumableUploader.ResumableUploadListener resumableUploadListener = percent -> true;
            if (urls != null &&
                    urls.downloadUrl != null &&
                    existingHashedMedia != null &&
                    urls.downloadUrl.equals(existingHashedMedia.url) &&
                    existingHashedMedia.encKey != null &&
                    existingHashedMedia.encSha256hash != null) {

                media.url = urls.downloadUrl;
                media.encKey = existingHashedMedia.encKey;
                media.encSha256hash = existingHashedMedia.encSha256hash;
                media.decSha256hash = decSha256hash;
                media.transferred = Media.TRANSFERRED_YES;
                if (encryptedFile.exists()) {
                    encryptedFile.delete();
                }
                success = true;
            } else if (urls != null && urls.patchUrl != null) {
                try {
                    media.url = ResumableUploader.sendPatchRequest(encryptedFile, offset, urls.patchUrl, resumableUploadListener);
                    media.encSha256hash = FileUtils.getFileSha256(encryptedFile);
                    media.decSha256hash = decSha256hash;
                    media.transferred = Media.TRANSFERRED_YES;
                    if (encryptedFile.exists()) {
                        encryptedFile.delete();
                    }
                    success = true;
                } catch (ResumableUploader.ResumableUploadException e) {
                    if (e.code / 100 == 4) {
                        Log.e("Resumable Uploader client exception: " + e.code);
                        media.transferred = Media.TRANSFERRED_FAILURE;
                        if (encryptedFile.exists()) {
                            encryptedFile.delete();
                        }
                        break;
                    } else {
                        Log.e("Resumable Uploader other exception:" + e.code);
                        break;
                    }
                } catch (IOException e) {
                    Log.e("Resumable Uploader: " + urls.patchUrl, e);
                    break;
                } catch (NoSuchAlgorithmException e) {
                    media.transferred = Media.TRANSFERRED_FAILURE;
                    if (encryptedFile.exists()) {
                        encryptedFile.delete();
                    }
                    Log.e("Resumable Uploader NoSuchAlgorithmException");
                    break;
                }
            } else if (urls != null && urls.putUrl != null) {
                try {
                    media.encSha256hash = Uploader.run(media.file, media.encKey, media.type, urls.putUrl, uploadListener);
                    media.decSha256hash = decSha256hash;
                    media.url = urls.getUrl;
                    media.transferred = Media.TRANSFERRED_YES;
                    success = true;
                } catch (Uploader.UploadException e) {
                    Log.e("UploadMediaTask: " + media.url, e);
                    if (e.code / 100 == 4) {
                        media.transferred = Media.TRANSFERRED_FAILURE;
                        break;
                    }
                } catch (IOException e) {
                    Log.e("UploadMediaTask: " + urls.putUrl, e);
                    break;
                }
            }
        }

        for (Media media : contentItem.media) {
            contentItem.setMediaTransferred(media, contentDb);
        }

        if (contentItem.isAllMediaTransferred()) {
            contentItem.send(connection);
        }
        UploadMediaTask.contentItemIds.remove(contentItem.id);

        long endTimeMs = System.currentTimeMillis();

        uploadEvent.setStatus(success ? MediaUpload.Status.OK : MediaUpload.Status.FAIL);
        uploadEvent.setDurationMs((int)(endTimeMs - startTimeMs));
        uploadEvent.setNumPhotos(numPhotos);
        uploadEvent.setNumVideos(numVideos);
        uploadEvent.setTotalSize((int)totalSize);
        uploadEvent.setRetryCount(totalRetries);
        Events.getInstance().sendEvent(uploadEvent.build());
        return null;
    }

    private File encryptFile(@NonNull File file, @Nullable byte[] mediaKey, @Media.MediaType int type, @NonNull String postId) throws IOException {
        final String finishedEncryptedFileName = "encrypted-" + file.getName() + "-" + postId + "-finished";
        final String unfinishedEncryptedFileName = "encrypted-" + file.getName() + "-" + postId + "-unfinished";

        File encryptedFile = new File(fileStore.getTmpDir(), finishedEncryptedFileName);
        if (encryptedFile.exists()) {
            return encryptedFile;
        } else {
            encryptedFile = new File(fileStore.getTmpDir(), unfinishedEncryptedFileName);
        }
        OutputStream out = new FileOutputStream(encryptedFile);
        if (mediaKey != null) {
            out = new MediaEncryptOutputStream(mediaKey, type, out);
        }

        final InputStream in = new FileInputStream(file);
        final int bufferSize = 1024;
        final byte[] bytes = new byte[bufferSize];
        while (true) {
            final int count = in.read(bytes, 0, bufferSize);
            if (count == -1) {
                break;
            }
            out.write(bytes, 0, count);
        }
        in.close();
        out.close();

        File newEncryptedFile = new File(fileStore.getTmpDir(), finishedEncryptedFileName);
        if (!encryptedFile.renameTo(newEncryptedFile)) {
            Log.e("Resumable Uploader Task convert: failed to rename " + encryptedFile.getAbsolutePath() + " to " + newEncryptedFile.getAbsolutePath());
        }
        return newEncryptedFile;
    }

    private void prepareMedia(@NonNull Media media, long maxVideoDurationSeconds) throws IOException, MediaConversionException {
        if (media.type == Media.MEDIA_TYPE_VIDEO && MediaUtils.shouldConvertVideo(media.file, maxVideoDurationSeconds)) {
            final File file = fileStore.getTmpFile(RandomId.create());
            final MediaConverter mediaConverter = new MediaConverter();
            mediaConverter.setInput(media.file);
            mediaConverter.setOutput(file);

            mediaConverter.setTimeRange(0, maxVideoDurationSeconds * 1000);
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
                Log.v("Resumable Uploader Task convert: " + percent);
                return false;
            });

            try {
                mediaConverter.convert();
            } catch (BadMediaException e) {
                throw new IOException(e);
            }

            if (!media.file.delete()) {
                Log.e("Resumable Uploader Task: failed to delete " + media.file.getAbsolutePath());
            }
            if (!file.renameTo(media.file)) {
                Log.e("Resumable Uploader Task convert: failed to rename " + file.getAbsolutePath() + " to " + media.file.getAbsolutePath());
            }
        }
    }

    public static void restartUpload(@NonNull ContentItem contentItem, @NonNull FileStore fileStore, @NonNull ContentDb contentDb, @NonNull Connection connection){
        contentItem.reInitialize();
        new UploadMediaTask(contentItem, fileStore, contentDb, connection).executeOnExecutor(MediaUploadDownloadThreadPool.THREAD_POOL_EXECUTOR);
    }
}
