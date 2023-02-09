package com.halloapp.katchup;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.halloapp.Preferences;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.RelationshipListResponseIq;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RelationshipSyncWorker extends Worker {

    private static final String RELATIONSHIP_SYNC_WORKER_ID = "relationship-sync-worker";

    public static void schedule(@NonNull Context context) {
        long lastFullSync = Preferences.getInstance().getLastFullRelationshipSyncTime();
        long syncDelayMs = ServerProps.getInstance().getRelationshipSyncIntervalSeconds() * 1000L;

        long timeSince = System.currentTimeMillis() - lastFullSync;
        long delay = Math.max(0, syncDelayMs - timeSince);

        Constraints.Builder constraintBuilder = new Constraints.Builder();
        constraintBuilder.setRequiredNetworkType(NetworkType.CONNECTED);
        final PeriodicWorkRequest workRequest = (new PeriodicWorkRequest.Builder(RelationshipSyncWorker.class, syncDelayMs, TimeUnit.MILLISECONDS))
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraintBuilder.build()).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(RELATIONSHIP_SYNC_WORKER_ID, ExistingPeriodicWorkPolicy.REPLACE, workRequest);
    }

    public RelationshipSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @Override
    public @NonNull Result doWork() {
        if (isStopped()) {
            return Result.failure();
        }
        Log.i("RelationshipSyncWorker.doWork");
        long lastFullSync = Preferences.getInstance().getLastFullRelationshipSyncTime();
        long syncDelayMs = ServerProps.getInstance().getRelationshipSyncIntervalSeconds() * 1000L;

        long timeSince = System.currentTimeMillis() - lastFullSync;
        if (timeSince > syncDelayMs) {
            Log.i("RelationshipSyncWorker.doWork starting full sync");
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

        for (@RelationshipInfo.Type int relationshipType : new int[] {
                RelationshipInfo.Type.FOLLOWING,
                RelationshipInfo.Type.FOLLOWER,
                RelationshipInfo.Type.BLOCKED
        }) {
            List<RelationshipInfo> localFollowing = contactsDb.getRelationships(relationshipType);
            List<RelationshipInfo> remoteFollowing = new ArrayList<>();
            try {
                RelationshipListResponseIq response;
                do {
                    response = connection.requestRelationshipList(relationshipType).await();
                    remoteFollowing.addAll(response.relationshipList);
                } while (!TextUtils.isEmpty(response.cursor));
            } catch (ObservableErrorException | InterruptedException e) {
                Log.e("Connection failed during relationship sync", e);
                return Result.failure();
            }

            Set<UserId> localUserIds = new HashSet<>();
            Map<UserId, RelationshipInfo> localInfoMap = new HashMap<>();
            for (RelationshipInfo katchupRelationshipInfo : localFollowing) {
                localUserIds.add(katchupRelationshipInfo.userId);
                localInfoMap.put(katchupRelationshipInfo.userId, katchupRelationshipInfo);
            }

            Set<UserId> remoteUserIds = new HashSet<>();
            Map<UserId, RelationshipInfo> remoteInfoMap = new HashMap<>();
            for (RelationshipInfo katchupRelationshipInfo : remoteFollowing) {
                remoteUserIds.add(katchupRelationshipInfo.userId);
                remoteInfoMap.put(katchupRelationshipInfo.userId, katchupRelationshipInfo);
                names.put(katchupRelationshipInfo.userId, katchupRelationshipInfo.name);
                usernames.put(katchupRelationshipInfo.userId, katchupRelationshipInfo.username);
                avatars.put(katchupRelationshipInfo.userId, katchupRelationshipInfo.avatarId);
            }

            Set<UserId> toAdd = new HashSet<>(remoteUserIds);
            toAdd.removeAll(localUserIds);

            Set<UserId> toRemove = new HashSet<>(localUserIds);
            toRemove.removeAll(remoteUserIds);

            Set<UserId> toDiff = new HashSet<>(localUserIds);
            toDiff.addAll(remoteUserIds);
            toDiff.removeAll(toAdd);
            toDiff.removeAll(toRemove);

            List<RelationshipInfo> added = new ArrayList<>();
            for (UserId userId : toAdd) {
                added.add(remoteInfoMap.get(userId));
            }

            List<RelationshipInfo> deleted = new ArrayList<>();
            for (UserId userId : toRemove) {
                deleted.add(localInfoMap.get(userId));
            }

            List<RelationshipInfo> changed = new ArrayList<>();
            for (UserId userId : toDiff) {
                if (!localInfoMap.get(userId).equals(remoteInfoMap.get(userId))) {
                    changed.add(remoteInfoMap.get(userId));
                }
            }

            for (RelationshipInfo info : added) {
                contactsDb.addRelationship(info);
            }
            Log.i("RelationshipSyncWorker: added " + added.size() + " for type " + relationshipType);

            for (RelationshipInfo info : deleted) {
                contactsDb.removeRelationship(info);
            }
            Log.i("RelationshipSyncWorker: deleted " + deleted.size() + " for type " + relationshipType);

            for (RelationshipInfo info : changed) {
                contactsDb.updateRelationship(info);
            }
            Log.i("RelationshipSyncWorker: updated " + changed.size() + " for type " + relationshipType);
        }

        contactsDb.updateUserNames(names);
        contactsDb.updateUserUsernames(usernames);
        contactsDb.updateUserAvatars(avatars);

        Preferences.getInstance().setLastFullRelationshipSyncTime(System.currentTimeMillis());

        return Result.success();
    }
}
