package com.halloapp.ui;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlPreviewTextWatcher implements TextWatcher {

    private static final int URL_DELAY = 1000;

    private Handler mainHandler;

    public interface UrlListener {
        void onUrl(String url);
    }

    private UrlListener listener;

    public UrlPreviewTextWatcher(@NonNull UrlListener listener) {
        mainHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 1) {
                    dispatchUrlChange();
                }
            }
        };
        this.listener = listener;
    }

    private static final String PROTOCOL = "(?i:http|https)://";

    private static final String USER_INFO = "(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
            + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
            + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@";

    private static final String PORT_NUMBER = "\\:\\d{1,5}";

    private static final String UCS_CHAR = "[" +
            "\u00A0-\uD7FF" +
            "\uF900-\uFDCF" +
            "\uFDF0-\uFFEF" +
            "\uD800\uDC00-\uD83F\uDFFD" +
            "\uD840\uDC00-\uD87F\uDFFD" +
            "\uD880\uDC00-\uD8BF\uDFFD" +
            "\uD8C0\uDC00-\uD8FF\uDFFD" +
            "\uD900\uDC00-\uD93F\uDFFD" +
            "\uD940\uDC00-\uD97F\uDFFD" +
            "\uD980\uDC00-\uD9BF\uDFFD" +
            "\uD9C0\uDC00-\uD9FF\uDFFD" +
            "\uDA00\uDC00-\uDA3F\uDFFD" +
            "\uDA40\uDC00-\uDA7F\uDFFD" +
            "\uDA80\uDC00-\uDABF\uDFFD" +
            "\uDAC0\uDC00-\uDAFF\uDFFD" +
            "\uDB00\uDC00-\uDB3F\uDFFD" +
            "\uDB44\uDC00-\uDB7F\uDFFD" +
            "&&[^\u00A0[\u2000-\u200A]\u2028\u2029\u202F\u3000]]";
    private static final String LABEL_CHAR = "a-zA-Z0-9" + UCS_CHAR;
    private static final String PATH_AND_QUERY = "[/\\?](?:(?:[" + LABEL_CHAR
            + ";/\\?:@&=#~"  // plus optional query params
            + "\\-\\.\\+!\\*'\\(\\),_\\$])|(?:%[a-fA-F0-9]{2}))*";
    /* A word boundary or end of input.  This is to stop foo.sure from matching as foo.su */
    private static final String WORD_BOUNDARY = "(?:\\b|$|^)";
    /**
     *  Regular expression pattern to match most part of RFC 3987
     *  Internationalized URLs, aka IRIs.
     */
    public static final Pattern WEB_URL = Pattern.compile("("
            + "("
            + "(?:" + PROTOCOL + "(?:" + USER_INFO + ")?" + ")"
            + "(?:" + Patterns.DOMAIN_NAME.pattern() + ")"
            + "(?:" + PORT_NUMBER + ")?"
            + ")"
            + "(" + PATH_AND_QUERY + ")?"
            + WORD_BOUNDARY
            + ")");

    private String lastUrl;
    private String url;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        Matcher urlMatcher = WEB_URL.matcher(s);
        if (urlMatcher.find()) {
            String matchedUrl = urlMatcher.group();
            if (url == null || !url.equals(matchedUrl)) {
                url = matchedUrl;
            }
        } else {
            url = "";
        }
        onUrlChanged();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void dispatchUrlChange() {
        if (!Objects.equals(lastUrl, url)) {
            listener.onUrl(url);
            lastUrl = url;
        }
    }

    private void onUrlChanged() {
        mainHandler.removeMessages(1);
        Message msg = new Message();
        msg.what = 1;
        mainHandler.sendMessageDelayed(msg, URL_DELAY);
    }
}
