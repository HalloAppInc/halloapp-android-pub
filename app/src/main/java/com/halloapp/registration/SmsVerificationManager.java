package com.halloapp.registration;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.Task;
import com.halloapp.util.Log;

import java.util.HashSet;
import java.util.Set;

public class SmsVerificationManager {

    private static SmsVerificationManager instance;

    private final Set<Observer> observers = new HashSet<>();

    private String lastReceivedCode;

    public interface Observer {
        void onVerificationSmsReceived(String code);
        void onVerificationSmsFailed();
    }

    public static SmsVerificationManager getInstance() {
        if (instance == null) {
            synchronized(Registration.class) {
                if (instance == null) {
                    instance = new SmsVerificationManager();
                }
            }
        }
        return instance;
    }

    private SmsVerificationManager() {
    }

    public String getLastReceivedCode() {
        return lastReceivedCode;
    }

    public void start(@NonNull Context context) {
        Log.i("SmsVerificationManager.start");
        lastReceivedCode = null;

        // Get an instance of SmsRetrieverClient, used to start listening for a matching SMS message
        SmsRetrieverClient client = SmsRetriever.getClient(context);

        // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
        // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
        // action SmsRetriever#SMS_RETRIEVED_ACTION.
        Task<Void> task = client.startSmsRetriever();

        // Listen for success/failure of the start Task. If in a background thread, this
        // can be made blocking using Tasks.await(task, [timeout]);
        task.addOnSuccessListener(aVoid -> {
            // Successfully started retriever, expect broadcast intent
            Log.i("SmsVerificationManager.onSuccess");
        });

        task.addOnFailureListener(e -> {
            Log.e("SmsVerificationManager.onFailure", e);
            notifyVerificationSmsFailed();
        });
    }

    public void addObserver(Observer observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void removeObserver(@NonNull Observer observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    void notifyVerificationSmsReceived(@NonNull String code) {
        Log.i("SmsVerificationManager.notifyVerificationSmsReceived: " + code);
        lastReceivedCode = code;
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onVerificationSmsReceived(code);
            }
        }
    }

    void notifyVerificationSmsFailed() {
        Log.i("SmsVerificationManager.notifyVerificationSmsFailed");
        lastReceivedCode = null;
        synchronized (observers) {
            for (Observer observer : observers) {
                observer.onVerificationSmsFailed();
            }
        }
    }
}
