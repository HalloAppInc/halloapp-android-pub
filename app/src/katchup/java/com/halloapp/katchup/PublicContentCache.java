package com.halloapp.katchup;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.ConnectionObservers;
import com.halloapp.Me;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.server.MomentInfo;
import com.halloapp.proto.server.MomentNotification;
import com.halloapp.proto.server.PublicFeedItem;
import com.halloapp.proto.server.PublicFeedUpdate;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.feed.FeedContentParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PublicContentCache {
    private static PublicContentCache instance;
    private static Set<PublicContentCache.Observer> observers = new HashSet<>();
    private String cursor;
    private List<Post> cachedItems = new ArrayList<>();
    private long postIndex;
    private MutableLiveData<Boolean> hasUpdatedFeed = new MutableLiveData<>(false);
    private Connection.Observer connectionObserver;
    private ConnectionObservers connectionObservers;


    public static PublicContentCache getInstance() {
        if (instance == null) {
            synchronized (PublicContentCache.class) {
                if (instance == null) {
                    instance = new PublicContentCache(ConnectionObservers.getInstance());
                }
            }
        }
        return instance;
    }

    public PublicContentCache(ConnectionObservers connectionObservers) {
        this.connectionObservers = connectionObservers;
    }

    public void init() {
        this.connectionObserver = new Connection.Observer() {
            @Override
            public void onMomentNotificationReceived(MomentNotification momentNotification, @NonNull String ackId) {
                clear();
            }
    
            @Override
            public void onPublicFeedUpdate(@NonNull PublicFeedUpdate publicFeedUpdate, @NonNull String ackId) {
                Log.d("PublicContentCache recieved public feed update");
                updatePublicFeed(publicFeedUpdate);
                Connection.getInstance().sendAck(ackId);
            }

        };
        connectionObservers.addObserver(connectionObserver);
    }

    private void updatePublicFeed(@NonNull PublicFeedUpdate publicFeedUpdate) {
        cursor = publicFeedUpdate.getCursor();
        Log.d("Public feed last cursor updated to " + cursor);

        List<PublicFeedItem> feedItems = publicFeedUpdate.getItemsList();
        BgWorkers.getInstance().execute(() -> {
            cachedItems = processPublicFeedItems(feedItems, false);
            hasUpdatedFeed.postValue(true);
        });
    }

    public void markFeedUpdateComplete() {
        hasUpdatedFeed.postValue(false);
    }

    public String getCursor() {
        return this.cursor;
    }

    public List<Post> processCachedItems() {
        List<Post> currentCachedItems = new ArrayList<>(cachedItems);
        cachedItems.clear();
        return currentCachedItems;
    }

    public MutableLiveData<Boolean> getHasUpdatedFeed() {
        return hasUpdatedFeed;
    }

    private final Map<String, Post> postCache = new HashMap<>();
    private final Map<String, List<Comment>> commentCache = new HashMap<>();

    public void insertContent(@NonNull List<Post> posts, @NonNull Map<String, List<Comment>> commentMap) {
        for (Post post : posts) {
            postCache.put(post.id, post);
        }
        commentCache.putAll(commentMap);
    }

    public void addPost(@NonNull Post post) {
        postCache.put(post.id, post);
    }

    public void addComment(@NonNull String postId, @NonNull Comment comment) {
        List<Comment> existingComments = commentCache.get(postId);
        if (existingComments == null) {
            existingComments = new ArrayList<>();
        }
        existingComments = new ArrayList<>(existingComments);
        existingComments.add(comment);
        commentCache.put(postId, existingComments);
    }

    public void removeComment(@NonNull String postId, @NonNull Comment comment) {
        List<Comment> existingComments = commentCache.get(postId);
        if (existingComments == null) {
            return;
        }
        existingComments = new ArrayList<>(existingComments);
        existingComments.remove(comment);
        commentCache.put(postId, existingComments);
    }

    public Post getPost(@NonNull String postId) {
        return postCache.get(postId);
    }

    public List<Comment> getComments(@NonNull String postId) {
        return commentCache.get(postId);
    }

    public void clear() {
        postCache.clear();
        commentCache.clear();
    }

    public void removeReportedPost(@NonNull String postId) {
        Post post = postCache.remove(postId);
        if (post == null) {
            Log.w("Failed to find post " + postId);
            return;
        }
        notifyPostRemoved(post);
    }

    public void notifyPostRemoved(@NonNull Post post) {
        synchronized (observers) {
            for (PublicContentCache.Observer observer : observers) {
                observer.onPostRemoved(post);
            }
        }
    }

    public void addObserver(@NonNull PublicContentCache.Observer observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void removeObserver(@NonNull PublicContentCache.Observer observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    public interface Observer {
        void onPostRemoved(@NonNull Post post);
    }

    public static class DefaultObserver implements PublicContentCache.Observer {
        public void onPostRemoved(@NonNull Post post) {}
    }

    public List<Post> processPublicFeedItems(List<PublicFeedItem> feedItems, boolean wasRestarted) {
        if (wasRestarted) {
            postIndex = 0;
        }

        Map<UserId, String> namesMap = new HashMap<>();
        Map<UserId, String> usernamesMap = new HashMap<>();
        Map<UserId, String> avatarsMap = new HashMap<>();
        List<Post> posts = new ArrayList<>();
        Map<String, List<Comment>> commentMap = new HashMap<>();
        FeedContentParser feedContentParser = new FeedContentParser(Me.getInstance());
        String meUser = Me.getInstance().getUser();
        for (PublicFeedItem item : feedItems) {
            try {
                UserId publisherUserId = new UserId(Long.toString(item.getPost().getPublisherUid()));
                Container container = Container.parseFrom(item.getPost().getPayload());
                namesMap.put(publisherUserId, item.getPost().getPublisherName());
                usernamesMap.put(publisherUserId, item.getUserProfile().getUsername());
                avatarsMap.put(publisherUserId, item.getUserProfile().getAvatarId());
                KatchupPost post = feedContentParser.parseKatchupPost(
                        item.getPost().getId(),
                        publisherUserId,
                        item.getPost().getTimestamp() * 1000L,
                        container.getKMomentContainer(),
                        false
                );
                MomentInfo momentInfo = item.getPost().getMomentInfo();
                post.timeTaken = momentInfo.getTimeTaken();
                post.numSelfieTakes = (int) momentInfo.getNumSelfieTakes();
                post.numTakes = (int) momentInfo.getNumTakes();
                post.notificationId = momentInfo.getNotificationId();
                post.notificationTimestamp = momentInfo.getNotificationTimestamp() * 1000L;
                post.serverScore = item.getScore().getDscore() + ": " + item.getScore().getExplanation();
                post.rowId = postIndex++;
                post.contentType = momentInfo.getContentType();


                List<Comment> comments = new ArrayList<>();
                for (com.halloapp.proto.server.Comment protoComment : item.getCommentsList()) {
                    try {
                        Container commentContainer = Container.parseFrom(protoComment.getPayload());
                        String userIdStr = Long.toString(protoComment.getPublisherUid());
                        Comment comment = feedContentParser.parseComment(
                                protoComment.getId(),
                                protoComment.getParentCommentId(),
                                meUser.equals(userIdStr) ? UserId.ME : new UserId(userIdStr),
                                protoComment.getTimestamp() * 1000L,
                                commentContainer.getCommentContainer(),
                                false
                        );
                        comments.add(comment);
                    } catch (InvalidProtocolBufferException e) {
                        Log.e("Failed to parse container for public feed comment");
                    }
                }
                commentMap.put(post.id, comments);

                post.commentCount = comments.size();
                posts.add(post);
            } catch (InvalidProtocolBufferException e) {
                Log.e("Failed to parse container for public feed post");
            }
        }

        if (wasRestarted) {
            cachedItems.addAll(posts);
        }

        insertContent(posts, commentMap);

        ContactsDb contactsDb = ContactsDb.getInstance();
        contactsDb.updateUserNames(namesMap);
        contactsDb.updateUserUsernames(usernamesMap);
        contactsDb.updateUserAvatars(avatarsMap);

        return posts;
    }

}
