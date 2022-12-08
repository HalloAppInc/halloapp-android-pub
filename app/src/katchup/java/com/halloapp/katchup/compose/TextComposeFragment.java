package com.halloapp.katchup.compose;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.BitmapUtils;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TextComposeFragment extends ComposeFragment {

    private SelfieComposerViewModel viewModel;

    private View controlsContainer;
    private ImageView textColorPreview;
    private EditText editText;
    private View previewContainer;

    private int textColorIndex = 0;

    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    private File createdImage;

    private static final @ColorRes int[] textColors = new int[]{
            R.color.text_composer_color_1,
            R.color.text_composer_color_2,
            R.color.text_composer_color_3,
            R.color.text_composer_color_4,
            R.color.text_composer_color_5,
            R.color.text_composer_color_6,
            R.color.text_composer_color_7,
            R.color.text_composer_color_8,
            R.color.text_composer_color_9,
            R.color.text_composer_color_10,
            R.color.text_composer_color_11,};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_text_compose, container, false);

        controlsContainer = root.findViewById(R.id.controls_container);
        previewContainer = root.findViewById(R.id.preview_container);
        View doneButton = controlsContainer.findViewById(R.id.done_button);
        View textColorButton = controlsContainer.findViewById(R.id.text_color_button);
        textColorPreview = controlsContainer.findViewById(R.id.bg_color_preview);
        editText = root.findViewById(R.id.edit_text);
        editText.addTextChangedListener(new TextWatcher() {
            private CharSequence before;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Layout layout = editText.getLayout();
                if (layout != null && layout.getLineCount() > 6) {
                    int offset = layout.getOffsetForHorizontal(6, 0);
                    editText.setText(s.subSequence(0, offset));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        viewModel = new ViewModelProvider(requireActivity()).get(SelfieComposerViewModel.class);
        viewModel.getComposerState().observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case SelfieComposerViewModel.ComposeState.COMPOSING_CONTENT:
                    showCaptureView();
                    break;
                case SelfieComposerViewModel.ComposeState.COMPOSING_SELFIE:
                case SelfieComposerViewModel.ComposeState.READY_TO_SEND:
                    showPreviewView();
                    break;
            }
        });

        doneButton.setOnClickListener(v -> {
            // TODO: pass the actual text color
            viewModel.onComposedText(editText.toString(), Color.RED);
        });
        textColorButton.setOnClickListener(v -> {
            cycleTextColor();
        });

        updateTextColorPreview();
        return root;
    }

    private void cycleTextColor() {
        textColorIndex++;
        textColorIndex = textColorIndex % textColors.length;
        updateTextColorPreview();
    }

    private void updateTextColorPreview() {
        @ColorInt int color = ContextCompat.getColor(textColorPreview.getContext(), textColors[textColorIndex]);
        textColorPreview.setBackgroundColor(color);
        editText.setTextColor(color);
    }

    private void showPreviewView() {
        controlsContainer.setVisibility(View.GONE);
        editText.clearFocus();
        KeyboardUtils.hideSoftKeyboard(editText);
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setClickable(false);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        previewContainer.post(() -> {
            previewContainer.buildDrawingCache();
            final Bitmap b = Bitmap.createBitmap(previewContainer.getDrawingCache());
            if (createdImage == null) {
                createdImage = FileStore.getInstance().getTmpFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));
            }
            bgWorkers.execute(() -> {
                if (createdImage == null) {
                    return;
                }
                createdImage.delete();
                try (FileOutputStream out = new FileOutputStream(createdImage)) {
                    b.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, out);
                } catch (IOException e) {
                    Log.e("TextComposeFragment/savePreview failed", e);
                }
            });
        });
    }

    private void showCaptureView() {
        controlsContainer.setVisibility(View.VISIBLE);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setClickable(true);
        editText.requestFocus();
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        KeyboardUtils.showSoftKeyboard(editText);
    }

    @Override
    public Media getComposedMedia() {
        return Media.createFromFile(Media.MEDIA_TYPE_IMAGE, createdImage);
    }
}