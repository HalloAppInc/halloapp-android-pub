package com.halloapp.katchup.compose;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.halloapp.content.Media;

public abstract class ComposeFragment extends Fragment {

    public abstract Media getComposedMedia();

    public abstract View getPreview();
}
