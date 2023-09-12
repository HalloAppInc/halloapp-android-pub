package com.halloapp.ui.home;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.halloapp.Constants;
import com.halloapp.Preferences;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.MomentPost;
import com.halloapp.content.Post;
import com.halloapp.content.PostsDataSource;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.FilterUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.invites.InvitesApi;
import com.halloapp.xmpp.invites.InvitesResponseIq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AndroidViewModel {

    private static final String STATE_SAVED_SCROLL_STATE = "homeviewmodel_saved_scroll_state";
    private static final int MAX_SUGGESTED_CONTACTS = 10;

    final LiveData<PagedList<Post>> postList;
    final ComputableLiveData<List<MomentPost>> momentList;

    final ComputableLiveData<List<Post>> unseenHomePosts;
    final MutableLiveData<List<Contact>> suggestedContacts = new MutableLiveData<>(null);

    private final MutableLiveData<Boolean> fabMenuOpen = new MutableLiveData<>(false);

    private final BgWorkers bgWorkers;
    private final ContentDb contentDb;
    private final ContactsDb contactsDb;
    private final Preferences preferences;
    private final AtomicBoolean pendingOutgoing = new AtomicBoolean(false);
    private final AtomicBoolean pendingIncoming = new AtomicBoolean(false);
    private final PostsDataSource.Factory dataSourceFactory;

    private InvitesApi invitesApi;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Parcelable savedScrollState;

    private long lastSeenTimestamp;
    private long lastSavedTimestamp;
    private String requestedTopMomentId = null;

    private final VoiceNotePlayer voiceNotePlayer;

    private final ContentDb.Observer contentObserver = new ContentDb.DefaultObserver() {
        @Override
        public void onPostAdded(@NonNull Post post) {
            if (post.isOutgoing()) {
                pendingOutgoing.set(true);
                mainHandler.post(() -> reloadPostsAt(Long.MAX_VALUE));
            } else {
                if (post.type != Post.TYPE_SYSTEM) {
                    pendingIncoming.set(true);
                }
                invalidatePosts();
                lastSeenTimestamp = Math.max(post.timestamp, lastSeenTimestamp);
                unseenHomePosts.invalidate();
            }
        }

        @Override
        public void onPostRetracted(@NonNull Post post) {
            invalidatePosts();
        }

        @Override
        public void onPostUpdated(@NonNull UserId senderUserId, @NonNull String postId) {
            invalidatePosts();
        }

        @Override
        public void onOutgoingPostSeen(@NonNull UserId seenByUserId, @NonNull String postId) {
            invalidatePosts();
        }


        @Override
        public void onIncomingPostSeen(@NonNull UserId senderUserId, @NonNull String postId, @Nullable GroupId groupId) {
            unseenHomePosts.invalidate();
            momentList.invalidate();
        }

        @Override
        public void onCommentAdded(@NonNull Comment comment) {
            invalidatePosts();
        }

        @Override
        public void onCommentRetracted(@NonNull Comment comment) {
            invalidatePosts();
        }

        @Override
        public void onCommentsSeen(@NonNull UserId postSenderUserId, @NonNull String postId, @Nullable GroupId parentGroup) {
            invalidatePosts();
        }

        @Override
        public void onChatDeleted(@NonNull ChatId chatId) {
            if (chatId instanceof GroupId) {
                invalidatePosts();
            }
        }

        @Override
        public void onFeedCleanup() {
            invalidatePosts();
        }

        private void invalidatePosts() {
            momentList.invalidate();
            mainHandler.post(dataSourceFactory::invalidateLatestDataSource);
        }
    };

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {

        @Override
        public void onSuggestedContactDismissed(long addressBookId) {
            List<Contact> currentList = suggestedContacts.getValue();
            if (currentList == null) {
                return;
            }
            currentList = new ArrayList<>(currentList);
            ListIterator<Contact> iterator = currentList.listIterator();
            while (iterator.hasNext()) {
                Contact contact = iterator.next();
                if (contact.getAddressBookId() == addressBookId) {
                    iterator.remove();
                }
            }
            suggestedContacts.postValue(currentList);
        }
    };

    public HomeViewModel(@NonNull Application application) {
        super(application);

        bgWorkers = BgWorkers.getInstance();
        contentDb = ContentDb.getInstance();
        contactsDb = ContactsDb.getInstance();
        contactsDb.addObserver(contactsObserver);
        preferences = Preferences.getInstance();

        invitesApi = new InvitesApi(Connection.getInstance());

        dataSourceFactory = new PostsDataSource.Factory(contentDb, null, null);
        postList = new LivePagedListBuilder<>(dataSourceFactory, 50).build();

        voiceNotePlayer = new VoiceNotePlayer(application);

        unseenHomePosts = new ComputableLiveData<List<Post>>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected List<Post> compute() {
                long lastTime = preferences.getLastSeenPostTime();
                List<Post> unseenPosts = contentDb.getUnseenPosts(lastTime, 5);
                if (!unseenPosts.isEmpty()) {
                    lastSeenTimestamp = Math.max(lastSeenTimestamp, unseenPosts.get(0).timestamp);
                }
                return unseenPosts;
            }
        };

        momentList = new ComputableLiveData<List<MomentPost>>() {
            @SuppressLint("RestrictedApi")
            @Override
            protected List<MomentPost> compute() {
                List<MomentPost> oldMoments = momentList.getLiveData().getValue();
                List<MomentPost> newMoments = contentDb.getMoments();
                MomentPost ownMoment = getOwnMomentFromList(newMoments);

                if (oldMoments != null) {
                    keepTopMomentsOnTop(oldMoments, newMoments);
                }

                if (requestedTopMomentId != null) {
                    MomentPost moment = getMomentFromList(newMoments, requestedTopMomentId);
                    requestedTopMomentId = null;

                    if (moment != null) {
                        newMoments.remove(moment);
                        newMoments.add(0, moment);
                    }
                } else if (ownMoment != null && oldMoments != null && getOwnMomentFromList(oldMoments) == null) {
                    // add your moment on top when it first appears
                    newMoments.remove(ownMoment);
                    newMoments.add(0, ownMoment);
                }

                return newMoments;
            }
        };

        contentDb.addObserver(contentObserver);

        loadSuggestedContacts();
    }

    private void loadSuggestedContacts() {
        bgWorkers.execute(() -> {
            List<Contact> contacts = contactsDb.getSuggestedContactsForInvite();
            Collections.shuffle(contacts);
            if (contacts.size() > MAX_SUGGESTED_CONTACTS) {
                contacts = contacts.subList(0, MAX_SUGGESTED_CONTACTS);
            }
            ListIterator<Contact> iterator = contacts.listIterator();
            while (iterator.hasNext()) {
                Contact contact = iterator.next();
                List<String> tokens = FilterUtils.getFilterTokens(contact.getDisplayName());
                if (tokens != null) {
                    for (String token : Constants.BANNED_INVITE_SUGGEST_TOKENS) {
                        if (tokens.contains(token)) {
                            iterator.remove();
                        }
                    }
                }
            }
            suggestedContacts.postValue(contacts);
        });
    }

    public LiveData<Integer> sendInvite(@NonNull Contact contact) {
        MutableLiveData<Integer> inviteResult = new MutableLiveData<>();
        bgWorkers.execute(() -> {
            invitesApi.sendInvite(Preconditions.checkNotNull(contact.normalizedPhone)).onResponse(result -> {
                inviteResult.postValue(result);
                if (result != null && InvitesResponseIq.Result.SUCCESS == result) {
                    contactsDb.markInvited(contact);
                }
            }).onError(e -> {
                inviteResult.postValue(InvitesResponseIq.Result.UNKNOWN);
                Log.e("inviteFriendsViewModel/sendInvite failed to send invite", e);
            });
        });
        return inviteResult;
    }

    public void loadSavedState(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null && savedScrollState == null) {
            savedScrollState = savedInstanceState.getParcelable(STATE_SAVED_SCROLL_STATE);
        }
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        if (savedScrollState != null) {
            outState.putParcelable(STATE_SAVED_SCROLL_STATE, savedScrollState);
        }
    }

    public LiveData<List<Post>> getUnseenHomePosts() {
        return unseenHomePosts.getLiveData();
    }

    public LiveData<List<Contact>> getSuggestedContacts() {
        return suggestedContacts;
    }

    @Override
    protected void onCleared() {
        contentDb.removeObserver(contentObserver);
        contactsDb.removeObserver(contactsObserver);
        voiceNotePlayer.onCleared();
    }

    boolean checkPendingOutgoing() {
        return pendingOutgoing.compareAndSet(true, false);
    }

    boolean checkPendingIncoming() {
        return pendingIncoming.compareAndSet(true, false);
    }

    void reloadPostsAt(long timestamp) {
        final PagedList<Post> pagedList = postList.getValue();
        if (pagedList != null) {
            ((PostsDataSource)pagedList.getDataSource()).reloadAt(timestamp);
        }
    }

    public void saveScrollState(@Nullable Parcelable savedScrollState) {
        this.savedScrollState = savedScrollState;
    }

    public @Nullable Parcelable getSavedScrollState() {
        return savedScrollState;
    }

    public void onScrollToTop() {
        if (lastSavedTimestamp != lastSeenTimestamp) {
            lastSavedTimestamp = lastSeenTimestamp;
            bgWorkers.execute(() -> {
                preferences.setLastSeenPostTime(lastSeenTimestamp);
                unseenHomePosts.invalidate();
            });
        }
    }

    @NonNull
    public VoiceNotePlayer getVoiceNotePlayer() {
        return voiceNotePlayer;
    }

    public void onFabMenuOpened() {
        fabMenuOpen.setValue(true);
    }

    public void onFabMenuClosed() {
        fabMenuOpen.setValue(false);
    }

    @NonNull
    public LiveData<Boolean> getFabMenuOpen() {
        return fabMenuOpen;
    }

    public void setRequestedTopMomentId(@Nullable String momentId) {
        requestedTopMomentId = momentId;
        momentList.invalidate();
    }

    private void keepTopMomentsOnTop(@NonNull List<MomentPost> oldMoments, @NonNull List<MomentPost> newMoments) {
        if (oldMoments.size() > 2) {
            MomentPost moment = getMomentFromList(newMoments, oldMoments.get(2).id);
            if (moment != null) {
                newMoments.remove(moment);
                newMoments.add(0, moment);
            }
        }

        if (oldMoments.size() > 1) {
            MomentPost moment = getMomentFromList(newMoments, oldMoments.get(1).id);
            if (moment != null) {
                newMoments.remove(moment);
                newMoments.add(0, moment);
            }
        }

        if (oldMoments.size() > 0) {
            MomentPost moment = getMomentFromList(newMoments, oldMoments.get(0).id);
            if (moment != null) {
                newMoments.remove(moment);
                newMoments.add(0, moment);
            }
        }
    }

    @Nullable
    private MomentPost getOwnMomentFromList(@NonNull List<MomentPost> list) {
        for (MomentPost moment : list) {
            if (moment.isOutgoing()) {
                return moment;
            }
        }

        return null;
    }

    @Nullable
    private MomentPost getMomentFromList(@NonNull List<MomentPost> list, @NonNull String id) {
        for (MomentPost item : list) {
            if (item.id.equals(id)) {
                return item;
            }
        }

        return null;
    }
}
