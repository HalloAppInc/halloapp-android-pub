package com.halloapp.ui.chat.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.contacts.ContactLoader;
import com.halloapp.content.Message;
import com.halloapp.content.MessageDeliveryState;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.profile.ViewProfileActivity;
import com.halloapp.util.TimeFormatter;
import com.halloapp.util.logs.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageInfoActivity extends HalloActivity {

    private static final String ARG_MESSAGE_ID = "message_id";

    private MessageInfoViewModel viewModel;

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();
    private ContactLoader contactLoader;

    private ReceiptAdapter adapter = new ReceiptAdapter();

    public static Intent viewMessageInfo(@NonNull Context context, @NonNull String messageId) {
        Intent i = new Intent(context, MessageInfoActivity.class);
        i.putExtra(ARG_MESSAGE_ID, messageId);
        return i;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        contactLoader = new ContactLoader(userId -> {
            startActivity(ViewProfileActivity.viewProfile(this, userId));
            return null;
        });

        String messageId = getIntent().getStringExtra(ARG_MESSAGE_ID);
        if (messageId == null) {
            Log.e("MessageInfoActivity/onCreate missing message id");
            return;
        }

        viewModel = new ViewModelProvider(this,
                new MessageInfoViewModel.Factory(getApplication(), messageId)).get(MessageInfoViewModel.class);

        setContentView(R.layout.activity_message_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView rv = findViewById(R.id.message_info_rv);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(llm);

        rv.setAdapter(adapter);

        viewModel.getMessageDeliveryState().observe(this, state -> {
            adapter.setReceipts(state);
        });
    }

    private class ReceiptViewHolder extends RecyclerView.ViewHolder {

        private final ImageView avatarView;
        private final TextView nameView;
        private final TextView timeView;
        private final TextView dayView;

        public ReceiptViewHolder(@NonNull View itemView) {
            super(itemView);

            avatarView = itemView.findViewById(R.id.avatar);
            nameView = itemView.findViewById(R.id.name);
            timeView = itemView.findViewById(R.id.timestamp);
            dayView = itemView.findViewById(R.id.day);
        }

        public void bindTo(MessageDeliveryState messageDeliveryState) {
            avatarLoader.load(avatarView, messageDeliveryState.userId);
            contactLoader.load(nameView, messageDeliveryState.userId);
            timeView.setText(TimeFormatter.formatMessageTime(timeView.getContext(), messageDeliveryState.timestamp));
            dayView.setText(TimeFormatter.formatMessageInfoDay(dayView.getContext(), messageDeliveryState.timestamp));
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private static class HeaderItem {
        public final @Message.State int state;
        public HeaderItem(@Message.State int state) {
            this.state = state;
        }
    }

    private final int VIEW_TYPE_HEADER_SEEN = 0;
    private final int VIEW_TYPE_HEADER_DELIVERED = 1;
    private final int VIEW_TYPE_RECEIPT = 2;

    private class ReceiptAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Object> listItems;

        public void setReceipts(@Nullable List<MessageDeliveryState> receipts) {
            if (receipts == null) {
                if (listItems != null) {
                    listItems = null;
                    notifyDataSetChanged();
                }
            } else {
                List<MessageDeliveryState> receiptsList = new ArrayList<>(receipts);
                Collections.sort(receiptsList, (o1, o2) -> {
                    if (o1 == o2) {
                        return 0;
                    }
                    if (o2 == null) {
                        return 1;
                    } else if (o1 == null) {
                        return -1;
                    }
                    if (o1.state != o2.state) {
                        return -(o1.state - o2.state);
                    }
                    return (int) -(o1.timestamp - o2.timestamp);
                });
                listItems = new ArrayList<>(receiptsList.size() + 2);
                MessageDeliveryState prev = null;
                for (MessageDeliveryState receipt : receiptsList) {
                    if (receipt.state < Message.STATE_OUTGOING_DELIVERED) {
                        break;
                    }
                    if (prev == null || prev.state != receipt.state) {
                        if (prev == null && receipt.state == Message.STATE_OUTGOING_DELIVERED) {
                            listItems.add(new HeaderItem(Message.STATE_OUTGOING_SEEN));
                        }
                        listItems.add(new HeaderItem(receipt.state));
                    }
                    prev = receipt;
                    listItems.add(prev);
                }
                if (prev == null) {
                    listItems.add(new HeaderItem(Message.STATE_OUTGOING_SEEN));
                    listItems.add(new HeaderItem(Message.STATE_OUTGOING_DELIVERED));
                }
                notifyDataSetChanged();
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_HEADER_SEEN) {
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_info_seen_by_header, parent, false));
            } else if (viewType == VIEW_TYPE_HEADER_DELIVERED) {
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_info_delivered_header, parent, false));
            }
            return new ReceiptViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_receipt, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ReceiptViewHolder) {
                ((ReceiptViewHolder) holder).bindTo((MessageDeliveryState) listItems.get(position));
            }
        }

        @Override
        public int getItemViewType(int position) {
            Object item = listItems.get(position);
            if (item instanceof HeaderItem) {
                HeaderItem headerItem = (HeaderItem) item;
                if (headerItem.state == Message.STATE_OUTGOING_SEEN) {
                    return VIEW_TYPE_HEADER_SEEN;
                }
                return VIEW_TYPE_HEADER_DELIVERED;
            }
            return VIEW_TYPE_RECEIPT;
        }

        @Override
        public int getItemCount() {
            return listItems == null ? 0 : listItems.size();
        }
    }

}
