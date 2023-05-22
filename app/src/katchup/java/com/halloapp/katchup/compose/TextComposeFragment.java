package com.halloapp.katchup.compose;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.Constants;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.emoji.EmojiKeyboardLayout;
import com.halloapp.katchup.Analytics;
import com.halloapp.props.ServerProps;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.RandomId;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;


public class TextComposeFragment extends ComposeFragment {

    public static TextComposeFragment newInstance(String prompt) {
        Bundle args = new Bundle();
        args.putString(EXTRA_PROMPT, prompt);

        TextComposeFragment fragment = new TextComposeFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static final int REQUEST_CODE_CUSTOM_AI = 5;
    public static final int REQUEST_CODE_PICK_IMAGE = 6;

    private static final String EXTRA_PROMPT = "prompt";

    private static final int CHAR_LIMIT = 60;

    private static final int TOP_MARGIN_PREVIEW = 15;
    private static final int TOP_MARGIN_SEND = 150;

    private SelfieComposerViewModel viewModel;

    private View controlsContainer;
    private ImageView textColorPreview;
    private View textColorButton;
    private View editButton;
    private View textContent;
    private TextView promptView;
    private EditText editText;
    private View previewContainer;
    private TextView remainingCharsText;

    private BgOptionsPopupWindow bgOptionsPopupWindow;

    private int textColorIndex = 0;

    private ImageView emojiButton;

    private final BgWorkers bgWorkers = BgWorkers.getInstance();

    private File createdImage;

    private EmojiKeyboardLayout emojiKeyboardLayout;

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

        Bundle args = getArguments();

        controlsContainer = root.findViewById(R.id.controls_container);
        textColorButton = root.findViewById(R.id.text_color_button);
        editButton = root.findViewById(R.id.edit_button);
        previewContainer = root.findViewById(R.id.preview_container);
        previewContainer.setClipToOutline(true);
        previewContainer.setOutlineProvider(new ViewOutlineProvider() {
            private final float radius = getResources().getDimension(R.dimen.post_card_radius);
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
            }
        });
        emojiButton = root.findViewById(R.id.emoji_button);
        emojiKeyboardLayout = root.findViewById(R.id.emoji_keyboard);
        View doneButton = controlsContainer.findViewById(R.id.done_button);
        remainingCharsText = controlsContainer.findViewById(R.id.remaining_chars_text);
        formatRemainingCharsText(0);
        textColorPreview = root.findViewById(R.id.bg_color_preview);
        textContent = root.findViewById(R.id.text_content);
        promptView = root.findViewById(R.id.prompt);
        editText = root.findViewById(R.id.edit_text);
        InputFilter[] editFilters = editText.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 1];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.AllCaps();
        editText.setFilters(newFilters);
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
                    int offset = layout.getOffsetForHorizontal(6, 0) - 1;
                    editText.setText(s.subSequence(0, offset));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    doneButton.setVisibility(View.INVISIBLE);
                } else {
                    doneButton.setVisibility(View.VISIBLE);
                }
                formatRemainingCharsText(s.toString().length());
            }
        });

        if (args != null) {
            String prompt = args.getString(EXTRA_PROMPT, null);
            if (!TextUtils.isEmpty(prompt)) {
                promptView.setText(prompt);
            }
        }

        viewModel = new ViewModelProvider(requireActivity()).get(SelfieComposerViewModel.class);
        viewModel.getComposerState().observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case SelfieComposerViewModel.ComposeState.COMPOSING_CONTENT:
                    showCaptureView();
                    break;
                case SelfieComposerViewModel.ComposeState.COMPOSING_SELFIE:
                    showPreviewView();
                    moveTextAbove();
                    viewModel.requestInitialAiImage(editText.getEditableText().toString());
                    updateTextColorPreview();
                    break;
                // This separate TRANSITIONING state is needed so that we save the view contents AFTER it has been moved below the selfie
                case SelfieComposerViewModel.ComposeState.TRANSITIONING:
                case SelfieComposerViewModel.ComposeState.READY_TO_SEND:
                    moveTextBelow();
                    showPreviewView();
                    break;
            }
        });

        ImageView backgroundImage = root.findViewById(R.id.background_image);

        viewModel.getGeneratedImages().observe(getViewLifecycleOwner(), bitmaps -> {
            Bitmap bitmap = bitmaps == null || bitmaps.isEmpty() ? null : bitmaps.get(bitmaps.size() - 1);
            backgroundImage.setImageBitmap(bitmap);
            updateCreatedImage();
        });
        viewModel.getGenerationFailed().observe(getViewLifecycleOwner(), failed -> {
            if (Boolean.TRUE.equals(failed)) {
                SnackbarHelper.showWarning(requireActivity(), R.string.ai_generation_failed);
            }
        });

        doneButton.setOnClickListener(v -> {
            viewModel.onComposedText(editText.getEditableText().toString(), Color.RED);
        });

        textColorButton.setOnClickListener(v -> {
            cycleTextColor();
        });
        editButton.setOnClickListener(v -> {
            openBgOptionsMenu();
        });

        emojiKeyboardLayout.setIcons(R.drawable.ic_emoji_kb_text_compose, R.drawable.ic_emoji_smile);
        emojiKeyboardLayout.bind(emojiButton, editText);
        return root;
    }

    private void openBgOptionsMenu() {
        if (bgOptionsPopupWindow != null) {
            bgOptionsPopupWindow.dismiss();
        }
        bgOptionsPopupWindow = new BgOptionsPopupWindow(requireContext());
        bgOptionsPopupWindow.show(editButton);
    }

    private void formatRemainingCharsText(int used) {
        remainingCharsText.setText(String.format(Locale.getDefault(), "%d", Math.max(0, CHAR_LIMIT - used)));
    }

    private void moveTextAbove() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) textContent.getLayoutParams();
        layoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TOP_MARGIN_PREVIEW, getResources().getDisplayMetrics());
        textContent.setLayoutParams(layoutParams);
    }

    private void moveTextBelow() {
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) textContent.getLayoutParams();
        layoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TOP_MARGIN_SEND, getResources().getDisplayMetrics());
        textContent.setLayoutParams(layoutParams);
    }

    private void cycleTextColor() {
        textColorIndex++;
        textColorIndex = textColorIndex % textColors.length;
        updateTextColorPreview();
    }

    private void updateTextColorPreview() {
        @ColorInt int color = ContextCompat.getColor(requireContext(), textColors[textColorIndex]);
        textColorPreview.setBackgroundColor(color);
        editText.setTextColor(color);
        updateCreatedImage();
    }

    private void showPreviewView() {
        textColorButton.setVisibility(View.VISIBLE);
        editButton.setVisibility(ServerProps.getInstance().getAiImageGenerationEnabled() ? View.VISIBLE : View.GONE);
        controlsContainer.setVisibility(View.GONE);
        promptView.setAlpha(0.8f);
        promptView.setShadowLayer(4, 0, 4, getResources().getColor(R.color.black_30));
        editText.clearFocus();
        KeyboardUtils.hideSoftKeyboard(editText);
        emojiKeyboardLayout.hideEmojiKeyboard();
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setClickable(false);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        updateCreatedImage();
    }

    private void updateCreatedImage() {
        previewContainer.post(() -> {
            previewContainer.setDrawingCacheEnabled(true);
            final Bitmap b = Bitmap.createBitmap(previewContainer.getDrawingCache());
            previewContainer.setDrawingCacheEnabled(false);
            if (createdImage == null) {
                createdImage = FileStore.getInstance().getTmpFile(RandomId.create() + "." + Media.getFileExt(Media.MEDIA_TYPE_IMAGE));
            }
            bgWorkers.execute(() -> {
                if (createdImage == null) {
                    return;
                }
                if (!createdImage.delete()) {
                    Log.e("TextComposeFragment/savePreview failed to delete file");
                }
                try (FileOutputStream out = new FileOutputStream(createdImage)) {
                    if (!b.compress(Bitmap.CompressFormat.JPEG, Constants.JPEG_QUALITY, out)) {
                        Log.e("TextComposeFragment/savePreview failed to compress");
                    }
                } catch (IOException e) {
                    Log.e("TextComposeFragment/savePreview failed", e);
                }
            });
        });
    }

    private void showCaptureView() {
        textColorButton.setVisibility(View.GONE);
        editButton.setVisibility(View.GONE);
        controlsContainer.setVisibility(View.VISIBLE);
        promptView.setAlpha(1f);
        promptView.setShadowLayer(0, 0, 0, Color.BLACK);
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

    @Override
    public View getPreview() {
        return previewContainer;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CUSTOM_AI && resultCode == Activity.RESULT_OK && data != null) {
            String path = data.getStringExtra(CustomAiActivity.EXTRA_RESULT_FILE);
            try {
                FileInputStream fis = new FileInputStream(path);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                Analytics.getInstance().changedPostBackground("user_ai");
                viewModel.handleGeneratedImage(bitmap);
            } catch (FileNotFoundException e) {
                Log.e("Failed to read ai image from disk", e);
            }
        } else if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            String path = data.getStringExtra(BackgroundImagePicker.EXTRA_RESULT_FILE);
            try {
                FileInputStream fis = new FileInputStream(path);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                Analytics.getInstance().changedPostBackground("custom_image");
                viewModel.handleGeneratedImage(bitmap);
            } catch (FileNotFoundException e) {
                Log.e("Failed to read selected image from disk", e);
            }
        }
    }

    public class BgOptionsPopupWindow extends PopupWindow {
        public BgOptionsPopupWindow(@NonNull Context context) {
            super(context);

            final View root = LayoutInflater.from(context).inflate(R.layout.bg_options_popup_window, null, false);

            final View remove = root.findViewById(R.id.remove);
            remove.setOnClickListener(v -> {
                Analytics.getInstance().changedPostBackground("none");
                viewModel.clearGeneratedImage();
                dismiss();
            });
            final View random = root.findViewById(R.id.random);
            random.setOnClickListener(v -> {
                Analytics.getInstance().changedPostBackground("server_ai");
                viewModel.generateAiImage(editText.getEditableText().toString(), false);
                dismiss();
            });
            final View custom = root.findViewById(R.id.custom);
            custom.setOnClickListener(v -> {
                startActivityForResult(CustomAiActivity.open(requireContext()), REQUEST_CODE_CUSTOM_AI);
                dismiss();
            });
            final View gallery = root.findViewById(R.id.gallery);
            gallery.setOnClickListener(v -> {
                startActivityForResult(BackgroundImagePicker.open(requireContext()), REQUEST_CODE_PICK_IMAGE);
                dismiss();
            });

            setContentView(root);

            setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            setOutsideTouchable(true);
            setFocusable(false);
        }

        public void show(@NonNull View anchor) {
            View contentView = getContentView();
            contentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            showAsDropDown(anchor, (contentView.getPaddingRight() + contentView.getPaddingLeft() + anchor.getWidth() - contentView.getMeasuredWidth()) / 2, -contentView.getMeasuredHeight() - anchor.getHeight() - 2);
        }
    }
}
