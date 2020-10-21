package com.halloapp.ui;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Size;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.FileStore;
import com.halloapp.content.Media;
import com.halloapp.media.MediaUtils;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CropImageViewModel extends AndroidViewModel {
    private final MutableLiveData<List<MediaModel>> mediaData = new MutableLiveData<>();
    private final MutableLiveData<MediaModel> selected = new MutableLiveData<>();

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

    public void update(MediaModel model) {
        BgWorkers.getInstance().execute(() -> {
            final Size size = MediaUtils.getDimensions(model.tmp.file, model.tmp.type);
            if (size == null) {
                return;
            }

            final Media tmp = Media.createFromFile(model.tmp.type, model.tmp.file);
            tmp.width = size.getWidth();
            tmp.height = size.getHeight();


            List<MediaModel> models = mediaData.getValue();
            if (tmp == null || models == null || !models.contains(model)) {
                return;
            }

            model.tmp = tmp;
            mediaData.postValue(models);
        });
    }

    public void onMoved(int original, int target) {
        List<MediaModel> models = mediaData.getValue();

        if (models != null) {
            MediaModel m = models.remove(original);
            models.add(target, m);

            mediaData.setValue(models);
        }
    }

    private static class LoadMediaTask extends AsyncTask<Void, Void, List<MediaModel>> {
        private final Application application;
        private final MutableLiveData<List<MediaModel>> mediaData;
        private final MutableLiveData<MediaModel> selected;
        private final Collection<Uri> uris;
        private final Bundle state;
        private final int position;

        LoadMediaTask(
                @NonNull Application application,
                @NonNull MutableLiveData<List<MediaModel>> mediaData,
                @NonNull MutableLiveData<MediaModel> selected,
                int position,
                @NonNull Collection<Uri> uris,
                Bundle state
        ) {
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
            final FileStore store = FileStore.getInstance();
            final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

            for (Uri uri : uris) {
                final boolean isLocalFile = Objects.equals(uri.getScheme(), "file");
                final File original = store.getTmpFileForUri(uri, null);
                final File edit = store.getTmpFileForUri(uri, "edit");
                final File tmp = store.getTmpFileForUri(uri, "tmp");

                @Media.MediaType int type = Media.getMediaType(isLocalFile ?
                        mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString())) :
                        resolver.getType(uri));

                if (!original.exists()) {
                    if (isLocalFile) {
                        try {
                            FileUtils.copyFile(new File(uri.getPath()), original);
                        } catch (IOException e) {
                            // Swallow the exception, the logic below will handle the case of empty file.
                            Log.e("LoadMediaTask: doInBackground copyFile " + uri, e);
                        }
                    } else {
                        FileUtils.uriToFile(application, uri, original);
                    }
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

                if (tmp.exists()) {
                    tmp.delete();
                }

                Media tmpMedia = new Media(0, type, null, tmp, null, null, 0, 0, Media.TRANSFERRED_NO);

                result.add(new MediaModel(uri, originalMedia, editMedia, tmpMedia, editState));
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

    static class MediaModel {
        Uri uri;
        Media original;
        Media edit;
        Media tmp;
        Parcelable state;
        Parcelable tmpState;

        MediaModel(Uri uri, Media original, Media edit, Media tmp, Parcelable state) {
            this.uri = uri;
            this.original = original;
            this.edit = edit;
            this.tmp = tmp;
            this.state = state;
        }

        Media getMedia() {
            if (tmpState != null && tmp != null) {
                return tmp;
            }

            if (state != null && edit != null) {
                return edit;
            }

            return original;
        }

        Parcelable getState() {
            if (tmpState != null && tmp != null) {
                return tmpState;
            }

            if (state != null && edit != null) {
                return state;
            }

            return null;
        }

        public void save() {
            if (tmpState != null && tmp != null && tmp.file.exists()) {
                state = tmpState;

                try {
                    FileUtils.copyFile(tmp.file, edit.file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void clear() {
            if (tmp != null && tmp.file.exists()) {
                tmp.file.delete();
            }
        }
    }
}
