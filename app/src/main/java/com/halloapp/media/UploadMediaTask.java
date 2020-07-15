package com.halloapp.media;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dstukalov.videoconverter.BadMediaException;
import com.dstukalov.videoconverter.MediaConversionException;
import com.dstukalov.videoconverter.MediaConverter;
import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.content.ContentDb;
import com.halloapp.content.ContentItem;
import com.halloapp.content.Media;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.MediaUploadIq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
            media.transferred = contentItem.getMediaTransferred(media.rowId, contentDb);
            if (media.transferred == Media.TRANSFERRED_YES || media.transferred == Media.TRANSFERRED_FAILURE || media.transferred == Media.TRANSFERRED_UNKNOWN) {
                continue;
            }
            try {
                prepareMedia(media);
            } catch (IOException | MediaConversionException e) {
                Log.e("UploadMediaTask", e);
                return null;
            }

            File encryptedFile;
            try {
                encryptedFile = encryptFile(media.file, media.encKey, media.type, contentItem.id);
            } catch (IOException e) {
                Log.e("resumable uploader Fail to encrypt file");
                return null;
            }

            MediaUploadIq.Urls urls = null;
            int offset = 0;
            if (media.transferred == Media.TRANSFERRED_NO) {
                try {
                    long fileSize = encryptedFile.length();
                    urls = connection.requestMediaUpload(fileSize).get();
                    if (urls == null) {
                        Log.e("Resumable Uploader: failed to get urls");
                        return null;
                    }

                    if (urls.patchUrl != null) {
                        contentItem.setPatchUrl(media.rowId, urls.patchUrl, contentDb);
                        media.transferred = Media.TRANSFERRED_RESUME;
                        contentItem.setMediaTransferred(media, contentDb);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    Log.e("Resumable Uploader", e);
                    return null;
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
                } catch (IOException e) {
                    Log.e("Resumable Uploader: failed to get offset from HEAD request" + e);
                }
            }

            final Uploader.UploadListener uploadListener = percent -> true;
            final ResumableUploader.ResumableUploadListener resumableUploadListener = percent -> true;
            if (urls != null && urls.patchUrl != null) {
                try {
                    media.url = ResumableUploader.sendPatchRequest(encryptedFile, offset, urls.patchUrl, resumableUploadListener);
                    media.sha256hash = calculateDigest(encryptedFile);
                    media.transferred = Media.TRANSFERRED_YES;
                    contentItem.setMediaTransferred(media, contentDb);
                    if (encryptedFile.exists()) {
                        encryptedFile.delete();
                    }
                } catch (ResumableUploader.ResumableUploadException e) {
                    if (e.code / 100 == 4) {
                        Log.e("Resumable Uploader client exception: " + e.code);
                        media.transferred = Media.TRANSFERRED_FAILURE;
                        if (encryptedFile.exists()) {
                            encryptedFile.delete();
                        }
                        return null;
                    } else {
                        Log.e("Resumable Uploader other exception:" + e.code);
                    }
                } catch (IOException e) {
                    Log.e("Resumable Uploader: " + urls.patchUrl, e);
                    return null;
                } catch (NoSuchAlgorithmException e) {
                    media.transferred = Media.TRANSFERRED_FAILURE;
                    if (encryptedFile.exists()) {
                        encryptedFile.delete();
                    }
                    contentItem.setMediaTransferred(media, contentDb);
                    return null;
                }
            } else if (urls != null && urls.putUrl != null) {
                try {
                    media.sha256hash = Uploader.run(media.file, media.encKey, media.type, urls.putUrl, uploadListener);
                    media.url = urls.getUrl;
                    media.transferred = Media.TRANSFERRED_YES;
                    contentItem.setMediaTransferred(media, contentDb);
                } catch (Uploader.UploadException e) {
                    Log.e("UploadMediaTask: " + media.url, e);
                    if (e.code / 100 == 4) {
                        media.transferred = Media.TRANSFERRED_FAILURE;
                        contentItem.setMediaTransferred(media, contentDb);
                    }
                } catch (IOException e) {
                    Log.e("UploadMediaTask: " + urls.putUrl, e);
                    return null;
                }
            }
        }

        if (contentItem.isAllMediaTransferred()) {
            contentItem.send(connection);
        }
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

    private byte[] calculateDigest(@NonNull File file) throws NoSuchAlgorithmException, IOException {
        MessageDigest newDigest = MessageDigest.getInstance("SHA-256");
        byte[] fileContent = FileUtils.readFileToByteArray(file);
        newDigest.update(fileContent);
        return newDigest.digest();
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
}
