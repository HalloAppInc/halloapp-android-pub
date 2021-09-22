package com.halloapp;

import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.id.GroupId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DebugStorageActivity extends HalloActivity {

    private StorageViewModel viewModel;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_storage_debug);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(StorageViewModel.class);

        TextView homeUsageTextView = findViewById(R.id.home_usage);
        TextView groupUsageTextView = findViewById(R.id.group_usage);
        TextView chatsUsageTextView = findViewById(R.id.chats_usage);
        TextView archiveUsageTextView = findViewById(R.id.archive_usage);
        TextView cacheUsageTextView = findViewById(R.id.cache_usage);
        TextView leakedMediaTextView = findViewById(R.id.leaked_usage);

        LinearLayout groupUsageBreakdown = findViewById(R.id.groups_children);
        LinearLayout chatsUsageBreakdown = findViewById(R.id.chats_children);
        LinearLayout leakedUsageBreakdown = findViewById(R.id.leaked_children);

        viewModel.homeUsageLiveData.observe(this, homeUsageTextView::setText);
        viewModel.groupsUsageLiveData.observe(this, groupUsageTextView::setText);
        viewModel.chatsUsageLiveData.observe(this, chatsUsageTextView::setText);
        viewModel.archiveUsageLiveData.observe(this, archiveUsageTextView::setText);
        viewModel.leakedMediaLiveData.observe(this, leakedMediaTextView::setText);
        viewModel.cacheUsage.getLiveData().observe(this, cacheUsageTextView::setText);

        viewModel.groupsUsageBreakdownLiveData.observe(this, map -> addDivisions(groupUsageBreakdown, map));
        viewModel.chatsUsageBreakdownLiveData.observe(this, map -> addDivisions(chatsUsageBreakdown, map));
        viewModel.leakedMediaBreakdownLiveData.observe(this, map -> addDivisions(leakedUsageBreakdown, map));
    }

    private void addDivisions(LinearLayout list, Map<String, Long> map) {
        list.removeAllViews();
        List<String> keys = new ArrayList<>(map.keySet());
        Comparator<String> orderBySizeComparator = (o1, o2) ->
                Long.compare(Preconditions.checkNotNull(map.get(o2)), Preconditions.checkNotNull(map.get(o1)));
        Collections.sort(keys, orderBySizeComparator);
        for (String key : keys) {
            View v = getLayoutInflater().inflate(R.layout.item_storage_usage, list, false);
            TextView label = v.findViewById(R.id.usage_label);
            TextView value = v.findViewById(R.id.usage_value);
            label.setText(key);
            value.setText(readableSize(Preconditions.checkNotNull(map.get(key))));
            list.addView(v);
        }
    }

    public static class StorageViewModel extends AndroidViewModel {

        public MutableLiveData<String> homeUsageLiveData = new MutableLiveData<>();
        public MutableLiveData<String> groupsUsageLiveData = new MutableLiveData<>();
        public MutableLiveData<String> chatsUsageLiveData = new MutableLiveData<>();
        public MutableLiveData<String> archiveUsageLiveData = new MutableLiveData<>();
        public MutableLiveData<String> leakedMediaLiveData = new MutableLiveData<>();
        public ComputableLiveData<String> cacheUsage;

        public MutableLiveData<Map<String, Long>> groupsUsageBreakdownLiveData = new MutableLiveData<>();
        public MutableLiveData<Map<String, Long>> chatsUsageBreakdownLiveData = new MutableLiveData<>();
        public MutableLiveData<Map<String, Long>> leakedMediaBreakdownLiveData = new MutableLiveData<>();

        public StorageViewModel(@NonNull Application application) {
            super(application);

            cacheUsage = new ComputableLiveData<String>() {
                @Override
                protected String compute() {
                    long homeUsage = 0;
                    long groupUsage = 0;
                    ConcurrentMap<String, Long> groupBreakdown = new ConcurrentHashMap<>();
                    List<Post> allPosts = ContentDb.getInstance().getAllPosts();
                    for (Post post : allPosts) {
                        GroupId parentGroup = post.getParentGroup();
                        for (Media media : post.media) {
                            if (media.file != null) {
                                if (parentGroup == null) {
                                    homeUsage += media.file.length();
                                } else {
                                    groupUsage += media.file.length();

                                    String key = parentGroup.rawId();
                                    groupBreakdown.putIfAbsent(key, 0L);
                                    groupBreakdown.put(key, groupBreakdown.get(key) + media.file.length());
                                }
                            }
                        }

                        for (Comment comment : ContentDb.getInstance().getAllComments(post.id)) {
                            for (Media media : comment.media) {
                                if (media.file != null) {
                                    if (parentGroup == null) {
                                        homeUsage += media.file.length();
                                    } else {
                                        groupUsage += media.file.length();

                                        String key = parentGroup.rawId();
                                        groupBreakdown.putIfAbsent(key, 0L);
                                        groupBreakdown.put(key, groupBreakdown.get(key) + media.file.length());
                                    }
                                }
                            }
                        }
                    }
                    homeUsageLiveData.postValue(readableSize(homeUsage));
                    groupsUsageLiveData.postValue(readableSize(groupUsage));
                    groupsUsageBreakdownLiveData.postValue(groupBreakdown);

                    long chatUsage = 0;
                    ConcurrentMap<String, Long> chatBreakdown = new ConcurrentHashMap<>();
                    List<Message> allMessages = ContentDb.getInstance().getAllMessages();
                    for (Message message : allMessages) {
                        for (Media media : message.media) {
                            if (media.file != null) {
                                chatUsage += media.file.length();

                                String key = message.chatId.rawId();
                                chatBreakdown.putIfAbsent(key, 0L);
                                chatBreakdown.put(key, chatBreakdown.get(key) + media.file.length());
                            }
                        }
                    }
                    chatsUsageLiveData.postValue(readableSize(chatUsage));
                    chatsUsageBreakdownLiveData.postValue(chatBreakdown);

                    long archiveUsage = 0;
                    List<Post> allArchived = ContentDb.getInstance().getArchivedPosts(null, null, false);
                    for (Post post : allArchived) {
                        for (Media media : post.media) {
                            if (media.file != null) {
                                archiveUsage += media.file.length();
                            }
                        }
                    }
                    archiveUsageLiveData.postValue(readableSize(archiveUsage));

                    File mediaDir = FileStore.getInstance().getMediaDir();

                    HashSet<String> allMediaFiles = new HashSet<>();
                    List<Media> medias = ContentDb.getInstance().getAllMedia();
                    for (Media m : medias) {
                        if (m != null && m.file != null) {
                            allMediaFiles.add(m.file.getAbsolutePath());
                        }
                    }

                    ConcurrentMap<String, Long> leakedBreakdown = new ConcurrentHashMap<>();
                    HashSet<File> leakedPaths = new HashSet<>();
                    findLeakedMedia(mediaDir, allMediaFiles, leakedPaths);
                    long leakedUsage = 0;
                    for (File f : leakedPaths) {
                        long len = f.length();
                        leakedUsage += len;
                        leakedBreakdown.put(f.getName(), len);
                        Log.e("DebugStorageActivity/leaked media found: " + f.getAbsolutePath() + " size: " + len);
                    }
                    leakedMediaLiveData.postValue(readableSize(leakedUsage));
                    leakedMediaBreakdownLiveData.postValue(leakedBreakdown);

                    File cacheDir = application.getCacheDir();
                    return readableSize(dirUsage(cacheDir));
                }
            };
        }

        private static void findLeakedMedia(File file, HashSet<String> mediaPaths, HashSet<File> leakedPaths) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files == null) {
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    findLeakedMedia(files[i], mediaPaths, leakedPaths);
                }
            } else {
                if (!mediaPaths.contains(file.getAbsolutePath())) {
                    // Leaked
                    leakedPaths.add(file);
                }
            }
        }

        private static long dirUsage(File file) {
            if (file == null || !file.exists()) {
                return 0;
            }
            if (file.isDirectory()) {
                long sum = 0;
                File[] files = file.listFiles();
                if (files != null) {
                    for (File subfile : files) {
                        sum += dirUsage(subfile);
                    }
                }
                return sum;
            }
            if (file.isFile()) {
                return file.length();
            }
            return 0;
        }
    }

    private static String readableSize(long size) {
        long factor = 1024;
        if (size < factor) {
            return size + " B";
        } else if (size < factor * factor) {
            return String.format(Locale.US, "%.2f", ((float) size) / factor) + " KiB";
        } else if (size < Math.pow(factor, 3)) {
            return String.format(Locale.US, "%.2f", ((float) size) / (factor * factor)) + " MiB";
        } else {
            return String.format(Locale.US, "%.2f", ((float) size) / Math.pow(factor, 3)) + " GiB";
        }
    }
}
