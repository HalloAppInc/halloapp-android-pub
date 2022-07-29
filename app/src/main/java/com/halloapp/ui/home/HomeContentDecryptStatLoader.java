package com.halloapp.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;
import androidx.lifecycle.LifecycleOwner;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.props.ServerProps;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogProvider;
import com.halloapp.util.stats.GroupDecryptStats;
import com.halloapp.util.stats.HomeDecryptStats;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

public class HomeContentDecryptStatLoader extends ViewDataLoader<TextView, HomeDecryptStats, String> {

    private final LruCache<String, HomeDecryptStats> cache = new LruCache<>(512);
    private final ServerProps serverProps = ServerProps.getInstance();
    private final ContentDb contentDb;

    public HomeContentDecryptStatLoader() {
        contentDb = ContentDb.getInstance();
    }

    private static String getEmoji(boolean success) {
        return success ? Constants.CRYPTO_SUCCESS_EMOJI : Constants.CRYPTO_FAILURE_EMOJI;
    }

    @MainThread
    public void loadPost(@NonNull LifecycleOwner lifecycleOwner, @NonNull TextView view, @NonNull String postId) {
        final Callable<HomeDecryptStats> loader = () -> contentDb.getHomePostDecryptStats(postId);
        load(lifecycleOwner, view, postId, loader);
    }

    @MainThread
    public void loadComment(@NonNull LifecycleOwner lifecycleOwner, @NonNull TextView view, @NonNull String commentId) {
        final Callable<HomeDecryptStats> loader = () -> contentDb.getHomeCommentDecryptStats(commentId);
        load(lifecycleOwner, view, commentId, loader);
    }

    @MainThread
    private void load(@NonNull LifecycleOwner lifecycleOwner, @NonNull TextView view, @NonNull String contentId, @NonNull Callable<HomeDecryptStats> loader) {
        final Displayer<TextView, HomeDecryptStats> displayer = new Displayer<TextView, HomeDecryptStats>() {

            @SuppressLint("SetTextI18n")
            @Override
            public void showResult(@NonNull TextView view, HomeDecryptStats result) {
                if (result == null) {
                    return;
                }
                view.setText(" " + getEmoji(result.failureReason == null));

                view.setOnClickListener(v -> {
                    String outcome = result.failureReason == null ? "success" : result.failureReason;
                    String start = DateFormat.getInstance().format(new Date(result.originalTimestamp));
                    String end = DateFormat.getInstance().format(new Date(result.lastUpdatedTimestamp));

                    //noinspection StringBufferReplaceableByString
                    StringBuilder sb = new StringBuilder();
                    sb.append("Content ID: ").append(contentId.substring(0, Math.min(20, contentId.length()))).append("...\n");
                    sb.append("Outcome: ").append(outcome).append("\n");
                    sb.append("Rerequest count: ").append(result.rerequestCount).append("\n");
                    sb.append("Sender: ").append(result.senderPlatform).append(" ").append(result.senderVersion).append("\n");
                    sb.append("Receiver: ").append("android ").append(result.version).append("\n");
                    sb.append("First seen: ").append(start).append("\n");
                    sb.append("Last updated: ").append(end);

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Send logs?");
                    builder.setMessage(sb.toString());
                    builder.setNegativeButton("No", (dialog, which) -> {
                        // just close
                    });
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        Log.e("Sending logs related to content " + contentId);
                        Context context = view.getContext();
                        ProgressDialog progressDialog = ProgressDialog.show(context, null, context.getString(R.string.preparing_logs));
                        LogProvider.openLogIntent(context, contentId).observe(lifecycleOwner, intent -> {
                            context.startActivity(intent);
                            progressDialog.dismiss();
                        });
                    });
                    builder.show();
                });
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                view.setText("");
            }
        };

        if (serverProps.getIsInternalUser()) {
            load(view, loader, displayer, contentId, cache);
        }
    }
}
