package com.halloapp.content;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.id.UserId;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.privacy.PrivacyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PostsManager {

    private static PostsManager instance;

    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final Connection connection;
    private final ContactsDb contactsDb;
    private final Preferences preferences;

    public static PostsManager getInstance() {
        if (instance == null) {
            synchronized (PostsManager.class) {
                if (instance == null) {
                    instance = new PostsManager(BgWorkers.getInstance(), ContentDb.getInstance(), Connection.getInstance(), ContactsDb.getInstance(), Preferences.getInstance());
                }
            }
        }
        return instance;
    }

    private final ContactsDb.Observer observer = new ContactsDb.BaseObserver() {
        @Override
        public void onNewFriends(@NonNull Collection<UserId> newFriends) {
            bgWorkers.execute(() -> {
                synchronized (PostsManager.this) {
                    shareOperations++;
                    if (shareOperations == 1) {
                        preferences.setRequireSharePosts(true);
                    }
                }
                sharePosts(newFriends, false);
            });
        }
    };

    private int shareOperations;
    private boolean shareFailure;

    private PostsManager(@NonNull BgWorkers bgWorkers, @NonNull ContentDb contentDb, @NonNull Connection connection, @NonNull ContactsDb contactsDb, @NonNull Preferences preferences) {
        this.bgWorkers = bgWorkers;
        this.contentDb = contentDb;
        this.connection = connection;
        this.contactsDb = contactsDb;
        this.preferences = preferences;

        shareOperations = 0;
        shareFailure = false;

        contactsDb.addObserver(observer);
    }

    @WorkerThread
    public void ensurePostsShared() {
        synchronized (PostsManager.this) {
            if (shareOperations > 0 || !preferences.getRequireSharePosts()) {
                return;
            }
            shareOperations++;
        }
        List<Contact> friends = contactsDb.getFriends();
        HashSet<UserId> shareToIds = new HashSet<>();
        for (Contact contact : friends) {
            shareToIds.add(contact.userId);
        }
        sharePosts(shareToIds, true);
    }

    @WorkerThread
    private void sharePosts(@NonNull Collection<UserId> friends, boolean dontResend) {
        Collection<Post> shareablePosts = contentDb.getShareablePosts();
        Map<UserId, Collection<Post>> shareMap = new HashMap<>();
        for (Post post : shareablePosts) {
            HashSet<UserId> friendSet = new HashSet<>(friends);
            if (post.getAudienceType() == null) {
                continue;
            }
            switch (post.getAudienceType()) {
                case PrivacyList.Type.ALL:
                    break;
                case PrivacyList.Type.EXCEPT:
                    List<UserId> exceptList = post.getExcludeList();
                    if (exceptList != null) {
                        for (UserId user : exceptList) {
                            friendSet.remove(user);
                        }
                    }
                    break;
                default:
                    continue;
            }
            List<UserId> currentAudience = post.getAudienceList();
            if (currentAudience == null) {
                Log.e("PostsManager/sharePosts post doesnt have an audience");
                continue;
            }
            if (dontResend) {
                for (UserId user : currentAudience) {
                    friendSet.remove(user);
                }
            }
            for (UserId user : friendSet) {
                Collection<Post> mapColl = shareMap.get(user);
                if (mapColl == null) {
                    mapColl = new ArrayList<>();
                    shareMap.put(user, mapColl);
                }
                mapColl.add(post);
            }
        }

        boolean shared = true;
        if (!shareMap.isEmpty()) {
            try {
                shared = connection.sharePosts(shareMap).get();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("PostsManager/sharePosts failed to share posts", e);
            }
            if (shared) {
                contentDb.updatePostAudience(shareMap);
            }
        }
        synchronized (PostsManager.this) {
            shareOperations--;
            if (!shared) {
                shareFailure = true;
            }
            if (shareOperations == 0) {
                preferences.setRequireSharePosts(shareFailure);
                shareFailure = false;
            }
        }
    }
}
