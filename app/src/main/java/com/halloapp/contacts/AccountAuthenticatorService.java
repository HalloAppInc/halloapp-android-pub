package com.halloapp.contacts;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class AccountAuthenticatorService extends Service {

    private static AccountAuthenticator accountAuthenticator = null;

    @Override
    public IBinder onBind(Intent intent) {
        if (android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT.equals(intent.getAction())) {
            return getAuthenticator().getIBinder();
        } else {
            return null;
        }
    }

    private AccountAuthenticator getAuthenticator() {
        synchronized (this) {
            if (accountAuthenticator == null) {
                accountAuthenticator = new AccountAuthenticator(this);
            }
        }

        return accountAuthenticator;
    }

    private static class AccountAuthenticator extends AbstractAccountAuthenticator {

        public AccountAuthenticator(Context context) {
            super(context);
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) {
            return null;
        }

        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
            return null;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
            return null;
        }

        @Override
        public String getAuthTokenLabel(String authTokenType) {
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
            return null;
        }
    }
}
