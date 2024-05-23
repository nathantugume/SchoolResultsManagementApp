package com.ugwebstudio.schoolresultsmanagementapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ugwebstudio.schoolresultsmanagementapp.Student.StudentLoginActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.AdminLoginActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.MainActivity;
import com.ugwebstudio.schoolresultsmanagementapp.teacher.TeacherDashboardActivity;
import com.ugwebstudio.schoolresultsmanagementapp.teacher.TeacherLoginActivity;

public class SelectUserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        Button btnTeacher,btnAdmin, btnStudent;

        btnAdmin = findViewById(R.id.admin);

        btnTeacher = findViewById(R.id.teacher);
        btnStudent = findViewById(R.id.student);
        btnTeacher.setOnClickListener(view -> startActivity(new Intent(SelectUserActivity.this, TeacherLoginActivity.class)));

        btnAdmin.setOnClickListener(view -> startActivity(new Intent(SelectUserActivity.this, AdminLoginActivity.class)));
        btnStudent.setOnClickListener(view -> startActivity(new Intent(SelectUserActivity.this, StudentLoginActivity.class)));

    }
}