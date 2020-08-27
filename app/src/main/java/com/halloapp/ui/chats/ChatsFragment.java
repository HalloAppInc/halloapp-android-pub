package com.halloapp.ui.chats;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Chat;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.groups.GroupInfo;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.ui.AdapterWithLifecycle;
import com.halloapp.ui.HalloFragment;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.chat.MessageViewHolder;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.xmpp.ChatState;
import com.halloapp.xmpp.PresenceLoader;

import java.util.List;
import java.util.Locale;

public class ChatsFragment extends HalloFragment {

    private final ChatsAdapter adapter = new ChatsAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance(getContext());
    private final ContactLoader contactLoader = new ContactLoader(getContext());
    private final PresenceLoader presenceLoader = PresenceLoader.getInstance();
    private ChatsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.i("ChatsFragment.onCreateView");
        final View root = inflater.inflate(R.layout.fragment_chats, container, false);
        final RecyclerView chatsView = root.findViewById(R.id.chats);
        final View emptyView = root.findViewById(android.R.id.empty);

        Preconditions.checkNotNull((SimpleItemAnimator)chatsView.getItemAnimator()).setSupportsChangeAnimations(false);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatsView.setLayoutManager(layoutManager);
        chatsView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ChatsViewModel.class);
        viewModel.chatsList.getLiveData().observe(getViewLifecycleOwner(), chats -> {
            adapter.setChats(chats);
            emptyView.setVisibility(chats.size() == 0 ? View.VISIBLE : View.GONE);
        });
        viewModel.messageUpdated.observe(getViewLifecycleOwner(), updated -> adapter.notifyDataSetChanged());

        chatsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()));

        return root;
    }

    private class ChatsAdapter extends AdapterWithLifecycle<ChatsAdapter.ViewHolder> {

        private List<Chat> chats;

        void setChats(@NonNull List<Chat> chats) {
            this.chats = chats;
            notifyDataSetChanged();
        }

        @Override
        public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bindTo(chats.get(position));
        }

        @Override
        public int getItemCount() {
            return chats == null ? 0 : chats.size();
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            holder.detatchObservers();
            super.onViewRecycled(holder);
        }

        class ViewHolder extends ViewHolderWithLifecycle {

            final ImageView avatarView;
            final TextView nameView;
            final TextView infoView;
            final TextView newMessagesView;
            final TextView timeView;
            final ImageView statusView;
            final ImageView mediaIcon;
            final TextView typingView;
            final View infoContainer;

            private Chat chat;

            private LiveData<PresenceLoader.PresenceState> presenceLiveData;
            private LiveData<PresenceLoader.GroupChatState> groupChatStateLiveData;

            private Observer<PresenceLoader.PresenceState> presenceObserver;
            private Observer<PresenceLoader.GroupChatState> groupChatStateObserver;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                infoView = itemView.findViewById(R.id.info);
                timeView = itemView.findViewById(R.id.time);
                newMessagesView = itemView.findViewById(R.id.new_messages);
                statusView = itemView.findViewById(R.id.status);
                mediaIcon = itemView.findViewById(R.id.media_icon);
                typingView = itemView.findViewById(R.id.typing_indicator);
                infoContainer = itemView.findViewById(R.id.info_container);
                itemView.setOnClickListener(v -> startActivity(new Intent(getContext(), ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, chat.chatId)));

                presenceObserver = presenceState -> {
                    if (presenceState.state == PresenceLoader.PresenceState.PRESENCE_STATE_TYPING) {
                        typingView.setVisibility(View.VISIBLE);
                        infoContainer.setVisibility(View.INVISIBLE);
                        typingView.setText(R.string.user_typing);
                    } else {
                        infoContainer.setVisibility(View.VISIBLE);
                        typingView.setVisibility(View.GONE);
                    }
                };

                groupChatStateObserver = groupChatState -> {
                    int typingUsers = groupChatState.typingUsers.size();
                    if (typingUsers > 0) {
                        typingView.setVisibility(View.VISIBLE);
                        infoContainer.setVisibility(View.INVISIBLE);
                        if (groupChatState.typingUsers.size() == 1) {
                            UserId typingUser = Preconditions.checkNotNull(groupChatState.typingUsers.get(0));
                            Contact contact = Preconditions.checkNotNull(groupChatState.contactMap.get(typingUser));
                            typingView.setText(getString(R.string.group_user_typing, contact.getDisplayName()));
                        } else if (groupChatState.typingUsers.size() > 0) {
                            typingView.setText(getString(R.string.group_many_users_typing));
                        }
                    }else {
                        infoContainer.setVisibility(View.VISIBLE);
                        typingView.setVisibility(View.GONE);
                    }
                };
            }

            void detatchObservers() {
                if (groupChatStateLiveData != null) {
                    groupChatStateLiveData.removeObserver(groupChatStateObserver);
                    groupChatStateLiveData = null;
                }

                if (presenceLiveData != null) {
                    presenceLiveData.removeObserver(presenceObserver);
                    presenceLiveData = null;
                }
            }

            void bindTo(@NonNull Chat chat) {
                this.chat = chat;
                timeView.setText(TimeFormatter.formatRelativeTime(timeView.getContext(), chat.timestamp));
                avatarLoader.load(avatarView, chat.chatId);
                nameView.setText(chat.name);
                detatchObservers();
                if (chat.isGroup) {
                    groupChatStateLiveData = presenceLoader.getChatStateLiveData((GroupId) chat.chatId);
                    groupChatStateLiveData.observe(getViewLifecycleOwner(), groupChatStateObserver);
                } else {
                    presenceLiveData = presenceLoader.getLastSeenLiveData((UserId) chat.chatId);
                    presenceLiveData.observe(getViewLifecycleOwner(), presenceObserver);
                }

                if (chat.lastMessageRowId >= 0) {
                    viewModel.messageLoader.load(itemView, chat.lastMessageRowId, new ViewDataLoader.Displayer<View, Message>() {
                        @Override
                        public void showResult(@NonNull View view, @Nullable Message message) {
                            if (message != null) {
                                bindMessagePreview(message);
                            } else {
                                infoView.setText("");
                                statusView.setVisibility(View.GONE);
                                mediaIcon.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void showLoading(@NonNull View view) {
                            infoView.setText("");
                            statusView.setVisibility(View.GONE);
                            mediaIcon.setVisibility(View.GONE);
                        }
                    });
                } else {
                    viewModel.messageLoader.cancel(itemView);
                    infoView.setText(getText(R.string.empty_chat_placeholder));
                    infoView.setTextColor(getResources().getColor(R.color.empty_chat_placeholder));
                    timeView.setText("");
                    statusView.setVisibility(View.GONE);
                    mediaIcon.setVisibility(View.GONE);
                }
                if (chat.newMessageCount > 0) {
                    newMessagesView.setVisibility(View.VISIBLE);
                    newMessagesView.setText(String.format(Locale.getDefault(), "%d", chat.newMessageCount));
                    timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), R.color.unread_indicator));
                } else {
                    timeView.setTextColor(ContextCompat.getColor(timeView.getContext(), R.color.secondary_text));
                    newMessagesView.setVisibility(View.GONE);
                }
            }

            private void bindMessagePreview(@NonNull Message message) {
                if (message.isIncoming()) {
                    statusView.setVisibility(View.GONE);
                } else {
                    statusView.setVisibility(View.VISIBLE);
                    statusView.setImageResource(MessageViewHolder.getStatusImageResource(message.state));
                }
                if (message.media.size() == 0) {
                    mediaIcon.setVisibility(View.GONE);
                } else if (message.media.size() == 1) {
                    mediaIcon.setVisibility(View.VISIBLE);
                    final Media media = message.media.get(0);
                    switch (media.type) {
                        case Media.MEDIA_TYPE_IMAGE: {
                            mediaIcon.setImageResource(R.drawable.ic_camera);
                            break;
                        }
                        case Media.MEDIA_TYPE_VIDEO: {
                            mediaIcon.setImageResource(R.drawable.ic_video);
                            break;
                        }
                        case Media.MEDIA_TYPE_UNKNOWN:
                        default: {
                            mediaIcon.setImageResource(R.drawable.ic_media_collection);
                            break;
                        }
                    }
                } else {
                    mediaIcon.setVisibility(View.VISIBLE);
                    mediaIcon.setImageResource(R.drawable.ic_media_collection);
                }

                if (message.chatId instanceof GroupId && !message.senderUserId.isMe()) {
                    contactLoader.load(infoView, message.senderUserId, new ViewDataLoader.Displayer<TextView, Contact>() {
                        @Override
                        public void showResult(@NonNull TextView view, @Nullable Contact result) {
                            bindMessageText(result == null ? null : result.getDisplayName(), message);
                        }

                        @Override
                        public void showLoading(@NonNull TextView view) {
                            infoView.setText("");
                        }
                    });
                } else {
                    contactLoader.cancel(infoView);
                    bindMessageText(null, message);
                }
            }

            private void bindMessageText(@Nullable String sender, @NonNull Message message) {
                final String text;
                if (TextUtils.isEmpty(message.text)) {
                    if (message.media.size() == 1) {
                        final Media media = message.media.get(0);
                        switch (media.type) {
                            case Media.MEDIA_TYPE_IMAGE: {
                                text = itemView.getContext().getString(R.string.photo);
                                break;
                            }
                            case Media.MEDIA_TYPE_VIDEO: {
                                text = itemView.getContext().getString(R.string.video);
                                break;
                            }
                            case Media.MEDIA_TYPE_UNKNOWN:
                            default: {
                                text = "";
                                break;
                            }
                        }
                    } else {
                        text = itemView.getContext().getString(R.string.album);
                    }
                } else {
                    text = message.text;
                }
                infoView.setTextColor(getResources().getColor(R.color.chat_message_preview));
                infoView.setText(sender == null ? text : getString(R.string.chat_message_attribution, sender, text));
            }
        }
    }
}
