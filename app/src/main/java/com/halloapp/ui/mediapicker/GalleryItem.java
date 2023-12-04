package com.halloapp.ui.mediapicker;

import androidx.annotation.Nullable;
import java.util.Objects;

public class GalleryItem {

    public final long id;
    public final int type;
    public long date;
    public long duration;
    public double latitude;
    public double longitude;
    public String suggestionId;
    public boolean suggested;
    public float score;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GalleryItem galleryItem = (GalleryItem) o;
        return id == galleryItem.id &&
                type == galleryItem.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    public GalleryItem(long id, int type) {
        this.id = id;
        this.type = type;
    }

    public GalleryItem(long id, int type, long date, long duration) {
        this.id = id;
        this.type = type;
        this.date = date * 1000;
        this.duration = duration;
    }

    public GalleryItem(long id, int type, long date, long duration, double latitude, double longitude, @Nullable String suggestionId) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.duration = duration;
        this.latitude = latitude;
        this.longitude = longitude;
        this.suggestionId = suggestionId;
    }
}
