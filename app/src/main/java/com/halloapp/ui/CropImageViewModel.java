package com.halloapp.ui;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.FileStore;
import com.halloapp.content.Media;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CropImageViewModel extends AndroidViewModel {
    private final MutableLiveData<List<MediaModel>> mediaData = new MutableLiveData<>();
    private final MutableLiveData<MediaModel> selected = new MutableLiveData<>();

    private boolean clearOnDestroy = false;

    public CropImageViewModel(Application application) {
        super(application);
    }

    public void loadMediaData(Collection<Uri> uris, Bundle state) {
        if (uris == null) {
            return;
        }

        new LoadMediaTask(getApplication(), mediaData, selected, -1, uris, state).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void loadMediaData(Collection<Uri> uris, Bundle state, int position) {
        if (uris == null) {
            return;
        }

        new LoadMediaTask(getApplication(), mediaData, selected, position, uris, state).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public LiveData<List<MediaModel>> getMediaData() {
        return mediaData;
    }

    public LiveData<MediaModel> getSelected() {
        return selected;
    }

    public ArrayList<Uri> getUris() {
        List<MediaModel> models = mediaData.getValue();

        if (models == null) {
            return null;
        }

        ArrayList<Uri> result = new ArrayList<>(models.size());
        for (MediaModel m : models) {
            result.add(m.uri);
        }

        return result;
    }

    public int getSelectedPosition() {
        List<MediaModel> models = mediaData.getValue();
        MediaModel m = selected.getValue();

        if (m == null || models == null) {
            return -1;
        }

        return models.indexOf(m);
    }

    public Bundle getState() {
        List<MediaModel> models = mediaData.getValue();

        if (models == null) {
            return null;
        }

        Bundle result = new Bundle();
        for (MediaModel m : models) {
            result.putParcelable(m.uri.toString(), m.state);
        }

        return result;
    }

    public void select(int position) {
        List<MediaModel> models = mediaData.getValue();

        if (models != null && 0 <= position && position < models.size()) {
            selected.postValue(models.get(position));
        }
    }

    public void delete(@NonNull MediaModel m) {
        List<MediaModel> models = mediaData.getValue();

        if (models != null && models.remove(m)) {
            mediaData.postValue(models);

            if (selected.getValue() == m) {
                for (MediaModel current : models) {
                    if (current.original.type == Media.MEDIA_TYPE_IMAGE) {
                        selected.postValue(current);
                        break;
                    }
                }
            }
        }
    }

    public void update(MediaModel m) {
        new UpdateCropTask(mediaData, m).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void clearOnDestroy() {
        clearOnDestroy = true;
    }

    public void onMoved(int original, int target) {
        List<MediaModel> models = mediaData.getValue();

        if (models != null) {
            MediaModel m = models.remove(original);
            models.add(target, m);

            mediaData.setValue(models);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        List<MediaModel> models = mediaData.getValue();

        if (clearOnDestroy && models != null) {
            new CleanupTask(models).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private static class LoadMediaTask extends AsyncTask<Void, Void, List<MediaModel>> {
        private final Application application;
        private final MutableLiveData<List<MediaModel>> mediaData;
        private final MutableLiveData<MediaModel> selected;
        private final Collection<Uri> uris;
        private final Bundle state;
        private int position;

        LoadMediaTask(@NonNull Application application, @NonNull MutableLiveData<List<MediaModel>> mediaData, @NonNull MutableLiveData<MediaModel> selected, int position, @NonNull Collection<Uri> uris, Bundle state) {
            this.application = application;
            this.mediaData = mediaData;
            this.selected = selected;
            this.uris = uris;
            this.state = state;
            this.position = position;
        }

        protected Media mediaFromFile(@Media.MediaType int type, File f) {
            final Size size = MediaUtils.getDimensions(f, type);

            if (size != null) {
                final Media m = Media.createFromFile(type, f);
                m.width = size.getWidth();
                m.height = size.getHeight();

                return m;
            }

            return null;
        }

        @Override
        protected List<MediaModel> doInBackground(Void... voids) {
            final List<MediaModel> result = new ArrayList<>(uris.size());
            final ContentResolver resolver = application.getContentResolver();
            final FileStore store = FileStore.getInstance(application);

            for (Uri uri : uris) {
                final String originalName = FileUtils.generateTempMediaFileName(uri, null);
                final String editName = FileUtils.generateTempMediaFileName(uri, "edit");

                final File original = store.getTmpFile(originalName);
                final File edit = store.getTmpFile(editName);

                @Media.MediaType int type = Media.getMediaType(resolver.getType(uri));

                if (!original.exists()) {
                    FileUtils.uriToFile(application, uri, original);
                }

                Media originalMedia = mediaFromFile(type, original);
                if (originalMedia == null) {
                    continue;
                }

                Media editMedia;
                if (edit.exists()) {
                    editMedia = mediaFromFile(type, edit);
                } else {
                    editMedia = new Media(0, type, null, edit, null, null, 0, 0, Media.TRANSFERRED_NO);
                }

                Parcelable editState = null;
                if (state != null) {
                    editState = state.getParcelable(uri.toString());
                }

                result.add(new MediaModel(uri, originalMedia, editMedia, editState));
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<MediaModel> result) {
            super.onPostExecute(result);

            mediaData.postValue(result);
            syncWithSelected(result);
        }

        private void syncWithSelected(List<MediaModel> result) {
            if (selected.getValue() == null) {
                if (0 <= position && position < result.size()) {
                    selected.postValue(result.get(position));
                }
            } else {
                Uri uri = selected.getValue().uri;

                for (MediaModel model : result) {
                    if (model.uri.equals(uri)) {
                        selected.postValue(model);
                        return;
                    }
                }

                for (MediaModel model : result) {
                    if (model.original.type == Media.MEDIA_TYPE_IMAGE) {
                        selected.postValue(model);
                        return;
                    }
                }
            }
        }
    }

    private static class UpdateCropTask extends AsyncTask<Void, Void, Media> {
        private final MutableLiveData<List<MediaModel>> mediaData;
        private final MediaModel model;

        UpdateCropTask(@NonNull MutableLiveData<List<MediaModel>> mediaData, @NonNull MediaModel model) {
            this.mediaData = mediaData;
            this.model = model;
        }

        protected Media mediaFromFile(@Media.MediaType int type, File f) {
            final Size size = MediaUtils.getDimensions(f, type);

            if (size != null) {
                final Media m = Media.createFromFile(type, f);
                m.width = size.getWidth();
                m.height = size.getHeight();

                return m;
            }

            return null;
        }

        @Override
        protected Media doInBackground(Void... voids) {
            return mediaFromFile(model.edit.type, model.edit.file);
        }

        @Override
        protected void onPostExecute(Media result) {
            super.onPostExecute(result);

            List<MediaModel> models = mediaData.getValue();
            if (result == null || models == null || models.indexOf(model) < 0) {
                return;
            }

            model.edit = result;
            mediaData.postValue(models);
        }
    }

    private static class CleanupTask extends AsyncTask<Void, Void, Void> {
        private final List<MediaModel> models;

        CleanupTask(@NonNull List<MediaModel> models) {
            this.models = models;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (MediaModel m : models) {
                if (m.original != null && m.original.file.exists()) {
                    if (!m.original.file.delete()) {
                        Log.e("failed to delete temporary file " + m.original.file.getAbsolutePath());
                    }
                }

                if (m.edit != null && m.edit.file.exists()) {
                    if (!m.edit.file.delete()) {
                        Log.e("failed to delete temporary file " + m.edit.file.getAbsolutePath());
                    }
                }
            }

            return null;
        }
    }

    static class MediaModel {
        Uri uri;
        Media original;
        Media edit;
        Parcelable state;

        MediaModel(Uri uri, Media original, Media edit, Parcelable state) {
            this.uri = uri;
            this.original = original;
            this.edit = edit;
            this.state = state;
        }
    }
}
