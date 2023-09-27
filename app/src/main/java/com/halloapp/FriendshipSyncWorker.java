package com.halloapp;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.proto.server.FriendListRequest;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.FriendListResponseIq;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class FriendshipSyncWorker extends Worker {

    private static final String FRIENDSHIP_SYNC_WORKER_ID = "friendship-sync-worker";

    public static void startFriendshipSync(@NonNull Context context) {
        BgWorkers.getInstance().execute(() -> {
            Preferences.getInstance().setLastFullFriendshipSyncTime(0);
            final Data data = new Data.Builder().build();
            final OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(FriendshipSyncWorker.class).setInputData(data).build();
            WorkManager.getInstance(context).enqueueUniqueWork(FriendshipSyncWorker.FRIENDSHIP_SYNC_WORKER_ID, ExistingWorkPolicy.REPLACE, workRequest);
        });
    }

    public static void schedule(@NonNull Context context) {
        long lastFullSync = Preferences.getInstance().getLastFullFriendshipSyncTime();
        long syncDelayMs = ServerProps.getInstance().getFriendshipSyncIntervalSeconds() * 1000L;

        long timeSince = System.currentTimeMillis() - lastFullSync;
        long delay = Math.max(0, syncDelayMs - timeSince);

        Constraints.Builder constraintBuilder = new Constraints.Builder();
        constraintBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
        final PeriodicWorkRequest workRequest = (new PeriodicWorkRequest.Builder(FriendshipSyncWorker.class, syncDelayMs, TimeUnit.MILLISECONDS))
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraintBuilder.build()).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(FRIENDSHIP_SYNC_WORKER_ID, ExistingPeriodicWorkPolicy.REPLACE, workRequest);
    }

    public FriendshipSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public @NonNull Result doWork() {
        if (isStopped()) {
            return Result.failure();
        }
        Log.i("FriendshipSyncWorker.doWork");
        long lastFullSync = Preferences.getInstance().getLastFullFriendshipSyncTime();
        long syncDelayMs = ServerProps.getInstance().getFriendshipSyncIntervalSeconds() * 1000L;

        long timeSince = System.currentTimeMillis() - lastFullSync;
        if (timeSince > syncDelayMs) {
            Log.i("FriendshipSyncWorker.doWork starting full sync");
            return performSync();
        } else {
            schedule(getApplicationContext());
        }
        return Result.success();
    }

    private Result performSync() {
        Connection connection = Connection.getInstance();
        ContactsDb contactsDb = ContactsDb.getInstance();
        Map<UserId, String> names = new HashMap<>();
        Map<UserId, String> usernames = new HashMap<>();
        Map<UserId, String> avatars = new HashMap<>();

        for (FriendListRequest.Action action : new FriendListRequest.Action[] {
                FriendListRequest.Action.GET_FRIENDS,
                FriendListRequest.Action.GET_INCOMING_PENDING,
                FriendListRequest.Action.GET_OUTGOING_PENDING
        }) {
            List<FriendshipInfo> localFollowing = contactsDb.getFriendships(FriendshipInfo.fromFriendListAction(action));
            List<FriendshipInfo> remoteFollowing = new ArrayList<>();
            try {
                String cursor = null;
                FriendListResponseIq response;
                do {
                    response = connection.requestFriendList(cursor, action).await();
                    cursor = response.cursor;
                    remoteFollowing.addAll(response.friendshipList);
                } while (!TextUtils.isEmpty(response.cursor));
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("Connection failed during friendship sync", e);
                return Result.failure();
            }

            Set<UserId> localUserIds = new HashSet<>();
            Map<UserId, FriendshipInfo> localInfoMap = new HashMap<>();
            for (FriendshipInfo friendshipInfo : localFollowing) {
                localUserIds.add(friendshipInfo.userId);
                localInfoMap.put(friendshipInfo.userId, friendshipInfo);
            }

            Set<UserId> remoteUserIds = new HashSet<>();
            Map<UserId, FriendshipInfo> remoteInfoMap = new HashMap<>();
            for (FriendshipInfo friendshipInfo : remoteFollowing) {
                remoteUserIds.add(friendshipInfo.userId);
                remoteInfoMap.put(friendshipInfo.userId, friendshipInfo);
                names.put(friendshipInfo.userId, friendshipInfo.name);
                usernames.put(friendshipInfo.userId, friendshipInfo.username);
                avatars.put(friendshipInfo.userId, friendshipInfo.avatarId);
            }

            Set<UserId> toAdd = new HashSet<>(remoteUserIds);
            toAdd.removeAll(localUserIds);

            Set<UserId> toRemove = new HashSet<>(localUserIds);
            toRemove.removeAll(remoteUserIds);

            Set<UserId> toDiff = new HashSet<>(localUserIds);
            toDiff.addAll(remoteUserIds);
            toDiff.removeAll(toAdd);
            toDiff.removeAll(toRemove);

            List<FriendshipInfo> added = new ArrayList<>();
            for (UserId userId : toAdd) {
                added.add(remoteInfoMap.get(userId));
            }

            List<FriendshipInfo> deleted = new ArrayList<>();
            for (UserId userId : toRemove) {
                deleted.add(localInfoMap.get(userId));
            }

            List<FriendshipInfo> changed = new ArrayList<>();
            for (UserId userId : toDiff) {
                if (!localInfoMap.get(userId).equals(remoteInfoMap.get(userId))) {
                    changed.add(remoteInfoMap.get(userId));
                }
            }

            for (FriendshipInfo info : added) {
                contactsDb.addFriendship(info);
            }
            Log.i("FriendshipSyncWorker: added " + added.size() + " for action " + action);

            for (FriendshipInfo info : deleted) {
                contactsDb.removeFriendship(info);
            }
            Log.i("FriendshipSyncWorker: deleted " + deleted.size() + " for action " + action);

            for (FriendshipInfo info : changed) {
                contactsDb.updateFriendship(info);
            }
            Log.i("FriendshipSyncWorker: updated " + changed.size() + " for action " + action);
        }

        contactsDb.updateUserNames(names);
        contactsDb.updateUserUsernames(usernames);
        contactsDb.updateUserAvatars(avatars);

        Preferences.getInstance().setLastFullFriendshipSyncTime(System.currentTimeMillis());

        return Result.success();
    }
}
