package com.halloapp.katchup.media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daasuu.mp4compose.composer.Mp4Composer;

public class MediaTranscoderTask {

    private Mp4Composer mp4Composer;

    private Listener listener;

    public interface Listener {
        void onSuccess();
        void onError(Exception e);
        void onProgress(double progress);
        void onCanceled();
    }

    public static abstract class DefaultListener implements Listener {
        public void onSuccess() {}
        public void onError(Exception e) {}
        public void onProgress(double progress) {}
        public void onCanceled() {}
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    public MediaTranscoderTask(@NonNull Mp4Composer mp4Composer) {
        this.mp4Composer = mp4Composer;
    }

    public void start() {
        mp4Composer.listener(new Mp4Composer.Listener() {
            @Override
            public void onProgress(double progress) {
                if (listener != null) {
                    listener.onProgress(progress);
                }
            }

            @Override
            public void onCurrentWrittenVideoTime(long timeUs) {
            }

            @Override
            public void onCompleted() {
                if (listener != null) {
                    listener.onSuccess();
                }
            }

            @Override
            public void onCanceled() {
                if (listener != null) {
                    listener.onCanceled();
                }
            }

            @Override
            public void onFailed(Exception exception) {
                if (listener != null) {
                    listener.onError(exception);
                }
            }
        });
        mp4Composer.start();
    }
}
