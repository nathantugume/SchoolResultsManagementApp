package com.ugwebstudio.schoolresultsmanagementapp.admin;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.Adapters.StudentAdapter;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.Student;

import java.util.ArrayList;
import java.util.List;

public class ManageStudentsActivity extends AppCompatActivity {
    private final String TAG = "ManageStudentsActivity";
    private FirebaseAuth mAuth;
    private StudentAdapter adapter;
    private List<Student> studentList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        mAuth = FirebaseAuth.getInstance();



        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        RecyclerView recyclerView;

        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.recyclerViewStudents);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentList = new ArrayList<>();
        adapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(adapter);

        // Retrieve student data from Firestore
        retrieveStudentsData();
        // Inside your activity onCreate() method or wherever you set up your views
        FloatingActionButton fabAddStudents = findViewById(R.id.fabAddStudent);
        fabAddStudents.setOnClickListener(v -> showAddStudentsDialog());

    }

    private void retrieveStudentsData() {
        // Access the "students" collection in Firestore
        CollectionReference studentsRef = db.collection("students");

        // Retrieve all documents from the "students" collection
        studentsRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Clear the existing list
                        studentList.clear();

                        // Add retrieved documents to the list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Student student = document.toObject(Student.class);
                            studentList.add(student);
                        }

                        // Notify the adapter that the data set has changed
                        adapter.notifyDataSetChanged();
                    } else {
                        // Error retrieving documents
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(ManageStudentsActivity.this, "Failed to retrieve student data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddStudentsDialog() {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_student, null);

        // Get Material TextInputLayouts and TextInputEditTexts
        TextInputEditText editTextName = dialogView.findViewById(R.id.editTextName);
        TextInputEditText editTextEmail = dialogView.findViewById(R.id.editTextEmail);
        TextInputEditText editTextPhone = dialogView.findViewById(R.id.editTextPhone);
        TextInputEditText editTextParent = dialogView.findViewById(R.id.editTextParent);
        TextInputEditText editTextParentPhone = dialogView.findViewById(R.id.editTextParentPhone);
        TextInputEditText editTextAddress = dialogView.findViewById(R.id.editTextAddress);
        TextInputEditText editTextDob = dialogView.findViewById(R.id.editTextDob);
        TextInputEditText editTextAcademicYear = dialogView.findViewById(R.id.editTextAcademicYear);


        // Get Material AutoCompleteTextViews for subjects and classes
        MaterialAutoCompleteTextView autoCompleteTextViewClass = dialogView.findViewById(R.id.autoCompleteTextViewStudentClass);

        // Get ChipGroups for displaying selected subjects and classes
        ChipGroup chipGroupClass = dialogView.findViewById(R.id.chipGroupClass);



        ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource(this,
                R.array.classes_array, android.R.layout.simple_spinner_item);
        autoCompleteTextViewClass.setAdapter(classAdapter);



        autoCompleteTextViewClass.setOnItemClickListener((parent, view, position, id) -> {
            // Add selected class as a chip
            String selectedClass = parent.getItemAtPosition(position).toString();
            Chip chip = new Chip(chipGroupClass.getContext());
            chip.setText(selectedClass);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> chipGroupClass.removeView(chip));
            chipGroupClass.addView(chip);
            autoCompleteTextViewClass.setText("");
        });

        // Create the AlertDialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView)
                .setTitle("Add Students")
                .setPositiveButton("Add", (dialog, which) -> {
                    // Handle Add button click
                    String name = editTextName.getText().toString().trim();
                    String email = editTextEmail.getText().toString().trim();
                    String phone = editTextPhone.getText().toString().trim();
                    String studentParent = editTextParent.getText().toString().trim();
                    String studentDOB = editTextDob.getText().toString().trim();
                    String address = editTextAddress.getText().toString();
                    String parentPhone = editTextParentPhone.getText().toString().trim();
                    String academicYear = editTextAcademicYear.getText().toString().trim();



                    // Get selected classes
                    StringBuilder classesBuilder = new StringBuilder();
                    for (int i = 0; i < chipGroupClass.getChildCount(); i++) {
                        Chip chip = (Chip) chipGroupClass.getChildAt(i);
                        classesBuilder.append(chip.getText());
                        if (i < chipGroupClass.getChildCount() - 1) {
                            classesBuilder.append(", ");
                        }
                    }
                    String studentClass = classesBuilder.toString();

                    // Add the student
                    addStudents(name, phone, studentClass, studentParent, studentDOB, email,address,parentPhone,academicYear);

                    // Dismiss the dialog
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Handle Cancel button click
                    dialog.dismiss();
                });

        // Show the AlertDialog
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


    private void addStudents(String name, String phone, String studentClass, String studentParent, String studentDOB, String email, String address, String parentPhone, String academicYear) {
        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(ManageStudentsActivity.this);
        progressDialog.setMessage("Adding student...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Create a new Students object with the provided information
        Student student = new Student(name, phone, studentClass, studentParent, studentDOB, email, address, parentPhone, academicYear);

        // Access Firestore database instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Access the "students" collection in Firestore
        CollectionReference studentsRef = db.collection("students");

        // Add the student to Firestore
        studentsRef.add(student)
                .addOnSuccessListener(documentReference -> {
                    String password = generateRandomPassword();

                    // DocumentSnapshot added successfully
                    Log.d(TAG, "Student added with ID: " + documentReference.getId());
                    // Optionally, show a success message to the user
                    Toast.makeText(ManageStudentsActivity.this, "Student added successfully", Toast.LENGTH_SHORT).show();

                    // Create account for the student with the generated password
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(authTask -> {
                                if (authTask.isSuccessful()) {
                                    // Account created successfully
                                    Log.d(TAG, "Account created successfully for " + email);

                                    // Send password reset email to the student
                                    sendPasswordResetEmail(email);
                                } else {
                                    // Account creation failed
                                    Log.w(TAG, "Failed to create account for " + email, authTask.getException());
                                }

                                // Dismiss loading dialog
                                progressDialog.dismiss();
                            });

                })
                .addOnFailureListener(e -> {
                    // Handle any errors that may occur
                    Log.w(TAG, "Error adding student", e);
                    // Optionally, show an error message to the user
                    Toast.makeText(ManageStudentsActivity.this, "Failed to add student", Toast.LENGTH_SHORT).show();

                    // Dismiss loading dialog
                    progressDialog.dismiss();
                });
    }


    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Password reset email sent successfully
                            Log.d(TAG, "Password reset email sent successfully to " + email);
                            Toast.makeText(ManageStudentsActivity.this, "Students added successfully. Password reset email sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to send password reset email
                            Log.w(TAG, "Failed to send password reset email to " + email, task.getException());
                            Toast.makeText(ManageStudentsActivity.this, "Failed to add student. Failed to send password reset email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String generateRandomPassword() {
        // Generate a random password using alphanumeric characters
        // You can customize the length and characters as needed
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * characters.length());
            password.append(characters.charAt(index));
        }
        return password.toString();
    }
}