package com.halloapp.ui.groups;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.id.UserId;

public class GroupParticipants {

    private static final int REPLY_NAME_ALPHA = 0x9A;

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
    };

    public static int getColorIndex(@Nullable UserId userId) {
        if (userId == null || userId.isMe()) {
            return 0;
        }
        return (int) (Long.parseLong(userId.rawId()) % PARTICIPANT_COLORS.length);
    }

    public static int getParticipantNameColor(@NonNull Context context, Contact contact) {
        if (contact == null) {
            return 0;
        }
        int colorIndex = contact.getColorIndex() % PARTICIPANT_COLORS.length;
        return ContextCompat.getColor(context, PARTICIPANT_COLORS[colorIndex]);
    }

    public static int getParticipantNameColor(@NonNull Context context, Contact contact, boolean isReply) {
        if (isReply) {
            return ColorUtils.setAlphaComponent(getParticipantNameColor(context, contact), REPLY_NAME_ALPHA);
        }
        return getParticipantNameColor(context, contact);
    }

    public static int getParticipantNameColor(@NonNull Context context, @Nullable UserId userId) {
        if (userId == null || userId.isMe()) {
            return ContextCompat.getColor(context, R.color.group_me);
        }
        int colorIndex = (int) (Long.parseLong(userId.rawId()) % PARTICIPANT_COLORS.length);
        return ContextCompat.getColor(context, PARTICIPANT_COLORS[colorIndex]);
    }

    public static int getParticipantNameColor(@NonNull Context context, @Nullable UserId userId, boolean isReply) {
        if (isReply) {
            return ColorUtils.setAlphaComponent(getParticipantNameColor(context, userId), REPLY_NAME_ALPHA);
        }
        return getParticipantNameColor(context, userId);
    }

}
