package com.halloapp.ui.groups;

import android.view.View;

import androidx.annotation.NonNull;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.GroupId;
import com.halloapp.util.ViewDataLoader;

import java.util.List;
import java.util.concurrent.Callable;

public class UnseenGroupPostsLoader extends ViewDataLoader<View, List<Post>, GroupId> {

    private final ContentDb contentDb;

    public UnseenGroupPostsLoader() {
        contentDb = ContentDb.getInstance();
    }

    public void load(@NonNull View view,@NonNull Displayer<View, List<Post>> displayer, @NonNull GroupId groupId) {
        Callable<List<Post>> loader = () -> contentDb.getUnseenGroupPosts(groupId);

        load(view, loader, displayer, groupId, null);
    }


}
