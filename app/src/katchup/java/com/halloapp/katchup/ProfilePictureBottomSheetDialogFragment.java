package com.halloapp.katchup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.id.UserId;
import com.halloapp.katchup.avatar.KAvatarLoader;
import com.halloapp.ui.HalloBottomSheetDialogFragment;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ProfilePictureBottomSheetDialogFragment extends HalloBottomSheetDialogFragment {
    public final static int ACTION_GALLERY = 1;
    public final static int ACTION_CAMERA = 2;
    public final static int ACTION_REMOVE = 3;

    public interface ProfilePictureBottomSheetCallback {
        void onActionSelected(int action);
    }

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final KAvatarLoader kAvatarLoader = KAvatarLoader.getInstance();
    private final ProfilePictureBottomSheetCallback callback;

    public ProfilePictureBottomSheetDialogFragment(@NonNull ProfilePictureBottomSheetCallback callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_picture_bottom_sheet, container, false);

        View gallery = view.findViewById(R.id.gallery);
        View camera = view.findViewById(R.id.camera);
        View remove = view.findViewById(R.id.remove);
        View cancel = view.findViewById(R.id.cancel);

        gallery.setOnClickListener(v -> {
            callback.onActionSelected(ACTION_GALLERY);
            dismiss();
        });

        camera.setOnClickListener(v -> {
            callback.onActionSelected(ACTION_CAMERA);
            dismiss();
        });

        remove.setOnClickListener(v -> {
            callback.onActionSelected(ACTION_REMOVE);
            dismiss();
        });

        cancel.setOnClickListener(v -> dismiss());

        bgWorkers.execute(() -> {
            if (!kAvatarLoader.hasAvatar(UserId.ME)) {
                remove.post(() -> remove.setVisibility(View.GONE));
            }
        });

        return view;
    }
}
