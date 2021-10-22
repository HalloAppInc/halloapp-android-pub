package com.halloapp.ui.groups;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import com.halloapp.BuildConfig;
import com.halloapp.content.ContentDb;
import com.halloapp.props.ServerProps;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogProvider;
import com.halloapp.util.stats.DecryptStats;
import com.halloapp.util.stats.GroupDecryptStats;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

public class GroupContentDecryptStatLoader extends ViewDataLoader<TextView, GroupDecryptStats, String> {

    private static final String successEmoji = "\u2714\uFE0E";
    private static final String failureEmoji ="\uD83D\uDCA5";

    private final LruCache<String, GroupDecryptStats> cache = new LruCache<>(512);
    private final ServerProps serverProps = ServerProps.getInstance();
    private final ContentDb contentDb;

    public GroupContentDecryptStatLoader() {
        contentDb = ContentDb.getInstance();
    }

    private static String getEmoji(boolean success) {
        return success ? successEmoji : failureEmoji;
    }

    @MainThread
    public void loadPost(@NonNull TextView view, @NonNull String postId) {
        final Callable<GroupDecryptStats> loader = () -> contentDb.getGroupPostDecryptStats(postId);
        load(view, postId, loader);
    }

    @MainThread
    public void loadComment(@NonNull TextView view, @NonNull String commentId) {
        final Callable<GroupDecryptStats> loader = () -> contentDb.getGroupCommentDecryptStats(commentId);
        load(view, commentId, loader);
    }

    @MainThread
    public void load(@NonNull TextView view, @NonNull String contentId, @NonNull Callable<GroupDecryptStats> loader) {
        final Displayer<TextView, GroupDecryptStats> displayer = new Displayer<TextView, GroupDecryptStats>() {

            @SuppressLint("SetTextI18n")
            @Override
            public void showResult(@NonNull TextView view, GroupDecryptStats result) {
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
                        Log.e("Sending logs related to message " + contentId);
                        if (BuildConfig.DEBUG) {
                            LogProvider.openDebugLogcatIntent(v.getContext(), contentId);
                        } else {
                            LogProvider.openEmailLogIntent(v.getContext(), contentId);
                        }
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
