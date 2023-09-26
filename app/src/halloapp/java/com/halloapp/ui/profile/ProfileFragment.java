package com.halloapp.ui.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.calling.calling.CallManager;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.contacts.FriendshipInfo;
import com.halloapp.content.Message;
import com.halloapp.id.UserId;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.proto.server.CallType;
import com.halloapp.ui.GroupsInCommonActivity;
import com.halloapp.ui.PostsFragment;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.ViewAvatarActivity;
import com.halloapp.ui.chat.chat.ChatActivity;
import com.halloapp.ui.chat.chat.KeyVerificationActivity;
import com.halloapp.ui.settings.SettingsPrivacy;
import com.halloapp.ui.settings.SettingsProfile;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.NestedHorizontalScrollHelper;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;

public class ProfileFragment extends PostsFragment {

    private static final String ARG_SELECTED_PROFILE_USER_ID = "view_user_id";
    private static final float HIDDEN_PROFILE_ALPHA = 0.8f;
    private static final float VISIBLE_PROFILE_ALPHA = 1f;

    public static ProfileFragment newProfileFragment(@NonNull UserId userId) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_PROFILE_USER_ID, userId.rawId());
        profileFragment.setArguments(args);
        return profileFragment;
    }

    private final Me me = Me.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();
    private final CallManager callManager = CallManager.getInstance();

    private ImageView avatarView;
    private TextView nameView;
    private TextView usernameView;
    private TextView requestsTextView;
    private Button friendsButtonView;
    private Button friendsDismissButtonView;
    private View messageView;
    private View voiceCallView;
    private View videoCallView;
    private View unblockView;
    private View contactActionsContainer;
    private RecyclerView postsView;

    private AvatarLoader avatarLoader;

    private MenuItem blockMenuItem;
    private MenuItem groupsInCommonMenuItem;

    private UserId profileUserId;

    protected LinearLayoutManager layoutManager;

    private ProfileViewModel viewModel;

    private final ContactsDb.Observer contactsObserver = new ContactsDb.BaseObserver() {

        @Override
        public void onContactsChanged() {
            avatarView.post(ProfileFragment.this::loadAvatar);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ProfileFragment: onCreate");
        avatarLoader = AvatarLoader.getInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ProfileFragment: onDestroy");
    }

    @Override
    public void onDestroyView() {
        if (viewModel != null && layoutManager != null) {
            viewModel.saveScrollState(layoutManager.onSaveInstanceState());
        }
        contactsDb.removeObserver(contactsObserver);
        super.onDestroyView();
    }

    @LayoutRes
    protected int getLayout() {
        return R.layout.fragment_profile;
    }

    @Override
    protected RecyclerView getRecyclerView() {
        return postsView;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentViewGroup = container;

        setHasOptionsMenu(true);

        final View root = inflater.inflate(getLayout(), container, false);
        postsView = root.findViewById(R.id.posts);

        layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);
        postsView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        NestedHorizontalScrollHelper.applyDefaultScrollRatio(postsView);

        profileUserId = UserId.ME;
        Bundle args = getArguments();
        if (args != null) {
            String extraUserId = args.getString(ARG_SELECTED_PROFILE_USER_ID);
            if (extraUserId != null) {
                profileUserId = new UserId(extraUserId);
            }
        }

        viewModel = new ViewModelProvider(requireActivity(), new ProfileViewModel.Factory(profileUserId)).get(ProfileViewModel.class);

        final View headerView = adapter.addHeader(R.layout.profile_header);
        final View emptyHeader = adapter.addHeader(R.layout.profile_empty);
        final View emptyContainer = emptyHeader.findViewById(android.R.id.empty);
        final TextView emptyView = emptyContainer.findViewById(R.id.empty_profile_text);
        final ImageView emptyIcon = emptyContainer.findViewById(R.id.empty_icon);

        viewModel.postList.observe(getViewLifecycleOwner(), posts -> {
            emptyContainer.setVisibility(posts != null && posts.size() == 0 ? View.VISIBLE : View.GONE);
            adapter.submitList(posts, null);
        });
        if (viewModel.getSavedScrollState() != null) {
            layoutManager.onRestoreInstanceState(viewModel.getSavedScrollState());
        }

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        nameView = headerView.findViewById(R.id.name);
        usernameView = headerView.findViewById(R.id.username);
        requestsTextView = headerView.findViewById(R.id.friends_text);
        friendsButtonView = headerView.findViewById(R.id.friends_button);
        friendsDismissButtonView = headerView.findViewById(R.id.friends_dismiss_button);
        messageView = headerView.findViewById(R.id.message);
        voiceCallView = headerView.findViewById(R.id.call);
        videoCallView = headerView.findViewById(R.id.video_call);
        unblockView = headerView.findViewById(R.id.unblock);
        contactActionsContainer = headerView.findViewById(R.id.actions_container);
        unblockView.setOnClickListener(v -> {
            unBlockContact();
        });
        messageView.setOnClickListener(v -> {
            final Intent intent = ChatActivity.open(requireContext(), profileUserId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        videoCallView.setOnClickListener(v -> {
            callManager.startCallActivity(requireContext(), profileUserId, CallType.VIDEO);
        });
        voiceCallView.setOnClickListener(v -> {
            callManager.startCallActivity(requireContext(), profileUserId, CallType.AUDIO);
        });
        if (profileUserId.isMe()) {
            me.name.observe(getViewLifecycleOwner(), nameView::setText);
        } else {
            viewModel.getProfileInfo().observe(getViewLifecycleOwner(), profile -> {
                nameView.setText(profile.name);
                if (!TextUtils.isEmpty(profile.username)) {
                    usernameView.setText("@" + profile.username);
                    usernameView.setVisibility(View.VISIBLE);
                } else {
                    usernameView.setVisibility(View.GONE);
                }
                friendsButtonView.setOnClickListener(view -> {
                    updateFriendship(profile.friendshipStatus);
                });
                updateProfileVisibility(profile.friendshipStatus);
                if (profile.friendshipStatus == FriendshipInfo.Type.FRIENDS) {
                    requestsTextView.setVisibility(View.GONE);
                    friendsButtonView.setText("Friends");
                    friendsButtonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.favorites_dialog_blue));
                    friendsButtonView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black_10)));
                    friendsDismissButtonView.setVisibility(View.GONE);
                } else if (profile.friendshipStatus == FriendshipInfo.Type.OUTGOING_PENDING) {
                    requestsTextView.setVisibility(View.VISIBLE);
                    requestsTextView.setText(getString(R.string.outgoing_sent_friend_request, profile.name));
                    friendsButtonView.setText("Cancel request");
                    friendsButtonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.favorites_dialog_blue));
                    friendsButtonView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black_10)));
                    friendsDismissButtonView.setVisibility(View.GONE);
                } else if (profile.friendshipStatus == FriendshipInfo.Type.INCOMING_PENDING) {
                    requestsTextView.setVisibility(View.VISIBLE);
                    requestsTextView.setText(getString(R.string.incoming_sent_friend_request, profile.name));
                    friendsButtonView.setText("Confirm");
                    friendsButtonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                    friendsButtonView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.favorites_dialog_blue)));
                    friendsDismissButtonView.setVisibility(View.VISIBLE);
                    friendsDismissButtonView.setOnClickListener(view -> rejectFriendRequest());
                } else if (profile.friendshipStatus == FriendshipInfo.Type.NONE_STATUS) {
                    requestsTextView.setVisibility(View.GONE);
                    friendsButtonView.setText("Add friend");
                    friendsButtonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                    friendsButtonView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.favorites_dialog_blue)));
                    friendsDismissButtonView.setVisibility(View.GONE);
                }
            });
        }

        viewModel.getIsBlocked().observe(getViewLifecycleOwner(), isBlocked -> {
            updateMenu(isBlocked);
            updateMessageUnblock();
        });

        avatarView = headerView.findViewById(R.id.avatar);
        avatarView.setOnClickListener(v -> {
            ViewAvatarActivity.viewAvatarWithTransition(requireActivity(), avatarView, profileUserId);
        });
        loadAvatar();

        if (profileUserId.isMe()) {
            final View.OnClickListener editProfileClickListener = v -> {
                openProfileEditor();
            };
            avatarView.setOnClickListener(editProfileClickListener);
            nameView.setOnClickListener(editProfileClickListener);
        }

        postsView.setAdapter(adapter);
        contactsDb.addObserver(contactsObserver);
        return root;
    }

    private void updateMessageUnblock() {
        Boolean isBlocked = viewModel.getIsBlocked().getValue();
        boolean blocked = isBlocked != null && isBlocked;
        ViewUtils.setViewAndChildrenEnabled(videoCallView, !profileUserId.isMe());
        ViewUtils.setViewAndChildrenEnabled(voiceCallView, !profileUserId.isMe());
        ViewUtils.setViewAndChildrenEnabled(messageView, !profileUserId.isMe());
        if (blocked) {
            contactActionsContainer.setVisibility(View.GONE);
            unblockView.setVisibility(View.VISIBLE);
        } else {
            contactActionsContainer.setVisibility(View.VISIBLE);
            unblockView.setVisibility(View.GONE);
        }
    }

    private void updateProfileVisibility(int friendshipStatus) {
        boolean canView = friendshipStatus == FriendshipInfo.Type.FRIENDS;
        ViewUtils.setViewAndChildrenEnabled(videoCallView, canView);
        ViewUtils.setViewAndChildrenEnabled(voiceCallView, canView);
        ViewUtils.setViewAndChildrenEnabled(messageView, canView);
        contactActionsContainer.setAlpha(canView ? VISIBLE_PROFILE_ALPHA : HIDDEN_PROFILE_ALPHA);
    }

    private void updateFriendship(int friendType) {
        if (friendType == FriendshipInfo.Type.FRIENDS) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getString(R.string.remove_friend_confirmation, nameView.getText().toString()));
            builder.setMessage(getString(R.string.remove_friend_confirmation_consequences, nameView.getText().toString()));
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.remove_friend, (dialog, which) -> removeFriend());
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
        } else {
            viewModel.updateFriendship(friendType, profileUserId).observe(getViewLifecycleOwner(), success -> {
                if (Boolean.TRUE.equals(success)) {
                    viewModel.computeUserProfileInfo();
                } else {
                    SnackbarHelper.showWarning(getActivity(), getString(R.string.failed_update_profile));
                }
            });
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        postsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()));
    }

    private void loadAvatar() {
        avatarLoader.load(avatarView, profileUserId, false);
    }

    private void openProfileEditor() {
        startActivity(new Intent(requireContext(), SettingsProfile.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAvatar();
        viewModel.computeUserProfileInfo();
        adapter.notifyDataSetChanged();
    }

    private void updateMenu(Boolean isBlocked) {
        if (blockMenuItem != null) {
            if (isBlocked == null || !isBlocked) {
                blockMenuItem.setTitle(R.string.block);
            } else {
                blockMenuItem.setTitle(R.string.unblock);
            }
        }
    }

    @Override
    protected boolean shouldOpenProfileOnNamePress() {
        return false;
    }

    @Override
    protected VoiceNotePlayer getVoiceNotePlayer() {
        return viewModel.getVoiceNotePlayer();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (!profileUserId.isMe()) {
            inflater.inflate(R.menu.other_profile_menu, menu);
            blockMenuItem = menu.findItem(R.id.block);
            groupsInCommonMenuItem = menu.findItem(R.id.groups_in_common);
            viewModel.getIsBlocked().observe(this, this::updateMenu);
            viewModel.getHasGroupsInCommon().observe(this, hasGroups -> groupsInCommonMenuItem.setVisible(hasGroups != null && hasGroups));
        }
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(getContext(), SettingsPrivacy.class));
            return true;
        } else if (item.getItemId() == R.id.block) {
            Boolean isBlocked = viewModel.getIsBlocked().getValue();
            if (isBlocked == null || !isBlocked) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.block_user_confirmation, viewModel.getContact().getValue().getDisplayName()));
                builder.setMessage(getString(R.string.block_user_confirmation_consequences));
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.yes, (dialog, which) -> blockContact());
                builder.setNegativeButton(R.string.no, null);
                builder.show();
            } else {
                unBlockContact();
            }
            return true;
        } else if (item.getItemId() == R.id.verify) {
            startActivity(KeyVerificationActivity.openKeyVerification(requireContext(), profileUserId));
        } else if (item.getItemId() == R.id.groups_in_common) {
            startActivity(GroupsInCommonActivity.viewGroupsInCommon(requireContext(), profileUserId));
        } else if (item.getItemId() == R.id.report_user) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.report_user_dialog_message);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> {
                ProgressDialog progressDialog = ProgressDialog.show(requireContext(), null, getString(R.string.report_user_in_progress), true);
                Connection.getInstance().reportUserContent(profileUserId, null, null)
                        .onResponse(response -> {
                            progressDialog.dismiss();
                            SnackbarHelper.showInfo(avatarView, R.string.report_user_succeeded);
                            postsView.post(this::blockContact);
                        }).onError(error -> {
                            Log.e("Failed to report user", error);
                            progressDialog.dismiss();
                            SnackbarHelper.showWarning(avatarView, R.string.report_user_failed);
                        });
            });
            builder.setNegativeButton(R.string.no, null);
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @MainThread
    private void blockContact() {
        String chatName = nameView.getText().toString();
        ProgressDialog blockDialog = ProgressDialog.show(requireContext(), null, getString(R.string.blocking_user_in_progress, chatName), true);
        blockDialog.show();

        viewModel.blockContact(profileUserId).observe(this, success -> {
            if (success == null) {
                return;
            }
            blockDialog.cancel();
            if (success) {
                SnackbarHelper.showInfo(nameView, getString(R.string.blocking_user_successful, chatName));
                blockMenuItem.setTitle(getString(R.string.unblock));
                viewModel.sendSystemMessage(Message.USAGE_BLOCK, profileUserId);
            } else {
                SnackbarHelper.showWarning(nameView, getString(R.string.blocking_user_failed_check_internet, chatName));
            }
        });
    }

    private void unBlockContact() {
        String chatName = nameView.getText().toString();
        ProgressDialog unblockDialog = ProgressDialog.show(requireContext(), null, getString(R.string.unblocking_user_in_progress, chatName), true);
        unblockDialog.show();
        viewModel.unblockContact(new UserId(profileUserId.rawId())).observe(getViewLifecycleOwner(), success -> {
            if (success == null) {
                return;
            }
            unblockDialog.cancel();
            if (success) {
                SnackbarHelper.showInfo(nameView, getString(R.string.unblocking_user_successful, chatName));
                blockMenuItem.setTitle(getString(R.string.block));
                viewModel.sendSystemMessage(Message.USAGE_UNBLOCK, profileUserId);
            } else {
                SnackbarHelper.showWarning(nameView, getString(R.string.unblocking_user_failed_check_internet, chatName));
            }
        });
    }

    private void removeFriend() {
        String name = nameView.getText().toString();
        ProgressDialog removeFriendDialog = ProgressDialog.show(requireContext(), null, getString(R.string.removing_friend_in_progress, name), true);
        removeFriendDialog.show();

        viewModel.removeFriend(profileUserId).observe(getViewLifecycleOwner(), success -> {
            removeFriendDialog.cancel();
            if (Boolean.TRUE.equals(success)) {
                SnackbarHelper.showInfo(nameView, getString(R.string.removing_friend_successful, name));
                viewModel.computeUserProfileInfo();
            } else {
                SnackbarHelper.showWarning(nameView, getString(R.string.removing_friend_failed_check_internet, name));
            }
        });
    }

    private void rejectFriendRequest() {
        viewModel.rejectFriendRequest(profileUserId).observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                viewModel.computeUserProfileInfo();
            } else {
                SnackbarHelper.showWarning(getActivity(), R.string.error_reject_friend_request);
            }
        });
    }
}
