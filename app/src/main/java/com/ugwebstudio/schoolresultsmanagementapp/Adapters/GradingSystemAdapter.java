package com.ugwebstudio.schoolresultsmanagementapp.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ugwebstudio.schoolresultsmanagementapp.Fragments.ALevelFragment;
import com.ugwebstudio.schoolresultsmanagementapp.Fragments.OLevelFragment;

public class GradingSystemAdapter extends FragmentStateAdapter {
    public GradingSystemAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new ALevelFragment();
        }
        return new OLevelFragment(); // Fallback
    }

    @Override
    public int getItemCount() {
        // Two fragments
        return 2;
    }

}
