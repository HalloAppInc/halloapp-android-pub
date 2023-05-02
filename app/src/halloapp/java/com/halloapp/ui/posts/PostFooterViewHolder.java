package com.halloapp.ui.posts;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.halloapp.R;
import com.halloapp.content.Post;
import com.halloapp.groups.MediaProgressLoader;

public abstract class PostFooterViewHolder {
    protected static final float DISABLED_OPACITY = 0.35f;

    protected Post post;
    protected MediaProgressLoader.MediaProgressCallback mediaProgressCallback;

    protected PostViewHolder.PostViewHolderParent parent;
    protected final Handler mainHandler = new Handler(Looper.getMainLooper());

    protected final View footerSpacing;

    protected View itemView;

    public PostFooterViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        this.parent = parent;
        this.itemView = itemView;
        footerSpacing = itemView.findViewById(R.id.footer_spacing);
    }

    public abstract void setCanInteract(boolean canInteract);

    public abstract void setPercentTransferred(int percent);

    @CallSuper
    public void bindTo(@NonNull Post post) {
        if (mediaProgressCallback != null) {
            parent.getMediaProgressLoader().removeCallback(mediaProgressCallback);
        }
        mediaProgressCallback = new MediaProgressLoader.MediaProgressCallback() {
            @Override
            public String getContentItemId() {
                return post.id;
            }

            @Override
            public void onPostProgress(int percent) {
                setPercentTransferred(percent);
            }
        };
        parent.getMediaProgressLoader().registerCallback(mediaProgressCallback);
        this.post = post;
    }

    public abstract void reloadReactions();
}
