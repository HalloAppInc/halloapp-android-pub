package com.halloapp.ui.archive;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.util.logs.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArchiveDataSource extends ItemKeyedDataSource<Pair<Long, Long>, ArchiveItem> {

    private final ContentDb contentDb;

    private final SimpleDateFormat monthFormatNoYear = new SimpleDateFormat("MMMM", Locale.getDefault());
    private final SimpleDateFormat monthFormatWithYear = new SimpleDateFormat("MMMM, y", Locale.getDefault());

    private long prevFetchLastTimestamp;

    public static class Factory extends DataSource.Factory<Pair<Long, Long>, ArchiveItem> {
        private final ContentDb contentDb;
        private ArchiveDataSource latestSource;

        public Factory(@NonNull ContentDb contentDb) {
            this.contentDb = contentDb;
            latestSource = new ArchiveDataSource(contentDb);
        }

        @NonNull
        @Override
        public DataSource<Pair<Long, Long>, ArchiveItem> create() {
            if (latestSource.isInvalid()) {
                latestSource = new ArchiveDataSource(contentDb);
            }
            return latestSource;
        }

        public void invalidateLatestDataSource() {
            Log.i("ArchiveDataSource.Factory.invalidateLatestDataSource");
            latestSource.invalidate();
        }
    }

    private ArchiveDataSource(@NonNull ContentDb contentDb) {
        this.contentDb = contentDb;
    }

    public void invalidateLatestDataSource() {
        invalidate();
    }

    @NonNull
    @Override
    public Pair<Long, Long> getKey(@NonNull ArchiveItem archiveItem) {
        return archiveItem.key;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Pair<Long, Long>> params, @NonNull LoadInitialCallback<ArchiveItem> callback) {
        final List<Post> posts = contentDb.getArchivedPosts(null, params.requestedLoadSize, false);
        callback.onResult(setupHeaders(posts, true));
        prevFetchLastTimestamp = posts.isEmpty() ? -1L : posts.get(posts.size() - 1).timestamp;
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Pair<Long, Long>> params, @NonNull LoadCallback<ArchiveItem> callback) {
        final List<Post> posts = contentDb.getArchivedPosts(params.key.first, params.requestedLoadSize, false);
        callback.onResult(setupHeaders(posts, false));
        prevFetchLastTimestamp = posts.isEmpty() ? prevFetchLastTimestamp : posts.get(posts.size() - 1).timestamp;
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Pair<Long, Long>> params, @NonNull LoadCallback<ArchiveItem> callback) {
        final List<Post> posts = contentDb.getArchivedPosts(params.key.first, params.requestedLoadSize, true);
        callback.onResult(setupHeaders(posts, false));
        prevFetchLastTimestamp = posts.isEmpty() ? prevFetchLastTimestamp : posts.get(posts.size() - 1).timestamp;
    }

    private List<ArchiveItem> setupHeaders(List<Post> posts, boolean isInitialLoad) {

        List<ArchiveItem> retList = new ArrayList<>();

        for (int i = 0; i < posts.size(); i++) {
            if ((i == 0 && isInitialLoad) ||
                    (i == 0 && shouldAddHeader(posts.get(i).timestamp, prevFetchLastTimestamp)) ||
                    (i >= 1 && shouldAddHeader(posts.get(i).timestamp, posts.get(i - 1).timestamp))) {
                long key = (i == 0) ? Long.MAX_VALUE : posts.get(i).timestamp;
                Calendar itemCal = Calendar.getInstance();
                itemCal.setTimeInMillis(posts.get(i).timestamp);
                if (Calendar.getInstance().get(Calendar.YEAR) == itemCal.get(Calendar.YEAR)) {
                    retList.add(new ArchiveItem(monthFormatNoYear.format(new Date(posts.get(i).timestamp)), key));
                } else {
                    retList.add(new ArchiveItem(monthFormatWithYear.format(new Date(posts.get(i).timestamp)), key));
                }
            }
            retList.add(new ArchiveItem(posts.get(i)));
        }
        return retList;
    }

    private boolean shouldAddHeader(long curr, long prev) {

        Calendar currCal = Calendar.getInstance();
        currCal.setTimeInMillis(curr);
        Calendar prevCal = Calendar.getInstance();
        prevCal.setTimeInMillis(prev);

        return currCal.get(Calendar.MONTH) != prevCal.get(Calendar.MONTH);
    }
}
