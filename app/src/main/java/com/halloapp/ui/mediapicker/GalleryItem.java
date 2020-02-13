package com.halloapp.ui.mediapicker;

import java.util.Objects;

public class GalleryItem {

    final long id;
    final int type;

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
}
