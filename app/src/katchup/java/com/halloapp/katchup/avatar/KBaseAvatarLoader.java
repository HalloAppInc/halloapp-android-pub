package com.halloapp.katchup.avatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.collection.LruCache;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.katchup.ui.Colors;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;

import java.util.concurrent.Executors;

public class KBaseAvatarLoader extends ViewDataLoader<ImageView, Drawable, String> {

    protected static final int DEFAULT_AVATAR_SIZE = 250;
    protected static final float DEFAULT_AVATAR_TEXT_SIZE = 80f;
    protected static final float AVATAR_ROTATION_DEG = -7.5f;

    protected final LruCache<String, Drawable> cache;

    public KBaseAvatarLoader() {
        super(Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1)));

        // Use 1/8th of the available memory for memory cache
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        Log.i("AvatarLoader: create " + cacheSize + "KB cache for avatars");
        cache = new LruCache<String, Drawable>(cacheSize) {

            @Override
            protected int sizeOf(@NonNull String key, @NonNull Drawable drawable) {
                // The cache size will be measured in kilobytes rather than number of items
                if (drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    return bitmap.getByteCount() / 1024;
                } else {
                    return 0;
                }
            }
        };
    }

    @WorkerThread
    @NonNull
    protected static Drawable getDefaultAvatar(@NonNull Context context, @NonNull Contact contact) {
        final String name;
        if (contact.userId != null && contact.userId.isMe()) {
            name = Me.getInstance().getName();
        } else if (contact.halloName != null) {
            name = contact.halloName;
        } else {
            name = contact.getDisplayName();
        }
        return createTextAvatar(context, name, ContextCompat.getColor(context, Colors.getAvatarBgColor(contact.getColorIndex())));
    }

    @WorkerThread
    @NonNull
    private static Drawable createTextAvatar(@NonNull Context context, @Nullable String name, @ColorInt int bgColor) {
        if (name == null) {
            name = "";
        }
        String[] split = name.split(" ");
        String s;
        if (split.length > 1) {
            s = split[0].charAt(0) + "" + split[1].charAt(0);
        } else {
            if (!TextUtils.isEmpty(name)) {
                s = name.substring(0, 1);
            } else {
                s = " ";
            }
        }
        int width = DEFAULT_AVATAR_SIZE;
        int height = DEFAULT_AVATAR_SIZE;

        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        TextPaint tp = new TextPaint();
        tp.setTextSize(DEFAULT_AVATAR_TEXT_SIZE);
        tp.setColor(Color.WHITE);
        tp.setTextAlign(Paint.Align.CENTER);
        tp.setTypeface(ResourcesCompat.getFont(context, R.font.rubikbubbles));
        Canvas canvas = new Canvas(b);
        canvas.drawColor(bgColor);
        canvas.rotate(AVATAR_ROTATION_DEG, width / 2f, height / 2f);

        canvas.drawText(s, width / 2f, ((height / 2f) - ((tp.descent() + tp.ascent()) / 2f)), tp);

        return new BitmapDrawable(context.getResources(), b);
    }
}
