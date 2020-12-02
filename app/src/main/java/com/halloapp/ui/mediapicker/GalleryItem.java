package com.halloapp.ui.mediapicker;

import java.util.Calendar;
import java.util.Objects;

public class GalleryItem {

    final long id;
    final int type;
    final long date;
    final int year;
    final int month;
    final int day;
    final long duration;

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

    public GalleryItem(long id, int type, long date, long duration) {
        this.id = id;
        this.type = type;
        this.date = date * 1000;
        this.duration = duration;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.date);

        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH);
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
    }
}
