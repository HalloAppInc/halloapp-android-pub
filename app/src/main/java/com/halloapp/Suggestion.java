package com.halloapp;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.util.TimeUtils;

import java.util.List;

public class Suggestion {

    private final double MAX_DISTANCE_IN_METERS = 100;

    public String id;
    public int size;
    public String locationName;
    public String locationAddress;
    public double longitude;
    public double latitude;
    public long timestamp;
    public GalleryItem[] thumbnails;
    public boolean isScored;
    public boolean isPlaceholder;

    public Suggestion() {
        isPlaceholder = true;
    }

    public Suggestion(@NonNull String id, double latitude, double longitude, long timestamp) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public Suggestion(@NonNull String id, double latitude, double longitude, @Nullable String locationName, @Nullable String locationAddress, long timestamp, int size, boolean isScored) {
        this(id, latitude, longitude, timestamp);
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.size = size;
        this.isScored = isScored;
    }

    // TODO: photos taken around midnight will not be clustered together
    public boolean isWithinRange(@Nullable List<GalleryItem> existingItems, @NonNull GalleryItem galleryItem) {
        if (existingItems == null || !TimeUtils.isSameDay(this.timestamp, galleryItem.date)) {
            return false;
        }
        for (GalleryItem item : existingItems) {
            float[] dist = new float[1];
            Location.distanceBetween(item.latitude, item.longitude, galleryItem.latitude, galleryItem.longitude, dist);
            if (dist[0] < MAX_DISTANCE_IN_METERS) {
                return true;
            }
        }
        return false;
    }
}
