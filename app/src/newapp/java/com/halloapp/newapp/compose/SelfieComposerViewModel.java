package com.halloapp.newapp.compose;

import android.net.Uri;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.halloapp.content.Media;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SelfieComposerViewModel extends ViewModel {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ComposeState.COMPOSING_CONTENT, ComposeState.COMPOSING_SELFIE, ComposeState.READY_TO_SEND})
    public @interface ComposeState {
        int COMPOSING_CONTENT = 0;
        int COMPOSING_SELFIE = 1;
        int READY_TO_SEND = 2;
    }

    private final MutableLiveData<Integer> currentState = new MutableLiveData<>(ComposeState.COMPOSING_CONTENT);

    public LiveData<Integer> getComposerState() {
        return currentState;
    }

    public void onCapturedSelfie(@NonNull Uri selfieUri) {
        currentState.setValue(ComposeState.READY_TO_SEND);
    }

    public void onDiscardSelfie() {
        currentState.setValue(ComposeState.COMPOSING_SELFIE);
    }

    public void onComposedMedia(@NonNull Uri uri, @Media.MediaType int mediaType) {
        currentState.setValue(ComposeState.COMPOSING_SELFIE);
    }

    public void onComposedText(@NonNull String text, @ColorInt int color) {

    }

    public boolean onBackPressed() {
        switch (currentState.getValue()) {
            case ComposeState.COMPOSING_CONTENT:
                return true;
            case ComposeState.COMPOSING_SELFIE:
                currentState.setValue(ComposeState.COMPOSING_CONTENT);
                return false;
            case ComposeState.READY_TO_SEND:
                currentState.setValue(ComposeState.COMPOSING_SELFIE);
                return false;
        }
        return true;
    }

}
