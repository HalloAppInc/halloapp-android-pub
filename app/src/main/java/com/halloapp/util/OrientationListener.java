package com.halloapp.util;

import android.content.Context;
import android.view.OrientationEventListener;
import android.view.Surface;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.util.logs.Log;

public class OrientationListener extends OrientationEventListener {
    public static final int[] ROTATION_ANGLES = new int[]{0, 90, 180, 270};
    public static final int[] ROTATIONS = new int[]{
            Surface.ROTATION_0,
            Surface.ROTATION_270,
            Surface.ROTATION_180,
            Surface.ROTATION_90,
    };

    private final static int TRANSITION_THRESHOLD = 35;

    public static int getRotationAngle(int rotationMode) {
        for (int i = 0; i < OrientationListener.ROTATIONS.length; i++) {
            if (OrientationListener.ROTATIONS[i] == rotationMode) {
                return OrientationListener.ROTATION_ANGLES[i];
            }
        }
        return ROTATION_ANGLES[0];
    }

    private int orientationMode = 0;
    private final MutableLiveData<Integer> rotationMode = new MutableLiveData<>();

    public OrientationListener(Context context) {
        super(context);
    }

    public LiveData<Integer> getRotationMode() {
        return rotationMode;
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation == ORIENTATION_UNKNOWN) {
            return;
        }
        if ((360 - TRANSITION_THRESHOLD < orientation || orientation < TRANSITION_THRESHOLD) && this.orientationMode != 0) {
            orientationMode = 0;
            rotationMode.setValue(ROTATIONS[orientationMode]);
            return;
        }
        for (int i = 1; i < ROTATION_ANGLES.length; ++i) {
            if (ROTATION_ANGLES[i] - TRANSITION_THRESHOLD < orientation && orientation < ROTATION_ANGLES[i] + TRANSITION_THRESHOLD && this.orientationMode != i) {
                orientationMode = i;
                rotationMode.setValue(ROTATIONS[orientationMode]);
                break;
            }
        }
    }
}
