package com.halloapp.ui.mediaedit;

import android.graphics.Outline;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.BuildConfig;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.props.ServerProps;

public class ImageEditFragment extends Fragment {

    private EditImageView editImageView;
    private ColorPickerView colorPickerView;

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
            requireActivity().invalidateOptionsMenu();
        });

        editImageView = view.findViewById(R.id.image);
        editImageView.setStateUpdateListener(state -> {
            if (!state.equals(selected.getState())) {
                viewModel.update(selected, state);
            }
        });
        editImageView.setAsyncImageFile(selected.original.file, (EditImageView.State) selected.getState(), null);

        viewModel.getMedia().observe(getViewLifecycleOwner(), models -> {
            if (!editImageView.getState().equals(selected.getState())) {
                editImageView.setState((EditImageView.State) selected.getState());
            }
        });
    }

    private void toggleDrawing() {
        if (editImageView.isDrawing()) {
            editImageView.setDrawing(false);
            colorPickerView.setVisibility(View.GONE);
        } else {
            editImageView.setDrawing(true);
            colorPickerView.setVisibility(View.VISIBLE);
        }
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

        if (BuildConfig.DEBUG || ServerProps.getInstance().getIsInternalUser() || ServerProps.getInstance().getMediaDrawingEnabled()) {
            drawItem.setVisible(true);
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
            toggleDrawing();
            requireActivity().invalidateOptionsMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean canUndo() {
        return editImageView.canUndo();
    }

    public void undo() {
        editImageView.undo();
    }
}
