package com.halloapp.newapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.halloapp.R;
import com.halloapp.ui.HalloActivity;

public class NewMainActivity extends HalloActivity {

    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);

        viewPager = findViewById(R.id.pager);
        pagerAdapter = new SlidingPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);
    }

    @Override
    public void onBackPressed() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem == 1) { // Main screen
            super.onBackPressed();
        } else if (currentItem == 0) {
            viewPager.setCurrentItem(1);
        } else {
            viewPager.setCurrentItem(currentItem - 1);
        }
    }

    public void nextScreen() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    public void previousScreen() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }

    private class SlidingPagerAdapter extends FragmentStateAdapter {
        public SlidingPagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new InviteFragment();
                case 1: return new MainFragment();
                case 2: return new NewProfileFragment();
                case 3: return new SettingsFragment();
                default: throw new IllegalArgumentException("Invalid position " + position);
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
