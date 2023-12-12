package com.halloapp.ui;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.AppContext;
import com.halloapp.Preferences;
import com.halloapp.Suggestion;
import com.halloapp.content.ContentDb;
import com.halloapp.proto.server.GpsLocation;
import com.halloapp.ui.mediapicker.GalleryDataSource;
import com.halloapp.ui.mediapicker.GalleryItem;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ReverseGeocodeResponseIq;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RequiresApi(api = 24)
public class MagicPostViewModel extends AndroidViewModel {

    private static final int MIN_GALLERY_ITEMS_FOR_SUGGESTION = 3;
    private static final int GALLERY_SIZE = 100;
    private static final int MAX_DISTANCE_IN_METERS = 100;
    private static final long CUTOFF_TIME_IN_SECONDS = (System.currentTimeMillis() - (2 * DateUtils.WEEK_IN_MILLIS)) / 1000;
    private static final int NUM_THUMBNAILS = 4;

    private final ContentResolver contentResolver;
    private final ContentDb contentDb;
    private final Preferences preferences = Preferences.getInstance();
    private final BgWorkers bgWorkers;
    private final Point displaySize;
    private final MutableLiveData<ArrayList<Suggestion>> suggestions;
    private final MutableLiveData<Boolean> showedMagicPostNux = new MutableLiveData<>();

    public MagicPostViewModel(@NonNull Application application, @NonNull ContentDb contentDb, @NonNull BgWorkers bgWorkers, @NonNull Point displaySize) {
        super(application);
        this.contentResolver = getApplication().getContentResolver();
        this.suggestions = new MutableLiveData<>();
        this.contentDb = contentDb;
        this.bgWorkers = bgWorkers;
        this.displaySize = displaySize;
        getSuggestions();
        bgWorkers.execute(() -> showedMagicPostNux.postValue(preferences.getPrefShowedMagicPostNux()));
    }

    public MutableLiveData<ArrayList<Suggestion>> getSuggestionsList() {
        return suggestions;
    }

    public MutableLiveData<Boolean> getShowedMagicPostNux() {
        return showedMagicPostNux;
    }

    public void setShowMagicPostNux(boolean showed) {
        bgWorkers.execute(() -> {
            showedMagicPostNux.postValue(showed);
            preferences.setPrefShowedMagicPostNux(showed);
        });
    }

    public void getSuggestions() {
        bgWorkers.execute(() -> {
            ArrayList<Suggestion> existingSuggestions = contentDb.getAllSuggestions();
            ArrayList<Suggestion> newSuggestions = getPendingSuggestions(existingSuggestions);
            if (existingSuggestions.isEmpty() && newSuggestions.isEmpty()) {
                Log.i("No suggestions from db, generating from gallery items");
                newSuggestions = getNewSuggestions();
            }
            contentDb.addAllSuggestions(newSuggestions);
            ArrayList<Suggestion> filteredSuggestions = new ArrayList<>();
            for (Suggestion suggestion : newSuggestions) {
                if (suggestion.size < MIN_GALLERY_ITEMS_FOR_SUGGESTION) {
                    continue;
                }
                // TODO(michelle): thumbnails are recalculated each time (consider saving in db)
                suggestion.thumbnails = suggestion.thumbnails == null ? contentDb.getThumbnailPhotosBySuggestion(suggestion.id) : suggestion.thumbnails;
                if (suggestion.thumbnails[0] != null) {
                    filteredSuggestions.add(suggestion);
                }
            }
            suggestions.postValue(filteredSuggestions);
        });
    }

    @WorkerThread
    private ArrayList<Suggestion> getNewSuggestions() {
        GalleryDataSource source = new GalleryDataSource(contentResolver, true, CUTOFF_TIME_IN_SECONDS);
        List<GalleryItem> galleryItems = source.load(null, false, GALLERY_SIZE);
        return clusterGalleryItems(galleryItems, new ArrayList<>());
    }

    @WorkerThread
    private ArrayList<Suggestion> getPendingSuggestions(@NonNull ArrayList<Suggestion> existingSuggestions) {
        ArrayList<GalleryItem> galleryItems = contentDb.getPendingGalleryItems(-1);
        return clusterGalleryItems(galleryItems, existingSuggestions);
    }

    @WorkerThread
    private ArrayList<Suggestion> clusterGalleryItems(@NonNull List<GalleryItem> galleryItems, @NonNull ArrayList<Suggestion> existingSuggestions) {
        LinkedHashMap<Suggestion, List<GalleryItem>> updatedSuggestions = new LinkedHashMap<>();
        List<GalleryItem> screenshots = new ArrayList<>();

        for (GalleryItem galleryItem : galleryItems) {
            Uri uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri(GalleryDataSource.MEDIA_VOLUME), galleryItem.id);

            if (galleryItem.type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE && isScreenshot(uri)) {
                screenshots.add(galleryItem);
                continue;
            }

            double[] latLong = getExifCoordinates(uri, galleryItem.type);
            galleryItem.latitude = latLong[0];
            galleryItem.longitude = latLong[1];

            // If a gallery item matches to an existing suggestion, move to updatedSuggestions (where clustering happens)
            Iterator<Suggestion> iterator = existingSuggestions.iterator();
            while (iterator.hasNext()) {
                Suggestion suggestion = iterator.next();
                List<GalleryItem> existingItemList = ContentDb.getInstance().getGalleryItemsBySuggestion(suggestion.id);
                if (suggestion.isWithinRange(existingItemList, galleryItem)) {
                    iterator.remove();
                    updatedSuggestions.computeIfAbsent(suggestion, s -> new ArrayList<>()).addAll(existingItemList);
                }
            }

            // If Photo A belongs in Suggestion A, 'cluster' becomes the list of all gallery items also in Suggestion A.
            // If Photo A later matches to Suggestion B, all of B's items will be added to 'cluster'
            // and Suggestion B will be removed (set to null to avoid ConcurrentModificationException)
            List<GalleryItem> cluster = null;
            for (Suggestion suggestion : updatedSuggestions.keySet()) {
                List<GalleryItem> existingItemList = updatedSuggestions.get(suggestion);
                if (existingItemList == null || !suggestion.isWithinRange(existingItemList, galleryItem)) {
                    continue;
                }
                if (cluster == null) {
                    cluster = existingItemList;
                    cluster.add(galleryItem);
                    // Adding a photo should trigger the rescoring of photos within that suggestion
                    suggestion.isScored = false;
                } else {
                    cluster.addAll(existingItemList);
                    updatedSuggestions.put(suggestion, null);
                }
            }

            // Unable to find a suggestion, create a new one
            if (cluster == null) {
                Suggestion suggestion = new Suggestion(RandomId.create(), galleryItem.latitude, galleryItem.longitude, galleryItem.date);
                String[] location = getLocation(suggestion.latitude, suggestion.longitude);
                suggestion.locationName = location[0];
                suggestion.locationAddress = location[1];
                suggestion.timestamp = galleryItem.date;
                updatedSuggestions.computeIfAbsent(suggestion, s -> new ArrayList<>()).add(galleryItem);
            }
        }

        for (Map.Entry<Suggestion, List<GalleryItem>> entry : updatedSuggestions.entrySet()) {
            int size = entry.getValue() == null ? 0 : entry.getValue().size();
            entry.getKey().size = size;
            GalleryItem[] thumbnails = new GalleryItem[NUM_THUMBNAILS];
            for (int i = 0; i < Math.min(NUM_THUMBNAILS, size); i++) {
                thumbnails[i] = entry.getValue().get(i);
            }
            entry.getKey().thumbnails = thumbnails;
        }
        contentDb.addAllGalleryItems(updatedSuggestions);
        contentDb.deleteGalleryItems(screenshots);

        ArrayList<Suggestion> allSuggestions= new ArrayList<>(updatedSuggestions.keySet());
        allSuggestions.addAll(existingSuggestions);
        return allSuggestions;
    }

    @WorkerThread
    private String[] getLocation(double latitude, double longitude) {
        String pendingPlace = null;
        String pendingAddress = null;
        if (latitude == 0 && longitude == 0) {
            return new String[] {null, null};
        }
        try {
            // Use server-provided location if it's within MAX_DISTANCE of the coordinates
            ReverseGeocodeResponseIq res = Connection.getInstance().getGeocodeLocation(latitude, longitude).await();
            if (res != null && res.success && res.location != null) {
                GpsLocation serverLocation = res.location.getLocation();
                float[] distance = new float[1];
                Location.distanceBetween(latitude, longitude, serverLocation.getLatitude(), serverLocation.getLongitude(), distance);
                if (distance[0] < MAX_DISTANCE_IN_METERS) {
                    pendingPlace = res.location.getName();
                    pendingAddress = res.location.getAddress();
                }
            } else if (res != null) {
                Log.w("MagicPostViewModel could not get location: " + res.reason);
            }
            // Otherwise, use Google geocoder
            if (pendingPlace == null || pendingAddress == null) {
                Geocoder geocoder = new Geocoder(getApplication().getBaseContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && addresses.size() > 0) {
                    pendingPlace = pendingPlace == null ? addresses.get(0).getLocality() : pendingPlace;
                    pendingAddress = pendingAddress == null ? addresses.get(0).getThoroughfare() : pendingAddress;
                }
            }
        } catch (IOException | ObservableErrorException | InterruptedException err) {
            Log.e("Failed to reverse geocode: ", err);
        }
        return new String[] {pendingPlace, pendingAddress};
    }

    @WorkerThread
    private double[] getExifCoordinates(@NonNull Uri uri, int type) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        double[] latLong = new double[2];
        try {
            if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                retriever.setDataSource(AppContext.getInstance().get(), uri);
                String latLongStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION);
                if (latLongStr != null) {
                    // Format of location: https://developer.android.com/reference/android/media/MediaMetadataRetriever#METADATA_KEY_LOCATION
                    latLongStr = latLongStr.replaceAll("/", "");
                    int longitudeIndex = latLongStr.lastIndexOf("+");
                    longitudeIndex = longitudeIndex > 0 ? longitudeIndex : latLongStr.lastIndexOf("-");
                    latLong[0] = Location.convert(latLongStr.substring(0, longitudeIndex));
                    latLong[1] = Location.convert(latLongStr.substring(longitudeIndex));
                }
            } else if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                ExifInterface exif = new ExifInterface(contentResolver.openInputStream(uri));
                // Format of location: https://developer.android.com/reference/androidx/exifinterface/media/ExifInterface#getLatLong()
                latLong = exif.getLatLong();
            }
        } catch (IOException e) {
            Log.e("Failed to open uri: " + uri, e);
        } catch (IllegalArgumentException e) {
            Log.e("Failed to geocode with invalid uri: " + uri, e);
        } catch (RuntimeException e) {
            Log.e("Failed to geocode for other reason with uri : " + uri, e);
        } finally  {
            try {
                retriever.release();
            } catch (IOException e) {
                Log.e("MagicPostViewModel retriever release failed", e);
            }
        }
        return latLong == null ? new double[] {0,0} : latLong;
    }

    private boolean isScreenshot(@NonNull Uri uri) {
        try {
            ExifInterface exif = new ExifInterface(contentResolver.openInputStream(uri));
            int width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
            int height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
            return (displaySize.x == width && displaySize.y == height) || (displaySize.x == height && displaySize.y == width);
        } catch (IOException e) {
            Log.e("MagicPostViewModel/isScreenshot IOException", e);
        }
        return false;
    }

    public void invalidate() {
        getSuggestions();
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final ContentDb contentDb;
        private final BgWorkers bgWorkers;
        private final Point displaySize;

        public Factory(@NonNull Application application, @NonNull ContentDb contentDb, @NonNull BgWorkers bgWorkers, @NonNull Point displaySize) {
            this.application = application;
            this.contentDb = contentDb;
            this.bgWorkers = bgWorkers;
            this.displaySize = displaySize;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(MagicPostViewModel.class)) {
                //noinspection unchecked
                return (T) new MagicPostViewModel(application, contentDb, bgWorkers, displaySize);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
