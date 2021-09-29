package com.halloapp.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.Me;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.FlatCommentsDataSource;
import com.halloapp.id.UserId;
import com.halloapp.util.ComputableLiveData;

import java.util.Objects;

class FlatCommentsViewModel extends CommentsViewModel {

    private final Me me = Me.getInstance();
    private final ContentDb contentDb = ContentDb.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();

    private FlatCommentsDataSource.Factory dataSourceFactory;

    private final String postId;

    private final ComputableLiveData<Reply> replyComputableLiveData;
    final ComputableLiveData<Pair<Long, Integer>> unseenCommentCount;

    private String replyCommentId;

    private ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {

        @Override
        public void onContactsChanged() {
            invalidateDataSource();
        }

        @Override
        public void onContactsReset() {
            invalidateDataSource();
        }

        private void invalidateDataSource() {
            mainHandler.post(FlatCommentsViewModel.this::invalidateLatestDataSource);
        }
    };

    FlatCommentsViewModel(@NonNull Application application, @NonNull String postId) {
        super(application, postId);

        this.postId = postId;

        contactsDb.addObserver(contactsObserver);

        replyComputableLiveData = new ComputableLiveData<Reply>() {
            @Override
            protected Reply compute() {
                if (replyCommentId != null) {
                    Comment comment = contentDb.getComment(replyCommentId);
                    if (comment != null) {
                        String name = getName(comment.senderUserId);
                        if (name != null) {
                            return new Reply(comment, name);
                        }
                    }
                }

                return null;
            }

            private String getName(UserId sender) {
                if (sender == null) {
                    return null;
                }
                if (sender.isMe()) {
                    return me.getName();
                }
                return contactsDb.getContact(sender).getDisplayName();
            }
        };
        unseenCommentCount = new ComputableLiveData<Pair<Long, Integer>>() {
            @Override
            protected Pair<Long, Integer> compute() {
                long rowId = contentDb.getFirstUnseenCommentRowId(postId);
                int commentCount = contentDb.getUnseenCommentCount(postId);
                contentDb.setCommentsSeen(postId);
                return new Pair<>(rowId, commentCount);
            }
        };
    }

    public LiveData<Reply> getReply() {
        return replyComputableLiveData.getLiveData();
    }

    public void loadReply(@Nullable String commentId) {
        if (Objects.equals(commentId, replyCommentId)) {
            return;
        }
        replyCommentId = commentId;
        replyComputableLiveData.invalidate();
    }

    @Override
    protected LiveData<PagedList<Comment>> createCommentsList() {
        dataSourceFactory = new FlatCommentsDataSource.Factory(contentDb, contactsDb, postId);

        return new LivePagedListBuilder<>(dataSourceFactory, new PagedList.Config.Builder().setPageSize(50).setEnablePlaceholders(false).build()).build();
    }

    public static class Reply {
        final Comment comment;
        final String name;

        public Reply(@NonNull Comment comment, @NonNull String name) {
            this.comment = comment;
            this.name = name;
        }
    }

    @Override
    protected void invalidateLatestDataSource() {
        dataSourceFactory.invalidateLatestDataSource();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        contactsDb.removeObserver(contactsObserver);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;
        private final String postId;

        Factory(@NonNull Application application, @NonNull String postId) {
            this.application = application;
            this.postId = postId;
        }

        @Override
        public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(FlatCommentsViewModel.class)) {
                //noinspection unchecked
                return (T) new FlatCommentsViewModel(application, postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

