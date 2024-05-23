package com.ugwebstudio.schoolresultsmanagementapp.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.Adapters.StudentClassAdapter;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.StudentClass;
import com.ugwebstudio.schoolresultsmanagementapp.teacher.ManageResultsActivity;

import java.util.ArrayList;
import java.util.List;

public class ManageClassesActivity extends AppCompatActivity {
    private final String TAG = "ManageClassesActivity";

    private StudentClassAdapter adapter;
    private List<StudentClass> classList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_classes);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_app_bar);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.bottom_home){
                    startActivity(new Intent(ManageClassesActivity.this, MainActivity.class));
                }
                if (item.getItemId() == R.id.bottom_report){
                    startActivity(new Intent(ManageClassesActivity.this, StudentReportActivity.class));

                }
                if (item.getItemId() == R.id.bottom_classes){
                    startActivity(new Intent(ManageClassesActivity.this, ManageClassesActivity.class));

                }
                if (item.getItemId() == R.id.bottom_results){
                    startActivity(new Intent(ManageClassesActivity.this, ManageResultsActivity.class));

                }

                return false;
            }
        });

        db = FirebaseFirestore.getInstance();

        RecyclerView recyclerView = findViewById(R.id.recyclerViewClasses);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        classList = new ArrayList<>();
        adapter = new StudentClassAdapter(classList);
        recyclerView.setAdapter(adapter);

        retrieveClassesData();

        FloatingActionButton fabAddClass = findViewById(R.id.fabAddClass);
        fabAddClass.setOnClickListener(v -> showAddClassDialog());
    }

    private void retrieveClassesData() {
        CollectionReference classesRef = db.collection("classes");
        classesRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        classList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            StudentClass studentClass = document.toObject(StudentClass.class);
                            classList.add(studentClass);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(ManageClassesActivity.this, "Failed to retrieve class data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddClassDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_class, null);

        TextInputEditText editTextClassName = dialogView.findViewById(R.id.editTextClassName);
        TextInputEditText editTextClassLevel = dialogView.findViewById(R.id.editTextClassLevel);
        TextInputEditText editTextStreams = dialogView.findViewById(R.id.editTextStrams);
        MaterialAutoCompleteTextView autoCompleteTextViewSubject = dialogView.findViewById(R.id.autoCompleteTextViewSubject);
        ChipGroup chipGroupSubject = dialogView.findViewById(R.id.chipGroupSubject);

        ArrayAdapter<CharSequence> subjectAdapter = ArrayAdapter.createFromResource(this,
                R.array.subjects_array, android.R.layout.simple_spinner_item);
        autoCompleteTextViewSubject.setAdapter(subjectAdapter);

        autoCompleteTextViewSubject.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSubject = parent.getItemAtPosition(position).toString();
            Chip chip = new Chip(chipGroupSubject.getContext());
            chip.setText(selectedSubject);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> chipGroupSubject.removeView(chip));
            chipGroupSubject.addView(chip);
            autoCompleteTextViewSubject.setText("");
        });

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView)
                .setTitle("Add Class")
                .setPositiveButton("Add", (dialog, which) -> {
                    String className = editTextClassName.getText().toString().trim();
                    String classLevel = editTextClassLevel.getText().toString().trim();
                    String streams = editTextStreams.getText().toString();

                    // Get selected subjects
                    StringBuilder subjectsBuilder = new StringBuilder();
                    for (int i = 0; i < chipGroupSubject.getChildCount(); i++) {
                        Chip chip = (Chip) chipGroupSubject.getChildAt(i);
                        subjectsBuilder.append(chip.getText());
                        if (i < chipGroupSubject.getChildCount() - 1) {
                            subjectsBuilder.append(", ");
                        }
                    }
                    String classSubjects = subjectsBuilder.toString();
                    addClass(className, streams, classLevel,classSubjects);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void addClass(String className, String classLevel, String streams, String classSubjects) {
        StudentClass studentClass = new StudentClass(className, streams, classLevel, classSubjects);
        CollectionReference classesRef = db.collection("classes");
        classesRef.add(studentClass)
                .addOnSuccessListener(documentReference -> {
            Log.d(TAG, "Class added with ID: " + documentReference.getId());
            Toast.makeText(ManageClassesActivity.this, "Class added successfully", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();

        })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding class", e);
                    Toast.makeText(ManageClassesActivity.this, "Failed to add class", Toast.LENGTH_SHORT).show();
                });
    }
}
