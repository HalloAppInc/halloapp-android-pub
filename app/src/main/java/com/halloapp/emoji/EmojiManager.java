package com.halloapp.emoji;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;

import java.io.IOException;

public class EmojiManager {

    private static EmojiManager instance;

    public static EmojiManager getInstance() {
        if (instance == null) {
            synchronized (EmojiManager.class) {
                if (instance == null) {
                    instance = new EmojiManager(BgWorkers.getInstance());
                }
            }
        }
        return instance;
    }

    private final BgWorkers bgWorkers;

    private EmojiPickerData emojiPickerData;
    private final MutableLiveData<EmojiPickerData> emojiPickerDataMutableLiveData;

    private EmojiManager(@NonNull BgWorkers bgWorkers) {
        this.bgWorkers = bgWorkers;

        emojiPickerDataMutableLiveData = new MutableLiveData<>();
    }

    public LiveData<EmojiPickerData> getEmojiPickerLiveData() {
        return emojiPickerDataMutableLiveData;
    }

    public void init() {
        bgWorkers.execute(() -> {
            try {
                synchronized (this) {
                    emojiPickerData = EmojiPickerData.parse();
                }
                emojiPickerDataMutableLiveData.postValue(emojiPickerData);
            } catch (IOException e) {
                Log.e("EmojiManager/init failed to load emoji picker data", e);
            }
        });
    }

    public synchronized EmojiPickerData getEmojiPickerData() {
        return emojiPickerData;
    }

}
