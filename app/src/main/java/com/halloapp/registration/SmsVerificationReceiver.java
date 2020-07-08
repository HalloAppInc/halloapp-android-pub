package com.halloapp.registration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.halloapp.util.Log;

public class SmsVerificationReceiver extends BroadcastReceiver {

    private SmsVerificationManager smsVerificationManager = SmsVerificationManager.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.e("SmsVerificationReceiver: no extras");
                return;
            }
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            if (status == null) {
                Log.e("SmsVerificationReceiver: no status");
                return;
            }

            switch(status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    final String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    Log.i("SmsVerificationReceiver: got message " + message);
                    final String code = parseSmsMessage(message);
                    if (code != null) {
                        smsVerificationManager.notifyVerificationSmsReceived(code);
                    } else {
                        smsVerificationManager.notifyVerificationSmsFailed();
                    }
                    break;
                case CommonStatusCodes.TIMEOUT:
                    Log.i("SmsVerificationReceiver: timeout");
                    break;
            }
        }
    }

    public static String parseSmsMessage(String message) {
        if (message == null) {
            return null;
        }
        int index = message.indexOf(':');
        if (index < 0 || index + 7 >= message.length()) {
            return null;
        }
        return message.substring(index + 2, index + 8).trim();
    }
}
