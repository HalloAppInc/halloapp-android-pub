package com.halloapp;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Group;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.groups.GroupLoader;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StorageUsageActivity extends HalloActivity {

    private StorageViewModel viewModel;

    private final HashMap<String, Long> ids = new HashMap<>();
    private long id = 0;

    private String homeUsage;
    private String groupUsage;
    private String chatsUsage;
    private String archiveUsage;
    private String internalUsage;

    private List<Item> groupsBreakdown;
    private List<Item> chatsBreakdown;

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private GroupLoader groupLoader;
    private ContactLoader contactLoader;

    private Item homeFeedItem;
    private Item chatsFeedItem;
    private Item groupsFeedItem;
    private Item archiveFeedItem;
    private Item internalFeedItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_storage);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(StorageViewModel.class);

        groupLoader = new GroupLoader();
        contactLoader = new ContactLoader(userId -> {
            startActivity(ViewProfileActivity.viewProfile(this, userId));
            return null;
        });

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        groupLoader.destroy();
        contactLoader.destroy();
    }

    private List<Item> convertMapToList(Map<ChatId, Long> map) {
        List<ChatId> keys = new ArrayList<>(map.keySet());
        Comparator<ChatId> orderBySizeComparator = (o1, o2) ->
                Long.compare(Preconditions.checkNotNull(map.get(o2)), Preconditions.checkNotNull(map.get(o1)));
        Collections.sort(keys, orderBySizeComparator);
        List<Item> items = new ArrayList<>(keys.size());
        for (ChatId key : keys) {
            items.add(new StorageItem(key, readableSize(Preconditions.checkNotNull(map.get(key)))));
        }
        return items;
    }

    private List<Item> constructList() {
        List<Item> list = new ArrayList<>();
        if (homeFeedItem == null) {
            homeFeedItem = new CategoryItem(getString(R.string.storage_usage_home));
        }
        homeFeedItem.setText(homeUsage);
        if (groupsFeedItem == null) {
            groupsFeedItem = new CategoryItem(getString(R.string.storage_usage_groups));
        }
        groupsFeedItem.setText(groupUsage);
        if (chatsFeedItem == null) {
            chatsFeedItem = new CategoryItem(getString(R.string.storage_usage_chats));
        }
        chatsFeedItem.setText(chatsUsage);
        if (archiveFeedItem == null) {
            archiveFeedItem = new CategoryItem(getString(R.string.storage_usage_archive));
        }
        archiveFeedItem.setText(archiveUsage);
        if (internalFeedItem == null) {
            internalFeedItem = new CategoryItem(getString(R.string.storage_usage_internal));
        }
        internalFeedItem.setText(internalUsage);

        list.add(homeFeedItem);
        list.add(groupsFeedItem);
        if (groupsBreakdown != null) {
            list.addAll(groupsBreakdown);
        }
        list.add(chatsFeedItem);
        if (chatsBreakdown != null) {
            list.addAll(chatsBreakdown);
        }
        list.add(archiveFeedItem);
        list.add(internalFeedItem);
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

        public void setText(String text) {
            this.text = text;
        }
    }

    private class StorageItem extends Item {

        ChatId chatId;

        public StorageItem(ChatId id, String text) {
            super(id.rawId(), text);

            this.chatId = id;
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
        public CategoryItem(String name) {
            super(name, null);
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
        ImageView avatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            usage = itemView.findViewById(R.id.usage_amount);
            itemName = itemView.findViewById(R.id.item_name);
            categoryName = itemView.findViewById(R.id.category_name);
            loading = itemView.findViewById(R.id.loading);
            avatar = itemView.findViewById(R.id.avatar);
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
                ChatId chatId = ((StorageItem) item).chatId;
                groupLoader.cancel(holder.itemName);
                contactLoader.cancel(holder.itemName);
                if (chatId instanceof GroupId) {
                    groupLoader.load(holder.itemName, new ViewDataLoader.Displayer<View, Group>() {
                        @Override
                        public void showResult(@NonNull View view, @Nullable Group result) {
                            if (result != null) {
                                holder.itemName.setText(result.name);
                            }
                        }

                        @Override
                        public void showLoading(@NonNull View view) {
                            holder.itemName.setText("");
                        }
                    }, (GroupId) chatId);
                } else {
                    contactLoader.load(holder.itemName, (UserId) chatId, false);
                }
                avatarLoader.load(holder.avatar, chatId);
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
        public ComputableLiveData<String> internalUsage;

        public MutableLiveData<Map<ChatId, Long>> groupsUsageBreakdownLiveData = new MutableLiveData<>();
        public MutableLiveData<Map<ChatId, Long>> chatsUsageBreakdownLiveData = new MutableLiveData<>();

        public StorageViewModel(@NonNull Application application) {
            super(application);

            internalUsage = new ComputableLiveData<String>() {
                @Override
                protected String compute() {
                    long homeUsage = 0;
                    long groupUsage = 0;
                    ConcurrentMap<ChatId, Long> groupBreakdown = new ConcurrentHashMap<>();
                    List<Post> allPosts = ContentDb.getInstance().getAllPosts();
                    for (Post post : allPosts) {
                        GroupId parentGroup = post.getParentGroup();
                        for (Media media : post.media) {
                            if (media.file != null) {
                                if (parentGroup == null) {
                                    homeUsage += media.file.length();
                                } else {
                                    groupUsage += media.file.length();

                                    GroupId key = parentGroup;
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

                                        GroupId key = parentGroup;
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
                    ConcurrentMap<ChatId, Long> chatBreakdown = new ConcurrentHashMap<>();
                    List<Message> allMessages = ContentDb.getInstance().getAllMessages();
                    for (Message message : allMessages) {
                        for (Media media : message.media) {
                            if (media.file != null) {
                                chatUsage += media.file.length();

                                ChatId key = message.chatId;
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

                    long internalUsage = 0;
                    File dataDir = application.getCacheDir().getParentFile();
                    File filesDir = application.getFilesDir();
                    File[] files = dataDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (!filesDir.equals(file)) {
                                long size = treeUsage(file);
                                internalUsage += size;
                            } else {
                                // Already counted in other categories, but still print to logs
                                treeUsage(file);
                            }
                        }
                    }
                    return readableSize(internalUsage);
                }
            };
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
                String splitAt = "com.halloapp/";
                String path = file.getAbsolutePath();
                int index = path.indexOf(splitAt);
                String localPath = index < 0 ? path : path.substring(index + splitAt.length());
                Log.d("StorageUsageActivity directory " + localPath + " has size " + sum);
                return sum;
            }
            if (file.isFile()) {
                return file.length();
            }
            return 0;
        }
    }

    private static String readableSize(long size) {
        long factor = 1000;
        if (size < factor) {
            return size + " B";
        } else if (size < factor * factor) {
            return String.format(Locale.US, "%.2f", ((float) size) / factor) + " KB";
        } else if (size < Math.pow(factor, 3)) {
            return String.format(Locale.US, "%.2f", ((float) size) / (factor * factor)) + " MB";
        } else {
            return String.format(Locale.US, "%.2f", ((float) size) / Math.pow(factor, 3)) + " GB";
        }
    }
}
