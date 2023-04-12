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
import com.halloapp.proto.server.FeedItem;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PublicContentCache {
    private static PublicContentCache instance;
    private final Set<PublicContentCache.Observer> observers = new HashSet<>();
    private String cursor;
    private List<Post> cachedItems = new ArrayList<>();
    private long postIndex;
    private MutableLiveData<Boolean> hasUpdatedFeed = new MutableLiveData<>(false);
    private Connection.Observer connectionObserver;

    private final ConnectionObservers connectionObservers;
    private final Me me;

    private final Executor executor = Executors.newSingleThreadExecutor();

    public static PublicContentCache getInstance() {
        if (instance == null) {
            synchronized (PublicContentCache.class) {
                if (instance == null) {
                    instance = new PublicContentCache(ConnectionObservers.getInstance(), Me.getInstance());
                }
            }
        }
        return instance;
    }

    public PublicContentCache(ConnectionObservers connectionObservers, Me me) {
        this.connectionObservers = connectionObservers;
        this.me = me;
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

            @Override
            public void onPublicCommentRevoked(@NonNull String id, @NonNull String postId) {
                List<Comment> existingComments = commentCache.get(postId);
                Post post = getPost(postId);
                if (existingComments == null || post == null) {
                    Log.w("Failed to find post " + postId + " with comment " + id);
                    return;
                }
                existingComments = new ArrayList<>(existingComments);
                for (Comment comment : existingComments) {
                    if (comment.id.equals(id)) {
                        removeComment(postId, comment);
                        notifyCommentRetracted(comment);
                    }
                }
            }

            @Override
            public void onPublicPostRevoked(@NonNull String postId) {
                executor.execute(() -> {
                    Post post = postCache.remove(postId);
                    if (post == null) {
                        Log.w("Failed to find post " + postId);
                        return;
                    }
                    notifyPostRemoved(post);
                });
            }

            @Override
            public void onIncomingPublicFeedItemsReceived(@NonNull List<Comment> comments) {
                for (Comment comment : new ArrayList<>(comments)) {
                    Post post = getPost(comment.postId);
                    if (post == null) {
                        Log.w("Failed to find post " + comment.postId);
                    } else {
                        addComment(comment.postId, comment);
                    }
                }
                notifyCommentsAdded(comments);
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
        executor.execute(() -> {
            for (Post post : posts) {
                postCache.put(post.id, post);
            }
            commentCache.putAll(commentMap);
        });
    }

    public void addPost(@NonNull Post post) {
        executor.execute(() -> {
            postCache.put(post.id, post);
        });
    }

    public void addComment(@NonNull String postId, @NonNull Comment comment) {
        executor.execute(() -> {
            Post post = postCache.get(postId);
            List<Comment> comments = commentCache.get(postId);
            if (comments == null) {
                comments = new ArrayList<>();
            }
            comments = new ArrayList<>(comments);
            comments.add(comment);
            commentCache.put(postId, comments);
            post.commentCount = comments.size();
        });
    }

    public void removeComment(@NonNull String postId, @NonNull Comment comment) {
        executor.execute(() -> {
            Post post = postCache.get(postId);
            List<Comment> existingComments = commentCache.get(postId);
            if (existingComments == null || post == null) {
                Log.w("Failed to find post " + postId);
            } else {
                existingComments = new ArrayList<>(existingComments);
                existingComments.remove(comment);
                commentCache.put(postId, existingComments);
                post.commentCount = existingComments.size();
            }
        });
    }

    public Post getPost(@NonNull String postId) {
        return postCache.get(postId);
    }

    public List<Comment> getComments(@NonNull String postId) {
        List<Comment> comments = commentCache.get(postId);
        comments = new ArrayList<>(comments);
        return comments;
    }

    public void clear() {
        executor.execute(() -> {
            postCache.clear();
            commentCache.clear();
        });
    }

    public void removeReportedPost(@NonNull String postId) {
        executor.execute(() -> {
            Post post = postCache.remove(postId);
            if (post == null) {
                Log.w("Failed to find post " + postId);
                return;
            }
            notifyPostRemoved(post);
        });
    }

    public void notifyPostRemoved(@NonNull Post post) {
        synchronized (observers) {
            for (PublicContentCache.Observer observer : observers) {
                observer.onPostRemoved(post);
            }
        }
    }

    public void notifyCommentsAdded(@NonNull List<Comment> comments) {
        synchronized (observers) {
            for (PublicContentCache.Observer observer : observers) {
                observer.onCommentsAdded(comments);
            }
        }
    }

    public void notifyCommentRetracted(@NonNull Comment comment) {
        synchronized (observers) {
            for (PublicContentCache.Observer observer : observers) {
                observer.onCommentRetracted(comment);
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
        void onCommentsAdded(@NonNull List<Comment> comments);
        void onCommentRetracted(@NonNull Comment comment);
    }

    public static class DefaultObserver implements PublicContentCache.Observer {
        public void onPostRemoved(@NonNull Post post) {}
        public void onCommentsAdded(@NonNull List<Comment> comments) {}
        public void onCommentRetracted(@NonNull Comment comment) {}
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
        FeedContentParser feedContentParser = new FeedContentParser(me);
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


                processProtoComments(post, item.getCommentsList(), feedContentParser, commentMap);

                posts.add(post);
            } catch (InvalidProtocolBufferException e) {
                Log.e("Failed to parse container for public feed post", e);
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



    public void processProtoComments(Post post, List<com.halloapp.proto.server.Comment> commentsList, FeedContentParser feedContentParser, Map<String, List<Comment>> commentMap) {
        String meUser = me.getUser();

        List<Comment> comments = new ArrayList<>();
        for (com.halloapp.proto.server.Comment protoComment : commentsList) {
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
                Log.e("Failed to parse container for public feed comment", e);
            }
        }

        commentMap.put(post.id, comments);
        post.commentCount = comments.size();
    }

    private List<com.halloapp.proto.server.Comment> getProtoComments(List<FeedItem> feedItems) {
        List<com.halloapp.proto.server.Comment> comments = new ArrayList<>();
        for (FeedItem item : feedItems) {
            if (item.hasComment() && item.getAction().equals(FeedItem.Action.PUBLIC_UPDATE_PUBLISH)) {
                comments.add(item.getComment());
            }
        }
        return comments;
    }

    public void subscribeToPost(Post post) {
        executor.execute(() -> {
            if (post == null) {
                Log.w("Cannot subscribe to null post");
                return;
            }
            String postId = post.id;
            Connection.getInstance().sendPostSubscriptionRequest(postId).onResponse(res -> {
                if (res.success) {
                    Log.d("ViewKatchupCommentsActivity successfully subscribed to post " + postId);
                    FeedContentParser feedContentParser = new FeedContentParser(me);
                    Map<String, List<Comment>> commentMap = new HashMap<>();
                    List<com.halloapp.proto.server.Comment> comments = getProtoComments(res.feedItems);
                    processProtoComments(post, comments, feedContentParser, commentMap);
                    commentCache.putAll(commentMap);
                    notifyCommentsAdded(commentMap.get(post.id));
                } else {
                    Log.w("ViewKatchupCommentsActivity post " + postId + " subscription failed: " + res.reason);
                }
            }).onError(err -> {
                Log.w("ViewKatchupCommentsActivity post " + postId + " subscription request failed", err);
            });
        });
    }
}
