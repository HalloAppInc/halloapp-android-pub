package com.halloapp.katchup;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.R;
import com.halloapp.content.Comment;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Media;
import com.halloapp.content.Post;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.BgWorkers;

import java.util.List;
import java.util.Locale;

public class SettingsStorageFragment extends HalloFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings_storage, container, false);

        TextView mediaView = root.findViewById(R.id.media);

        StorageViewModel viewModel = new ViewModelProvider(this).get(StorageViewModel.class);
        viewModel.media.observe(getViewLifecycleOwner(), usage -> mediaView.setText(formatUsageSize(usage)));

        return root;
    }

    private static String formatUsageSize(long size) {
        long factor = 1000;
        if (size < factor) {
            return size + " B";
        } else if (size < factor * factor) {
            return String.format(Locale.US, "%.2f", ((float) size) / factor) + " KB";
        } else if (size < Math.pow(factor, 3)) {
            return String.format(Locale.US, "%.2f", ((float) size) / (factor * factor)) + " MB";
        } else {
            return String.format(Locale.US, "%.2f", ((float) size) / Math.pow(factor, 3)) + " GB";
        }
    }

    public static class StorageViewModel extends AndroidViewModel {

        public final MutableLiveData<Long> media = new MutableLiveData<>();

        public StorageViewModel(@NonNull Application application) {
            super(application);
            computeMediaUsage();
        }

        private void computeMediaUsage() {
            BgWorkers.getInstance().execute(() -> {
                long usage = 0;

                List<Post> posts = ContentDb.getInstance().getAllPosts();
                for (Post post : posts) {
                    for (Media media : post.media) {
                        if (media.file != null) {
                            usage += media.file.length();
                        }
                    }

                    List<Comment> comments = ContentDb.getInstance().getAllComments(post.id);
                    for (Comment comment : comments) {
                        for (Media media : comment.media) {
                            if (media.file != null) {
                                usage += media.file.length();
                            }
                        }
                    }
                }

                media.postValue(usage);
            });
        }
    }
}
