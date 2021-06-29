package com.halloapp.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.halloapp.content.Media;
import com.halloapp.util.StringUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ContentPhotoView;
import com.halloapp.widget.PlaceholderDrawable;

import java.io.File;
import java.util.concurrent.Callable;

public class AudioDurationLoader extends ViewDataLoader<TextView, Long, File> {

    private final LruCache<File, Long> cache;

    private final Context context;

    public AudioDurationLoader(@NonNull Context context) {
        this.context = context;

        cache = new LruCache<File, Long>(128);
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull Media media) {
        final ViewDataLoader.Displayer<TextView, Long> displayer = new ViewDataLoader.Displayer<TextView, Long>() {

            @Override
            public void showResult(@NonNull TextView view, @Nullable Long result) {
                if (result == null) {
                    view.setText("");
                } else {
                    view.setText(StringUtils.formatVoiceNoteDuration(context, result));
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setText("");
            }
        };
        load(view, media, displayer);
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull Media media, @NonNull ViewDataLoader.Displayer<TextView, Long> displayer) {
        if (media.file == null) {
            view.setText("");
            return;
        }
        final Callable<Long> loader = () -> {
            Long duration = null;
            if (media.file != null) {
                if (media.file.exists()) {
                    duration = MediaUtils.getAudioDuration(media.file);
                } else {
                    Log.i("AudioDurationLoader:load file " + media.file.getAbsolutePath() + " doesn't exist");
                }
            }
            return duration;
        };
        load(view, loader, displayer, media.file, cache);
    }

}
