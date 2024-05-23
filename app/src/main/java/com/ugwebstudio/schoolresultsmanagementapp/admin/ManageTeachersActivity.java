package com.ugwebstudio.schoolresultsmanagementapp.admin;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.Adapters.TeacherAdapter;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.Teacher;
import com.ugwebstudio.schoolresultsmanagementapp.teacher.ManageResultsActivity;

import java.util.ArrayList;
import java.util.List;

public class ManageTeachersActivity extends AppCompatActivity implements TeacherAdapter.OnItemClickListener,TeacherAdapter.OnItemLongClickListener {
    private final String TAG = "ManageTeachersActivity";
    private FirebaseAuth mAuth;
    private TeacherAdapter adapter;
    private List<Teacher> teacherList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_teachers);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_app_bar);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home){
                startActivity(new Intent(ManageTeachersActivity.this, MainActivity.class));
            }
            if (item.getItemId() == R.id.bottom_report){
                startActivity(new Intent(ManageTeachersActivity.this, StudentReportActivity.class));

            }
            if (item.getItemId() == R.id.bottom_classes){
                startActivity(new Intent(ManageTeachersActivity.this, ManageClassesActivity.class));

            }
            if (item.getItemId() == R.id.bottom_results){
                startActivity(new Intent(ManageTeachersActivity.this, ManageResultsActivity.class));

            }

            return false;
        });


        mAuth = FirebaseAuth.getInstance();

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        RecyclerView recyclerView;

        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.recyclerViewTeachers);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        teacherList = new ArrayList<>();
        adapter = new TeacherAdapter(teacherList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemLongClickListener(this);
        adapter.setOnItemClickListener(this);
        // Retrieve teacher data from Firestore
        retrieveTeacherData();
        // Inside your activity onCreate() method or wherever you set up your views
        FloatingActionButton fabAddTeacher = findViewById(R.id.fabAddTeacher);
        fabAddTeacher.setOnClickListener(v -> showAddTeacherDialog());

    }

    private void retrieveTeacherData() {
        // Access the "teachers" collection in Firestore
        CollectionReference teachersRef = db.collection("teachers");

        // Retrieve all documents from the "teachers" collection
        teachersRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Clear the existing list
                        teacherList.clear();

                        // Add retrieved documents to the list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Teacher teacher = document.toObject(Teacher.class);
                            String id = document.getId();
                            teacher.setId(id);
                            teacherList.add(teacher);
                        }

                        // Notify the adapter that the data set has changed
                        adapter.notifyDataSetChanged();
                    } else {
                        // Error retrieving documents
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(ManageTeachersActivity.this, "Failed to retrieve teacher data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddTeacherDialog() {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_teacher, null);

        // Get Material TextInputLayouts and TextInputEditTexts
        TextInputEditText editTextName = dialogView.findViewById(R.id.editTextName);
        TextInputEditText editTextEmail = dialogView.findViewById(R.id.editTextEmail);
        TextInputEditText editTextPhone = dialogView.findViewById(R.id.editTextPhone);

        // Get Material AutoCompleteTextViews for subjects and classes
        MaterialAutoCompleteTextView autoCompleteTextViewSubject = dialogView.findViewById(R.id.autoCompleteTextViewSubject);
        MaterialAutoCompleteTextView autoCompleteTextViewClass = dialogView.findViewById(R.id.autoCompleteTextViewClass);

        // Get ChipGroups for displaying selected subjects and classes
        ChipGroup chipGroupSubject = dialogView.findViewById(R.id.chipGroupSubject);
        ChipGroup chipGroupClass = dialogView.findViewById(R.id.chipGroupClass);

        // Populate Material AutoCompleteTextViews with data
        ArrayAdapter<CharSequence> subjectAdapter = ArrayAdapter.createFromResource(this,
                R.array.subjects_array, android.R.layout.simple_spinner_item);
        autoCompleteTextViewSubject.setAdapter(subjectAdapter);

        ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource(this,
                R.array.classes_array, android.R.layout.simple_spinner_item);
        autoCompleteTextViewClass.setAdapter(classAdapter);

        // Set up click listeners for selecting subjects and classes
        autoCompleteTextViewSubject.setOnItemClickListener((parent, view, position, id) -> {
            // Add selected subject as a chip
            String selectedSubject = parent.getItemAtPosition(position).toString();
            Chip chip = new Chip(chipGroupSubject.getContext());
            chip.setText(selectedSubject);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> chipGroupSubject.removeView(chip));
            chipGroupSubject.addView(chip);
            autoCompleteTextViewSubject.setText("");
        });

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
                .setTitle("Add Teacher")
                .setPositiveButton("Add", (dialog, which) -> {
                    // Handle Add button click
                    String name = editTextName.getText().toString().trim();
                    String email = editTextEmail.getText().toString().trim();
                    String phone = editTextPhone.getText().toString().trim();


                    // Get selected subjects
                    StringBuilder subjectsBuilder = new StringBuilder();
                    for (int i = 0; i < chipGroupSubject.getChildCount(); i++) {
                        Chip chip = (Chip) chipGroupSubject.getChildAt(i);
                        subjectsBuilder.append(chip.getText());
                        if (i < chipGroupSubject.getChildCount() - 1) {
                            subjectsBuilder.append(", ");
                        }
                    }
                    String subjects = subjectsBuilder.toString();

                    // Get selected classes
                    StringBuilder classesBuilder = new StringBuilder();
                    for (int i = 0; i < chipGroupClass.getChildCount(); i++) {
                        Chip chip = (Chip) chipGroupClass.getChildAt(i);
                        classesBuilder.append(chip.getText());
                        if (i < chipGroupClass.getChildCount() - 1) {
                            classesBuilder.append(", ");
                        }
                    }
                    String teacherClass = classesBuilder.toString();

                    // Add the teacher
                    addTeacher(name, email, phone, subjects, teacherClass);

                    // Dismiss the dialog
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle Cancel button click
                        dialog.dismiss();
                    }
                });

        // Show the AlertDialog
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


    private void addTeacher(String name, String email, String phone, String subjects, String teacherClass) {
        // Create a new Teacher object with the provided information
        Teacher teacher = new Teacher(name, email, phone, subjects, teacherClass,"teacher");

        // Access Firestore database instance

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Access the "teachers" collection in Firestore
        CollectionReference teachersRef = db.collection("teachers");

        // Add the teacher to Firestore
        teachersRef.add(teacher)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        String password = generateRandomPassword();

                        // DocumentSnapshot added successfully
                        Log.d(TAG, "Teacher added with ID: " + documentReference.getId());
                        // Optionally, show a success message to the user
                        Toast.makeText(ManageTeachersActivity.this, "Teacher added successfully", Toast.LENGTH_SHORT).show();

                        // Create account for the teacher with the generated password
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task authTask) {
                                        if (authTask.isSuccessful()) {
                                            // Account created successfully
                                            Log.d(TAG, "Account created successfully for " + email);

                                            // Send password reset email to the teacher
                                            sendPasswordResetEmail(email);
                                        } else {
                                            // Account creation failed
                                            Log.w(TAG, "Failed to create account for " + email, authTask.getException());
                                        }
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors that may occur
                        Log.w(TAG, "Error adding teacher", e);
                        // Optionally, show an error message to the user
                        Toast.makeText(ManageTeachersActivity.this, "Failed to add teacher", Toast.LENGTH_SHORT).show();
                    }
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
                            Toast.makeText(ManageTeachersActivity.this, "Teacher added successfully. Password reset email sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to send password reset email
                            Log.w(TAG, "Failed to send password reset email to " + email, task.getException());
                            Toast.makeText(ManageTeachersActivity.this, "Failed to add teacher. Failed to send password reset email.", Toast.LENGTH_SHORT).show();
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
    public void onItemClick(Teacher teacher) {
        showEditTeacherDialog(teacher);

    }

    //edit teacher show dialogue

    private void showEditTeacherDialog(Teacher teacher) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_teacher, null);

        TextInputEditText editTextName = dialogView.findViewById(R.id.editTextEditName);
        TextInputEditText editTextEmail = dialogView.findViewById(R.id.editTextEditEmail);
        TextInputEditText editTextPhone = dialogView.findViewById(R.id.editTextEditPhone);
        // Populate fields with teacher data
        editTextName.setText(teacher.getName());
        editTextEmail.setText(teacher.getEmail());
        editTextPhone.setText(teacher.getPhone());

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView)
                .setTitle("Edit Teacher")
                .setPositiveButton("Save", (dialog, which) -> {
                    // Update teacher information
                    String name = editTextName.getText().toString().trim();
                    String email = editTextEmail.getText().toString().trim();
                    String phone = editTextPhone.getText().toString().trim();
                    updateTeacher(teacher.getId(), name, email, phone);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void updateTeacher(String id, String name, String email, String phone) {
        DocumentReference teacherRef = db.collection("teachers").document(id);
        teacherRef.update("name", name, "email", email, "phone", phone)
                .addOnSuccessListener(aVoid -> {
                    // Update local list and notify adapter
                    for (Teacher teacher : teacherList) {
                        if (teacher.getId().equals(id)) {
                            teacher.setName(name);
                            teacher.setEmail(email);
                            teacher.setPhone(phone);
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Teacher updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating teacher", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onItemLongClick(Teacher teacher) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Teacher "+ teacher.getName())
                .setMessage("Do you want to delete this teacher?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteTeacher(teacher))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deleteTeacher(Teacher teacher) {
        DocumentReference teacherRef = db.collection("teachers").document(teacher.getId());
        teacherRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from local list and notify adapter
                    teacherList.remove(teacher);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Teacher deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting teacher", Toast.LENGTH_SHORT).show();
                });
    }
}