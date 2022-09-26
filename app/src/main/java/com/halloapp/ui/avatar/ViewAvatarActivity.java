package com.halloapp.ui.avatar;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.github.chrisbanes.photoview.PhotoView;
import com.halloapp.R;
import com.halloapp.contacts.ContactsDb;
import com.halloapp.content.ContentDb;
import com.halloapp.id.ChatId;
import com.halloapp.id.GroupId;
import com.halloapp.id.UserId;
import com.halloapp.ui.DragDownToDismissHelper;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.RadiusTransition;
import com.halloapp.ui.groups.EditGroupActivityViewModel;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;

public class ViewAvatarActivity extends HalloActivity {

    private static final String EXTRA_CHAT_ID = "chat_id";

    private static final String AVATAR_TRANSITION_NAME = "avatar-image-transition";

    private ChatId chatId;

    private PhotoView avatarView;
    private ViewAvatarViewModel viewModel;

    public static void viewAvatarWithTransition(@NonNull Activity activity, @NonNull View avatarView, @NonNull ChatId chatId) {
        Context context = avatarView.getContext();
        Intent i = new Intent(context, ViewAvatarActivity.class);
        i.putExtra(EXTRA_CHAT_ID, chatId);
        avatarView.setTransitionName(AVATAR_TRANSITION_NAME);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, avatarView, AVATAR_TRANSITION_NAME);

        activity.startActivity(i, options.toBundle());
    }

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private DragDownToDismissHelper dragDownToDismissHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatId = getIntent().getParcelableExtra(EXTRA_CHAT_ID);

        if (chatId == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_view_avatar);

        avatarView = findViewById(R.id.image);
        avatarView.setTransitionName(AVATAR_TRANSITION_NAME);
        avatarView.setReturnToMinScaleOnUp(false);
        avatarLoader.load(avatarView, chatId, null);

        dragDownToDismissHelper = new DragDownToDismissHelper(avatarView, findViewById(R.id.main));
        dragDownToDismissHelper.setDragDismissListener(this::onBackPressed);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Preconditions.checkNotNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_left_stroke);

        TransitionSet enterTransition =  createTransitionSet(getWindow().getSharedElementEnterTransition());
        Transition toSquare = RadiusTransition.toSquare();
        enterTransition.addTransition(toSquare);
        enterTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                avatarLoader.loadLarge(avatarView, chatId, null);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
        getWindow().setSharedElementEnterTransition(enterTransition);

        TransitionSet returnTransition = createTransitionSet(getWindow().getSharedElementReturnTransition());
        Transition toCircle = RadiusTransition.toCircle();
        returnTransition.addTransition(toCircle);
        getWindow().setSharedElementReturnTransition(returnTransition);

        viewModel = new ViewModelProvider(this, new ViewAvatarViewModel.Factory(getApplication(), chatId)).get(ViewAvatarViewModel.class);
        viewModel.getTitleLiveData().observe(this, this::setTitle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (dragDownToDismissHelper != null) {
            if (dragDownToDismissHelper.onTouchEvent(event)) {
                return true;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private TransitionSet createTransitionSet(Transition transition) {
        if (transition instanceof TransitionSet) {
            return ((TransitionSet) transition).clone();
        }
        TransitionSet set = new TransitionSet();
        set.addTransition(transition);
        return set;
    }

    @Override
    public void onBackPressed() {
        avatarLoader.load(avatarView, chatId, null);
        super.onBackPressed();
    }

    public static class ViewAvatarViewModel extends AndroidViewModel {
        private final ComputableLiveData<String> titleLiveData;

        public ViewAvatarViewModel(@NonNull Application application, @NonNull ChatId chatId) {
            super(application);

            titleLiveData = new ComputableLiveData<String>() {
                @Override
                protected String compute() {
                    if (chatId instanceof UserId) {
                        return ContactsDb.getInstance().getContact((UserId) chatId).getDisplayName();
                    } else if (chatId instanceof GroupId) {
                        return Preconditions.checkNotNull(ContentDb.getInstance().getGroupFeedOrChat((GroupId) chatId)).name;
                    }
                    return null;
                }
            };
        }

        public LiveData<String> getTitleLiveData() {
            return titleLiveData.getLiveData();
        }

        public static class Factory implements ViewModelProvider.Factory {
            private final Application application;
            private final ChatId chatId;

            Factory(@NonNull Application application, @NonNull ChatId chatId) {
                this.application = application;
                this.chatId = chatId;
            }

            @Override
            public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ViewAvatarViewModel.class)) {
                    //noinspection unchecked
                    return (T) new ViewAvatarViewModel(application, chatId);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }
}
