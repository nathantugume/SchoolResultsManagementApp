package com.ugwebstudio.schoolresultsmanagementapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.Student.StudentDashboardActivity;
import com.ugwebstudio.schoolresultsmanagementapp.admin.MainActivity;
import com.ugwebstudio.schoolresultsmanagementapp.teacher.TeacherDashboardActivity;

public class SelectUserActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        btnLogin.setOnClickListener(v -> loginUser());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            progressDialog.setMessage("Loading...");

            checkUserRole(currentUser.getEmail());
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserRole(email);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(SelectUserActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(String email) {
        if (email.equals("admin@gmail.com")) {
            startActivity(new Intent(SelectUserActivity.this, MainActivity.class));
            finish();
            return;
        }

        progressDialog.setMessage("Checking teacher role...");
        db.collection("teachers").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Teachers", document.getId());
                                if (document.exists()) {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(SelectUserActivity.this, TeacherDashboardActivity.class);
                                    intent.putExtra("userType", "teacher"); // Pass user type to the next activity
                                    startActivity(intent);
                                    finish();
                                    finish();
                                    return;
                                }
                            }
                        }
                        checkStudentCollection(email);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(SelectUserActivity.this, "Failed to check teacher role", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkStudentCollection(String email) {
        progressDialog.setMessage("Checking student role...");
        db.collection("students").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Students", document.getId());

                                if (document.exists()) {
                                    progressDialog.dismiss();

                                    startActivity(new Intent(SelectUserActivity.this, StudentDashboardActivity.class));
                                    finish();
                                    return;
                                }
                            }
                        }
                        progressDialog.dismiss();
                        Toast.makeText(SelectUserActivity.this, "No such user exists", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(SelectUserActivity.this, "Failed to check student role", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
