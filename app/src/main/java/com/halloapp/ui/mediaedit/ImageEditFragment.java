package com.halloapp.ui.mediaedit;

import android.graphics.Outline;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.BuildConfig;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.props.ServerProps;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.logs.Log;

public class ImageEditFragment extends Fragment {

    private EditImageView editImageView;
    private ColorPickerView colorPickerView;
    private EditText annotationText;
    private FrameLayout annotationContainer;
    private boolean isAnnotating = false;
    private EditImageView.Annotation currentAnnotation;
    private android.view.ActionMode actionMode;

    public ImageEditFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MediaEditViewModel viewModel = new ViewModelProvider(requireActivity()).get(MediaEditViewModel.class);
        MediaEditViewModel.Model selected = viewModel.getSelected().getValue();

        if (selected == null || selected.getType() != Media.MEDIA_TYPE_IMAGE) {
            return;
        }

        colorPickerView = view.findViewById(R.id.color_picker);
        colorPickerView.setColorUpdateListener(color -> {
            editImageView.setDrawingColor(color);
            annotationText.setTextColor(color);
            requireActivity().invalidateOptionsMenu();
        });

        editImageView = view.findViewById(R.id.image);
        editImageView.setStateUpdateListener(state -> {
            if (!state.equals(selected.getState())) {
                viewModel.update(selected, state);
            }
        });
        editImageView.setAnnotationListener(new EditImageView.AnnotationListener() {
            @Override
            public void onTap(@NonNull EditImageView.Annotation annotation) {
                ImageEditFragment.this.currentAnnotation = annotation;

                colorPickerView.setColor(annotation.getColor());
                editImageView.setDrawingColor(annotation.getColor());
                annotationText.setTextColor(annotation.getColor());

                toggleAnnotating(annotation.getText());
            }

            @Override
            public void onDrag(@NonNull EditImageView.Annotation annotation, float x, float y) {
                if (actionMode == null) {
                    actionMode = requireActivity().startActionMode(new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            return true;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            actionMode = null;
                        }
                    });
                }

                int[] location = {0, 0};
                editImageView.getLocationOnScreen(location);
                setAnnotationDeleteButton(isOverAnnotationDeleteButton((float)location[0] + x, (float)location[1] +  + y));
            }

            @Override
            public boolean onDragEnd(@NonNull EditImageView.Annotation annotation, float x, float y) {
                if (actionMode != null) {
                    int[] location = {0, 0};
                    editImageView.getLocationOnScreen(location);
                    boolean deleteAnnotation = isOverAnnotationDeleteButton((float)location[0] + x, (float)location[1] +  + y);

                    actionMode.finish();
                    actionMode = null;

                    return deleteAnnotation;
                }

                return false;
            }
        });
        editImageView.setAsyncImageFile(selected.original.file, (EditImageView.State) selected.getState(), null);

        annotationText = view.findViewById(R.id.annotation);
        annotationContainer = view.findViewById(R.id.annotationContainer);
        annotationContainer.setOnClickListener(v -> {
            String text = annotationText.getText().toString().trim();

            if (!text.isEmpty() && currentAnnotation == null) {
                editImageView.addAnnotation(text);
            } else if (!text.isEmpty()) {
                editImageView.updateAnnotation(currentAnnotation, text);
            } else if (currentAnnotation != null) {
                editImageView.removeAnnotation(currentAnnotation);
            }

            currentAnnotation = null;
            isAnnotating = false;
            requireActivity().invalidateOptionsMenu();
            updateUI();
        });

        editImageView.setDrawingColor(colorPickerView.getColor());
        annotationText.setTextColor(colorPickerView.getColor());

        viewModel.getMedia().observe(getViewLifecycleOwner(), models -> {
            if (!editImageView.getState().equals(selected.getState())) {
                editImageView.setState((EditImageView.State) selected.getState());
            }
        });
    }

    private void setAnnotationDeleteButton(boolean highlighted) {
        View view = requireActivity().findViewById(R.id.action_mode_close_button);

        if (view instanceof AppCompatImageView) {
            AppCompatImageView closeImageView = (AppCompatImageView) view;
            closeImageView.setImageResource(R.drawable.ic_delete);
            closeImageView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    int size = Math.min(view.getWidth(), view.getHeight());
                    int left = (view.getWidth() - size) / 2;
                    int top = (view.getHeight() - size) / 2;

                    outline.setRoundRect(left, top, left + size,  top + size, (float)size / 2);
                }
            });
            closeImageView.setClipToOutline(true);

            if (highlighted) {
                closeImageView.setColorFilter(getResources().getColor(R.color.white));
                closeImageView.setBackgroundColor(getResources().getColor(R.color.color_primary));
            } else {
                closeImageView.setColorFilter(getResources().getColor(R.color.color_primary));
                closeImageView.setBackgroundColor(0);
            }
        }
    }

    private boolean isOverAnnotationDeleteButton(float x, float y) {
        View view = requireActivity().findViewById(R.id.action_mode_close_button);
        if (view != null) {
            int[] location = {0, 0};
            view.getLocationOnScreen(location);

            return location[0] <= x && x <= location[1] + view.getWidth() && location[1] <= y && y <= location[1] + view.getHeight();
        }

        return false;
    }

    private void toggleDrawing() {
        editImageView.setDrawing(!editImageView.isDrawing());
        updateUI();
    }

    private void toggleAnnotating() {
        toggleAnnotating("");
    }

    private void toggleAnnotating(String text) {
        annotationText.setText(text);
        isAnnotating = !isAnnotating;
        updateUI();
    }

    private void updateUI() {
        if (isAnnotating) {
            annotationText.setSelection(annotationText.getText().length());
            KeyboardUtils.showSoftKeyboard(annotationText);
        } else if (annotationText.hasFocus()) {
            annotationText.clearFocus();
            KeyboardUtils.hideSoftKeyboard(annotationText);
        }

        colorPickerView.setVisibility(isAnnotating || editImageView.isDrawing() ? View.VISIBLE : View.GONE);
        annotationContainer.setVisibility(isAnnotating ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.image_crop_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem drawItem = menu.findItem(R.id.draw);
        View drawView = drawItem.getActionView();
        drawView.setOnClickListener(v -> onOptionsItemSelected(drawItem));

        if (editImageView.isDrawing()) {
            float radius = getResources().getDimension(R.dimen.media_edit_menu_draw_background) / 2;

            drawView.setBackgroundColor(colorPickerView.getColor());
            drawView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
            drawView.setClipToOutline(true);
        }

        MenuItem annotateItem = menu.findItem(R.id.annotate);
        View annotateView = annotateItem.getActionView();
        annotateView.setOnClickListener(v -> onOptionsItemSelected(annotateItem));

        if (isAnnotating) {
            float radius = getResources().getDimension(R.dimen.media_edit_menu_draw_background) / 2;

            annotateView.setBackgroundColor(colorPickerView.getColor());
            annotateView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
            annotateView.setClipToOutline(true);
        }

        if (BuildConfig.DEBUG || ServerProps.getInstance().getIsInternalUser() || ServerProps.getInstance().getMediaDrawingEnabled()) {
            drawItem.setVisible(true);
            annotateItem.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.rotate) {
            editImageView.rotate();
            return true;
        } else if (id == R.id.flip) {
            editImageView.flip();
            return true;
        } else if (id == R.id.draw) {
            isAnnotating = false;
            toggleDrawing();
            requireActivity().invalidateOptionsMenu();
            return true;
        } else if (id == R.id.annotate) {
            editImageView.setDrawing(false);
            toggleAnnotating();
            requireActivity().invalidateOptionsMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void undo() {
        editImageView.undo();
    }
}
