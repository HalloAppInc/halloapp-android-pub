package com.halloapp.katchup.ui;

import androidx.annotation.NonNull;

public class LateEmoji {
    private static final String[] LATE_EMOJI = new String[] {
            "\uD83D\uDC80", // skull
            "\uD83E\uDD78", // disguised face
            "\uD83D\uDE40", // cat surprised
            "\uD83D\uDE35\u200D\uD83D\uDCAB", // confused
            "\uD83D\uDE2E", // surprised
            "\uD83E\uDD71", // yawn
            "\uD83E\uDD77", // ninja
            "\uD83E\uDEC2", // hug
            "\uD83D\uDEF8", // ufo
            "\uD83D\uDCA5" // explosion
    };

    public static String getLateEmoji(@NonNull String postId) {
        int index = postId.hashCode() % LATE_EMOJI.length;
        if (index < 0) {
            index += LATE_EMOJI.length;
        }
        return LATE_EMOJI[index];
    }
}
