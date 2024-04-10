package com.ugwebstudio.schoolresultsmanagementapp.teacher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.appbar.MaterialToolbar;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.admin.MainActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.ManageClassesActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.ManageStudentsActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.ManageTeachersActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.ResultsActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.StudentReportActivity;

public class TeacherDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        toolbar.setNavigationOnClickListener(view -> drawerLayout.open());

        CardView manage_results = findViewById(R.id.manage_results_card);
        CardView manage_students = findViewById(R.id.manage_student_card);
        CardView manage_class = findViewById(R.id.manage_classes_card);
        CardView view_reports_card = findViewById(R.id.viewReportsCard);
        manage_students.setOnClickListener(view -> startActivity(new Intent(TeacherDashboardActivity.this, ManageStudentsActivity.class)));
        manage_class.setOnClickListener(view -> startActivity(new Intent(TeacherDashboardActivity.this, ManageClassesActivity.class)));
        manage_results.setOnClickListener(view -> startActivity(new Intent(TeacherDashboardActivity.this, ResultsActivity.class)));
        view_reports_card.setOnClickListener(view -> startActivity(new Intent(TeacherDashboardActivity.this, StudentReportActivity.class)));
    }
}