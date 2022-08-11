package com.halloapp.emoji;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.emoji2.text.DefaultEmojiCompatConfig;
import androidx.emoji2.text.EmojiCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.halloapp.AppContext;
import com.halloapp.FileStore;
import com.halloapp.Preferences;
import com.halloapp.props.ServerProps;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.FileUtils;
import com.halloapp.util.StringUtils;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class EmojiManager {

    private static EmojiManager instance;

    private static final String EMOJI_FONT_FILE = "emoji_font.ttf";
    private static final String EMOJI_DATA_FILE = "emoji_data.json";

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
    private final FileStore fileStore = FileStore.getInstance();
    private final AppContext appContext = AppContext.getInstance();
    private final Preferences preferences = Preferences.getInstance();
    private final ServerProps serverProps = ServerProps.getInstance();
    private final EmojiVariantManager emojiVariantManager = EmojiVariantManager.getInstance();

    private EmojiPickerData emojiPickerData;
    private final MutableLiveData<EmojiPickerData> emojiPickerDataMutableLiveData;

    private EmojiManager(@NonNull BgWorkers bgWorkers) {
        this.bgWorkers = bgWorkers;
        emojiPickerDataMutableLiveData = new MutableLiveData<>();
    }

    public LiveData<EmojiPickerData> getEmojiPickerLiveData() {
        return emojiPickerDataMutableLiveData;
    }

    public void init(@NonNull Context context) {
        Log.i("EmojiManager.init");
        int localEmojiVersion = preferences.getLocalEmojiVersion();
        EmojiCompat.Config config;
        if (localEmojiVersion > 0) {
            Log.i("EmojiManager/init local emoji files present version=" + localEmojiVersion);
            config = new HAEmojiCompatConfig(getEmojiFontFile());
            config.setReplaceAll(true);
        } else {
            Log.i("EmojiManager/init no local emoji files, using default emoji loader");
            config = DefaultEmojiCompatConfig.create(context);
            EmojiDataDownloadWorker.schedule(context);
        }
        EmojiCompat emojiCompat;
        if (config != null) {
            config.setMetadataLoadStrategy(EmojiCompat.LOAD_STRATEGY_MANUAL);
            emojiCompat = EmojiCompat.init(config);
        } else {
            Log.i("EmojiManager/init no emoji config present");
            emojiCompat = EmojiCompat.init(context);
        }
        bgWorkers.execute(() -> {
            if (emojiCompat != null) {
                Log.i("EmojiManager/init loading emoji compat");
                emojiCompat.load();
            }
            emojiVariantManager.init();
            try {
                synchronized (this) {
                    if (localEmojiVersion == 0) {
                        Log.i("EmojiManager/init loading bundled emoji picker data");
                        emojiPickerData = EmojiPickerData.parse();
                    } else {
                        Log.i("EmojiManager/init loading downloaded emoji picker data");
                        emojiPickerData = EmojiPickerData.parse(getEmojiDataFile());
                    }
                    for (EmojiCategory category : emojiPickerData.categories) {
                        for (Emoji e : category.emojis) {
                            if (e instanceof EmojiWithVariants) {
                                ((EmojiWithVariants) e).setVariantIndex(emojiVariantManager.getVariantIndex(e.getUnicode()));
                            }
                        }
                    }
                }
                emojiPickerDataMutableLiveData.postValue(emojiPickerData);
            } catch (IOException e) {
                Log.e("EmojiManager/init failed to load emoji picker data", e);
                EmojiDataDownloadWorker.schedule(context);
            }
        });
    }

    public File getEmojiFontFile() {
        File emojiDir = fileStore.getEmojiDir();
        return new File(emojiDir, EMOJI_FONT_FILE);
    }

    public File getEmojiDataFile() {
        File emojiDir = fileStore.getEmojiDir();
        return new File(emojiDir, EMOJI_DATA_FILE);
    }

    @WorkerThread
    public boolean validateFiles() {
        EmojiPickerData pickerData;
        try {
            pickerData = EmojiPickerData.parse(getEmojiDataFile());
        } catch (IOException e) {
            Log.e("EmojiManager/validateFiles failed to parse picker data", e);
            return false;
        }
        File font = getEmojiFontFile();
        String md5 = getFileHash(font);
        if (!Objects.equals(pickerData.fontHash, md5)) {
            Log.e("EmojiManager/validateFiles md5 mismatch expected=" + pickerData.fontHash + " actual=" + md5);
            return false;
        }
        return true;
    }

    @Nullable
    private String getFileHash(File file) {
        try {
            byte[] md5 = FileUtils.getFileMd5(file);
            return StringUtils.bytesToHexString(md5);
        } catch (IOException | NoSuchAlgorithmException e) {
            Log.e("EmojiManager/getFileHash failed to get hash", e);
        }
        return null;
    }

    @Nullable
    public String getCurrentFontHash() {
        return getFileHash(getEmojiDataFile());
    }

    public boolean updateEmojis(int newVersion, @Nullable File emojiData, @Nullable File emojiFont) {
        Log.i("EmojiManager/updating emojis to version=" + newVersion);
        File emojiDir = fileStore.getEmojiDir();
        if (emojiFont != null && !emojiFont.renameTo(new File(emojiDir, EMOJI_FONT_FILE))) {
            Log.e("EmojiManager/updateEmojis failed to move font file");
            return false;
        }
        if (emojiData != null && !emojiData.renameTo(getEmojiDataFile())) {
            Log.e("EmojiManager/updateEmojis failed to move emoji data");
            return false;
        }
        try {
            EmojiPickerData newData = EmojiPickerData.parse(getEmojiDataFile());
            emojiPickerDataMutableLiveData.postValue(newData);
            Log.i("EmojiManager/loaded new emoji picker data");
        } catch (IOException e) {
            Log.e("EmojiManager/updateEmojis failed to load new data", e);
            return false;
        }
        preferences.setLocalEmojiVersion(newVersion);
        Log.i("EmojiManager/force reloading emoji font");
        EmojiCompat.Config config = new HAEmojiCompatConfig(getEmojiFontFile());
        config.setReplaceAll(true)
                .setMetadataLoadStrategy(EmojiCompat.LOAD_STRATEGY_MANUAL);
        EmojiCompat.reset(config);
        EmojiCompat.get().load();
        return true;
    }

    public void checkUpdate() {
        if (preferences.getLocalEmojiVersion() < serverProps.getEmojiVersion()) {
            Log.i("EmojiManager/checkUpdate new emoji version found, scheduling download");
            EmojiDataDownloadWorker.schedule(appContext.get());
        }
    }

    public synchronized EmojiPickerData getEmojiPickerData() {
        return emojiPickerData;
    }

}
