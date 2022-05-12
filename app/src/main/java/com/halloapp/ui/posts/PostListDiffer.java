package com.halloapp.ui.posts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;

import com.halloapp.content.Post;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ListDiffer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class PostListDiffer implements ListUpdateCallback, ListDiffer<Post> {

    private AsyncPagedListDiffer<Post> differ;
    private ListUpdateCallback callback;

    private List<Post> snapshot;

    private static final DiffUtil.ItemCallback<Post> DIFF_CALLBACK = new DiffUtil.ItemCallback<Post>() {

        @Override
        public boolean areItemsTheSame(Post oldItem, Post newItem) {
            // The ID property identifies when items are the same.
            return oldItem.rowId == newItem.rowId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post oldItem, @NonNull Post newItem) {
            return oldItem.equals(newItem);
        }
    };

    public PostListDiffer(@NonNull ListUpdateCallback callback) {
        this.callback = callback;
        differ = new AsyncPagedListDiffer<>(this, new AsyncDifferConfig.Builder<>(DIFF_CALLBACK).build());
    }

    public static class PostCollection extends Post {

        public final int size;
        public Section parent;

        public PostCollection(@NonNull Post post, int size) {
            super(post.rowId, post.senderUserId, post.id, post.timestamp, post.transferred, post.seen, post.type, post.text);
            this.size = size;
            this.usage = post.usage;
        }
    }

    public static class ExpandedPost extends Post {

        public Post wrappedPost;
        public Section parent;

        public ExpandedPost(@NonNull Post post) {
            super(post.rowId, post.senderUserId, post.id, post.timestamp, post.transferred, post.seen, post.type, post.text);
            usage = post.usage;
            wrappedPost = post;
        }
    }

    private int itemCount;

    @Override
    public int getItemCount() {
        return itemCount;
    }

    public Post getItem(int index) {
        int offset = 0;
        for (Section g : collapsedSections) {
            if (g.adapterIndex == index) {
                if (!g.expanded) {
                    return g.collectionPost;
                } else {
                    return g.expandedPost;
                }
            } else if (g.adapterIndex < index) {
                int size = g.size();
                if (!g.expanded) {
                    offset += size - 1;
                } else if (index - g.adapterIndex < size) {
                    ExpandedPost p = new ExpandedPost(differ.getItem(offset + index));
                    p.parent = g;
                    return p;
                }
            }
        }
        return differ.getItem(offset + index);
    }

    @Override
    public void submitList(@Nullable PagedList<Post> pagedList, @Nullable Runnable completion) {
        PagedList<Post> posts = differ.getCurrentList();
        if (posts != null) {
            snapshot = new LinkedList<>(posts.snapshot());
        } else {
            snapshot = new LinkedList<>();
        }
        differ.submitList(pagedList, () -> {
            recomputeAdapterPositions();
            if (completion != null) {
                completion.run();
            }
        });
    }

    public void collapse(int adapterIndex) {
        for (Section s : collapsedSections) {
            if (s.adapterIndex == adapterIndex) {
                if (s.expanded) {
                    s.expanded = false;
                    recomputeAdapterPositions();
                    callback.onRemoved(s.adapterIndex, s.size());
                    callback.onInserted(s.adapterIndex, 1);
                    break;
                }
            }
        }
    }

    public void expand(int adapterIndex) {
        for (Section s : collapsedSections) {
            if (s.adapterIndex == adapterIndex) {
                if (!s.expanded) {
                    s.expanded = true;
                    recomputeAdapterPositions();
                    callback.onInserted(s.adapterIndex + 1, s.size() - 1);
                    callback.onChanged(s.adapterIndex, 1, null);
                    break;
                }
            }
        }
    }

    private boolean canCollapse(@Nullable Post post, @Nullable Post prev) {
        if (post == null || prev == null) {
            return false;
        }
        if (prev.type == post.type) {
            if (prev.type == Post.TYPE_RETRACTED) {
                return true;
            } else if (prev.type == Post.TYPE_SYSTEM && prev.usage == post.usage) {
                switch (post.usage) {
                    case Post.USAGE_ADD_MEMBERS:
                    case Post.USAGE_REMOVE_MEMBER:
                    case Post.USAGE_MEMBER_JOINED:
                    case Post.USAGE_MEMBER_LEFT:
                        return true;
                }
            }
        }
        return false;
    }

    private int translateDifferIndexToAdapter(int differIndex) {
        int offset = 0;
        for (Section s : collapsedSections) {
            if (differIndex >= s.startIndex) {
                if (differIndex < s.endIndex) {
                    return s.adapterIndex;
                } else {
                    offset += s.size() - 1;
                }
            } else {
                break;
            }
        }
        return differIndex - offset;
    }

    public static class Section {
        boolean expanded;
        int startIndex; //inclusive
        int endIndex; //non-inclusive
        int adapterIndex;

        PostCollection collectionPost;
        ExpandedPost expandedPost;

        public boolean contains(int differIndex) {
            return differIndex >= startIndex && differIndex < endIndex;
        }

        public int getAdapterIndex() {
            return adapterIndex;
        }

        public int size() {
            return endIndex - startIndex;
        }
    }

    private List<Section> collapsedSections = new LinkedList<>();

    private List<Section> findSections(int startIndex, int endIndex) {
        List<Section> groups = new ArrayList<>();
        Post prev = null;
        Section section = null;
        for (int i = startIndex; i < endIndex; i++) {
            Post post = differ.getItem(i);
            if (post == null) {
                if (section != null) {
                    groups.add(section);
                    section = null;
                }
            } else {
                if (canCollapse(post, prev)) {
                    if (section == null) {
                        section = new Section();
                        section.startIndex = i - 1;
                    }
                } else if (section != null) {
                    section.endIndex = i;
                    groups.add(section);
                    section = null;
                }
            }
            prev = post;
        }
        if (section != null) {
            section.endIndex = endIndex;
            groups.add(section);
        }
        return groups;
    }

    private void recomputeAdapterPositionsFromSnapshot() {
        int offset = 0;
        for (Section s : collapsedSections) {
            s.adapterIndex = s.startIndex - offset;
            if (!s.expanded) {
                offset += s.size() - 1;
            }
        }
        itemCount = differ.getItemCount() - offset;
    }

    private void recomputeAdapterPositions() {
        int offset = 0;
        for (Section s : collapsedSections) {
            s.adapterIndex = s.startIndex - offset;
            if (!s.expanded) {
                offset += s.size() - 1;
            }
            Post firstPost = differ.getItem(s.startIndex);
            s.collectionPost = new PostCollection(firstPost, s.size());
            s.collectionPost.parent = s;
            s.expandedPost = new ExpandedPost(firstPost);
            s.expandedPost.parent = s;
        }
        itemCount = differ.getItemCount() - offset;
    }

    @Override
    public void onInserted(int position, int count) {
        Log.i("PostListDiffer/onInserted position=" + position + " count=" + count);
        for (int i = 0; i < count; i++) {
            snapshot.add(position, null);
        }
        int insertStart = position;
        int insertEnd = position + count;

        HashSet<Integer> changedAdapterPositions = new HashSet<>();
        // Update all section indices after insertion point
        ListIterator<Section> sectionListIterator = collapsedSections.listIterator();
        while (sectionListIterator.hasNext()) {
            Section s = sectionListIterator.next();
            if (s.startIndex >= position) {
                s.startIndex += count;
                s.endIndex += count;
            } else if (s.endIndex > position) {
                // Split the section, consider tail end part of inserted elements
                changedAdapterPositions.add(s.adapterIndex);
                if (position - s.startIndex < 2) {
                    // Start of split section is too small, remove it
                    sectionListIterator.remove();
                    // Collapsed section now turns into a single element, should count as a change which is already
                    // handled by changedAdapterPositions
                } else {
                    s.endIndex = position;
                }
                insertEnd = Math.max(s.endIndex + count, insertEnd);
            }
        }

        // At this point all indices are updated, but we need to collapse the inserted elements
        // Search for new sections in the range +/- 1 to overlap existing sections
        List<Section> insertedSections = findSections(
                Math.max(0, insertStart - 1),
                Math.min(differ.getItemCount(), insertEnd + 1));

        sectionListIterator = collapsedSections.listIterator();
        // Move iterator to insertion point
        Section head = null;
        Section tail = null;
        while (sectionListIterator.hasNext()) {
            Section s = sectionListIterator.next();
            if (s.startIndex > position) {
                tail = s;
                sectionListIterator.previous();
                break;
            }
            head = s;
        }

        Section first = insertedSections.isEmpty() ? null : insertedSections.remove(0);
        Section last = insertedSections.isEmpty() ? null : insertedSections.remove(insertedSections.size() - 1);

        // Number of new elements assuming none collapse
        int adapterItemsAdded = insertEnd - insertStart;
        if (first != null) {
            if (first.startIndex < insertStart) {
                adapterItemsAdded -= first.endIndex - insertStart;
                insertStart = first.endIndex;
                if (head != null && first.startIndex <= head.endIndex) {
                    head.endIndex = first.endIndex;
                } else {
                    sectionListIterator.add(first);
                    head = first;
                }
            } else {
                head = null;
                sectionListIterator.add(first);
                adapterItemsAdded -= first.size() - 1;
            }
        } else {
            head = null;
        }
        for (Section insert : insertedSections) {
            sectionListIterator.add(insert);
            adapterItemsAdded -= insert.size() - 1;
        }
        if (last != null) {
            if (last.endIndex > insertEnd) {
                adapterItemsAdded -= insertEnd - last.startIndex;
                if (tail != null && tail.startIndex <= last.endIndex) {
                    tail.startIndex = last.startIndex;
                } else {
                    sectionListIterator.add(last);
                    tail = last;
                }
            } else {
                adapterItemsAdded -= last.size() - 1;
                sectionListIterator.add(last);
            }
        } else {
            tail = null;
        }
        recomputeAdapterPositionsFromSnapshot();
        if (tail != null) {
            changedAdapterPositions.add(tail.adapterIndex);
        }
        if (head != null) {
            changedAdapterPositions.add(head.adapterIndex);
        }
        if (adapterItemsAdded > 0) {
            callback.onInserted(translateDifferIndexToAdapter(insertStart), adapterItemsAdded);
        }
        for (Integer i : changedAdapterPositions) {
            callback.onChanged(i, 1, null);
        }
    }


    @Override
    public void onRemoved(int position, int count) {
        Log.i("PostListDiffer/onRemoved pos=" + position + " count=" + count);
        for (int i = 0; i < count; i++) {
            snapshot.remove(position);
        }
        ListIterator<Section> sectionListIterator = collapsedSections.listIterator();
        HashSet<Integer> changedAdapterPositions = new HashSet<>();
        int adapterItemsRemoved = count;
        int removalEndIndex = position + count;
        Section head = null;
        Section tail = null;
        while (sectionListIterator.hasNext()) {
            Section s = sectionListIterator.next();
            Log.i("PostListDiffer/onRemoved updating section from=" + s.startIndex + " to=" + s.endIndex);
            if (s.startIndex >= removalEndIndex) {
                s.startIndex -= count;
                s.endIndex -= count;
            } else { // s.start index < removalEndIndex
                if (s.startIndex >= position) {
                    if (removalEndIndex >= s.endIndex) {
                        // section is fully inside removal region
                        sectionListIterator.remove();
                        adapterItemsRemoved -= s.size() - 1;
                    } else {
                        // section is only partially inside removal region
                        s.startIndex = position;
                        s.endIndex -= count;
                    }
                } else if (s.endIndex >= removalEndIndex) {
                    s.endIndex -= count;
                    // removal is fully inside section
                } else if (s.endIndex > position) {
                    s.endIndex = position;
                }
            }
        }
        // All indices of sections are updated, may be some singular item sections that need to be cleaned up
        if (position < differ.getItemCount() && position > 0) {
            if (canCollapse(differ.getItem(position - 1), differ.getItem(position))) {
                sectionListIterator = collapsedSections.listIterator();
                Section newSection = null;
                while (sectionListIterator.hasNext()) {
                    Section s = sectionListIterator.next();
                    if (s.startIndex >= position) {
                        tail = s;
                        sectionListIterator.previous();
                        break;
                    }
                    head = s;
                }
                if (tail != null) {
                    if (head != null && head.endIndex == tail.startIndex) {
                        head.endIndex = tail.endIndex;
                        adapterItemsRemoved++;
                        sectionListIterator.remove();
                        newSection = head;
                    } else {
                        tail.startIndex--;
                        newSection = tail;
                        adapterItemsRemoved++;
                    }
                } else {
                    newSection = new Section();
                    newSection.startIndex = position - 1;
                    newSection.endIndex = position + 1;
                    sectionListIterator.add(newSection);
                    adapterItemsRemoved++;
                }
                recomputeAdapterPositionsFromSnapshot();
                changedAdapterPositions.add(newSection.adapterIndex);
            } else {
                recomputeAdapterPositionsFromSnapshot();
            }
        } else {
            recomputeAdapterPositionsFromSnapshot();
        }
        sectionListIterator = collapsedSections.listIterator();
        while (sectionListIterator.hasNext()) {
            Section s = sectionListIterator.next();
            if (s.size() < 2) {
                sectionListIterator.remove();
                changedAdapterPositions.add(s.adapterIndex);
            }
        }
        if (adapterItemsRemoved > 0) {
            callback.onRemoved(translateDifferIndexToAdapter(position), adapterItemsRemoved);
        }
        for (Integer index : changedAdapterPositions) {
            callback.onChanged(index, 1, null);
        }
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        Log.i("PostListDiffer/onMoved from=" + fromPosition + " to=" + toPosition);
    }

    @Override
    public void onChanged(int position, int count, @Nullable Object payload) {
        Log.i("PostListDiffer/onChanged position=" + position + " count=" + count);
        int adapterIndex = translateDifferIndexToAdapter(position);
        if (adapterIndex + count > itemCount) {
            count = itemCount - (adapterIndex + count);
        }
        callback.onChanged(adapterIndex, count, payload);
    }

}
