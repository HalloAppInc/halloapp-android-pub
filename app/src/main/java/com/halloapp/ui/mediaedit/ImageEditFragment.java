package com.halloapp.ui.mediaedit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.content.Media;

public class ImageEditFragment extends Fragment {

    private EditImageView editImageView;

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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.image_crop_menu, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }
}
