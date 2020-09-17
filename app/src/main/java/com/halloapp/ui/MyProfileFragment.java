package com.halloapp.ui;

import android.os.Bundle;
import androidx.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.Fade;
import androidx.transition.TransitionSet;

import com.halloapp.R;
import com.halloapp.ui.profile.ProfileFragment;
import com.halloapp.util.Preconditions;

public class MyProfileFragment extends ProfileFragment implements MainNavFragment {

    private ProfileNuxViewModel nuxViewModel;

    private FrameLayout nuxContainer;
    private View nux;

    @Override
    public void resetScrollPosition() {
        layoutManager.scrollToPosition(0);
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_my_profile;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = Preconditions.checkNotNull(super.onCreateView(inflater, container, savedInstanceState));
        nuxViewModel = new ViewModelProvider(requireActivity()).get(ProfileNuxViewModel.class);
        nuxContainer = root.findViewById(R.id.nux_container);

        nuxViewModel.showNux.getLiveData().observe(getViewLifecycleOwner(), nuxToShow -> {
            if (nuxToShow == null) {
                return;
            }
            if (nuxToShow == 0) {
                TransitionManager.beginDelayedTransition(nuxContainer);
                nuxContainer.setVisibility(View.GONE);
            } else if (nuxToShow == 1) {
                if (nux == null) {
                    TransitionManager.beginDelayedTransition(nuxContainer);
                    nux = LayoutInflater.from(requireContext()).inflate(R.layout.nux_bubble_up_arrow, nuxContainer, true);
                    TextView text = nux.findViewById(R.id.nux_text);
                    View btn = nux.findViewById(R.id.ok_btn);
                    text.setText(R.string.profile_nux_text);
                    btn.setOnClickListener(v -> nuxViewModel.closeProfileNux());
                }
            } else if (nuxToShow == 2) {
                if (nuxContainer.getVisibility() != View.GONE) {
                    TransitionManager.beginDelayedTransition(nuxContainer);
                    nuxContainer.setVisibility(View.GONE);
                    //Transition to next nux,
                    nuxContainer.postDelayed(this::showMakePostNux, 250);
                } else {
                    showMakePostNux();
                }
            }
        });
        return root;
    }

    private void showMakePostNux() {
        nuxContainer.removeAllViews();
        nux = LayoutInflater.from(requireContext()).inflate(R.layout.nux_make_first_post, nuxContainer, true);
        TransitionManager.beginDelayedTransition(nuxContainer, new Fade());
        nuxContainer.setVisibility(View.VISIBLE);
    }
}
