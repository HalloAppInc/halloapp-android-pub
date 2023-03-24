package com.halloapp.katchup;

import androidx.annotation.NonNull;

import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.RelationshipInfo;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.util.List;

public class KatchupContactsDbObserver extends ContactsDb.BaseObserver {

    private static KatchupContactsDbObserver instance;

    public static KatchupContactsDbObserver getInstance() {
        if (instance == null) {
            synchronized (KatchupContactsDbObserver.class) {
                if (instance == null) {
                    instance = new KatchupContactsDbObserver();
                }
            }
        }
        return instance;
    }

    private BgWorkers bgWorkers;
    private ContentDb contentDb;

    public KatchupContactsDbObserver() {
        this.bgWorkers = BgWorkers.getInstance();
        this.contentDb = ContentDb.getInstance();
    }

    @Override
    public void onRelationshipRemoved(@NonNull RelationshipInfo relationshipInfo) {
        if (relationshipInfo.relationshipType == RelationshipInfo.Type.FOLLOWING) {
            bgWorkers.execute(() -> {
                List<Post> posts = contentDb.getPosts(null, 16, false, relationshipInfo.userId, null);
                for (Post post : posts) {
                    Log.d("KatchupContactsDbObserver deleting post " + post + " from user " + relationshipInfo.userId + " who was removed from following list");
                    contentDb.deletePost(post, null);
                }
            });
        }
    }
}
