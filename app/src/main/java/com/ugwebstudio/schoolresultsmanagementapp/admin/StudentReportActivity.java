package com.ugwebstudio.schoolresultsmanagementapp.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.Adapters.StudentReportsAdapter;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.StudentClass;
import com.ugwebstudio.schoolresultsmanagementapp.classes.StudentResults;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudentReportActivity extends AppCompatActivity {

    private Spinner spinnerClass, spinnerTerms, spinnerYear;
    private TextInputEditText editTextSearch;
    private RecyclerView recyclerViewStudents;
    private FirebaseFirestore db;
    private StudentReportsAdapter adapter;
    private List<StudentResults> studentResultsList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String selectedClass;
    private String selectedTerm;
    private String selectedYear;
    private TextView txt_no_students;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_report);

        progressDialog = new ProgressDialog(StudentReportActivity.this);
        progressDialog.setMessage("Loading students...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        spinnerClass = findViewById(R.id.spinner_class);
        spinnerTerms = findViewById(R.id.spinner_term);
        spinnerYear = findViewById(R.id.spinner_year);
        editTextSearch = findViewById(R.id.editText_search);
        recyclerViewStudents = findViewById(R.id.recycler_view_students);
        db = FirebaseFirestore.getInstance();
        txt_no_students = findViewById(R.id.txt_no_students);
        txt_no_students.setVisibility(View.GONE);

        loadClassesFromFirestore();
        loadYearsFromFirestore();
        loadTerms();
        setupRecyclerView();

        // Listeners for class, term, and year selection
        AdapterView.OnItemSelectedListener selectionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getId() == R.id.spinner_class ){
                    selectedClass = parent.getItemAtPosition(position).toString();
                    loadStudentResultsFromFirestore(selectedClass, selectedTerm, selectedYear);
                }

                if (parent.getId() == R.id.spinner_term){
                    selectedTerm = parent.getItemAtPosition(position).toString();
                    loadStudentResultsFromFirestore(selectedClass, selectedTerm, selectedYear);
                }
                if (parent.getId() == R.id.spinner_year){
                    selectedYear = parent.getItemAtPosition(position).toString();
                    loadStudentResultsFromFirestore(selectedClass, selectedTerm, selectedYear);
                }



            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if no selection is made
            }
        };

        spinnerClass.setOnItemSelectedListener(selectionListener);
        spinnerTerms.setOnItemSelectedListener(selectionListener);
        spinnerYear.setOnItemSelectedListener(selectionListener);

        // Listener for search input
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudentResults(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });
    }

    private void loadStudentResultsFromFirestore(String selectedClass, String selectedTerm, String selectedYear) {
        if (selectedClass == null || selectedTerm == null || selectedYear == null) {
            progressDialog.dismiss();
            return;
        }

        progressDialog.setMessage("Loading students...");
        progressDialog.show();
        studentResultsList.clear(); // Clear the existing list

        db.collection("results")
                .whereEqualTo("className", selectedClass)
                .whereEqualTo("term", selectedTerm)
                .whereEqualTo("year", selectedYear)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        txt_no_students.setVisibility(View.GONE);
                        List<String> uniqueStudents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            StudentResults studentResults = document.toObject(StudentResults.class);
                            studentResults.setStudentClass(document.getString("className"));
                            studentResults.setName(document.getString("studentName"));
                            String studentName = studentResults.getName();
                            if (!uniqueStudents.contains(studentName)) {
                                uniqueStudents.add(studentName);
                                studentResultsList.add(studentResults);
                            }
                        }
                        if (uniqueStudents.size() < 1) {
                            txt_no_students.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "No students found!!", Toast.LENGTH_SHORT).show();
                        }
                        adapter.updateList(studentResultsList);
                    } else {
                        Toast.makeText(StudentReportActivity.this, "Failed to load student results", Toast.LENGTH_SHORT).show();
                        Log.e("FirestoreResults", "Failed to load student results", task.getException());
                    }
                });
    }

    private void setupRecyclerView() {
        adapter = new StudentReportsAdapter(studentResultsList);
        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewStudents.setAdapter(adapter);
        adapter.updateList(studentResultsList);
    }

    private void filterStudentResults(String searchText) {
        adapter.filterList(searchText);
    }

    // Method to load terms
    private void loadTerms() {
        String[] termsArray = {"Term One", "Term Two", "Term Three"};
        ArrayAdapter<String> termsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, termsArray);
        termsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTerms.setAdapter(termsAdapter);
    }

    // Method to load classes
    private void loadClassesFromFirestore() {
        progressDialog.setMessage("Loading Classes...");
        progressDialog.show();

        db.collection("classes")
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        List<String> classNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            StudentClass studentClass = document.toObject(StudentClass.class);
                            classNames.add(studentClass.getClassName());
                        }
                        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(StudentReportActivity.this, android.R.layout.simple_spinner_item, classNames);
                        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerClass.setAdapter(classAdapter);
                    } else {
                        Toast.makeText(StudentReportActivity.this, "Failed to load classes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to load years
    private void loadYearsFromFirestore() {
        progressDialog.setMessage("Loading Years...");
        progressDialog.show();

        db.collection("students")
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Set<String> years = new HashSet<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String year = document.getString("academicYear");
                            if (year != null) {
                                years.add(year);
                            }
                        }
                        List<String> yearList = new ArrayList<>(years);
                        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(StudentReportActivity.this, android.R.layout.simple_spinner_item, yearList);
                        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerYear.setAdapter(yearAdapter);
                    } else {
                        Toast.makeText(StudentReportActivity.this, "Failed to load years", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
