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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ViewReportActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private FirebaseFirestore db;
    private TextView studentNameTxt;
    private static final Map<String, Integer> gradePoints = new HashMap<>();
    private float average;
    static {
        gradePoints.put("A", 1);
        gradePoints.put("B", 2);
        gradePoints.put("C", 3);
        gradePoints.put("D", 4);
        gradePoints.put("E", 5);
        gradePoints.put("O", 6);
        gradePoints.put("F", 7);
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
        TextView gradeTextView = createTextView(calculateGrades(getMarks(resultTypes, "Beginning of Term"), getMarks(resultTypes, "Midterm"), getMarks(resultTypes, "End of Term")));

        // Add EditTexts and TextView to the row
        row.addView(beginningEditText);
        row.addView(midtermEditText);
        row.addView(endEditText);
        row.addView(gradeTextView);

        // Add the row to the TableLayout
        tableLayout.addView(row);
    }

    private String getMarksString(Map<String, Integer> resultTypes, String resultType) {
        return resultTypes.containsKey(resultType) ? String.valueOf(resultTypes.get(resultType)) : "-";
    }

    private int getMarks(Map<String, Integer> resultTypes, String resultType) {
        return resultTypes.getOrDefault(resultType, 0);
    }

//    private String calculateGrade(int beginningMarks, int midMarks, int endMarks) {
//        // Assume this method will use the fetched grading scale from Firestore to calculate the grade
//        return "A"; // Placeholder for the actual grade calculation logic
//    }

    private void setRowBackgroundColor(TableRow row, int childCount) {
        if (childCount % 2 == 0) {
            row.setBackgroundColor(ContextCompat.getColor(this, R.color.row_color_even));
        } else {
            row.setBackgroundColor(ContextCompat.getColor(this, R.color.row_color_odd));
        }
    }






    private String calculateGrades(int beginningMarks, int midMarks, int endMarks) {



        float average = ((float) beginningMarks + (float) midMarks + (float) endMarks) / 3;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("gradingScales")
                .orderBy("from")
                .get()
                .addOnCompleteListener(task -> {
                    String[] gra;
                    if (task.isSuccessful()) {
                        String[] grades = {"Not Graded"};
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (Objects.requireNonNull(document.getString("level")).equalsIgnoreCase("o_level")){
                                float minMark = document.getLong("from");
                                float maxMark = document.getLong("to");
                                if (average >= minMark && average <= maxMark) {
                                    grades[0] = document.getString("grade");
                                    break; // Found the correct grade, exit loop
                                }

                            }

                        }
                        // Now you have the grade, you can calculate the aggregate if needed
                        int aggregate = calculateAggregate(grades);
                         gra = grades;

                        Log.d("calculateGrades", "Average: " + average + ", Grade: " + grades[0] + ", Aggregate: " + aggregate);

                    } else {
                        Log.w("calculateGrades", "Error getting documents.", task.getException());
                    }

                });
        return "null";
    }

    private int calculateAggregate(String[] grades) {
        // Implementation depends on how you map grades to points or other aggregate calculations
        int aggregate = 0;
        for (String grade : grades) {
            if (gradePoints.containsKey(grade)) {
                aggregate += gradePoints.get(grade);
            } else {
                Log.e("calculateAggregate", "Invalid grade: " + grade);
            }
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
}