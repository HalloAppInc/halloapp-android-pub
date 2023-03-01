package com.halloapp.ui.mediapicker;

import java.util.Calendar;
import java.util.Objects;

public class GalleryItem {

    public final long id;
    public final int type;
    public final long date;
    public final int year;
    public final int month;
    public final int day;
    public final long duration;

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
