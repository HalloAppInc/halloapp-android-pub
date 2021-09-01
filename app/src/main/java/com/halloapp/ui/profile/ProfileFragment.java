package com.halloapp.ui.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.Message;
import com.halloapp.id.UserId;
import com.halloapp.props.ServerProps;
import com.halloapp.ui.GroupsInCommonActivity;
import com.halloapp.ui.PostsFragment;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.ViewAvatarActivity;
import com.halloapp.ui.chat.ChatActivity;
import com.halloapp.ui.chat.KeyVerificationActivity;
import com.halloapp.ui.settings.SettingsPrivacy;
import com.halloapp.ui.settings.SettingsProfile;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.NestedHorizontalScrollHelper;
import com.halloapp.widget.SnackbarHelper;

public class ProfileFragment extends PostsFragment {

    private static final String ARG_SELECTED_PROFILE_USER_ID = "view_user_id";

    private final Me me = Me.getInstance();
    private final ContactsDb contactsDb = ContactsDb.getInstance();
    private final ServerProps serverProps = ServerProps.getInstance();

    private ImageView avatarView;
    private TextView nameView;
    private TextView subtitleView;
    private View messageView;
    private View unblockView;
    private View addToContactsView;
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

    public static ProfileFragment newProfileFragment(@NonNull UserId userId) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SELECTED_PROFILE_USER_ID, userId.rawId());
        profileFragment.setArguments(args);
        return profileFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ProfileFragment: onCreate");
        avatarLoader = AvatarLoader.getInstance(getActivity());
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

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentViewGroup = container;

        setHasOptionsMenu(true);

        final View root = inflater.inflate(getLayout(), container, false);
        postsView = root.findViewById(R.id.posts);
        final TextView emptyView = root.findViewById(R.id.empty_profile_text);
        final View emptyContainer = root.findViewById(android.R.id.empty);
        final ImageView emptyIcon = root.findViewById(R.id.empty_icon);

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
        viewModel.postList.observe(getViewLifecycleOwner(), posts -> adapter.submitList(posts, () -> emptyContainer.setVisibility(posts.size() == 0 ? View.VISIBLE : View.GONE)));
        if (viewModel.getSavedScrollState() != null) {
            layoutManager.onRestoreInstanceState(viewModel.getSavedScrollState());
        }

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        final View headerView = adapter.addHeader(R.layout.profile_header);
        subtitleView = headerView.findViewById(R.id.subtitle);
        nameView = headerView.findViewById(R.id.name);
        messageView = headerView.findViewById(R.id.message);
        unblockView = headerView.findViewById(R.id.unblock);
        addToContactsView = headerView.findViewById(R.id.add_to_contacts);
        viewModel.getSubtitle().observe(getViewLifecycleOwner(), s -> {
            updateAddToContacts();
            subtitleView.setText(s);
            if (s == null) {
                subtitleView.setVisibility(View.GONE);
            } else {
                subtitleView.setVisibility(View.VISIBLE);
            }
        });
        unblockView.setOnClickListener(v -> {
            unBlockContact();
        });
        messageView.setOnClickListener(v -> {
            final Intent intent = ChatActivity.open(requireContext(), profileUserId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        addToContactsView.setOnClickListener(v -> {
            Contact contact = viewModel.getContact().getValue();
            String phone = viewModel.getSubtitle().getValue();
            Intent intent = IntentUtils.createContactIntent(contact, phone);
            startActivity(intent);
        });
        if (profileUserId.isMe()) {
            me.name.observe(getViewLifecycleOwner(), nameView::setText);
        } else {
            viewModel.getContact().observe(getViewLifecycleOwner(), contact -> {
                String name = contact.getDisplayName();
                nameView.setText(name);
                if (contact.addressBookName == null) {
                    emptyIcon.setImageResource(R.drawable.ic_exchange_numbers);
                    emptyView.setText(getString(R.string.posts_exchange_numbers));
                } else {
                    emptyIcon.setImageResource(R.drawable.ic_posts);
                    emptyView.setText(getString(R.string.contact_profile_empty, name));
                }
                updateAddToContacts();
                updateMessageUnblock();
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
        Contact contact = viewModel.getContact().getValue();
        Boolean isBlocked = viewModel.getIsBlocked().getValue();
        boolean blocked = isBlocked != null && isBlocked;
        if (contact == null || contact.addressBookName == null || blocked) {
            messageView.setVisibility(View.GONE);
        } else {
            messageView.setVisibility(View.VISIBLE);
        }
        if (blocked) {
            unblockView.setVisibility(View.VISIBLE);
        } else {
            unblockView.setVisibility(View.GONE);
        }
    }

    private void updateAddToContacts() {
        if (profileUserId.isMe() || TextUtils.isEmpty(viewModel.getSubtitle().getValue())) {
            addToContactsView.setVisibility(View.GONE);
            return;
        }
        Contact contact = viewModel.getContact().getValue();
        addToContactsView.setVisibility(contact == null || contact.addressBookName != null ? View.GONE : View.VISIBLE);
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
                blockContact();
            } else {
                unBlockContact();
            }
            return true;
        } else if (item.getItemId() == R.id.verify) {
            startActivity(KeyVerificationActivity.openKeyVerification(requireContext(), profileUserId));
        } else if (item.getItemId() == R.id.groups_in_common) {
            startActivity(GroupsInCommonActivity.viewGroupsInCommon(requireContext(), profileUserId));
        }
        return super.onOptionsItemSelected(item);
    }

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
}
