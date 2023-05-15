package com.halloapp.katchup;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.ConnectionObservers;
import com.halloapp.Me;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.Post;
import com.halloapp.content.Reaction;
import com.halloapp.content.ReactionComment;
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
                List<Reaction> existingReactions = reactionCache.get(postId);
                Post post = getPost(postId);
                if (post == null || (existingComments == null && existingReactions == null)) {
                    Log.w("Failed to find post " + postId + " with comment " + id);
                    return;
                }
                if (existingComments != null) {
                    existingComments = new ArrayList<>(existingComments);
                    for (Comment comment : existingComments) {
                        if (comment.id.equals(id)) {
                            removeComment(postId, comment);
                            notifyCommentRetracted(comment);
                        }
                    }
                }
                if (existingReactions != null) {
                    existingReactions = new ArrayList<>(existingReactions);
                    for (Reaction reaction : existingReactions) {
                        if (reaction.reactionId.equals(id)) {
                            removeReaction(postId, reaction);
                            notifyReactionRetracted(reaction);
                        }
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
            public void onIncomingPublicFeedItemsReceived(@NonNull List<Comment> commentsReceived) {
                final List<Reaction> reactions = new ArrayList<>();
                final List<Comment> comments = new ArrayList<>();
                for (Comment comment : commentsReceived) {
                    Post post = getPost(comment.postId);
                    if (post == null) {
                        Log.w("Failed to find post " + comment.postId);
                    } else {
                        if (comment instanceof ReactionComment) {
                            addReaction(comment.postId, ((ReactionComment) comment).reaction);
                            reactions.add(((ReactionComment) comment).reaction);
                        } else {
                            addComment(comment.postId, comment);
                            comments.add(comment);
                        }
                    }
                }
                notifyReactionsAdded(reactions);
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

    public MutableLiveData<Boolean> getHasUpdatedPublicFeed() {
        return hasUpdatedFeed;
    }

    private final Map<String, Post> postCache = new HashMap<>();
    private final Map<String, List<Comment>> commentCache = new HashMap<>();
    private final Map<String, List<Reaction>> reactionCache = new HashMap<>();

    public void insertContent(@NonNull List<Post> posts, @NonNull Map<String, List<Comment>> commentMap, @NonNull Map<String, List<Reaction>> reactionMap) {
        executor.execute(() -> {
            for (Post post : posts) {
                postCache.put(post.id, post);
            }
            commentCache.putAll(commentMap);
            reactionCache.putAll(reactionMap);
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

    public void addReaction(@NonNull String postId, @NonNull Reaction reaction) {
        executor.execute(() -> {
            Post post = postCache.get(postId);
            List<Reaction> reactions = reactionCache.get(postId);
            if (reactions == null) {
                reactions = new ArrayList<>();
            }
            reactions = new ArrayList<>(reactions);
            boolean reactionAdded = false;
            for (int i = 0; i < reactions.size(); i++) {
                Reaction existingReaction = reactions.get(i);
                if (existingReaction.contentId.equals(reaction.contentId) && existingReaction.senderUserId.equals(reaction.senderUserId)) {
                    reactions.set(i, reaction);
                    reactionAdded = true;
                    break;
                }
            }
            if (!reactionAdded) {
                reactions.add(reaction);
            }
            reactionCache.put(postId, reactions);
            post.reactionCount = reactions.size();
            boolean reactedByMe = false;
            for (Reaction r : reactions) {
                if (r.senderUserId.isMe()) {
                    reactedByMe = true;
                    break;
                }
            }
            post.reactedByMe = reactedByMe;
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

    public void removeReaction(@NonNull String postId, @NonNull Reaction reaction) {
        executor.execute(() -> {
            Post post = postCache.get(postId);
            List<Reaction> existingReactions = reactionCache.get(postId);
            if (existingReactions == null || post == null) {
                Log.w("Failed to find post " + postId);
            } else {
                existingReactions = new ArrayList<>(existingReactions);
                for (int i = 0; i < existingReactions.size(); i++) {
                    Reaction existingReaction = existingReactions.get(i);
                    if (existingReaction.contentId.equals(reaction.contentId) && existingReaction.senderUserId.equals(reaction.senderUserId)) {
                        existingReactions.remove(i);
                        break;
                    }
                }
                reactionCache.put(postId, existingReactions);
                post.reactionCount = existingReactions.size();
                boolean reactedByMe = false;
                for (Reaction r : existingReactions) {
                    if (r.senderUserId.isMe()) {
                        reactedByMe = true;
                        break;
                    }
                }
                post.reactedByMe = reactedByMe;
            }
        });
    }

    public Post getPost(@NonNull String postId) {
        return postCache.get(postId);
    }

    public List<Comment> getComments(@NonNull String postId) {
        List<Comment> ret = new ArrayList<>();
        List<Comment> comments = commentCache.get(postId);
        if (comments != null) {
            ret.addAll(comments);
        }
        return ret;
    }

    public List<Reaction> getReactions(@NonNull String postId) {
        List<Reaction> ret = new ArrayList<>();
        List<Reaction> reactions = reactionCache.get(postId);
        if (reactions != null) {
            ret.addAll(reactions);
        }
        return ret;
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

    public void notifyReactionsAdded(@NonNull List<Reaction> reactions) {
        synchronized (observers) {
            for (PublicContentCache.Observer observer : observers) {
                observer.onReactionsAdded(reactions);
            }
        }
    }

    public void notifyReactionRetracted(@NonNull Reaction reaction) {
        synchronized (observers) {
            for (PublicContentCache.Observer observer : observers) {
                observer.onReactionRetracted(reaction);
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
        void onCommentRetracted(@NonNull Comment comment);;
        void onReactionsAdded(@NonNull List<Reaction> reactions);
        void onReactionRetracted(@NonNull Reaction comment);
    }

    public static class DefaultObserver implements PublicContentCache.Observer {
        public void onPostRemoved(@NonNull Post post) {}
        public void onCommentsAdded(@NonNull List<Comment> comments) {}
        public void onCommentRetracted(@NonNull Comment comment) {}
        public void onReactionsAdded(@NonNull List<Reaction> reactions) {}
        public void onReactionRetracted(@NonNull Reaction comment) {}
    }

    public List<Post> processPublicFeedItems(List<PublicFeedItem> feedItems, boolean wasRestarted) {
        if (wasRestarted) {
            postIndex = 0;
        }

        Map<UserId, String> namesMap = new HashMap<>();
        Map<UserId, String> usernamesMap = new HashMap<>();
        Map<UserId, String> geotagsMap = new HashMap<>();
        Map<UserId, String> avatarsMap = new HashMap<>();
        List<Post> posts = new ArrayList<>();
        Map<String, List<Comment>> commentMap = new HashMap<>();
        Map<String, List<Reaction>> reactionMap = new HashMap<>();
        FeedContentParser feedContentParser = new FeedContentParser(me);
        for (PublicFeedItem item : feedItems) {
            try {
                UserId publisherUserId = new UserId(Long.toString(item.getPost().getPublisherUid()));
                Container container = Container.parseFrom(item.getPost().getPayload());
                namesMap.put(publisherUserId, item.getPost().getPublisherName());
                usernamesMap.put(publisherUserId, item.getUserProfile().getUsername());
                List<String> geotags = item.getUserProfile().getGeoTagsList();
                geotagsMap.put(publisherUserId, geotags.size() > 0 ? geotags.get(0) : null);
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


                processProtoComments(post, item.getCommentsList(), feedContentParser, commentMap, reactionMap);

                posts.add(post);
            } catch (InvalidProtocolBufferException e) {
                Log.e("Failed to parse container for public feed post", e);
            }
        }

        if (wasRestarted) {
            cachedItems.addAll(posts);
        }

        insertContent(posts, commentMap, reactionMap);

        ContactsDb contactsDb = ContactsDb.getInstance();
        contactsDb.updateUserNames(namesMap);
        contactsDb.updateUserUsernames(usernamesMap);
        contactsDb.updateGeotags(geotagsMap);
        contactsDb.updateUserAvatars(avatarsMap);

        return posts;
    }



    public void processProtoComments(Post post, List<com.halloapp.proto.server.Comment> commentsList, FeedContentParser feedContentParser, Map<String, List<Comment>> commentMap, Map<String, List<Reaction>> reactionMap) {
        String meUser = me.getUser();

        List<Reaction> reactions = new ArrayList<>();
        Set<Pair<String, UserId>> reactionIndexSet = new HashSet<>();
        List<Comment> comments = new ArrayList<>();
        boolean reactedByMe = false;
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
                if (comment instanceof ReactionComment) {
                    Reaction reaction = ((ReactionComment) comment).reaction;
                    Pair<String, UserId> reactionIndex = new Pair<>(reaction.contentId, reaction.senderUserId);
                    if (!reactionIndexSet.contains(reactionIndex)) {
                        reactionIndexSet.add(reactionIndex);
                        reactions.add(reaction);
                        if (reaction.senderUserId.isMe()) {
                            reactedByMe = true;
                        }
                    }
                } else {
                    comments.add(comment);
                }
            } catch (InvalidProtocolBufferException e) {
                Log.e("Failed to parse container for public feed comment", e);
            }
        }

        commentMap.put(post.id, comments);
        reactionMap.put(post.id, reactions);
        post.commentCount = comments.size();
        post.reactionCount = reactions.size();
        post.reactedByMe = reactedByMe;
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
                    Map<String, List<Reaction>> reactionMap = new HashMap<>();
                    List<com.halloapp.proto.server.Comment> protoComments = getProtoComments(res.feedItems);
                    processProtoComments(post, protoComments, feedContentParser, commentMap, reactionMap);
                    commentCache.putAll(commentMap);
                    List<Comment> comments = commentCache.get(post.id);
                    if (comments != null) {
                        notifyCommentsAdded(comments);
                    }
                    reactionCache.putAll(reactionMap);
                    List<Reaction> reactions = reactionCache.get(post.id);
                    if (reactions != null) {
                        notifyReactionsAdded(reactions);
                    }
                } else {
                    Log.w("ViewKatchupCommentsActivity post " + postId + " subscription failed: " + res.reason);
                }
            }).onError(err -> {
                Log.w("ViewKatchupCommentsActivity post " + postId + " subscription request failed", err);
            });
        });
    }
}
