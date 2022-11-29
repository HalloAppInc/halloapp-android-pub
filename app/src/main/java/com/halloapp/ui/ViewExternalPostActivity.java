package com.halloapp.ui;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.MainActivity;
import com.halloapp.Me;
import com.halloapp.RegistrationRequestActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.util.Locale;

public class ViewExternalPostActivity extends HalloActivity {

    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(this));

        MutableLiveData<Boolean> isRegistered = new MutableLiveData<>();
        bgWorkers.execute(() -> isRegistered.postValue(Me.getInstance().isRegistered()));

        SharedUrlParts parsedUrl = tryParseUri(getIntent().getData());
        if (parsedUrl == null) {
            Log.e("ViewExternalPostActivity/onCreate missing url parts");
            finish();
            return;
        }

        isRegistered.observe(this, registered -> {
            TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
            taskStackBuilder.addNextIntent(new Intent(this, registered ? MainActivity.class : RegistrationRequestActivity.class));
            taskStackBuilder.addNextIntent(PostContentActivity.openExternal(this, parsedUrl.id, parsedUrl.key));
            taskStackBuilder.startActivities();
            finish();
        });
    }

    @Nullable
    private SharedUrlParts tryParseUri(@Nullable Uri uri) {
        if (uri == null || uri.getHost() == null) {
            return null;
        }
        String hostLowerCase = uri.getHost().toLowerCase(Locale.ROOT);
        if (hostLowerCase.startsWith("share.") || hostLowerCase.startsWith("share-test.")) {
            String id = uri.getLastPathSegment();
            String fragment = uri.getFragment();
            if (id == null || fragment == null || !fragment.startsWith("k")) {
                Log.w("Tried to parse invalid external post uri");
                return null;
            }
            String key = fragment.substring(1);
            return new SharedUrlParts(id, key);
        }
        return null;
    }

    private static class SharedUrlParts {
        String id;
        String key;

        public SharedUrlParts(String id, String key) {
            this.id = id;
            this.key = key;
        }
    }
}
