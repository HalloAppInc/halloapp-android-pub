package com.halloapp.ui.home;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.halloapp.BuildConfig;
import com.halloapp.Constants;
import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.ContentDb;
import com.halloapp.content.Post;
import com.halloapp.content.PostThumbnailLoader;
import com.halloapp.id.UserId;
import com.halloapp.media.VoiceNotePlayer;
import com.halloapp.nux.ZeroZoneManager;
import com.halloapp.permissions.PermissionUtils;
import com.halloapp.ui.MainActivity;
import com.halloapp.ui.MainNavFragment;
import com.halloapp.ui.PostsFragment;
import com.halloapp.ui.ViewHolderWithLifecycle;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.ui.avatar.DeviceAvatarLoader;
import com.halloapp.ui.invites.InviteContactsActivity;
import com.halloapp.ui.posts.InviteFriendsPostViewHolder;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.IntentUtils;
import com.halloapp.util.Preconditions;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.ActionBarShadowOnScrollListener;
import com.halloapp.widget.AvatarsLayout;
import com.halloapp.widget.BadgedDrawable;
import com.halloapp.widget.FabExpandOnScrollListener;
import com.halloapp.widget.NestedHorizontalScrollHelper;
import com.halloapp.xmpp.invites.InvitesResponseIq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class HomeFragment extends PostsFragment implements MainNavFragment, EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_ASK_CONTACTS_PERMISSION = 1;
    private static final int NUX_POST_DELAY = 2000;
    private static final int NEW_POSTS_BANNER_DISAPPEAR_TIME_MS = 5000;

    private HomeViewModel viewModel;
    private PostThumbnailLoader postThumbnailLoader;
    private DeviceAvatarLoader deviceAvatarLoader;

    private boolean scrollUpOnDataLoaded;
    private boolean restoreStateOnDataLoaded;

    private RecyclerView postsView;
    private View newPostsView;
    private AvatarsLayout newPostsAvatars;

    private View contactsNag;
    private Button contactsSettingsButton;
    private TextView contactsNagTextView;
    private View contactsLearnMore;

    private View inviteView;

    private MenuItem profileMenuItem;

    private boolean addedHomeZeroZonePost = false;
    private boolean pausedNewPostHide = false;

    private final Runnable hidePostsCallback = this::hideNewPostsBanner;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = requireContext();
        postThumbnailLoader = new PostThumbnailLoader(context, context.getResources().getDimensionPixelSize(R.dimen.comment_history_thumbnail_size));
        deviceAvatarLoader = new DeviceAvatarLoader(context);
        Log.d("HomeFragment: onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("HomeFragment: onDestroy");
        postThumbnailLoader.destroy();
        deviceAvatarLoader.destroy();
    }

    private LinearLayoutManager layoutManager;

    @Override
    public void resetScrollPosition() {
        scrollUpOnDataLoaded = true;
        restoreStateOnDataLoaded = false;
        viewModel.reloadPostsAt(Long.MAX_VALUE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentViewGroup = container;

        setHasOptionsMenu(true);

        final View root = inflater.inflate(R.layout.fragment_home, container, false);
        postsView = root.findViewById(R.id.posts);
        final View emptyView = root.findViewById(android.R.id.empty);
        newPostsView = root.findViewById(R.id.new_posts);
        newPostsAvatars = newPostsView.findViewById(R.id.new_post_avatars);
        newPostsAvatars.setAvatarLoader(AvatarLoader.getInstance());

        newPostsView.setOnClickListener(v -> {
            scrollUpOnDataLoaded = true;
            viewModel.reloadPostsAt(Long.MAX_VALUE);
        });
        layoutManager = new LinearLayoutManager(getContext());
        postsView.setLayoutManager(layoutManager);
        postsView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);

        inviteView = root.findViewById(R.id.home_invite);
        inviteView.setOnClickListener(v -> openInviteFlow());
        postsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                refreshInviteNux();
            }
        });

        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        viewModel.loadSavedState(savedInstanceState);

        if (viewModel.getSavedScrollState() != null) {
            restoreStateOnDataLoaded = true;
        }

        viewModel.getFabMenuOpen().observe(getViewLifecycleOwner(), isOpen -> {
            if (isOpen == null) {
                return;
            }
            if (isOpen) {
                if (newPostsView.removeCallbacks(hidePostsCallback)) {
                    pausedNewPostHide = true;
                }
            } else {
                if (pausedNewPostHide) {
                    newPostsView.postDelayed(hidePostsCallback, NEW_POSTS_BANNER_DISAPPEAR_TIME_MS);
                    pausedNewPostHide = false;
                }
            }
        });

        viewModel.getSuggestedContacts().observe(getViewLifecycleOwner(), list -> {
            adapter.notifyDataSetChanged();
        });

        viewModel.unseenHomePosts.getLiveData().observe(getViewLifecycleOwner(), new Observer<List<Post>>() {
            @Override
            public void onChanged(List<Post> unseenPosts) {
                if (unseenPosts != null && !unseenPosts.isEmpty()) {
                    showNewPostsBanner(unseenPosts);
                }
                viewModel.unseenHomePosts.getLiveData().removeObserver(this);
            }
        });

        viewModel.postList.observe(getViewLifecycleOwner(), posts -> {
            if (posts.isEmpty() && !addedHomeZeroZonePost) {
                postsView.postDelayed(() -> {
                    if (adapter.getItemCount() == 0) {
                        BgWorkers.getInstance().execute(() -> {
                            ZeroZoneManager.addHomeZeroZonePost(ContentDb.getInstance());
                        });
                    }
                }, NUX_POST_DELAY);
                addedHomeZeroZonePost = true;
            }
            adapter.submitList(posts, () -> {
                Log.i("HomeFragment: post list updated " + posts);
                if (viewModel.checkPendingOutgoing() || scrollUpOnDataLoaded) {
                    scrollUpOnDataLoaded = false;
                    postsView.scrollToPosition(0);
                    newPostsView.setVisibility(View.GONE);
                    onScrollToTop();
                } else if (viewModel.checkPendingIncoming()) {
                    final View childView = layoutManager.getChildAt(0);
                    final boolean scrolled = childView == null || layoutManager.getPosition(childView) != 0;
                    if (scrolled) {
                        List<Post> unseen = new ArrayList<>();
                        for (int i = 0; i < posts.size(); i++) {
                            Post post = posts.get(i);
                            if (post == null || post.seen != Post.SEEN_NO) {
                                break;
                            }
                            unseen.add(post);
                        }
                        showNewPostsBanner(unseen);
                    } else {
                        scrollUpOnDataLoaded = false;
                        postsView.scrollToPosition(0);
                        newPostsView.setVisibility(View.GONE);
                        onScrollToTop();
                    }
                }
                emptyView.setVisibility(posts.size() == 0 ? View.VISIBLE : View.GONE);
                if (restoreStateOnDataLoaded) {
                    layoutManager.onRestoreInstanceState(viewModel.getSavedScrollState());
                    restoreStateOnDataLoaded = false;
                }
                inviteView.post(this::refreshInviteNux);
            });
        });

        contactsNag = root.findViewById(R.id.contacts_nag);
        contactsNag.setOnClickListener(v -> {}); // Don't let touches pass through
        contactsNagTextView = contactsNag.findViewById(R.id.contact_permissions_nag);
        contactsLearnMore = contactsNag.findViewById(R.id.learn_more);
        contactsLearnMore.setOnClickListener(v -> IntentUtils.openUrlInBrowser(contactsLearnMore, Constants.CONTACT_PERMISSIONS_LEARN_MORE_URL));
        contactsSettingsButton = contactsNag.findViewById(R.id.settings_btn);
        contactsSettingsButton.setOnClickListener(v -> {
            if (EasyPermissions.permissionPermanentlyDenied(requireActivity(), Manifest.permission.READ_CONTACTS)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                startActivity(intent);
            } else {
                // You can directly ask for the permission.
                final String[] perms = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
                requestPermissions(perms, REQUEST_CODE_ASK_CONTACTS_PERMISSION);
            }
        });
        viewModel.getHasContactPermission().observe(getViewLifecycleOwner(), this::updateContactsNag);

        NestedHorizontalScrollHelper.applyDefaultScrollRatio(postsView);

        Preconditions.checkNotNull((SimpleItemAnimator) postsView.getItemAnimator()).setSupportsChangeAnimations(false);

        postsView.setAdapter(adapter);

        return root;
    }

    @Override
    protected PostsAdapter createAdapter() {
        return new HomePostAdapter();
    }

    private void onScrollToTop() {
        viewModel.onScrollToTop();
        newPostsView.setVisibility(View.GONE);
    }

    private void updateContactsNag(boolean hasPermission) {
        if (hasPermission) {
            contactsNag.setVisibility(View.GONE);
        } else {
            contactsNag.setVisibility(View.VISIBLE);
            if (EasyPermissions.permissionPermanentlyDenied(this, Manifest.permission.READ_CONTACTS)
                    || EasyPermissions.permissionPermanentlyDenied(this, Manifest.permission.WRITE_CONTACTS)) {
                contactsSettingsButton.setText(R.string.go_to_settings);
                contactsNagTextView.setText(R.string.contact_permissions_request_permanently_denied);
            } else {
                contactsSettingsButton.setText(R.string.continue_button);
                contactsNagTextView.setText(R.string.contact_permissions_request);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        postsView.addOnScrollListener(new ActionBarShadowOnScrollListener((AppCompatActivity) requireActivity()) {
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final RecyclerView.LayoutManager layoutManager = Preconditions.checkNotNull(recyclerView.getLayoutManager());
                final View childView = layoutManager.getChildAt(0);
                if (childView != null && layoutManager.getPosition(childView) == 0) {
                    onScrollToTop();
                }
            }
        });

        postsView.addOnScrollListener(new FabExpandOnScrollListener((AppCompatActivity) requireActivity()));
    }

    @Override
    protected VoiceNotePlayer getVoiceNotePlayer() {
        return viewModel.getVoiceNotePlayer();
    }

    private void showNewPostsBanner(List<Post> unseenPosts) {
        if (newPostsView.getVisibility() != View.VISIBLE) {
            newPostsView.setVisibility(View.VISIBLE);
            final float initialTranslation = -getResources().getDimension(R.dimen.details_media_list_height);
            newPostsView.setTranslationY(initialTranslation);
            newPostsView.animate().setDuration(200).translationY(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    newPostsView.setTranslationY(0);
                }
            }).start();
        }
        if (unseenPosts != null) {
            HashSet<UserId> users = new HashSet<>();
            for (Post post : unseenPosts) {
                users.add(post.senderUserId);
            }
            newPostsAvatars.setAvatarCount(Math.min(users.size(), 3));
            newPostsAvatars.setUsers(new ArrayList<>(users));
        }
        newPostsView.removeCallbacks(hidePostsCallback);
        newPostsView.postDelayed(hidePostsCallback, NEW_POSTS_BANNER_DISAPPEAR_TIME_MS);
    }

    private void hideNewPostsBanner() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        final float initialTranslation = -context.getResources().getDimension(R.dimen.details_media_list_height);
        newPostsView.animate().setDuration(200).translationY(initialTranslation).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                newPostsView.setVisibility(View.GONE);
            }
        }).start();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewModel != null && layoutManager != null) {
            viewModel.saveInstanceState(outState);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        saveScrollState();
    }

    private void refreshInviteNux() {
        int lastItem = adapter.getItemCount() - 1;
        if (lastItem == layoutManager.findLastVisibleItemPosition()) {
            View v = layoutManager.findViewByPosition(lastItem);
            if (v != null && v.getBottom() > inviteView.getTop()) {
                int dist = v.getBottom() - inviteView.getTop();
                int height = inviteView.getHeight() / 2;
                if (dist > height) {
                    inviteView.setVisibility(View.INVISIBLE);
                } else {
                    float alpha = 1.0f - ((float) dist / (float) height);
                    inviteView.setVisibility(View.VISIBLE);
                    inviteView.setAlpha(alpha);
                }
            } else {
                inviteView.setAlpha(1.0f);
                inviteView.setVisibility(View.VISIBLE);
            }
        } else {
            inviteView.setVisibility(View.INVISIBLE);
        }
    }

    private void saveScrollState() {
        if (viewModel != null && layoutManager != null) {
            viewModel.saveScrollState(layoutManager.onSaveInstanceState());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.invite) {
            openInviteFlow();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openInviteFlow() {
        if (PermissionUtils.hasOrRequestContactPermissions(requireActivity(), MainActivity.REQUEST_CODE_ASK_CONTACTS_PERMISSION_INVITE)) {
            startActivity(new Intent(requireContext(), InviteContactsActivity.class));
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (requestCode) {
            case REQUEST_CODE_ASK_CONTACTS_PERMISSION: {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).refreshFab();
                }
                break;
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        updateContactsNag(false);
    }

    private void sendInvite(@NonNull Contact contact) {
        if (contact.normalizedPhone == null) {
            Log.e("InvitecontactsActivity/sendInvite null contact phone");
            return;
        }
        ProgressDialog dialog = ProgressDialog.show(requireContext(), null, getString(R.string.invite_creation_in_progress));
        viewModel.sendInvite(contact).observe(this, nullableResult -> {
            dialog.cancel();
            if (nullableResult != InvitesResponseIq.Result.SUCCESS) {
                showErrorDialog(nullableResult);
            } else {
                onSuccessfulInvite(contact);
            }
        });
    }

    private void onSuccessfulInvite(@NonNull Contact contact) {
        Intent chooser = IntentUtils.createSmsChooserIntent(requireContext(), getString(R.string.invite_friend_chooser_title, contact.getShortName()), Preconditions.checkNotNull(contact.normalizedPhone), getInviteText(contact));
        startActivity(chooser);
    }

    private void showErrorDialog(@Nullable @InvitesResponseIq.Result Integer result) {
        @StringRes int errorMessageRes;
        if (result == null) {
            errorMessageRes = R.string.invite_failed_internet;
        } else {
            switch (result) {
                case InvitesResponseIq.Result.EXISTING_USER:
                    errorMessageRes = R.string.invite_failed_existing_user;
                    break;
                case InvitesResponseIq.Result.INVALID_NUMBER:
                    errorMessageRes = R.string.invite_failed_invalid_number;
                    break;
                case InvitesResponseIq.Result.NO_INVITES_LEFT:
                    errorMessageRes = R.string.invite_failed_no_invites;
                    break;
                case InvitesResponseIq.Result.NO_ACCOUNT:
                case InvitesResponseIq.Result.UNKNOWN:
                default:
                    errorMessageRes = R.string.invite_failed_unknown;
                    break;
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setMessage(errorMessageRes).setPositiveButton(R.string.ok, null).create();
        dialog.show();
    }

    private String getInviteText(Contact contact) {
        return getString(R.string.invite_text_with_name_and_number, contact.getShortName(), contact.getDisplayPhone(), Constants.DOWNLOAD_LINK_URL);
    }

    protected class HomePostAdapter extends PostsAdapter {

        private int inviteCardIndex = -1;

        private InviteFriendsPostViewHolder.Host host = new InviteFriendsPostViewHolder.Host() {
            @Override
            public void sendInvite(Contact contact) {
                HomeFragment.this.sendInvite(contact);
            }

            @Override
            public DeviceAvatarLoader getAvatarLoader() {
                return deviceAvatarLoader;
            }
        };

        @Override
        public int getInviteCardIndex() {
            return inviteCardIndex;
        }

        @Override
        public void submitList(@Nullable PagedList<Post> pagedList, @Nullable Runnable completion) {
            super.submitList(pagedList, () -> {
                int items = super.getItemCount();
                if (items == 0) {
                    removeInviteCard();
                } else {
                    List<Contact> suggestedList = viewModel.getSuggestedContacts().getValue();
                    if (suggestedList == null || suggestedList.isEmpty()) {
                        removeInviteCard();
                    } else {
                        int newIndex = Math.min(5, items);
                        if (inviteCardIndex == -1) {
                            inviteCardIndex = newIndex;
                            notifyItemInserted(inviteCardIndex);
                        } else {
                            int old = inviteCardIndex;
                            inviteCardIndex = newIndex;
                            notifyItemMoved(old, inviteCardIndex);
                        }
                    }
                }
                if (completion != null) {
                    completion.run();
                }
            });
        }

        private void removeInviteCard() {
            if (inviteCardIndex != -1) {
                notifyItemRemoved(inviteCardIndex);
                inviteCardIndex = -1;
            }
        }

        @Override
        public long getItemId(int position) {
            if (position == inviteCardIndex) {
                return -position;
            }
            if (position < inviteCardIndex) {
                return super.getItemId(position);
            } else if (inviteCardIndex != -1) {
                return super.getItemId(position - 1);
            }
            return super.getItemId(position);
        }

        @Override
        public int getItemCount() {
            int count = super.getItemCount();
            if (inviteCardIndex != -1) {
                return count + 1;
            }
            return count;
        }

        @Override
        protected ListUpdateCallback createUpdateCallback() {
            final AdapterListUpdateCallback adapterCallback = new AdapterListUpdateCallback(this);
            return new ListUpdateCallback() {

                public void onInserted(int position, int count) {
                    position = translateToAdapterPos(position);
                    adapterCallback.onInserted(position, count);
                    if (position < inviteCardIndex) {
                        inviteCardIndex += count;
                    }
                }

                public void onRemoved(int position, int count) {
                    position = translateToAdapterPos(position);
                    adapterCallback.onRemoved(position, count);
                    if (position < inviteCardIndex) {
                        if (position + count <= inviteCardIndex) {
                            inviteCardIndex -= count;
                        } else {
                            inviteCardIndex = position;
                        }
                    }
                }

                public void onMoved(int fromPosition, int toPosition) {
                    fromPosition = translateToAdapterPos(fromPosition);
                    toPosition = translateToAdapterPos(toPosition);
                    adapterCallback.onMoved(fromPosition, toPosition);
                }

                public void onChanged(int position, int count, @Nullable Object payload) {
                    position = translateToAdapterPos(position);
                    adapterCallback.onChanged(position, count, payload);
                }
            };
        }

        protected int translateToAdapterPos(int position) {
            if (getInviteCardIndex() >= 0 && position >= getInviteCardIndex()) {
                position += 1;
            }
            return super.translateToAdapterPos(position);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == inviteCardIndex) {
                return POST_TYPE_INVITE_CARD;
            }
            if (position < inviteCardIndex) {
                return super.getItemViewType(position);
            } else if (inviteCardIndex != -1) {
                return super.getItemViewType(position - 1);
            }
            return super.getItemViewType(position);
        }

        @NonNull
        @Override
        public ViewHolderWithLifecycle onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == POST_TYPE_INVITE_CARD) {
                return new InviteFriendsPostViewHolder(viewModel.getSuggestedContacts(), LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_invite_card, parent, false), host);
            }
            return super.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolderWithLifecycle holder, int position) {
            if (!(holder instanceof InviteFriendsPostViewHolder)) {
                if (inviteCardIndex != -1 && position > inviteCardIndex) {
                    position -= 1;
                }
                super.onBindViewHolder(holder, position);
            }
        }
    }
}
