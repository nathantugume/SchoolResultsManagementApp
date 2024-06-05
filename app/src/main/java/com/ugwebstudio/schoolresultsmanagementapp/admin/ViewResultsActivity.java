package com.ugwebstudio.schoolresultsmanagementapp.admin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ugwebstudio.schoolresultsmanagementapp.R;
import com.ugwebstudio.schoolresultsmanagementapp.classes.InputFilterMinMax;
import com.ugwebstudio.schoolresultsmanagementapp.classes.SubjectResult;
import com.ugwebstudio.schoolresultsmanagementapp.teacher.ManageResultsActivity;

import java.util.ArrayList;
import java.util.List;

public class ViewResultsActivity extends AppCompatActivity {

    private Spinner spinnerYear, spinnerClass, spinnerTerm, spinnerStudentName;
    private TableLayout tableLayoutSubjects;
    private FirebaseFirestore db;
    private ExtendedFloatingActionButton buttonUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_results);

        initViews();
        setupToolbar();
        setupBottomNavigation();
        setupSpinners();
        loadSubjects();

        db = FirebaseFirestore.getInstance();

        buttonUpdate.setOnClickListener(view -> {
            String documentId = getSelectedStudentId();
            StudentResultData resultData = collectResultsFromTable();
            updateResultsInFirestore(documentId, resultData);
        });
    }

    private void initViews() {
        spinnerYear = findViewById(R.id.year);
        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerTerm = findViewById(R.id.spinnerTerm);
        spinnerStudentName = findViewById(R.id.spinnerStudentName);
        tableLayoutSubjects = findViewById(R.id.tableLayoutSubjects);
        buttonUpdate = findViewById(R.id.buttonAddResult);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_app_bar);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home){
                startActivity(new Intent(ViewResultsActivity.this, MainActivity.class));
            }
            if (item.getItemId() == R.id.bottom_report){
                startActivity(new Intent(ViewResultsActivity.this, StudentReportActivity.class));

            }
            if (item.getItemId() == R.id.bottom_classes){
                startActivity(new Intent(ViewResultsActivity.this, ManageClassesActivity.class));

            }
            if (item.getItemId() == R.id.bottom_results){
                startActivity(new Intent(ViewResultsActivity.this, ManageResultsActivity.class));

            }
            return true;
        });
    }

    private void setupSpinners() {
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.year_array));
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.classes_array));
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);

        ArrayAdapter<String> termAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.terms_array));
        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTerm.setAdapter(termAdapter);

        spinnerYear.setOnItemSelectedListener(new SpinnerListener());
        spinnerClass.setOnItemSelectedListener(new SpinnerListener());
        spinnerStudentName.setOnItemSelectedListener(new StudentSpinnerListener());
    }

    private class SpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            fetchStudents();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    private class StudentSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            fetchResultsForSelectedStudent();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    private void loadSubjects() {
        String[] subjects = getResources().getStringArray(R.array.o_level_subs);
        int studentCount = 10;

        for (String subject : subjects) {
            TableRow row = new TableRow(this);

            TextView subjectText = new TextView(this);
            subjectText.setText(subject.length() >= 3 ? subject.substring(0, 3) : subject);
            row.addView(subjectText);

            double[] subjectScores = new double[studentCount];
            for (int i = 0; i < studentCount; i++) {
                EditText editText = new EditText(this);
                editText.setHint(" ");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                editText.setFilters(new InputFilter[]{new InputFilterMinMax(0.9, 3.0)});

                final int index = i;
                editText.addTextChangedListener(new ScoreTextWatcher(row, subjectScores, index));
                row.addView(editText);
            }

            TextView totalView = new TextView(this);
            totalView.setText("0.0");
            row.addView(totalView);

            TextView averageView = new TextView(this);
            averageView.setText("0.0");
            row.addView(averageView);

            tableLayoutSubjects.addView(row);
        }
    }

    private class ScoreTextWatcher implements TextWatcher {
        private final TableRow row;
        private final double[] subjectScores;
        private final int index;

        public ScoreTextWatcher(TableRow row, double[] subjectScores, int index) {
            this.row = row;
            this.subjectScores = subjectScores;
            this.index = index;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s.toString())) {
                subjectScores[index] = Double.parseDouble(s.toString());
            } else {
                subjectScores[index] = 0.0;
            }
            updateTotalAndAverage(row, subjectScores);
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateTotalAndAverage(TableRow row, double[] scores) {
        double total = 0;
        for (double score : scores) {
            total += score;
        }

        double average = total / scores.length;

        TextView totalView = (TextView) row.getChildAt(row.getChildCount() - 2);
        TextView averageView = (TextView) row.getChildAt(row.getChildCount() - 1);

        totalView.setText(String.format("%.1f", total));
        averageView.setText(String.format("%.1f", average));
    }

    private void fetchStudents() {
        String selectedClass = spinnerClass.getSelectedItem().toString();
        String selectedYear = spinnerYear.getSelectedItem().toString();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading students...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("students")
                .whereEqualTo("studentClass", selectedClass)
                .whereEqualTo("academicYear", selectedYear)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        List<String> studentNames = new ArrayList<>();
                        List<String> studentIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            studentNames.add(document.getString("name"));
                            studentIds.add(document.getId());
                        }
                        updateStudentSpinner(studentNames, studentIds);
                    } else {
                        Toast.makeText(ViewResultsActivity.this, "Failed to fetch students", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchResultsForSelectedStudent() {
        String selectedStudentId = getSelectedStudentId();
        if (selectedStudentId == null) {
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading results...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("results")
                .whereEqualTo("studentId", selectedStudentId)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Log.d(TAG, "Document ID: " + document.getId());
                                StudentResultData resultData = document.toObject(StudentResultData.class);
                                Log.d(TAG, "Student ID: " + resultData.getStudentId());
                                Log.d(TAG, "Student Name: " + resultData.getStudent());
                                // Log other properties as needed
                                populateResultsInTable(resultData);
                            }
                        } else {
                            Toast.makeText(ViewResultsActivity.this, "No results found for the selected student", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ViewResultsActivity.this, "Error fetching results: " + task.getException(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching results: ", task.getException());
                    }
                });


    }

    private void populateResultsInTable(StudentResultData resultData) {
        for (int i = 0; i < tableLayoutSubjects.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);
            TextView subjectText = (TextView) row.getChildAt(0);
            String subject = subjectText.getText().toString();

            for (SubjectResult result : resultData.getResults()) {
                if (result.getSubject().equals(subject)) {
                    List<Double> scores = result.getScores();
                    for (int j = 0; j < scores.size(); j++) {
                        EditText scoreInput = (EditText) row.getChildAt(j + 1);
                        scoreInput.setText(String.valueOf(scores.get(j)));
                    }
                    updateTotalAndAverage(row, scores.stream().mapToDouble(d -> d).toArray());
                    break;
                }
            }
        }
    }


    private void updateStudentSpinner(List<String> studentNames, List<String> studentIds) {
        ArrayAdapter<String> studentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentNames);
        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStudentName.setAdapter(studentAdapter);
        spinnerStudentName.setTag(studentIds);
    }

    private void updateResultsInFirestore(String documentId, StudentResultData resultData) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating results...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        db.collection("results")
                .document(documentId)
                .set(resultData)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(ViewResultsActivity.this, "Results updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ViewResultsActivity.this, "Failed to update results", Toast.LENGTH_SHORT).show();
                });
    }

    private String getSelectedStudentId() {
        List<String> studentIds = (List<String>) spinnerStudentName.getTag();
        if (studentIds != null && !studentIds.isEmpty()) {
            int selectedPosition = spinnerStudentName.getSelectedItemPosition();
            return studentIds.get(selectedPosition);
        }
        return null;
    }

    // Define a nested class for student result data
    public static class StudentResultData {

        private String studentId;
        private String student;
        private String subject;
        private String term;
        private double marks;
        private String resultType;
        private String studentClass; // Renamed from class to studentClass

        public String getStudentId() {
            return studentId;
        }

        public void setStudentId(String studentId) {
            this.studentId = studentId;
        }

        public String getStudent() {
            return student;
        }

        public void setStudent(String student) {
            this.student = student;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getTerm() {
            return term;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        public double getMarks() {
            return marks;
        }

        public void setMarks(double marks) {
            this.marks = marks;
        }

        public String getResultType() {
            return resultType;
        }

        public void setResultType(String resultType) {
            this.resultType = resultType;
        }

        public String getStudentClass() {
            return studentClass;
        }

        public void setStudentClass(String studentClass) {
            this.studentClass = studentClass;
        }

        private List<SubjectResult> results;

        public StudentResultData() {
            results = new ArrayList<>();
        }

        public void addResult(SubjectResult result) {
            results.add(result);
        }

        public List<SubjectResult> getResults() {
            return results;
        }

        public void setResults(List<SubjectResult> results) {
            this.results = results;
        }
    }

    private StudentResultData collectResultsFromTable() {
        StudentResultData resultData = new StudentResultData();

        for (int i = 0; i < tableLayoutSubjects.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayoutSubjects.getChildAt(i);
            TextView subjectText = (TextView) row.getChildAt(0);
            String subject = subjectText.getText().toString();

            List<Double> scores = new ArrayList<>();
            for (int j = 1; j < row.getChildCount() - 2; j++) {
                EditText scoreInput = (EditText) row.getChildAt(j);
                double score = Double.parseDouble(scoreInput.getText().toString());
                scores.add(score);
            }

            resultData.addResult(new SubjectResult(subject, scores));
        }

        return resultData;
    }
}
