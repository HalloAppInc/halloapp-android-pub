package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.arch.core.util.Function;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.daasuu.mp4compose.composer.ImagePostShareGenerator;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.katchup.media.MediaTranscoderTask;
import com.halloapp.katchup.media.TranscodeExternalShareVideoTask;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;

public class ShareIntentHelper {

    public static LiveData<Intent> shareExternallyWithPreview(@NonNull Context context, @Nullable String targetPackage, @NonNull Post post) {
        MutableLiveData<Intent> result = new MutableLiveData<>();

        BgWorkers.getInstance().execute(() -> {
            try {
                prepareExternalShareVideo(post, input -> {
                    if (input == null) {
                        Log.e("ShareIntentHelper/shareExternallyWithPreview failed to get transcoded file");
                        result.postValue(null);
                    } else {
                        result.postValue(generateShareIntent(context, targetPackage, input));
                    }
                    return null;
                });
            } catch (IOException e) {
                Log.e("CommentsViewModel/shareExternallyWithPreview failed", e);
            }
        });

        return result;
    }

    private static Intent generateShareIntent(@NonNull Context context, @Nullable String targetPackage, @NonNull File postFile) {
        Uri videoUri = FileProvider.getUriForFile(context, "com.halloapp.katchup.fileprovider", postFile);

        ShareCompat.IntentBuilder builder = (new ShareCompat.IntentBuilder(context))
                .setStream(videoUri)
                .setType("video/mp4");

        if (targetPackage == null) {
            builder.setChooserTitle(context.getString(R.string.share_moment_label)).createChooserIntent();
            return builder.getIntent();
        } else {
            Intent intent = builder.getIntent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setPackage(targetPackage);

            return intent;
        }
    }

    @WorkerThread
    public static void prepareExternalShareVideo(@NonNull Post post, @NonNull Function<File, Void> callback) throws IOException {
        Media selfie = post.getMedia().get(0);
        Media content = post.getMedia().get(1);
        File postFile = FileStore.getInstance().getShareFile(post.id + ".mp4");
        if (postFile.exists()) {
            callback.apply(postFile);
            return;
        }
        if (content.type == Media.MEDIA_TYPE_VIDEO) {
            TranscodeExternalShareVideoTask transcodeExternalShareVideoTask = new TranscodeExternalShareVideoTask(content.file, selfie.file, postFile);
            MediaTranscoderTask transcoderTask = new MediaTranscoderTask(transcodeExternalShareVideoTask);
            transcoderTask.setListener(new MediaTranscoderTask.Listener() {
                @Override
                public void onSuccess() {
                    callback.apply(postFile);
                }

                @Override
                public void onError(Exception e) {
                    postFile.delete();
                    callback.apply(null);
                }

                @Override
                public void onProgress(double progress) {

                }

                @Override
                public void onCanceled() {
                    postFile.delete();
                    callback.apply(null);
                }
            });
            transcoderTask.start();
        } else if (content.type == Media.MEDIA_TYPE_IMAGE) {
            ImagePostShareGenerator.generateExternalShareVideo(content.file, selfie.file, postFile);
            callback.apply(postFile);
        } else {
            Log.e("Unexpected content type " + content.type);
            callback.apply(null);
        }
    }
}
