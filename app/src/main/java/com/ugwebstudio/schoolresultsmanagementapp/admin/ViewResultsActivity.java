package com.ugwebstudio.schoolresultsmanagementapp.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.Adapters.StudentResultAdapter;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.StudentClass;
import com.ugwebstudio.schoolresultsmanagementapp.classes.StudentResults;

import java.util.ArrayList;
import java.util.List;

public class ViewResultsActivity extends AppCompatActivity {

    private Spinner spinnerClass, spinnerTerms;
    private TextInputEditText editTextSearch;
    private RecyclerView recyclerViewStudents;
    private FirebaseFirestore db;
    private StudentResultAdapter adapter;
    private List<StudentResults> studentResultsList = new ArrayList<>();
    private String selectedClass;
    private String selectedTerm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_results);

        spinnerClass = findViewById(R.id.spinner_class);
        spinnerTerms = findViewById(R.id.spinner_term);
        editTextSearch = findViewById(R.id.editText_search);
        recyclerViewStudents = findViewById(R.id.recycler_view_students);
        db = FirebaseFirestore.getInstance();


        loadClassesFromFirestore();
        setupRecyclerView();


        // Listeners for class and term selection
        spinnerClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedClass = parent.getItemAtPosition(position).toString();
                loadTerms();
                loadStudentResultsFromFirestore(selectedClass, selectedTerm);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if no class is selected
            }
        });

        spinnerTerms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 selectedTerm = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing if no term is selected
            }
        });

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

    private void loadStudentResultsFromFirestore(String selectedClass, String selectedTerm) {
        db.collection("results")
                .whereEqualTo("class", selectedClass)
                .whereEqualTo("term", selectedTerm)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> uniqueStudents = new ArrayList<>();
                            studentResultsList.clear(); // Clear the existing list
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                StudentResults studentResults = document.toObject(StudentResults.class);
                                studentResults.setStudentClass(document.getString("class"));
                                String studentName = studentResults.getStudent();
                                if (!uniqueStudents.contains(studentName)) {
                                    uniqueStudents.add(studentName);
                                    studentResultsList.add(studentResults);
                                    adapter.updateList(studentResultsList);
                                    Log.d("FirestoreResults", studentName);
                                }
                            }
                            Log.d("FirestoreResults", "Total unique students: " + uniqueStudents.size());
                            adapter.notifyDataSetChanged(); // Notify the adapter of data change
                        } else {
                            Toast.makeText(ViewResultsActivity.this, "Failed to load student results", Toast.LENGTH_SHORT).show();
                            Log.e("FirestoreResults", "Failed to load student results", task.getException());
                        }
                    }
                });
    }



    private void setupRecyclerView() {
        adapter = new StudentResultAdapter(studentResultsList);
        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewStudents.setAdapter(adapter);
        adapter.updateList(studentResultsList);

    }

    private void filterStudentResults(String searchText) {
        adapter.filterList(searchText);
    }

    // Method to load terms
    private void loadTerms() {
        String[] termsArray = {
                "Term One", "Term Two", "Term Three"
        };
        ArrayAdapter<String> termsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, termsArray);
        termsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTerms.setAdapter(termsAdapter);
    }

    // Method to load classes
    private void loadClassesFromFirestore() {
        db.collection("classes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> classNames = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                StudentClass studentClass = document.toObject(StudentClass.class);
                                classNames.add(studentClass.getClassName());
                            }
                            ArrayAdapter<String> classAdapter = new ArrayAdapter<>(ViewResultsActivity.this, android.R.layout.simple_spinner_item, classNames);
                            classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerClass.setAdapter(classAdapter);


                        } else {
                            Toast.makeText(ViewResultsActivity.this, "Failed to load classes", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
