package com.halloapp.ui.chat.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.halloapp.DocumentPreviewLoader;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.content.Message;
import com.halloapp.util.FileUtils;
import com.halloapp.util.ViewDataLoader;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.util.Locale;

public class DocumentMessageViewHolder extends MessageViewHolder {

    private final TextView fileNameView;
    private final TextView fileDescriptionView;
    private final View previewContainer;
    private final ImageView previewImageView;

    private final ViewDataLoader.Displayer<ImageView, DocumentPreviewLoader.DocumentPreview> displayer;

    DocumentMessageViewHolder(@NonNull View itemView, @NonNull MessageViewHolderParent parent) {
        super(itemView, parent);

        fileNameView = itemView.findViewById(R.id.file_name);
        fileDescriptionView = itemView.findViewById(R.id.file_description);
        previewContainer = itemView.findViewById(R.id.preview_container);
        previewImageView = itemView.findViewById(R.id.file_preview);

        previewContainer.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int left = 0;
                int top = 0;
                int right = view.getWidth();
                int bottom = view.getHeight();
                float cornerRadius = itemView.getContext().getResources().getDimension(R.dimen.message_bubble_reply_corner_radius);
                outline.setRoundRect(left, top, right, bottom, cornerRadius);

            }
        });
        previewContainer.setOnClickListener(v -> {
            if (message == null || message.media == null || message.media.isEmpty()) {
                return;
            }
            Media firstMedia = message.media.get(0);
            Uri uri = FileProvider.getUriForFile(v.getContext(), "com.halloapp.fileprovider", firstMedia.file, message.text);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(message.text);
            if (extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            intent.setDataAndType(uri, type);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                v.getContext().startActivity(intent);
            } catch (Exception e){
                Toast.makeText(v.getContext(), R.string.no_app_to_open_file, Toast.LENGTH_SHORT).show();
                Log.e("DocumentMessageViewHolder/onClick no app found that can open file with extension " + extension, e);
            }
        });
        previewContainer.setClipToOutline(true);
        displayer = new ViewDataLoader.Displayer<ImageView, DocumentPreviewLoader.DocumentPreview>() {
            @Override
            public void showResult(@NonNull ImageView view, @Nullable DocumentPreviewLoader.DocumentPreview result) {
                if (result == null) {
                    previewImageView.setVisibility(View.GONE);
                    fileDescriptionView.setText("");
                } else {
                    if (result.thumbnail != null) {
                        previewImageView.setVisibility(View.VISIBLE);
                        previewImageView.setImageBitmap(result.thumbnail);
                    } else {
                        previewImageView.setVisibility(View.GONE);
                    }
                    StringBuilder sb = new StringBuilder();
                    final Context context = view.getContext();
                    if (result.numPages > 0) {
                        sb.append(context.getResources().getQuantityString(R.plurals.num_pages, result.numPages, result.numPages));
                        sb.append("\u00a0\u2022\u00a0");
                    }
                    sb.append(FileUtils.getReadableFileSizeShort(result.fileSize));

                    if (message != null) {
                        String extension = MimeTypeMap.getFileExtensionFromUrl(message.text);
                        if (!TextUtils.isEmpty(extension)) {
                            sb.append("\u00a0\u2022\u00a0");
                            sb.append(extension.toUpperCase(Locale.ROOT));
                        }
                    }
                    fileDescriptionView.setText(sb.toString());
                }
            }

            @Override
            public void showLoading(@NonNull ImageView view) {

            }
        };
    }

    @Override
    protected void fillView(@NonNull Message message, boolean changed) {
        fileNameView.setText(message.text);
        if (changed) {
            File docFile = null;
            String fileName = message.text;
            if (!message.media.isEmpty()) {
                docFile = message.media.get(0).file;
            }
            if (docFile == null) {
                parent.getDocumentPreviewLoader().cancel(previewImageView);
                previewImageView.setVisibility(View.GONE);
                fileDescriptionView.setText("");
            } else {
                String type = null;
                String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
                if (extension != null) {
                    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                }
                if (type != null) {
                    type = type.toLowerCase(Locale.ROOT);
                }
                if (type != null && type.contains("image")) {
                    parent.getDocumentPreviewLoader().loadImage(previewImageView, docFile, displayer);
                } else if (type != null && type.contains("pdf")) {
                    parent.getDocumentPreviewLoader().loadPdf(previewImageView, docFile, displayer);
                } else {
                    parent.getDocumentPreviewLoader().loadNoThumbnail(previewImageView, docFile, displayer);
                }
            }
        }
    }
}
