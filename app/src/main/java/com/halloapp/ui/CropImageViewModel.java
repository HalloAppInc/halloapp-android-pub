package com.halloapp.ui;

import android.app.Application;
import android.net.Uri;
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
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CropImageViewModel extends AndroidViewModel {
    private final MutableLiveData<List<MediaModel>> mediaData = new MutableLiveData<>();
    private final MutableLiveData<MediaModel> selected = new MutableLiveData<>();
    private final FileStore store = FileStore.getInstance();

    public CropImageViewModel(Application application) {
        super(application);
    }

    public void loadMediaData(List<Uri> uris, Bundle state, int position) {
        if (uris == null) {
            return;
        }

        BgWorkers.getInstance().execute(() -> {
            List<MediaModel> models = load(uris, state);
            mediaData.postValue(models);

            if (0 <= position && position < models.size()) {
                selected.postValue(models.get(position));
            }
        });
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

    public void sort() {
        List<MediaModel> models = mediaData.getValue();

        if (models != null) {
            Collections.sort(models, (a, b) -> Long.compare(a.date, b.date));
            mediaData.postValue(models);
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
            if (models == null || !models.contains(model)) {
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

    private Media mediaFromFile(@Media.MediaType int type, File f) {
        final Size size = MediaUtils.getDimensions(f, type);

        if (size != null) {
            final Media m = Media.createFromFile(type, f);
            m.width = size.getWidth();
            m.height = size.getHeight();

            return m;
        }

        return null;
    }

    private List<MediaModel> load(List<Uri> uris, Bundle state) {
        List<MediaModel> result = new ArrayList<>(uris.size());
        Map<Uri, Integer> types = MediaUtils.getMediaTypes(getApplication(), uris);
        Map<Uri, Long> dates = MediaUtils.getDates(getApplication(), uris);

        for (Uri uri : uris) {
            final boolean isLocalFile = Objects.equals(uri.getScheme(), "file");
            final File original = store.getTmpFileForUri(uri, null);
            final File edit = store.getTmpFileForUri(uri, "edit");
            final File tmp = store.getTmpFileForUri(uri, "tmp");

            @Media.MediaType int type = types.get(uri);

            if (!original.exists()) {
                if (isLocalFile) {
                    try {
                        FileUtils.copyFile(new File(uri.getPath()), original);
                    } catch (IOException e) {
                        // Swallow the exception, the logic below will handle the case of empty file.
                        Log.e("LoadMediaTask: doInBackground copyFile " + uri, e);
                    }
                } else {
                    FileUtils.uriToFile(getApplication(), uri, original);
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

            final Long date = dates.get(uri);
            result.add(new MediaModel(uri, originalMedia, editMedia, tmpMedia, editState, date != null ? date : 0L));
        }

        return result;
    }

    static class MediaModel {
        Uri uri;
        Media original;
        Media edit;
        Media tmp;
        Parcelable state;
        Parcelable tmpState;
        long date;

        MediaModel(Uri uri, Media original, Media edit, Media tmp, Parcelable state, long date) {
            this.uri = uri;
            this.original = original;
            this.edit = edit;
            this.tmp = tmp;
            this.state = state;
            this.date = date;
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
