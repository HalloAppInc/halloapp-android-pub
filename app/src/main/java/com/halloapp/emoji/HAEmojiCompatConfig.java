package com.halloapp.emoji;

import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.emoji2.text.EmojiCompat;
import androidx.emoji2.text.MetadataRepo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class HAEmojiCompatConfig extends EmojiCompat.Config {

    public HAEmojiCompatConfig(@NonNull File fontFile) {
        super(new FileMetadataLoader(fontFile));
    }

    private static class FileMetadataLoader implements EmojiCompat.MetadataRepoLoader {
        private final File fontFile;

        FileMetadataLoader(@NonNull File fontFile) {
            this.fontFile = fontFile;
        }

        @Override
        public void load(@NonNull EmojiCompat.MetadataRepoLoaderCallback loaderCallback) {
            final InitRunnable runnable = new InitRunnable(fontFile, loaderCallback);
            final Thread thread = new Thread(runnable);
            thread.setDaemon(false);
            thread.start();
        }
    }

    private static class InitRunnable implements Runnable {
        private final EmojiCompat.MetadataRepoLoaderCallback loaderCallback;
        private final File emojiFont;

        InitRunnable(File emojiFont, EmojiCompat.MetadataRepoLoaderCallback loaderCallback) {
            this.emojiFont = emojiFont;
            this.loaderCallback = loaderCallback;
        }

        @Override
        public void run() {
            try {
                Typeface typeface = Typeface.createFromFile(emojiFont);
                try (InputStream is = new FileInputStream(emojiFont)) {
                    final MetadataRepo resourceIndex = MetadataRepo.create(typeface, is);
                    loaderCallback.onLoaded(resourceIndex);
                }

            } catch (Throwable t) {
                loaderCallback.onFailed(t);
            }
        }
    }
}
