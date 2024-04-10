package com.ugwebstudio.schoolresultsmanagementapp.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ugwebstudio.schoolresultsmanagementapp.Adapters.GradingSystemAdapter;
import com.ugwebstudio.schoolresultsmanagementapp.R;

public class GradingSystemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grading_system);

        GradingSystemAdapter sectionsPagerAdapter = new GradingSystemAdapter(this);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("O'Level");
                    } else {
                        tab.setText("A'Level");
                    }
                }
        ).attach();


    }
}