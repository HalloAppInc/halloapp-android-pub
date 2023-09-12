package com.halloapp.ui;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.FileStore;
import com.halloapp.Preferences;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.media.ChunkedMediaParametersException;
import com.halloapp.media.Downloader;
import com.halloapp.media.ForeignRemoteAuthorityException;
import com.halloapp.proto.server.ExportData;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ExportDataResponseIq;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExportDataActivity extends HalloActivity {

    public static final int EXPORT_STATE_INITIAL = 0;
    public static final int EXPORT_STATE_PENDING = 1;
    public static final int EXPORT_STATE_READY = 2;

    private ExportDataViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_export_data);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewModel = new ViewModelProvider(this).get(ExportDataViewModel.class);

        viewModel.exportDataState.getLiveData().observe(this, state -> {
            TextView requestData = findViewById(R.id.request_data);
            TextView dataPending = findViewById(R.id.data_pending);
            TextView exportData = findViewById(R.id.export_data);

            if (state.initialState) {
                dataPending.setVisibility(View.GONE);
                exportData.setVisibility(View.GONE);
                requestData.setVisibility(View.VISIBLE);
                requestData.setOnClickListener(v -> getRequestStatus());
            } else if (state.requestFailed) {
                SnackbarHelper.showWarning(this, R.string.request_export_data_failed);
            } else if (state.status == ExportData.Status.PENDING) {
                requestData.setVisibility(View.GONE);
                exportData.setVisibility(View.GONE);
                dataPending.setVisibility(View.VISIBLE);
                dataPending.setText(getString(R.string.data_available_at, SimpleDateFormat.getDateInstance().format(new Date(state.dataReadyTs * 1000L))));
            } else if (state.status == ExportData.Status.READY) {
                dataPending.setVisibility(View.GONE);
                requestData.setVisibility(View.GONE);
                exportData.setVisibility(View.VISIBLE);
                exportData.setOnClickListener(v -> exportData(state));
            } else {
                Log.w("Unexpected state " + state);
            }
        });

        viewModel.exportDataIntent.observe(this, this::startActivity);
    }

    private void getRequestStatus() {
        viewModel.startRequest();
    }

    private void exportData(ExportDataState state) {
        viewModel.exportData(state.url);
    }

    private static class ExportDataState {
        final boolean requestFailed;
        final boolean initialState;
        final ExportData.Status status;
        final Long dataReadyTs;
        final String url;

        private ExportDataState(boolean requestFailed, boolean initialState, ExportData.Status status, Long dataReadyTs, String url) {
            this.requestFailed = requestFailed;
            this.initialState = initialState;
            this.status = status;
            this.dataReadyTs = dataReadyTs;
            this.url = url;
        }

        public static ExportDataState requestFailed() {
            return new ExportDataState(true, false, null, null, null);
        }

        public static ExportDataState initialState() {
            return new ExportDataState(false, true, null, null, null);
        }

        public static ExportDataState pending(long dataReadyTs) {
            return new ExportDataState(false, false, ExportData.Status.PENDING, dataReadyTs, null);
        }

        public static ExportDataState ready(long dataReadyTs, String url) {
            return new ExportDataState(false, false, ExportData.Status.READY, dataReadyTs, url);
        }
    }

    public static class ExportDataViewModel extends AndroidViewModel {

        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final FileStore fileStore = FileStore.getInstance();
        private final Connection connection = Connection.getInstance();
        private final Preferences preferences = Preferences.getInstance();

        public final ComputableLiveData<ExportDataState> exportDataState;
        public final MutableLiveData<Boolean> exportDataFailed = new MutableLiveData<>();
        public final MutableLiveData<Intent> exportDataIntent = new MutableLiveData<>();

        public ExportDataViewModel(@NonNull Application application) {
            super(application);

            exportDataState = new ComputableLiveData<ExportDataState>() {
                @SuppressLint("RestrictedApi")
                @Override
                protected ExportDataState compute() {
                    int localState = preferences.getExportDataState();
                    if (localState == EXPORT_STATE_INITIAL) {
                        return ExportDataState.initialState();
                    }

                    try {
                        ExportDataResponseIq responseIq = connection.requestAccountData().await();
                        if (responseIq.status == ExportData.Status.PENDING) {
                            preferences.setExportDataState(EXPORT_STATE_PENDING);
                            return ExportDataState.pending(responseIq.dataReadyTs);
                        } else if (responseIq.status == ExportData.Status.READY) {
                            preferences.setExportDataState(EXPORT_STATE_READY);
                            return ExportDataState.ready(responseIq.dataReadyTs, responseIq.dataUrl);
                        }
                    } catch (ObservableErrorException e) {
                        Log.e("ExportDataActivity data fetch observable error", e);
                    } catch (InterruptedException e) {
                        Log.e("ExportDataActivity data fetch interrupted", e);
                    }

                    return ExportDataState.requestFailed();
                }
            };
        }

        void startRequest() {
            bgWorkers.execute(() -> {
                preferences.setExportDataState(EXPORT_STATE_PENDING);
                exportDataState.invalidate();
            });
        }

        void exportData(String url) {
            bgWorkers.execute(() -> {
                File file = fileStore.getExportDataFile();
                if (!file.exists()) {
                    try {
                        Downloader.run(url, null, null, Media.MEDIA_TYPE_UNKNOWN, null, file, new Downloader.DownloadListener() {
                            @Override
                            public boolean onProgress(long bytesWritten) {
                                return true;
                            }
                        }, "export-data");
                    } catch (IOException | GeneralSecurityException | ChunkedMediaParametersException | ForeignRemoteAuthorityException e) {
                        Log.w("Failed to save export data", e);
                    }
                }

                Uri uri = FileProvider.getUriForFile(getApplication(), "com.halloapp.fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                exportDataIntent.postValue(intent);
            });
        }
    }
}
