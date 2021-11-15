package com.halloapp.media;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.halloapp.Constants;

import java.io.File;

public class ExoUtils {
    public static DataSpec createChunkedVideoDataSpec(@NonNull String cacheKey, @NonNull String remoteLocation, @NonNull ChunkedMediaParameters chunkedParameters) {
        return new DataSpec.Builder().setKey(cacheKey).setUri(Uri.parse(remoteLocation)).setLength(C.LENGTH_UNSET).build();
    }

    public static DataSource.Factory getChunkedMediaDataSourceFactory(long modelRowId, @NonNull String remoteLocation, @NonNull ChunkedMediaParameters chunkedParameters, @NonNull File file, @Nullable TransferListener transferListener) {
        return new ChunkedMediaDataSource.Factory(modelRowId, remoteLocation, chunkedParameters, file).setTransferListener(transferListener);
    }

    public static DataSource.Factory getChunkedMediaDataSourceFactory(long modelRowId, @NonNull String remoteLocation, @NonNull ChunkedMediaParameters chunkedParameters, @NonNull File file) {
        return getChunkedMediaDataSourceFactory(modelRowId, remoteLocation, chunkedParameters, file, null);
    }

    public static DataSource.Factory getDefaultDataSourceFactory(@NonNull Context context) {
        return new DefaultDataSourceFactory(context, Constants.USER_AGENT);
    }

    public static MediaItem getChunkedMediaItem(long mediaRowId, @NonNull String remoteLocation) {
        return new MediaItem.Builder().setCustomCacheKey(String.valueOf(mediaRowId)).setUri(Uri.parse(remoteLocation)).build();
    }

    public static MediaItem getUriMediaItem(@NonNull Uri uri) {
        return MediaItem.fromUri(uri);
    }
}
