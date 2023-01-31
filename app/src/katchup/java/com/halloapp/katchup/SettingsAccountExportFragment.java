package com.halloapp.katchup;

import android.app.Application;
import android.content.Intent;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.halloapp.FileStore;
import com.halloapp.R;
import com.halloapp.content.Media;
import com.halloapp.media.ChunkedMediaParametersException;
import com.halloapp.media.Downloader;
import com.halloapp.media.ForeignRemoteAuthorityException;
import com.halloapp.proto.server.ExportData;
import com.halloapp.ui.HalloFragment;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.logs.Log;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.ExportDataResponseIq;
import com.halloapp.xmpp.util.ObservableErrorException;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsAccountExportFragment extends HalloFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_account_export, container, false);

        AccountExportViewModel viewModel = new ViewModelProvider(this).get(AccountExportViewModel.class);

        TextView stateView = root.findViewById(R.id.state);

        TextView exportBtn = root.findViewById(R.id.export);
        exportBtn.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), view.getHeight());
            }
        });
        exportBtn.setClipToOutline(true);

        exportBtn.setOnClickListener(v -> {
            State state = viewModel.state.getValue();
            if (state != null && state.success && state.status == ExportData.Status.NOT_STARTED) {
                viewModel.requestExport();
            } else if (state != null && state.success && state.status == ExportData.Status.READY) {
                viewModel.download().observe(getViewLifecycleOwner(), intent -> {
                    if (intent != null) {
                        startActivity(intent);
                    }
                });
            }
        });

        viewModel.state.observe(getViewLifecycleOwner(), state -> {
            if (state.success && state.status == ExportData.Status.NOT_STARTED) {
                exportBtn.setVisibility(View.VISIBLE);
                stateView.setVisibility(View.GONE);
                exportBtn.setText(R.string.account_export);
            } else if (state.success && state.status == ExportData.Status.PENDING && state.dataReadyTs != null) {
                exportBtn.setVisibility(View.GONE);
                stateView.setVisibility(View.VISIBLE);
                stateView.setText(getString(R.string.data_available_at, SimpleDateFormat.getDateInstance().format(new Date(state.dataReadyTs * 1000L))));
            } else if (state.success && state.status == ExportData.Status.READY) {
                exportBtn.setVisibility(View.VISIBLE);
                stateView.setVisibility(View.GONE);
                exportBtn.setText(R.string.account_export_download);
            } else {
                exportBtn.setVisibility(View.GONE);
                stateView.setVisibility(View.VISIBLE);
                stateView.setText(getString(R.string.error_unknown));
            }
        });

        return root;
    }

    private static class State {
        final boolean success;
        final ExportData.Status status;
        final Long dataReadyTs;
        final String url;

        private State(boolean success, @Nullable ExportData.Status status, @Nullable Long dataReadyTs, @Nullable String url) {
            this.success = success;
            this.status = status;
            this.dataReadyTs = dataReadyTs;
            this.url = url;
        }

        public static State success(ExportDataResponseIq responseIq) {
            return new State(true, responseIq.status, responseIq.dataReadyTs, responseIq.dataUrl);
        }

        public static State fail() {
            return new State(false, null, null, null);
        }
    }

    public static class AccountExportViewModel extends AndroidViewModel {
        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final FileStore fileStore = FileStore.getInstance();
        private final Connection connection = Connection.getInstance();

        public final MutableLiveData<State> state = new MutableLiveData<>();

        private boolean processing = false;

        public AccountExportViewModel(@NonNull Application application) {
            super(application);
            fetchExportState();
        }

        private void fetchExportState() {
            if (processing) {
                return;
            }
            processing = true;

            bgWorkers.execute(() -> {
                try {
                    ExportDataResponseIq responseIq = connection.getAccountDataRequestState().await();
                    state.postValue(State.success(responseIq));
                } catch (ObservableErrorException e) {
                    Log.e("AccountExportsViewModel.fetchExportState observable error", e);
                    state.postValue(State.fail());
                } catch (InterruptedException e) {
                    Log.e("AccountExportsViewModel.fetchExportState interrupted", e);
                    state.postValue(State.fail());
                }

                processing = false;
            });
        }

        public void requestExport() {
            if (processing) {
                return;
            }
            processing = true;

            bgWorkers.execute(() -> {
                try {
                    ExportDataResponseIq responseIq = connection.requestAccountData().await();
                    state.postValue(State.success(responseIq));
                } catch (ObservableErrorException e) {
                    Log.e("AccountExportsViewModel.requestExport observable error", e);
                    state.postValue(State.fail());
                } catch (InterruptedException e) {
                    Log.e("AccountExportsViewModel.requestExport interrupted", e);
                    state.postValue(State.fail());
                }

                processing = false;
            });
        }

        public LiveData<Intent> download() {
            MutableLiveData<Intent> result = new MutableLiveData<>();

            State state = this.state.getValue();
            if (state == null || state.url == null || state.status != ExportData.Status.READY) {
                return result;
            }

            if (processing) {
                return result;
            }
            processing = true;

            bgWorkers.execute(() -> {
                File file = fileStore.getExportDataFile();

                if (!file.exists()) {
                    try {
                        Downloader.run(state.url, null, null, Media.MEDIA_TYPE_UNKNOWN, null, file, new Downloader.DownloadListener() {
                            @Override
                            public boolean onProgress(long bytesWritten) {
                                return true;
                            }
                        }, "export-data");
                    } catch (IOException | GeneralSecurityException | ChunkedMediaParametersException | ForeignRemoteAuthorityException e) {
                        Log.w("Failed to save export data", e);
                    }
                }

                Uri uri = FileProvider.getUriForFile(getApplication(), "com.halloapp.katchup.fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                result.postValue(intent);
                processing = false;
            });

            return result;
        }
    }
}
