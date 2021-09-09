package com.halloapp.ui.avatar;

import android.app.Activity;
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

import com.github.chrisbanes.photoview.PhotoView;
import com.halloapp.R;
import com.halloapp.id.UserId;
import com.halloapp.ui.DragDownToDismissHelper;
import com.halloapp.ui.HalloActivity;
import com.halloapp.ui.RadiusTransition;
import com.halloapp.util.Preconditions;

public class ViewAvatarActivity extends HalloActivity {

    private static final String EXTRA_USER_ID = "user_id";

    private static final String AVATAR_TRANSITION_NAME = "avatar-image-transition";

    private PhotoView avatarView;

    public static void viewAvatarWithTransition(@NonNull Activity activity, @NonNull View avatarView, UserId userId) {
        Context context = avatarView.getContext();
        Intent i = new Intent(context, ViewAvatarActivity.class);
        i.putExtra(EXTRA_USER_ID, userId);
        avatarView.setTransitionName(AVATAR_TRANSITION_NAME);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, avatarView, AVATAR_TRANSITION_NAME);

        activity.startActivity(i, options.toBundle());
    }

    private final AvatarLoader avatarLoader = AvatarLoader.getInstance();

    private DragDownToDismissHelper dragDownToDismissHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserId userId = getIntent().getParcelableExtra(EXTRA_USER_ID);

        if (userId == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_view_avatar);

        avatarView = findViewById(R.id.image);
        avatarView.setTransitionName(AVATAR_TRANSITION_NAME);
        avatarView.setReturnToMinScaleOnUp(false);
        avatarLoader.load(avatarView, userId, false);

        dragDownToDismissHelper = new DragDownToDismissHelper(avatarView, findViewById(R.id.main));
        dragDownToDismissHelper.setDragDismissListener(this::onBackPressed);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Preconditions.checkNotNull(getSupportActionBar()).setHomeAsUpIndicator(R.drawable.ic_arrow_left_stroke);

        TransitionSet enterTransition =  createTransitionSet(getWindow().getSharedElementEnterTransition());
        Transition toSquare = RadiusTransition.toSquare();
        enterTransition.addTransition(toSquare);
        getWindow().setSharedElementEnterTransition(enterTransition);

        TransitionSet returnTransition = createTransitionSet(getWindow().getSharedElementReturnTransition());
        Transition toCircle = RadiusTransition.toCircle();
        returnTransition.addTransition(toCircle);
        getWindow().setSharedElementReturnTransition(returnTransition);
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
}
