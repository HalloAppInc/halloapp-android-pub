package com.halloapp.ui;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Me;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.groups.MemberInfo;
import com.halloapp.id.UserId;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.content.SeenByInfo;
import com.halloapp.util.ComputableLiveData;

import java.util.ArrayList;
import java.util.List;

public class PostSeenByViewModel extends AndroidViewModel {

    final ComputableLiveData<Post> post;
    final ComputableLiveData<List<SeenByContact>> seenByList;
    final ComputableLiveData<List<Contact>> friendsList;
    final MutableLiveData<Boolean> postDeleted = new MutableLiveData<>();

    private final Me me;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {

        @Override
        public void onPostRetracted(@NonNull Post retractedPost) {
            final Post post = PostSeenByViewModel.this.post.getLiveData().getValue();
            if (post != null && post.senderUserId.equals(retractedPost.senderUserId) && post.id.equals(retractedPost.id)) {
                postDeleted.postValue(true);
            }
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            invalidateSeenBy();
        }

        @Override
        public void onFeedCleanup() {
            invalidateSeenBy();
        }

        private void invalidateSeenBy() {
            mainHandler.post(seenByList::invalidate);
        }
    };

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {
        @Override
        public void onContactsChanged() {
            mainHandler.post(seenByList::invalidate);
            mainHandler.post(friendsList::invalidate);
        }
    };

    private PostSeenByViewModel(@NonNull Application application, @NonNull String postId) {
        super(application);

        me = Me.getInstance();
        contentDb = ContentDb.getInstance();
        contentDb.addObserver(contentObserver);

        contactsDb = ContactsDb.getInstance();
        contactsDb.addObserver(contactsObserver);

        seenByList = new ComputableLiveData<List<SeenByContact>>() {

            @Override
            protected List<SeenByContact> compute() {
                final List<SeenByInfo> seenByInfos = contentDb.getPostSeenByInfos(postId);
                final List<SeenByContact> seenByContacts = new ArrayList<>(seenByInfos.size());
                for (SeenByInfo seenByInfo : seenByInfos) {
                    seenByContacts.add(new SeenByContact(contactsDb.getContact(seenByInfo.userId), seenByInfo.timestamp));
                }
                return seenByContacts;
            }
        };

        friendsList = new ComputableLiveData<List<Contact>>() {
            @Override
            protected List<Contact> compute() {
                Post post = contentDb.getPost(postId);
                if (post != null) {
                    if (post.getParentGroup() == null) {
                        List<UserId> audienceList = post.getAudienceList();
                        if (audienceList != null) {
                            List<Contact> sharedTo = new ArrayList<>();
                            for (UserId userId : audienceList) {
                                sharedTo.add(contactsDb.getContact(userId));
                            }
                            return Contact.sort(sharedTo);
                        }
                    } else {
                        List<MemberInfo> members = contentDb.getGroupMembers(post.getParentGroup());
                        List<Contact> audience = new ArrayList<>(members.size());
                        for (MemberInfo info : members) {
                            if (info.userId.isMe()) {
                                continue;
                            }
                            audience.add(contactsDb.getContact(info.userId));
                        }
                        return Contact.sort(audience);
                    }
                }
                return Contact.sort(contactsDb.getFriends());
            }
        };

        post = new ComputableLiveData<Post>() {
            @Override
            protected Post compute() {
                return contentDb.getPost(postId);
            }
        };
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
        contactsDb.removeObserver(contactsObserver);
    }

    static class SeenByContact {
        public final Contact contact;
        public final long timestamp;

        SeenByContact(Contact contact, long timestamp) {
            this.contact = contact;
            this.timestamp = timestamp;
        }
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
            if (modelClass.isAssignableFrom(PostSeenByViewModel.class)) {
                //noinspection unchecked
                return (T) new PostSeenByViewModel(application, postId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
