package com.halloapp.ui.groups;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.id.UserId;

public class GroupParticipants {

    public static final int[] PARTICIPANT_COLORS = {
            R.color.group_name_1,
            R.color.group_name_2,
            R.color.group_name_3,
            R.color.group_name_4,
            R.color.group_name_5,
            R.color.group_name_6,
            R.color.group_name_7,
            R.color.group_name_8,
            R.color.group_name_9,
            R.color.group_name_10,
            R.color.group_name_11,
            R.color.group_name_12,
            R.color.group_name_13,
            R.color.group_name_14,
    };

    public static final int[] PARTICIPANT_REPLY_BG_COLORS = {
            R.color.group_bg_1,
            R.color.group_bg_2,
            R.color.group_bg_3,
            R.color.group_bg_4,
            R.color.group_bg_5,
            R.color.group_bg_6,
            R.color.group_bg_7,
            R.color.group_bg_8,
            R.color.group_bg_9,
            R.color.group_bg_10,
            R.color.group_bg_11,
            R.color.group_bg_12,
            R.color.group_bg_13,
            R.color.group_bg_14,
    };

    public static int getParticipantNameColor(@NonNull Context context, @Nullable UserId userId) {
        if (userId == null || userId.isMe()) {
            return ContextCompat.getColor(context, R.color.group_me);
        }
        int colorIndex = (int) (Long.parseLong(userId.rawId()) % PARTICIPANT_COLORS.length);
        return ContextCompat.getColor(context, PARTICIPANT_COLORS[colorIndex]);
    }

    public static int getParticipantReplyBgColor(@NonNull Context context, @Nullable UserId userId) {
        if (userId == null || userId.isMe()) {
            return ContextCompat.getColor(context, R.color.group_me_bg);
        }
        int colorIndex = (int) (Long.parseLong(userId.rawId()) % PARTICIPANT_COLORS.length);
        return ContextCompat.getColor(context, PARTICIPANT_REPLY_BG_COLORS[colorIndex]);
    }

}
