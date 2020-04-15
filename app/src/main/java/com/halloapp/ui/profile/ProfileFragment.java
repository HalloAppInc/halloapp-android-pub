package com.halloapp.ui.profile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.BidiFormatter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.ui.PostsFragment;
import com.halloapp.ui.SettingsActivity;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.mediapicker.MediaPickerActivity;
import com.halloapp.util.Log;
import com.halloapp.util.Preconditions;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.xmpp.Connection;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends PostsFragment {

    private static final int CODE_CHANGE_AVATAR = 1;

    private ImageView avatarView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ProfileFragment: onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ProfileFragment: onDestroy");
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        final View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final RecyclerView postsView = root.findViewById(R.id.posts);
        final View emptyView = root.findViewById(android.R.id.empty);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);
        postsView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);

        final ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.postList.observe(getViewLifecycleOwner(), posts -> adapter.submitList(posts, () -> emptyView.setVisibility(posts.size() == 0 ? View.VISIBLE : View.GONE)));

        postsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) Preconditions.checkNotNull(getActivity())));

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        final View headerView = getLayoutInflater().inflate(R.layout.profile_header, container, false);
        final TextView nameView = headerView.findViewById(R.id.name);
        final LoadNameTask loadNameTask = new LoadNameTask(Me.getInstance(Preconditions.checkNotNull(getContext())));
        loadNameTask.name.observe(getViewLifecycleOwner(), user -> nameView.setText(
                BidiFormatter.getInstance().unicodeWrap(PhoneNumberUtils.formatNumber("+" + user, null))));
        loadNameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        avatarView = headerView.findViewById(R.id.avatar);
        AvatarLoader.getInstance(Connection.getInstance(), getContext()).load(avatarView, UserId.ME);

        final CircleImageView changeAvatarView = headerView.findViewById(R.id.change_avatar);
        changeAvatarView.setOnClickListener(v -> {
            Log.d("ProfileFragment request change avatar");
            final Intent intent = new Intent(getContext(), MediaPickerActivity.class);
            intent.putExtra(MediaPickerActivity.EXTRA_PICKER_PURPOSE, MediaPickerActivity.PICKER_PURPOSE_AVATAR);
            startActivityForResult(intent, CODE_CHANGE_AVATAR);
        });

        adapter.addHeader(headerView);

        postsView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_CHANGE_AVATAR && resultCode == RESULT_OK) {
            AvatarLoader.getInstance(Connection.getInstance(), getContext()).load(avatarView, UserId.ME);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.settings: {
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private static class LoadNameTask extends AsyncTask<Void, Void, Void> {

        final Me me;
        final MutableLiveData<String> name = new MutableLiveData<>() ;

        LoadNameTask(@NonNull Me me) {
            this.me = me;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            name.postValue(me.getPhone());
            return null;
        }
    }

}
