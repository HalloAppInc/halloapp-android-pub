package com.halloapp.ui.chat.chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;
import androidx.lifecycle.LifecycleOwner;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.props.ServerProps;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogProvider;
import com.halloapp.util.stats.DecryptStats;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

public class DecryptStatLoader extends ViewDataLoader<TextView, DecryptStats, String> {

    private final LruCache<String, DecryptStats> cache = new LruCache<>(512);
    private final ServerProps serverProps = ServerProps.getInstance();
    private final ContentDb contentDb;

    public DecryptStatLoader() {
        contentDb = ContentDb.getInstance();
    }

    private static String getEmoji(boolean success) {
        return success ? Constants.CRYPTO_SUCCESS_EMOJI : Constants.CRYPTO_FAILURE_EMOJI;
    }

    @MainThread
    public void load(@NonNull LifecycleOwner lifecycleOwner, @NonNull TextView view, @NonNull String messageId) {
        final Callable<DecryptStats> loader = () -> contentDb.getMessageDecryptStats(messageId);
        final ViewDataLoader.Displayer<TextView, DecryptStats> displayer = new ViewDataLoader.Displayer<TextView, DecryptStats>() {

            @SuppressLint("SetTextI18n")
            @Override
            public void showResult(@NonNull TextView view, DecryptStats result) {
                view.setText(" " + getEmoji(result.failureReason == null));

                view.setOnClickListener(v -> {
                    String outcome = result.failureReason == null ? "success" : result.failureReason;
                    String start = DateFormat.getInstance().format(new Date(result.originalTimestamp));
                    String end = DateFormat.getInstance().format(new Date(result.lastUpdatedTimestamp));

                    //noinspection StringBufferReplaceableByString
                    StringBuilder sb = new StringBuilder();
                    sb.append("Message ID: ").append(messageId.substring(0, Math.min(20, messageId.length()))).append("...\n");
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
                        Log.e("Sending logs related to message " + messageId);
                        Context context = view.getContext();
                        ProgressDialog progressDialog = ProgressDialog.show(context, null, context.getString(R.string.preparing_logs));
                        LogProvider.openLogIntent(context, messageId).observe(lifecycleOwner, intent -> {
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
            view.setVisibility(View.VISIBLE);
            load(view, loader, displayer, messageId, cache);
        } else {
            view.setVisibility(View.GONE);
        }
    }
}
