package com.halloapp.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.LayoutRes;

import com.halloapp.R;
import com.halloapp.props.ServerProps;

public class ChatInputView extends BaseInputView {

    private AttachmentPopupWindow attachmentPopupWindow;

    private View cameraView;

    public ChatInputView(Context context) {
        super(context);
        init();
    }

    public ChatInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected @LayoutRes int getLayout() {
        return ServerProps.getInstance().useNewAttachmentPicker() ? R.layout.chat_input_layout : R.layout.input_layout;
    }

    private void init() {
        cameraView = findViewById(R.id.camera);
        if (cameraView != null) {
            cameraView.setOnClickListener(v -> {
                inputParent.onChooseCamera();
            });
        }
    }

    protected void onClickMedia() {
        if (ServerProps.getInstance().useNewAttachmentPicker()) {
            if (attachmentPopupWindow != null) {
                attachmentPopupWindow.dismiss();
            }
            attachmentPopupWindow = new AttachmentPopupWindow(type -> {
                switch (type) {
                    case AttachmentPopupWindow.AttachmentType.GALLERY:
                        inputParent.onChooseGallery();
                        break;
                    case AttachmentPopupWindow.AttachmentType.DOCUMENT:
                        inputParent.onChooseDocument();
                        break;
                    case AttachmentPopupWindow.AttachmentType.CAMERA:
                        inputParent.onChooseCamera();
                        break;
                    case AttachmentPopupWindow.AttachmentType.CONTACT:
                        inputParent.onChooseContact();
                        break;
                }
                if (attachmentPopupWindow != null) {
                    attachmentPopupWindow.dismiss();
                }
            }, getContext());
            attachmentPopupWindow.show(this, emojiKeyboardLayout);
        } else {
            super.onClickMedia();
        }
    }
}
