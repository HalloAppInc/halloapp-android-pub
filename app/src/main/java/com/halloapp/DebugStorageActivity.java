package com.halloapp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.id.GroupId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.FileUtils;
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

    private final HashMap<String, Long> ids = new HashMap<>();
    private long id = 0;

    private String homeUsage;
    private String groupUsage;
    private String chatsUsage;
    private String archiveUsage;
    private String leakedMediaUsage;
    private String internalUsage;

    private List<Item> groupsBreakdown;
    private List<Item> chatsBreakdown;
    private List<Item> internalBreakdown;
    private List<Item> leakedBreakdown;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_storage_debug);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(StorageViewModel.class);

        RecyclerView storageRv = findViewById(R.id.storage_rv);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        storageRv.setLayoutManager(layoutManager);

        StorageAdapter adapter = new StorageAdapter();
        adapter.setStorageItems(constructList());
        storageRv.setAdapter(adapter);

        viewModel.homeUsageLiveData.observe(this, t -> {
            homeUsage = t;
            adapter.setStorageItems(constructList());
        });
        viewModel.groupsUsageLiveData.observe(this, t -> {
            groupUsage = t;
            adapter.setStorageItems(constructList());
        });
        viewModel.chatsUsageLiveData.observe(this, t -> {
            chatsUsage = t;
            adapter.setStorageItems(constructList());
        });
        viewModel.archiveUsageLiveData.observe(this, t -> {
            archiveUsage = t;
            adapter.setStorageItems(constructList());
        });
        viewModel.leakedMediaLiveData.observe(this, t -> {
            leakedMediaUsage = t;
            adapter.setStorageItems(constructList());
        });
        viewModel.internalUsage.getLiveData().observe(this, t -> {
            internalUsage = t;
            adapter.setStorageItems(constructList());
        });

        viewModel.groupsUsageBreakdownLiveData.observe(this, map -> {
            groupsBreakdown = convertMapToList(map);
            adapter.setStorageItems(constructList());
        });
        viewModel.chatsUsageBreakdownLiveData.observe(this, map -> {
            chatsBreakdown = convertMapToList(map);
            adapter.setStorageItems(constructList());
        });
        viewModel.internalUsageBreakdownLiveData.observe(this, map -> {
            internalBreakdown = convertMapToList(map);
            adapter.setStorageItems(constructList());
        });
        viewModel.leakedMediaBreakdownLiveData.observe(this, map -> {
            leakedBreakdown = convertMapToList(map);
            adapter.setStorageItems(constructList());
        });
    }

    private List<Item> convertMapToList(Map<String, Long> map) {
        List<String> keys = new ArrayList<>(map.keySet());
        Comparator<String> orderBySizeComparator = (o1, o2) ->
                Long.compare(Preconditions.checkNotNull(map.get(o2)), Preconditions.checkNotNull(map.get(o1)));
        Collections.sort(keys, orderBySizeComparator);
        List<Item> items = new ArrayList<>(keys.size());
        for (String key : keys) {
            items.add(new StorageItem(key, FileUtils.getReadableFileSize(Preconditions.checkNotNull(map.get(key)))));
        }
        return items;
    }

    private List<Item> constructList() {
        List<Item> list = new ArrayList<>();
        list.add(new CategoryItem("Home feed", homeUsage));
        list.add(new CategoryItem("Group feed", groupUsage));
        if (groupsBreakdown != null) {
            list.addAll(groupsBreakdown);
        }
        list.add(new CategoryItem("Chats", chatsUsage));
        if (chatsBreakdown != null) {
            list.addAll(chatsBreakdown);
        }
        list.add(new CategoryItem("Archive", archiveUsage));
        list.add(new CategoryItem("Other internal", internalUsage));
        if (internalBreakdown != null) {
            list.addAll(internalBreakdown);
        }
        list.add(new CategoryItem("Leaked media", leakedMediaUsage));
        if (leakedBreakdown != null) {
            list.addAll(leakedBreakdown);
        }
        return list;
    }

    private abstract class Item {
        public Item(String name, String text) {
            this.name = name;
            this.text = text;
        }
        public abstract long getId();
        String name;
        String text;
    }

    private class StorageItem extends Item {

        public StorageItem(String name, String text) {
            super(name, text);
        }

        public long getId() {
            String idKey = "item-" + name;
            if (!ids.containsKey(idKey)) {
                ids.put(idKey, id);
                id++;
            }
            return ids.get(idKey);
        }
    }

    private class CategoryItem extends Item {
        public CategoryItem(String name, String text) {
            super(name, text);
        }
        public long getId() {
            String idKey = "category-" + name;
            if (!ids.containsKey(idKey)) {
                ids.put(idKey, id);
                id++;
            }
            return ids.get(idKey);
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        TextView categoryName;
        TextView itemName;
        TextView usage;
        View loading;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            usage = itemView.findViewById(R.id.usage_amount);
            itemName = itemView.findViewById(R.id.item_name);
            categoryName = itemView.findViewById(R.id.category_name);
            loading = itemView.findViewById(R.id.loading);
        }
    }

    private class StorageAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final int TYPE_HEADER = 1;
        private final int TYPE_ITEM = 2;

        private List<Item> storageItems;

        public StorageAdapter() {
            setHasStableIds(true);
        }

        public void setStorageItems(List<Item> items) {
            this.storageItems = items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_usage_category_header, parent, false));
            }
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.storage_usage_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Item item = storageItems.get(position);
            if (item instanceof StorageItem) {
                holder.itemName.setVisibility(View.VISIBLE);
                holder.itemName.setText(item.name);
            } else {
                holder.categoryName.setVisibility(View.VISIBLE);
                holder.categoryName.setText(item.name);
            }
            if (item.text == null) {
                holder.usage.setVisibility(View.GONE);
                holder.loading.setVisibility(View.VISIBLE);
            } else {
                holder.usage.setVisibility(View.VISIBLE);
                holder.loading.setVisibility(View.GONE);
                holder.usage.setText(item.text);
            }
        }

        @Override
        public int getItemViewType(int position) {
            Item item = storageItems.get(position);
            if (item instanceof StorageItem) {
                return TYPE_ITEM;
            }
            return TYPE_HEADER;
        }

        @Override
        public long getItemId(int position) {
            return storageItems.get(position).getId();
        }

        @Override
        public int getItemCount() {
            return (storageItems == null) ? 0 : storageItems.size();
        }
    }

    public static class StorageViewModel extends AndroidViewModel {

        public MutableLiveData<String> homeUsageLiveData = new MutableLiveData<>();
        public MutableLiveData<String> groupsUsageLiveData = new MutableLiveData<>();
        public MutableLiveData<String> chatsUsageLiveData = new MutableLiveData<>();
        public MutableLiveData<String> archiveUsageLiveData = new MutableLiveData<>();
        public MutableLiveData<String> leakedMediaLiveData = new MutableLiveData<>();
        public ComputableLiveData<String> internalUsage;

        public MutableLiveData<Map<String, Long>> groupsUsageBreakdownLiveData = new MutableLiveData<>();
        public MutableLiveData<Map<String, Long>> chatsUsageBreakdownLiveData = new MutableLiveData<>();
        public MutableLiveData<Map<String, Long>> internalUsageBreakdownLiveData = new MutableLiveData<>();
        public MutableLiveData<Map<String, Long>> leakedMediaBreakdownLiveData = new MutableLiveData<>();

        public StorageViewModel(@NonNull Application application) {
            super(application);

            internalUsage = new ComputableLiveData<String>() {
                @SuppressLint("RestrictedApi")
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
                    homeUsageLiveData.postValue(FileUtils.getReadableFileSize(homeUsage));
                    groupsUsageLiveData.postValue(FileUtils.getReadableFileSize(groupUsage));
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
                    chatsUsageLiveData.postValue(FileUtils.getReadableFileSize(chatUsage));
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
                    archiveUsageLiveData.postValue(FileUtils.getReadableFileSize(archiveUsage));

                    File mediaDir = FileStore.getInstance().getMediaDir();

                    HashSet<String> allMediaFiles = new HashSet<>();
                    @SuppressLint("VisibleForTests") List<Media> medias = ContentDb.getInstance().getAllMedia();
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
                    leakedMediaLiveData.postValue(FileUtils.getReadableFileSize(leakedUsage));
                    leakedMediaBreakdownLiveData.postValue(leakedBreakdown);

                    ConcurrentMap<String, Long> internalBreakdown = new ConcurrentHashMap<>();
                    long internalUsage = 0;
                    File dataDir = application.getCacheDir().getParentFile();
                    File filesDir = application.getFilesDir();
                    File[] files = dataDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (!filesDir.equals(file)) {
                                long size = treeUsage(file);
                                internalUsage += size;
                                internalBreakdown.put(file.getName(), size);
                            }
                        }
                    }
                    internalUsageBreakdownLiveData.postValue(internalBreakdown);
                    return FileUtils.getReadableFileSize(internalUsage);
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

        private static long treeUsage(File file) {
            if (file == null || !file.exists()) {
                return 0;
            }
            if (file.isDirectory()) {
                long sum = 0;
                File[] files = file.listFiles();
                if (files != null) {
                    for (File subfile : files) {
                        sum += treeUsage(subfile);
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
}
