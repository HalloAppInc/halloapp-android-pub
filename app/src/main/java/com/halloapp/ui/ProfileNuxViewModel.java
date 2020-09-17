package com.halloapp.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.halloapp.Preferences;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;

public class ProfileNuxViewModel extends AndroidViewModel {

    private final BgWorkers bgWorkers;
    private final Preferences preferences;

    final ComputableLiveData<Integer> showNux;

    public ProfileNuxViewModel(@NonNull Application application) {
        super(application);
        bgWorkers = BgWorkers.getInstance();
        preferences = Preferences.getInstance();

        showNux = new ComputableLiveData<Integer>() {
            @Override
            protected Integer compute() {
                if (preferences.getShowedMakePostNux()) {
                    return 0;
                }
                if (preferences.getShowedProfileNux()) {
                    return 2;
                }
                return 1;
            }
        };
    }

    public void closeProfileNux() {
        bgWorkers.execute(() -> {
            preferences.markProfileNuxShown();
            showNux.invalidate();
        });
    }

    public void onFabToggled() {
        Integer currentNux = showNux.getLiveData().getValue();
        if (currentNux == null) {
            return;
        }
        if (currentNux == 2) {
            bgWorkers.execute(() -> {
                preferences.markMakePostNuxShown();
                showNux.invalidate();
            });
        }
    }
}
