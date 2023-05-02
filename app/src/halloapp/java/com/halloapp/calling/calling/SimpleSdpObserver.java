package com.halloapp.calling.calling;

import com.halloapp.util.logs.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class SimpleSdpObserver implements SdpObserver {

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.i("SimpleSdpObserver: onCreateSuccessful");
    }

    @Override
    public void onSetSuccess() {
        Log.i("SimpleSdpObserver: onSetSuccess");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e("SimpleSdpObserver: onCreateFailure: " + s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e("SimpleSdpObserver: onSetFailure" + s);
    }

}
