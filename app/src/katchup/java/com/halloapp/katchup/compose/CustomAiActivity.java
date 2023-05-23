package com.halloapp.katchup.compose;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.ConnectionObservers;
import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.katchup.Analytics;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ThreadUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

public class CustomAiActivity extends HalloActivity {

    public static final String EXTRA_RESULT_FILE = "result_file";

    public static Intent open(@NonNull Context context) {
        return new Intent(context, CustomAiActivity.class);
    }

    private static final int HINT_KEYSTROKE_DELAY_MS = 100;
    private static final int IMAGINE_HINT_DELAY_MS = 3000;
    private static final int EMPTY_HINT_DELAY_MS = 500;
    private static final int SAMPLE_PROMPT_DELAY_MS = 1500;

    private static final String[] SAMPLE_PROMPTS = new String[] {
            "painting of a dancing cat",
            "blue donuts flying",
            "phone melting like dali painting"
    };

    private CustomAiViewModel viewModel;

    private ImageView generateButton;
    private EditText editText;
    private TextView hintText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custom_ai);

        viewModel = new ViewModelProvider(this, new CustomAiViewModel.Factory(getApplication())).get(CustomAiViewModel.class);

        View prev = findViewById(R.id.prev);
        prev.setOnClickListener(v -> {
            finish();
        });

        View done = findViewById(R.id.done);
        done.setOnClickListener(v -> {
            Bitmap bitmap = Preconditions.checkNotNull(viewModel.generatedImage.getValue());
            try {
                File file = FileStore.getInstance().getTmpFile("custom_ai");
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RESULT_FILE, file.getAbsolutePath());
                setResult(RESULT_OK, intent);
                finish();
            } catch (FileNotFoundException e) {
                Log.e("Failed to save ai bitmap to disk", e);
            }
        });

        ImageView image = findViewById(R.id.image);
        generateButton = findViewById(R.id.generate);
        editText = findViewById(R.id.edit_text);
        hintText = findViewById(R.id.hint);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateUi();
            }
        });
        generateButton.setOnClickListener(v -> {
            String text = editText.getEditableText().toString();
            if (TextUtils.isEmpty(text)) {
                return;
            }
            viewModel.generateAiImage(text, true);
        });

        viewModel.generatedImage.observe(this, bitmap -> {
            done.setVisibility(bitmap != null ? View.VISIBLE : View.GONE);
            image.setImageBitmap(bitmap);
        });
        viewModel.getGenerationFailed().observe(this, failed -> {
            if (Boolean.TRUE.equals(failed)) {
                SnackbarHelper.showWarning(this, R.string.ai_generation_failed);
            }
        });
        viewModel.generationRequestInFlight.observe(this, inFlight -> {
            updateUi();
        });

        editText.requestFocus();

        startAnimation();
    }

    private void startAnimation() {
        String customAiHint = getString(R.string.custom_ai_hint);
        pause(IMAGINE_HINT_DELAY_MS, () -> deleteText(customAiHint, () -> animate(SAMPLE_PROMPTS[0], () -> animate(SAMPLE_PROMPTS[1], () -> animate(SAMPLE_PROMPTS[2], () -> typeText(customAiHint, this::startAnimation))))));
    }

    private void animate(@NonNull String s, @NonNull Runnable completionRunnable) {
        typeText(s, () -> pause(SAMPLE_PROMPT_DELAY_MS, () -> deleteText(s, () -> pause(EMPTY_HINT_DELAY_MS, completionRunnable))));
    }

    private void pause(long delayMs, @NonNull Runnable completionRunnable) {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                completionRunnable.run();
            }
        };
        timer.schedule(timerTask, delayMs);
    }

    private void typeText(@NonNull String s, @NonNull Runnable completionRunnable) {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int len = 0;
            @Override
            public void run() {
                String toShow = s.substring(0, len);
                hintText.post(() -> hintText.setText(toShow));

                if (len == s.length()) {
                    timer.cancel();
                    completionRunnable.run();
                }
                len++;
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, HINT_KEYSTROKE_DELAY_MS);
    }

    private void deleteText(@NonNull String s, @NonNull Runnable completionRunnable) {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int len = s.length();
            @Override
            public void run() {
                String toShow = s.substring(0, len);
                hintText.post(() -> hintText.setText(toShow));

                if (len == 0) {
                    timer.cancel();
                    completionRunnable.run();
                }
                len--;
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, HINT_KEYSTROKE_DELAY_MS);
    }

    private void updateUi() {
        boolean empty = TextUtils.isEmpty(editText.getEditableText().toString());
        hintText.setVisibility(empty ? View.VISIBLE : View.GONE);
        boolean enabled = !empty && Boolean.FALSE.equals(viewModel.generationRequestInFlight.getValue());
        generateButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(enabled ? R.color.color_primary_light : R.color.white_20)));
    }

    public static class CustomAiViewModel extends AndroidViewModel {
        private final Connection.Observer connectionObserver = new Connection.Observer() {
            @Override
            public void onAiImageReceived(@NonNull String id, @Nullable byte[] bytes, @NonNull String ackId) {
                if (id.equals(pendingAiImageId)) {
                    if (bytes == null || bytes.length == 0) {
                        generationError.postValue(true);
                    } else {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        generatedImage.postValue(bitmap);
                        generationError.postValue(false);
                    }
                    generationRequestInFlight.postValue(false);
                }
            }
        };


        private final MutableLiveData<Boolean> generationRequestInFlight = new MutableLiveData<>(false);
        private final MutableLiveData<Bitmap> generatedImage = new MutableLiveData<>();
        private final MutableLiveData<Boolean> generationError = new MutableLiveData<>(false);

        private String pendingAiImageId;

        public CustomAiViewModel(@NonNull Application application) {
            super(application);

            ConnectionObservers.getInstance().addObserver(connectionObserver);
        }

        public void generateAiImage(@NonNull String text, boolean custom) {
            pendingAiImageId = null;
            generationError.postValue(false);
            generationRequestInFlight.postValue(true);
            generatedImage.postValue(null);
            Analytics.getInstance().generatedAiImage("user");
            Connection.getInstance().sendAiImageRequest(text, 1, custom).onResponse(res -> {
                if (res.success) {
                    pendingAiImageId = res.id;
                } else {
                    Log.w("CustomAiActivity AI image request failed");
                    generationError.postValue(true);
                    generationRequestInFlight.postValue(false);
                }
            }).onError(err -> {
                Log.w("CustomAiActivity AI image request failed", err);
                generationError.postValue(true);
                generationRequestInFlight.postValue(false);
            });
        }

        public LiveData<Boolean> getGenerationFailed() {
            return generationError;
        }

        @Override
        protected void onCleared() {
            ConnectionObservers.getInstance().removeObserver(connectionObserver);
        }

        public static class Factory implements ViewModelProvider.Factory {
            private final Application application;

            Factory(@NonNull Application application) {
                this.application = application;
            }

            @Override
            public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(CustomAiViewModel.class)) {
                    //noinspection unchecked
                    return (T) new CustomAiViewModel(application);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }
}
