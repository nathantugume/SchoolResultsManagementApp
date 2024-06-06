package com.ugwebstudio.schoolresultsmanagementapp.admin;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.SubjectResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewCurriculumReport extends AppCompatActivity {

    private TableLayout tableLayoutResults;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_curriculum_report);

        tableLayoutResults = findViewById(R.id.tableLayoutResults);
        db = FirebaseFirestore.getInstance();

        // Get intent extras
        String selectedClass = getIntent().getStringExtra("selectedClass");
        String selectedTerm = getIntent().getStringExtra("selectedTerm");
        String studentId = getIntent().getStringExtra("studentId");

        // Fetch and display results
        fetchAndDisplayResults(selectedClass, selectedTerm, studentId);
    }

    private void fetchAndDisplayResults(String selectedClass, String selectedTerm, String studentId) {
        db.collection("results")
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("className", selectedClass)
                .whereEqualTo("term", selectedTerm)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (result != null && !result.isEmpty()) {
                            for (DocumentSnapshot document : result) {
                                List<HashMap<String, Object>> results = (List<HashMap<String, Object>>) document.get("results");
                                List<SubjectResult> subjectResults = new ArrayList<>();
                                for (HashMap<String, Object> map : results) {
                                    String subject = (String) map.get("subject");
                                    List<Double> scores = (List<Double>) map.get("scores");
                                    subjectResults.add(new SubjectResult(subject, scores));
                                }

                                String studentName = document.getString("studentName");
                                String year = document.getString("year");

                                displayResults(subjectResults, studentName, selectedClass, year, selectedTerm);
                            }
                        } else {
                            Toast.makeText(NewCurriculumReport.this, "No results found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(NewCurriculumReport.this, "Failed to fetch results", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayResults(List<SubjectResult> subjectResults, String studentName, String selectedClass, String year, String selectedTerm) {
        if (subjectResults == null || subjectResults.isEmpty()) {
            return;
        }

        // Add school header with logo
        TableRow schoolHeaderRow = new TableRow(this);
        schoolHeaderRow.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView logoImageView = new ImageView(this);
        logoImageView.setImageResource(R.mipmap.ic_logo_round);
        logoImageView.setForegroundGravity(Gravity.CENTER_HORIZONTAL);
        schoolHeaderRow.addView(logoImageView);

        TextView schoolNameHeader = new TextView(this);
        schoolNameHeader.setText(R.string.result_mastery);
        schoolNameHeader.setGravity(Gravity.CENTER);
        schoolHeaderRow.addView(schoolNameHeader);

        tableLayoutResults.addView(schoolHeaderRow);

        TableRow schoolLocationRow = new TableRow(this);
        schoolLocationRow.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView schoolLocationHeader = new TextView(this);
        schoolLocationHeader.setText(R.string.location);
        schoolLocationHeader.setGravity(Gravity.CENTER);
        schoolLocationRow.addView(schoolLocationHeader);

        tableLayoutResults.addView(schoolLocationRow);

        // Add student details header
        TableRow studentDetailsRow = new TableRow(this);
        studentDetailsRow.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView studentDetailsHeader = new TextView(this);
        studentDetailsHeader.setText("Student: " + studentName + " | Class: " + selectedClass + "\n | Year: " + year + " | Term: " + selectedTerm);
        studentDetailsHeader.setGravity(Gravity.CENTER);
        studentDetailsRow.addView(studentDetailsHeader);

        tableLayoutResults.addView(studentDetailsRow);

        // Add table header
        TableRow headerRow = new TableRow(this);
        headerRow.setGravity(Gravity.CENTER_HORIZONTAL);
        headerRow.setPadding(0, 5, 0, 5);

        TextView subjectHeader = new TextView(this);
        subjectHeader.setText("Subject");
        subjectHeader.setGravity(Gravity.CENTER);
        headerRow.addView(subjectHeader);

        for (int i = 0; i < 10; i++) {
            TextView studentHeader = new TextView(this);
            studentHeader.setText("C" + (i + 1));
            studentHeader.setGravity(Gravity.CENTER);
            headerRow.addView(studentHeader);
        }

        TextView totalHeader = new TextView(this);
        totalHeader.setText("/20");
        totalHeader.setGravity(Gravity.CENTER);
        headerRow.addView(totalHeader);

        TextView averageHeader = new TextView(this);
        averageHeader.setText("Ave");
        averageHeader.setGravity(Gravity.CENTER);
        headerRow.addView(averageHeader);

        tableLayoutResults.addView(headerRow);

        // Add table rows for each subject
        for (SubjectResult subjectResult : subjectResults) {
            TableRow row = new TableRow(this);
            row.setGravity(Gravity.CENTER_HORIZONTAL);
            row.setPadding(0, 5, 0, 5);

            TextView subjectName = new TextView(this);
            subjectName.setText(subjectResult.getSubject());
            subjectName.setGravity(Gravity.CENTER);
            row.addView(subjectName);

            List<Double> scoreList = subjectResult.getScores();
            double[] scores = convertListToArray(scoreList);
            double total = 0;

            for (double score : scores) {
                TextView scoreView = new TextView(this);
                scoreView.setText(String.valueOf(score));
                scoreView.setGravity(Gravity.CENTER);
                row.addView(scoreView);
                total += score;
            }

            TextView totalView = new TextView(this);
            totalView.setText(String.valueOf(total));
            totalView.setGravity(Gravity.CENTER);
            row.addView(totalView);

            double average = total / scores.length;
            TextView averageView = new TextView(this);
            averageView.setText(String.valueOf(average));
            averageView.setGravity(Gravity.CENTER);
            row.addView(averageView);

            tableLayoutResults.addView(row);
        }
    }

    private double[] convertListToArray(List<Double> scoreList) {
        double[] scores = new double[scoreList.size()];
        for (int i = 0; i < scoreList.size(); i++) {
            scores[i] = scoreList.get(i);
        }
        return scores;
    }


}
