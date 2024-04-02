package com.ugwebstudio.schoolresultsmanagementapp.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.StudentClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageResultsActivity extends AppCompatActivity {

    private Spinner spinnerClass, spinnerStudent, spinnerTerms;
    private MaterialButton buttonAddResult;

    private FirebaseFirestore db;

    private StudentClass studentClass;
    private AutoCompleteTextView subjectAutoComplete;

    private ChipGroup chipGroupResultType;
    private String studentId;

    private List<String> studentIds;
    private TextInputEditText marksEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_results);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerStudent = findViewById(R.id.spinnerStudent);
        buttonAddResult = findViewById(R.id.buttonAddResult);
        spinnerTerms = findViewById(R.id.spinnerTerm);
        subjectAutoComplete = findViewById(R.id.autoCompleteTextViewSubject);
        marksEditText = findViewById(R.id.editTextMarks);

// Find the ChipGroup in your layout
        chipGroupResultType = findViewById(R.id.chipGroupResultType);

// Set an OnCheckedChangeListener to the ChipGroup
        chipGroupResultType.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                // Loop through each chip in the group
                for (int i = 0; i < group.getChildCount(); i++) {
                    Chip chip = (Chip) group.getChildAt(i);
                    // If the chip is checked and its ID is not the same as the checkedId,
                    // deselect it
                    if (chip.getId() != checkedId) {
                        chip.setChecked(false);
                    } else {
                        // Update the chip background to show the tick
                        chip.setCheckedIconVisible(true);
                    }
                }
            }
        });



        // Inside onCreate method or wherever appropriate
        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedClass = parentView.getItemAtPosition(position).toString();
                loadStudentsFromFirestore(selectedClass);
                loadSubjectsFromFirestore();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing if no class is selected
            }
        });


        // Load classes from Firestore
        loadClassesFromFirestore();
        loadTerms();

        // Add click listener to the Add Result button
        buttonAddResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add your logic to add the result to Firestore
                addResultToFirestore();
            }
        });
    }


    // Method to load classes from Firestore and populate spinner
    private void loadClassesFromFirestore() {
        db.collection("classes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> classNames = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                 studentClass = document.toObject(StudentClass.class);
                                classNames.add(document.getString("className"));

                            }
                            // Populate spinner with class names
                            ArrayAdapter<String> classAdapter = new ArrayAdapter<>(ManageResultsActivity.this, android.R.layout.simple_spinner_item, classNames);
                            classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerClass.setAdapter(classAdapter);


                        } else {
                            Toast.makeText(ManageResultsActivity.this, "Failed to load classes", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Define the method to load students from Firestore and populate spinner based on selected class
    private void loadStudentsFromFirestore(String selectedClass) {
        db.collection("students")
                .whereEqualTo("studentClass", selectedClass)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> studentNames = new ArrayList<>();
                        studentIds = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            studentNames.add(document.getString("name"));
                            studentIds.add(document.getId());

                        }
                        // Populate spinner with student names
                        ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(ManageResultsActivity.this, android.R.layout.simple_spinner_item, studentNames);
                        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerStudent.setAdapter(studentAdapter);
                    } else {
                        Toast.makeText(ManageResultsActivity.this, "Failed to load students", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void loadSubjectsFromFirestore() {
        // Define your array of subjects for O level and A level
        String[] subjectsArray = {
                "Mathematics", "English Language", "Physics", "Chemistry", "Biology", "Geography", "History", "Commerce", "Accounts", "Divinity", "Agriculture", "Fine Art", // O level subjects
                "General Paper",  "Economics", "Art", "Literature in English", "Entrepreneurship", "Computer Studies" // A level subjects
        };

// Create an ArrayAdapter for the subjects array
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(ManageResultsActivity.this, android.R.layout.simple_dropdown_item_1line, subjectsArray);

// Set the adapter to your AutoCompleteTextView
        subjectAutoComplete.setAdapter(subjectAdapter);

    }

    private void loadTerms() {
        // Define your array of subjects for O level and A level
        String[] termsArray = {
                 "Term One", "Term Two", "Term Three" // A level subjects
        };

// Create an ArrayAdapter for the subjects array
        ArrayAdapter<String> termsAdapter = new ArrayAdapter<>(ManageResultsActivity.this, android.R.layout.simple_dropdown_item_1line, termsArray);

// Set the adapter to your AutoCompleteTextView
        spinnerTerms.setAdapter(termsAdapter);

    }


    private void addResultToFirestore() {
        // Get selected values
        String selectedClass = spinnerClass.getSelectedItem().toString();
        String selectedStudent = spinnerStudent.getSelectedItem().toString();
        studentId = studentIds.get(spinnerStudent.getSelectedItemPosition());
        String selectedSubject = subjectAutoComplete.getText().toString();
        String selectedTerm = spinnerTerms.getSelectedItem().toString();
        String selectedResultType = getSelectedResultType();
        int marks = Integer.parseInt(marksEditText.getText().toString().trim());


        // Perform validation if needed
        validateInputs();

        // Create a map to store the result data
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("class", selectedClass);
        resultData.put("student", selectedStudent);
        resultData.put("subject", selectedSubject);
        resultData.put("term", selectedTerm);
        resultData.put("resultType", selectedResultType);
        resultData.put("studentId", studentId);
        resultData.put("marks",marks);

        // Add the result data to Firestore
        db.collection("results")
                .add(resultData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ManageResultsActivity.this, "Result added successfully", Toast.LENGTH_SHORT).show();
                    // Clear input fields or perform any other necessary actions
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ManageResultsActivity.this, "Failed to add result", Toast.LENGTH_SHORT).show();
                });
    }

    private String getSelectedResultType() {
        Chip selectedChip = findViewById(chipGroupResultType.getCheckedChipId());
        return selectedChip != null ? selectedChip.getText().toString() : "";
    }

    private boolean validateInputs() {
        // Validate selected class
        String selectedClass = spinnerClass.getSelectedItem().toString();
        if (selectedClass.isEmpty()) {
            Toast.makeText(this, "Please select a class", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate selected student
        String selectedStudent = spinnerStudent.getSelectedItem().toString();
        if (selectedStudent.isEmpty()) {
            Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate selected subject
        String selectedSubject = subjectAutoComplete.getText().toString().trim();
        if (selectedSubject.isEmpty()) {
            Toast.makeText(this, "Please enter a subject", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate selected term
        String selectedTerm = spinnerTerms.getSelectedItem().toString();
        if (selectedTerm.isEmpty()) {
            Toast.makeText(this, "Please select a term", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate selected result type
        String selectedResultType = getSelectedResultType();
        if (selectedResultType.isEmpty()) {
            Toast.makeText(this, "Please select a result type", Toast.LENGTH_SHORT).show();
            return false;
        }
        String marks = marksEditText.getText().toString();

        if (marks.isEmpty()){
            Toast.makeText(this, "Please Enter the marks", Toast.LENGTH_SHORT).show();
            marksEditText.requestFocus();
            marksEditText.setError("Please Enter the marks");
            return false;
        }

        return true;
    }

}
