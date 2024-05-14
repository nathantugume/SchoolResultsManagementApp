package com.ugwebstudio.schoolresultsmanagementapp.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.teacher.ManageResultsActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigationView);


        toolbar.setNavigationOnClickListener(view -> drawerLayout.open());

        CardView manage_results = findViewById(R.id.manage_results_card);
        CardView manage_teachers = findViewById(R.id.manage_teacher_card);
        CardView manage_students = findViewById(R.id.manage_student_card);
        CardView manage_class = findViewById(R.id.manage_classes_card);
        CardView view_reports_card = findViewById(R.id.viewReportsCard);
        manage_teachers.setOnClickListener(view -> startActivity(new Intent(MainActivity.this,ManageTeachersActivity.class)));
        manage_students.setOnClickListener(view -> startActivity(new Intent(MainActivity.this,ManageStudentsActivity.class)));
        manage_class.setOnClickListener(view -> startActivity(new Intent(MainActivity.this,ManageClassesActivity.class)));
        manage_results.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ResultsActivity.class)));
        view_reports_card.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, StudentReportActivity.class)));

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_students){
                startActivity(new Intent(MainActivity.this, ManageStudentsActivity.class));
                drawerLayout.close();
            }

            if (item.getItemId() == R.id.nav_results){
                startActivity(new Intent(MainActivity.this, ResultsActivity.class));
                drawerLayout.close();
            }

            if (item.getItemId() == R.id.nav_reports){
                startActivity(new Intent(MainActivity.this, StudentReportActivity.class));
                drawerLayout.close();
            }
            return false;
        });


    }
}