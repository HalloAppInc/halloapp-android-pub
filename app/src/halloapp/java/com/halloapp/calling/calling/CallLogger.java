package com.halloapp.calling.calling;

import com.halloapp.util.logs.Log;
import org.webrtc.Logging;

public class CallLogger implements org.webrtc.Loggable {
    @Override
    public void onLogMessage(String message, Logging.Severity severity, String tag) {
        if (severity == Logging.Severity.LS_ERROR) {
            Log.e("Webrtc Log: " + tag + " " + message);
            return;
        }
        if (severity == Logging.Severity.LS_WARNING) {
            Log.w("Webrtc Log: " + tag + " " + message);
            return;
        }
        Log.d("Webrtc Log: " + tag + " " + message);
    }
}
