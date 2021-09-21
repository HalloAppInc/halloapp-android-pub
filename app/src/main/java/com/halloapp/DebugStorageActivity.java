package com.halloapp;

import android.app.Application;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.id.GroupId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class DebugStorageActivity extends HalloActivity {

    private StorageViewModel viewModel;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_storage_debug);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(StorageViewModel.class);

        TextView homeUsageTextView = findViewById(R.id.home_usage);
        viewModel.homeUsageLiveData.observe(this, homeUsageTextView::setText);

        TextView groupUsageTextView = findViewById(R.id.group_usage);
        viewModel.groupsUsageLiveData.observe(this, groupUsageTextView::setText);

        TextView chatsUsageTextView = findViewById(R.id.chats_usage);
        viewModel.chatsUsageLiveData.observe(this, chatsUsageTextView::setText);

        TextView archiveUsageTextView = findViewById(R.id.archive_usage);
        viewModel.archiveUsageLiveData.observe(this, archiveUsageTextView::setText);

        TextView cacheUsageTextView = findViewById(R.id.cache_usage);
        viewModel.cacheUsage.getLiveData().observe(this, cacheUsageTextView::setText);
    }

    public static class StorageViewModel extends AndroidViewModel {

        public MutableLiveData<String> homeUsageLiveData = new MutableLiveData<>();
        public MutableLiveData<String> groupsUsageLiveData = new MutableLiveData<>();
        public MutableLiveData<String> chatsUsageLiveData = new MutableLiveData<>();
        public MutableLiveData<String> archiveUsageLiveData = new MutableLiveData<>();
        public ComputableLiveData<String> cacheUsage;

        public StorageViewModel(@NonNull Application application) {
            super(application);

            cacheUsage = new ComputableLiveData<String>() {
                @Override
                protected String compute() {
                    long homeUsage = 0;
                    long groupUsage = 0;
                    List<Post> allPosts = ContentDb.getInstance().getAllPosts();
                    for (Post post : allPosts) {
                        GroupId parentGroup = post.getParentGroup();
                        for (Media media : post.media) {
                            if (parentGroup == null) {
                                homeUsage += media.file.length();
                            } else {
                                groupUsage += media.file.length();
                            }
                        }
                    }
                    homeUsageLiveData.postValue(readableSize(homeUsage));
                    groupsUsageLiveData.postValue(readableSize(groupUsage));

                    long chatUsage = 0;
                    List<Message> allMessages = ContentDb.getInstance().getAllMessages();
                    for (Message message : allMessages) {
                        for (Media media : message.media) {
                            chatUsage += media.file.length();
                        }
                    }
                    chatsUsageLiveData.postValue(readableSize(chatUsage));

                    long archiveUsage = 0;
                    List<Post> allArchived = ContentDb.getInstance().getArchivedPosts(null, null, false);
                    for (Post post : allArchived) {
                        for (Media media : post.media) {
                            archiveUsage += media.file.length();
                        }
                    }
                    archiveUsageLiveData.postValue(readableSize(archiveUsage));

                    File cacheDir = application.getCacheDir();
                    return readableSize(dirUsage(cacheDir));
                }
            };
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
}
