package com.halloapp.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.props.ServerProps;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AttachmentPopupWindow extends PopupWindow {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({AttachmentType.GALLERY, AttachmentType.CAMERA, AttachmentType.DOCUMENT, AttachmentType.CONTACT})
    public @interface AttachmentType {
        int GALLERY = 1;
        int CAMERA = 2;
        int DOCUMENT = 3;
        int CONTACT = 4;
    }

    private RecyclerView attachmentRv;
    private AttachmentAdapter adapter;

    private AttachmentListener listener;

    public interface AttachmentListener {
        void onAttachmentSelected(@AttachmentType int type);
    }

    public AttachmentPopupWindow(@NonNull AttachmentListener listener, Context context) {
        super(context);

        this.listener = listener;

        setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        View root = LayoutInflater.from(context).inflate(R.layout.popup_attachment_picker, null, false);
        setContentView(root);

        attachmentRv = root.findViewById(R.id.attachment_rv);
        adapter = new AttachmentAdapter();
        attachmentRv.setAdapter(adapter);
        setOutsideTouchable(true);
        setFocusable(true);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public void show(View anchor, KeyboardAwareLayout keyboardAwareLayout) {
        setHeight(keyboardAwareLayout.getKeyboardHeight());
        if (keyboardAwareLayout.isKeyboardOpen()) {
            getContentView().setBackgroundColor(ContextCompat.getColor(anchor.getContext(), R.color.attachment_picker_bg));
            showAtLocation(keyboardAwareLayout, Gravity.BOTTOM, 0, 0);
        } else {
            attachmentRv.setBackgroundResource(R.drawable.bg_attachment_picker);
            showAsDropDown(anchor, 0, 0, Gravity.TOP);
        }
    }

    private class AttachmentViewHolder extends RecyclerView.ViewHolder {

        private ImageView iconView;
        private TextView labelView;

        private View container;
        private @AttachmentType int type;

        public AttachmentViewHolder(@NonNull View itemView) {
            super(itemView);

            iconView = itemView.findViewById(R.id.icon);
            labelView = itemView.findViewById(R.id.label);

            container = itemView.findViewById(R.id.icon_container);
            container.setOnClickListener(v -> {
                if (type != 0) {
                    listener.onAttachmentSelected(type);
                }
            });
        }

        public void bind(Attachment attachment) {
            iconView.setImageResource(attachment.iconRes);
            labelView.setText(attachment.labelRes);
            this.type = attachment.type;
        }
    }

    private static class Attachment {

        private @DrawableRes int iconRes;
        private @StringRes int labelRes;
        private @AttachmentType int type;

        public Attachment(@DrawableRes int icon, @StringRes int label, @AttachmentType int type) {
            this.iconRes = icon;
            this.labelRes = label;
            this.type = type;
        }

    }

    private class AttachmentAdapter extends RecyclerView.Adapter<AttachmentViewHolder> {

        private final List<Attachment> attachments = new ArrayList<>();

        public AttachmentAdapter() {
            if (ServerProps.getInstance().getFileSharingEnabled()) {
                attachments.add(new Attachment(R.drawable.ic_attachment_document, R.string.attachment_document, AttachmentType.DOCUMENT));
            }
            attachments.add(new Attachment(R.drawable.ic_attachment_camera, R.string.camera_post, AttachmentType.CAMERA));
            attachments.add(new Attachment(R.drawable.ic_attachment_image, R.string.gallery_post, AttachmentType.GALLERY));
            if (ServerProps.getInstance().getContactSharingEnabled()) {
                attachments.add(new Attachment(R.drawable.ic_attachment_contact, R.string.attachment_contact, AttachmentType.CONTACT));
            }
        }

        @NonNull
        @Override
        public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AttachmentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.attachment_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
            holder.bind(attachments.get(position));
        }

        @Override
        public int getItemCount() {
            return attachments.size();
        }
    }
}
