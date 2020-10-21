package com.halloapp.ui;

import android.app.Application;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.halloapp.Preferences;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ProfileNuxViewModel extends AndroidViewModel {

    private final BgWorkers bgWorkers;
    private final Preferences preferences;

    final ComputableLiveData<Integer> showNux;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Nux.NUX_NONE, Nux.NUX_PROFILE, Nux.NUX_MAKE_POST})
    public @interface Nux {
        int NUX_NONE = 0;
        int NUX_PROFILE = 1;
        int NUX_MAKE_POST = 2;
    }

    public ProfileNuxViewModel(@NonNull Application application) {
        super(application);
        bgWorkers = BgWorkers.getInstance();
        preferences = Preferences.getInstance();

        showNux = new ComputableLiveData<Integer>() {
            @Override
            protected Integer compute() {
                if (preferences.getShowedMakePostNux()) {
                    return Nux.NUX_NONE;
                }
                if (preferences.getShowedProfileNux()) {
                    return Nux.NUX_MAKE_POST;
                }
                return Nux.NUX_PROFILE;
            }
        };
    }

    public void closeProfileNux() {
        bgWorkers.execute(() -> {
            preferences.markProfileNuxShown();
            showNux.invalidate();
        });
    }

    public boolean dismissMakePostNuxIfOpen() {
        Integer currentNux = showNux.getLiveData().getValue();
        if (currentNux == null) {
            return false;
        }
        if (currentNux == 2) {
            bgWorkers.execute(() -> {
                preferences.markMakePostNuxShown();
                showNux.invalidate();
            });
            return true;
        }
        return false;
    }
}
