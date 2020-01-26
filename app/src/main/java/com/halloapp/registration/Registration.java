package com.halloapp.registration;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.halloapp.Constants;
import com.halloapp.util.FileUtils;
import com.halloapp.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Registration {

    private static Registration instance;

    public static Registration getInstance() {
        if (instance == null) {
            synchronized(Registration.class) {
                if (instance == null) {
                    instance = new Registration();
                }
            }
        }
        return instance;
    }

    private Registration() {
    }

    @WorkerThread
    public void requestRegistration(@NonNull String phone) throws IOException, JSONException {
        InputStream inStream = null;
        HttpURLConnection connection = null;
        try {
            final URL url = new URL("https://s.halloapp.net/cgi-bin/request.sh?user=" + phone);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(30_000);
            connection.setReadTimeout(30_000);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Bad response code: " + connection.getResponseCode());
            }

            inStream = connection.getInputStream();
            final JSONObject responseJson = new JSONObject(FileUtils.inputStreamToString(inStream));

            final String result = responseJson.optString("result");
            Log.i("Registration.requestRegistration: " + result);
            if ("Error".equals(result)) {
                throw new IOException("Registration error");
            }
        } finally {
            FileUtils.closeSilently(inStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @WorkerThread
    public String verifyRegistration(@NonNull String phone, @NonNull String code) throws IOException, JSONException {
        InputStream inStream = null;
        HttpURLConnection connection = null;
        try {
            final URL url = new URL("https://s.halloapp.net/cgi-bin/register.sh?user=" + phone + "&code=" + code);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", Constants.USER_AGENT);
            connection.setUseCaches(false);
            connection.setAllowUserInteraction(false);
            connection.setConnectTimeout(30_000);
            connection.setReadTimeout(30_000);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Bad response code: " + connection.getResponseCode());
            }

            inStream = connection.getInputStream();
            final JSONObject responseJson = new JSONObject(FileUtils.inputStreamToString(inStream));
            final String password = responseJson.optString("pass");
            if (TextUtils.isEmpty(password)) {
                throw new IOException("Unexpected result: " + responseJson.getString("result"));
            }
            return password;
        } finally {
            FileUtils.closeSilently(inStream);
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
