package com.ugwebstudio.schoolresultsmanagementapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ugwebstudio.schoolresultsmanagementapp.admin.MainActivity;
import com.ugwebstudio.schoolresultsmanagementapp.teacher.TeacherDashboardActivity;

public class SelectUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        Button btnStudent,btnAdmin;

        btnAdmin = findViewById(R.id.admin);

        btnStudent = findViewById(R.id.teacher);
        btnStudent.setOnClickListener(view -> startActivity(new Intent(SelectUserActivity.this, TeacherDashboardActivity.class)));

        btnAdmin.setOnClickListener(view -> startActivity(new Intent(SelectUserActivity.this, MainActivity.class)));

    }
}