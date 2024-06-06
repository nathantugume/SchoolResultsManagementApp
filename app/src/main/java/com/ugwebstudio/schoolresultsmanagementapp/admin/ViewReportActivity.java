package com.ugwebstudio.schoolresultsmanagementapp.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.StudentResults;
import com.ugwebstudio.schoolresultsmanagementapp.teacher.ManageResultsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewReportActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private FirebaseFirestore db;
    private TextView studentNameTxt;
    private List<Integer> aggregatesList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private static final Map<String, Integer> gradePoints = new HashMap<>();
    private float average;

    static {
        gradePoints.put("A", 1);
        gradePoints.put("B", 2);
        gradePoints.put("C", 3);
        gradePoints.put("D", 4);
        gradePoints.put("P", 8);
        gradePoints.put("F", 9);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);

        progressDialog = new ProgressDialog(ViewReportActivity.this);
        progressDialog.setMessage("Loading  results...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        db = FirebaseFirestore.getInstance();

        // Get table layout reference
        tableLayout = findViewById(R.id.table_layout);

        studentNameTxt = findViewById(R.id.student_name);

        // Get student results data from Intent
        Intent intent = getIntent();
        String selectedClass = intent.getStringExtra("selectedClass");
        String selectedTerm = intent.getStringExtra("selectedTerm");
        String studentId = intent.getStringExtra("studentId");

        // Fetch student results from Firestore
        fetchStudentResults(selectedClass, selectedTerm, studentId);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_app_bar);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home){
                startActivity(new Intent(ViewReportActivity.this, MainActivity.class));
            }
            if (item.getItemId() == R.id.bottom_report){
                startActivity(new Intent(ViewReportActivity.this, StudentReportActivity.class));

            }
            if (item.getItemId() == R.id.bottom_classes){
                startActivity(new Intent(ViewReportActivity.this, ManageClassesActivity.class));

            }
            if (item.getItemId() == R.id.bottom_results){
                startActivity(new Intent(ViewReportActivity.this, ManageResultsActivity.class));

            }

            return false;
        });
    }

    private void fetchStudentResults(String selectedClass, String selectedTerm, String studentId) {
        progressDialog.show();
        // Determine if this is for the third term
        if ("Third Term".equalsIgnoreCase(selectedTerm)) {
            // Fetch and calculate third-term results
            fetchAndCalculateThirdTerm(selectedClass, studentId);
        } else {
            // Fetch and display regular term results
            fetchAndDisplayRegularResults(selectedClass, selectedTerm, studentId);
        }


    }

    private void fetchAndDisplayRegularResults(String selectedClass, String selectedTerm, String studentId) {
        progressDialog.show();
        // Fetch and display regular term results
        db.collection("results")
                .whereEqualTo("className", selectedClass)
                .whereEqualTo("term", selectedTerm)
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Map<String, Map<String, Integer>> subjectResults = new HashMap<>();
                        task.getResult().forEach(document -> {
                            StudentResults studentResults = document.toObject(StudentResults.class);
                            studentNameTxt.setText(studentResults.getStudentName());

                            // Get subject and result type from Firestore document
                            String subject = document.getString("subject");
                            String resultType = document.getString("resultType");
                            int marks = studentResults.getMarks();

                            // Initialize the inner map if subject is encountered for the first time
                            subjectResults.putIfAbsent(subject, new HashMap<>());
                            subjectResults.get(subject).put(resultType, marks);
                        });

                        // Display regular term results
                        subjectResults.forEach((subject, resultTypes) -> addTableRow(subject, resultTypes));
                    } else {
                        Log.e("ViewReportActivity", "Error getting results: ", task.getException());
                    }
                });
    }

    private void fetchAndCalculateThirdTerm(String selectedClass, String studentId) {
        progressDialog.setMessage("Loading results...");
        progressDialog.show();
        // Fetch all terms for the selected student and class
        db.collection("results")
                .whereEqualTo("class", selectedClass)
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        Map<String, List<Integer>> subjectMarks = new HashMap<>();
                        task.getResult().forEach(document -> {
                            String term = document.getString("term");
                            String subject = document.getString("subject");
                            int marks = document.getLong("marks").intValue();

                            // Add marks to the subject
                            subjectMarks.computeIfAbsent(subject, k -> new ArrayList<>()).add(marks);
                        });

                        // Calculate and display third-term report
                        subjectMarks.forEach((subject, marksList) -> {
                            double average = marksList.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                            String grade = calculateGradeFromAverage(average);
                            addTableRow(subject, Collections.singletonMap("Third Term", (int) average), grade);
                        });
                    } else {
                        Log.e("ViewReportActivity", "Error getting results: ", task.getException());
                    }
                });
    }

    private String calculateGradeFromAverage(double average) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        progressDialog.setMessage("Loading results...");

        // Assuming 'gradingScales' is your Firestore collection containing grading scale documents
        db.collection("gradingScales")
                .orderBy("from")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        String grade = "Not Graded";

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if ("o_level".equalsIgnoreCase(document.getString("level"))) {
                                progressDialog.dismiss();
                                float minMark = document.getLong("from");
                                float maxMark = document.getLong("to");
                                if (average >= minMark && average <= maxMark) {
                                    grade = document.getString("grade");
                                    break;
                                }
                            }
                        }

                        // Use the grade here or pass it to another method for further processing
                        // For simplicity, let's just log the grade
                        Log.d("calculateGradeFromAverage", "Grade: " + grade);
                    } else {
                        Log.w("calculateGradeFromAverage", "Error getting documents.", task.getException());
                    }
                });

        return null; // Return null temporarily since we're using an asynchronous operation
    }

    private void addTableRow(String subject, Map<String, Integer> resultTypes) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        // Set the background color for the row
        setRowBackgroundColor(row, tableLayout.getChildCount());

        // Subject TextView
        TextView subjectTextView = createTextView(subject);
        row.addView(subjectTextView);

        // Beginning, Mid, End term EditTexts and final grade TextView
        EditText beginningEditText = createEditText(getMarksString(resultTypes, "Beginning of Term"));
        EditText midtermEditText = createEditText(getMarksString(resultTypes, "Midterm"));
        EditText endEditText = createEditText(getMarksString(resultTypes, "End of Term"));

        calculateGrades(getMarks(resultTypes, "Beginning of Term"), getMarks(resultTypes, "Midterm"), getMarks(resultTypes, "End of Term"), new GradeCallback() {
            @Override
            public void onGradeComputed(String grade, int aggregate, String division) {
                TextView gradeTextView = createTextView(grade);
                TextView aggregatesTxt = findViewById(R.id.aggregates);
                TextView divisionTxt = findViewById(R.id.report_division);
                // Ensure all UI updates are done on the main thread
                Collections.sort(aggregatesList);
                Collections.reverse(aggregatesList);
                int totalAggregate = 0;
                for (int i = 0; i < Math.min(8, aggregatesList.size()); i++) {
                    totalAggregate += aggregatesList.get(i);
                }
                String div = getDivision(totalAggregate);

                Log.d("aggregates", String.valueOf(aggregate));
                int finalTotalAggregate = totalAggregate;
                runOnUiThread(() -> {
                    row.addView(beginningEditText);
                    row.addView(midtermEditText);
                    row.addView(endEditText);
                    row.addView(gradeTextView); // Updated to include division
                    aggregatesTxt.setText(String.valueOf(finalTotalAggregate));
                    divisionTxt.setText(div);


                    // Add the row to the TableLayout
                    tableLayout.addView(row);
                });
            }
        });

    }
    // Overloaded addTableRow method with grade parameter
    private void addTableRow(String subject, Map<String, Integer> resultTypes, String grade) {
        TableRow row = new TableRow(this);
        // Set up row...

        // Add views for subject, result types, and grade
        TextView subjectTextView = createTextView(subject);
        row.addView(subjectTextView);
        resultTypes.forEach((resultType, marks) -> {
            EditText editText = createEditText(String.valueOf(marks));
            row.addView(editText);
        });
        TextView gradeTextView = createTextView(grade);
        row.addView(gradeTextView);

        // Add row to table layout
        tableLayout.addView(row);
    }
    private String getMarksString(Map<String, Integer> resultTypes, String resultType) {
        return resultTypes.containsKey(resultType) ? String.valueOf(resultTypes.get(resultType)) : "-";
    }

    private int getMarks(Map<String, Integer> resultTypes, String resultType) {
        return resultTypes.getOrDefault(resultType, 0);
    }



    private void setRowBackgroundColor(TableRow row, int childCount) {
        if (childCount % 2 == 0) {
            row.setBackgroundColor(ContextCompat.getColor(this, R.color.row_color_even));
        } else {
            row.setBackgroundColor(ContextCompat.getColor(this, R.color.row_color_odd));
        }
    }


    public interface GradeCallback {
        void onGradeComputed(String grade, int aggregate, String division);
    }


    private void calculateGrades(int beginningMarks, int midMarks, int endMarks, GradeCallback callback) {
        float average = ((float) beginningMarks + (float) midMarks + (float) endMarks) / 3;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("gradingScales")
                .orderBy("from")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String[] grades = {"Not Graded"};
                        int aggregate = 0;


                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if ("o_level".equalsIgnoreCase(document.getString("level"))) {
                                float minMark = document.getLong("from");
                                float maxMark = document.getLong("to");
                                if (average >= minMark && average <= maxMark) {
                                    grades[0] = document.getString("grade");
                                    aggregate = calculateAggregate(grades[0]); // Calculate aggregate for this grade
                                    aggregatesList.add(aggregate);

                                    break;
                                }
                            }
                        }


                        String division = getDivision(aggregate); // Determine the division based on the aggregate
                        callback.onGradeComputed(grades[0], aggregate, division);
                    } else {
                        Log.w("calculateGrades", "Error getting documents.", task.getException());
                    }
                });
    }

    private String getDivision(int totalAggregate) {
        if (totalAggregate <= 32) {
            return "Division I";
        } else if (totalAggregate <= 45) {
            return "Division II";
        } else if (totalAggregate <= 58) {
            return "Division III";
        } else if (totalAggregate <= 72) {
            return "Division IV";
        } else {
            return "Ungraded";
        }
    }


    private int calculateAggregate(String grade) {
        int aggregate = 0;
        if (gradePoints.containsKey(grade)) {
            aggregate = gradePoints.get(grade);
        } else {
            Log.e("calculateAggregate", "Invalid grade: " + grade);
        }
        return aggregate;
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textView.setPadding(8, 8, 8, 8);
        textView.setTextColor(ContextCompat.getColor(this, R.color.black));
        return textView;
    }

    private EditText createEditText(String text) {
        EditText editText = new EditText(this);
        editText.setText(text);
        editText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        editText.setPadding(8, 8, 8, 8);
        editText.setTextColor(ContextCompat.getColor(this, R.color.black));
        return editText;
    }


    private void applyThirdTermGrades(Map<String, List<Integer>> subjectMarks) {
        for (Map.Entry<String, List<Integer>> entry : subjectMarks.entrySet()) {
            double average = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0);
            String grade = calculateGradesFromAverage(average, new GradeCallback() {
                @Override
                public void onGradeComputed(String grade, int aggregate, String division) {
                     aggregate = calculateAggregate(grade);
                }
            });


            // Update UI for third term using calculated average
            runOnUiThread(() -> {
                addTableRow(entry.getKey(), Collections.singletonMap("Third Term", (int) average));
            });
        }
    }



    private String calculateGradesFromAverage(double average, GradeCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("gradingScales")
                .orderBy("from")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String[] grades = {"Not Graded"};
                        int aggregate = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if ("o_level".equalsIgnoreCase(document.getString("level"))) {
                                float minMark = document.getLong("from");
                                float maxMark = document.getLong("to");
                                if (average >= minMark && average <= maxMark) {
                                    grades[0] = document.getString("grade");
                                    aggregate = calculateAggregate(grades[0]);
                                    aggregatesList.add(aggregate);
                                    break;
                                }
                            }
                        }

                        String division = getDivision(aggregate);
                        callback.onGradeComputed(grades[0], aggregate, division);
                    } else {
                        Log.w("calculateGrades", "Error getting documents.", task.getException());
                    }
                });
        return null;
    }




}