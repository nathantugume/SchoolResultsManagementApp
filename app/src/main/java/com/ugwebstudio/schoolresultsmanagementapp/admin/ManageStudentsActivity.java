package com.ugwebstudio.schoolresultsmanagementapp.admin;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ugwebstudio.schoolresultsmanagementapp.Adapters.StudentAdapter;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.Student;
import com.ugwebstudio.schoolresultsmanagementapp.teacher.ManageResultsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ManageStudentsActivity extends AppCompatActivity implements StudentAdapter.OnItemClickListener,StudentAdapter.OnItemLongClickListener {
    private final String TAG = "ManageStudentsActivity";
    private FirebaseAuth mAuth;
    private StudentAdapter adapter;
    private List<Student> studentList;
    private FirebaseFirestore db;
    private String selectedClass;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageViewStudent;
    private Uri imageUri;
    private FirebaseStorage storage;
    
    private TextInputEditText searchEditText;
    private List<Student> fullStudentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_students);

        storage = FirebaseStorage.getInstance();
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_app_bar);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home){
                startActivity(new Intent(ManageStudentsActivity.this, MainActivity.class));
            }
            if (item.getItemId() == R.id.bottom_report){
                startActivity(new Intent(ManageStudentsActivity.this, StudentReportActivity.class));

            }
            if (item.getItemId() == R.id.bottom_classes){
                startActivity(new Intent(ManageStudentsActivity.this, ManageClassesActivity.class));

            }
            if (item.getItemId() == R.id.bottom_results){
                startActivity(new Intent(ManageStudentsActivity.this, ManageResultsActivity.class));

            }

            return false;
        });

        // Get the search EditText
        searchEditText = findViewById(R.id.searchEditText);
        // Assuming you have an EditText named searchEditText
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString().toLowerCase().trim();
                adapter.filterList(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });




        mAuth = FirebaseAuth.getInstance();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        RecyclerView recyclerView;

        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.recyclerViewStudents);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentList = new ArrayList<>();
        fullStudentList = new ArrayList<>();
        adapter = new StudentAdapter(studentList);
        recyclerView.setAdapter(adapter);

        // Set the listener for item clicks
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        // Retrieve student data from Firestore
        retrieveStudentsData();
        // Inside your activity onCreate() method or wherever you set up your views
        FloatingActionButton fabAddStudents = findViewById(R.id.fabAddStudent);
        fabAddStudents.setOnClickListener(v -> showAddStudentsDialog());

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewStudent.setImageURI(imageUri);
        }
    }

    private void showEditStudentDialog(Student student) {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_student, null);

        // Get TextInputEditTexts and populate with student data
        TextInputEditText editTextName = dialogView.findViewById(R.id.editTextEditName);
        TextInputEditText editTextEmail = dialogView.findViewById(R.id.editTextEditEmail);
        TextInputEditText editTextPhone = dialogView.findViewById(R.id.editTextEditPhone);
        TextInputEditText editTextParent = dialogView.findViewById(R.id.editTextEditParent);
        TextInputEditText editTextParentPhone = dialogView.findViewById(R.id.editTextEditParentPhone);
        TextInputEditText editTextAddress = dialogView.findViewById(R.id.editTextEditAddress);
        TextInputEditText editTextDob = dialogView.findViewById(R.id.editTextEditDob);
        TextInputEditText editTextAcademicYear = dialogView.findViewById(R.id.editTextEditAcademicYear);
        MaterialAutoCompleteTextView autoCompleteTextViewClass = dialogView.findViewById(R.id.autoCompleteTextViewEditClass);
        ChipGroup chipGroupClass = dialogView.findViewById(R.id.chipGroupEditClass);

        // Set existing student information
        editTextName.setText(student.getName());
        editTextEmail.setText(student.getEmail());
        editTextPhone.setText(student.getPhone());
        editTextParent.setText(student.getStudentParent());
        editTextParentPhone.setText(student.getParentPhone());
        editTextAddress.setText(student.getAddress());
        editTextDob.setText(student.getStudentDOB());
        editTextAcademicYear.setText(student.getAcademicYear());

        ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource(this,
                R.array.classes_array, android.R.layout.simple_spinner_item);
        autoCompleteTextViewClass.setAdapter(classAdapter);
        Chip chip = new Chip(chipGroupClass.getContext());
        chip.setText(student.getStudentClass());
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroupClass.removeView(chip));
        chipGroupClass.addView(chip);

        // Create the AlertDialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView)
                .setTitle("Edit Student")
                .setPositiveButton("Update", (dialog, which) -> {
                    // Handle Update button click
                    String name = editTextName.getText().toString().trim();
                    String email = editTextEmail.getText().toString().trim();
                    String phone = editTextPhone.getText().toString().trim();
                    String studentParent = editTextParent.getText().toString().trim();
                    String studentDOB = editTextDob.getText().toString().trim();
                    String address = editTextAddress.getText().toString();
                    String parentPhone = editTextParentPhone.getText().toString().trim();
                    String academicYear = editTextAcademicYear.getText().toString().trim();
                    String studentClass = chipGroupClass.getChildCount() > 0 ? ((Chip) chipGroupClass.getChildAt(0)).getText().toString() : "";

                    // Update the student
                    updateStudent(student.getId(), name, phone, studentClass, studentParent, studentDOB, email, address, parentPhone, academicYear);

                    // Dismiss the dialog
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the AlertDialog
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void updateStudent(String studentId, String name, String phone, String studentClass, String studentParent, String studentDOB, String email, String address, String parentPhone, String academicYear) {
        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(ManageStudentsActivity.this);
        progressDialog.setMessage("Updating student...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Create a map of the updated student information
        Student updatedStudent = new Student(name,studentId, phone, studentClass, studentParent, studentDOB, email, address, parentPhone, academicYear);

        // Update the student in Firestore
        db.collection("students").document(studentId)
                .set(updatedStudent)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Student updated with ID: " + studentId);
                    Toast.makeText(ManageStudentsActivity.this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < studentList.size(); i++) {
                        if (studentList.get(i).getId().equals(studentId)) {
                            studentList.set(i, updatedStudent);
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating student", e);
                    Toast.makeText(ManageStudentsActivity.this, "Failed to update student", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> progressDialog.dismiss());
    }

    private void retrieveStudentsData() {
        CollectionReference studentsRef = db.collection("students");
        studentsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                studentList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Student student = document.toObject(Student.class);
                    String id = document.getId();
                    student.setId(id);
                    Log.d(TAG, "Student ID: " + id);
                    studentList.add(student);
                    fullStudentList.add(student);
                }
                adapter.setInitialList(studentList);
                adapter.notifyDataSetChanged();

            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
                Toast.makeText(ManageStudentsActivity.this, "Failed to retrieve student data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showAddStudentsDialog() {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_student, null);
         imageViewStudent = dialogView.findViewById(R.id.imageViewStudent);
         Button buttonSelectImage = dialogView.findViewById(R.id.buttonSelectImage);
        // Get Material TextInputLayouts and TextInputEditTexts

        //fetch image
        buttonSelectImage.setOnClickListener(v -> openFileChooser());

        TextInputEditText editTextName = dialogView.findViewById(R.id.editTextName);
        TextInputEditText editTextEmail = dialogView.findViewById(R.id.editTextEmail);
        TextInputEditText editTextPhone = dialogView.findViewById(R.id.editTextPhone);
        TextInputEditText editTextParent = dialogView.findViewById(R.id.editTextParent);
        TextInputEditText editTextParentPhone = dialogView.findViewById(R.id.editTextParentPhone);
        TextInputEditText editTextAddress = dialogView.findViewById(R.id.editTextAddress);
        TextInputEditText editTextDob = dialogView.findViewById(R.id.editTextDob);
        TextInputEditText editTextAcademicYear = dialogView.findViewById(R.id.editTextAcademicYear);

        //set dob
        editTextDob.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Get current date to set as default in the picker
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Create and show DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(ManageStudentsActivity.this, (view, year1, monthOfYear, dayOfMonth) -> {
                    // Set selected date in the editTextDob
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year1);
                    editTextDob.setText(selectedDate);
                }, year, month, day);
                datePickerDialog.show();
            }
        });


        // Get Material AutoCompleteTextViews for subjects and classes
        MaterialAutoCompleteTextView autoCompleteTextViewClass =
                dialogView.findViewById(R.id.autoCompleteTextViewStudentClass);

        // Get ChipGroups for displaying selected subjects and classes
        ChipGroup chipGroupClass = dialogView.findViewById(R.id.chipGroupClass);



        ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource(this,
                R.array.classes_array, android.R.layout.simple_spinner_item);
        autoCompleteTextViewClass.setAdapter(classAdapter);



        autoCompleteTextViewClass.setOnItemClickListener((parent, view, position, id) -> {
            // Add selected class as a chip
             selectedClass = parent.getItemAtPosition(position).toString();
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




                    // Add the student
                    addStudents(name, phone,selectedClass , studentParent, studentDOB, email,address,parentPhone,academicYear);


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

        // Access Firestore database instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (imageUri != null) {
            StorageReference storageReference = storage.getReference().child("student_images/" + System.currentTimeMillis() + ".jpg");
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Create user account
                        mAuth.createUserWithEmailAndPassword(email, generateRandomPassword())
                                .addOnCompleteListener(authTask -> {
                                    if (authTask.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            String userId = user.getUid();
                                            // Create a new Students object with the provided information
                                            Student student = new Student(name,userId,phone,studentClass,studentParent,studentDOB,email,address,parentPhone,academicYear,imageUrl);
                                            // Access the "students" collection in Firestore
                                            CollectionReference studentsRef = db.collection("students");

                                            // Add the student to Firestore with the user ID as the document ID
                                            studentsRef.document(userId)
                                                    .set(student)
                                                    .addOnSuccessListener(aVoid -> {
                                                        progressDialog.dismiss();
                                                        // Document successfully written
                                                        Log.d(TAG, "Student added with ID: " + userId);
                                                        // Optionally, show a success message to the user
                                                        Toast.makeText(ManageStudentsActivity.this, "Student added successfully", Toast.LENGTH_SHORT).show();
                                                        // Add student to the list
                                                        studentList.add(student);
                                                        adapter.notifyDataSetChanged();
                                                        // Send password reset email to the student
                                                        sendPasswordResetEmail(email);
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        progressDialog.dismiss();
                                                        // Handle any errors that may occur
                                                        Log.w(TAG, "Error adding student", e);
                                                        // Optionally, show an error message to the user
                                                        Toast.makeText(ManageStudentsActivity.this, "Failed to add student", Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    } else {
                                        // Account creation failed
                                        Log.w(TAG, "Failed to create account for " + email, authTask.getException());
                                        // Optionally, show an error message to the user
                                        Toast.makeText(ManageStudentsActivity.this, "Failed to create account for " + email, Toast.LENGTH_SHORT).show();
                                    }

                                    // Dismiss loading dialog
                                    progressDialog.dismiss();
                                });



                    }))
                    .addOnFailureListener(e -> Toast.makeText(ManageStudentsActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
                    progressDialog.dismiss();
        } else {
            // If no image selected, proceed with other student data
            // Create user account
            mAuth.createUserWithEmailAndPassword(email, generateRandomPassword())
                    .addOnCompleteListener(authTask -> {
                        progressDialog.dismiss();
                        if (authTask.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                // Create a new Students object with the provided information
                                Student student = new Student(name,userId,phone,studentClass,studentParent,studentDOB,email,address,parentPhone,academicYear);
                                // Access the "students" collection in Firestore
                                CollectionReference studentsRef = db.collection("students");

                                // Add the student to Firestore with the user ID as the document ID
                                studentsRef.document(userId)
                                        .set(student)
                                        .addOnSuccessListener(aVoid -> {
                                            // Document successfully written
                                            Log.d(TAG, "Student added with ID: " + userId);
                                            // Optionally, show a success message to the user
                                            Toast.makeText(ManageStudentsActivity.this, "Student added successfully", Toast.LENGTH_SHORT).show();
                                            // Add student to the list
                                            studentList.add(student);
                                            adapter.notifyDataSetChanged();
                                            // Send password reset email to the student
                                            sendPasswordResetEmail(email);
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                            // Handle any errors that may occur
                                            Log.w(TAG, "Error adding student", e);
                                            // Optionally, show an error message to the user
                                            Toast.makeText(ManageStudentsActivity.this, "Failed to add student", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // Account creation failed
                            Log.w(TAG, "Failed to create account for " + email, authTask.getException());
                            // Optionally, show an error message to the user
                            Toast.makeText(ManageStudentsActivity.this, "Failed to create account for " + email, Toast.LENGTH_SHORT).show();
                        }

                        // Dismiss loading dialog
                        progressDialog.dismiss();
                    });
        }


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

    @Override
    public void onItemClick(Student student) {
        Toast.makeText(this, student.getName(), Toast.LENGTH_SHORT).show();
        showEditStudentDialog(student);
    }

    @Override
    public void onItemLongClick(Student student) {
        showDeleteConfirmationDialog(student);
    }

    private void showDeleteConfirmationDialog(Student student) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Student "+student.getName())
                .setMessage("Do you want to delete this student?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteStudent(student))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deleteStudent(Student student) {
        ProgressDialog progressDialog = new ProgressDialog(ManageStudentsActivity.this);
        progressDialog.setMessage("Deleting student...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        db.collection("students").document(student.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    studentList.remove(student);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Student deleted", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting student", Toast.LENGTH_SHORT).show());
    }
}