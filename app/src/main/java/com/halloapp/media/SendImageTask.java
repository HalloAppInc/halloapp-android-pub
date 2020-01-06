package com.halloapp.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.util.Preconditions;
import androidx.exifinterface.media.ExifInterface;

import com.halloapp.Connection;
import com.halloapp.Constants;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.LoadUriTask;
import com.halloapp.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SendImageTask extends LoadUriTask {

    private final File imageFile;

    public SendImageTask(final @NonNull Context context, final @NonNull Uri uri, final @NonNull File file) {
        super(context, uri, new File(context.getCacheDir(), "tmp.jpg"));
        imageFile = file;
    }

    @Override
    protected File doInBackground(Void... voids) {
        File tmpFile = super.doInBackground(voids);

        try {
            MediaUtils.transcode(tmpFile, imageFile, Constants.MAX_IMAGE_DIMENSION, Constants.JPEG_QUALITY);
        } catch (IOException e) {
            Log.e("failed to transcode image", e);
            return imageFile;
        } finally {
            tmpFile.delete();
        }

        final Post post = new Post(
                0,
                Connection.FEED_JID.toString(),
                "",
                UUID.randomUUID().toString().replaceAll("-", ""),
                "",
                0,
                System.currentTimeMillis(),
                Post.POST_STATE_OUTGOING_SENDING,
                Post.POST_TYPE_IMAGE,
                null,
                null,
                imageFile.getName());
        PostsDb.getInstance(Preconditions.checkNotNull(context)).addPost(post);

        return imageFile;

    }
}
