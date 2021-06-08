package com.halloapp.ui.mediaedit;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
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
import java.util.List;
import java.util.Map;

public class MediaEditViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Model>> media = new MutableLiveData<>();
    private final MutableLiveData<Model> selected = new MutableLiveData<>();
    private final FileStore store = FileStore.getInstance();
    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private boolean isSaving = false;

    public MediaEditViewModel(@NonNull Application application) {
        super(application);
    }

    public @NonNull LiveData<List<Model>> getMedia() {
        return media;
    }

    public @NonNull LiveData<Model> getSelected() {
        return selected;
    }

    public int getSelectedPosition() {
        List<Model> models = media.getValue();
        return models == null ? -1 : models.indexOf(selected.getValue());
    }

    public void setMedia(List<Uri> uris, Bundle state, int position) {
        if (uris == null || isSaving) {
            return;
        }

        bgWorkers.execute(() -> {
            List<Model> models = load(uris, state);
            media.postValue(models);

            if (0 <= position && position < models.size()) {
                selected.postValue(models.get(position));
            }
        });
    }

    public void select(int position) {
        List<Model> models = media.getValue();

        if (!isSaving && models != null && 0 <= position && position < models.size()) {
            selected.postValue(models.get(position));
        }
    }

    public void move(int original, int target) {
        if (original == target || isSaving) {
            return;
        }

        List<Model> models = media.getValue();

        if (models != null) {
            models.add(target, models.remove(original));
            media.setValue(models);
        }
    }

    public void update(Model model, Parcelable state) {
        List<Model> models = media.getValue();
        if (models == null || !models.contains(model) || isSaving) {
            return;
        }

        model.tmpState = state;
        media.postValue(models);
    }

    public void reset() {
        List<Model> models = media.getValue();
        if (models == null || isSaving) {
            return;
        }

        for (Model m : models) {
            m.reset();
        }

        media.postValue(models);
    }

    public ArrayList<Uri> getUris() {
        List<Model> models = media.getValue();

        if (models == null) {
            return null;
        }

        ArrayList<Uri> result = new ArrayList<>(models.size());
        for (Model m : models) {
            result.add(m.uri);
        }

        return result;
    }

    public Bundle getState() {
        List<Model> models = media.getValue();

        if (models == null) {
            return null;
        }

        Bundle result = new Bundle();
        for (Model m : models) {
            result.putParcelable(m.uri.toString(), m.state);
        }

        return result;
    }

    @WorkerThread
    public void save(Context context) {
        List<Model> models = getMedia().getValue();
        if (models == null || isSaving) {
            return;
        }

        isSaving = true;
        for (Model m : models) {
            m.save(context);
        }
    }

    @WorkerThread
    private void copyUriToFile(Uri uri, File file) throws IOException {
        if (!file.exists()) {
            String path = uri.getPath();
            if ("file".equalsIgnoreCase(uri.getScheme()) && path != null) {
                FileUtils.copyFile(new File(path), file);
            } else {
                FileUtils.uriToFile(getApplication(), uri, file);
            }
        }
    }

    @WorkerThread
    private List<Model> load(List<Uri> uris, Bundle state) {
        List<Model> models = new ArrayList<>(uris.size());
        Map<Uri, Integer> types = MediaUtils.getMediaTypes(getApplication(), uris);
        Map<Uri, Long> dates = MediaUtils.getDates(getApplication(), uris);

        for (Uri uri : uris) {
            File original = store.getTmpFileForUri(uri, null);
            File edit = store.getTmpFileForUri(uri, "edit");

            try {
                copyUriToFile(uri, original);
            } catch (IOException e) {
                Log.e("MediaEditViewModel: copyUriToFile " + uri, e);
                continue;
            }

            Integer type = types.get(uri);
            if (type == null) {
                Log.w("MediaEditViewModel: unable to determine media type " + uri);
                continue;
            }

            Media originalMedia = Media.createFromFile(type, original);
            Media editMedia = Media.createFromFile(type, edit);

            Parcelable editState = null;
            if (state != null) {
                editState = state.getParcelable(uri.toString());
            }

            Long date = dates.get(uri);
            models.add(new Model(uri, originalMedia, editMedia, editState, date != null ? date : 0L));
        }

        return models;
    }

    static class Model {
        Uri uri;
        Media original;
        Media edit;
        Parcelable state;
        Parcelable tmpState;
        long date;

        Model(Uri uri, Media original, Media edit, Parcelable state, long date) {
            this.uri = uri;
            this.original = original;
            this.edit = edit;
            this.state = state;
            this.date = date;
        }

        public Media getMedia() {
            if (state != null && edit != null && edit.file.exists()) {
                return edit;
            }

            return original;
        }

        public Parcelable getState() {
            if (tmpState != null) {
                return tmpState;
            }

            if (state != null && edit != null) {
                return state;
            }

            return null;
        }

        public int getType() {
            return original.type;
        }

        @WorkerThread
        public void save(Context context) {
            if (tmpState != null) {
                state = tmpState;

                switch (getType()) {
                case Media.MEDIA_TYPE_IMAGE:
                    ImageCropper.crop(context, original.file, edit.file, (EditImageView.State) state);
                    break;
                case Media.MEDIA_TYPE_VIDEO:
                    VideoEditFragment.State videoState = (VideoEditFragment.State) state;
                    try {
                        MediaUtils.trimVideo(context, original.file, edit.file, videoState.startUs, videoState.endUs, videoState.mute);
                    } catch (IOException e) {
                        Log.w("MediaEditViewModel: unable to trim file");
                    }
                    break;
                }
            } else if (state == null && edit.file.exists()) {
                edit.file.delete();
            }
        }

        public void reset() {
            state = tmpState = null;
        }
    }
}
