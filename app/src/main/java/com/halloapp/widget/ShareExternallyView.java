package com.halloapp.widget;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.provider.Telephony;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

public class ShareExternallyView extends RecyclerView {

    public ShareExternallyView(@NonNull Context context) {
        this(context, null);
    }

    public ShareExternallyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShareExternallyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    protected List<ShareTarget> shareTargetList = new ArrayList<>();

    protected ShareOptionAdapter adapter = setupShareOptionAdapter();

    private ShareListener listener;

    public interface ShareListener {
        void onOpenShare();
        void onShareTo(ShareTarget target);
    }

    public void setListener(ShareListener listener) {
        this.listener = listener;
    }

    protected ShareOptionAdapter setupShareOptionAdapter() {
        return new ShareOptionAdapter();
    }

    private void init(@NonNull Context context) {
        addTargetIfAvailable(Constants.PACKAGE_WHATSAPP);
        ThreadUtils.runWithoutStrictModeRestrictions(() -> {
            String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context);
            addTargetIfAvailable(defaultSmsPackage);
        });
        addTargetIfAvailable(Constants.PACKAGE_INSTAGRAM);
        addTargetIfAvailable(Constants.PACKAGE_SNAPCHAT);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        setLayoutManager(layoutManager);
        setAdapter(adapter);
    }

    private void addTargetIfAvailable(String packageName) {
        ShareTarget target = tryToGetTarget(packageName);
        if (target != null) {
            shareTargetList.add(target);
        }
    }

    private ShareTarget tryToGetTarget(String packageName) {
        ShareTarget target = new ShareTarget();
        PackageManager pm = getContext().getPackageManager();
        try {
            target.drawable = pm.getApplicationIcon(packageName);
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            if (info == null) {
                return null;
            }
            target.packageName = packageName;
            target.name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0));
        } catch (Exception e) {
            return null;
        }
        return target;
    }

    public static class ShareTarget {
        public Drawable drawable;
        public CharSequence name;
        public String packageName;

        public String getPackageName() {
            return packageName;
        }
    }

    protected static final int TYPE_SHARE_VIA = 0;
    protected static final int TYPE_SHARE = 1;

    protected class ShareOptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_SHARE_VIA) {
                return new ShareViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_external_share_option, parent, false));
            }
            return new ShareTargetViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_external_share_app_option, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (holder instanceof ShareTargetViewHolder) {
                ShareTargetViewHolder tvh = (ShareTargetViewHolder) holder;
                tvh.bind(shareTargetList.get(position - 1));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_SHARE_VIA;
            }
            return TYPE_SHARE;
        }

        @Override
        public int getItemCount() {
            return shareTargetList.size() + 1;
        }
    }

    protected class ShareViewHolder extends RecyclerView.ViewHolder {

        public ShareViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOpenShare();
                }
            });
        }
    }

    protected class ShareTargetViewHolder extends RecyclerView.ViewHolder {

        protected ImageView icon;
        protected TextView name;

        protected ShareTarget target;

        public ShareTargetViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onShareTo(target);
                }
            });

            icon = itemView.findViewById(R.id.app_icon);
            name = itemView.findViewById(R.id.app_name);
        }

        public void bind(ShareTarget target) {
            this.target = target;
            icon.setImageDrawable(target.drawable);
            name.setText(target.name);
        }
    }
}
