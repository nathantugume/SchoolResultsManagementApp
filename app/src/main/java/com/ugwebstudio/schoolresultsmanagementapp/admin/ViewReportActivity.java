package com.ugwebstudio.schoolresultsmanagementapp.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.StudentResults;

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
    }

    private void fetchStudentResults(String selectedClass, String selectedTerm, String studentId) {

        // Determine if this is for the third term
        boolean isThirdTerm = "Third Term".equalsIgnoreCase(selectedTerm);
        if (isThirdTerm) {

            // If it's the third term, fetch results from all terms to calculate the cumulative average
            String[] terms = {"First Term", "Second Term", "Third Term"};
            Map<String, List<Integer>> subjectMarks = new HashMap<>();
            AtomicInteger termsProcessed = new AtomicInteger(0);

            for (String term : terms) {
                db.collection("results")
                        .whereEqualTo("class", selectedClass)
                        .whereEqualTo("term", term)
                        .whereEqualTo("studentId", studentId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String subject = document.getString("subject");
                                    int marks = document.getLong("marks").intValue();
                                    subjectMarks.computeIfAbsent(subject, k -> new ArrayList<>()).add(marks);
                                }

                                // After all terms are processed, calculate the grade for the third term
                                if (termsProcessed.incrementAndGet() == terms.length) {
                                    applyThirdTermGrades(subjectMarks);
                                }
                            } else {
                                Log.e("ViewReportActivity", "Error getting results: ", task.getException());
                            }
                        });


            }
        } else {
            // Query Firestore for student results
            db.collection("results")
                    .whereEqualTo("class", selectedClass)
                    .whereEqualTo("term", selectedTerm)
                    .whereEqualTo("studentId", studentId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Map<String, Map<String, Integer>> subjectResults = new HashMap<>();

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    StudentResults studentResults = document.toObject(StudentResults.class);
                                    studentNameTxt.setText(studentResults.getStudent());

                                    // Get subject and result type from Firestore document
                                    String subject = document.getString("subject");
                                    String resultType = document.getString("resultType");
                                    int marks = studentResults.getMarks();

                                    // Initialize the inner map if subject is encountered for the first time
                                    if (!subjectResults.containsKey(subject)) {
                                        subjectResults.put(subject, new HashMap<>());
                                    }

                                    // Add marks to the result type for the subject
                                    subjectResults.get(subject).put(resultType, marks);
                                }

                                // Add a row for each subject with each result type
                                for (Map.Entry<String, Map<String, Integer>> entry : subjectResults.entrySet()) {
                                    addTableRow(entry.getKey(), entry.getValue());
                                }
                            } else {
                                Log.e("StudentReportsActivity", "Error getting student results: ", task.getException());
                            }
                        }
                    });
        }


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