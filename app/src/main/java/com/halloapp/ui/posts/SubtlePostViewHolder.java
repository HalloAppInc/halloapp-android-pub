package com.halloapp.ui.posts;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.util.ListFormatter;
import com.halloapp.util.StringUtils;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SeenDetectorLayout;

import java.util.ArrayList;
import java.util.List;

public class SubtlePostViewHolder extends ViewHolderWithLifecycle {

    private Post post;
    private PostViewHolder.PostViewHolderParent parent;

    private TextView textView;
    private TextView timeView;

    public SubtlePostViewHolder(@NonNull View itemView, @NonNull PostViewHolder.PostViewHolderParent parent) {
        super(itemView);

        this.parent = parent;
        this.textView = itemView.findViewById(R.id.deleted_text);
        this.timeView = itemView.findViewById(R.id.time);
    }

    public void bindTo(Post post) {
        this.post = post;
        TimeFormatter.setTimePostsFormat(timeView, post.timestamp);
        parent.getTimestampRefresher().scheduleTimestampRefresh(post.timestamp);

        if (post.type == Post.TYPE_USER) {
            if (post.isOutgoing()) {
                // I deleted a post
                textView.setText(R.string.post_retracted_by_me);
            } else {
                parent.getContactLoader().load(textView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                    @Override
                    public void showResult(@NonNull TextView view, @Nullable Contact result) {
                        Context context = view.getContext();
                        view.setText(context.getString(R.string.post_retracted_with_name, result == null ? context.getString(R.string.unknown_contact) : result.getDisplayName()));
                    }

                    @Override
                    public void showLoading(@NonNull TextView view) {
                        view.setText("");
                    }
                });
            }

            final SeenDetectorLayout postContentLayout = itemView.findViewById(R.id.post_container);
            postContentLayout.setOnSeenListener(() -> {
                if (post.seen == Post.SEEN_NO && post.isIncoming()) {
                    post.seen = Post.SEEN_YES_PENDING;
                    ContentDb.getInstance().setIncomingPostSeen(post.senderUserId, post.id);
                }
            });
        } else {
            bindGroupSystemPostPreview(post);
        }
    }

    private void bindGroupSystemPostPreview(@NonNull Post post) {
        switch (post.usage) {
            case Post.USAGE_CREATE_GROUP: {
                systemMessageSingleUser(post, R.string.system_post_group_created_by_you, R.string.system_post_group_created);
                break;
            }
            case Post.USAGE_ADD_MEMBERS: {
                systemMessageAffectedList(post, R.string.system_post_members_added_by_you, R.string.system_post_members_added);
                break;
            }
            case Post.USAGE_REMOVE_MEMBER: {
                systemMessageAffectedList(post, R.string.system_post_members_removed_by_you, R.string.system_post_members_removed);
                break;
            }
            case Post.USAGE_MEMBER_LEFT: {
                systemMessageSingleUser(post, R.string.system_post_member_you_left, R.string.system_post_member_left);
                break;
            }
            case Post.USAGE_PROMOTE: {
                systemMessageAffectedList(post, R.string.system_post_members_promoted_by_you, R.string.system_post_members_promoted);
                break;
            }
            case Post.USAGE_DEMOTE: {
                systemMessageAffectedList(post, R.string.system_post_members_demoted_by_you, R.string.system_post_members_demoted);
                break;
            }
            case Post.USAGE_AUTO_PROMOTE: {
                systemMessageSingleUser(post, R.string.system_post_member_auto_promoted_you, R.string.system_post_member_auto_promoted);
                break;
            }
            case Post.USAGE_NAME_CHANGE: {
                if (post.senderUserId.isMe()) {
                    textView.setText(itemView.getContext().getString(R.string.system_post_group_name_changed_by_you, post.text));
                } else {
                    parent.getContactLoader().load(textView, post.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                        @Override
                        public void showResult(@NonNull TextView view, @Nullable Contact result) {
                            if (result != null) {
                                textView.setText(StringUtils.parseBoldMedium(itemView.getContext().getString(R.string.system_post_group_name_changed, result.getDisplayName(), post.text)));
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
                systemMessageSingleUser(post, R.string.system_post_group_avatar_changed_by_you, R.string.system_post_group_avatar_changed);
                break;
            }
            case Post.USAGE_GROUP_DELETED: {
                systemMessageSingleUser(post, R.string.system_post_group_deleted_by_you, R.string.system_post_group_deleted);
                break;
            }
            case Post.USAGE_POST:
            default: {
                Log.w("Unrecognized system message usage " + post.usage);
            }
        }
    }

    private void systemMessageSingleUser(@NonNull Post message, @StringRes int meString, @StringRes int otherString) {
        if (message.senderUserId.isMe()) {
            textView.setText(StringUtils.parseBoldMedium(itemView.getContext().getString(meString)));
        } else {
            parent.getContactLoader().load(textView, message.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                @Override
                public void showResult(@NonNull TextView view, @Nullable Contact result) {
                    if (result != null) {
                        textView.setText(StringUtils.parseBoldMedium(itemView.getContext().getString(otherString, result.getDisplayName())));
                    }
                }

                @Override
                public void showLoading(@NonNull TextView view) {
                    textView.setText("");
                }
            });
        }
    }

    private void systemMessageAffectedList(@NonNull Post message, @StringRes int meString, @StringRes int otherString) {
        String commaSeparatedMembers = message.text;
        if (commaSeparatedMembers == null) {
            Log.w("MessageViewHolder system message of type " + message.usage + " missing affected list " + message);
            return;
        }
        String[] parts = commaSeparatedMembers.split(",");
        List<UserId> userIds = new ArrayList<>();
        userIds.add(message.senderUserId);
        for (String part : parts) {
            userIds.add(new UserId(part));
        }
        parent.getContactLoader().loadMultiple(textView, userIds, new ViewDataLoader.Displayer<TextView, List<Contact>>() {
            @Override
            public void showResult(@NonNull TextView view, @Nullable List<Contact> result) {
                if (result != null) {
                    Contact sender = result.get(0);
                    boolean senderIsMe = sender.userId.isMe();
                    List<String> names = new ArrayList<>();
                    for (int i=1; i<result.size(); i++) {
                        Contact contact = result.get(i);
                        names.add(contact.userId.isMe() ? textView.getResources().getString(R.string.you) : contact.getDisplayName());
                    }
                    String formatted = ListFormatter.format(itemView.getContext(), names);
                    if (senderIsMe) {
                        textView.setText(StringUtils.parseBoldMedium(itemView.getContext().getString(meString, formatted)));
                    } else {
                        String senderName = sender.getDisplayName();
                        textView.setText(StringUtils.parseBoldMedium(itemView.getContext().getString(otherString, senderName, formatted)));
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
