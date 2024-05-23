package com.ugwebstudio.schoolresultsmanagementapp.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
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

public class StudentResultsActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private FirebaseFirestore db;
    private TextView studentNameTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_results);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_app_bar);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home){
                startActivity(new Intent(StudentResultsActivity.this, MainActivity.class));
            }
            if (item.getItemId() == R.id.bottom_report){
                startActivity(new Intent(StudentResultsActivity.this, StudentReportActivity.class));

            }
            if (item.getItemId() == R.id.bottom_classes){
                startActivity(new Intent(StudentResultsActivity.this, ManageClassesActivity.class));

            }
            if (item.getItemId() == R.id.bottom_results){
                startActivity(new Intent(StudentResultsActivity.this, ManageResultsActivity.class));

            }

            return false;
        });

        // Initialize Firebase Firestore
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
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                StudentResults studentResults = document.toObject(StudentResults.class);

                                studentNameTxt.setText(studentResults.getStudent());

                                // Get subject and result type from Firestore document
                                String subject = document.getString("subject");
                                String resultType = document.getString("resultType");

                                // Add a row to the table for each result
                                addTableRow(subject, resultType,studentResults.getMarks());
                            }
                        } else {
                            Log.e("StudentResultsActivity", "Error getting student results: ", task.getException());
                        }
                    }
                });
    }

    private void addTableRow(String subject, String resultType, int marks) {
        TableRow row = new TableRow(this);


        // Add background color to alternate rows for stripe effect
        if (tableLayout.getChildCount() % 2 == 0) {
            row.setBackgroundColor(ContextCompat.getColor(this, R.color.row_color_even));
        } else {
            row.setBackgroundColor(ContextCompat.getColor(this, R.color.row_color_odd));
        }

        String beginning = resultType.equalsIgnoreCase("Beginning of Term") ? String.valueOf(marks) : "";
        String mid = resultType.equalsIgnoreCase("Midterm") ? String.valueOf(marks) : "-";
        String end = resultType.equalsIgnoreCase("End of Term") ? String.valueOf(marks) : "-";


        // Create TextViews for subject and result type
        TextView subjectTextView = createTextView(subject);
        EditText beginningTextView = createEditText(beginning);
        EditText midtermTextView = createEditText(mid);
        EditText endTextView = createEditText(end);


        // Add TextViews to the TableRow
        row.addView(subjectTextView);
        row.addView(beginningTextView);
        row.addView(midtermTextView);
        row.addView(endTextView);

        // Add TableRow to the TableLayout
        tableLayout.addView(row);
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
