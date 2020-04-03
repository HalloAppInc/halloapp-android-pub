package com.halloapp.ui.chats;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.contacts.UserId;
import com.halloapp.content.Chat;
import com.halloapp.content.LoadPostsHistoryWorker;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.content.MessageLoader;
import com.halloapp.ui.AdapterWithLifecycle;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.xmpp.Connection;

import java.util.List;
import java.util.Locale;

public class ChatsFragment extends Fragment {

    private final ChatsAdapter adapter = new ChatsAdapter();
    private final AvatarLoader avatarLoader = AvatarLoader.getInstance(Connection.getInstance(), getContext());
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

        setHasOptionsMenu(true);

        final View root = inflater.inflate(R.layout.fragment_chats, container, false);
        final RecyclerView chatsView = root.findViewById(R.id.chats);
        final View emptyView = root.findViewById(android.R.id.empty);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatsView.setLayoutManager(layoutManager);
        chatsView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ChatsViewModel.class);
        viewModel.chatsList.getLiveData().observe(getViewLifecycleOwner(), chats -> {
            adapter.setChats(chats);
            emptyView.setVisibility(chats.size() == 0 ? View.VISIBLE : View.GONE);
        });
        viewModel.messageUpdated.observe(getViewLifecycleOwner(), updated -> {
            adapter.notifyDataSetChanged();
        });

        chatsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) Preconditions.checkNotNull(getActivity())));

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.chats_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.refresh_contacts: {
                ContactsSync.getInstance(Preconditions.checkNotNull(getContext())).startContactsSync(true);
                LoadPostsHistoryWorker.loadPostsHistory(getContext()); // TODO (ds): remove
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
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

        class ViewHolder extends ViewHolderWithLifecycle {

            final ImageView avatarView;
            final TextView nameView;
            final TextView infoView;
            final TextView newMessagesView;
            final TextView timeView;
            final ImageView statusView;
            final ImageView mediaIcon;

            private Chat chat;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                avatarView = itemView.findViewById(R.id.avatar);
                nameView = itemView.findViewById(R.id.name);
                infoView = itemView.findViewById(R.id.info);
                timeView = itemView.findViewById(R.id.time);
                newMessagesView = itemView.findViewById(R.id.new_messages);
                statusView = itemView.findViewById(R.id.status);
                mediaIcon = itemView.findViewById(R.id.media_icon);
                itemView.setOnClickListener(v -> {
                    startActivity(new Intent(getContext(), ChatActivity.class).putExtra(ChatActivity.EXTRA_CHAT_ID, chat.chatId));
                });
            }

            void bindTo(@NonNull Chat chat) {
                this.chat = chat;
                TimeFormatter.setTimeDiffText(timeView, System.currentTimeMillis() - chat.timestamp);
                avatarView.setImageResource(R.drawable.avatar_person); // TODO (ds): load contact image
                avatarLoader.load(avatarView, new UserId(chat.chatId));
                nameView.setText(chat.name);
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
                    infoView.setText(chat.info);
                    timeView.setText("");
                    statusView.setVisibility(View.GONE);
                    mediaIcon.setVisibility(View.GONE);
                }
                if (chat.newMessageCount > 0) {
                    newMessagesView.setVisibility(View.VISIBLE);
                    newMessagesView.setText(String.format(Locale.getDefault(), "%d", chat.newMessageCount));
                } else {
                    newMessagesView.setVisibility(View.GONE);
                }
            }

            private void bindMessagePreview(@NonNull Message message) {
                if (message.isIncoming()) {
                    statusView.setVisibility(View.GONE);
                } else {
                    statusView.setVisibility(View.VISIBLE);
                    if (message.seen == Message.SEEN_YES) {
                        statusView.setImageResource(R.drawable.ic_messaging_seen);
                    } else if (message.transferred == Message.TRANSFERRED_DESTINATION) {
                        statusView.setImageResource(R.drawable.ic_messaging_delivered);
                    } else if (message.transferred == Message.TRANSFERRED_SERVER) {
                        statusView.setImageResource(R.drawable.ic_messaging_sent);
                    } else {
                        statusView.setImageResource(R.drawable.ic_messaging_clock);
                    }
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

                infoView.setText(text);
            }
        }
    }
}