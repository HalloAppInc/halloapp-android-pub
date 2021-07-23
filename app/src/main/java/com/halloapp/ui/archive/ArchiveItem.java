package com.halloapp.ui.archive;

import android.util.Pair;

import androidx.annotation.IntDef;

import com.halloapp.content.Post;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ArchiveItem {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_HEADER, TYPE_ITEM})
    public @interface Type {}
    public final static int TYPE_HEADER = 1;
    public final static int TYPE_ITEM = 2;

    public @Type int type;
    public int position;

    public Post post;
    public String title;
    public Pair<Long, Long> key; //Using a pair to prevent collisions between headers and items

    public ArchiveItem(Post post) {
        this.type = TYPE_ITEM;
        this.post = post;
        this.key = new Pair<>(post.timestamp, 0L);
    }
    public ArchiveItem(String title, Long key) {
        this.type = TYPE_HEADER;
        this.title = title;
        this.key = new Pair<>(key, 1L);;
    }
}
