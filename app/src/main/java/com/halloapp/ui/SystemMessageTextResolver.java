package com.halloapp.ui;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Message;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.util.ListFormatter;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.List;

public class SystemMessageTextResolver {

    private final Me me = Me.getInstance();

    private ContactLoader contactLoader;

    public SystemMessageTextResolver(@NonNull ContactLoader contactLoader) {
        this.contactLoader = contactLoader;
    }

    public void bindSystemMessagePostPreview(@NonNull TextView textView, @NonNull Message message) {
        switch (message.usage) {
            case Message.USAGE_BLOCK: {
                textView.setText(R.string.block_notification);
                break;
            }
            case Message.USAGE_UNBLOCK: {
                textView.setText(R.string.unblock_notification);
                break;
            }
            case Message.USAGE_CREATE_GROUP: {
                systemMessageSingleUser(textView, message.senderUserId, R.string.system_message_group_created_by_you, R.string.system_message_group_created);
                break;
            }
            case Message.USAGE_ADD_MEMBERS: {
                systemMessageAffectedList(textView, message, R.string.system_message_members_added_by_you, R.string.system_message_members_added);
                break;
            }
            case Message.USAGE_REMOVE_MEMBER: {
                systemMessageAffectedList(textView, message, R.string.system_message_members_removed_by_you, R.string.system_message_members_removed);
                break;
            }
            case Message.USAGE_MEMBER_LEFT: {
                systemMessageSingleUser(textView, message.senderUserId, R.string.system_message_member_you_left, R.string.system_message_member_left);
                break;
            }
            case Message.USAGE_PROMOTE: {
                systemMessageAffectedList(textView, message, R.string.system_message_members_promoted_by_you, R.string.system_message_members_promoted);
                break;
            }
            case Message.USAGE_DEMOTE: {
                systemMessageAffectedList(textView, message, R.string.system_message_members_demoted_by_you, R.string.system_message_members_demoted);
                break;
            }
            case Message.USAGE_AUTO_PROMOTE: {
                systemMessageSingleUser(textView, message.senderUserId, R.string.system_message_member_auto_promoted_you, R.string.system_message_member_auto_promoted);
                break;
            }
            case Message.USAGE_NAME_CHANGE: {
                if (message.senderUserId.isMe()) {
                    textView.setText(textView.getContext().getString(R.string.system_message_group_name_changed_by_you, message.text));
                } else {
                    contactLoader.load(textView, message.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                        @Override
                        public void showResult(@NonNull TextView view, @Nullable Contact result) {
                            if (result != null) {
                                view.setText(view.getContext().getString(R.string.system_message_group_name_changed, result.getDisplayName(), message.text));
                            }
                        }

                        @Override
                        public void showLoading(@NonNull TextView view) {
                            view.setText("");
                        }
                    });
                }
                break;
            }
            case Message.USAGE_AVATAR_CHANGE: {
                systemMessageSingleUser(textView, message.senderUserId, R.string.system_message_group_avatar_changed_by_you, R.string.system_message_group_avatar_changed);
                break;
            }
            case Message.USAGE_GROUP_DELETED: {
                systemMessageSingleUser(textView, message.senderUserId, R.string.system_message_group_deleted_by_you, R.string.system_message_group_deleted);
                break;
            }
            case Message.USAGE_KEYS_CHANGED: {
                contactLoader.load(textView, message.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                    @Override
                    public void showResult(@NonNull TextView view, @Nullable Contact result) {
                        if (result != null) {
                            view.setText(view.getContext().getString(R.string.system_message_keys_changed, result.getDisplayName()));
                        }
                    }

                    @Override
                    public void showLoading(@NonNull TextView view) {
                        view.setText("");
                    }
                });
                break;
            }
            case Message.USAGE_CHAT:
            default: {
                Log.w("Unrecognized system message usage " + message.usage);
            }
        }
    }

    public void bindGroupSystemPostPreview(@NonNull TextView textView, @NonNull Post post) {
        switch (post.usage) {
            case Post.USAGE_CREATE_GROUP: {
                systemMessageSingleUser(textView, post.senderUserId, R.string.system_post_group_created_by_you, R.string.system_post_group_created);
                break;
            }
            case Post.USAGE_ADD_MEMBERS: {
                systemMessageAffectedList(textView, post, R.string.system_post_members_added_by_you, R.string.system_post_members_added);
                break;
            }
            case Post.USAGE_REMOVE_MEMBER: {
                systemMessageAffectedList(textView, post, R.string.system_post_members_removed_by_you, R.string.system_post_members_removed);
                break;
            }
            case Post.USAGE_MEMBER_LEFT: {
                systemMessageSingleUser(textView, post.senderUserId, R.string.system_post_member_you_left, R.string.system_post_member_left);
                break;
            }
            case Post.USAGE_PROMOTE: {
                systemMessageAffectedList(textView, post, R.string.system_post_members_promoted_by_you, R.string.system_post_members_promoted);
                break;
            }
            case Post.USAGE_DEMOTE: {
                systemMessageAffectedList(textView, post, R.string.system_post_members_demoted_by_you, R.string.system_post_members_demoted);
                break;
            }
            case Post.USAGE_AUTO_PROMOTE: {
                systemMessageSingleUser(textView, post.senderUserId, R.string.system_post_member_auto_promoted_you, R.string.system_post_member_auto_promoted);
                break;
            }
            case Post.USAGE_NAME_CHANGE: {
                if (post.senderUserId.isMe()) {
                    textView.setText(textView.getContext().getString(R.string.system_post_group_name_changed_by_you, post.text));
                } else {
                    contactLoader.load(textView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                        @Override
                        public void showResult(@NonNull TextView view, @Nullable Contact result) {
                            if (result != null) {
                                textView.setText(textView.getContext().getString(R.string.system_post_group_name_changed, result.getDisplayName(), post.text));
                            }
                        }

                        @Override
                        public void showLoading(@NonNull TextView view) {
                            textView.setText("");
                        }
                    });
                }
                break;
            }
            case Post.USAGE_AVATAR_CHANGE: {
                systemMessageSingleUser(textView, post.senderUserId, R.string.system_post_group_avatar_changed_by_you, R.string.system_post_group_avatar_changed);
                break;
            }
            case Post.USAGE_GROUP_DELETED: {
                systemMessageSingleUser(textView, post.senderUserId, R.string.system_post_group_deleted_by_you, R.string.system_post_group_deleted);
                break;
            }
            case Post.USAGE_MEMBER_JOINED: {
                systemMessageSingleUser(textView, post.senderUserId, R.string.system_message_you_joined, R.string.system_message_joined);
                break;
            }
            case Post.USAGE_GROUP_THEME_CHANGED: {
                systemMessageSingleUser(textView, post.senderUserId, R.string.system_message_group_bg_changed_by_you, R.string.system_message_group_bg_changed);
                break;
            }
            case Post.USAGE_POST:
            default: {
                Log.w("Unrecognized system message usage " + post.usage);
            }
        }
    }

    private void systemMessageSingleUser(@NonNull TextView textView, @NonNull UserId senderUserId, @StringRes int meString, @StringRes int otherString) {
        if (senderUserId.isMe()) {
            textView.setText(textView.getContext().getString(meString));
        } else {
            contactLoader.load(textView, senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                    if (result != null) {
                        textView.setText(textView.getContext().getString(otherString, result.getDisplayName()));
                    }
                }

                @Override
                public void showLoading(@NonNull TextView view) {
                    textView.setText("");
                }
            });
        }
    }

    private void systemMessageAffectedList(@NonNull TextView textView, @NonNull Message message, @StringRes int meString, @StringRes int otherString) {
        if (message.text == null) {
            Log.w("SystemMessageTextResolver/systemMessageAffectedList message with usage=" + message.usage + " is missing affected list");
            return;
        }
        systemMessageAffectedList(textView, message.senderUserId, message.text, meString, otherString);
    }

    private void systemMessageAffectedList(@NonNull TextView textView, @NonNull Post post, @StringRes int meString, @StringRes int otherString) {
        if (post.text == null) {
            Log.w("SystemMessageTextResolver/systemMessageAffectedList post with usage=" + post.usage + " is missing affected list");
            return;
        }
        systemMessageAffectedList(textView, post.senderUserId, post.text, meString, otherString);
    }

    private void systemMessageAffectedList(@NonNull TextView textView, @NonNull UserId senderUserId, @NonNull String commaSeparatedMembers, @StringRes int meString, @StringRes int otherString) {
        String[] parts = commaSeparatedMembers.split(",");
        List<UserId> userIds = new ArrayList<>();
        String rawMeId = me.user.getValue();
        userIds.add(senderUserId);
        for (String part : parts) {
            if (part.equals(rawMeId)) {
                userIds.add(UserId.ME);
            } else {
                userIds.add(new UserId(part));
            }
        }
        contactLoader.loadMultiple(textView, userIds, new ViewDataLoader.Displayer<TextView, List<Contact>>() {
            @Override
            public void showResult(@NonNull TextView view, @Nullable List<Contact> result) {
                if (result != null) {
                    Contact sender = result.get(0);
                    boolean senderIsMe = sender.userId.isMe();
                    List<String> names = new ArrayList<>();
                    for (int i = 1; i < result.size(); i++) {
                        Contact contact = result.get(i);
                        names.add(Preconditions.checkNotNull(contact.userId).isMe() ? textView.getResources().getString(R.string.you) : contact.getDisplayName());
                    }
                    String formatted = ListFormatter.format(textView.getContext(), names);
                    if (senderIsMe) {
                        textView.setText(textView.getContext().getString(meString, formatted));
                    } else {
                        String senderName = sender.getDisplayName();
                        textView.setText(textView.getContext().getString(otherString, senderName, formatted));
                    }
                }
            }

            @Override
            public void showLoading(@NonNull TextView view) {
                textView.setText("");
            }
        });
    }
}
