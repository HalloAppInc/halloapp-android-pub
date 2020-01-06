package com.halloapp;

import android.app.Application;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.crashlytics.android.Crashlytics;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.Log;

import org.jivesoftware.smack.SmackConfiguration;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Domainpart;
import org.jxmpp.jid.parts.Localpart;

import java.util.Arrays;
import java.util.List;

public class HalloApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("halloapp: onCreate");

        Crashlytics.setBool("debug", BuildConfig.DEBUG);

        SmackConfiguration.DEBUG = BuildConfig.DEBUG;

        final PostsDb postsDb = PostsDb.getInstance(this);
        final Connection connection = Connection.getInstance(new ConnectionObserver(postsDb));
        postsDb.addObserver(new MainPostsObserver(connection, getFilesDir(), postsDb));

        connect(connection);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver() {

            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            void onBackground() {
                Log.i("halloapp: onBackground");
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            void onForeground() {
                Log.i("halloapp: onForeground");
                connect(connection);
            }
        });

        List<Jid> contacts = Arrays.asList(
                //JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("13477521636"), Domainpart.fromOrNull(Connection.XMPP_DOMAIN)), // duygu
                //JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("14703381473"), Domainpart.fromOrNull(Connection.XMPP_DOMAIN)), // murali
                //JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("14154121848"), Domainpart.fromOrNull(Connection.XMPP_DOMAIN)), // michael
                //JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("14088922686"), Domainpart.fromOrNull(Connection.XMPP_DOMAIN)), // tony
                JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("16502752675"), Domainpart.fromOrNull(Connection.XMPP_DOMAIN)),
                JidCreate.entityBareFrom(Localpart.fromOrThrowUnchecked("16502813677"), Domainpart.fromOrNull(Connection.XMPP_DOMAIN))
        );
        connection.syncPubSub(contacts);
        //AddressBookContacts.getAddressBookContacts(this);
    }

    private void connect(@NonNull Connection connection) {
        if (Build.MODEL.contains("Android SDK")) {
            connection.connect("16502752675", "CdnEMOAcO4xSoOsOhsDs4ChGeV2weCHK");
            Crashlytics.setString("user", "16502752675");
        } else {
            connection.connect("16502813677", "_SBgWL2sz6GRbBa12AdbUJ1IuO4q1o2j");
            Crashlytics.setString("user", "16502813677");
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w("low memory");
    }
}
