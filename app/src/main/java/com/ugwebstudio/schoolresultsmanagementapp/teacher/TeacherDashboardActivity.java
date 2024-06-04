package com.ugwebstudio.schoolresultsmanagementapp.teacher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.SelectUserActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.MainActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.ManageClassesActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.ManageStudentsActivity;
import com.ugwebstudio.schoolresultsmanagementapp.teacher.TeacherDashboardActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.ManageTeachersActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.ResultsActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.StudentReportActivity;

public class TeacherDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        toolbar.setNavigationOnClickListener(view -> drawerLayout.open());
        // Retrieve the user type from the intent
        String userType = getIntent().getStringExtra("userType");

        // Use the user type as needed
        if (userType != null) {
            // Do something with the user type
           toolbar.setTitle("Teacher Dashboard");
        }

        CardView manage_results = findViewById(R.id.manage_results_card);
        CardView manage_students = findViewById(R.id.manage_student_card);
        CardView manage_class = findViewById(R.id.manage_classes_card);
        CardView view_reports_card = findViewById(R.id.viewReportsCard);
        view_reports_card.setOnClickListener(view -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, StudentReportActivity.class);
            intent.putExtra("userType", userType); // Pass user type to the next activity
            startActivity(intent);
        });
        manage_students.setOnClickListener(view -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, ManageStudentsActivity.class);
            intent.putExtra("userType", userType); // Pass user type to the next activity
            startActivity(intent);
        });

        manage_class.setOnClickListener(view -> {
            Intent intent = new Intent(TeacherDashboardActivity.this, ManageClassesActivity.class);
            intent.putExtra("userType", userType); // Pass user type to the next activity
            startActivity(intent);
        });
        manage_results.setOnClickListener(view -> {
                    Intent intent = new Intent(TeacherDashboardActivity.this, ResultsActivity.class);
                    intent.putExtra("userType", userType); // Pass user type to the next activity
                    startActivity(intent);
                });

                navigationView.setNavigationItemSelectedListener(item -> {
                    if (item.getItemId() == R.id.nav_students) {
                        startActivity(new Intent(TeacherDashboardActivity.this, TeacherDashboardActivity.class));
                        drawerLayout.close();
                    }

                    if (item.getItemId() == R.id.nav_results) {
                        startActivity(new Intent(TeacherDashboardActivity.this, ResultsActivity.class));
                        drawerLayout.close();
                    }

                    if (item.getItemId() == R.id.nav_reports) {
                        startActivity(new Intent(TeacherDashboardActivity.this, StudentReportActivity.class));
                        drawerLayout.close();
                    }
                    return false;
                });
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.sign_out_menu){
                mAuth.signOut();
                startActivity(new Intent(TeacherDashboardActivity.this, SelectUserActivity.class));
            }
            return true;
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_app_bar);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home){
                startActivity(new Intent(TeacherDashboardActivity.this, TeacherDashboardActivity.class));
            }
            if (item.getItemId() == R.id.bottom_report){
                startActivity(new Intent(TeacherDashboardActivity.this, StudentReportActivity.class));

            }
            if (item.getItemId() == R.id.bottom_classes){
                startActivity(new Intent(TeacherDashboardActivity.this, ManageClassesActivity.class));

            }
            if (item.getItemId() == R.id.bottom_results){
                startActivity(new Intent(TeacherDashboardActivity.this, ManageResultsActivity.class));

            }

            return false;
        });
    }
    
    
}